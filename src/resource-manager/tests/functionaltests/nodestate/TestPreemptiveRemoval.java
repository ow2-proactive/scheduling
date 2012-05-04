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

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.utils.NodeSet;

import org.ow2.tests.FunctionalTest;
import functionaltests.RMTHelper;


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
public class TestPreemptiveRemoval extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper.log("Deployment");

        RMTHelper helper = RMTHelper.getDefaultInstance();
        ResourceManager resourceManager = helper.getResourceManager();

        helper.createLocalNodeSource();
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.LOCAL_INFRASTRUCTURE_NAME);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            //for deploying nodes
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            //wait for nodes to be in free state
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and remove preemptively a node
        RMTHelper.log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Node n1 = nodes.get(0);

        //for after, test 2
        Node n2 = nodes.get(1);

        //remove n, which is busy
        resourceManager.removeNode(n1.getNodeInformation().getURL(), true);

        RMNodeEvent evt = helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n1.getNodeInformation().getURL());

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);

        //try to give back removed node => no effect
        try {
            resourceManager.releaseNode(n1).getBooleanValue();
            assertTrue("Released node which had been removed", false);
        } catch (RuntimeException e) {
        }

        boolean timeouted = false;
        try {
            helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n1.getNodeInformation().getURL(), 4000);
        } catch (ProActiveTimeoutException e) {
            // TODO Auto-generated catch block
            timeouted = true;
        }

        assertTrue(timeouted);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);

        nodes.remove(n1);
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);

        //----------------------------------------------------------
        // and remove preemptively a free node
        RMTHelper.log("Test 2");

        resourceManager.removeNode(n2.getNodeInformation().getURL(), true);

        evt = helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n2.getNodeInformation().getURL());

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 2);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 2);

        //----------------------------------------------------------
        // remove preemptively a toRelease node
        RMTHelper.log("Test 3");

        nodes = resourceManager.getAtMostNodes(2, null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == 2);

        for (int i = 0; i < 2; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Node n3 = nodes.get(0);

        //for after, test 4
        Node n4 = nodes.get(1);

        //place node in toRelease state (by a non preemptive removal)
        resourceManager.removeNode(n3.getNodeInformation().getURL(), false);

        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n3.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 2);

        //finally remove preemptively the node
        resourceManager.removeNode(n3.getNodeInformation().getURL(), true);

        helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n3.getNodeInformation().getURL());

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 3);

        nodes.remove(n3);
        resourceManager.releaseNodes(nodes);

        evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        //----------------------------------------------------------
        // remove preemptively a down node
        RMTHelper.log("Test 4");

        try {
            n4.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n4.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        //check that node down event has been thrown
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 3);

        resourceManager.removeNode(n4.getNodeInformation().getURL(), true);

        helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n4.getNodeInformation().getURL());

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 4);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 4);

        //----------------------------------------------------------
        // and remove preemptively a node not handled by RM
        RMTHelper.log("Test 5");

        resourceManager.removeNode("rmi://unknown_node", true);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 4);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 4);

        timeouted = false;
        try {
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, 4000);
        } catch (ProActiveTimeoutException e) {
            // TODO Auto-generated catch block
            timeouted = true;
        }

        assertTrue(timeouted);

        RMTHelper.log("end of test");
    }
}
