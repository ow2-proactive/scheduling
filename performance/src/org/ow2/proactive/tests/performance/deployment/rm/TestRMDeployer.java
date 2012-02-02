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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.deployment.rm;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.rm.NodeSourceEventsMonitor;
import org.ow2.proactive.tests.performance.rm.RMTestListener;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestRMDeployer extends TestDeployer {

    public static final String NODE_SOURCE_NAME = "org-ow2-proactive-tests-performance-RM_NODES";

    public static final String CLIENT_CONFIG_FILE_NAME = "RMClientProActiveConfiguration.xml";

    public static final long RM_START_TIMEOUT = 60000;

    public static final long RM_NODE_DEPLOY_TIMEOUT = 2 * 60000;

    private final List<String> rmNodesHosts;

    private final int rmNodesPerHost;

    public static TestRMDeployer createRMDeployerUsingSystemProperties() throws Exception {
        String rmHostName = TestUtils.getRequiredProperty("rm.deploy.rmHost");
        String protocol = TestUtils.getRequiredProperty("test.deploy.protocol");
        String javaPath = TestUtils.getRequiredProperty("test.javaPath");
        String rmPath = TestUtils.getRequiredProperty("test.schedulingPath");

        String[] rmNodesHosts = {};
        String rmNodesHostsString = TestUtils.getRequiredProperty("rm.deploy.rmNodesHosts");
        if (!rmNodesHostsString.trim().isEmpty()) {
            rmNodesHosts = rmNodesHostsString.split(",");
        }

        int nodesPerHost = Integer.valueOf(TestUtils.getRequiredProperty("rm.deploy.rmNodesPerHosts"));
        String[] testNodes = {};
        String testNodesString = TestUtils.getRequiredProperty("rm.deploy.testNodes");
        if (!testNodesString.trim().isEmpty()) {
            testNodes = testNodesString.split(",");
        }

        return TestRMDeployer.createRMDeployer(javaPath, rmPath, rmHostName, protocol, Arrays
                .asList(rmNodesHosts), nodesPerHost, Arrays.asList(testNodes));
    }

    public static TestRMDeployer createRMDeployer(String javaPath, String rmPath, String rmHostName,
            String protocol, List<String> rmNodesHosts, int rmNodesPerHost, List<String> testNodes)
            throws InterruptedException {
        TestRMDeployer deployer = new TestRMDeployer(javaPath, rmPath, rmHostName, protocol, rmNodesHosts,
            rmNodesPerHost, testNodes);
        TestRMDeployHelper deployHelper = new TestRMDeployHelper(javaPath, deployer.getSchedulingFolder(),
            deployer.getServerHost(), protocol);
        deployer.setDeployHelper(deployHelper);
        return deployer;
    }

    private TestRMDeployer(String javaPath, String schedulingPath, String serverHostName, String protocol,
            List<String> rmNodesHosts, int rmNodesPerHost, List<String> testNodes)
            throws InterruptedException {
        super(javaPath, schedulingPath, CLIENT_CONFIG_FILE_NAME, serverHostName);

        this.rmNodesHosts = rmNodesHosts;
        if (rmNodesPerHost <= 0) {
            throw new IllegalArgumentException("Invalid rmNodesPerHost: " + rmNodesPerHost);
        }
        this.rmNodesPerHost = rmNodesPerHost;

        Set<String> allHosts = new LinkedHashSet<String>();
        allHosts.addAll(rmNodesHosts);
        allHosts.addAll(testNodes);
        prepareHostsForTest(allHosts, javaPath, schedulingPath);
    }

    @Override
    protected void waitForServerStartup(String expectedUrl, String clientJavaOptions,
            File clientProActiveConfig) throws Exception {
        Credentials credentials = schedulingFolder.getRMCredentials();

        System.out
                .println("Waiting for the RM on the URL: " + expectedUrl + ", timeout: " + RM_START_TIMEOUT);

        RMAuthentication auth = RMConnection.waitAndJoin(expectedUrl, RM_START_TIMEOUT);
        ResourceManager rm = auth.login(credentials);

        NodeSourceEventsMonitor eventsMonitor = new NodeSourceEventsMonitor(NODE_SOURCE_NAME);
        RMTestListener listener = RMTestListener.createRMTestListener(eventsMonitor);
        RMInitialState state = rm.getMonitoring().addRMEventListener(listener);
        PAFuture.waitFor(state);
        state.getNodeSource().size();

        if (!rmNodesHosts.isEmpty()) {
            if (!createNodeSource(rm, expectedUrl, clientJavaOptions, clientProActiveConfig)) {
                throw new TestExecutionException("Failed to create node source");
            }

            int expectedNodesNumber = rmNodesHosts.size() * rmNodesPerHost;
            System.out.println("Waiting for nodes deployment (nodes: " + expectedNodesNumber + ", timeout: " +
                RM_NODE_DEPLOY_TIMEOUT + ")");
            boolean nodesStarted = eventsMonitor.waitForNodesInitialization(expectedNodesNumber,
                    RM_NODE_DEPLOY_TIMEOUT);
            if (!nodesStarted) {
                throw new TestExecutionException("Failed to deploy nodes");
            }

            int nodesNumber = rm.getState().getFreeNodesNumber();
            if (nodesNumber != expectedNodesNumber) {
                throw new TestExecutionException("Unexpected number of free nodes: " + nodesNumber);
            }
            System.out.println("RM was started, total nodes: " + nodesNumber);

            try {
                rm.getMonitoring().removeRMEventListener();
            } catch (Exception e) {
                throw new TestExecutionException("Failed to remove listener", e);
            }

            BooleanWrapper disconnected = rm.disconnect();
            PAFuture.waitFor(disconnected);
            if (!disconnected.getBooleanValue()) {
                throw new TestExecutionException("Failed to disconnect from RM");
            }
        } else {
            System.out
                    .println("WARNING: RM nodes hosts are not specified, RM is created without node source");
        }
    }

    private boolean createNodeSource(ResourceManager rm, String rmUrl, String javaOptions,
            File clientProActiveConfig) throws Exception {
        StringBuilder hostsListString = new StringBuilder();
        for (String hostName : rmNodesHosts) {
            hostsListString.append(String.format("%s %d\n", hostName, rmNodesPerHost));
        }

        int timeout = 60000;
        int attempt = 1;
        String sshOptions = "";
        String targetOs = "UNIX";

        byte[] creds = schedulingFolder.getRMCredentialsBytes();

        Object[] infrastructureParameters = new Object[] { rmUrl, hostsListString.toString().getBytes(),
                timeout, attempt, sshOptions, javaPath, schedulingFolder.getRootDirPath(), targetOs,
                javaOptions.toString(), creds };

        Object[] policyParameters = new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString() };

        System.out.println(String.format(
                "Creating node source, ssh infrastructure, rmUrl=%s, hostsList=%s, javaOptions: %s", rmUrl,
                hostsListString, javaOptions.toString()));

        BooleanWrapper result = rm.createNodeSource(NODE_SOURCE_NAME, SSHInfrastructure.class.getName(),
                infrastructureParameters, StaticPolicy.class.getName(), policyParameters);
        return result.getBooleanValue();
    }

}
