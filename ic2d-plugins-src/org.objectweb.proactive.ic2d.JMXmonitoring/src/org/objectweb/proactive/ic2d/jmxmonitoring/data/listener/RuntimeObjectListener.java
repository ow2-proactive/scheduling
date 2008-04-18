/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.data.listener;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.BodyNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;


/**
 *
 * Listener for a RuntimeObject. Listens for notifications concerning a ProActive Runtime and updates the
 * IC2D model object representation of it.
 * For each IC2D representation a ProActive Runtime, a RuntimeObjectListener will be created and subscribed to
 * the </code>org.objectweb.proactive.core.jmx.util.JMXNotificationManager</code> (singleton).
 * Each time an event occur related to the ProActive Runtime, a notification
 * will be sent to this listener.  This listener will update the model representation of the Runtime
 * which will send notification for its own listener(s) (the edit part(s))
 *
 * @author The ProActive Team
 *
 */
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
            System.out.println("...............................Body Created " + notification.getSource());
            NodeObject node = (NodeObject) runtimeObject.getChild(nodeUrl);
            if (node != null) {
                node.addChild(new ActiveObject(node, id, name, oname));
            } else {
                System.out.println("RuntimeObjectListener.handleNotification() node pas trouve nodeUrl=" +
                    nodeUrl);
            }
        } else if (type.equals(NotificationType.bodyDestroyed)) {
            BodyNotificationData notificationData = (BodyNotificationData) notification.getUserData();
            UniqueID id = notificationData.getId();
            // ObjectName oname = Name.createActiveObjectName(id);
            System.out.println("...............................Body Destroyed " + notification.getSource());

            ActiveObject ao = runtimeObject.getWorldObject().findActiveObject(id);
            if (ao != null) {
                ao.setDestroyed(true);
            }

            runtimeObject.getWorldObject().removeActiveObject(id);
        } else if (type.equals(NotificationType.runtimeRegistered)) {
            System.out.println("...............................Runtime Registered " +
                notification.getSource());
            this.runtimeObject.getParent().explore();
            // this lines don't actually do anything  
            //TODO: remove them        	
            //RuntimeNotificationData userData = (RuntimeNotificationData) notification.getUserData();
            runtimeObject.getParent().proposeChild();
        } else if (type.equals(NotificationType.runtimeUnregistered)) {
            System.out.println("...............................Runtime Unregistered " +
                notification.getSource());
            this.runtimeObject.getParent().explore();
            //   RuntimeNotificationData userData = (RuntimeNotificationData) notification.getUserData();
        } else if (type.equals(NotificationType.runtimeDestroyed)) {
            System.out.println("...............................Runtime destroyed " + runtimeObject);
            runtimeObject.runtimeKilled();
        }
        // --- NodeEvent ----------------
        else if (type.equals(NotificationType.nodeCreated)) {
            System.out.println("...............................Node Created");
            this.runtimeObject.getParent().explore();
            //            
            //            Node node = ((NodeNotificationData) notification.getUserData()).getNode();
            //            String nodeUrl = node.getNodeInformation().getURL();
            //            ObjectName oname = FactoryName.createNodeObjectName(runtimeObject.getUrl(),
            //                    node.getNodeInformation().getName());
            //            NodeObject child = new NodeObject(runtimeObject, nodeUrl, oname);
            //            runtimeObject.addChild(child);
        } else if (type.equals(NotificationType.nodeDestroyed)) {
            this.runtimeObject.getParent().explore();
            String nodeUrl = (String) notification.getUserData();
            System.out.println("...............................Node Destroyed : " + nodeUrl);
            //            NodeObject node = (NodeObject) runtimeObject.getChild(nodeUrl);
            //            if (node != null) {
            //                node.destroy();
            //            }
        } else {
            System.out.println(runtimeObject + " => " + type);
        }
    }
}
