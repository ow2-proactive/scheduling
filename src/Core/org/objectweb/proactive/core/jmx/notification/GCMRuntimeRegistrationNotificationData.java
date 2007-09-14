package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;


public class GCMRuntimeRegistrationNotificationData implements Serializable {
    private String childURL;
    private long deploymentId;

    public GCMRuntimeRegistrationNotificationData() {
        // No-args constructor
    }

    public GCMRuntimeRegistrationNotificationData(String childURL,
        long deploymentId) {
        this.childURL = childURL;
        this.deploymentId = deploymentId;
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
}
