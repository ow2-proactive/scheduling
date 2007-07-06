package org.objectweb.proactive.extensions.jmx.util;

import java.io.IOException;
import java.io.Serializable;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.extensions.jmx.ProActiveConnection;


/**
 * An IC2DListener is an active object which listens several remotes MBeans with the ProActiveConnection.
 * This listener is used by the JMXNotificationManager.
 * @author ProActive Team
 */
public class JMXNotificationListener implements NotificationListener,
    ProActiveInternalObject, Serializable {
    public JMXNotificationListener() {
        // Empty Constructor
    }

    /**
     * Subscribes the current active object to the JMX notifications of a remote MBean.
     * @param connection The ProActiveConnection in order to connect to the remote server MBean.
     * @param oname The ObjectName of the MBean
     * @param filter A notification filter
     * @param handback A hanback
     */
    public void subscribe(ProActiveConnection connection, ObjectName oname,
        NotificationFilter filter, Object handback) {
        try {
            connection.addNotificationListener(oname,
                (NotificationListener) ProActive.getStubOnThis(), filter,
                handback);
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribes the current active object to the JMX notifications of a remote MBean.
     * @param connection The ProActiveConnection in order to connect to the remote server MBean.
     * @param oname The ObjectName of the MBean
     * @param filter A notification filter
     * @param handback A hanback
     */
    public void unsubscribe(ProActiveConnection connection, ObjectName oname,
        NotificationFilter filter, Object handback) {
        try {
            if (connection.isRegistered(oname)) {
                connection.removeNotificationListener(oname,
                    (NotificationListener) ProActive.getStubOnThis(), filter,
                    handback);
            }
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ListenerNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        JMXNotificationManager.getInstance()
                              .handleNotification(notification, handback);
    }
}
