package functionaltests.monitor;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;


/**
*
* Defines a waiting monitor used by waiting methods of RMMonitorsHandler.
* It defines an RM event to wait. This class is used to store event no yet
* waited by a test too.
*
*
* @author ProActive team
*
*/
public class RMEventMonitor {

    /**
     * Defines event of this monitor
     */
    private RMEventType waitedEvent;

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
    public RMEventMonitor(RMEventType evt) {
        this.waitedEvent = evt;
    }

    /**
     * Return Scheduler event corresponding to this monitor.
     * @return
     */
    public RMEventType getWaitedEvent() {
        return this.waitedEvent;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && o instanceof RMEventMonitor) {
            return ((RMEventMonitor) o).getWaitedEvent() == waitedEvent;
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
