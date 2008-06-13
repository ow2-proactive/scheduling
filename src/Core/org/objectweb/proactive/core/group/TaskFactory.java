package org.objectweb.proactive.core.group;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.core.mop.MethodCall;


/**
 * Specifies the partitioning of data into tasks that are mapped to available workers, 
 * according to a scheme defined either through annotation on the invoked method, or directly on the group proxy.
 * 
 * 
 * @author The ProActive Team
 *
 */
public interface TaskFactory {

    /**
     * 
     * @param methodCalls maps each method call generated to the index of the target member it should be assigned to
     * This is overriden when performing dynamic dispatch.
     * @param result
     * @param exceptionList
     * @param doneSignal
     * @param originalReifiedMethod TODO
     * @param groupProxy TODO
     * @return
     */
    public Queue<AbstractProcessForGroup> generateTasks(MethodCall originalMethodCall,
            List<MethodCall> methodCalls, Object result, ExceptionListException exceptionList,
            CountDownLatch doneSignal, ProxyForGroup groupProxy);

    /**
     * 
     * @param mc
     * @return
     * @throws InvocationTargetException
     */
    public List<MethodCall> generateMethodCalls(MethodCall mc) throws InvocationTargetException;

    /**
     * Static mapping of a task to a worker
     * @param mc a method call
     * @param partitioningIndex index allocated in partitioning step,overridable in dispatch step
     * @param groupSize number of workers
     * @return
     */
    public int getTaskIndex(MethodCall mc, int partitioningIndex, int groupSize);
}
