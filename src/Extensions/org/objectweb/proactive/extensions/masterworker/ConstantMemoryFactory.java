package org.objectweb.proactive.extensions.masterworker;

import org.objectweb.proactive.extensions.masterworker.interfaces.MemoryFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;


/**
 * EmptyMemoryFactory
 *
 * @author The ProActive Team
 */
public class ConstantMemoryFactory implements MemoryFactory {

    private HashMap<String, Serializable> memory;

    public ConstantMemoryFactory() {
        this.memory = new HashMap<String, Serializable>();
    }

    public ConstantMemoryFactory(HashMap<String, Serializable> memory) {
        this.memory = memory;
    }

    public Map<String, Serializable> newMemoryInstance() {
        return memory;
    }
}
