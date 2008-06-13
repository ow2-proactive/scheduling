package org.objectweb.proactive.core.group;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class implements a task factory applicable for standard ProActive groups.
 * 
 * @author The ProActive Team
 *
 */
public class BasicTaskFactory implements TaskFactory {

    protected Dispatcher dispatcher;
    protected ProxyForGroup groupProxy;

    public BasicTaskFactory(ProxyForGroup groupProxy) {
        this.groupProxy = groupProxy;
        // this.dispatcher = dispatcher;
    }

    /**
     * Returns a mapping [method call --> target index]
     * 
     * It relies on an analysis of invocation parameters to determine the number
     * of invocations to be performed.
     * 
     * Note that in the first version of this class, group parameters
     * must be in *scatter* mode in order to take advantage of dynamic dispatch.
     * 
     */
    public List<MethodCall> generateMethodCalls(MethodCall mc) throws InvocationTargetException {
        List<MethodCall> methodsToDispatch = new ArrayList<MethodCall>(getNbTasks(mc));
        if (!groupProxy.isDispatchingCall(mc)) {
            // enqueue all tasks with same parameters
            if (groupProxy.isUniqueSerializationOn()) {
                mc.transformEffectiveArgumentsIntoByteArray();
            }
            // broadcast
            for (int i = 0; i < getNbTasks(mc); i++) {
                methodsToDispatch.add(mc);
            }
        } else { // isDispatchingCall == true

            for (int i = 0; i < getNbTasks(mc); i++) {
                Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
                MethodCall dispatchedMc;
                for (int j = 0; j < mc.getNumberOfParameter(); j++)
                    if (PAGroup.isScatterGroupOn(mc.getParameter(j))) {
                        individualEffectiveArguments[j] = PAGroup.get(mc.getParameter(j), i %
                            PAGroup.size(mc.getParameter(j)));
                    } else {
                        individualEffectiveArguments[j] = mc.getParameter(j);
                    }
                methodsToDispatch.add(MethodCall.getMethodCall(mc.getReifiedMethod(), mc
                        .getGenericTypesMapping(), individualEffectiveArguments, mc.getExceptionContext()));
            }

        }
        return methodsToDispatch;

    }

    private int getNbTasks(MethodCall mc) {

        int nbTasks = 0;
        // for standard proactive groups, the number of tasks is given by the
        // highest number
        // of parameters to dispatch
        int maxParameters = 0;
        boolean broadcast = false;
        if (mc.getEffectiveArguments() != null) {
            for (int i = 0; i < mc.getNumberOfParameter(); i++) {
                if (PAGroup.isGroup(mc.getParameter(i))) {
                    if (PAGroup.isScatterGroupOn(mc.getParameter(i))) {
                        int nbParams = (PAGroup.getGroup(mc.getParameter(i)).size());
                        if (nbParams > maxParameters) {
                            maxParameters = nbParams;
                        }
                    } else {
                        broadcast = true;
                    }
                }
            }
        }
        if ((broadcast == true) || (maxParameters == 0)) {
            nbTasks = groupProxy.getMemberList().size();
        } else {
            nbTasks = maxParameters;
        }

        return nbTasks;
    }

