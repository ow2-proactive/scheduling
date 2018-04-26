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
package functionaltests.nodesource.deployment;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMNodeSourceHelper;
import functionaltests.utils.RMTHelper;


public class LocalInfrastructureLifecycleTest extends RMFunctionalTest {

    private static final String NODE_SOURCE_NAME = "NodeSourceFor" +
                                                   LocalInfrastructureLifecycleTest.class.getSimpleName();

    private static final int NUMBER_OF_NODES = 5;

    private static final int NO_NODES_EXPECTED = 0;

    private ResourceManager resourceManager;

    private RMMonitorsHandler monitor;

    @Before
    public void setup() throws Exception {
        this.resourceManager = this.rmHelper.getResourceManager();
        this.monitor = this.rmHelper.getMonitorsHandler();
    }

    @Test
    public void testDeployAndUndeploySSHInfrastructureV2() throws Exception {
        RMTHelper.log("Starting test of deployment and undeployment of node source " + NODE_SOURCE_NAME);

        RMNodeSourceHelper.defineLocalNodeSourceAndWait(NODE_SOURCE_NAME,
                                                        NUMBER_OF_NODES,
                                                        this.resourceManager,
                                                        this.monitor);
        this.checkResourceManagerState(NO_NODES_EXPECTED);

        this.deployNodeSourceAndCheck();

        RMTHelper.log("Undeploy node source");
        this.resourceManager.undeployNodeSource(NODE_SOURCE_NAME, true);
        RMNodeSourceHelper.waitForNodeSourceUndeployment(NODE_SOURCE_NAME, NUMBER_OF_NODES, this.monitor);
        this.checkResourceManagerState(NO_NODES_EXPECTED);

        RMTHelper.log("Deploy node source again");
        this.deployNodeSourceAndCheck();

        RMTHelper.log("Remove node source");
        this.resourceManager.removeNodeSource(NODE_SOURCE_NAME, true);
        RMNodeSourceHelper.waitForNodeSourceRemoval(NODE_SOURCE_NAME, NUMBER_OF_NODES, this.monitor);
        this.checkResourceManagerState(NO_NODES_EXPECTED);
    }

    private void deployNodeSourceAndCheck() {
        RMNodeSourceHelper.deployNodeSourceAndWait(NODE_SOURCE_NAME,
                                                   this.resourceManager,
                                                   this.monitor,
                                                   NUMBER_OF_NODES);
        this.checkResourceManagerState(NUMBER_OF_NODES);
    }

    private void checkResourceManagerState(int expectedNumberOfNodes) {
        RMState s = this.resourceManager.getState();
        assertEquals(expectedNumberOfNodes, s.getTotalNodesNumber());
        assertEquals(expectedNumberOfNodes, s.getFreeNodesNumber());
    }

}
