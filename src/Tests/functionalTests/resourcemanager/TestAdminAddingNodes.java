package functionalTests.resourcemanager;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEventType;
import org.objectweb.proactive.extensions.resourcemanager.core.properties.PAResourceManagerProperties;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;


/*
 * This class tests different cases of adding an already deployed 
 * (i.e. not deployed by Resource Manager) node to the resource Manager
 * and removal of these already deployed nodes
 * 
 * simply add a node (test 1)
 * simply remove an already deployed node (test 2)
 * add a node, kill this node, node is detected down, and add a node that has the same URL (test 3).
 * 
 * For the next tests, we put a big ping frequency in order to avoid detection of failed nodes,
 * in order to test the replacement of a node by another with the same URL.
 * 
 * add a node, keep this node free, kill this node, and add a node that has the same URL (test 4).
 * add a node, put this node busy, kill this node, and add a node that has the same URL (test 5).
 * add a node, put this node toRelease, kill this node, and add a node that has the same URL (test 6).
 */
public class TestAdminAddingNodes extends FunctionalTDefaultRM {

    @org.junit.Test
    public void action() throws Exception {

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_BUSY, RMEventType.NODE_DOWN,
                RMEventType.NODE_FREE, RMEventType.NODE_REMOVED, RMEventType.NODE_TO_RELEASE };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        String hostName = ProActiveInet.getInstance().getHostname();

        int pingFrequency = 5000;
        admin.setDefaultNodeSourcePingFrequency(pingFrequency);

        System.out.println("------------------------------ Test 1");

        String node1URL = "rmi://" + hostName + "/node1";
        createNode(node1URL);

        admin.addNode(node1URL);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 1);

        System.out.println("------------------------------ Test 2");

        //preemptive removal is useless for this case, because node is free 
        admin.removeNode(node1URL, false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 0);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        System.out.println("------------------------------ Test 3");

        String node2URL = "rmi://" + hostName + "/node2";
        createNode(node2URL);

        admin.addNode(node2URL);

        //wait the node added event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 1);

        //kill the node
        Node node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
        }

        //wait the node down event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesdownEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        //create another node with the same URL, and add it to Resource manager
        createNode(node2URL);
        admin.addNode(node2URL);

        //wait for removal of the previous down node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 1);

        System.out.println("------------------------------ Test 4");

        //put a large ping frequency in order to avoid down nodes detection
        admin.setDefaultNodeSourcePingFrequency(10000);

        //wait the end of last ping sequence 
        Thread.sleep(PAResourceManagerProperties.RM_NODE_SOURCE_PING_FREQUENCY.getValueAsInt() + 500);

        //node2 is free, kill the node 
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        createNode(node2URL);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event, node added is free
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 1);

        System.out.println("------------------------------ Test 5");

        //put the the node to busy state
        NodeSet nodes = user.getAtMostNodes(new IntWrapper(1), null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        //node2 is busy, kill the node 
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        createNode(node2URL);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event, node added is free
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 1);

        System.out.println("------------------------------ Test 6");

        //put the the node to busy state
        nodes = user.getAtMostNodes(new IntWrapper(1), null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        //put the node in to Release state
        admin.removeNode(node2URL, false);

        //wait the node to release event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        //node2 is to release, kill the node 
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        createNode(node2URL);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event, node added is free
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 1);

    }
}
