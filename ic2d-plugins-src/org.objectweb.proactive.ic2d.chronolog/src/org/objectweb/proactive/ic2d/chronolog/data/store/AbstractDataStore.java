/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.data.store;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;


/**
 * An abstract class that is used to store data. The store references model
 * elements used to provide values through a data provider.
 * <p>
 * The store needs a runnable data collector that implements the
 * <code>IRunnableDataCollector</code> interface.
 * <p>
 * The store may be used as a sliding window.
 * 
 * @see IRunnableDataCollector
 * @author The ProActive Team
 */
public abstract class AbstractDataStore {

    /**
     * The default step in seconds that can be used by the runnable data
     * collector
     */
    public static final int DEFAULT_STEP_IN_SECONDS = 4;

    /**
     * The name of this data store
     */
    protected final String dataStoreName;
    /**
     * The list of model elements used by this data store
     */
    protected final List<AbstractTypeModel> modelElements;
    /**
     * The runnable data collector
     */
    protected final IRunnableDataCollector runnableDataCollector;
    /**
     * The current step in seconds
     */
    protected final int stepInSeconds;

    /**
     * Creates a new instance of <code>AbstractDataStore</code>.
     * 
     * @param dataStoreName
     *            The name of this data store
     * @param modelElements
     *            The list of elements
     * @param stepInSeconds
     *            The step in seconds
     */
    public AbstractDataStore(final String dataStoreName, final List<AbstractTypeModel> modelElements,
            final int stepInSeconds) {
        this.dataStoreName = dataStoreName;
        this.modelElements = modelElements;
        this.stepInSeconds = stepInSeconds;
        this.runnableDataCollector = this.provideNewRunnableDataCollector();
    }

    /**
     * Creates an instance of the AbstractDataStoreModel with a default step.
     * 
     * @param dataStoreName
     *            The name of this data store
     */
    public AbstractDataStore(final String dataStoreName) {
        this(dataStoreName, new ArrayList<AbstractTypeModel>(), DEFAULT_STEP_IN_SECONDS);
    }

    /**
     * Creates an instance of the AbstractDataStoreModel.
     * 
     * @param dataStoreName
     *            The name of this data store
     * @param stepInSeconds
     *            The step in seconds
     */
    public AbstractDataStore(final String dataStoreName, final int stepInSeconds) {
        this(dataStoreName, new ArrayList<AbstractTypeModel>(), stepInSeconds);
    }

    /**
     * Returns the reference on the runnable data collector.
     * 
     * @return The instance of the current runnable collector used by this store
     */
    public final IRunnableDataCollector getRunnableDataCollector() {
        return runnableDataCollector;
    }

    /**
     * Returns the name of the data store.
     * 
     * @return the dataStoreName
     */
    public final String getDataStoreName() {
        return dataStoreName;
    }

    /**
     * This method may be overridden by subclasses to provide another collector
     * implementation.
     * 
     * @return the runnable data collector instance
     */
    public IRunnableDataCollector provideNewRunnableDataCollector() {
        return new RunnableDataCollectorImpl();
    }

    /**
     * Puts the value to the store by element.
     * 
     * @param modelElement
     *            The model element associated to the value
     * @param value
     *            The value to put
     */
    public final void putValueByElement(final AbstractTypeModel modelElement, double value) {
        this.putValueByIndex(this.modelElements.indexOf(modelElement), value);
    }

    /**
     * Adds an element to this data store.
     * 
     * @param modelElement
     *            The model element to add
     */
    public void addElement(final AbstractTypeModel modelElement) {
        this.modelElements.add(modelElement);
    }

    /**
     * Removes the element from this store.
     * 
     * @param modelElement
     *            The element to remove
     */
    public void removeElement(final AbstractTypeModel modelElement) {
        this.modelElements.remove(modelElement);
    }

    /**
     * Returns the list of model elements of this store.
     * 
     * @return The list of model elements
     */
    public final List<? extends AbstractTypeModel> getElements() {
        return this.modelElements;
    }

    /**
     * Notifies each model element of this store.
     */
    public final void notifyAllElements() {
        for (final AbstractTypeModel modelElement : this.modelElements) {
            modelElement.firePropertyChange(AbstractTypeModel.ELEMENT_CHANGED, null, null);
        }
    }

    /**
     * Initializes the underlying data store implementation and starts a new
     * Thread with a runnable data collector.
     * 
     * @return <code>true</code> if the initialization was performed; <code>false</code> otherwise
     */
    public final boolean initDataStoreAndStartCollectingData() {
        if (this.init()) {
            new Thread(this.runnableDataCollector).start(); // The runnable may be
            // used as a task for a
            // thread pool
            return true;
        }
        return false;
    }

    // /////////////////
    // Abstract methods
    // /////////////////

    /**
     * Puts the value by the index of the element.
     * 
     * @see NumberBasedTypeModel
     * @param index
     *            The index of the element
     * @param value
     *            The value to set
     */
    public abstract void putValueByIndex(final int index, final double value);

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
    public abstract boolean init();

    /**
     * Stores all values, this method must be called after all values for each
     * element were added.
     */
    public abstract void store();

    /**
     * Closes this data store.
     */
    public abstract void close();

    /**
     * To know if this data store is closed.
     * 
     * @return True if this data store is closed False otherwise
     */
    public abstract boolean isClosed();

    /**
     * Dumps this data store to the standard output.
     */
    public abstract void dump();

    /**
     * Returns the left bound time in seconds.
     * 
     * @return the left bound time
     */
    public abstract long getLeftBoundTime();

    /**
     * Returns the right bound time in seconds.
     * 
     * @return the right bound time
     */
    public abstract long getRightBoundTime();

    /**
     * Iterates through all elements of this store and asks each element
     * provider for a value.
     */
    public abstract void provideValuesFromElements();

    /**
     * The default implementation of the runnable data collector.
     * 
     * @see IRunnableDataCollector
     * @author The ProActive Team
     */
    final class RunnableDataCollectorImpl implements IRunnableDataCollector {
        /**
         * Since the variable is volatile no need to synchronize methods that
         * want to access it.
         */
        protected volatile boolean isRunning;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            final long stepInMs = stepInSeconds * 1000; // in ms
            this.isRunning = true;
            try {
                while (isRunning) {
                    Thread.sleep(stepInMs);
                    provideValuesFromElements();
                    if (isRunning) {
                        store();
                        notifyAllElements();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.proactive.ic2d.chronolog.data.store.IRunnableDataCollector#cancel()
         */
        public void cancel() {
            this.isRunning = false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.proactive.ic2d.chronolog.data.store.IRunnableDataCollector#isRunning()
         */
        public boolean isRunning() {
            return this.isRunning;
        }
    }
}