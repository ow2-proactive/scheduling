/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.core.UniqueID;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.threading.ReifiedMethodCall;


/**
 * TimedClientRequestQueue is used to delegate event call to client.
 * This class should be used with the ThreadPoolController which handles threads.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class TimedClientRequestQueue implements Runnable {

    /** Busy state of this client request queue */
    private AtomicBoolean busy = new AtomicBoolean(false);
    /** Client id on which to send the request */
    private UniqueID clientId;
    /** Client (listener) on which to send the request */
    private SchedulerEventListener client;
    /** Events queue to be sent */
    private LinkedList<ReifiedMethodCall> eventCalls;
    /** Cross reference to the front-end : used to mark client as dirty */
    private SchedulerFrontend frontend;

    /**
     * Create a new instance of TimedClientRequestQueue
     *
     * @param frontend a link to the front-end
     * @param clientId the Id of the client on which to talk to.
     * @param client the reference on the client itself.
     */
    public TimedClientRequestQueue(SchedulerFrontend frontend, UniqueID clientId,
            SchedulerEventListener client) {
        this.client = client;
        this.eventCalls = new LinkedList<ReifiedMethodCall>();
        this.frontend = frontend;
        this.clientId = clientId;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        //if events list is empty, stop it right now
        if (getEventsQueueSize() == 0) {
            return;
        }
        //if a thread is not already working on this queue
        if (!busy.getAndSet(true)) {
            try {
                //loop on the copy and send events
                while (!eventCalls.isEmpty()) {
                    ReifiedMethodCall methodCall = eventCalls.removeFirst();
                    methodCall.getMethod().invoke(client, methodCall.getArguments());
                }
                //not busy anymore
                busy.set(false);
            } catch (Throwable t) {
                //remove this client from Frontend (client dead or timed out)
                frontend.markAsDirty(clientId);
                //do not set busy here, we don't want to wait N times for the network timeout
                //so when client is dead keep busy to avoid re-execution of this run method by another thread
            }
        }
    }

    /**
     * Add an event to the request queue of this client
     *
     * @param method the method to be called (must be a method implemented by the client)
     * @param args the argument to be passed to the method
     */
    public void addEvent(Method method, Object... args) {
        synchronized (eventCalls) {
            eventCalls.add(new ReifiedMethodCall(method, args));
        }
    }

    /**
     * Get the number of events in the queue
     *
     * @return the number of events in the queue
     */
    private int getEventsQueueSize() {
        synchronized (eventCalls) {
            return eventCalls.size();
        }
    }

    /**
     * Return true if this task can be restarted, false if not.<br>
     * This 'state' satisfy the two following conditions :<br>
     * <ul>
     * <li>The task is not busy (it has left the run method)</li>
     * <li>The task has at least one event to send</li>
     * </ul>
     * Calling this method can help improving performance, but restart a task
     * which this method returns false is allowed as the run method will take care of that.
     *
     * @return true if this task can be restarted, false otherwise.
     */
    public boolean shouldStart() {
        return !busy.get() && getEventsQueueSize() > 0;
    }

}
