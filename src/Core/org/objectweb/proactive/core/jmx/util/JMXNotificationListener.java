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
package org.objectweb.proactive.core.jmx.util;

import java.io.IOException;
import java.io.Serializable;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An IC2DListener is an active object which listens several remotes MBeans with the ProActiveConnection.
 * This listener is used by the JMXNotificationManager.
 * @author ProActive Team
 */
public class JMXNotificationListener implements NotificationListener,
    ProActiveInternalObject, Serializable {
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX);

    public JMXNotificationListener() {
        // Empty Constructor
    }

    /**
     * Subscribes the current active object to the JMX notifications of a remote MBean.
     * @param connection The ProActiveConnection in order to connect to the remote server MBean.
     * @param oname The ObjectName of the MBean
     * @param filter A notification filter
     * @param handback A hanback
     * This method is deprecated. Use subscribeObjectFromRemoteMBean(ProActiveConnection connection, ObjectName oname,
            NotificationFilter filter, Object handback) instead
     */
    @Deprecated
    public void subscribe(ProActiveConnection connection, ObjectName oname,
        NotificationFilter filter, Object handback) {
        try {
            if (!connection.isRegistered(oname)) {
                System.err.println(
                    "JMXNotificationListener.subscribe() Oooops oname not known:" +
                    oname);
                return;
            }
            connection.addNotificationListener(oname,
                (NotificationListener) ProActiveObject.getStubOnThis(), filter,
                handback);
        } catch (InstanceNotFoundException e) {
            logger.error("Doesn't find the object name " + oname +
                " during the registration", e);
        } catch (IOException e) {
            logger.error("Doesn't subscribe the JMX Notification listener to the Notifications",
                e);
        }
    }

    /**
     * Unsubscribes the current active object to the JMX notifications of a remote MBean.
     * @param connection The ProActiveConnection in order to connect to the remote server MBean.
     * @param oname The ObjectName of the MBean
     * @param filter A notification filter
     * @param handback A hanback
     * this method is deprecated. Use JMXNotificationManager.unsubscribeObjectFromRemoteMBean(..) instead
     */
    @Deprecated
    public void unsubscribe(ProActiveConnection connection, ObjectName oname,
        NotificationFilter filter, Object handback) {
        if (!ProActiveObject.pingActiveObject(connection)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Trying to unregister listener on a connection with terminated body. Ping faild on the connection object: " +
                    connection.toString());
            }
            return;
        }
        try {
            if (connection.isRegistered(oname)) {
                connection.removeNotificationListener(oname,
                    (NotificationListener) ProActiveObject.getStubOnThis(),
                    filter, handback);
            }
        } catch (InstanceNotFoundException e) {
            logger.error("Doesn't find the object name " + oname +
                " during the registration", e);
        } catch (ListenerNotFoundException e) {
            logger.error("Doesn't find the Notification Listener", e);
        } catch (IOException e) {
            logger.error("Can't unsubscribe the JMX Notification listener to the Notifications",
                e);
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        JMXNotificationManager.getInstance()
                              .handleNotification(notification, handback);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Warning loggers is transient
        logger = ProActiveLogger.getLogger(Loggers.JMX);
    }

    public boolean unsubscribeFromRegistry() {
        try {
            Body myBody = ProActiveObject.getBodyOnThis();
            if (myBody instanceof AbstractBody) {
                System.out.println("Unregistring JMXNotificationManager .... ");
                RemoteObjectExposer roe = ((AbstractBody) myBody).getRemoteObjectExposer();
                //  ProActiveObject.terminateActiveObject(ProActiveObject.getStubOnThis(), false);
                roe.unregisterAll();
                System.out.println(
                    "Unregistered JMXNotificationListener from the registry (body:" +
                    myBody.toString() + " )");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
