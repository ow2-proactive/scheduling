/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.net.URI;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Assert;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;
import functionaltests.TNode;


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
public class TestAdminAddingNodes extends RMConsecutive {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        String hostName = ProActiveInet.getInstance().getHostname();
        final String NS_NAME = "TestAdminAddingNodes";

        int pingFrequency = 5000;
        ResourceManager resourceManager = helper.getResourceManager();
        resourceManager.createNodeSource(NS_NAME, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), null);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NS_NAME);

        resourceManager.setNodeSourcePingFrequency(pingFrequency, NS_NAME);

        RMTHelper.log("Test 1");
        String node1Name = "node1";
        String node1URL = helper.createNode(node1Name).getNodeURL();

        resourceManager.addNode(node1URL, NS_NAME);

        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the node to be in free state
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getTotalAliveNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 2");

        //preemptive removal is useless for this case, because node is free
        resourceManager.removeNode(node1URL, false);

        helper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        Assert.assertEquals(0, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getTotalAliveNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 3");
        String node2Name = "node2";
        String node2URL = helper.createNode(node2Name).getNodeURL();

        resourceManager.addNode(node2URL, NS_NAME);

        //wait the node added event
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getTotalAliveNodesNumber());

        //kill the node
        helper.killNode(node2URL);

        RMNodeEvent evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);

        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);
        //wait the node down event
        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getTotalAliveNodesNumber());

        //create another node with the same URL, and add it to Resource manager
        TNode node = helper.createNode(node2Name);
        node2URL = node.getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        //wait the node added event
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(2, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getTotalAliveNodesNumber());

        RMTHelper.log("Test 4");

        //put a large ping frequency in order to avoid down nodes detection
        resourceManager.setNodeSourcePingFrequency(Integer.MAX_VALUE, NS_NAME);

        //wait the end of last ping sequence
        Thread.sleep(10000);

        //node2 is free, kill the node
        helper.killNode(node2URL);

        //create another node with the same URL, and add it to Resource manager
        node2URL = helper.createNode(node2Name, new URI(node2URL).getPort()).getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        NodeFactory.getNode(node2URL);

        //wait the node added event, node added is configuring
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(2, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 5");

        //put the the node to busy state
        NodeSet nodes = resourceManager.getAtMostNodes(1, null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        Assert.assertEquals(2, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        //node2 is busy, kill the node
        helper.killNode(node2URL);

        //create another node with the same URL, and add it to Resource manager
        node2URL = helper.createNode(node2Name).getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        try {
            NodeFactory.getNode(node2URL);
        } catch (Exception e) {
            Assert.assertEquals("Runtime of the new node was killed", false);
        }

        //wait the node added event, node added is configuring
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(3, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 6");

        //put the the node to busy state
        nodes = resourceManager.getAtMostNodes(1, null);
        PAFuture.waitFor(nodes);

        //wait the node busy event
        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        Assert.assertEquals(3, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        //put the node in to Release state
        resourceManager.removeNode(node2URL, false);

        //wait the node to release event
        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        Assert.assertEquals(3, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        //node2 is to release, kill the node
        helper.killNode(node2URL);

        //create another node with the same URL, and add it to Resource manager
        node2URL = helper.createNode(node2Name).getNodeURL();
        resourceManager.addNode(node2URL, NS_NAME);

        try {
            NodeFactory.getNode(node2URL);
        } catch (Exception e) {
            Assert.assertEquals("Runtime of the new node was killed", false);
        }

        //wait the node added event, node added is configuring
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        Assert.assertEquals(4, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 7");

        //add the same node twice and check that RM will not kill the node. If it does
        //second attempt will fail
        BooleanWrapper result = resourceManager.addNode(node2URL, NS_NAME);
        if (result.getBooleanValue()) {
            Assert.assertEquals("Successfully added the same node twice - incorrect", false);
        }

        boolean timeouted = false;
        try {
            helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL, 5000);
        } catch (ProActiveTimeoutException e) {
            timeouted = true;
        }

        Assert.assertTrue(timeouted);
    }
}
