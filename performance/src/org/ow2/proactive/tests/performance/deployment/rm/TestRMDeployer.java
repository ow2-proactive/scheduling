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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.utils.RMStarter;
import org.ow2.proactive.tests.performance.deployment.HostTestEnv;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestEnv;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.rm.NodesDeployWaitCondition;
import org.ow2.proactive.tests.performance.rm.RMEventsMonitor;
import org.ow2.proactive.tests.performance.rm.RMTestListener;
import org.ow2.proactive.tests.performance.rm.RMWaitCondition;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestRMDeployer extends TestDeployer {

    public static final String NODE_SOURCE_NAME = "org-ow2-proactive-tests-performance-RM_NODES";

    public static final String CLIENT_CONFIG_FILE_NAME = "RMClientProActiveConfiguration.xml";

    public static final long RM_START_TIMEOUT = 5 * 60000;

    public static final long RM_NODE_DEPLOY_TIMEOUT = 5 * 60000;

    private final List<String> rmNodesHosts;

    private final int rmNodesPerHost;

    private final int nodeSourcesNumber;

    private final TestEnv nodesTestEnv;

    public static TestRMDeployer createRMDeployerUsingSystemProperties() throws Exception {
        String rmHostName = TestUtils.getRequiredProperty("rm.deploy.rmHost").trim();
        HostTestEnv serverHostEnv = new HostTestEnv(rmHostName, TestEnv.getEnvUsingSystemProperties("rm"));

        String protocol = TestUtils.getRequiredProperty("test.deploy.protocol");

        String[] rmNodesHosts = {};
        String rmNodesHostsString = TestUtils.getRequiredProperty("rm.deploy.rmNodesHosts");
        if (!rmNodesHostsString.trim().isEmpty()) {
            rmNodesHosts = rmNodesHostsString.split(",");
            for (int i = 0; i < rmNodesHosts.length; i++) {
                rmNodesHosts[i] = rmNodesHosts[i].trim();
            }
        }

        int nodesPerHost = Integer.valueOf(TestUtils.getRequiredProperty("rm.deploy.rmNodesPerHosts"));
        String[] testNodes = {};
        String testNodesString = TestUtils.getRequiredProperty("rm.deploy.testNodes");
        if (!testNodesString.trim().isEmpty()) {
            testNodes = testNodesString.split(",");
            for (int i = 0; i < testNodes.length; i++) {
                testNodes[i] = testNodes[i].trim();
            }
        }

        int nodeSourcesNumber = Integer.parseInt(System.getProperty("rm.deploy.rmNodesSourcesNumber", "1"));

        TestEnv nodesTestEnv = TestEnv.getEnvUsingSystemProperties("rmNodes");

        return TestRMDeployer.createRMDeployer(serverHostEnv, protocol, nodesTestEnv, Arrays
                .asList(rmNodesHosts), nodesPerHost, nodeSourcesNumber, Arrays.asList(testNodes));
    }

    public static TestRMDeployer createRMDeployer(HostTestEnv serverHostEnv, String protocol,
            TestEnv nodesTestEnv, List<String> rmNodesHosts, int rmNodesPerHost, int nodeSourcesNumber,
            List<String> testNodes) throws Exception {
        TestRMDeployer deployer = new TestRMDeployer(serverHostEnv, protocol, nodesTestEnv, rmNodesHosts,
            rmNodesPerHost, nodeSourcesNumber, testNodes);
        return deployer;
    }

    private TestRMDeployer(HostTestEnv serverHostEnv, String protocol, TestEnv nodesTestEnv,
            List<String> rmNodesHosts, int rmNodesPerHost, int nodeSourcesNumber, List<String> testNodes)
            throws Exception {
        super(serverHostEnv, CLIENT_CONFIG_FILE_NAME, protocol);

        this.nodesTestEnv = nodesTestEnv;

        this.rmNodesHosts = rmNodesHosts;
        if (rmNodesPerHost <= 0) {
            throw new IllegalArgumentException("Invalid rmNodesPerHost: " + rmNodesPerHost);
        }
        this.rmNodesPerHost = rmNodesPerHost;

        if (nodeSourcesNumber > rmNodesHosts.size()) {
            System.out
                    .println(String
                            .format(
                                    "WARNING: requested number of node sources(%d) is bigger than number of available hosts(%d)",
                                    nodeSourcesNumber, rmNodesHosts.size()));
            this.nodeSourcesNumber = rmNodesHosts.size();
        } else {
            this.nodeSourcesNumber = nodeSourcesNumber;
        }

        Set<String> allHosts = new LinkedHashSet<String>();
        allHosts.addAll(rmNodesHosts);
        allHosts.addAll(testNodes);
        for (String nodeHost : testNodes) {
            nodesTestEnv.validateEnv(nodeHost);
        }
    }

    @Override
    protected void waitForServerStartup(String expectedUrl) throws Exception {
        Credentials credentials = localEnv.getSchedulingFolder().getRMCredentials();

        System.out
                .println("Waiting for the RM on the URL: " + expectedUrl + ", timeout: " + RM_START_TIMEOUT);

        RMAuthentication auth = RMConnection.waitAndJoin(expectedUrl, RM_START_TIMEOUT);
        ResourceManager rm = auth.login(credentials);

        RMEventsMonitor eventsMonitor = new RMEventsMonitor();
        RMTestListener listener = RMTestListener.createRMTestListener(eventsMonitor);
        RMInitialState state = rm.getMonitoring().addRMEventListener(listener);
        PAFuture.waitFor(state);
        state.getNodeSource().size();

        if (!rmNodesHosts.isEmpty()) {
            int expectedNodesNumber = rmNodesHosts.size() * rmNodesPerHost;

            String clientJavaOptions = javaPropertiesAsSingleString(getClientJavaProperties(nodesTestEnv));

            if (!createNodeSource(rm, eventsMonitor, expectedUrl, clientJavaOptions)) {
                throw new TestExecutionException("Failed to create node source");
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

    private boolean createNodeSource(ResourceManager rm, RMEventsMonitor eventsMonitor, String rmUrl,
            String javaOptions) throws Exception {

        int hostsPerNodeSource = rmNodesHosts.size() / nodeSourcesNumber;

        for (int i = 0; i < nodeSourcesNumber; i++) {
            int start = i * hostsPerNodeSource;
            int end = start + hostsPerNodeSource;
            if (i == nodeSourcesNumber - 1) {
                end = rmNodesHosts.size();
            }
            List<String> nodeSourceHosts = rmNodesHosts.subList(start, end);

            String nodeSourceName = NODE_SOURCE_NAME + "-" + i;

            int expectedNodesNumber = nodeSourceHosts.size() * rmNodesPerHost;
            RMWaitCondition waitCondition = eventsMonitor.addWaitCondition(new NodesDeployWaitCondition(
                nodeSourceName, expectedNodesNumber));

            System.out.println("Creating node source '" + nodeSourceName + "' using " +
                nodeSourceHosts.size() + " hosts (" + nodeSourceHosts + ")");
            if (!createNodeSource(rm, rmUrl, javaOptions, nodeSourceHosts, nodeSourceName)) {
                return false;
            }

            System.out.println("Waiting for nodes deployment (nodes: " + expectedNodesNumber + ", timeout: " +
                RM_NODE_DEPLOY_TIMEOUT + ")");
            boolean nodesStarted = eventsMonitor.waitFor(waitCondition, RM_NODE_DEPLOY_TIMEOUT, null);
            if (!nodesStarted) {
                throw new TestExecutionException("Failed to deploy nodes");
            }
        }

        return true;
    }

    private boolean createNodeSource(ResourceManager rm, String rmUrl, String javaOptions,
            List<String> hosts, String nodeSourceName) throws Exception {
        StringBuilder hostsListString = new StringBuilder();
        for (String hostName : hosts) {
            hostsListString.append(String.format("%s %d\n", hostName, rmNodesPerHost));
        }

        int timeout = 60000;
        int attempt = 1;
        String sshOptions = "";
        String targetOs = "UNIX";

        byte[] creds = serverHostEnv.getEnv().getSchedulingFolder().getRMCredentialsBytes();

        Object[] infrastructureParameters = new Object[] { rmUrl, hostsListString.toString().getBytes(),
                timeout, attempt, sshOptions, nodesTestEnv.getJavaPath(),
                nodesTestEnv.getSchedulingFolder().getRootDirPath(), targetOs, javaOptions.toString(), creds };

        Object[] policyParameters = new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString() };

        System.out.println(String.format(
                "Creating node source, ssh infrastructure, rmUrl=%s, hostsList=%s, javaOptions: %s", rmUrl,
                hostsListString, javaOptions.toString()));

        BooleanWrapper result = rm.createNodeSource(nodeSourceName, SSHInfrastructure.class.getName(),
                infrastructureParameters, StaticPolicy.class.getName(), policyParameters);
        return result.getBooleanValue();
    }

    @Override
    protected String getPamrServedReservedId() {
        return TestUtils.getRequiredProperty("rm.deploy.pamr.serverReservedId");
    }

    @Override
    public List<String> createServerStartCommand() {
        String rmHibernateConfig = createTestHibernateConfig();

        SchedulingFolder schedulingFolder = serverHostEnv.getEnv().getSchedulingFolder();

        List<String> result = new ArrayList<String>();
        result.add(serverHostEnv.getEnv().getJavaPath());

        List<String> javaOpts = protocolHelper.getAdditionalServerJavaOptions();
        if (javaOpts != null) {
            result.addAll(javaOpts);
        }

        String rmJavaOpts = System.getProperty("rm.deploy.javaOpts");
        if (rmJavaOpts != null && !rmJavaOpts.isEmpty()) {
            for (String opt : rmJavaOpts.split(" ")) {
                result.add(opt);
            }
        }

        result.add("-D" + TEST_JVM_OPTION);

        result.add("-Djava.security.manager");

        String dropDB = System.getProperty("rm.deploy.dropDB", "false");
        result.add(PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getCmdLine() + dropDB);
        result.add(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getCmdLine() + rmHibernateConfig);

        File rootDir = schedulingFolder.getRootDir();
        result.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            TestDeployer.getFileName(rootDir, "/config/security.java.policy-server"));

        result.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        result.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + schedulingFolder.getRootDirPath());

        result.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "EmptyProActiveConfiguration.xml"));

        result.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-rm-server"));

        result.add("-cp");
        result.add(buildSchedulingClasspath());
        result.add(RMStarter.class.getName());

        return result;
    }

    @Override
    public Map<String, String> getClientJavaProperties(TestEnv env) {
        Map<String, String> result = super.getClientJavaProperties(env);
        result.put(PAResourceManagerProperties.RM_HOME.getKey(), env.getSchedulingFolder().getRootDirPath());
        return result;
    }

    private String createTestHibernateConfig() {
        try {
            TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();
            File localFile = new File(localEnv.getSchedulingFolder().getTestTmpDir(), "rm.hibernate.cfg.xml");

            String hibernateCfg = TestFileUtils.readStreamToString(new FileInputStream(new File(localEnv
                    .getSchedulingFolder().getTestConfigDir(), "rm/rm.hibernate.cfg.xml")));
            hibernateCfg = hibernateCfg.replace("@RM_DB_DIR", serverHostEnv.getEnv().getSchedulingFolder()
                    .getTestTmpDir() +
                "/RM_DB");
            TestFileUtils.writeStringToFile(localFile, hibernateCfg);

            return serverHostEnv.copyFileFromLocalEnv(localEnv, localFile);
        } catch (Exception e) {
            throw new TestExecutionException("Failed to prepare test configuration", e);
        }
    }

}
