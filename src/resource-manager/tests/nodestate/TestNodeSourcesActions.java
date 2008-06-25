package nodestate;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.FileToBytesConverter;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;


/*
 * This class tests actions of adding and removing node sources, particulary the removal
 * of a node source, preemptively or not
 * 
 * Add a node source (test 1)
 * put nodes of the nodes in different states ; free, busy, down, to Release, 
 * remove the node source preemptively (test 2). 
 * 
 * Add another node source, and put nodes of the nodes in different states ;
 * free, busy, down, to Release,
 * Remove the node source non preemptively (test 3). 
 */
public class TestNodeSourcesActions extends FunctionalTDefaultRM {

    @org.junit.Test
    public void action() throws Exception {

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_BUSY, RMEventType.NODE_DOWN,
                RMEventType.NODE_FREE, RMEventType.NODE_REMOVED, RMEventType.NODE_TO_RELEASE,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODESOURCE_REMOVED };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();

        System.out.println("------------------------------ Test 1");

        String nodeSourceName = "GCM_Node_source_test1";
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
        admin.createGCMNodesource(GCMDeploymentData, nodeSourceName);

        int pingFrequency = 5000;
        admin.setNodeSourcePingFrequency(pingFrequency, nodeSourceName);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        //book 3 nodes
        NodeSet nodes = user.getAtMostNodes(new IntWrapper(3), null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 3);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 3);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);

        receiver.waitForNEvent(3);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 3);

        //put one of the busy node in 'to release' state
        Node n1 = nodes.remove(0);
        admin.removeNode(n1.getNodeInformation().getURL(), false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);

        //put one of the busy node in 'down' state
        Node n2 = nodes.remove(0);

        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesdownEvents().size() == 1);

        //kill preemptively the node source
        admin.removeSource(nodeSourceName, true);

        //wait the n events of the n nodes removals of the node source
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == defaultDescriptorNodesNb);

        //wait for the event of the node source removal 
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);
        assertTrue(user.getTotalNodesNumber().intValue() == 0);

        //test the non preemptive node source removal 

        System.out.println("------------------------------ Test 2");

        String nodeSourceName2 = "GCM_Node_source_test2";
        admin.createGCMNodesource(GCMDeploymentData, nodeSourceName2);

        admin.setNodeSourcePingFrequency(pingFrequency, nodeSourceName2);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        //book 3 nodes
        nodes = user.getAtMostNodes(new IntWrapper(3), null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 3);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 3);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);

        receiver.waitForNEvent(3);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 3);

        //put one of the busy node in 'to release' state
        n1 = nodes.remove(0);
        admin.removeNode(n1.getNodeInformation().getURL(), false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);

        //put one of the busy node in 'down' state
        n2 = nodes.remove(0);

        Node n3 = nodes.remove(0);

        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesdownEvents().size() == 1);

        //kill non preemptively the node source
        admin.removeSource(nodeSourceName2, false);

        //the node isn't removed immediately because one its node is
        //in to Release state, and one in busy state

        //the two free nodes and the down node (n2) are removed immediately
        //the 'to release' node (n1) keeps the same state
        //the busy node (n3) becomes a 'to release' node

        //wait the n events of the n nodes removals of the node source
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        //the two free nodes and the down node are removed immediately
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == defaultDescriptorNodesNb - 2);
        //the 'to release' node keeps the same state
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);

        assertTrue(user.getFreeNodesNumber().intValue() == 0);
        assertTrue(user.getTotalNodesNumber().intValue() == 2);

        //give back the two nodes in 'to release' state, they are directly removed
        user.freeNode(n1);
        user.freeNode(n3);
        receiver.waitForNEvent(2);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 2);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);
        assertTrue(user.getTotalNodesNumber().intValue() == 0);

        //no more nodes handled by the node source, 
        //so the node source can be removed
        //wait for the event of the node source removal 
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);
    }
}
