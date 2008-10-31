package org.ow2.proactive.resourcemanager.gui.table;

import org.ow2.proactive.resourcemanager.common.NodeState;


public class NodeTableItem {

    private String nodeSource;
    private String host;
    private NodeState nodeState;
    private String nodeUrl;

    public NodeTableItem(String ns, String host, NodeState st, String url) {
        this.nodeSource = ns;
        this.host = host;
        this.nodeState = st;
        this.nodeUrl = url;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public NodeState getState() {
        return nodeState;
    }

    public void setState(NodeState state) {
        this.nodeState = state;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o instanceof NodeTableItem && this.nodeUrl.equals(((NodeTableItem) o).getNodeUrl())) {
            return true;
        } else
            return false;
    }
}
