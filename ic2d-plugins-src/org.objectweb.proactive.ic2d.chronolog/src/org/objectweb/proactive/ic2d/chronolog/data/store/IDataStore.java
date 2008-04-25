package org.objectweb.proactive.ic2d.chronolog.data.store;

import java.util.List;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;


public interface IDataStore {

    // /////////////////
    // Abstract methods
    // /////////////////

    /**
     * Returns the name of the data store.
     * 
     * @return the dataStoreName
     */
    public String getDataStoreName();

    /**
     * Puts the value by the name of the element data provider.
     * 
     * @see NumberBasedTypeModel
     * @param dataProviderName
     *            The name of the data provider
     * @param value
     *            The value to set
     */
    public abstract void setValueByName(final String dataProviderName, final double value);

    /**
     * This method must be called once all elements were added.
     *
     * @return <code>true</code> if the initialization was performed; <code>false</code> otherwise
     */
    public boolean init(final List<AbstractTypeModel<?>> modelsToStore, final int stepInSeconds);

    /**
     * Stores all values, this method must be called after all values for each
     * element were added.
     */
    public void update();

    /**
     * Closes this data store.
     */
    public void close();

    /**
     * To know if this data store is closed.
     * 
     * @return True if this data store is closed False otherwise
     */
    public boolean isClosed();

    /**
     * Dumps this data store to the standard output.
     */
    public void dump();

    /**
     * Returns the left bound time in seconds.
     * 
     * @return the left bound time
     */
    public long getLeftBoundTime();

    /**
     * Returns the right bound time in seconds.
     * 
     * @return the right bound time
     */
    public long getRightBoundTime();
}
