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

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.CronPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.TimeSlotPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.nodesource.helper.CronPolicyTestHelper;
import functionaltests.nodesource.helper.RestartDownNodesPolicyTestHelper;
import functionaltests.nodesource.helper.SSHInfrastructureV2TestHelper;
import functionaltests.nodesource.helper.StaticPolicyTestHelper;
import functionaltests.nodesource.helper.TimeSlotPolicyTestHelper;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class TestSSHInfrastructureV2AllPolicies extends RMFunctionalTest {

    private static final int NB_NODES = 2;

    private static final String NODE_SOURCE_NAME = "SSHInfrastructureV2TestNodeSource";

    private ResourceManager resourceManager;

    @BeforeClass
    public static void setupTestClass() throws Exception {
        SSHInfrastructureV2TestHelper.startSSHServer();
    }

    @Before
    public void setupTest() throws Exception {
        this.resourceManager = this.rmHelper.getResourceManager();
    }

    @Test
    public void testSSHInfrastructureV2WithStaticPolicy() throws Exception {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              StaticPolicy.class.getName(),
                                              StaticPolicyTestHelper.getParameters(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(NODE_SOURCE_NAME);

        RMTHelper.waitForNodeSourceCreation(NODE_SOURCE_NAME, NB_NODES, this.rmHelper.getMonitorsHandler());

        RMTHelper.log("Check scheduler state after node source creation");
        RMState s = this.resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());
    }

    @Test
    public void testSSHInfrastructureV2WithCronPolicy() throws Exception {

        log("Create an SSHV2 node source with " + NB_NODES + " nodes with cron policy");
        createNodeSourceWithCronPolicy(NODE_SOURCE_NAME);

        log("Wait for the cron policy to remove the nodes");
        for (int i = 0; i < NB_NODES; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        log("Wait for the cron policy to add the nodes again");
        for (int i = 0; i < NB_NODES; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
    }

    @Test
    public void testSSHInfrastructureV2WithTimeSlotPolicy() throws Exception {

        log("Create a local node source with " + NB_NODES + " nodes with time slot policy");
        createNodeSourceWithTimeSlotPolicy(NODE_SOURCE_NAME);

        log("Wait for the time slot policy to remove the nodes");
        for (int i = 0; i < NB_NODES; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        log("Wait for the time slot policy to add the nodes again");
        for (int i = 0; i < NB_NODES; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        log("Wait for the time slot policy to remove the nodes again");
        for (int i = 0; i < NB_NODES; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
    }

    @Test
    public void testSSHInfrastructureV2WithRestartDownNodesPolicy() throws Exception {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              RestartDownNodesPolicy.class.getName(),
                                              RestartDownNodesPolicyTestHelper.getParameters(10000),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(NODE_SOURCE_NAME);

        RMMonitorsHandler monitorsHandler = this.rmHelper.getMonitorsHandler();

        RMTHelper.waitForNodeSourceCreation(NODE_SOURCE_NAME, NB_NODES, monitorsHandler);

        RMState s = this.resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());

        NodeSet nodeset = this.resourceManager.getNodes(new Criteria(NB_NODES));

        if (nodeset.size() != NB_NODES) {
            RMTHelper.log("Illegal state : the infrastructure could not deploy nodes or they died immediately. Ending test");
            throw new RuntimeException("Illegal state : the infrastructure could not deploy nodes or they died immediately. Ending test");
        }

        for (Node n : nodeset) {
            RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                       n.getNodeInformation().getURL(),
                                       60000,
                                       monitorsHandler);
        }

        String nodeUrl = nodeset.get(0).getNodeInformation().getURL();
        RMTHelper.log("Killing nodes");
        // Nodes will be redeployed only if we kill the whole runtime
        RMTHelper.killRuntime(nodeUrl);

        RMTHelper.log("Wait for down nodes detection by the rm");
        for (Node n : nodeset) {
            RMNodeEvent ev = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                        n.getNodeInformation().getURL(),
                                                        120000,
                                                        monitorsHandler);
            assertEquals(NodeState.DOWN, ev.getNodeState());
        }

        for (Node n : nodeset) {
            RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED,
                                       n.getNodeInformation().getURL(),
                                       120000,
                                       monitorsHandler);
        }
        RMTHelper.log("Dumping events not consumed yet");
        monitorsHandler.dumpEvents();

        RMTHelper.log("Wait for nodes restart by the policy");
        RMTHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_ADDED, NB_NODES, monitorsHandler);
        for (int i = 0; i < NB_NODES; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, monitorsHandler);
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED, monitorsHandler);
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, monitorsHandler);
        }

        RMTHelper.log("Final checks on the scheduler state");
        nodeset = this.resourceManager.getNodes(new Criteria(NB_NODES));

        for (Node n : nodeset) {
            System.out.println("NODE::" + n.getNodeInformation().getURL());
        }

        s = this.resourceManager.getState();

        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getTotalAliveNodesNumber()); // check amount of all nodes that are not down
    }

    @After
    public void tearDownTest() throws Exception {
        try {
            removeNodeSource(NODE_SOURCE_NAME);
        } catch (Exception e) {
            // ignored
        }
    }

    @AfterClass
    public static void tearDownTestClass() throws Exception {
        SSHInfrastructureV2TestHelper.stopSSHServer();
    }

    private void createNodeSourceWithCronPolicy(String nodeSourceName) throws Exception {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              CronPolicy.class.getName(),
                                              CronPolicyTestHelper.getParameters(),
                                              RMFunctionalTest.NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(nodeSourceName);

        RMTHelper.waitForNodeSourceCreation(nodeSourceName, NB_NODES, this.rmHelper.getMonitorsHandler());
    }

    private void createNodeSourceWithTimeSlotPolicy(String nodeSourceName) throws Exception {

        this.resourceManager.defineNodeSource(nodeSourceName,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              TimeSlotPolicy.class.getName(),
                                              TimeSlotPolicyTestHelper.getParameters(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(nodeSourceName);

        RMTHelper.waitForNodeSourceCreation(nodeSourceName, NB_NODES, this.rmHelper.getMonitorsHandler());
    }

    private void removeNodeSource(String sourceName) throws Exception {
        this.rmHelper.getResourceManager().removeNodeSource(sourceName, true);
        this.rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

}
