package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;


public class GCMRuntimeRegistrationNotificationData implements Serializable {
    private String childURL;
    private long deploymentId;
    private Set<Node> nodes;

    public GCMRuntimeRegistrationNotificationData() {
        // No-args constructor
    }

    public GCMRuntimeRegistrationNotificationData(String childURL,
        long deploymentId, Set<Node> nodes) {
        this.childURL = childURL;
        this.deploymentId = deploymentId;
        this.nodes = nodes;
    }

    public String getChildURL() {
        return childURL;
    }

    public void setChildURL(String childURL) {
        this.childURL = childURL;
    }

    public long getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(long deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }
}
