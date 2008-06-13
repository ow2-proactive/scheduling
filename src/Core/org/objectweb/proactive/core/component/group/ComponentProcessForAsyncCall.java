/**
 * 
 */
package org.objectweb.proactive.core.component.group;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.group.ProcessForAsyncCall;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * @author The ProActive Team
 *
 */
public class ComponentProcessForAsyncCall extends ProcessForAsyncCall {

    public ComponentProcessForAsyncCall(ProxyForGroup proxyGroup, Vector memberList,
            Vector memberListOfResultGroup, int groupIndex, MethodCall mc, int resultIndex, Body body,
            CountDownLatch doneSignal) {
        super(proxyGroup, memberList, memberListOfResultGroup, groupIndex, mc, resultIndex, body, doneSignal);
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
            this.addToListOfResult(mc.execute(target));
        } else if (object instanceof ProActiveInterface) {
            this.addToListOfResult(mc.execute(object));
        }
    }

}
