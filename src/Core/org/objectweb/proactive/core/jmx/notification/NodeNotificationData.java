package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;


/**
 * Used in the JMX notifications
 * @author ProActive Team
 */
public class NodeNotificationData implements Serializable {

    /** The node */
    private Node node;
    private String vn;

    public NodeNotificationData() {
        // No args constructor
    }

    /**
     * Creates a new NodeNotificationData
     * @param node The node
     */
    public NodeNotificationData(Node node, String vn) {
        this.node = node;
        this.vn = vn;
    }

    public Node getNode() {
        return this.node;
    }

    public String getVirtualNode() {
        return this.vn;
    }

    @Override
    public String toString() {
        return this.node.toString();
    }
}
