package org.objectweb.proactive.core.component.group;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.collectiveitfs.MulticastHelper;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceTypeImpl;
import org.objectweb.proactive.core.group.AbstractProcessForGroup;
import org.objectweb.proactive.core.group.BasicTaskFactory;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;


public class ComponentTaskFactoryBasic extends BasicTaskFactory {

    public ComponentTaskFactoryBasic(ProxyForGroup groupProxy) {
        super(groupProxy);
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<MethodCall> generateMethodCalls(MethodCall mc) throws InvocationTargetException {
        ProxyForComponentInterfaceGroup parent = ((ProxyForComponentInterfaceGroup) groupProxy).getParent();
        if (parent != null && (((ProActiveInterfaceTypeImpl) parent.getInterfaceType()).isFcCollective())) {
            // ok we are dealing with a delegation proxy for a collective
            // interface
            // use helper class
            try {
                List<MethodCall> methodCalls = MulticastHelper.generateMethodCallsForMulticastDelegatee(
                        (ProActiveComponent) parent.getOwner(), mc,
                        (ProxyForComponentInterfaceGroup) groupProxy);
                return methodCalls;
            } catch (ParameterDispatchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new InvocationTargetException(e);
            }

        } else {
            return super.generateMethodCalls(mc);
        }
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
            AbstractProcessForGroup task = useOneWayProcess(mc) ? new ComponentProcessForOneWayCall(
                groupProxy, groupProxy.getMemberList(),
                getTaskIndex(mc, i, groupProxy.getMemberList().size()), mc, ProActive.getBodyOnThis(),
                exceptionList, doneSignal)

            : new ComponentProcessForAsyncCall(groupProxy, groupProxy.getMemberList(),
                memberListOfResultGroup, taskIndexes.get(i), mc, i, ProActive.getBodyOnThis(), doneSignal);

            setDynamicDispatchTag(task, originalMethodCall);
            taskList.offer(task);
            //          System.out.println("*** worker index = [" + i
            //                  % groupProxy.getMemberList().size() + "]");
        }
        // }
        return taskList;
    }
}
