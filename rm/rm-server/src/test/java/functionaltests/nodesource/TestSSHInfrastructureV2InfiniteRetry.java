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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class TestSSHInfrastructureV2InfiniteRetry extends RMFunctionalTest {

    private static final String NODE_SOURCE_NAME = TestSSHInfrastructureV2InfiniteRetry.class.getSimpleName();

    private static final int ONE_NODE_PER_HOST = 1;

    private static final int INFINITE_RETRY = -1;

    private ResourceManager resourceManager;

    @Before
    public void setup() throws Exception {
        TestSSHInfrastructureV2.startSSHServer();
    }

    @Test
    public void testSSHInfrastructureV2WithRestartDownNodes() throws Exception {

        this.resourceManager = this.rmHelper.getResourceManager();

        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              getInfiniteRetryInfrastructureParameters(),
                                              RestartDownNodesPolicy.class.getName(),
                                              TestSSHInfrastructureV2.policyParameters,
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(NODE_SOURCE_NAME);

        RMTHelper.log("Waiting for the RM to have one free node");
        while (this.resourceManager.getState().getFreeNodesNumber() != ONE_NODE_PER_HOST) {
            Thread.sleep(500);
        }
        RMTHelper.log("The RM has one free node");

        RMTHelper.log("Killing the free node");
        String freeNodeUrl = this.resourceManager.getState().getFreeNodes().iterator().next();
        RMTHelper.killRuntime(freeNodeUrl);

        RMTHelper.log("Waiting for the RM to detect the down node");
        while (this.resourceManager.getState().getFreeNodesNumber() != 0) {
            Thread.sleep(500);
        }
        RMTHelper.log("The RM has detected the down node");

        RMTHelper.log("Waiting for the down node to be redeployed by the policy");
        while (this.resourceManager.getState().getFreeNodesNumber() != ONE_NODE_PER_HOST) {
            Thread.sleep(500);
        }
        RMTHelper.log("The down node has been redeployed by the policy");
    }

    private Object[] getInfiniteRetryInfrastructureParameters() {
        return new Object[] { getHostsFileContent(), // hosts
                              180000, // node timeout 3 minutes -- not relevant to the test
                              INFINITE_RETRY, // failure attempts
                              10000, // wait between failures
                              TestSSHInfrastructureV2.getPort(), //ssh server port
                              "toto", //ssh username
                              "toto", //ssh password
                              new byte[0], // optional ssh private key
                              new byte[0], // optional ssh options file
                              TestSSHInfrastructureV2.getJavaPath(), //java path on the remote machines
                              PAResourceManagerProperties.RM_HOME.getValueAsString(), //Scheduling path on remote machines
                              OperatingSystem.getOperatingSystem(), "" }; // extra java options
    }

    private byte[] getHostsFileContent() {
        return ("localhost " + ONE_NODE_PER_HOST + "\n10.2.3.4 " + ONE_NODE_PER_HOST + "\n10.2.3.5 " +
                ONE_NODE_PER_HOST + "\n10.2.3.6 " + ONE_NODE_PER_HOST + "\n10.2.3.7 " + ONE_NODE_PER_HOST +
                "\n10.2.3.8 " + ONE_NODE_PER_HOST).getBytes();
    }

    @After
    public void removeNS() throws Exception {
        RMTHelper.log("Removing node source");
        try {
            this.resourceManager.removeNodeSource(NODE_SOURCE_NAME, true);
        } catch (Exception ignored) {

        }
        TestSSHInfrastructureV2.stopSSHServer();
    }
}
