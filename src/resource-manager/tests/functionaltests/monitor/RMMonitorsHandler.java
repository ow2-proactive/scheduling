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
package functionaltests.monitor;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.utils.TimeoutAccounter;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;


public class RMMonitorsHandler {

    /**
     * Event concerning RM State (started, killed, etc)
     * and not yet checked by a waiter.
     */
    private List<RMEventType> stateEvents;

    /**
     * Event concerning nodes sources.
     * and not yet checked by a waiter.
     */
    private List<NodeSourceEventMonitor> nodeSourcesEvent;

    /**
     * Event concerning nodes,(added, busy, down...)
     * and not yet checked by a waiter.
     */
    private List<NodeEventMonitor> nodesEvent;

    /**
     * Awaited event from Scheduler ;
     * a list of monitors, used for synchronization with threads that have called waitForEvent**()
     * methods. These monitors are notified only if corresponding event is thrown by Scheduler
     */
    private List<RMEventMonitor> eventsMonitors;

    public RMMonitorsHandler() {
        stateEvents = new ArrayList<RMEventType>();
        nodeSourcesEvent = new ArrayList<NodeSourceEventMonitor>();
        nodesEvent = new ArrayList<NodeEventMonitor>();
        eventsMonitors = new ArrayList<RMEventMonitor>();
    }

    public void waitForRMStateEvent(RMEventType eventType, long timeout) throws ProActiveTimeoutException {
        RMEventMonitor monitor = null;
        synchronized (this) {
            if (stateEvents.contains(eventType)) {
                stateEvents.remove(eventType);
                return;
            }
            monitor = getMonitor(new RMEventMonitor(eventType));
        }
        waitWithMonitor(monitor, timeout);
    }

    public void waitForNodesourceEvent(RMEventType eventType, String nodeSourceName, long timeout)
            throws ProActiveTimeoutException {
        NodeSourceEventMonitor monitor = new NodeSourceEventMonitor(eventType, nodeSourceName);
        synchronized (this) {
            if (this.nodeSourcesEvent.contains(monitor)) {
                nodeSourcesEvent.remove(monitor);
                return;
            }
            monitor = (NodeSourceEventMonitor) getMonitor(new NodeSourceEventMonitor(eventType,
                nodeSourceName));
        }
        waitWithMonitor(monitor, timeout);
    }

    public RMNodeEvent waitForNodeEvent(RMEventType eventType, String nodeUrl, long timeout)
            throws ProActiveTimeoutException {
        NodeEventMonitor monitor = new NodeEventMonitor(eventType, nodeUrl);
        synchronized (this) {
            if (this.nodesEvent.contains(monitor)) {
                RMNodeEvent event = nodesEvent.get(nodesEvent.indexOf(monitor)).getNodeEvent();
                nodesEvent.remove(monitor);
                return event;
            }
            monitor = (NodeEventMonitor) getMonitor(monitor);
        }
        waitWithMonitor(monitor, timeout);
        return monitor.getNodeEvent();
    }

    public RMNodeEvent waitForAnyNodeEvent(RMEventType eventType, long timeout)
            throws ProActiveTimeoutException {
        NodeEventMonitor monitor = new NodeEventMonitor(eventType, "");
        synchronized (this) {
            for (NodeEventMonitor e : nodesEvent) {
                if (e.getWaitedEvent().equals(eventType)) {
                    nodesEvent.remove(e);
                    return e.getNodeEvent();
                }
            }
            monitor = (NodeEventMonitor) getMonitor(monitor);
        }
        waitWithMonitor(monitor, timeout);
        return monitor.getNodeEvent();
    }

    /**
    *
    * @param event
    */
    public void handleSchedulerStateEvent(RMEventType eventType) {
        synchronized (this) {
            if (!lookAndNotifyMonitor(new RMEventMonitor(eventType))) {
                this.stateEvents.add(eventType);
            }
        }
    }

    /**
    *
    * @param event
    */
    public void handleNodesourceEvent(RMNodeSourceEvent event) {
        synchronized (this) {
            NodeSourceEventMonitor nsem = new NodeSourceEventMonitor(event.getEventType(), event
                    .getSourceName());
            if (!lookAndNotifyMonitor(nsem)) {
                this.nodeSourcesEvent.add(nsem);
            }
        }
    }

    /**
    *
    * @param event
    */
    public void handleNodeEvent(RMNodeEvent event) {
        synchronized (this) {
            boolean anyNodeMonitorFound = false;
            boolean specificNodeMonitorFound = false;

            //notify eventual monitors that wait for an 'any node event',
            //i.e a specific node Event, but not occurred on a specific node
            NodeEventMonitor nem = new NodeEventMonitor(event.getEventType(), "", event);
            if (lookAndNotifyMonitor(nem)) {
                anyNodeMonitorFound = true;
            }

            //notify eventual monitors that wait for any 'a specific any node event',
            //i.e a specific node Event, on a specific node, specified by Url
            nem = new NodeEventMonitor(event.getEventType(), event.getNodeUrl(), event);
            if (lookAndNotifyMonitor(nem)) {
                specificNodeMonitorFound = true;
            }

            //no monitor has consumed this event, store it
            if (!(anyNodeMonitorFound || specificNodeMonitorFound)) {
                this.nodesEvent.add(nem);
            }
        }
    }

