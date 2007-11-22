package functionalTests.gcmdeployment.virtualnode;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;
import org.xml.sax.SAXException;


public class TestVirtualNodeSubscribe extends Abstract {
    static GCMApplicationDescriptor gcma;
    boolean isReady = false;
    long nodes = 0;

    @BeforeClass
    static public void setup()
        throws IllegalArgumentException, XPathExpressionException,
            FileNotFoundException, SAXException, IOException {
        gcma = API.getGCMApplicationDescriptor(getDescriptor(
                    TestVirtualNodeAPI.class));

        waitAllocation();
    }

    @Test
    public void test()
        throws IllegalArgumentException, XPathExpressionException,
            FileNotFoundException, SAXException, IOException {
        GCMApplicationDescriptor gcma;

        gcma = API.getGCMApplicationDescriptor(getDescriptor(this));
        VirtualNode vnGreedy = gcma.getVirtualNode("greedy");
        VirtualNode vnMaster = gcma.getVirtualNode("master");

        Assert.assertFalse(vnGreedy.subscribeIsReady(this, "isReady"));

        Assert.assertFalse(vnMaster.subscribeNodeAttachment(this, "LOL"));
        Assert.assertFalse(vnMaster.subscribeNodeAttachment(this,
                "brokenNodeAttached"));
        Assert.assertFalse(vnMaster.subscribeIsReady(this, "brokenIsReady"));

        Assert.assertTrue(vnMaster.subscribeNodeAttachment(this, "nodeAttached"));
        Assert.assertTrue(vnMaster.subscribeIsReady(this, "isReady"));

        // Crash it !
        vnMaster.subscribeIsReady(this, "null");
        vnMaster.subscribeIsReady(null, null);
        vnMaster.unsubscribeIsReady(this, "null");
        vnMaster.unsubscribeIsReady(null, null);

        gcma.startDeployment();
        waitAllocation();

        Assert.assertTrue(isReady);
        Assert.assertTrue(nodes == 2);
    }

    public void isReady(VirtualNode vn) {
        isReady = true;
    }

    public void nodeAttached(Node node, VirtualNode vn) {
        nodes++;
        if (nodes == 2) {
            vn.unsubscribeNodeAttachment(this, "nodeAttached");
        }
    }

    public void brokenIsReady(long l) {
    }

    public void brokenNodeAttached(Object o) {
    }
}
