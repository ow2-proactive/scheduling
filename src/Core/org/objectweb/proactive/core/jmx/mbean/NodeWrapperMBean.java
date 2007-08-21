package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;


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
     * @throws ProActiveException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     */
    public List<ObjectName> getActiveObjects()
        throws ProActiveException, MalformedObjectNameException,
            NullPointerException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException;

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
