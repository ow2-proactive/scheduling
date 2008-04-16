package functionalTests.gcmdeployment.virtualnode;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestVirtualNodeSubscribeWithHistory extends GCMFunctionalTestDefaultNodes {
    int counter = 0;

    public TestVirtualNodeSubscribeWithHistory() {
        super(DeploymentType._2x2);
    }

    @Test
    public void test() throws InterruptedException {
        // Block until a node register, so history will be used at least for one node
        super.getANode();

        GCMVirtualNode vn = super.gcmad.getVirtualNode(super.VN_NAME);
        Assert.assertNotNull(vn);
        vn.subscribeNodeAttachment(this, "callback", true);

        // ensure that all nodes registered
        super.getANode();
        super.getANode();
        super.getANode();

        // Wait for the notification
        Thread.sleep(500);

        Assert.assertEquals(4, counter);
    }

    public void callback(Node node, String vn) {
        counter++; // atomic on it
    }
}
