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


public class TestGetANode {
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

    @Test
    public void multithreadSimple() throws InterruptedException {
        int nodeCounter = 0;
        final int nodes = 10000;
        for (int i = 0; i < nodes; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Client[] clients = new Client[CLIENTS];

        for (int client = 0; client < clients.length; client++) {
            clients[client] = new Client(vn);
        }

        for (int client = 0; client < clients.length; client++) {
            clients[client].start();
        }

        for (int client = 0; client < clients.length; client++) {
            clients[client].join();
        }

        int sum = 0;
        for (int client = 0; client < clients.length; client++) {
            sum += clients[client].counter;
        }

        Assert.assertEquals(nodes, sum);
    }

    class Client extends Thread {
        GCMVirtualNodeImpl vn;
        public int counter;

        public Client(GCMVirtualNodeImpl vn) {
            this.vn = vn;
            counter = -1;
        }

        public void run() {
            Node node;

            do {
                node = vn.getANode(TIMEOUT);
                counter++;
            } while (node != null);
        }
    }

    static void checkGetANodeIsNull(GCMVirtualNodeImpl vn) {
        long before = System.currentTimeMillis();
        Node rNode = vn.getANode(TIMEOUT);
        long after = System.currentTimeMillis();
        long timeElapsed = after - before;

        Assert.assertFalse(timeoutTooShort(TIMEOUT, timeElapsed));
        Assert.assertFalse(timeoutTooLong(TIMEOUT, timeElapsed));
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
        if (timeElapsed > timeout + 1000)
            return true;

        return false;
    }
}
