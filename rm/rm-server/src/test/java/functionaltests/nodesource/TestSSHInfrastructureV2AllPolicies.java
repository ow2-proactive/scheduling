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

import static com.google.common.truth.Truth.assertThat;
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
        RMState s = this.resourceManager.getState();
        assertEquals(0, s.getTotalNodesNumber());
    }

    @Test
    public void testSSHInfrastructureV2WithStaticPolicy() throws Exception {

        log("Create an SSHV2 node source with " + NB_NODES + " nodes using static policy");
        createNodeSourceWithStaticPolicy();
        assertThatRmHasAllNodes();
    }

    @Test
    public void testSSHInfrastructureV2WithCronPolicy() throws Exception {

        log("Create an SSHV2 node source with " + NB_NODES + " nodes using cron policy");
        createNodeSourceWithCronPolicy();
        assertThatRmHasAllNodes();

        log("Wait for the cron policy to remove the nodes");
        waitForNodesToBeRemoved();
        assertThatRmHasNoNode();

        log("Wait for the cron policy to add the nodes again");
        RMTHelper.waitForNodesToBeUp(NB_NODES, this.rmHelper.getMonitorsHandler());
        assertThatRmHasAllNodes();
    }

    @Test
    public void testSSHInfrastructureV2WithTimeSlotPolicy() throws Exception {

        log("Create an SSHV2 node source with " + NB_NODES + " nodes using time slot policy");
        createNodeSourceWithTimeSlotPolicy();
        assertThatRmHasAllNodes();

        log("Wait for the time slot policy to remove the nodes");
        waitForNodesToBeRemoved();
        assertThatRmHasNoNode();

        log("Wait for the time slot policy to add the nodes again");
        RMTHelper.waitForNodesToBeUp(NB_NODES, this.rmHelper.getMonitorsHandler());
        assertThatRmHasAllNodes();

        log("Wait for the time slot policy to remove the nodes again");
        waitForNodesToBeRemoved();
        assertThatRmHasNoNode();
    }

    @Test
    public void testSSHInfrastructureV2WithRestartDownNodesPolicy() throws Exception {

        log("Create an SSHV2 node source with " + NB_NODES + " nodes using restart down nodes policy");
        createNodeSourceWithRestartDownNodesPolicy();
        assertThatRmHasAllNodes();

        NodeSet nodeset = this.resourceManager.getNodes(new Criteria(NB_NODES));
        assertThat(nodeset.size()).isEqualTo(NB_NODES);

        RMMonitorsHandler monitorsHandler = this.rmHelper.getMonitorsHandler();

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
        RMTHelper.waitForNodesToBeUp(NB_NODES, monitorsHandler);

        RMTHelper.log("Final checks on the scheduler state");
        nodeset = this.resourceManager.getNodes(new Criteria(NB_NODES));

        for (Node n : nodeset) {
            System.out.println("NODE::" + n.getNodeInformation().getURL());
            assertThat(n.getNodeInformation().getURL()).contains(NODE_SOURCE_NAME);
        }

        RMState s = this.resourceManager.getState();

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

    private void createNodeSourceWithStaticPolicy() throws Exception {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              StaticPolicy.class.getName(),
                                              StaticPolicyTestHelper.getParameters(),
                                              NODES_NOT_RECOVERABLE);
        deployNodeSourceAndWait();
    }

    private void createNodeSourceWithCronPolicy() throws Exception {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              CronPolicy.class.getName(),
                                              CronPolicyTestHelper.getParameters(),
                                              RMFunctionalTest.NODES_NOT_RECOVERABLE);
        deployNodeSourceAndWait();
    }

    private void createNodeSourceWithTimeSlotPolicy() throws Exception {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              TimeSlotPolicy.class.getName(),
                                              TimeSlotPolicyTestHelper.getParameters(),
                                              NODES_NOT_RECOVERABLE);
        deployNodeSourceAndWait();
    }

    private void createNodeSourceWithRestartDownNodesPolicy() {

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              SSHInfrastructureV2TestHelper.getParameters(NB_NODES),
                                              RestartDownNodesPolicy.class.getName(),
                                              RestartDownNodesPolicyTestHelper.getParameters(10000),
                                              NODES_NOT_RECOVERABLE);
        deployNodeSourceAndWait();
    }

    private void deployNodeSourceAndWait() {

        this.resourceManager.deployNodeSource(NODE_SOURCE_NAME);
        RMTHelper.waitForNodeSourceCreation(NODE_SOURCE_NAME, NB_NODES, this.rmHelper.getMonitorsHandler());
    }

    private void assertThatRmHasNoNode() {
        RMState s = this.resourceManager.getState();
        assertEquals(0, s.getTotalNodesNumber());
    }

    private void assertThatRmHasAllNodes() {
        RMState s = this.resourceManager.getState();
        assertThat(s.getTotalNodesNumber()).isEqualTo(NB_NODES);
        assertThat(s.getFreeNodesNumber()).isEqualTo(NB_NODES);
    }

    private void waitForNodesToBeRemoved() {
        for (int i = 0; i < NB_NODES; i++) {
            this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
    }

    private void removeNodeSource(String sourceName) throws Exception {
        this.rmHelper.getResourceManager().removeNodeSource(sourceName, true);
        this.rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

}
