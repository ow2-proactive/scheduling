package org.objectweb.proactive.extensions.masterworker.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the worker memory
 *
 * @author The ProActive Team
 */
public class WorkerMemoryImpl implements WorkerMemory {
    /**
    * The memory of the worker <br>
    * the worker can keep some data between different tasks executions <br>
    * e.g. connection to a database, file descriptor, etc ...
    */
    private Map<String, Object> memory;

    public WorkerMemoryImpl(Map<String, Serializable> memory) {
        this.memory = new HashMap<String, Object>(memory);
    }

    /**
     * {@inheritDoc}
     */
    public void save(final String dataName, final Object data) {
        memory.put(dataName, data);
    }

    /**
         * {@inheritDoc}
         */
    public Object load(final String dataName) {
        return memory.get(dataName);
    }

    /**
     * {@inheritDoc}
     */
    public void erase(final String dataName) {
        memory.remove(dataName);
    }

    public void clear() {
        memory.clear();
    }

}
