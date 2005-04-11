/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body.request;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.event.RequestQueueEvent;
import org.objectweb.proactive.core.group.spmd.MethodBarrier;
import org.objectweb.proactive.core.group.spmd.MethodCallBarrierWithMethodName;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManager;
import org.objectweb.proactive.core.mop.MethodCall;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


public class BlockingRequestQueueImpl extends RequestQueueImpl
    implements java.io.Serializable, BlockingRequestQueue {
    public static Logger logger = Logger.getLogger(BlockingRequestQueueImpl.class.getName());

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected boolean shouldWait;
    private transient ProActiveSPMDGroupManager spmdManager = null;
    private boolean firstBarrierCallEncountered = false;
    private boolean suspended = false;
    private boolean specialExecution = false;
    private String specialMethod = "";
    private int awaitedBarrierCall = 0;
    private HashMap currentBarriers = new HashMap();
    private LinkedList methodBarriers = new LinkedList();

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
            Iterator it = this.methodBarriers.iterator();
            boolean methodFound = false;
            MethodBarrier mb;
            int i = 1;
            while (it.hasNext() && !methodFound) {
                mb = (MethodBarrier) it.next();
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

    public synchronized int addToFront(Request r) {
        int ftres = super.addToFront(r);
        this.notifyAll();
        return ftres;
    }

    public synchronized Request blockingRemoveOldest(
        RequestFilter requestFilter) {
        return blockingRemove(requestFilter, true);
    }

    public synchronized Request blockingRemoveOldest(String methodName) {
        return blockingRemove(methodName, true);
    }

    public synchronized Request blockingRemoveOldest() {
        if (this.spmdManager == null) {
            this.spmdManager = ((AbstractBody) ProActive.getBodyOnThis()).getProActiveSPMDGroupManager();
        }
        return this.barrierBlockingRemove();
    }

    public synchronized Request blockingRemoveOldest(long timeout) {
        return blockingRemove(timeout, true);
    }

    public synchronized Request blockingRemoveYoungest(
        RequestFilter requestFilter) {
        return blockingRemove(requestFilter, false);
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

    public synchronized void waitForRequest() {
        while (isEmpty() && shouldWait) {
            if (hasListeners()) {
                notifyAllListeners(new RequestQueueEvent(ownerID,
                        RequestQueueEvent.WAIT_FOR_REQUEST));
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    protected Request blockingRemove(RequestFilter requestFilter, boolean oldest) {
        Request r = oldest ? removeOldest(requestFilter)
                           : removeYoungest(requestFilter);
        while ((r == null) && shouldWait) {
            if (hasListeners()) {
                notifyAllListeners(new RequestQueueEvent(ownerID,
                        RequestQueueEvent.WAIT_FOR_REQUEST));
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
            r = oldest ? removeOldest(requestFilter)
                       : removeYoungest(requestFilter);
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
        Request r = oldest ? removeOldest(methodName) : removeYoungest(methodName);
        while ((r == null) && shouldWait) {
            if (hasListeners()) {
                notifyAllListeners(new RequestQueueEvent(ownerID,
                        RequestQueueEvent.WAIT_FOR_REQUEST));
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
            r = oldest ? removeOldest(methodName) : removeYoungest(methodName);
        }
        return r;
    }

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param oldest true if the request to remove is the oldest, false for the youngest
     * @return the request found in the queue.
     */
    protected Request blockingRemove(boolean oldest) {
        while (isEmpty() && shouldWait) {
            if (hasListeners()) {
                notifyAllListeners(new RequestQueueEvent(ownerID,
                        RequestQueueEvent.WAIT_FOR_REQUEST));
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return oldest ? removeOldest() : removeYoungest();
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
        long timeStartWaiting = System.currentTimeMillis();
        while (isEmpty() && shouldWait) {
            if (hasListeners()) {
                notifyAllListeners(new RequestQueueEvent(ownerID,
                        RequestQueueEvent.WAIT_FOR_REQUEST));
            }
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
            }
            if ((System.currentTimeMillis() - timeStartWaiting) > timeout) {
                return oldest ? removeOldest() : removeYoungest();
            }
        }
        return oldest ? removeOldest() : removeYoungest();
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
            if (this.hasListeners()) {
                this.notifyAllListeners(new RequestQueueEvent(this.ownerID,
                        RequestQueueEvent.WAIT_FOR_REQUEST));
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        if (this.specialExecution) {
            this.specialExecution = false;
            return this.blockingRemoveOldest(this.specialMethod);
        }
        return this.barrierRemoveOldest();
    }

    public Request barrierRemoveOldest() {
        Request r = (Request) requestQueue.remove(this.indexOfRequestToServe());
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            this.notifyAllListeners(new RequestQueueEvent(this.ownerID,
                    RequestQueueEvent.REMOVE_REQUEST));
        }
        return r;
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
                if (mc==null){
                    return -1;
                }
                isServable = this.spmdManager.checkExecution(mc.getBarrierTags());
            }
            return isServable ? index : (-1);
        }
    }
}
