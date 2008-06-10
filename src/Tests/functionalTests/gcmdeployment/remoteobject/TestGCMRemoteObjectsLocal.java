package functionalTests.gcmdeployment.remoteobject;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGCMRemoteObjectsLocal extends GCMFunctionalTestDefaultNodes {
    public TestGCMRemoteObjectsLocal() {
        super(1, 1);
    }

    @Test
    public void testLocal() {
        GCMVirtualNode vn1 = super.gcmad.getVirtualNode(GCMFunctionalTestDefaultNodes.VN_NAME);
        Assert.assertNotNull(vn1);

        boolean atLeastOne = false;
        for (GCMVirtualNode vn : super.gcmad.getVirtualNodes().values()) {
            atLeastOne = true;
            for (Node node : vn.getCurrentNodes()) {
                System.out.println(node.getNodeInformation().getURL());
            }
            Assert.assertTrue(atLeastOne);
        }
    }
}
