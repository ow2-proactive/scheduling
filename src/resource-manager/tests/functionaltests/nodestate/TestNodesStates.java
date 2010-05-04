/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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

import functionalTests.FunctionalTest;
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
public class TestNodesStates extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper.log("Deployment");

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and give back to RM
        RMTHelper.log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        //for next test
        Node n = nodes.get(0);

        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        //----------------------------------------------------------
        //give back a node already given back (i.e; node already free)
        //this action causes nothing(nor increasing free nodes number, nor generation of any event)
        RMTHelper.log("Test 2");

        resourceManager.releaseNode(n);

        boolean timeouted = false;
        try {
            RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL(), 4000);
        } catch (ProActiveTimeoutException e) {
            // TODO Auto-generated catch block
            timeouted = true;
        }

        assertTrue(timeouted);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor
        // Test admin action : Remove a node from the RM (non preemptively),
        // node is busy, so becomes in "toRelease" state
        // user give back to RM the "toRelease" node, node is now removed
        RMTHelper.log("Test 3");

        nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);

        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        n = nodes.remove(0);

        //put node in "To Release" state
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        //check that node toRelease event has been thrown
        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation()
                .getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_RELEASED);

        //node is in "ToRelease" state, so always handled by RM
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);

        //user give back the node, so node is now removed
        resourceManager.releaseNode(n);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());

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

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        resourceManager.releaseNodes(nodes);

        // check Nodes freed Event has been thrown
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 2; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        //two nodes killed, but the detected down is in RM down nodes list
        //( down nodes are in total nodes count)
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 2);

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

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 3);

        //----------------------------------------------------------
        // book nodes, put one node in "toRelease" state,
        // then kill its JVM,
        // node must detected down by RM
        RMTHelper.log("Test 6");

        nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber - 3, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber - 3; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        n = nodes.get(0);
        n2 = nodes.get(1); //for next test

        //put node in "To Release" state
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_RELEASED);

        RMTHelper.log("Test 6 Bis");

        //kill the node
        try {
            n.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        //user tries to give back a down node, no bad effect
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber - 4; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 4);

        //admin removes again the node, ok he already asked this removal when node n was busy
        //choice here is advert admin that node has fallen (not hiding the down node event),
        //rather than automatically remove it
        resourceManager.removeNode(n.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 2);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 4);

        //----------------------------------------------------------
        // Remove a free node,
        //
        RMTHelper.log("Test 7");

        resourceManager.removeNode(n2.getNodeInformation().getURL(), false);

        //check that node removed event has been received
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n2.getNodeInformation().getURL());

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber - 3);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 5);

        RMTHelper.log("End of test");
    }

}
