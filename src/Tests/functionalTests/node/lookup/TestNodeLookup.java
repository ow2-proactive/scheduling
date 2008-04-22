package functionalTests.node.lookup;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestNodeLookup extends FunctionalTest {

    @Test(expected = NodeException.class)
    public void testRemoteException() throws NodeException {
        NodeFactory.getNode("rmi://chuck.norris:1099/gun");
    }

    @Test(expected = NodeException.class)
    public void testNotBoundException() throws NodeException {
        NodeFactory.getNode("rmi://localhost:1099/gun");
    }

    @Test
    public void test() throws NodeException {
        Node node = NodeFactory.getDefaultNode();
        String url = node.getNodeInformation().getURL();

        NodeFactory.getNode(url);
    }
}
