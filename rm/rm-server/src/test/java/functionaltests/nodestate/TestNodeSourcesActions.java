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

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import java.io.File;

import static org.junit.Assert.assertEquals;


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
public class TestNodeSourcesActions extends RMFunctionalTest {

    @Test
    public void testAddRemoveNodesPreemptively() throws Exception {

        String nodeSourceName = "TestNodeSourcesActions";

        ResourceManager resourceManager = rmHelper.getResourceManager();
        int nodeNumber = 5;

        int pingFrequency = 5000;

        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));

        resourceManager.createNodeSource(nodeSourceName, LocalInfrastructure.class.getName(), new Object[] {
                creds, nodeNumber, RMTHelper.DEFAULT_NODES_TIMEOUT, "" }, StaticPolicy.class.getName(), null);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nodeSourceName);
        resourceManager.setNodeSourcePingFrequency(pingFrequency, nodeSourceName);

        RMTHelper.log("Test 1");
        for (int i = 0; i < nodeNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //wait for the nodes to be in free state
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        assertEquals(nodeNumber, resourceManager.getState().getTotalNodesNumber());
        assertEquals(nodeNumber, resourceManager.getState().getFreeNodesNumber());

        //book 3 nodes
        NodeSet nodes = resourceManager.getAtMostNodes(3, null);
        PAFuture.waitFor(nodes);

        assertEquals(3, nodes.size());
        assertEquals(nodeNumber - 3, resourceManager.getState().getFreeNodesNumber());
        assertEquals(nodeNumber, resourceManager.getState().getTotalNodesNumber());

        for (int i = 0; i < 3; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        //put one of the busy node in 'to release' state
        Node n1 = nodes.remove(0);
        resourceManager.removeNode(n1.getNodeInformation().getURL(), false);

        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
          n1.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        //put one of the busy node in 'down' state
        Node n2 = nodes.remove(0);

        try {
            n2.getProActiveRuntime().killNode(n2.getNodeInformation().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.DOWN);

        //kill preemptively the node source
        resourceManager.removeNodeSource(nodeSourceName, true);

        for (int i = 0; i < nodeNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nodeSourceName);

        assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        assertEquals(0, resourceManager.getState().getTotalNodesNumber());

        //test the non preemptive node source removal
        RMTHelper.log("Test 2");

        String nodeSourceName2 = "TestNodeSourcesActions2";
        //first im parameter is default rmHelper url
        int expectedNodeNumber = 3;
        rmHelper.createNodeSource(nodeSourceName2, expectedNodeNumber);
        resourceManager.setNodeSourcePingFrequency(pingFrequency, nodeSourceName2);

        assertEquals(expectedNodeNumber, resourceManager.getState().getTotalNodesNumber());
        assertEquals(expectedNodeNumber, resourceManager.getState().getFreeNodesNumber());

        //book 3 nodes
        nodes = resourceManager.getAtMostNodes(3, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < 3; i++) {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        assertEquals(3, nodes.size());
        assertEquals(expectedNodeNumber - 3, resourceManager.getState().getFreeNodesNumber());
        assertEquals(expectedNodeNumber, resourceManager.getState().getTotalNodesNumber());

        //put one of the busy node in 'to release' state
        n1 = nodes.remove(0);
        resourceManager.removeNode(n1.getNodeInformation().getURL(), false);

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n1.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        //put one of the busy node in 'down' state
        n2 = nodes.remove(0);

        Node n3 = nodes.remove(0);

        try {
            n2.getProActiveRuntime().killNode(n2.getNodeInformation().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n2.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.DOWN);

        //kill non preemptively the node source
        resourceManager.removeNodeSource(nodeSourceName2, false);

        //the node isn't removed immediately because one its node is
        //in to Release state, and one in busy state

        //the two free nodes and the down node (n2) are removed immediately
        for (int i = 0; i < expectedNodeNumber - 2; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //the 'to release' node (n1) keeps the same state

        //the busy node (n3) becomes a 'to release' node
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n3.getNodeInformation().getURL());
        assertEquals(evt.getNodeState(), NodeState.TO_BE_REMOVED);

        assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        assertEquals(2, resourceManager.getState().getTotalNodesNumber());

        //give back the two nodes in 'to release' state, they are directly removed
        resourceManager.releaseNode(n1);
        resourceManager.releaseNode(n3);

        for (int i = 0; i < 2; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        assertEquals(0, resourceManager.getState().getTotalNodesNumber());
    }
}
