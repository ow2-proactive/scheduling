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
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
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

        RMAdmin admin = RMTHelper.getAdminInterface();

        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        //----------------------------------------------------------
        // Book all nodes deployed by descriptor (user action)
        // verify that there are no free nodes left,
        // and remove preemptively a node
        RMTHelper.log("Test 1");

        NodeSet nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Node n1 = nodes.get(0);

        //for after, test 2
        Node n2 = nodes.get(1);

        //remove n, which is busy
        admin.removeNode(n1.getNodeInformation().getURL(), true);

        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n1.getNodeInformation()
                .getURL());

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 1);

        //try to give back removed node => no effect
        admin.freeNode(n1);

        boolean timeouted = false;
        try {
            RMTHelper
                    .waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n1.getNodeInformation().getURL(), 4000);
        } catch (ProActiveTimeoutException e) {
            // TODO Auto-generated catch block
            timeouted = true;
        }

        assertTrue(timeouted);
        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 1);

        admin.freeNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 1);

        //----------------------------------------------------------
        // and remove preemptively a free node
        RMTHelper.log("Test 2");

        admin.removeNode(n2.getNodeInformation().getURL(), true);

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n2.getNodeInformation().getURL());

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 2);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 2);

        //----------------------------------------------------------
        // remove preemptively a toRelease node
        RMTHelper.log("Test 3");

        nodes = admin.getAtMostNodes(2, null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == 2);

        for (int i = 0; i < 2; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Node n3 = nodes.get(0);

        //for after, test 4
        Node n4 = nodes.get(1);

        //place node in toRelease state (by a non preemptive removal)
        admin.removeNode(n3.getNodeInformation().getURL(), false);

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n3.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_RELEASED);

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 2);

        //finally remove preemptively the node
        admin.removeNode(n3.getNodeInformation().getURL(), true);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n3.getNodeInformation().getURL());

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 3);

        admin.freeNodes(nodes);

        evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        //----------------------------------------------------------
        // remove preemptively a down node
        RMTHelper.log("Test 4");

        try {
            n4.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n4.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        //check that node down event has been thrown
        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 3);

        admin.removeNode(n4.getNodeInformation().getURL(), true);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n4.getNodeInformation().getURL());

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 4);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 4);

        //----------------------------------------------------------
        // and remove preemptively a node not handled by RM
        RMTHelper.log("Test 5");

        admin.removeNode("rmi://unknown_node", true);
        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 4);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 4);

        timeouted = false;
        try {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, 4000);
        } catch (ProActiveTimeoutException e) {
            // TODO Auto-generated catch block
            timeouted = true;
        }

        assertTrue(timeouted);

        RMTHelper.log("end of test");
    }
}
