package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;


public class MonitoredNode extends BasicMonitoredObject {
    static protected int lastID = 0;
    static protected Map prettyNames = new HashMap();
    protected Node node;
    protected int incLastID() {
        return ++lastID;
    }

    protected Map getPrettyNames() {
        return prettyNames;
    }

    public MonitoredNode(Node node, String jvm) {
        super(NODE, node.getNodeInformation().getName(),node.getNodeInformation().getName() + " on " + jvm);
        this.node = node;
    }
    
    public Node getNode() {
        return node;
    }
    
}
