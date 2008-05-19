package functionalTests.gcmdeployment.virtualnode;

import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestVirtualNodeSubscribeWithHistory extends GCMFunctionalTestDefaultNodes {
    static DeploymentType deployment = DeploymentType._2x2;

    int counter = 0;
    Semaphore sem = new Semaphore(deployment.size);

    public TestVirtualNodeSubscribeWithHistory() {
        super(deployment);
    }

    @Test
    public void test() throws InterruptedException {
        // Block until a node register, so history will be used at least for one node
        super.getANode();

        GCMVirtualNode vn = super.gcmad.getVirtualNode(super.VN_NAME);
        Assert.assertNotNull(vn);
        vn.subscribeNodeAttachment(this, "callback", true);

        sem.acquire();
        // Test passed ! (callback has been invoked deployment.size times)
    }

    public void callback(Node node, String vn) {
        sem.release();
    }
}
