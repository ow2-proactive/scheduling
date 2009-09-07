package functionaltests.monitor;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


public class NodeEventMonitor extends RMEventMonitor {

    private String nodeUrl;

    private RMNodeEvent nodeEvent;

    public NodeEventMonitor(RMEventType evt, String nodeUrl) {
        super(evt);
        this.nodeUrl = nodeUrl;
    }

    public NodeEventMonitor(RMEventType evt, String nodeUrl, RMNodeEvent event) {
        super(evt);
        this.nodeUrl = nodeUrl;
        this.nodeEvent = event;
    }

    public boolean equals(Object o) {
        if (super.equals(o)) {
            if (o instanceof NodeEventMonitor) {
                return (((NodeEventMonitor) o).getNodeUrl().equals(nodeUrl));
            }
        }
        return false;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String url) {
        nodeUrl = url;
    }

    public RMNodeEvent getNodeEvent() {
        return nodeEvent;
    }

    public void setNodeEvent(RMNodeEvent nodeEvent) {
        this.nodeEvent = nodeEvent;
    }

}
