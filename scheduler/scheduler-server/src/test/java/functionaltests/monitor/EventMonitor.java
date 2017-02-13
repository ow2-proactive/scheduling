/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
