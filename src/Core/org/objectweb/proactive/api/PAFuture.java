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
package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.TimeoutAccounter;


/**
 * This class provides wait primitives on a future or on a Vector of futures. It also provides an
 * event mechanism that allows to associate a method execution with a future update. Finally, it
 * also provides a way to explicitely monitor an active object from which a result is awaited.
 * 
 * @author The ProActive Team
 * @since ProActive 3.9 (December 2007)
 */
@PublicAPI
public class PAFuture {

    /**
     * This negative value is returned by waitForAny(java.util.Vector futures) if the parameter
     * futures is an empty collection.
     */
    public static final int INVALID_EMPTY_COLLECTION = -1337;

    /**
     * Return the object contains by the future (ie its target). If parameter is not a future, it is
     * returned. A wait-by-necessity occurs if future is not available. This method is recursive,
     * i.e. if result of future is a future too, <CODE>getFutureValue</CODE> is called again on
     * this result, and so on.
     */
    public static Object getFutureValue(Object future) {
        while (true) {
            // If the object is not reified, it cannot be a future
            if ((MOP.isReifiedObject(future)) == false) {
                return future;
            } else {
                org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

                // If it is reified but its proxy is not of type future, we cannot wait
                if (!(theProxy instanceof Future)) {
                    return future;
                } else {
                    future = ((Future) theProxy).getResult();
                }
            }
        }
    }

    /**
     * Return false if the object <code>future</code> is available. This method is recursive, i.e.
     * if result of future is a future too, <CODE>isAwaited</CODE> is called again on this result,
     * and so on.
     */
    public static boolean isAwaited(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return false;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return false;
            } else {
                if (((Future) theProxy).isAwaited()) {
                    return true;
                } else {
                    return isAwaited(((Future) theProxy).getResult());
                }
            }
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code> is available.
     * <code>future</code> must be the result object of an asynchronous call. Usually the the wait
     * by necessity model take care of blocking the caller thread asking for a result not yet
     * available. This method allows to block before the result is first used.
     * 
     * @param future
     *            object to wait for
     */
    public static void waitFor(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return;
            } else {
                ((Future) theProxy).waitFor();
            }
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code> is available or until the
     * timeout expires. <code>future</code> must be the result object of an asynchronous call.
     * Usually the the wait by necessity model take care of blocking the caller thread asking for a
     * result not yet available. This method allows to block before the result is first used.
     * 
     * @param future
     *            object to wait for
     * @param timeout
     *            to wait in ms
     * @throws ProActiveException
     *             if the timeout expire
     */
    public static void waitFor(Object future, long timeout) throws ProActiveTimeoutException {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return;
            } else {
                ((Future) theProxy).waitFor(timeout);
            }
        }
    }

    /**
     * Blocks the calling thread until all futures in the vector are available.
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     * @param futures
     *            a collection of futures
     */
    public static void waitForAll(java.util.Vector futures) {
        try {
            PAFuture.waitForAll(futures, 0);
        } catch (ProActiveTimeoutException e) {
            //Exception above should never be thrown since timeout=0 means no timeout
            e.printStackTrace();
        }
    }

    /**
     * Blocks the calling thread until all futures in the vector are available or until the timeout
     * expires.
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     * @param futures
     *            a collection of futures
     * @param timeout
     *            to wait in ms
     * @throws ProActiveException
     *             if the timeout expires
     */
    public static void waitForAll(java.util.Vector futures, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        for (Object future : futures) {
            if (time.isTimeoutElapsed()) {
                throw new ProActiveTimeoutException("Timeout expired while waiting for future update");
            }
            PAFuture.waitFor(future, time.getRemainingTimeout());
        }
    }

    /**
     * Blocks the calling thread until one of the futures in the vector is available.
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     * @param futures
     *            a collection of futures
     * @return index of the available future in the vector
     */
    public static int waitForAny(java.util.Vector futures) {
        try {
            return PAFuture.waitForAny(futures, 0);
        } catch (ProActiveException e) {
            //Exception above should never be thrown since timeout=0 means no timeout
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Blocks the calling thread until one of the futures in the vector is available or until the
     * timeout expires. 
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     *
     * @param futures
     *            a collection of futures
     * @param timeout
     *            to wait in ms
     * @return index of the available future in the vector
     * @throws ProActiveException
     *             if the timeout expires
     */
    public static int waitForAny(java.util.Collection futures, long timeout) throws ProActiveException {
        if (futures.isEmpty()) {

            /*
             * Yes, this return value is meaningless but at least we are not permanently blocked
             */
            return PAFuture.INVALID_EMPTY_COLLECTION;
        }
        FuturePool fp = PAActiveObject.getBodyOnThis().getFuturePool();
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);

        for (Object future : futures) {
            if (PAFuture.isAwaited(future)) {
                monitorFuture(future);
            }
        }

        synchronized (fp) {
            while (true) {
                java.util.Iterator it = futures.iterator();
                int index = 0;

                while (it.hasNext()) {
                    Object current = it.next();

                    if (!PAFuture.isAwaited(current)) {
                        return index;
                    }

                    index++;
                }
                if (time.isTimeoutElapsed()) {
                    throw new ProActiveException("Timeout expired while waiting for future update");
                }
                fp.waitForReply(time.getRemainingTimeout());
            }
        }
    }

    /**
     * Return <code>false</code> if one object of <code>futures</code> is available.
     * 
     * @param futures
     *            a table with futures.
     * @return <code>true</code> if all futures are awaited, else <code>false
     * </code>.
     */
    public static boolean allAwaited(java.util.Vector futures) {
        FuturePool fp = PAActiveObject.getBodyOnThis().getFuturePool();

        synchronized (fp) {
            java.util.Iterator it = futures.iterator();

            while (it.hasNext()) {
                Object current = it.next();

                if (!PAFuture.isAwaited(current)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Register a method in the calling active object to be called when the specified future is
     * updated. The registered method takes a java.util.concurrent.Future as parameter.
     * 
     * @param future
     *            the future to watch
     * @param methodName
     *            the name of the method to call on the current active object
     * @throws IllegalArgumentException
     *             if the first argument is not a future or if the method could not be found
     */
    public static void addActionOnFuture(Object future, String methodName) {
        FutureProxy f;
        try {
            f = (FutureProxy) ((StubObject) future).getProxy();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a future, got a " + future.getClass());
        }

        f.addCallback(methodName);
    }

    /**
     * Add a future to the list of monitored future. This is automatically done when waiting a
     * future. If the active object serving the method for this future cannot be pinged, the future
     * is updated with a RuntimeException.
     * 
     * @param future
     *            the future object to monitor
     */
    public static void monitorFuture(Object future) {
        if (!MOP.isReifiedObject(future)) {
            throw new IllegalArgumentException("Parameter is not a future object (actual type is " +
                future.getClass().getName() + ")");
        }
        FutureProxy fp = (FutureProxy) ((StubObject) future).getProxy();
        FutureMonitoring.monitorFutureProxy(fp);
    }
}
