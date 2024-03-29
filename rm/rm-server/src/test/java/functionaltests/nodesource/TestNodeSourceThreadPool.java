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
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.TestTimedOutException;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class TestNodeSourceThreadPool extends RMFunctionalTest {

    private static final String TWO_THREADS_CONFIG_FILE_PATH = "/functionaltests/config/functionalTRMProperties-RM-node-source-thread-pool-2-threads.ini";

    private static final String SIX_THREADS_CONFIG_FILE_PATH = "/functionaltests/config/functionalTRMProperties-RM-node-source-thread-pool-6-threads.ini";

    private static final String NODE_SOURCE_NAME = TestNodeSourceThreadPool.class.getSimpleName();

    private static final String FAKE_HOST_FOR_DEPLOYMENT_FAILURE = "10.2.3.4";

    private static final int NODE_STATE_CHANGED_TIMEOUT = 30000; // 30 seconds

    private static final int ACTIVE_WAITING_PERIOD = 500; // 500 milliseconds

    private static final int ONE_NODE_PER_HOST = 1;

    private static final int INFINITE_RETRY = -1;

    private ResourceManager resourceManager;

    @Before
    public void setup() throws Exception {
        // ignore test on windows, the windows command spawned from the ssh server fails (without any possibility to make it work)
        assumeTrue(OperatingSystem.getOperatingSystem() != OperatingSystem.windows);
        TestSSHInfrastructureV2.startSSHServer();
    }

    @Test
    public void testDownNodesCanStillBeDetectedWhenDeploymentThreadsAreExhausted() throws Exception {
        String rmConfig = new File(RMTHelper.class.getResource(TWO_THREADS_CONFIG_FILE_PATH).toURI()).getAbsolutePath();
        this.rmHelper.startRM(rmConfig);
        this.resourceManager = this.rmHelper.getResourceManager();
        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              getInfiniteRetryInfrastructureParameters(),
                                              StaticPolicy.class.getName(),
                                              getStaticPolicyParameters(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(NODE_SOURCE_NAME);

        RMTHelper.log("Waiting for the RM to have one free node");
        while (this.resourceManager.getState().getFreeNodesNumber() != ONE_NODE_PER_HOST) {
            Thread.sleep(ACTIVE_WAITING_PERIOD);
        }
        RMTHelper.log("The RM has one free node");

        RMTHelper.log("Killing the free node");
        String freeNodeUrl = this.resourceManager.getState().getFreeNodes().iterator().next();
        RMTHelper.killRuntime(freeNodeUrl);

        RMMonitorsHandler monitor = this.rmHelper.getMonitorsHandler();
        monitor.flushEvents();
        RMTHelper.log("Waiting for the RM to detect the node down");
        RMNodeEvent nodeEvent = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                           freeNodeUrl,
                                                           NODE_STATE_CHANGED_TIMEOUT,
                                                           monitor);

        assertEquals(NodeState.DOWN, nodeEvent.getNodeState());
    }

    @Test
    /*
     * This setup makes LOST nodes on one host and FREE nodes on another host.
     * We use a configuration of two threads for the nodes deployment, and an
     * infrastructure with infinite retry number for deploying. After cycling
     * two redeployment phases thanks to the policy, we expect the situation
     * to be:
     * - one thread being always taken by the infinite retry on the fake host
     * - one thread available to deploy with success/redeploy on the localhost
     * A timeout is thrown if a thread leak prevents the redeployment to happen
     */
    public void testNodeSourceDeploymentThreadsDoNotLeakWithInfiniteRetry() throws Exception {
        String rmConfig = new File(RMTHelper.class.getResource(SIX_THREADS_CONFIG_FILE_PATH).toURI()).getAbsolutePath();
        this.rmHelper.startRM(rmConfig);
        this.resourceManager = this.rmHelper.getResourceManager();
        this.resourceManager.defineNodeSource(NODE_SOURCE_NAME,
                                              SSHInfrastructureV2.class.getName(),
                                              getInfiniteRetryInfrastructureParameters(),
                                              RestartDownNodesPolicy.class.getName(),
                                              getRestartDownNodesPolicyParameters(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(NODE_SOURCE_NAME);

        RMTHelper.log("Waiting for the RM to have one free node");
        while (this.resourceManager.getState().getFreeNodesNumber() != ONE_NODE_PER_HOST) {
            Thread.sleep(ACTIVE_WAITING_PERIOD);
        }

        assertEquals(1, this.resourceManager.getState().getAliveNodes().size());

        for (int i = 0; i < 2; i++) {
            String freeNodeUrl = this.resourceManager.getState().getFreeNodes().iterator().next();
            RMTHelper.killRuntime(freeNodeUrl);
            RMMonitorsHandler monitor = this.rmHelper.getMonitorsHandler();
            monitor.flushEvents();
            RMTHelper.log("Waiting for the RM to detect the down node");
            RMNodeEvent nodeEvent = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                               freeNodeUrl,
                                                               NODE_STATE_CHANGED_TIMEOUT,
                                                               monitor);

            assertEquals(NodeState.DOWN, nodeEvent.getNodeState());

            RMTHelper.log("Waiting for the RM to redeploy the down node");
            int totalWaitingTime = 0;
            while (this.resourceManager.getState().getFreeNodesNumber() != ONE_NODE_PER_HOST) {
                Thread.sleep(ACTIVE_WAITING_PERIOD);
                totalWaitingTime += ACTIVE_WAITING_PERIOD;
                if (totalWaitingTime > NODE_STATE_CHANGED_TIMEOUT) {
                    throw new TestTimedOutException(NODE_STATE_CHANGED_TIMEOUT, TimeUnit.MILLISECONDS);
                }
            }
        }

        assertEquals(1, this.resourceManager.getState().getAliveNodes().size());
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
                              OperatingSystem.getOperatingSystem(), "", "", "", "", "" }; // extra java options and startup script parameters
    }

    private byte[] getHostsFileContent() {
        return (FAKE_HOST_FOR_DEPLOYMENT_FAILURE + " " + ONE_NODE_PER_HOST + "\n" + "localhost " + ONE_NODE_PER_HOST +
                "\n").getBytes();
    }

    private Object[] getStaticPolicyParameters() {
        return new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString() };
    }

    private Object[] getRestartDownNodesPolicyParameters() {
        return new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString(), "2000" };
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
