package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.util.Collection;

import javax.management.ObjectName;

import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;


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
     * Returns the name of the body of the active object that can be used for displaying information
     * @return the name of the body of the active object
     */
    public String getName();

    /**
     * Returns the url of the node containing the active object.
     * @return Returns the url of the node containing the active object
     */
    public String getNodeUrl();

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

    /**
     * Returns a collection of BasicTimer.
     * @param timerNames
     * @return a collection of BasicTimer
     */
    public Collection<BasicTimer> getTimersSnapshot(String[] timerNames);

    /**
     * Migrate the body to the given node.
     * @param nodeUrl
     * @throws MigrationException
     */
    public void migrateTo(String nodeUrl) throws MigrationException;
}
