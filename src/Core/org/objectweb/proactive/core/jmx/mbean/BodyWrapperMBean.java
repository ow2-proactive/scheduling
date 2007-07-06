package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;

import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;


/**
 * MBean representing an active object.
 * @author ProActive Team
 */
public interface BodyWrapperMBean extends Serializable {

    /**
     * Returns the unique id.
     * @return The unique id of this active object.
     */
    public UniqueID getID();

    /**
     * Retuns the name of the active object.
     * @return The name of the active object.
     */
    public String getName();

    /**
     * Send a new notification.
     * @param type The type of the notification. See {@link NotificationType}
     */
    public void sendNotification(String type);

    /**
     * Send a new notification.
     * @param type Type of the notification. See {@link NotificationType}
     * @param userData The user data.
     */
    public void sendNotification(String type, Object userData);

    /**
     * Returns the object name used for this MBean.
     * @return The object name used for this MBean.
     */
    public ObjectName getObjectName();
}
