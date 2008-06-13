package org.objectweb.proactive.core.component.group;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.objectweb.proactive.core.component.collectiveitfs.MulticastHelper;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceTypeImpl;
import org.objectweb.proactive.core.group.BasicTaskFactory;
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

}
