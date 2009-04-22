/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package nodestate;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.utils.NodeSet;


/**
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
 * It tests 'node added' event too, during deployment.
 * 
 * @author ProActive team
 */
public class TestPreemptiveRemoval extends FunctionalTDefaultRM {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework. 
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        log("Deployment");

        System.out.println(monitor.isAlive());
        System.out.println(admin.isAlive());

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODESOURCE_CREATED,
                RMEventType.NODE_STATE_CHANGED, RMEventType.NODE_REMOVED };

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
        log("Test 1");

        NodeSet nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb);

        Node n1 = nodes.get(0);

        //for after, test 2
        Node n2 = nodes.get(1);

        //remove n, which is busy 
        admin.removeNode(n1.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        //try to give back removed node => no effect
        admin.freeNode(n1);

        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 0);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        admin.freeNodes(nodes);
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb - 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        //---------------------------------------------------------- 
        // and remove preemptively a free node
        log("Test 2");

        admin.removeNode(n2.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 2);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 2);

        //---------------------------------------------------------- 
        // remove preemptively a toRelease node
        log("Test 3");

        nodes = admin.getAtMostNodes(new IntWrapper(2), null);

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
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 2);

        //finally remove preemptively the node
        admin.removeNode(n3.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 3);

        admin.freeNodes(nodes);
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);

        //---------------------------------------------------------- 
        // remove preemptively a down node
        log("Test 4");

        try {
            n4.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //check that node down event has been thrown
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesdownEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 3);

        admin.removeNode(n4.getNodeInformation().getURL(), true);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 4);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 4);

        //---------------------------------------------------------- 
        // and remove preemptively a node not handled by RM
        log("Test 5");

        admin.removeNode("rmi://unknown_node", true);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb - 4);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 4);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 0);

        log("end of test");
    }
}
