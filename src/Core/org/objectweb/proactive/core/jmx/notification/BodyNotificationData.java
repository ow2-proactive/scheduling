package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * Used in the JMX notifications
 * @author ProActive Team
 */
public class BodyNotificationData implements Serializable {

    /** The unique id of the body */
    private UniqueID id;

    /** The jobID */
    private String jobID;

    /** The nodeUrl */
    private String nodeUrl;

    /** The className */
    private String className;

    public BodyNotificationData() {
        // No args constructor
    }

    /**
     * Creates a new BodyNotificationData.
     * @param bodyID Id of the new active object.
     * @param jobID Id of the job of the active object.
     * @param nodeURL Url of the node containing this active object.
     * @param className Name of the classe used to create the active object.
     */
    public BodyNotificationData(UniqueID bodyID, String jobID, String nodeURL,
        String className) {
        this.id = bodyID;
        this.jobID = jobID;
        this.nodeUrl = nodeURL;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public UniqueID getId() {
        return id;
    }

    public String getJobID() {
        return jobID;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }
}
