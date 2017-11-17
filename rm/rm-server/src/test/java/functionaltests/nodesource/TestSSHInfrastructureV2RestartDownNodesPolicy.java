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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class TestSSHInfrastructureV2RestartDownNodesPolicy extends RMFunctionalTest {

    private static String nsname = "testSSHInfra";

    private static int NB_NODES = 3;

    private ResourceManager resourceManager;

    @Before
    public void setup() throws Exception {
        TestSSHInfrastructureV2.startSSHServer();
    }

    @Test
    public void testSSHInfrastructureV2WithRestartDownNodes() throws Exception {

        nsname = "testSSHInfraRestart";

        resourceManager = this.rmHelper.getResourceManager();

        RMTHelper.log("Test - Create SSH infrastructure with RestartDownNodes policy on ssh://localhost on port " +
                      TestSSHInfrastructureV2.port);

        resourceManager.createNodeSource(nsname,
                                         SSHInfrastructureV2.class.getName(),
                                         TestSSHInfrastructureV2.infraParams,
                                         RestartDownNodesPolicy.class.getName(),
                                         TestSSHInfrastructureV2.policyParameters);
        RMMonitorsHandler monitorsHandler = this.rmHelper.getMonitorsHandler();

        this.rmHelper.waitForNodeSourceCreation(nsname, NB_NODES, monitorsHandler);

        RMState s = resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());

        NodeSet nodeset = resourceManager.getNodes(new Criteria(NB_NODES));

        if (nodeset.size() != NB_NODES) {
            RMTHelper.log("Illegal state : the infrastructure could not deploy nodes or they died immediately. Ending test");
            throw new RuntimeException("Illegal state : the infrastructure could not deploy nodes or they died immediately. Ending test");
        }

        for (Node n : nodeset) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                      n.getNodeInformation().getURL(),
                                      60000,
                                      monitorsHandler);
        }

        String nodeUrl = nodeset.get(0).getNodeInformation().getURL();
        RMTHelper.log("Killing nodes");
        // Nodes will be redeployed only if we kill the whole runtime
        rmHelper.killRuntime(nodeUrl);

        RMTHelper.log("Wait for down nodes detection by the rm");
        for (Node n : nodeset) {
            RMNodeEvent ev = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                       n.getNodeInformation().getURL(),
                                                       120000,
                                                       monitorsHandler);
            assertEquals(NodeState.DOWN, ev.getNodeState());
        }

        for (Node n : nodeset) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED,
                                      n.getNodeInformation().getURL(),
                                      120000,
                                      monitorsHandler);
        }
        RMTHelper.log("Dumping events not consumed yet");
        monitorsHandler.dumpEvents();

        RMTHelper.log("Wait for nodes restart by the policy");
        rmHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_ADDED, NB_NODES, monitorsHandler);
        for (int i = 0; i < NB_NODES; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, monitorsHandler);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED, monitorsHandler);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, monitorsHandler);
        }

        RMTHelper.log("Final checks on the scheduler state");
        nodeset = resourceManager.getNodes(new Criteria(NB_NODES));

        for (Node n : nodeset) {
            System.out.println("NODE::" + n.getNodeInformation().getURL());
        }

        s = resourceManager.getState();

        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getTotalAliveNodesNumber()); // check amount of all nodes that are not down
    }

    @After
    public void removeNS() throws Exception {
        RMTHelper.log("Removing node source");
        try {
            resourceManager.removeNodeSource(nsname, true);
        } catch (Exception ignored) {

        }
        TestSSHInfrastructureV2.stopSSHServer();
    }
}
