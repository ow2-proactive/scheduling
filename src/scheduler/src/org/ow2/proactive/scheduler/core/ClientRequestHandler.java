/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.threading.ReifiedMethodCall;


/**
 * ClientRequestHandler is used to delegate event call to client.
 * This class should be used with the ThreadPoolController which handles threads.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class ClientRequestHandler {

    public static final Logger logger = ProActiveLogger.getLogger(ClientRequestHandler.class);

    /** Number of threads used by the thread pool for clients events sending */
    private static final int THREAD_NUMBER = PASchedulerProperties.SCHEDULER_LISTENERS_THREADNUMBER
            .getValueAsInt();
    /** thread pool */
    private static final ExecutorService threadPoolForNetworkCalls = Executors.newFixedThreadPool(
            THREAD_NUMBER, new NamedThreadFactory("ClientRequestHandlerPool"));

    private static final AtomicInteger requestLeft = new AtomicInteger();

    public static void terminate() {
        // Precondition: no new event is emitted
        try {
            synchronized (requestLeft) {
                //obviously wait for events to be sent only if their are events
                if (requestLeft.get() > 0) {
                    requestLeft.wait();
                }
            }
            threadPoolForNetworkCalls.shutdown();
            threadPoolForNetworkCalls.awaitTermination(13, TimeUnit.SECONDS);
        } catch (Exception e) {
        }
    }

    /** Busy state of this client request queue */
    private final AtomicBoolean busy = new AtomicBoolean(false);
    /** Client id on which to send the request */
    private final UniqueID clientId;
    /** Client (listener) on which to send the request */
    private final SchedulerEventListener client;
    /** Events queue to be stored */
    private final LinkedList<ReifiedMethodCall> eventCallsToStore;
    /** Cross reference to the front-end : used to mark client as dirty */
    private final SchedulerFrontend frontend;

    /**
     * Create a new instance of ClientRequestHandler
     *
     * @param frontend a link to the front-end
     * @param clientId the Id of the client on which to talk to.
     * @param client the reference on the client itself.
     */
    public ClientRequestHandler(SchedulerFrontend frontend, UniqueID clientId, SchedulerEventListener client) {
        this.client = client;
        this.frontend = frontend;
        this.clientId = clientId;
        this.eventCallsToStore = new LinkedList<ReifiedMethodCall>();
    }

    /**
     * Add an event to the request queue of this client
     *
     * @param method the method to be called (must be a method implemented by the client)
     * @param args the argument to be passed to the method
     */
    public void addEvent(Method method, Object... args) {
        synchronized (eventCallsToStore) {
            eventCallsToStore.add(new ReifiedMethodCall(method, args));
            requestLeft.incrementAndGet();
        }
        tryStartTask();
    }

    /**
     * Try to create a task with new events to send, and start it in the thread pool.
     * Can do nothing if some previous events are currently being sent.
     *
     * Can be called from two different thread, even if it is private!
     */
    @SuppressWarnings("unchecked")
    private void tryStartTask() {
        synchronized (eventCallsToStore) {
            if (eventCallsToStore.size() > 0 && !busy.get()) {
                LinkedList<ReifiedMethodCall> tasks = (LinkedList<ReifiedMethodCall>) eventCallsToStore
                        .clone();
                eventCallsToStore.clear();
                busy.set(true);
                threadPoolForNetworkCalls.execute(new TaskRunnable(tasks));
            }
        }
    }

    /**
     * TaskRunnable is the task in charge to send the events in its list.
     *
     * @author The ProActive Team
     * @since ProActive Scheduling 2.0
     */
    class TaskRunnable implements Runnable {

        /** Events queue to be sent */
        private final LinkedList<ReifiedMethodCall> eventCallsToSend;

        /**
         * Create a new instance of Task
         *
         * @param eventCallsToSend
         */
        public TaskRunnable(LinkedList<ReifiedMethodCall> eventCalls) {
            if (eventCalls == null || eventCalls.size() == 0) {
                throw new IllegalArgumentException("List argument must not be null nor empty !");
            }
            this.eventCallsToSend = eventCalls;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            try {
                //unlock shutdown request if needed
                if (requestLeft.addAndGet(-eventCallsToSend.size()) == 0) {
                    synchronized (requestLeft) {
                        //inner synchronized OK since only used during termination (= no new event)
                        requestLeft.notify();
                    }
                }
                //loop on the list and send events
                while (!eventCallsToSend.isEmpty()) {
                    ReifiedMethodCall methodCall = eventCallsToSend.removeFirst();
                    methodCall.getMethod().invoke(client, methodCall.getArguments());
                }
                busy.set(false);
                //try to empty the events list if no event comes from the core
                tryStartTask();
            } catch (Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error during sending event to the cleint " + clientId, t);
                }
                //remove this client from Frontend (client dead or timed out)
                frontend.markAsDirty(clientId);
                //do not set busy here, we don't want to wait N times for the network timeout
                //so when client is dead keep busy to avoid re-execution of this run method by another thread
                busy.set(false);
            }
        }

    }

}
