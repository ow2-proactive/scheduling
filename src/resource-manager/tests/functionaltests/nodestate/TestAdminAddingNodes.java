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
package functionaltests.nodestate;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


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
public class TestAdminAddingNodes extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        String hostName = ProActiveInet.getInstance().getHostname();

        int pingFrequency = 5000;
        RMAdmin admin = RMTHelper.getAdminInterface();

        admin.setDefaultNodeSourcePingFrequency(pingFrequency);

        RMTHelper.log("Test 1");
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        RMTHelper.createNode(node1Name);

        admin.addNode(node1URL);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        RMTHelper.log("Test 2");

        //preemptive removal is useless for this case, because node is free
        admin.removeNode(node1URL, false);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        assertTrue(admin.getTotalNodesNumber().intValue() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        RMTHelper.log("Test 3");
        String node2Name = "node2";
        String node2URL = "//" + hostName + "/" + node2Name;
        RMTHelper.createNode(node2Name);

        admin.addNode(node2URL);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait the node added event
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        //kill the node
        Node node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);

        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);
        //wait the node down event
        Assert.assertEquals(admin.getTotalNodesNumber().intValue(), 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //create another node with the same URL, and add it to Resource manager
        RMTHelper.createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous down node with the same URL
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node2URL);

        //wait the node added event
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);

        Assert.assertEquals(admin.getTotalNodesNumber().intValue(), 1);
        Assert.assertEquals(admin.getFreeNodesNumber().intValue(), 1);

        RMTHelper.log("Test 4");

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

        RMTHelper.createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node2URL);

        //wait the node added event, node added is free
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        Assert.assertEquals(admin.getTotalNodesNumber().intValue(), 1);
        Assert.assertEquals(admin.getFreeNodesNumber().intValue(), 1);

        RMTHelper.log("Test 5");

        //put the the node to busy state
        NodeSet nodes = admin.getAtMostNodes(1, null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        Assert.assertEquals(admin.getTotalNodesNumber().intValue(), 1);
        Assert.assertEquals(admin.getFreeNodesNumber().intValue(), 0);

        //node2 is busy, kill the node
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        RMTHelper.createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous free node with the same URL
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node2URL);

        //wait the node added event, node added is free
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        RMTHelper.log("Test 6");

        //put the the node to busy state
        nodes = admin.getAtMostNodes(1, null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //put the node in to Release state
        admin.removeNode(node2URL, false);

        //wait the node to release event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_RELEASED);

        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        //node2 is to release, kill the node
        node2 = NodeFactory.getNode(node2URL);
        try {
            node2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }

        //create another node with the same URL, and add it to Resource manager
        RMTHelper.createNode(node2Name);
        admin.addNode(node2URL);

        //wait for removal of the previous down node with the same URL
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node2URL);

        //wait the node added event, node added is free
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        RMTHelper.log("Test 7");

        //add the same node twice and check that RM will not kill the node. If it does
        //second attempt will fail
        admin.addNode(node2URL);
        BooleanWrapper result = admin.addNode(node2URL);
        if (!result.booleanValue()) {
            assertTrue("Cannot add the same node twice", false);
        }

        boolean timeouted = false;
        try {
            RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL, 5000);
        } catch (ProActiveTimeoutException e) {
            // TODO Auto-generated catch block
            timeouted = true;
        }

        assertTrue(timeouted);
    }
}
