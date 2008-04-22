package functionalTests.resourcemanager;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;


/*
 * This class tests different nodes states changes and their related Events launched by RMMonitoring 
 * It tests Nodes removal mechanism (non preemptively method) too.
 * Nodes states changes can be :
 * 
 * free -> busy, and busy -> free  (test 1)
 * give back to RM a node already free (test 2)
 * busy -> toRelease , and toRelease -> removed (test 3)
 * busy -> down (test 4)
 * free -> down (test 5)
 * toRelease -> down, and down -> removed(test 6)
 * free -> removed (test 7)
 * 
 * It tests 'node added' event too, during deployment
 */
public class TestNodesStates extends FunctionalTDefaultRM {

    @org.junit.Test
    public void action() throws Exception {

        System.out.println("------------------------------ Deployment");

        System.out.println(monitor.echo());
        System.out.println(user.echo());

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor });

        receiver.cleanEventLists();
        super.deployDefault();

        receiver.waitForNEvent(defaultDescriptorNodesNb);
        receiver.cleanEventLists();

        //---------------------------------------------------------- 
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and give back to RM
        System.out.println("------------------------------ Test 1");

        NodeSet nodes = user.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == defaultDescriptorNodesNb);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb);

        //for next test
        Node n = nodes.get(0);

        user.freeNodes(nodes);
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb);

        //----------------------------------------------------------
        //give back a node already given back (i.e; node already free)
        //this action causes nothing(nor increasing free nodes number, nor generation of any event)
        System.out.println("------------------------------ Test 2");

        user.freeNode(n);

        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 0);

        //---------------------------------------------------------- 
        // Book all nodes deployed by descriptor
        // Test admin action : Remove a node from the RM (non preemptively), 
        // node is busy, so becomes in "toRelease" state 
        // user give back to RM the "toRelease" node, node is now removed
        System.out.println("------------------------------ Test 3");

        nodes = user.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), null);
        PAFuture.waitFor(nodes);

        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb);

        n = nodes.remove(0);

        //put node in "To Release" state
        admin.removeNode(n.getNodeInformation().getURL(), false);

        //check that node toRelease event has been thrown
        receiver.waitForNEvent(1);
        ArrayList<String> list = receiver.cleanNgetNodesToReleaseEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n.getNodeInformation().getURL()));

        //node is in "ToRelease" state, so always handled by RM
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);

        //user give back the node, so node is now removed
        user.freeNode(n);

        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        receiver.waitForNEvent(1);
        list = receiver.cleanNgetNodesremovedEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n.getNodeInformation().getURL()));

        //----------------------------------------------------------
        // nodes are always in busy state 
        // kill JVM of a node (simulate a fallen JVM or broken connection, i.e down node)
        // node must detected down by RM
        System.out.println("------------------------------ Test 4");
        n = nodes.get(0);

        Node n2 = nodes.get(1); //for next test

        try {
            n.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiver.waitForNEvent(1);

        //check that node down event has been thrown
        list = receiver.cleanNgetNodesdownEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n.getNodeInformation().getURL()));

        user.freeNodes(nodes);

        // check Nodes freed Event has been thrown
        receiver.waitForNEvent(defaultDescriptorNodesNb - 2);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb - 2);

        //two nodes killed, but the detected down is in RM down nodes list
        //( down nodes are in total nodes count)
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 2);

        //----------------------------------------------------------
        // nodes left are in free state 
        // kill JVM of a free node
        // node must detected down by RM
        System.out.println("------------------------------ Test 5");
        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiver.waitForNEvent(1);
        //check that node down event has been thrown
        list = receiver.cleanNgetNodesdownEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n2.getNodeInformation().getURL()));

        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 3);

        //----------------------------------------------------------
        // book nodes, put one node in "toRelease" state,
        // then kill its JVM,
        // node must detected down by RM
        System.out.println("------------------------------ Test 6");

        nodes = user.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb - 3), null);
        PAFuture.waitFor(nodes);

        receiver.waitForNEvent(defaultDescriptorNodesNb - 3);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb - 3);

        n = nodes.get(0);
        n2 = nodes.get(1); //for next test

        //put node in "To Release" state
        admin.removeNode(n.getNodeInformation().getURL(), false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);

        System.out.println("------------------------------ Test 6 Bis");

        //kill the node
        try {
            n.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        receiver.waitForNEvent(1);
        // check that down node event has been received
        list = receiver.cleanNgetNodesdownEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n.getNodeInformation().getURL()));

        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getFreeNodesNumber().intValue() == 0);

        //user tries to give back a down node, no bad effect
        user.freeNodes(nodes);

        receiver.waitForNEvent(defaultDescriptorNodesNb - 4);
        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 4);

        //admin removes again the node, ok he already asked this removal when node n was busy
        //choice here is advert admin that node has fallen (not hiding the down node event),
        //rather than automatically remove it
        admin.removeNode(n.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        receiver.waitForNEvent(1);
        list = receiver.cleanNgetNodesremovedEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n.getNodeInformation().getURL()));

        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 2);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 4);

        //----------------------------------------------------------
        // Remove a free node,
        //
        System.out.println("------------------------------ Test 7");

        admin.removeNode(n2.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        receiver.waitForNEvent(1);
        list = receiver.cleanNgetNodesremovedEvents();
        assertTrue(list.size() == 1);
        assertTrue(list.contains(n2.getNodeInformation().getURL()));

        assertTrue(user.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 3);
        assertTrue(user.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 5);

        System.out.println("------------------------------ End of test");
    }

}
