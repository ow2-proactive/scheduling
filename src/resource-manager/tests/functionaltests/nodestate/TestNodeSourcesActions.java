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

import java.io.File;

import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * This class tests actions of adding and removing node sources, particulary the removal
 * of a node source, preemptively or not
 *
 * Add a node source (test 1)
 * put nodes of the nodes in different states ; free, busy, down, to Release,
 * remove the node source preemptively (test 2).
 *
 * Add another node source, and put nodes of the nodes in different states ;
 * free, busy, down, to Release,
 * Remove the node source non preemptively (test 3).
 *
 * @author ProActive team
 */
public class TestNodeSourcesActions extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        String nodeSourceName = "GCM_Node_source_test1";

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(
            RMTHelper.defaultDescriptor)));
        //first im parameter is default rm url
        resourceManager.createNodeSource(nodeSourceName, GCMInfrastructure.class.getName(), new Object[] {
                "", GCMDeploymentData }, StaticPolicy.class.getName(), null);

        int pingFrequency = 5000;
        resourceManager.setNodeSourcePingFrequency(pingFrequency, nodeSourceName);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nodeSourceName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //wait for the nodes to be in free state
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        //book 3 nodes
        NodeSet nodes = resourceManager.getAtMostNodes(3, null);
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 3);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 3);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);

        for (int i = 0; i < 3; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        //put one of the busy node in 'to release' state
        Node n1 = nodes.remove(0);
        resourceManager.removeNode(n1.getNodeInformation().getURL(), false);

        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n1.getNodeInformation()
                .getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        //put one of the busy node in 'down' state
        Node n2 = nodes.remove(0);

        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        //kill preemptively the node source
        resourceManager.removeNodeSource(nodeSourceName, true);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nodeSourceName);

        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 0);

        //test the non preemptive node source removal
        RMTHelper.log("Test 2");

        String nodeSourceName2 = "GCM_Node_source_test2";
        //first im parameter is default rm url
        resourceManager.createNodeSource(nodeSourceName2, GCMInfrastructure.class.getName(), new Object[] {
                "", GCMDeploymentData }, StaticPolicy.class.getName(), null);

        resourceManager.setNodeSourcePingFrequency(pingFrequency, nodeSourceName2);

        //wait for creation of GCM Node Source event, and deployment of its nodes

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nodeSourceName2);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //wait for the nodes to be in free state
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        //book 3 nodes
        nodes = resourceManager.getAtMostNodes(3, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < 3; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        assertTrue(nodes.size() == 3);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber - 3);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);

        //put one of the busy node in 'to release' state
        n1 = nodes.remove(0);
        resourceManager.removeNode(n1.getNodeInformation().getURL(), false);

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n1.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        //put one of the busy node in 'down' state
        n2 = nodes.remove(0);

        Node n3 = nodes.remove(0);

        try {
            n2.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.DOWN);

        //kill non preemptively the node source
        resourceManager.removeNodeSource(nodeSourceName2, false);

        //the node isn't removed immediately because one its node is
        //in to Release state, and one in busy state

        //the two free nodes and the down node (n2) are removed immediately
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 2; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //the 'to release' node (n1) keeps the same state

        //the busy node (n3) becomes a 'to release' node
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n3.getNodeInformation().getURL());
        Assert.assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 2);

        //give back the two nodes in 'to release' state, they are directly removed
        resourceManager.releaseNode(n1);
        resourceManager.releaseNode(n3);

        for (int i = 0; i < 2; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 0);

        //no more nodes handled by the node source,
        //so the node source can be removed
        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nodeSourceName2);
    }
}
