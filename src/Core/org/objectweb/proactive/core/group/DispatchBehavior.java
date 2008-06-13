package org.objectweb.proactive.core.group;

import java.util.List;

import org.objectweb.proactive.core.mop.MethodCall;


/**
 * Specifies the mapping between tasks and workers.
 * 
 * @author The ProActive Team
 *
 */
public interface DispatchBehavior {

    /**
     * Maps a list of method calls (corresponding to tasks to be executed on workers), to
     * indexes of the workers (no information is available from workers).
     * 
     * @param originalMethodCall the reified method call invoked on the group
     * @param generatedMethodCalls the reified method calls generated according to the partitioning scheme, and
     * that should be invoked on workers
     * @param nbWorkers the number of available workers
     * @return the mapping tasks --> worker index, for the given list of tasks
     */
    public List<Integer> getTaskIndexes(MethodCall originalMethodCall,
            final List<MethodCall> generatedMethodCalls, int nbWorkers);

}
