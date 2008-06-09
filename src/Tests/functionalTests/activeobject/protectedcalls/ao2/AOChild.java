package functionalTests.activeobject.protectedcalls.ao2;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.api.PAActiveObject;
import functionalTests.activeobject.protectedcalls.ao.AO;


public class AOChild extends AO {

    public AOChild() {
        // Empty
    }

    public IntWrapper foo2() {
        return ((AOChild) PAActiveObject.getStubOnThis()).foo();
    }
}