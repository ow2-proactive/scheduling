package functionalTests.activeobject.onfailure;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


public class TestExceptionInInitActivity {
    @Test(expected = ActiveObjectCreationException.class)
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        System.out.println("1");
        TestExceptionInInitActivityAO ao = (TestExceptionInInitActivityAO) PAActiveObject.newActive(
                TestExceptionInInitActivityAO.class.getName(), new Object[] {});

        // Should not be executed (or at least a Runtime Exception must been thrown by ao.getTrue())
        ao.getTrue();
    }
}
