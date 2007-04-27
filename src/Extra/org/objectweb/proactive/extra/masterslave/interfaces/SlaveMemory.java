package org.objectweb.proactive.extra.masterslave.interfaces;


/**
 * This interface gives access to the memory of a slave, a task can record data in this memory under a specific name. <br/>
 * This data could be loaded later on by another task <br/>
 * @author fviale
 *
 */
public interface SlaveMemory {

    /**
     * Save data under a specific name
     * @param name name of the data
     * @param data data to be saved
     */
    void save(String name, Object data);

    /**
     * Load some data previously saved
     * @param name the name under which the data was saved
     * @return the data
     */
    Object load(String name);

    /**
     * Erase some data previously saved
     * @param name the name of the data which need to be erased
     */
    void erase(String name);
}
