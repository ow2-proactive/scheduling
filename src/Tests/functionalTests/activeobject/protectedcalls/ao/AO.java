package functionalTests.activeobject.protectedcalls.ao;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class AO {
    public AO() {
        // Empty
    }

    protected IntWrapper foo() {
        return new IntWrapper(-10);
    }
}
