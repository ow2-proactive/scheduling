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
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Test;

import functionaltests.RMConsecutive;

import static functionaltests.RMTHelper.log;
import static org.junit.Assert.*;


/**
 * This class tests ResourceManager.getNodes method
 * in best effort mode and in strict mode 
 * 
 */
public class TestGetNodes extends RMConsecutive {

    @Test
    public void action() throws Exception {
        log("Deployment");

        ResourceManager resourceManager = rmHelper.getResourceManager();
        int nodesNumber = rmHelper.createNodeSource("TestGetNodes");

        log("Test 1 - best effort mode");

        NodeSet nodes = resourceManager.getNodes(nodesNumber, TopologyDescriptor.ARBITRARY, null, null, true);

        PAFuture.waitFor(nodes);
        assertEquals(nodesNumber, nodes.size());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.BUSY, evt.getNodeState());
        }
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, evt.getNodeState());
        }

        nodes = resourceManager.getNodes(nodesNumber + 1, TopologyDescriptor.ARBITRARY, null, null, true);

        PAFuture.waitFor(nodes);
        assertEquals(nodesNumber, nodes.size());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.BUSY, evt.getNodeState());
        }
        resourceManager.releaseNodes(nodes);
        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, evt.getNodeState());
        }

        log("Test 2 - strict mode");
        nodes = resourceManager.getNodes(nodesNumber, TopologyDescriptor.ARBITRARY, null, null, false);

        PAFuture.waitFor(nodes);
        assertEquals(nodesNumber, nodes.size());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.BUSY, evt.getNodeState());
        }
        resourceManager.releaseNodes(nodes);
        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, evt.getNodeState());
        }

        nodes = resourceManager.getNodes(nodesNumber + 1, TopologyDescriptor.ARBITRARY, null, null, false);

        PAFuture.waitFor(nodes);
        assertEquals(0, nodes.size());
        assertEquals(nodesNumber, resourceManager.getState().getFreeNodesNumber());

        log("End of test");
    }

}
