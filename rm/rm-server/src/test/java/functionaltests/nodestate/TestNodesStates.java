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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
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
public class TestNodesStates extends RMConsecutive {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper helper = RMTHelper.getDefaultInstance();

        ResourceManager resourceManager = helper.getResourceManager();
        int totalNodeNumber = 5;
        helper.createNodeSource("TestNodesStates", totalNodeNumber);
        //----------------------------------------------------------
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and give back to RM
        RMTHelper.log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(totalNodeNumber, null);

        PAFuture.waitFor(nodes);
        Assert.assertEquals(totalNodeNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodes.get(i).getNodeInformation().getURL());
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
            checkEvent(evt, nodes.get(i));
        }

        //for next test
        Node n = nodes.get(0);
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodes.get(i).getNodeInformation().getURL());
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
            checkEvent(evt, nodes.get(i));
        }

        //----------------------------------------------------------
        //give back a node already given back (i.e; node already free)
        //this action causes nothing(nor increasing free nodes number, nor generation of any event)
        RMTHelper.log("Test 2");

        resourceManager.releaseNode(n);

        boolean timeouted = false;
        try {
            helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL(), 4000);
        } catch (ProActiveTimeoutException e) {
            timeouted = true;
        }

        Assert.assertTrue(timeouted);
        Assert.assertEquals(totalNodeNumber, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor
        // Test admin action : Remove a node from the RM (non preemptively),
        // node is busy, so becomes in "toRelease" state
        // user give back to RM the "toRelease" node, node is now removed
        RMTHelper.log("Test 3");

        nodes = resourceManager.getAtMostNodes(totalNodeNumber, null);

        PAFuture.waitFor(nodes);

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        n = nodes.remove(0);

        //put node in "To Release" state
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        //check that node toRelease event has been thrown
        RMNodeEvent evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation()
                .getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        //node is in "ToRelease" state, so always handled by RM
        Assert.assertEquals(totalNodeNumber, resourceManager.getState().getTotalNodesNumber());

        //user give back the node, so node is now removed
        resourceManager.releaseNode(n);

        Assert.assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        evt = helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());

        //----------------------------------------------------------
        // nodes are always in busy state
        // kill JVM of a node (simulate a fallen JVM or broken connection, i.e down node)
        // node must detected down by RM
        RMTHelper.log("Test 4");
        n = nodes.get(0);

        Node n2 = nodes.get(1); //for next test

        try {
            n.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);
        checkEvent(evt, n);

        resourceManager.releaseNodes(nodes);

        // check Nodes freed Event has been thrown
        for (int i = 0; i < totalNodeNumber - 1; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        // the down node became free
        // wait while rm detects again that it's down
        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        //two nodes killed, but the detected down is in RM down nodes list
        //( down nodes are in total nodes count)
        Assert.assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(totalNodeNumber - 2, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // nodes left are in free state
        // kill JVM of a free node
        // node must detected down by RM
        RMTHelper.log("Test 5");
        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        Assert.assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(totalNodeNumber - 3, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // book nodes, put one node in "toRelease" state,
        // then kill its JVM,
        // node must detected down by RM
        RMTHelper.log("Test 6");

        nodes = resourceManager.getAtMostNodes(totalNodeNumber - 3, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < totalNodeNumber - 3; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        n = nodes.get(0);
        n2 = nodes.get(1); //for next test

        //put node in "To Release" state
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        RMTHelper.log("Test 6 Bis");

        //kill the node
        try {
            n.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        Assert.assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        // at this phase we have
        // initial nodes in free state
        // in created ns:
        //  1 node removed
        //  1 to be removed
        //  3 down
        // 

        //user tries to give back a down node, no bad effect
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < totalNodeNumber - 1 /*tbr*/- 1 /*removed*/- 3/*down*/; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        Assert.assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(totalNodeNumber - 4, resourceManager.getState().getFreeNodesNumber());

        //admin removes again the node, ok he already asked this removal when node n was busy
        //choice here is advert admin that node has fallen (not hiding the down node event),
        //rather than automatically remove it
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        evt = helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());

        Assert.assertEquals(totalNodeNumber - 2, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(totalNodeNumber - 4, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // Remove a free node,
        //
        RMTHelper.log("Test 7");

        resourceManager.removeNode(n2.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        evt = helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n2.getNodeInformation().getURL());

        Assert.assertEquals(totalNodeNumber - 3, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(totalNodeNumber - 5, resourceManager.getState().getFreeNodesNumber());
        RMTHelper.log("End of test");

    }

    private void checkEvent(RMNodeEvent event, Node node) {
        Assert.assertEquals(node.getNodeInformation().getURL(), event.getNodeUrl());
        Assert.assertThat(event.getNodeInfo(), CoreMatchers.containsString(event.getNodeUrl()));
        Assert.assertThat(event.getNodeInfo(), CoreMatchers.containsString(event.getNodeProvider()));
        Assert.assertThat(event.getNodeInfo(), CoreMatchers.containsString(event.getHostName()));
    }
}
