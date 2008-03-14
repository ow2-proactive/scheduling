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
package org.objectweb.proactive.core.body;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.body.proxy.RequestToSend;


/**
 * There is one instance of this class per SendingQueue. Each instance is Runnable. Thus, it can be
 * started to poll the SendingQueue for sending the RequestToSend through a ThreadPoolExecutor.
 */
public class SendingThreadPool implements Runnable {

    private static final int MAX_THREAD_POOL_CORE_SIZE = 10;
    private static final int THREAD_POOL_CORE_SIZE = 10;
    private static final int THREAD_KEEP_ALIVE_TIME = 10; // In Seconds

    /* Needed by the ThreadPoolExecutor */
    private BlockingQueue<Runnable> threadQueue;

    /* The ThreadPoolExecutor which handles the RTS */
    private ThreadPoolExecutor threadPool;

    /* The SendingQueue this SendingThreadPool is attached to */
    private SendingQueue sendingQueue;

    /* A reference to the running thread */
    private Thread myThread;

    /* A boolean to terminate the thread (see stop()) */
    private boolean continueRunning;

    /**
     * Creates a {@link SendingThreadPool} instance without starting it. This should be done through
     * the <i>wakeup()</i> method which would be invoked when trying to send a FOS request.
     * 
     * @param sendingQueue
     */
    public SendingThreadPool(SendingQueue sendingQueue) {
        this.threadQueue = new SynchronousQueue<Runnable>();
        this.threadPool = new ThreadPoolExecutor(THREAD_POOL_CORE_SIZE, MAX_THREAD_POOL_CORE_SIZE,
            THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS, threadQueue);
        this.sendingQueue = sendingQueue;
        this.continueRunning = true;
    }

    /**
     * Starts the thread if not already started. This will cause the {@link SendingThreadPool} poll
     * the {@link SendingQueue} to get and send some {@link RequestToSend}.
     */
    public void wakeUp() {
        if (myThread == null) {
            myThread = new Thread(this);
            myThread.start();
        }
    }

    /**
     * Invoking this method will cause the running thread (if it's running) to terminate ASAP. ASAP
     * means that it will waits until the {@link SendingQueue} is empty, to ensure that all pending
     * {@link RequestToSend} would be sent.
     */
    public void stop() {
        this.continueRunning = false;
    }

    /**
     * While the body is active, retrieve a {@link RequestToSend} from the {@link SendingQueue} and
     * send it to the Thread Pool for immediate execution (ie. sending the request).
     */
    public void run() {
        try {
            RequestToSend rts = null;
            do {
                rts = sendingQueue.poll(1, TimeUnit.SECONDS);
                if (rts != null) {
                    threadPool.execute(rts);
                }

            } while (continueRunning || rts != null);

            threadPool.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (java.util.concurrent.RejectedExecutionException e) {
            e.printStackTrace();
        }
    }
}
