package functionalTests.gcmdeployment.virtualnode;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


public class TestSubscribeAttachmentFromAO extends FunctionalTest {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        TestSubscribeAttachmentFromAODeployer ao = (TestSubscribeAttachmentFromAODeployer) PAActiveObject
                .newActive(TestSubscribeAttachmentFromAODeployer.class.getName(), new Object[] {});
        ao.deploy();
        Assert.assertTrue(ao.waitUntilCallbackOccur());
    }
}
