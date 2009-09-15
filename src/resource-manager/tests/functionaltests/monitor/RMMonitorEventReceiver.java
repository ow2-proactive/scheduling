package functionaltests.monitor;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


public class RMMonitorEventReceiver implements RMEventListener {

    private RMMonitorsHandler monitorsHandler;

    /**
     * empty constructor
     */
    public RMMonitorEventReceiver() {
    }

    /**
     * @param monitor SchedulerMonitorsHandler object which is notified
     * of Schedulers events.
     */
    public RMMonitorEventReceiver(RMMonitorsHandler monitor) {
        this.monitorsHandler = monitor;
    }

    public void nodeEvent(RMNodeEvent event) {
        System.out.println("Event: " + event);
        monitorsHandler.handleNodeEvent(event);
    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
        System.out.println("Event: " + event);
        monitorsHandler.handleNodesourceEvent(event);
    }

    public void rmEvent(RMEvent event) {
        System.out.println("Event: " + event);
        monitorsHandler.handleSchedulerStateEvent(event.getEventType());
    }

    public RMInitialState init(RMAuthentication auth) {
        RMMonitoring monitor = auth.logAsMonitor();
        return monitor.addRMEventListener((RMEventListener) PAActiveObject.getStubOnThis());
    }
}
