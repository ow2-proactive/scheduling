package functionalTests.activeobject.onfailure;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedException;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestExceptionInInitActivity extends FunctionalTest {
    @Test(expected = BodyTerminatedException.class)
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        ExceptionInInitActivityAO ao = (ExceptionInInitActivityAO) PAActiveObject.newActive(
                ExceptionInInitActivityAO.class.getName(), new Object[] {});
        // Should not be executed (or at least a Runtime Exception must been thrown by ao.getTrue())
        ao.getTrue();
    }
}
