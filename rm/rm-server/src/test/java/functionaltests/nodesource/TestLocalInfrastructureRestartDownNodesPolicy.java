/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.nodesource;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 * Test checks the correct behavior of node source consisted of Local infrastructure manager
 * and static acquisition policy.
 *
 */
public class TestLocalInfrastructureRestartDownNodesPolicy extends RMFunctionalTest {

    protected int defaultDescriptorNodesNb = 3;

    ResourceManager resourceManager;

    String nodeSourceName;

    protected void createNodeSourceWithNodes(String sourceName, Object[] policyParameters) throws Exception {

        // creating node source
        // first parameter of im is empty default rmHelper url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        rmHelper.getResourceManager()
                .createNodeSource(sourceName,
                                  LocalInfrastructure.class.getName(),
                                  new Object[] { creds, defaultDescriptorNodesNb, RMTHelper.DEFAULT_NODES_TIMEOUT, "" },
                                  RestartDownNodesPolicy.class.getName(),
                                  policyParameters);

        rmHelper.waitForNodeSourceCreation(sourceName, defaultDescriptorNodesNb);
    }

    @Before
    public void setup() throws Exception {
        resourceManager = rmHelper.getResourceManager();
    }

    public void cleanup() {
        try {
            resourceManager.removeNodeSource(nodeSourceName, true);
        } catch (Exception e) {

        }
    }

    @Test
    public void testRestartDownNodesPolicyWithNullParams() throws Exception {
        RMTHelper.log("Test 0 - create down nodes policy with null parameters (null is a valid input)");
        nodeSourceName = "Node_source_0";
        createNodeSourceWithNodes(nodeSourceName, null);

        RMState stateTest0 = resourceManager.getState();
        assertEquals(defaultDescriptorNodesNb, stateTest0.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest0.getFreeNodesNumber());
    }

    @Test
    public void testRestartDownNodesPolicy() throws Exception {
        nodeSourceName = "Node_source_1";

        RMTHelper.log("Test 1 - restart down nodes policy");
        createNodeSourceWithNodes(nodeSourceName, new Object[] { "ALL", "ALL", "10000" });

        RMState stateTest1 = resourceManager.getState();
        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest1.getFreeNodesNumber());

        NodeSet ns = resourceManager.getNodes(new Criteria(defaultDescriptorNodesNb));

        for (Node n : ns) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        }

        String nodeUrl = ns.get(0).getNodeInformation().getURL();
        // Nodes will be redeployed only if we kill the whole runtime
        rmHelper.killRuntime(nodeUrl);

        RMNodeEvent ev = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeUrl);

        assertEquals(NodeState.DOWN, ev.getNodeState());

        // one node is down - the policy should detect it and redeploy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalAliveNodesNumber());
    }

    /**
     * This test ensures that when a node has been manually removed it is not redeployed by the policy
     */
    @Test
    public void testRestartDownNodesPolicyWithRemoveNode() throws Exception {
        nodeSourceName = "Node_source_2";

        RMTHelper.log("Test 2 - restart down nodes policy with a node removed");
        createNodeSourceWithNodes(nodeSourceName, new Object[] { "ALL", "ALL", "10000" });

        RMState stateTest1 = resourceManager.getState();
        assertEquals(defaultDescriptorNodesNb, stateTest1.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb, stateTest1.getFreeNodesNumber());

        NodeSet ns = resourceManager.getNodes(new Criteria(defaultDescriptorNodesNb));

        for (Node n : ns) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, n.getNodeInformation().getURL());
        }

        // remove the first node
        Node nodeToRemove = ns.remove(0);
        resourceManager.removeNode(nodeToRemove.getNodeInformation().getURL(), true);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, nodeToRemove.getNodeInformation().getURL());

        RMTHelper.log("Node removed.");

        stateTest1 = resourceManager.getState();
        assertEquals(defaultDescriptorNodesNb - 1, stateTest1.getTotalNodesNumber());

        String nodeUrl = ns.get(0).getNodeInformation().getURL();
        // Nodes will be redeployed only if we kill the whole runtime
        rmHelper.killRuntime(nodeUrl);

        // one node is down - the policy should detect it and redeploy
        for (int i = 0; i < defaultDescriptorNodesNb - 1; i++) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, ns.get(i).getNodeInformation().getURL());
        }

        RMNodeEvent ev = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeUrl);

        assertEquals(NodeState.DOWN, ev.getNodeState());

        // one node is down - the policy should detect it and redeploy
        for (int i = 0; i < defaultDescriptorNodesNb - 1; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        // now one node less should be deployed
        assertEquals(defaultDescriptorNodesNb - 1, stateTest1.getTotalNodesNumber());
        assertEquals(defaultDescriptorNodesNb - 1, stateTest1.getTotalAliveNodesNumber());
    }
}
