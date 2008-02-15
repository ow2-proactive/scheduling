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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class SendingQueue implements Runnable {
    public enum Strategy {
        ForgetOnSend, Standard
    };

    private static Map<String /* method name */, Strategy> sendingStrategyMap = new HashMap<String, Strategy>();
    private static Map<UniversalBodyProxy, SendingQueue> sendingQueueMap = new HashMap<UniversalBodyProxy, SendingQueue>();
    private BlockingQueue<RequestToSend> queue;

    //
    // ********* RequestToSend OBJECT POOL MANAGEMENT *************************
    //
    public static final int CORE_POOL_SIZE = 8;
    private static Stack<RequestToSend> pool;

    static {
        pool = new Stack<RequestToSend>();
        for (int i = 0; i < CORE_POOL_SIZE; i++) {
            pool.push(new RequestToSend());
        }
    }

    //
    // -- CONSTRUCTOR ----------------------------------------------------------
    //

    private SendingQueue() {
        this.queue = new LinkedBlockingQueue<RequestToSend>();
    }

    //
    // --- PUBLIC METHODS ------------------------------------------------------
    //

    /**
     * Invoke sendRequest on destBody if it is a standard method call, otherwise, add the request in
     * the correct queue (regarding its sending strategy)
     * 
     * @param methodCall
     * @param future
     * @param sourceBody
     * @param destBody
     * @throws IOException
     * @throws RenegotiateSessionException
     * @throws CommunicationForbiddenException
     */
    public static void sendRequest(MethodCall methodCall, Future future, Body sourceBody,
            UniversalBodyProxy destBody) throws IOException, RenegotiateSessionException,
            CommunicationForbiddenException {

        // ForgetOnSend security check regarding sterility
        if (sourceBody.isSteril()) {
            if ((destBody.getBodyID() != sourceBody.getID()) &&
                (destBody.getBodyID() != sourceBody.getParentUID())) {
                throw new java.io.IOException("Unable to perform the " + methodCall.getName() + "(): " +
                    "When using the ForgetOnSend sending strategy, " +
                    "you are not allowed to send any request, " +
                    "except to yourself or to your parent (the body " +
                    "who send the request you are serving)" + Thread.currentThread().getName());
            } else {
                // A sterile service can only send sterile requests (and also the limitation
                // described above)
                methodCall.setSterility(true);
            }
        }

        Strategy strategy = sendingStrategyMap.get(methodCall.getName());

        if (strategy == null || strategy == Strategy.Standard) {
            // ProActiveLogger.getLogger(Loggers.REQUESTS).debug(
            // "Sending '" + methodCall.getName() + "' as a Standard request.");
            waitForEmpty();
            destBody.sendRequest(methodCall, future, sourceBody);

        } else {
            ProActiveLogger.getLogger(Loggers.REQUESTS).debug(
                    "Sending '" + methodCall.getName() + "' as a ForgetOnSend request.");
            // Forget On Send strategy :
            // One thread per destination
            // Needs sterility
            // -> Add it on the body proxy's sending queue
            methodCall.setSterility(true);
            SendingQueue queue = sendingQueueMap.get(destBody);
            if (queue == null) {
                queue = new SendingQueue();
                sendingQueueMap.put(destBody, queue);
                (new Thread(queue)).start();
            }
            queue.add(methodCall, future, sourceBody, destBody);
        }
    }

    public void run() {
        try {
            while (true) {
                RequestToSend rts = this.queue.take();

                rts.destBody.sendRequest(rts.methodCall, rts.future, rts.sourceBody);
                pool.push(rts);
                // If the queue is empty, we have to notify the threads which
                // are waiting for to send a standard call (with RDV)
                checkEmpty();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            e.printStackTrace();
        } catch (CommunicationForbiddenException e) {
            e.printStackTrace();
        }
    }

    /**
     * Define the <i>strategy</i> the local body will have to use to invoke a method called
     * <i>methodName</i> on another body.
     * 
     * @param methodName
     * @param strategy
     */
    public static void setSendingStrategy(String methodName, Strategy strategy) {
        sendingStrategyMap.put(methodName, strategy);
    }

    /**
     * Returns the strategy which will be used to invoke <i>methodName</i>
     * 
     * @param methodName
     * @return
     */
    public static Strategy getSendingStrategy(String methodName) {
        return sendingStrategyMap.get(methodName);
    }

    //
    // --- PRIVATE METHODS ------------------------------------------------------
    //  

    private static void waitForEmpty() {
        Iterator<SendingQueue> it = sendingQueueMap.values().iterator();
        while (it.hasNext()) {
            SendingQueue queue = it.next();
            queue.waitForEmptyQueue();
        }
    }

    private synchronized void checkEmpty() {
        if (this.queue.isEmpty()) {
            notify();
        }
    }

    private synchronized void waitForEmptyQueue() {
        try {
            while (!this.queue.isEmpty()) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void add(MethodCall mc, Future ft, Body sourceBody, UniversalBodyProxy destBody) {
        if (pool.isEmpty()) {
            queue.add(new RequestToSend(mc, ft, sourceBody, destBody));
        } else {
            RequestToSend result = pool.pop();
            result.update(mc, ft, sourceBody, destBody);
            queue.add(result);
        }
    }

    public void add(MethodCall mc, Future ft) {
    }

    public int size() {
        return queue.size();
    }

    //
    // ********* INTERNAL CLASS ***********************************************
    //
    // An internal class to represent all the parameters needed for a requestToSend() call
    private static class RequestToSend {
        private MethodCall methodCall;
        private Future future;
        private Body sourceBody;
        private UniversalBodyProxy destBody;

        public RequestToSend() {
        }

        public RequestToSend(MethodCall mc, Future ft, Body sourceBody, UniversalBodyProxy destBody) {
            this.update(mc, ft, sourceBody, destBody);
        }

        public void update(MethodCall mc, Future ft, Body sourceBody, UniversalBodyProxy destBody) {
            this.methodCall = mc;
            this.future = ft;
            this.sourceBody = sourceBody;
            this.destBody = destBody;
        }
    }
}
