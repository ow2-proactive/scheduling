package org.objectweb.proactive.extensions.masterworker.interfaces;

import org.objectweb.proactive.annotation.PublicAPI;

import java.io.Serializable;


/**
 * A task which can be divided into subtasks by submitting new tasks to the master
 * and collecting results
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface DivisibleTask<R extends Serializable> extends Task<R> {

    /**
    * A task to be executed<br/>
    * @param memory access to the worker memory
    * @param master access to the master, to submit new task 
    * @return the result
    * @throws Exception any exception thrown by the task
    */
    R run(WorkerMemory memory, SubMaster master) throws Exception;
}
