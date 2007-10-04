package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.UniqueID;


public class NamesFactory {
    private static NamesFactory instance;

    // <ID, Name>
    private Map<UniqueID, String> names;
    private Integer counter;

    private NamesFactory() {
        this.names = new HashMap<UniqueID, String>();
        this.counter = 1;
    }

    public static NamesFactory getInstance() {
        if (instance == null) {
            instance = new NamesFactory();
        }
        return instance;
    }

    /**
     * Associates a name to an unique id.
     * The returned name is 'the_given_name#a_number'.
     * Example: if the name is 'ao', the returned name is like 'ao#1'
     * @param id An unique id.
     * @param name The name to associated to the unique id.
     * @return The active object name associated to this unique id.
     */
    public synchronized String associateName(UniqueID id, String name) {
        if (id == null) {
            return null;
        }
        String recordedName = this.names.get(id);
        if (recordedName != null) {
            return recordedName;
        } else {
            name = name.substring(name.lastIndexOf(".") + 1) + "#" +
                (counter++);
            this.names.put(id, name);
            return name;
        }
    }

    /**
     * Returns the name associated to the given id.
     * @param id An unique id.
     * @return The name associated to the given id.
     */
    public String getName(UniqueID id) {
        return names.get(id);
    }
}
