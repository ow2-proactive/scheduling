package functionalTests.activeobject.getstubonthis;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;
import functionalTests.GCMDeploymentReady;


/**
 * See PROACTIVE-273
 */
@GCMDeploymentReady
public class TestGetStubOnThis extends FunctionalTest {
    public TestGetStubOnThis() {

    }

    @Test
    public void notFromServiceThread() {
        StubObject stub = PAActiveObject.getStubOnThis();
        Assert.assertNull("Stub Object should be null", stub);
    }

    @Test
    public void fromServiceThread() throws ActiveObjectCreationException, NodeException {
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {});
        Assert.assertTrue(ao.isOk());
    }

}
