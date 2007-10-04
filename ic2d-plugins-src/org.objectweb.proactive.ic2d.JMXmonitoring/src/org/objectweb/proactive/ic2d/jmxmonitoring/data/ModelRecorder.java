package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is used to have several monitoring models.
 */
public class ModelRecorder extends Observable {
    private static ModelRecorder instance;
    private static Integer counter = 1;
    private Map<String, WorldObject> models = new ConcurrentHashMap<String, WorldObject>();

    private ModelRecorder() {
    }

    /**
     * Returns the recorder of monitoring models.
     * @return The recorder
     */
    public static ModelRecorder getInstance() {
        if (instance == null) {
            instance = new ModelRecorder();
        }
        return instance;
    }

    /**
     * Records a model. And returns the title associated to this model.
     * @param model The model to add.
     * @return The title of the model
     */
    public String addModel(WorldObject model) {
        String title = "Monitoring#" + counter++;
        models.put(title, model);
        setChanged();
        notifyObservers(title);
        return title;
    }

    /**
     * Returns a model.
     * @param title The title given by the addView method
     * @return The associated view.
     */
    public WorldObject getModel(String name) {
        return models.get(name);
    }

    /**
     * Returns all the titles of the models.
     * @return The titles.
     */
    public Set<String> getNames() {
        return models.keySet();
    }
}
