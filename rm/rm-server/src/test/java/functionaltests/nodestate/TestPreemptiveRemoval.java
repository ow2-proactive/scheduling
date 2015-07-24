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
import static org.junit.Assert.*;


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
public class TestPreemptiveRemoval extends RMFunctionalTest {

    @Test
    public void action() throws Exception {

        log("Deployment");

        ResourceManager resourceManager = rmHelper.getResourceManager();
        int totalNodeNumber = 5;
        rmHelper.createNodeSource("TestPreemptiveRemoval", totalNodeNumber);

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and remove preemptively a node
        log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(totalNodeNumber, null);
        PAFuture.waitFor(nodes);

        assertEquals(totalNodeNumber, nodes.size());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < totalNodeNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Node n1 = nodes.get(0);

        //for after, test 2
        Node n2 = nodes.get(1);

        //remove n, which is busy
        resourceManager.removeNode(n1.getNodeInformation().getURL(), true);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n1.getNodeInformation().getURL());

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());

        //try to give back removed node => no effect
        try {
            resourceManager.releaseNode(n1).getBooleanValue();
            fail("Released node which had been removed");
        } catch (RuntimeException expected) {
        }

        try {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n1.getNodeInformation().getURL(), 4000);
            fail("Should timeout");
        } catch (ProActiveTimeoutException expected) {
        }

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());

        nodes.remove(n1);
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < totalNodeNumber - 1; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertEquals(totalNodeNumber - 1, resourceManager.getState().getFreeNodesNumber());
        assertEquals(totalNodeNumber - 1, resourceManager.getState().getTotalNodesNumber());

        //----------------------------------------------------------
        // and remove preemptively a free node
        log("Test 2");

        resourceManager.removeNode(n2.getNodeInformation().getURL(), true);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n2.getNodeInformation().getURL());

        assertEquals(totalNodeNumber - 2, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 2, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // remove preemptively a toRelease node
        log("Test 3");

        nodes = resourceManager.getAtMostNodes(2, null);

        PAFuture.waitFor(nodes);
        assertEquals(2, nodes.size());

        for (int i = 0; i < 2; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Node n3 = nodes.get(0);

        //for after, test 4
        Node n4 = nodes.get(1);

        //place node in toRelease state (by a non preemptive removal)
        resourceManager.removeNode(n3.getNodeInformation().getURL(), false);

        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n3.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        assertEquals(totalNodeNumber - 2, resourceManager.getState().getTotalNodesNumber());

        //finally remove preemptively the node
        resourceManager.removeNode(n3.getNodeInformation().getURL(), true);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n3.getNodeInformation().getURL());

        assertEquals(totalNodeNumber - 3, resourceManager.getState().getTotalNodesNumber());

        nodes.remove(n3);
        resourceManager.releaseNodes(nodes);

        evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        assertEquals(evt.getNodeState(), NodeState.FREE);

        //----------------------------------------------------------
        // remove preemptively a down node
        log("Test 4");

        try {
            n4.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n4.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.DOWN);

        //check that node down event has been thrown
        assertEquals(totalNodeNumber - 3, resourceManager.getState().getTotalNodesNumber());

        resourceManager.removeNode(n4.getNodeInformation().getURL(), true);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n4.getNodeInformation().getURL());

        assertEquals(totalNodeNumber - 4, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 4, resourceManager.getState().getFreeNodesNumber());

        //----------------------------------------------------------
        // and remove preemptively a node not handled by RM
        log("Test 5");

        resourceManager.removeNode("rmi://unknown_node", true);
        assertEquals(totalNodeNumber - 4, resourceManager.getState().getTotalNodesNumber());
        assertEquals(totalNodeNumber - 4, resourceManager.getState().getFreeNodesNumber());

        try {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, 3000);
            fail("Unexpected node event " + evt);
        } catch (ProActiveTimeoutException expected) {
        }

    }
}
