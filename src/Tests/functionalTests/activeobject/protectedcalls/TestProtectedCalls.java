package functionalTests.activeobject.protectedcalls;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.protectedcalls.ao.AO;
import functionalTests.activeobject.protectedcalls.ao2.AOChild;
import static junit.framework.Assert.assertTrue;


/*
 * See PROACTIVE-229
 */
public class TestProtectedCalls extends FunctionalTest {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        AOChild ao = (AOChild) PAActiveObject.newActive(AOChild.class.getName(), new Object[] {});
        IntWrapper bw = ao.foo2();
        assertTrue("Protected method reached", bw.intValue() == -10);
    }

}