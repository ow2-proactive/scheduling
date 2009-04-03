/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.utils.NodeSet;


/**
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
 *
 * @author ProActive team
 * 
 */
public class TestAdminAddingNodes extends FunctionalTDefaultRM {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework. 
     * @throws Exception If the test fails.
     */
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

        log("Test 1");
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        createNode(node1Name);

        admin.addNode(node1URL);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 2");

        //preemptive removal is useless for this case, because node is free 
        admin.removeNode(node1URL, false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        log("Test 3");
        String node2Name = "node2";
        String node2URL = "//" + hostName + "/" + node2Name;
        createNode(node2Name);

        admin.addNode(node2URL);

        //wait the node added event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        //kill the node
        Node node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        //wait the node down event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesdownEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //create another node with the same URL, and add it to Resource manager
        createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous down node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 4");

        //put a large ping frequency in order to avoid down nodes detection
        admin.setDefaultNodeSourcePingFrequency(100000);

        //wait the end of last ping sequence 
        Thread.sleep(PAResourceManagerProperties.RM_NODE_SOURCE_PING_FREQUENCY.getValueAsInt() + 500);

        //node2 is free, kill the node 
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager

        createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event, node added is free
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 5");

        //put the the node to busy state
        NodeSet nodes = admin.getAtMostNodes(new IntWrapper(1), null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //node2 is busy, kill the node 
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event, node added is free
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 6");

        //put the the node to busy state
        nodes = admin.getAtMostNodes(new IntWrapper(1), null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //put the node in to Release state
        admin.removeNode(node2URL, false);

        //wait the node to release event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesToReleaseEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //node2 is to release, kill the node 
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);

        //wait the node added event, node added is free
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

    }
}
