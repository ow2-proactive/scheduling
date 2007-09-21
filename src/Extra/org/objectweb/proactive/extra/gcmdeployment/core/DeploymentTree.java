/**
 *
 */
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;


public class DeploymentTree implements NotificationListener {
    protected DeploymentNode rootNode;
    protected Map<Long, DeploymentNode> nodeMap;

    public DeploymentTree() {
        rootNode = null;
        nodeMap = new HashMap<Long, DeploymentNode>();
        subscribe();
    }

    public void addNode(DeploymentNode node, DeploymentNode parent) {
        parent.addChildren(node);
        nodeMap.put(node.getId(), node);
    }

    public DeploymentNode getNode(Long deploymentId) {
        return nodeMap.get(deploymentId);
    }

    public DeploymentNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(DeploymentNode rootNode) {
        this.rootNode = rootNode;
    }

    protected void subscribe() {
        JMXNotificationManager.getInstance()
                              .subscribe(ProActiveRuntimeImpl.getProActiveRuntime()
                                                             .getMBean()
                                                             .getObjectName(),
            this);
    }

    public void handleNotification(Notification notification, Object handback) {
        String type = notification.getType();

        if (NotificationType.GCMRuntimeRegistered.equals(type)) {
            GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification.getUserData();

            DeploymentNode deploymentNode = getNode(data.getDeploymentId());

            Node node = data.getNodes().iterator().next();
            VMInformation information = node.getVMInformation();

            VMNodeList vmNodeList = new VMNodeList(information);
            vmNodeList.addNodes(data.getNodes());

            deploymentNode.addVMNodes(vmNodeList);
        }
    }
}
