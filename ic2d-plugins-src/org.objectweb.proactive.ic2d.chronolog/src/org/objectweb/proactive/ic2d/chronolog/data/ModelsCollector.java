package org.objectweb.proactive.ic2d.chronolog.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore;


/**
 * 
 * @author vbodnart
 *
 */
public final class ModelsCollector {

    /**
     * Minimal period for refreshing cached value (seconds)
     */
    public static final int MIN_REFRESH_PERIOD_IN_SECS = 3;

    /**
     * Minimal period for refreshing cached value (milliseconds)
     */
    public static final int MIN_REFRESH_PERIOD_IN_MILLIS = MIN_REFRESH_PERIOD_IN_SECS * 1000;

    /**
     * 
     */
    public static final String REFRESH_THREAD_NAME = "GraphIt-Models-Refresher";

    /**
     * The models used by this data store
     */
    protected final List<AbstractTypeModel<?>> models;

    /**
     * The underlying data store
     */
    protected final IDataStore dataStore;

    /**
     * A timer used to run models updates as tasks 
     */
    protected final Timer scheduledModelsCollector;

    /**
     * All scheduled tasks
     */
    private final List<TimerTask> scheduledTasks;

    /**
     * To know if this collector is running or not
     */
    protected volatile boolean isRunning;

    /**
     * Creates a new instance of the models collector
     * @param models
     */
    public ModelsCollector(final List<AbstractTypeModel<?>> models, final IDataStore dataStore) {
        this.models = models;
        this.dataStore = dataStore;
        this.scheduledModelsCollector = new Timer(REFRESH_THREAD_NAME);
        this.scheduledTasks = new java.util.LinkedList<TimerTask>();
    }

    /**
     * Creates a new instance of the models collector
     */
    public ModelsCollector(final IDataStore dataStore) {
        this(new ArrayList<AbstractTypeModel<?>>(), dataStore);
    }

    /**
     * Adds a model to this data store.
     * 
     * @param model The model to add
     */
    public void addModel(final AbstractTypeModel<?> model) {
        this.models.add(model);
    }

    public void removeModel(final AbstractTypeModel<?> model) {
        this.models.remove(model);
    }

    /**
     * Returns the list of models
     * 
     * @return The list of 
     */
    public List<AbstractTypeModel<?>> getModels() {
        return this.models;
    }

    /**
     * 
     * @return
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Starts collecting data
     * @return
     */
    public void startCollector() {
        // Set this collector as running
        this.isRunning = true;

        // Find all stored models
        final List<AbstractTypeModel<?>> modelsToStore = new ArrayList<AbstractTypeModel<?>>(this.models
                .size());
        for (final AbstractTypeModel<?> model : this.models) {
            if (model.needStorage()) {
                modelsToStore.add(model);
            }
        }

        // An extra task for updating data store
        TimerTask dataStoreUpdaterTask = null;
        // Only init the data store if there are some models to store 
        if (modelsToStore.size() > 0) {
            // Init the data store
            this.dataStore.init(modelsToStore, MIN_REFRESH_PERIOD_IN_SECS);

            // Create the extra task
            dataStoreUpdaterTask = new TimerTask() {
                @Override
                public final void run() {
                    ModelsCollector.this.dataStore.update();
                }
            };

            // First Schedule the store updater
            this.scheduledModelsCollector.schedule(dataStoreUpdaterTask, MIN_REFRESH_PERIOD_IN_MILLIS,
                    MIN_REFRESH_PERIOD_IN_MILLIS);
        }

        // Then schedule all tasks that run models
        for (final AbstractTypeModel<?> model : this.models) {
            // First create the task that will run the model
            final TimerTask task = new TimerTask() {
                @Override
                public final void run() {
                    model.run();
                }
            };

            // Then schedule it
            this.scheduledModelsCollector.schedule(task, model.getRefreshPeriod(), //initial delay
                    model.getRefreshPeriod());

            // Finally add it to the task list
            this.scheduledTasks.add(task);
        }

        // Finally add the extra task to tasks list
        if (dataStoreUpdaterTask != null) {
            this.scheduledTasks.add(dataStoreUpdaterTask);
        }
    }

    /**
     * Stops this collector
     */
    public void stopCollector() {
        // Cancel all tasks  
        for (final TimerTask task : this.scheduledTasks) {
            task.cancel();
        }
        // Clear the task list
        this.scheduledTasks.clear();
        //  Purge the timer so that it can be reused
        this.scheduledModelsCollector.purge();
        this.isRunning = false;
    }

    public IDataStore getDataStore() {
        return dataStore;
    }
}