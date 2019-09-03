package org.ow2.proactive.resourcemanager.common.event;

import org.ow2.proactive.resourcemanager.common.NodeState;

import java.io.Serializable;

public class RMNodeHistory implements Serializable {

    private String nodeUrl;

    private String host;

    private String nodeSource;

    private String userName;

    private String providerName;

    private NodeState nodeState;

    protected long startTime;

    protected long endTime;

    public RMNodeHistory() {}

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
