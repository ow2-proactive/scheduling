package org.objectweb.proactive.ic2d.chartit.data.store;

import java.util.List;

import org.objectweb.proactive.ic2d.chartit.data.ChartModel;


/**
 * A common interface for all data store implementation
 * @author vbodnart
 *
 */
public interface IDataStore {

    /**
     * This method must be called once all elements were added.
     *
     * @return <code>true</code> if the initialization was performed; <code>false</code> otherwise
     */
    public boolean init(final List<ChartModel> modelsToStore, final int stepInSeconds);

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

    /**
     * Returns the name of the data store.
     * 
     * @return the dataStoreName
     */
    public String getDataStoreName();
}
