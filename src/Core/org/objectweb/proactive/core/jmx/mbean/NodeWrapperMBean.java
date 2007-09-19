package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.util.List;

import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.notification.NotificationType;


/**
 * MBean representing a Node.
 * @author ProActive Team
 */
public interface NodeWrapperMBean extends Serializable {

    /**
     * Returns the url of the node.
     * @return The url of the node.
     */
    public String getURL();

    /**
     * Returns a list of Object Name used by the MBeans of the active objects containing in the Node.
     * @return The list of ObjectName of MBeans representing the active objects of this node.
     */
    public List<ObjectName> getActiveObjects();

    /**
     * Sends a new notification.
     * @param type The type of the notification. See {@link NotificationType}
     */
    public void sendNotification(String type);

    /**
     * Sends a new notification.
     * @param type Type of the notification. See {@link NotificationType}
     * @param userData The user data.
     */
    public void sendNotification(String type, Object userData);

    /**
     * Returns the object name used for this MBean.
     * @return The object name used for this MBean.
     */
    public ObjectName getObjectName();

    /**
     * Returns the name of the virtual node by which the node
     * has been instancied if any.
     * @return the name of the virtual node.
     */
    public String getVirtualNodeName();

    /**
     * Returns the jobId.
     * @return The jobId.
     */
    public String getJobId();
}
