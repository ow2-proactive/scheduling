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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.tests.performance.deployment.DeploymentTestUtils;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.deployment.process.SSHProcessExecutor;
import org.ow2.proactive.tests.performance.rm.NodeSourceEventsMonitor;
import org.ow2.proactive.tests.performance.rm.RMTestListener;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;


public class TestRMDeployer {

    public static final String NODE_SOURCE_NAME = "org-ow2-proactive-tests-performance-RM_NODES";

    public static final String CLIENT_CONFIG_FILE_NAME = "ClientProActiveConfiguration.xml";

    private final InetAddress rmHost;

    private final List<String> rmNodesHosts;

    private final int rmNodesPerHost;

    private final String javaPath;

    private final SchedulingFolder schedulingFolder;

    private final TestRMDeployHelper rmDeployHelper;

    public TestRMDeployer(String javaPath, String rmPath, String rmHostName, String protocol,
            List<String> rmNodesHosts, int rmNodesPerHost, List<String> testNodes)
            throws InterruptedException {
        this.schedulingFolder = new SchedulingFolder(rmPath);
        this.javaPath = javaPath;
        this.rmNodesHosts = rmNodesHosts;
        if (rmNodesPerHost <= 0) {
            throw new IllegalArgumentException("Invalid rmNodesPerHost: " + rmNodesPerHost);
        }
        this.rmNodesPerHost = rmNodesPerHost;

        Set<String> allHosts = new LinkedHashSet<String>();
        allHosts.add(rmHostName);
        allHosts.addAll(rmNodesHosts);
        allHosts.addAll(testNodes);

        rmHost = prepareHostsForTest(allHosts, javaPath, rmPath).get(0);

        rmDeployHelper = TestRMDeployHelper
                .createRMDeployHelper(protocol, schedulingFolder, rmHost, javaPath);
    }

    public Map<String, String> startRM() {
        Credentials credentials = schedulingFolder.getRMCredentials();
        SSHProcessExecutor rmExecutor;
        String rmUrl;
        File clientProActiveConfig;
        String clientJavaOptions;
        try {
            rmUrl = rmDeployHelper.prepareForDeployment();

            Map<String, String> clientJavaOptionsMap = rmDeployHelper.getClientProActiveProperties();
            String xmlConfiguration = DeploymentTestUtils.createProActiveConfiguration(clientJavaOptionsMap);
            clientProActiveConfig = new File(schedulingFolder.getTestTmpDir(), CLIENT_CONFIG_FILE_NAME);
            TestFileUtils.writeStringToFile(clientProActiveConfig, xmlConfiguration);
            System.out.println("Created client configuration: " + clientProActiveConfig.getAbsolutePath());
            System.out.println(xmlConfiguration);

            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                    clientProActiveConfig.getAbsolutePath());

            StringBuilder javaOptionsBuilder = new StringBuilder();
            javaOptionsBuilder.append(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
                clientProActiveConfig.getAbsolutePath());
            for (String option : rmDeployHelper.getClientJavaOptions()) {
                javaOptionsBuilder.append(" " + option);
            }
            clientJavaOptions = javaOptionsBuilder.toString();

            List<String> rmStartCommand = rmDeployHelper.createRMStartCommand();
            System.out.println("Starting RM process on the " + rmHost + ": " + rmStartCommand);
            rmExecutor = SSHProcessExecutor.createExecutorPrintOutput("RM", rmHost, rmStartCommand
                    .toArray(new String[rmStartCommand.size()]));
            rmExecutor.start();
        } catch (Exception e) {
            throw new TestExecutionException("Failed to start RM process", e);
        }

        Throwable error = null;
        try {
            System.out.println("Waiting for the RM on the URL: " + rmUrl);

            RMAuthentication auth = RMConnection.waitAndJoin(rmUrl, 30000);
            ResourceManager rm = auth.login(credentials);

            NodeSourceEventsMonitor eventsMonitor = new NodeSourceEventsMonitor(NODE_SOURCE_NAME);
            RMTestListener listener = RMTestListener.createRMTestListener(eventsMonitor);
            RMInitialState state = rm.getMonitoring().addRMEventListener(listener);
            PAFuture.waitFor(state);
            state.getNodeSource().size();

            if (!rmNodesHosts.isEmpty()) {
                if (!createNodeSource(rm, rmUrl, clientJavaOptions, clientProActiveConfig)) {
                    throw new TestExecutionException("Failed to create node source");
                }

                int expectedNodesNumber = rmNodesHosts.size() * rmNodesPerHost;
                long timeout = 60000;
                System.out.println("Waiting for nodes deployment (nodes: " + expectedNodesNumber +
                    ", timeout: " + timeout + ")");
                boolean nodesStarted = eventsMonitor.waitForNodesInitialization(expectedNodesNumber, timeout);
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

            Map<String, String> result = new HashMap<String, String>();
            result.put("rm.deploy.result.rmUrl", rmUrl);
            result.put("rm.deploy.result.clientJavaOptions", clientJavaOptions);
            return result;
        } catch (Throwable t) {
            error = t;
            return null;
        } finally {
            rmExecutor.killProcess();

            if (error != null) {
                System.out.println("Error during RM deployment: " + error);
                error.printStackTrace(System.out);

                System.out.println("Trying to kill all test processes");
                killTestProcesses(rmHost);
            }
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

    static List<InetAddress> prepareHostsForTest(Collection<String> hostNames, String javaPath, String rmPath)
            throws InterruptedException {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        for (String hostName : hostNames) {
            System.out.println("Before-execution checks for " + hostName);
            InetAddress hostAddr = prepareHostForTest(hostName, javaPath, rmPath);
            if (hostAddr == null) {
                throw new TestExecutionException("Before-execution checks failed for " + hostName +
                    ", see log for more details");
            } else {
                addresses.add(hostAddr);
            }
        }
        return addresses;
    }

    private static boolean killTestProcesses(InetAddress hostAddr) {
        try {
            return DeploymentTestUtils.killProcessesUsingPgrep(hostAddr, TestRMDeployHelper.TEST_JVM_OPTION);
        } catch (InterruptedException e) {
            throw new TestExecutionException("Main test execution thread was interrupted", e);
        }
    }

    private static InetAddress prepareHostForTest(String hostName, String javaPath, String schedulerPath)
            throws InterruptedException {
        InetAddress hostAddr = DeploymentTestUtils.checkHostIsAvailable(hostName);
        if (hostAddr == null) {
            return null;
        }

        // System.out.println("Checking scheduler path is available for the host " + hostAddr);
        if (!DeploymentTestUtils.checkPathIsAvailable(hostAddr, schedulerPath)) {
            return null;
        }

        // System.out.println("Checking java for host " + hostAddr);
        if (!DeploymentTestUtils.checkJavaIsAvailable(hostAddr, javaPath)) {
            return null;
        }

        // System.out.println("Trying to find and kill existing test processes on the host " + hostAddr);
        killTestProcesses(hostAddr);

        List<String> javaProcesses = DeploymentTestUtils.listProcesses(hostAddr, "java");
        if (!javaProcesses.isEmpty()) {
            System.out.println("WARNING: there are java processes on the host " + hostAddr + ":");
            for (String process : javaProcesses) {
                System.out.println(process);
            }
        }

        return hostAddr;
    }

}
