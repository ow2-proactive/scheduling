package unitTests.gcmdeployment.virtualnode;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;


public class TestGetANodeWithTimeout {
    static final int TIMEOUT = 1000;
    static final int CLIENTS = 10;
    GCMVirtualNodeImpl vn;
    GCMApplicationDescriptorMockup gcma;
    ProActiveRuntime part;

    @BeforeClass
    static public void setCapacity() {
        ProActiveRuntimeImpl.getProActiveRuntime().setCapacity(12000);
    }

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
        gcma = new GCMApplicationDescriptorMockup();
        part = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Test
    public void withTimeout() {
        int nodeCounter = 0;

        for (int i = 0; i < 4; i++) {
            checkGetANodeIsNull(vn);
        }

        vn.addNode(new FakeNode(gcma, part));
        checkGetANodeIsNotNull(vn, nodeCounter);
        checkGetANodeIsNull(vn);
        nodeCounter++;

        for (int i = 1; i < 100; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        for (int i = 1; i < 100; i++) {
            checkGetANodeIsNotNull(vn, i);
        }

        checkGetANodeIsNull(vn);
    }

    static void checkGetANodeIsNull(GCMVirtualNodeImpl vn) {
        long before = System.currentTimeMillis();
        Node rNode = vn.getANode(TIMEOUT);
        long after = System.currentTimeMillis();
        long timeElapsed = after - before;

        Assert.assertFalse("Timeout too short", timeoutTooShort(TIMEOUT, timeElapsed));
        Assert.assertFalse("Timeout too long", timeoutTooLong(TIMEOUT, timeElapsed));
        Assert.assertNull(rNode);
    }

    void checkGetANodeIsNotNull(GCMVirtualNodeImpl vn, int i) {
        Node rNode = vn.getANode(TIMEOUT);
        String nodeName = part.getVMInformation().getName() + "_" + Constants.GCM_NODE_NAME + i;

        Assert.assertNotNull(rNode);
        Assert.assertEquals(nodeName, rNode.getNodeInformation().getName());
    }

    static boolean timeoutTooShort(long timeout, long timeElapsed) {
        if (timeElapsed < timeout)
            return true;

        return false;
    }

    static boolean timeoutTooLong(long timeout, long timeElapsed) {
        if (timeElapsed > 2 * timeout)
            return true;

        return false;
    }
}
