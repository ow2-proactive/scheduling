package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.jmx.naming.FactoryName;


/**
 * Used in the JMX notifications
 * @author ProActive Team
 */
public class RuntimeNotificationData implements Serializable {

    /** The name of the creator of the registered ProActiveRuntime */
    private String creatorID;

    /** The url of the ProActiveRuntime */
    private String runtimeUrl;

    /** The protocol used to register the registered ProActiveRuntime when created */
    private String creationProtocol;

    /** The name of the registered ProActiveRuntime */
    private String vmName;

    /**
     * Empty constructor
     */
    public RuntimeNotificationData() {
        // No args constructor
    }

    /** Creates a new RuntimeNotificationData
     * @param creatorID The name of the creator of the registered ProActiveRuntime
     * @param runtimeUrl The url of the ProActiveRuntime
     * @param creationProtocol The protocol used to register the registered ProActiveRuntime when created
     * @param vmName The name of the registered ProActiveRuntime
     */
    public RuntimeNotificationData(String creatorID, String runtimeUrl,
        String creationProtocol, String vmName) {
        this.creatorID = creatorID;
        this.creationProtocol = creationProtocol;
        this.vmName = vmName;

        this.runtimeUrl = FactoryName.getCompleteUrl(runtimeUrl);
    }

    /**
     * Returns The protocol used to register the registered ProActiveRuntime when created
     * @return The protocol used to register the registered ProActiveRuntime when created
     */
    public String getCreationProtocol() {
        return this.creationProtocol;
    }

    /**
     * Returns The name of the creator of the registered ProActiveRuntime
     * @return The name of the creator of the registered ProActiveRuntime
     */
    public String getCreatorID() {
        return this.creatorID;
    }

    /**
     * Returns The name of the registered ProActiveRuntime
     * @return The name of the registered ProActiveRuntime
     */
    public String getVmName() {
        return this.vmName;
    }

    /**
     * Returns The url of the ProActiveRuntime
     * @return The url of the ProActiveRuntime
     */
    public String getRuntimeUrl() {
        return this.runtimeUrl;
    }
}
