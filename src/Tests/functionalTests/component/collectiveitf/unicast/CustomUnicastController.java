package functionalTests.component.collectiveitf.unicast;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.controller.MulticastControllerImpl;
import org.objectweb.proactive.core.mop.MethodCall;


public class CustomUnicastController extends MulticastControllerImpl {

    public CustomUnicastController(Component owner) {
        super(owner);
    }

    int i = 1;

    @Override
    public int allocateServerIndex(MethodCall mc, int partitioningIndex, int nbConnectedServerInterfaces) {
        int index = i;
        if (i == 1) {
            i = 2;
        } else {
            i = 1;
        }
        return index;
    }

}
