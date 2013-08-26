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
import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;
import junit.framework.Assert;


/**
 * This class tests ResourceManager.getNodes method
 * in best effort mode and in strict mode 
 * 
 */
public class TestGetNodes extends RMConsecutive {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper.log("Deployment");

        RMTHelper helper = RMTHelper.getDefaultInstance();
        ResourceManager resourceManager = helper.getResourceManager();
        int nodesNumber = helper.createNodeSource("TestGetNodes");

        RMTHelper.log("Test 1 - best effort mode");

        NodeSet nodes = resourceManager.getNodes(nodesNumber, TopologyDescriptor.ARBITRARY, null, null, true);

        PAFuture.waitFor(nodes);
        Assert.assertEquals(nodesNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.BUSY, evt.getNodeState());
        }
        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.FREE, evt.getNodeState());
        }

        nodes = resourceManager.getNodes(nodesNumber + 1, TopologyDescriptor.ARBITRARY, null, null, true);

        PAFuture.waitFor(nodes);
        Assert.assertEquals(nodesNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.BUSY, evt.getNodeState());
        }
        resourceManager.releaseNodes(nodes);
        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.FREE, evt.getNodeState());
        }

        RMTHelper.log("Test 2 - strict mode");
        nodes = resourceManager.getNodes(nodesNumber, TopologyDescriptor.ARBITRARY, null, null, false);

        PAFuture.waitFor(nodes);
        Assert.assertEquals(nodesNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.BUSY, evt.getNodeState());
        }
        resourceManager.releaseNodes(nodes);
        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.FREE, evt.getNodeState());
        }

        nodes = resourceManager.getNodes(nodesNumber + 1, TopologyDescriptor.ARBITRARY, null, null, false);

        PAFuture.waitFor(nodes);
        Assert.assertEquals(0, nodes.size());
        Assert.assertEquals(nodesNumber, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("End of test");
    }

}
