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

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;

import static functionaltests.utils.RMTHelper.log;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;


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
public class TestNodesStates extends RMFunctionalTest {

    @Test
    public void action() throws Exception {

        ResourceManager resourceManager = rmHelper.getResourceManager();
        int totalNodeNumber = 5;
        rmHelper.createNodeSource("TestNodesStates", totalNodeNumber);
        //----------------------------------------------------------
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and give back to RM
        log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(totalNodeNumber, null);

        PAFuture.waitFor(nodes);
        assertEquals(totalNodeNumber, nodes.size());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
              nodes.get(i).getNodeInformation().getURL());
            assertEquals(NodeState.BUSY, evt.getNodeState());
            checkEvent(evt, nodes.get(i));
        }

        //for next test
        Node n = nodes.get(0);
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
              nodes.get(i).getNodeInformation().getURL());
            assertEquals(NodeState.FREE, evt.getNodeState());
            checkEvent(evt, nodes.get(i));
        }

        //----------------------------------------------------------
        //give back a node already given back (i.e; node already free)
        //this action causes nothing(nor increasing free nodes number, nor generation of any event)
        log("Test 2");

        resourceManager.releaseNode(n);

        boolean timeouted = false;
        try {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL(), 4000);
        } catch (ProActiveTimeoutException e) {
            timeouted = true;
        }

        assertTrue(timeouted);
        assertEquals(totalNodeNumber, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor
        // Test admin action : Remove a node from the RM (non preemptively),
        // node is busy, so becomes in "toRelease" state
        // user give back to RM the "toRelease" node, node is now removed
        log("Test 3");

        nodes = resourceManager.getAtMostNodes(totalNodeNumber, null);

        PAFuture.waitFor(nodes);

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.BUSY, evt.getNodeState());
        }

        n = nodes.remove(0);

        //put node in "To Release" state
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        //check that node toRelease event has been thrown
        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL
          ());
        assertEquals(NodeState.TO_BE_REMOVED, evt.getNodeState());

        //node is in "ToRelease" state, so always handled by RM
        assertEquals(totalNodeNumber, resourceManager.getState().getTotalNodesNumber());

        //user give back the node, so node is now removed
        resourceManager.releaseNode(n);

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());

        //----------------------------------------------------------
        // nodes are always in busy state
        // kill JVM of a node (simulate a fallen JVM or broken connection, i.e down node)
        // node must detected down by RM
        log("Test 4");
        n = nodes.get(0);

        Node n2 = nodes.get(1); //for next test

        try {
            n.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        assertEquals(NodeState.DOWN, evt.getNodeState());
        checkEvent(evt, n);

        resourceManager.releaseNodes(nodes);

        // we should get 4 FREE events

        for (int i = 0; i < totalNodeNumber - 2; i++) {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, evt.getNodeState());
        }

        //two nodes killed, but the detected down is in RM down nodes list
        //( down nodes are in total nodes count)
        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 2, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // nodes left are in free state
        // kill JVM of a free node
        // node must detected down by RM
        log("Test 5");
        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        assertEquals(NodeState.DOWN, evt.getNodeState());

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 3, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // book nodes, put one node in "toRelease" state,
        // then kill its JVM,
        // node must detected down by RM
        log("Test 6");

        nodes = resourceManager.getAtMostNodes(totalNodeNumber - 3, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < totalNodeNumber - 3; i++) {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.BUSY, evt.getNodeState());
        }

        n = nodes.get(0);
        n2 = nodes.get(1); //for next test

        //put node in "To Release" state
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        assertEquals(NodeState.TO_BE_REMOVED, evt.getNodeState());

        log("Test 6 Bis");

        //kill the node
        try {
            n.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        assertEquals(NodeState.DOWN, evt.getNodeState());

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (Node node : nodes) {
            log("Taken node: " + node.getNodeInformation().getURL());
        }

        // we have 2 nodes: 11 busy and one still down
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < 1; i++) {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, evt.getNodeState());
        }

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 4, resourceManager.getState().getFreeNodesNumber());

        //admin removes again the node, ok he already asked this removal when node n was busy
        //choice here is advert admin that node has fallen (not hiding the down node event),
        //rather than automatically remove it
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());

        assertEquals(totalNodeNumber - 2, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 4, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // Remove a free node,
        //
        log("Test 7");

        resourceManager.removeNode(n2.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n2.getNodeInformation().getURL());

        assertEquals(totalNodeNumber - 3, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 5, resourceManager.getState().getFreeNodesNumber());
        log("End of test");

    }

    private void checkEvent(RMNodeEvent event, Node node) {
        assertEquals(node.getNodeInformation().getURL(), event.getNodeUrl());
        assertThat(event.getNodeInfo(), containsString(event.getNodeUrl()));
        assertThat(event.getNodeInfo(), containsString(event.getNodeProvider()));
        assertThat(event.getNodeInfo(), containsString(event.getHostName()));
    }
}
