package org.objectweb.proactive.core.jmx.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * @author ProActive Team
 */
public class BodyWrapper extends NotificationBroadcasterSupport
    implements Serializable, BodyWrapperMBean {
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);

    /** Timeout between updates */
    private long updateFrequence = 3000;
    private transient ConcurrentLinkedQueue<Notification> notifications;
    private boolean shouldNotify = true;

    /** Unique id of the active object */
    private UniqueID id;

    /** Body of the active object */
    private transient Body body;

    /** ObjectName of this MBean */
    private transient ObjectName objectName;

    /** */
    private Boolean listening = false;

    /** The url of node containing this active object. */
    private transient String nodeUrl;

    /** Used by the JMX notifications */
    private long counter = 1;

    public BodyWrapper() {

        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new BodyWrapper MBean, representing an active object.
     * @param oname
     * @param body
     */
    public BodyWrapper(ObjectName oname, AbstractBody body) {
        this.objectName = oname;
        this.body = body;
        this.id = body.getID();

        this.nodeUrl = body.getNodeURL();
        this.notifications = new ConcurrentLinkedQueue<Notification>();

        new Thread() {
                public void run() {
                    while (shouldNotify) {
                        try {
                            Thread.sleep(updateFrequence);
                        } catch (InterruptedException e) {
                            logger.error("The JMX notifications sender thread was interrupted",
                                e);
                        }
                        sendNotifications();
                    }
                }
            }.start();
    }

    public UniqueID getID() {
        return this.id;
    }

    public String getName() {
        String name = "undefined";
        if (body != null) {
            name = this.body.getName();
        }
        return name;
    }

    public ObjectName getObjectName() {
        //return this.objectName;
        return FactoryName.createActiveObjectName(id);
    }

    private void sendNotifications() {
        if (notifications == null) {
            this.notifications = new ConcurrentLinkedQueue<Notification>();
        }
        synchronized (notifications) {
            if (!notifications.isEmpty()) {
                ObjectName source = getObjectName();

                //Object[] source = {this.objectName, this.nodeUrl};
                //NotificationSource source = new NotificationSource(this.objectName, this.nodeUrl);
                Notification n = new Notification(NotificationType.unknown,
                        source, counter++);
                n.setUserData(notifications);
                super.sendNotification(n);
                notifications.clear();
            }
        }
    }

    public void sendNotification(String type) {
        sendNotification(type, null);
    }

    public void sendNotification() {
        Notification notification = new Notification("TEST", this, 1);
        super.sendNotification(notification);
    }

    public void sendNotification(String type, Object userData) {
        if (notifications == null) {
            this.notifications = new ConcurrentLinkedQueue<Notification>();
        }

        logger.debug("[" + type + "]  ");
        ObjectName source = getObjectName();
        //Object[] source = {this.objectName, this.nodeUrl};
        //NotificationSource source = new NotificationSource(objectName, nodeUrl);
        /*
        if(logger.isDebugEnabled()){
                logger.debug("Send a notification ["+ type +"] source: "+source);
        }
        */
        logger.debug("[BodyWrapper.sendNotification] source=" + source);
        Notification notification = new Notification(type, source, counter++);
        notification.setUserData(userData);

        if (type.equals(NotificationType.migrationFinished)) {
            sendNotifications();
            sendNotification(notification);
        } else {
            notifications.add(notification);
        }
    }

    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        logger.debug("[Serialisation.writeObject]");

        // Send the notifications before migrates.
        if (!notifications.isEmpty()) {
            sendNotifications();
        }

        // Unregister the MBean from the MBean Server.
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs.isRegistered(objectName)) {
            try {
                mbs.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                logger.error("The objectName " + objectName +
                    " was not found during the serialization of the MBean", e);
            } catch (MBeanRegistrationException e) {
                logger.error("The MBean " + objectName +
                    " can't be unregistered from the MBean server during the serialization of the MBean",
                    e);
            }
        }

        logger.debug("Serialization of the MBean : " + objectName);

        // Default Serialization
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        logger.debug("[Serialisation.readObject]");

        in.defaultReadObject();

        // Warning objectName is transient
        if (objectName == null) {
            objectName = FactoryName.createActiveObjectName(id);
        }

        // Warning logger is transient
        logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
        logger.debug("Deserialization of the MBean : " + objectName);

        // Warning nodeUrl is transient
        if (nodeUrl == null) {
            nodeUrl = body.getNodeURL();
        }

        // Warning notifications is transient
        if (notifications == null) {
            this.notifications = new ConcurrentLinkedQueue<Notification>();
        }

        // Register the MBean into the MBean Server
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(this, objectName);
        } catch (InstanceAlreadyExistsException e) {
            logger.error("A Mean is already registered with this objectName " +
                objectName, e);
        } catch (MBeanRegistrationException e) {
            logger.error("The MBean " + objectName +
                " can't be registered on the MBean server during the deserialization of the MBean",
                e);
        } catch (NotCompliantMBeanException e) {
            logger.error("Execption throws during the deserialization of the MBean",
                e);
        }

        new Thread() {
                public void run() {
                    while (shouldNotify) {
                        try {
                            Thread.sleep(updateFrequence);
                        } catch (InterruptedException e) {
                            logger.error("The JMX notifications sender thread was interrupted",
                                e);
                        }
                        sendNotifications();
                    }
                }
            }.start();
    }
}
