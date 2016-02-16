/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodestate;

import functionaltests.RMFunctionalTestWithTestNode;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestNode;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;

import java.net.URI;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


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
public class TestAdminAddingNodes extends RMFunctionalTestWithTestNode {

    @Test
    public void testAddNodes() throws Exception {

        final String NS_NAME = "TestAdminAddingNodes";

        int pingFrequency = 8000;
        ResourceManager resourceManager = rmHelper.getResourceManager();
        resourceManager.createNodeSource(NS_NAME, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), null);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NS_NAME);

        resourceManager.setNodeSourcePingFrequency(pingFrequency, NS_NAME);

        log("Test 1 : add a node");
        String node1Name = "node1";
        TestNode testNode1 = RMTHelper.createNode(node1Name);
        testNodes.add(testNode1);
        String node1URL = testNode1.getNodeURL();

        resourceManager.addNode(node1URL, NS_NAME);

        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getTotalAliveNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 2 : remove an already deployed node");

        //preemptive removal is useless for this case, because node is free
        resourceManager.removeNode(node1URL, false);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        assertEquals(0, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getTotalAliveNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        log("Test 3 : add a node, kill this node, node is detected down, and add a node that has the same URL");
        String node2Name = "node2";
        TestNode testNode2 = RMTHelper.createNode(node2Name);
        testNodes.add(testNode2);
        String node2URL = testNode2.getNodeURL();

        resourceManager.addNode(node2URL, NS_NAME);

        //wait the node added event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());
        assertEquals(1, resourceManager.getState().getTotalAliveNodesNumber());

        testNode2.kill();
        testNodes.remove(testNode2);

        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);

        assertEquals(evt.getNodeState(), NodeState.DOWN);
        //wait the node down event
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        assertEquals(0, resourceManager.getState().getTotalAliveNodesNumber());

        //create another node with the same URL, and add it to Resource manager
        testNode2 = RMTHelper.createNode(node2Name, new URI(node2URL).getPort());
        testNodes.add(testNode2);
        node2URL = testNode2.getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        //wait the node added event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());
        assertEquals(1, resourceManager.getState().getTotalAliveNodesNumber());

        log("Test 4 : add a node, keep this node free, kill this node, and add a node that has the same URL");

        //put a large ping frequency in order to avoid down nodes detection
        resourceManager.setNodeSourcePingFrequency(Integer.MAX_VALUE, NS_NAME);

        //wait the end of last ping sequence
        Thread.sleep(12000);

        //node2 is free, kill the node
        testNode2.kill();
        testNodes.remove(testNode2);

        //create another node with the same URL, and add it to Resource manager
        testNode2 = RMTHelper.createNode(node2Name, new URI(node2URL).getPort());
        testNodes.add(testNode2);
        node2URL = testNode2.getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        NodeFactory.getNode(node2URL);

        //wait the node added event, node added is configuring
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 5 : add a node, put this node busy, kill this node, and add a node that has the same URL");

        //put the the node to busy state
        NodeSet nodes = resourceManager.getAtMostNodes(1, null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(evt.getNodeState(), NodeState.BUSY);

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        //node2 is busy, kill the node
        testNode2.kill();
        testNodes.remove(testNode2);

        //create another node with the same URL, and add it to Resource manager
        testNode2 = RMTHelper.createNode(node2Name, new URI(node2URL).getPort());
        testNodes.add(testNode2);
        node2URL = testNode2.getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        NodeFactory.getNode(node2URL);

        //wait the node added event, node added is configuring
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 6 : add a node, put this node toRelease, kill this node, and add a node that has the same URL");

        //put the the node to busy state
        nodes = resourceManager.getAtMostNodes(1, null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(evt.getNodeState(), NodeState.BUSY);

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        //put the node in to Release state
        resourceManager.removeNode(node2URL, false);

        //wait the node to release event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        testNode2.kill();
        testNodes.remove(testNode2);

        //create another node with the same URL, and add it to Resource manager
        testNode2 = RMTHelper.createNode(node2Name, new URI(node2URL).getPort());
        testNodes.add(testNode2);
        node2URL = testNode2.getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        NodeFactory.getNode(node2URL);

        //wait the node added event, node added is configuring
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(evt.getNodeState(), NodeState.FREE);
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 7");

        //add the same node twice and check that RM will not kill the node. If it does
        //second attempt will fail
        BooleanWrapper result = resourceManager.addNode(node2URL, NS_NAME);
        assertFalse(result.getBooleanValue());

        try {
            rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL, 8000);
            fail("Should timeout");
        } catch (ProActiveTimeoutException expected) {
            // expected
        }

    }
}
