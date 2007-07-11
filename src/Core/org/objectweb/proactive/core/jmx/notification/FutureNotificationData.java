package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * Used by the JMX Notifications
 * @author ProActive Team
 */
public class FutureNotificationData implements Serializable {

    /**
     * UniqueID of the body which wait this future
     */
    private UniqueID bodyID;

    /**
     * UniqueID of the body which create this future
     */
    private UniqueID creatorID;

    public FutureNotificationData() {
        // No args constructor
    }

    /**
     * Creates an new FuturNotificationData, used by JMX Notification
     * @param bodyID UniqueID of the body which wait this future
     * @param creatorID UniqueID of the body which create this future
     */
    public FutureNotificationData(UniqueID bodyID, UniqueID creatorID) {
        this.bodyID = bodyID;
        this.creatorID = creatorID;
    }

    /**
     * Returns the UniqueID of the body which wait this future
     * @return The UniqueID of the body which wait this future
     */
    public UniqueID getBodyID() {
        return bodyID;
    }

    /**
     * Returns the UniqueID of the body which create this future
     * @return The UniqueID of the body which create this future
     */
    public UniqueID getCreatorID() {
        return creatorID;
    }
}
