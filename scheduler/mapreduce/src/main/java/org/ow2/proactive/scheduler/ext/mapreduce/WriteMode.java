package org.ow2.proactive.scheduler.ext.mapreduce;

/**
 * The {@link WriteMode} class enumerates the possible write mode a task of the
 * ProActive MapReduce workflow can use
 *
 * @author The ProActive Team
 *
 */
public enum WriteMode {

    /**
     * It indicates that the data are written directly into the OUTPUT space of
     * the ProActive MapReduce workflow.
     */
    remoteWrite("remoteWrite", PropertyType.STRING),

    /**
     * It indicates that the data are written first into the LOCAL space of the
     * task of the ProActive MapReduce workflow and successively they are
     * transferred into the ProActive MapReduce workflow OUTPUT space.
     */
    localWrite("localWrite", PropertyType.STRING);

    /**
     * The key of the property
     */
    protected String key = null;

    /**
     * The type of the property
     */
    protected PropertyType propertyType = null;

    /**
     * It creates a new {@link WriteMode} property
     *
     * @param key
     *            the key of the property
     * @param propertyType
     *            the type of the property
     */
    WriteMode(String key, PropertyType propertyType) {
        this.key = key;
        this.propertyType = propertyType;
    }

    /**
     * It retrieves the key of the property
     *
     * @return the key of the property
     */
    public String getKey() {
        return key;
    }

    /**
     * Supported types for {@link WriteMode}
     *
     * @author The ProActive Team
     *
     */
    protected enum PropertyType {
        STRING
    }

}