    public Queue<AbstractProcessForGroup> generateTasks(MethodCall originalMethodCall,
            List<MethodCall> methodCalls, Object result, ExceptionListException exceptionList,
            CountDownLatch doneSignal, ProxyForGroup groupProxy) {

        Queue<AbstractProcessForGroup> taskList = new ConcurrentLinkedQueue<AbstractProcessForGroup>();

        // not a broadcast: use generated method calls
        Vector<Object> memberListOfResultGroup = null;

        // if dynamic dispatch or random distribution, randomly distribute
        // tasks
        // reorder(methodCalls, originalMethodCall.getReifiedMethod());

        List<Integer> taskIndexes = getTaskIndexes(originalMethodCall, methodCalls, groupProxy
                .getMemberList().size());
        if (!(result == null)) {
            memberListOfResultGroup = initializeResultsGroup(result, methodCalls.size());
        }

        for (int i = 0; i < methodCalls.size(); i++) {
            MethodCall mc = methodCalls.get(i);
            AbstractProcessForGroup task = useOneWayProcess(mc) ? new ProcessForOneWayCall(groupProxy,
                groupProxy.getMemberList(), getTaskIndex(mc, i, groupProxy.getMemberList().size()), mc,
                PAActiveObject.getBodyOnThis(), exceptionList, doneSignal) : new ProcessForAsyncCall(
                groupProxy, groupProxy.getMemberList(), memberListOfResultGroup, taskIndexes.get(i), mc, i,
                PAActiveObject.getBodyOnThis(), doneSignal);

            setDynamicDispatchTag(task, originalMethodCall);
            taskList.offer(task);
            //			System.out.println("*** worker index = [" + i
            //					% groupProxy.getMemberList().size() + "]");
        }
        // }
        return taskList;
    }

    protected boolean useOneWayProcess(MethodCall mc) {
        return (mc.isOneWayCall() || (mc.getReifiedMethod().getReturnType() == Void.TYPE));
    }

    public int getTaskIndex(MethodCall mc, int partitioningIndex, int groupSize) {
        return partitioningIndex % groupSize;
    }

    public List<Integer> getTaskIndexes(MethodCall originalMethodCall, List<MethodCall> generatedMethodCalls,
            int nbWorkers) throws AllocationException {

        // TODO delegate annotation processing to component framework
        Dispatch dispatchAnnotation = originalMethodCall.getReifiedMethod().getAnnotation(Dispatch.class);
        if (dispatchAnnotation != null) {
            if (dispatchAnnotation.mode().equals(DispatchMode.CUSTOM)) {
                // TODO cache instance !
                try {
                    DispatchBehavior allocationBehavior = (DispatchBehavior) dispatchAnnotation.customMode()
                            .newInstance();
                    return allocationBehavior.getTaskIndexes(originalMethodCall, generatedMethodCalls,
                            nbWorkers);
                } catch (InstantiationException e) {
                    throw new AllocationException("cannot instantiate custom class for allocation behavior",
                        e);
                } catch (IllegalAccessException e) {
                    throw new AllocationException("cannot instantiate custom class for allocation behavior",
                        e);
                }
            } else {
                return dispatchAnnotation.mode().getTaskIndexes(originalMethodCall, generatedMethodCalls,
                        nbWorkers);
            }
        } else {
            return DispatchMode.UNSPECIFIED.getTaskIndexes(originalMethodCall, generatedMethodCalls,
                    nbWorkers);
        }
    }

    protected Vector<Object> initializeResultsGroup(Object result, int nbExpectedResults) {
        Vector<Object> memberListOfResultGroup = ((ProxyForGroup) ((StubObject) result).getProxy()).memberList;

        // default group size for async call == size of target group (1 result
        // per member)

        // TODO use ensureCapacity!
        for (int i = 0; i < nbExpectedResults; i++) {
            memberListOfResultGroup.add(null);
        }
        return memberListOfResultGroup;
    }

    protected void setDynamicDispatchTag(AbstractProcessForGroup task, MethodCall originalMethodCall) {
        // knowledge based means dynamic dispatch
        // info specified through proxy API has priority
        if (groupProxy.balancing().equals(DispatchMode.DYNAMIC) ||
            (groupProxy.balancing().equals(DispatchMode.UNSPECIFIED) && ((originalMethodCall
                    .getReifiedMethod().getAnnotation(Dispatch.class) != null) && (originalMethodCall
                    .getReifiedMethod().getAnnotation(Dispatch.class).mode().equals(DispatchMode.DYNAMIC))))) {
            task.setDynamicallyDispatchable(true);
        }
    }

    private static boolean generatedProcessForOneWayCall(MethodCall mc) {
        return (mc.isOneWayCall() || mc.getReifiedMethod().getReturnType() == Void.TYPE);
    }

}
