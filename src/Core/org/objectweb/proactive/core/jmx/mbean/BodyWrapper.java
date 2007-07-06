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
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX);

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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
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

        System.out.print("[" + type + "]  ");
        ObjectName source = getObjectName();
        //Object[] source = {this.objectName, this.nodeUrl};
        //NotificationSource source = new NotificationSource(objectName, nodeUrl);
        /*
        if(logger.isDebugEnabled()){
                logger.debug("Send a notification ["+ type +"] source: "+source);
        }
        */
        System.out.println("[BodyWrapper.sendNotification] source=" + source);
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
        System.out.println("[Serialisation.writeObject]");

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MBeanRegistrationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Serialization of the MBean : " + objectName);
        }

        // Default Serialization
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        System.out.println("[Serialisation.readObject]");

        in.defaultReadObject();

        // Warning objectName is transient
        if (objectName == null) {
            objectName = FactoryName.createActiveObjectName(id);
        }

        // Warning logger is transient
        logger = ProActiveLogger.getLogger(Loggers.JMX);
        if (logger.isDebugEnabled()) {
            logger.debug("Deserialization of the MBean : " + objectName);
        }

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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        new Thread() {
                public void run() {
                    while (shouldNotify) {
                        try {
                            Thread.sleep(updateFrequence);
                            //System.out.println("wake up");
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        sendNotifications();
                    }
                }
            }.start();
    }
}
