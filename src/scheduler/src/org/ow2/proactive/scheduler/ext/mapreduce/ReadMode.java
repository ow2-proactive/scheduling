package org.ow2.proactive.scheduler.ext.mapreduce;

/**
 * The {@link ReadMode} class enumerates the possible read mode a task of the
 * ProActive MapReduce workflow can use.
 *
 * @author The ProActive Team
 *
 */
public enum ReadMode {

    /**
     * It indicates that the data are a read remotely. This means the data are
     * not transferred on the node the task executes on.
     */
    remoteRead("remoteRead", PropertyType.STRING),

    /**
     * It indicates that the data are transferred entirely on the node the task
     * executes on before the task begin to read them.
     */
    fullLocalRead("fullLocalRead", PropertyType.STRING),

    /**
     * It indicates that only the data the task needs are transferred on the
     * node the task executes on.
     */
    partialLocalRead("partialLocalRead", PropertyType.STRING);

    /**
     * The key of the property
     */
    protected String key = null;

    /**
     * The type of the property
     */
    protected PropertyType propertyType = null;

    /**
     * It creates a new readMode property
     *
     * @param key
     *            the key of the property
     * @param propertyType
     *            the type of the property
     */
    ReadMode(String key, PropertyType propertyType) {
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
     * Supported types for {@link ReadMode} properties
     *
     * @author The ProActive Team
     *
     */
    protected enum PropertyType {
        STRING
    }
}
