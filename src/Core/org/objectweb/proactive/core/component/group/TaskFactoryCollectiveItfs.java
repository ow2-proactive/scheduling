package org.objectweb.proactive.core.component.group;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.objectweb.proactive.core.component.collectiveitfs.MulticastHelper;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceTypeImpl;
import org.objectweb.proactive.core.group.AbstractProcessForGroup;
import org.objectweb.proactive.core.group.BasicTaskFactory;
import org.objectweb.proactive.core.group.Dispatch;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.group.TaskFactory;
import org.objectweb.proactive.core.mop.MethodCall;


public class TaskFactoryCollectiveItfs extends BasicTaskFactory implements TaskFactory {

    public TaskFactoryCollectiveItfs(ProxyForGroup groupProxy) {
        super(groupProxy);
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

    @Override
    public int getTaskIndex(MethodCall mc, int partitioningIndex, int groupSize) {
        // TODO Auto-generated method stub
        return super.getTaskIndex(mc, partitioningIndex, groupSize);
    }

    public void setDynamicDispatchTag(AbstractProcessForGroup task, MethodCall originalMethodCall) {
        // knowledge based means dynamic dispatch
        // info specified through proxy API has priority

        Dispatch balancingModeAnnotation = originalMethodCall.getReifiedMethod()
                .getAnnotation(Dispatch.class);
        if (balancingModeAnnotation != null) {
            task.setDynamicallyDispatchable(balancingModeAnnotation.mode().equals(DispatchMode.DYNAMIC));
        }
    }
    //	@Override
    //	public void setDynamicDispatchTag(AbstractProcessForGroup task,
    //			MethodCall mc) {
    //		// defined by a specific annotation
    //		task.setDynamicallyDispatchable(MulticastHelper.dynamicDispatch(mc));
    //	}

}