    //---------------------------------------------------------------//
    //private methods
    // these methods MUST be called from a synchronized(this) block
    //---------------------------------------------------------------//

    /**
     * Notify threads, if any that wait for a specific event
     *
     * @param monitor TaskEventMonitor object to look and notify if found
     * @return true if a monitor has been found and notification has been performed, false otherwise
     */
    private boolean lookAndNotifyMonitor(RMEventMonitor monitor) {
        RMEventMonitor monitorToNotify = this.getAndRemoveMonitor(monitor);
        if (monitorToNotify != null) {
            synchronized (monitorToNotify) {
                //monitor exists, but maybe created by a waiter that has reached timeout
                //so check if it has been timeouted
                if (!monitorToNotify.isTimeouted()) {
                    notifyMonitor(monitorToNotify);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Notify threads, if any that wait for a specific event
     *
     * @param monitor TaskEventMonitor object to look and notify if found
     * @return true if a monitor has been found and notification has been performed, false otherwise
     */
    private boolean lookAndNotifyMonitor(NodeEventMonitor monitor) {
        NodeEventMonitor monitorToNotify = (NodeEventMonitor) this.getAndRemoveMonitor(monitor);
        if (monitorToNotify != null) {
            synchronized (monitorToNotify) {
                //monitor exists, but maybe created by a waiter that has reached timeout
                //so check if it has been timeouted
                if (!monitorToNotify.isTimeouted()) {
                    monitorToNotify.setNodeUrl(monitor.getNodeUrl());
                    monitorToNotify.setNodeEvent(monitor.getNodeEvent());
                    notifyMonitor(monitorToNotify);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return an EventMonitor that is currently used to wait for an event, or null if
     * this event is awaited by no one.
     * @param monitor EventMonitor Object representing Event to look for.
     * @return an eventMonitor object to notify, or null.
     */
    private RMEventMonitor getAndRemoveMonitor(RMEventMonitor monitor) {
        if (eventsMonitors.contains(monitor)) {
            return eventsMonitors.remove(eventsMonitors.indexOf(monitor));
        }
        return null;
    }

    /**
     * Returns a monitor used to wait for a specific event that hasn't yet occurred.
     * If there is not yet a monitor for this event, EventMonitor passed in parameter is
     * used as Monitor object for this event.
     * @param monitor representing event to wait for.
     * @return an EventMonitorJob to use as waiting Monitor.
     */
    private RMEventMonitor getMonitor(RMEventMonitor monitor) {
        if (!eventsMonitors.contains(monitor)) {
            eventsMonitors.add(monitor);
            return monitor;
        } else {
            return eventsMonitors.get(eventsMonitors.indexOf(monitor));
        }
    }

    /**
     * Notify an EventMonitor object, i.e resume threads that have perform
     * a wait on EventMonitor object passed in parameter.
     * @param monitorToNotify EventMonitor to notify.
     */
    private void notifyMonitor(RMEventMonitor monitorToNotify) {
        //System.out.println("===========================================");
        //System.out.println("NOTIFYING FOR EVENT : " + monitorToNotify.getWaitedEvent());
        //System.out.println("===========================================");
        synchronized (monitorToNotify) {
            monitorToNotify.setEventOccured();
            monitorToNotify.notify();
        }
    }

    //---------------------------------------------------------------//
    //private methods
    // these method MUST NOT be called from a synchronized(this) block
    //---------------------------------------------------------------//

    private void waitWithMonitor(RMEventMonitor monitor, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter counter = TimeoutAccounter.getAccounter(timeout);
        synchronized (monitor) {
            monitor.setTimeouted(false);
            while (!counter.isTimeoutElapsed()) {
                if (monitor.eventOccured())
                    return;
                try {
                    //System.out.println("===========================================");
                    //System.out.println("I AM WAITING FOR EVENT : " + monitor.getWaitedEvent() + " during " +
                    //counter.getRemainingTimeout());
                    //System.out.println("===========================================");
                    monitor.wait(counter.getRemainingTimeout());
                } catch (InterruptedException e) {
                    //spurious wake-up, nothing to do
                    e.printStackTrace();
                }
            }
            monitor.setTimeouted(true);
        }
        throw new ProActiveTimeoutException("timeout elapsed");
    }

    public synchronized void dumpEvents() {
        System.out.println("RM events");
        for (RMEventType e : stateEvents) {
            System.out.println(e);
        }
        System.out.println("RM node source events");
        for (NodeSourceEventMonitor e : nodeSourcesEvent) {
            System.out.println(e);
        }
        System.out.println("RM node events");
        for (NodeEventMonitor e : nodesEvent) {
            System.out.println(e);
        }
    }
}
