package org.ow2.proactive.scheduler.common.jmx.test;

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
 * @author The ProActive Team
 */
@SuppressWarnings("serial")
public class ConnectionListener implements NotificationListener, Serializable {
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
    public void listenTo(ObjectName name, NotificationFilter filter, Object handback) throws IOException {
        try {
            this.connection.addNotificationListener(name,
                    (ConnectionListener) PAActiveObject.getStubOnThis(), filter, handback);
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
            this.connection.removeNotificationListener(name, (ConnectionListener) PAActiveObject
                    .getStubOnThis());
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