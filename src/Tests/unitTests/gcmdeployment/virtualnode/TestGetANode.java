package unitTests.gcmdeployment.virtualnode;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeImpl;


public class TestGetANode {
    static final int TIMEOUT = 1000;
    static final int CLIENTS = 10;
    VirtualNodeImpl vn;

    @Before
    public void before() {
        vn = new VirtualNodeImpl();
    }

    @Test
    public void withTimeout() {
        int nodeCounter = 0;

        for (int i = 0; i < 4; i++) {
            checkGetANodeIsNull(vn);
        }

        vn.addNode(new NodeMockup(nodeCounter));
        checkGetANodeIsNotNull(vn, new Integer(nodeCounter).toString());
        checkGetANodeIsNull(vn);
        nodeCounter++;

        for (int i = 1; i < 100; i++) {
            vn.addNode(new NodeMockup(nodeCounter++));
        }

        for (int i = 1; i < 100; i++) {
            checkGetANodeIsNotNull(vn, new Integer(i).toString());
        }

        checkGetANodeIsNull(vn);
    }

    @Test
    public void multithreadSimple() throws InterruptedException {
        int nodeCounter = 0;
        final int nodes = 10000;
        for (int i = 0; i < nodes; i++) {
            vn.addNode(new NodeMockup(nodeCounter++));
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
        VirtualNodeImpl vn;
        public int counter;

        public Client(VirtualNodeImpl vn) {
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

    static void checkGetANodeIsNull(VirtualNodeImpl vn) {
        long before = System.currentTimeMillis();
        Node rNode = vn.getANode(TIMEOUT);
        long after = System.currentTimeMillis();
        long timeElapsed = after - before;

        Assert.assertFalse(timeoutTooShort(TIMEOUT, timeElapsed));
        Assert.assertFalse(timeoutTooLong(TIMEOUT, timeElapsed));
        Assert.assertNull(rNode);
    }

    static void checkGetANodeIsNotNull(VirtualNodeImpl vn, String nodeName) {
        Node rNode = vn.getANode(TIMEOUT);

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
