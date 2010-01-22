/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.monitor;

import org.ow2.proactive.scheduler.common.SchedulerEvent;


/**
 *
 * Defines a waiting monitor used by waiting methods of SchedulerMonitorsHandler.
 * It defines a Scheduler event to wait. This class is used to store event no yet
 * waited by a test too.
 *
 *
 * @author ProActive team
 *
 */
public class EventMonitor {

    /**
     * Defines event of this monitor
     */
    private SchedulerEvent waitedEvent;

    /**
     *
     */
    boolean eventOccured = false;

    /**
     * to check if that monitor has bee created, but no more
     * used as monitor, caused to timeout reached.
     */
    boolean timeouted = false;

    /**
     * @param evt
     */
    public EventMonitor(SchedulerEvent evt) {
        this.waitedEvent = evt;
    }

    /**
     * Return Scheduler event corresponding to this monitor.
     * @return
     */
    public SchedulerEvent getWaitedEvent() {
        return this.waitedEvent;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && o instanceof EventMonitor) {
            return ((EventMonitor) o).getWaitedEvent() == waitedEvent;
        }
        return false;
    }

    /**
     * Set state to the monitor to "waited Event has occurred"
     */
    public void setEventOccured() {
        eventOccured = true;
    }

    /**
     * get status of event awaited event, occurred or not
     * @return
     */
    public boolean eventOccured() {
        return eventOccured;
    }

    /**
     * Return true if that monitor has been used to wait an event,
     * and has timeouted.
     * @return
     */
    public boolean isTimeouted() {
        return timeouted;
    }

    /**
     * Set timeouted state for the monitor.
     * @param timeouted
     */
    public void setTimeouted(boolean timeouted) {
        this.timeouted = timeouted;
    }
}
