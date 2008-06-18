package org.objectweb.proactive.extensions.masterworker.interfaces;

import java.io.Serializable;
import java.util.Map;


/**
 * This class defines the general contract of instantiating a memory instance for a new deployed worker
 */
public interface MemoryFactory extends Serializable {
    /**
     * Returns a new memory instance for a worker which has just been deployed
     *
     * @return memory instance;
     */
    Map<String, Serializable> newMemoryInstance();
}
