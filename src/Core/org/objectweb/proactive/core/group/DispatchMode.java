package org.objectweb.proactive.core.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * This enumeration defines the dispatch behavior to be applied during a group communication operation.

 * The dispatch operation follows the partitioning operation, during which a set of tasks
 * have been generated, corresponding to a specified partitioning scheme.
 * 
 * The dispatch operation maps generated tasks to available workers, using one of the available dispatch modes.
 * 
 * 
 * @author The ProActive Team
 *
 */
@PublicAPI
public enum DispatchMode implements DispatchBehavior, Serializable {
    /**
     * Not specified: will use the default strategy.
     */
    UNSPECIFIED,

    /**
     * Tasks are statically allocated to workers in a round robin fashion, starting with worker of index 0
     */
    STATIC_ROUND_ROBIN,

    /**
     * Tasks are statically allocated to workers in a random fashion.
     */
    STATIC_RANDOM,

    /**
     * Buffered tasks are statically allocated to workers using the default allocation mode. 
     * Remaining tasks (unbuffered) are dynamically allocated to most appropriate workers.
     */
    DYNAMIC,

    /**
     * Custom static partitioning of tasks can be specified in an external class file that
     * implements the {@link AllocationBehavior} interface.
     */
    CUSTOM;

    public List<Integer> getTaskIndexes(MethodCall originalMethodCall, List<MethodCall> generatedMethodCalls,
            int nbWorkers) {
        //			int nbTasks = getExpectedNumberOfTasks(originalMethodCall, generatedMethodCalls, nbWorkers);
        List<Integer> taskIndexes = new ArrayList<Integer>(generatedMethodCalls.size());
        switch (this) {
            case UNSPECIFIED:
                taskIndexes = STATIC_ROUND_ROBIN.getTaskIndexes(originalMethodCall, generatedMethodCalls,
                        nbWorkers);
                break;
            case STATIC_ROUND_ROBIN:
                for (int i = 0; i < generatedMethodCalls.size(); i++) {
                    taskIndexes.add(i % nbWorkers);
                }
                break;
            case STATIC_RANDOM:
                for (int i = 0; i < generatedMethodCalls.size(); i++) {
                    taskIndexes.add(i);
                }
                Collections.shuffle(taskIndexes);
                break;
            case DYNAMIC:
                taskIndexes = UNSPECIFIED.getTaskIndexes(originalMethodCall, generatedMethodCalls, nbWorkers);
            default:
                taskIndexes = UNSPECIFIED.getTaskIndexes(originalMethodCall, generatedMethodCalls, nbWorkers);

        }
        return taskIndexes;
    }

}
