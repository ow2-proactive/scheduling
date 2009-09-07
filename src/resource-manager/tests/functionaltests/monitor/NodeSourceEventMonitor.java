package functionaltests.monitor;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;


public class NodeSourceEventMonitor extends RMEventMonitor {

    private String nodeSourceName;

    public NodeSourceEventMonitor(RMEventType evt, String nodeSourceName) {
        super(evt);
        this.nodeSourceName = nodeSourceName;
    }

    public boolean equals(Object o) {
        if (super.equals(o)) {
            return (((NodeSourceEventMonitor) o).getNodeSourceName().equals(nodeSourceName));
        }
        return false;
    }

    public String getNodeSourceName() {
        return nodeSourceName;
    }

}
