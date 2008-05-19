package org.objectweb.proactive.ic2d.chartit.data;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.ic2d.chartit.data.store.IDataStore;


public class ChartModelContainer {
    /**
     * Minimal period for refreshing models (seconds)
     */
    public static final int MIN_REFRESH_PERIOD_IN_SECS = 3;

    /**
     * Minimal period for refreshing models (milliseconds)
     */
    public static final int MIN_REFRESH_PERIOD_IN_MILLIS = MIN_REFRESH_PERIOD_IN_SECS * 1000;

    /**
     * Maximal period for refreshing models (seconds)
     */
    public static final int MAX_REFRESH_PERIOD_IN_SECS = 100;

    /**
     * Default period for refreshing models (seconds)
     */
    public static final int DEFAULT_REFRESH_PERIOD_IN_SECS = 4;

    /**
     * Default refresh thread name
     */
    public static final String REFRESH_THREAD_NAME = "ChartIt-Models-Refresher";

    /**
     * The models used by this data store
     */
    protected final List<ChartModel> models;

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
    public ChartModelContainer(final List<ChartModel> models, final IDataStore dataStore) {
        this.models = models;
        this.dataStore = dataStore;
        this.scheduledModelsCollector = new Timer(REFRESH_THREAD_NAME);
        this.scheduledTasks = new java.util.LinkedList<TimerTask>();
    }

    /**
     * Creates a new instance of the models collector
     */
    public ChartModelContainer(final IDataStore dataStore) {
        this(new ArrayList<ChartModel>(), dataStore);
    }

    /**
     * Adds a model to this data store.
     * 
     * @param model The model to add
     */
    public void addModel(final ChartModel model) {
        this.models.add(model);
    }

    public void removeModel(final ChartModel model) {
        this.models.remove(model);
    }

    public void removeByName(String name) {
        ChartModel toRemove = this.getModelByName(name);
        if (toRemove != null)
            this.models.remove(toRemove);
    }

    public ChartModel getModelByName(String name) {
        for (ChartModel c : this.models) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public ChartModel createNewChartModel() {
        ChartModel c = new ChartModel(ChartModel.DEFAULT_CHART_NAME + this.models.size());
        this.models.add(c);
        return c;
    }

    /**
     * Returns the list of models
     * 
     * @return The list of 
     */
    public List<ChartModel> getModels() {
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

        // Find all providers from stored models
        final List<ChartModel> modelsToStore = new ArrayList<ChartModel>();
        for (final ChartModel model : this.models) {
            if (model.isChronological()) {
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
                    ChartModelContainer.this.dataStore.update();
                }
            };

            // First Schedule the store updater
            this.scheduledModelsCollector.schedule(dataStoreUpdaterTask, MIN_REFRESH_PERIOD_IN_MILLIS,
                    MIN_REFRESH_PERIOD_IN_MILLIS);
        }

        // Then schedule all tasks that run models
        for (final ChartModel model : this.models) {
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

    public static final void main(String[] args) {
        // Create an object with an immutable property and set the value
        // of the immutable property in the constructor
        String s = "toto";
        Integer i = new Integer(5);
        Object[] arr = new Object[] { "s", "v", "u" };
        try {
            // Create the encoder
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(
                "outfilename.xml")));

            // Specify to the encoder, the name of the property that is associated
            // with the constructor's parameter(s)
            //            String[] propertyNames = new String[]{"prop"};
            //            encoder.setPersistenceDelegate(MyClass3.class,
            //                new DefaultPersistenceDelegate(propertyNames);

            // Serialize the object into XML
            encoder.writeObject(s);
            encoder.writeObject(i);
            encoder.writeObject(arr);
            encoder.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}