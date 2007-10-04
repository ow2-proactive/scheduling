package org.objectweb.proactive.ic2d.jmxmonitoring.data.listener;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.BodyNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RuntimeNotificationData;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;


public class RuntimeObjectListener implements NotificationListener {
    private RuntimeObject runtimeObject;

    public RuntimeObjectListener(RuntimeObject runtimeObject) {
        this.runtimeObject = runtimeObject;
    }

    public void handleNotification(Notification notification, Object handback) {
        String type = notification.getType();
        if (type.equals(NotificationType.bodyCreated)) {
            BodyNotificationData notificationData = (BodyNotificationData) notification.getUserData();
            UniqueID id = notificationData.getId();
            String nodeUrl = notificationData.getNodeUrl();
            String name = notificationData.getClassName();
            ObjectName oname = FactoryName.createActiveObjectName(id);
            System.out.println("...............................Body Created " +
                notification.getSource());
            NodeObject node = (NodeObject) runtimeObject.getChild(nodeUrl);
            if (node != null) {
                node.addChild(new ActiveObject(node, id, name, oname));
            } else {
                System.out.println(
                    "RuntimeObjectListener.handleNotification() node pas trouve nodeUrl=" +
                    nodeUrl);
            }
        } else if (type.equals(NotificationType.bodyDestroyed)) {
            BodyNotificationData notificationData = (BodyNotificationData) notification.getUserData();
            UniqueID id = notificationData.getId();
            // ObjectName oname = Name.createActiveObjectName(id);
            System.out.println("...............................Body Destroyed " +
                notification.getSource());
            runtimeObject.getWorldObject().removeActiveObject(id);
        } else if (type.equals(NotificationType.runtimeRegistered)) {
            System.out.println(
                "...............................Runtime Registered " +
                notification.getSource());
            RuntimeNotificationData userData = (RuntimeNotificationData) notification.getUserData();
            runtimeObject.getParent().proposeChild();
        } else if (type.equals(NotificationType.runtimeUnregistered)) {
            System.out.println(
                "...............................Runtime Unregistered " +
                notification.getSource());
            RuntimeNotificationData userData = (RuntimeNotificationData) notification.getUserData();
        } else if (type.equals(NotificationType.runtimeDestroyed)) {
            System.out.println(
                "...............................Runtime destroyed " +
                runtimeObject);
            runtimeObject.runtimeKilled();
        }
        // --- NodeEvent ----------------
        else if (type.equals(NotificationType.nodeCreated)) {
            System.out.println("...............................Node Created");
            Node node = (Node) notification.getUserData();
            String nodeUrl = node.getNodeInformation().getURL();
            ObjectName oname = FactoryName.createNodeObjectName(runtimeObject.getUrl(),
                    node.getNodeInformation().getName());
            NodeObject child = new NodeObject(runtimeObject, nodeUrl, oname);
            runtimeObject.addChild(child);
        } else if (type.equals(NotificationType.nodeDestroyed)) {
            String nodeUrl = (String) notification.getUserData();
            System.out.println(
                "...............................Node Destroyed : " + nodeUrl);
            NodeObject node = (NodeObject) runtimeObject.getChild(nodeUrl);
            if (node != null) {
                node.destroy();
            }
        } else {
            System.out.println(runtimeObject + " => " + type);
        }
    }
}
