package functionnaltests.monitor;

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
