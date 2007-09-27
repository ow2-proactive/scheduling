package org.objectweb.proactive.core.jmx.util;

import java.io.IOException;
import java.io.Serializable;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.jmx.ProActiveConnection;


/**
 * An IC2DListener is an active object which listens several remotes MBeans with the ProActiveConnection.
 * This listener is used by the JMXNotificationManager.
 * @author ProActive Team
 */
public class JMXNotificationListener implements NotificationListener,
    ProActiveInternalObject, Serializable {
    //private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX);
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

            /*logger.error("Doesn't find the object name " + oname +
                " during the registration", e);*/
        } catch (IOException e) {

            /*logger.error("Doesn't subscribe the JMX Notification listener to the Notifications",
                e);*/
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
                    (NotificationListener) ProActiveObject.getStubOnThis(),
                    filter, handback);
            }
        } catch (InstanceNotFoundException e) {

            /*logger.error("Doesn't find the object name " + oname +
                " during the registration", e);*/
        } catch (ListenerNotFoundException e) {

            /*logger.error("Doesn't find the Notification Listener", e);*/
        } catch (IOException e) {

            /*logger.error("Can't unsubscribe the JMX Notification listener to the Notifications",
                e);*/
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
        //logger = ProActiveLogger.getLogger(Loggers.JMX);
    }
}
