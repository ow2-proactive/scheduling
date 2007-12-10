/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body.request;

import java.util.Iterator;
import java.util.LinkedList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.event.RequestQueueEvent;
import org.objectweb.proactive.core.group.spmd.MethodBarrier;
import org.objectweb.proactive.core.group.spmd.MethodCallBarrierWithMethodName;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManager;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.TimeoutAccounter;
import org.objectweb.proactive.core.util.profiling.Profiling;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;


public class BlockingRequestQueueImpl extends RequestQueueImpl implements java.io.Serializable,
    BlockingRequestQueue {
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected boolean shouldWait;
    private transient ProActiveSPMDGroupManager spmdManager = null;
    private boolean suspended = false;
    private boolean specialExecution = false;
    private String specialMethod = "";
    private LinkedList<MethodBarrier> methodBarriers = new LinkedList<MethodBarrier>();
    private boolean waitingForRequest = false;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public BlockingRequestQueueImpl(UniqueID ownerID) {
        super(ownerID);
        shouldWait = true;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public synchronized void destroy() {
        super.clear();
        shouldWait = false;
        notifyAll();
    }

    public synchronized boolean isDestroyed() {
        return !shouldWait;
    }

    @Override
    public synchronized int add(Request r) {
        int ftres = super.add(r);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding request " + r.getMethodName());
        }

        // FAULT-TOLERANCE
        // STILL NOT OOSPMD COMPLIANT !
        if (r instanceof AwaitedRequest) {
            this.notifyAll();
            return ftres;
        }

        // if there is a "method based barrier"
        if (this.methodBarriers.size() != 0) {
            Iterator<MethodBarrier> it = this.methodBarriers.iterator();
            boolean methodFound = false;
            MethodBarrier mb;
            while (it.hasNext() && !methodFound) {
                mb = it.next();
                methodFound = mb.checkMethod(r.getMethodName());
                if (methodFound) {
                    if (mb.barrierOver()) {
                        it.remove();
                        if (this.methodBarriers.size() == 0) {
                            this.resume();
                        }
                    }
                    this.specialMethod = r.getMethodName();
                    this.specialExecution = true;
                }
            }
        }

        // a "method based barrier" => stop the activity of this AO
        if (r.getMethodCall() instanceof MethodCallBarrierWithMethodName) {
            MethodCallBarrierWithMethodName mcbwmn = (MethodCallBarrierWithMethodName) r.getMethodCall();
            this.methodBarriers.add(new MethodBarrier(mcbwmn.getMethodNames()));
            this.suspend();
        }
        this.notifyAll();
        return ftres;
    }

    @Override
    public synchronized int addToFront(Request r) {
        int ftres = super.addToFront(r);
        this.notifyAll();
        return ftres;
    }

    public synchronized Request blockingRemoveOldest(
        RequestFilter requestFilter) {
        return blockingRemove(requestFilter, true, 0);
    }

    public synchronized Request blockingRemoveOldest(
        RequestFilter requestFilter, long timeout) {
        return blockingRemove(requestFilter, true, timeout);
    }

    public synchronized Request blockingRemoveOldest(String methodName) {
        requestFilterOnMethodName.setMethodName(methodName);
        return blockingRemove(requestFilterOnMethodName, true, 0);
    }

    public synchronized Request blockingRemoveOldest() {
        return blockingRemove(null, true, 0);
    }

    public synchronized Request blockingRemoveOldest(long timeout) {
        return blockingRemove(null, true, timeout);
    }

    public synchronized Request blockingRemoveYoungest(
        RequestFilter requestFilter) {
        return blockingRemove(requestFilter, false);
    }

    public synchronized Request blockingRemoveYoungest(
        RequestFilter requestFilter, long timeout) {
        return blockingRemove(requestFilter, false, timeout);
    }

    public synchronized Request blockingRemoveYoungest(String methodName) {
        return blockingRemove(methodName, false);
    }

    public synchronized Request blockingRemoveYoungest() {
        return blockingRemove(false);
    }

    public synchronized Request blockingRemoveYoungest(long timeout) {
        return blockingRemove(timeout, false);
    }

    public synchronized boolean isWaitingForRequest() {
        return waitingForRequest;
    }

    /**
     * Does not check for pending requests before waiting
     */
    private synchronized void internalWaitForRequest(long timeout) {
        if (Profiling.TIMERS_COMPILED) {
            TimerWarehouse.startTimer(this.ownerID,
                TimerWarehouse.WAIT_FOR_REQUEST);
        }

        // ProActiveEvent
        if (hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID,
                    RequestQueueEvent.WAIT_FOR_REQUEST));
        }

        // END ProActiveEvent

        // JMX Notification
        Body body = LocalBodyStore.getInstance().getLocalBody(ownerID);
        if (body != null) {
            BodyWrapperMBean mbean = body.getMBean();
            if (mbean != null) {
                mbean.sendNotification(NotificationType.waitForRequest);
            }
        }

        // END JMX Notification
        try {
            this.waitingForRequest = timeout == 0;
            this.wait(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.waitingForRequest = false;
            // THIS CODE IS NEVER EXECUTED IF THE ACTIVE OBJECT IS TERMINATED
            if (Profiling.TIMERS_COMPILED) {
                TimerWarehouse.stopTimer(this.ownerID,
                    TimerWarehouse.WAIT_FOR_REQUEST);
            }
        }
    }

    /**
     * User API: checks for pending requests before waiting
     */
    public synchronized void waitForRequest(long timeout) {
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);

        while (isEmpty() && shouldWait && !time.isTimeoutElapsed()) {
            internalWaitForRequest(time.getRemainingTimeout());
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    protected Request blockingRemove(RequestFilter requestFilter, boolean oldest) {
        return blockingRemove(requestFilter, oldest, 0);
    }

    protected Request blockingRemove(RequestFilter requestFilter,
        boolean oldest, long timeout) {
        if (oldest && (requestFilter == null) && (timeout == 0)) {
            if (this.spmdManager == null) {
                this.spmdManager = ((AbstractBody) PAActiveObject.getBodyOnThis()).getProActiveSPMDGroupManager();
            }
            if (!spmdManager.isCurrentBarriersEmpty()) {
                return this.barrierBlockingRemove(); // the oospmd way ...
            }
        }

        long timeStartWaiting = 0;
        if (timeout > 0) {
            timeStartWaiting = System.currentTimeMillis();
        }
        Request r = oldest
            ? ((requestFilter == null) ? removeOldest()
                                       : removeOldest(requestFilter))
            : ((requestFilter == null) ? removeYoungest()
                                       : removeYoungest(requestFilter));
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        while ((r == null) && shouldWait && !time.isTimeoutElapsed()) {
            internalWaitForRequest(time.getRemainingTimeout());
            r = oldest
                ? ((requestFilter == null) ? removeOldest()
                                           : removeOldest(requestFilter))
                : ((requestFilter == null) ? removeYoungest()
                                           : removeYoungest(requestFilter));
            if ((timeout != 0) &&
                    ((System.currentTimeMillis() - timeStartWaiting) > timeout)) {
                // force return when timeout exceeded
                return r;
            }
        }
        return r;
    }

    /**
     * Blocks the calling thread until there is a request of name methodName
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param methodName the name of the method to wait for
     * @param oldest true if the request to remove is the oldest, false for the youngest
     * @return the request of name methodName found in the queue.
     */
    protected Request blockingRemove(String methodName, boolean oldest) {
        requestFilterOnMethodName.setMethodName(methodName);
        return blockingRemove(requestFilterOnMethodName, oldest, 0);
    }

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param oldest true if the request to remove is the oldest, false for the youngest
     * @return the request found in the queue.
     */
    protected Request blockingRemove(boolean oldest) {
        return blockingRemove(null, oldest, 0);
    }

    /**
     * Blocks the calling thread until there is a request available but try
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null if a request has been found during the given time.
     * @param timeout the maximum time to wait
     * @param oldest true if the request to remove is the oldest, false for the youngest
     * @return the request found in the queue or null.
     */
    protected Request blockingRemove(long timeout, boolean oldest) {
        return blockingRemove(null, oldest, timeout);
    }

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @return the request found in the queue.
     */
    protected Request barrierBlockingRemoveOldest(long timeout) {
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        while (((this.isEmpty() && this.shouldWait) || this.suspended ||
                (this.indexOfRequestToServe() == -1)) &&
                !this.specialExecution) {
            if (time.isTimeoutElapsed()) {
                return removeOldest();
            }
            internalWaitForRequest(time.getRemainingTimeout());
        }
        if (specialExecution) {
            specialExecution = false;
            return blockingRemoveOldest(specialMethod);
        }
        return barrierRemoveOldest();
    }

    protected Request barrierRemoveOldest() {
        Request r = (Request) requestQueue.remove(indexOfRequestToServe());

        // ProActiveEvent
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID,
                    RequestQueueEvent.REMOVE_REQUEST));
        }

        // END ProActiveEvent
        return r;
    }

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @return the request found in the queue.
     */
    protected Request barrierBlockingRemove() {
        while (((this.isEmpty() && this.shouldWait) || this.suspended ||
                (this.indexOfRequestToServe() == -1)) &&
                !this.specialExecution) {
            internalWaitForRequest(0);
        }
        if (this.specialExecution) {
            this.specialExecution = false;
            return this.blockingRemoveOldest(this.specialMethod);
        }
        return this.barrierRemoveOldest();
    }

    /**
     * Blocks the service of requests.
     * Incoming requests are still added in queue.
     */
    public void suspend() {
        this.suspended = true;
    }

    /**
     * Resumes the service of requests.
     */
    public void resume() {
        this.suspended = false;
    }

    /**
     * Returns the index of the first servable request in the requestQueue
     * @return the index of the first servable request in the requestQueue, -1 if there is no request to serve
     */
    private int indexOfRequestToServe() {
        // if there is no barrier currently active, avoid the iteration
        if (this.spmdManager.isCurrentBarriersEmpty()) {
            return 0;
        } else { // there is at least one active barrier
            int index = -1;
            boolean isServable = false;
            Iterator it = this.requestQueue.iterator();

            // look for the first request in the queue we can serve
            while (!isServable && it.hasNext()) {
                index++;
                MethodCall mc = ((Request) it.next()).getMethodCall();

                // FT : mc could be an awaited request
                if (mc == null) {
                    return -1;
                }
                isServable = this.spmdManager.checkExecution(mc.getBarrierTags());
            }
            return isServable ? index : (-1);
        }
    }
}
