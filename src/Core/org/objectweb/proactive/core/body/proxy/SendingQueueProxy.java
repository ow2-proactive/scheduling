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
package org.objectweb.proactive.core.body.proxy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.objectweb.proactive.core.body.SendingQueue;


/**
 * This class handles the request to send with a FOS strategy for a specific destination.
 * There is one instance of the class per bodyProxy 
 */
public class SendingQueueProxy {

    private static final int QUEUE_CAPACITY = 10;

    /* The local's body SendingQueue this instance is attached to */
    private SendingQueue sendingQueue;

    /* A cache for the already checked methodNames */
    private HashMap<String, HashSet<List<Class<?>>>> checkedMethodNames;

    /* Contains the methodName which have to be sent with a FOS strategy */
    private Map<String /* methodName */, Object /* parameters */> fosRequests;

    /* Contains the pending RequestToSend, but not the one which is currently in sending */
    private ArrayBlockingQueue<RequestToSend> queue;

    /* Contains the RequestToSend which is currently in sending (not yet acknowledged) */
    private RequestToSend lastSent;

    /* A lock to handle concurrency on sending/ack procedures */
    private ReentrantLock lock;

    // CONSTRUCTOR
    public SendingQueueProxy(SendingQueue sendingQueue) {
        this.checkedMethodNames = new HashMap<String, HashSet<List<Class<?>>>>();
        this.fosRequests = new HashMap<String, Object>();
        this.queue = new ArrayBlockingQueue<RequestToSend>(QUEUE_CAPACITY);
        this.lock = new ReentrantLock();
        this.sendingQueue = sendingQueue;
    }

    /**
     * Invoke this method to delegate the sending of a request.
     * 
     * The <i>RequestToSend</i> instance will immediately be put on the <i>SendingQueue</i> if
     * precedent sending (if there is one) is done. Otherwise, it will be enqueued to the local
     * queue for future sending.
     * 
     * @param rts
     * @throws InterruptedException
     */
    public void put(RequestToSend rts) throws InterruptedException {
        lock.lock();
        if (lastSent == null) { // Precedent sending is done
            sendingQueue.put(rts); // Sending it immediately to the sending queue
            lastSent = rts;

        } else {
            queue.put(rts); // Keep it in local for future sending
        }
        lock.unlock();
    }

    /**
     * Invoked by a <i>RequestToSend</i> instance (from the threadpool) to acknowledge the sending
     * of a request. It causes the sending of the next request to the <i>SendingQueue</i> (if there
     * is one).
     * 
     * Be careful: this method should be called from a threadpool's thread (and not the body's
     * thread).
     */
    public void sendingACK() {
        lock.lock();
        if (!queue.isEmpty()) {
            try {
                RequestToSend rts = queue.take();
                sendingQueue.put(rts);
                lastSent = rts;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            lastSent = null;
            synchronized (this) {
                this.notifyAll();
            }
        }
        lock.unlock();
    }

    /**
     * Causes the current thread to wait until no sending is awaited and no future RequestToSend are
     * in the waiting queue.
     * 
     * @throws InterruptedException
     */
    public void waitForEmpty() throws InterruptedException {
        synchronized (this) {
            while (!(lastSent == null && queue.isEmpty())) {
                this.wait();
            }
        }
    }

    /**
     * Retrieve the last RequestToSend sent but not yet acknowledged.
     * 
     * @return
     */
    public RequestToSend getLastSent() {
        return lastSent;
    }

    // -- MANAGING FORGET ON SEND METHOD DECLARATIONS --

    /**
     * Declares 'methodName' as 'ForgetOnSend'
     */
    public void addFosRequest(Object obj, String methodName, String parameters) {
        checkMethodName(obj, methodName, null);
        fosRequests.put(methodName, parameters);
    }

    /**
     * Declares 'methodName' as not 'ForgetOnSend'
     */
    public void removeFosRequest(String methodName) {
        fosRequests.remove(methodName);
    }

    /**
     * Check if methodName has been declared as 'ForgetOnSend' or not
     */
    public boolean isFosRequest(String methodName) {
        return fosRequests.containsKey(methodName);
    }

    //
    // ------ PRIVATE METHODS ---------------------------------------
    //

    /**
     * Check if method name exists and is correctly spelled
     */
    private boolean checkMethodName(Object obj, String methodName, Class<?>[] parametersTypes) {
        if (this.checkedMethodNames.containsKey(methodName)) {
            if (parametersTypes != null) {
                // the method name with the right signature has already been checked
                List<Class<?>> parameterTlist = Arrays.asList(parametersTypes);
                HashSet<List<Class<?>>> signatures = this.checkedMethodNames.get(methodName);
                if (signatures.contains(parameterTlist)) {
                    return true;
                }
            } else {
                // the method name has already been checked
                return true;
            }
        }

        // check if the method is defined as public
        Class<?> reifiedClass = obj.getClass();
        boolean exists = org.objectweb.proactive.core.mop.Utils.checkMethodExistence(reifiedClass,
                methodName, parametersTypes);
        if (exists) {
            storeInMethodCache(methodName, parametersTypes);
            return true;
        }
        return false;
    }

    /**
     * Stores the given method name with the given parameters types inside our method signature
     * cache to avoid re-testing them
     * 
     * @param methodName
     *            name of the method
     * @param parametersTypes
     *            parameter type list
     */
    private void storeInMethodCache(String methodName, Class<?>[] parametersTypes) {
        List<Class<?>> parameterTlist = null;
        if (parametersTypes != null) {
            parameterTlist = Arrays.asList(parametersTypes);
        }

        // if we already know a version of this method, we store the new version in the existing set
        if (this.checkedMethodNames.containsKey(methodName) && (parameterTlist != null)) {
            HashSet<List<Class<?>>> signatures = this.checkedMethodNames.get(methodName);
            signatures.add(parameterTlist);
        }
        // otherwise, we create a set containing a single element
        else {
            HashSet<List<Class<?>>> signatures = new HashSet<List<Class<?>>>();
            if (parameterTlist != null) {
                signatures.add(parameterTlist);
            }
            checkedMethodNames.put(methodName, signatures);
        }
    }
}
