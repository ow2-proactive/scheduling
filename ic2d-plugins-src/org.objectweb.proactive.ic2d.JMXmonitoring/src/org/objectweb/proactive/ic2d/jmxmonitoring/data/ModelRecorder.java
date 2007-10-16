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
 *  Contributor(s):
 *
 * ################################################################
 */
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
