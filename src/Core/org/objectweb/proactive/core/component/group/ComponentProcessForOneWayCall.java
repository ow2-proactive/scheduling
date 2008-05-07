/**
 * 
 */
package org.objectweb.proactive.core.component.group;

import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.ProcessForOneWayCall;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * @author cdalmass
 *
 */
public class ComponentProcessForOneWayCall extends ProcessForOneWayCall {

    public ComponentProcessForOneWayCall(ProxyForGroup proxyGroup, Vector memberList, int index,
            MethodCall mc, Body body, ExceptionListException exceptionList) {
        super(proxyGroup, memberList, index, mc, body, exceptionList);
    }

    @Override
    public void executeMC(MethodCall mc, Object object) throws Throwable {
        if (object instanceof ProActiveComponentRepresentative) {
            // delegate to the corresponding interface
            Object target;
            if (mc.getComponentMetadata().getComponentInterfaceName() == null) {
                // a call on the Component interface
                target = object;
            } else {
                target = ((ProActiveComponentRepresentative) object).getFcInterface(mc.getComponentMetadata()
                        .getComponentInterfaceName());
            }
            mc.execute(target);
        } else if (object instanceof ProActiveInterface) {
            mc.execute(object);
        } else
            throw new RuntimeException("Should not be here for component");
    }
}
