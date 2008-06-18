package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.extensions.masterworker.TaskException;

import java.io.Serializable;
import java.util.List;


/**
 * Executor
 *
 * This interface represents an access to the Monte-Carlo engine for running a list of engine task in parallel
 *  
 *
 * @author The ProActive Team
 */
public interface Executor {

    /**
     * Asks the engine to run a list of engine tasks in parallel.<br/>
     * As each engine task returns a Serializable object the general result of the parallel tasks<br/>
     * are a list of these objects, with coherent ordering.
     * @param engineTasks list of tasks to run in parallel
     * @return a list of objects as output
     * @throws TaskException is an exception occured during the execution of the user code 
     */
    public List<Serializable> solve(List<EngineTask> engineTasks) throws TaskException;
}
