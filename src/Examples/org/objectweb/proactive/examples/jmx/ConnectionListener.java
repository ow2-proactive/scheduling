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
package org.objectweb.proactive.examples.jmx;

import java.io.IOException;
import java.io.Serializable;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.api.PAActiveObject;


/**
 * <p>Listens  to and handles JMX Notifications occuring on a Mbean Server
 * <p>This object has to be an Active Objet in order to receive remotely JMX Notifications
 * @author ProActive Team
 */
public class ConnectionListener implements NotificationListener, Serializable {
    private static final long serialVersionUID = 651518694903154592L;
    private MBeanServerConnection connection;

    /**
     * Empty no args constructor
     *
     */
    public ConnectionListener() {
    }

    /**
     * Build a Connection Listener thanks to the specified MBean Server Connection
     * @param connection a MBean Server Connection
     */
    public ConnectionListener(MBeanServerConnection connection) {
        this.connection = connection;
    }

    /**
     *  Listen to MBean corresponding to the object name
     * @param name the object name of the MBean one want to listen to
     * @throws IOException
     */
    public void listenTo(ObjectName name, NotificationFilter filter,
        Object handback) throws IOException {
        try {
            this.connection.addNotificationListener(name,
                (ConnectionListener) PAActiveObject.getStubOnThis(), filter,
                handback);
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Stop listening to the MBean corresponding to the objectName
     * @param name the objectname of the MBean one want to stop listening to
     * @throws IOException
     */
    public void stopListening(ObjectName name) throws IOException {
        try {
            this.connection.removeNotificationListener(name,
                (ConnectionListener) PAActiveObject.getStubOnThis());
        } catch (InstanceNotFoundException e) {
            throw new IOException(e.getMessage());
        } catch (ListenerNotFoundException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        System.out.println("Receiving Notification : " + notification);
    }
}
