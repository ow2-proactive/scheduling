package functionalTests.resourcemanager;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEventType;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;


/*
 * This class tests different preemptive nodes removal that can be done on any RM's Node
 * preemptive removal means removing immediately a node, regardless of its state, 
 * and without waiting an eventually task's end on this job (i.e. without waiting that a RM
 * gives back the node to RM. We check too that RMEvent corresponding 
 * to a removal is correctly generated
 * Here we try a preemptive removal for each possible node's state :
 * 
 * busy (test 1)
 * free (test 2) 
 * toRelease (test 3)
 * down (test 4)
 * and finally for an unknown node (node not handled by RM, test 5)
 * 
 * It tests 'node added' event too, during deployment
 */
public class TestPreemptiveRemoval extends FunctionalTDefaultRM {

    @org.junit.Test
    public void action() throws Exception {

        System.out.println("------------------------------ Deployment");

        System.out.println(monitor.echo());
        System.out.println(user.echo());

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODESOURCE_CREATED,
                RMEventType.NODE_BUSY, RMEventType.NODE_DOWN, RMEventType.NODE_FREE,
                RMEventType.NODE_REMOVED, RMEventType.NODE_TO_RELEASE };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        super.deployDefault();

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        receiver.cleanEventLists();

        //---------------------------------------------------------- 
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and remove preemptively a node
        System.out.println("------------------------------ Test 1");

        NodeSet nodes = user.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == defaultDescriptorNodesNb);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb);

        Node n1 = nodes.get(0);

        //for after, test 2
        Node n2 = nodes.get(1);

        //remove n, which is busy 
        admin.removeNode(n1.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        //try to give back removed node => no effect
        user.freeNode(n1);

        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 0);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        user.freeNodes(nodes);
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        //---------------------------------------------------------- 
        // and remove preemptively a free node
        System.out.println("------------------------------ Test 2");

        admin.removeNode(n2.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 2);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 2);

        //---------------------------------------------------------- 
        // remove preemptively a toRelease node
        System.out.println("------------------------------ Test 3");

        nodes = user.getAtMostNodes(new IntWrapper(2), null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == 2);

        receiver.waitForNEvent(2);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 2);

        Node n3 = nodes.get(0);

        //for after, test 4
        Node n4 = nodes.get(1);

        //place node in toRelease state (by a non preemptive removal)
        admin.removeNode(n3.getNodeInformation().getURL(), false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 2);

        //finally remove preemptively the node
        admin.removeNode(n3.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 3);

        user.freeNodes(nodes);
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);

        //---------------------------------------------------------- 
        // remove preemptively a down node
        System.out.println("------------------------------ Test 4");

        try {
            n4.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        //check that node down event has been thrown
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesdownEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 3);

        admin.removeNode(n4.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 4);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 4);

        //---------------------------------------------------------- 
        // and remove preemptively a node not handled by RM
        System.out.println("------------------------------ Test 5");

        admin.removeNode("rmi://unknown_node", true);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 4);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 4);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 0);

        System.out.println("------------------------------ end of test");
    }
}
