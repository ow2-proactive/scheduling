package functionalTests.activeobject.getstubonthis;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestGCMDeployment extends FunctionalTest {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        DeployerAO ao = (DeployerAO) PAActiveObject.newActive(DeployerAO.class.getName(), new Object[] {});
        ao.deploy();
        Assert.assertTrue(ao.waitUntilCallbackOccur());
    }
}
