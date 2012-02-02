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
package org.ow2.proactive.tests.performance.deployment;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.tests.performance.deployment.process.SSHProcessExecutor;
import org.ow2.proactive.tests.performance.deployment.rm.TestRMDeployHelper;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;


public abstract class TestDeployer {

    protected final String javaPath;

    protected final SchedulingFolder schedulingFolder;

    protected final InetAddress serverHost;

    protected final String clientConfigFileName;

    protected TestDeployHelper deployHelper;

    private String serverUrl;

    public TestDeployer(String javaPath, String schedulingPath, String clientConfigFileName,
            String serverHostName) throws InterruptedException {
        this.javaPath = javaPath;
        this.schedulingFolder = new SchedulingFolder(schedulingPath);
        this.clientConfigFileName = clientConfigFileName;
        this.serverHost = prepareHostsForTest(Collections.singleton(serverHostName), javaPath, schedulingPath)
                .get(0);
    }

    public String getJavaPath() {
        return javaPath;
    }

    public SchedulingFolder getSchedulingFolder() {
        return schedulingFolder;
    }

    public InetAddress getServerHost() {
        return serverHost;
    }

    public String getClientConfigFileName() {
        return clientConfigFileName;
    }

    public void setDeployHelper(TestDeployHelper deployHelper) {
        this.deployHelper = deployHelper;
    }

    protected SSHProcessExecutor startServer(InetAddress host) throws Exception {
        List<String> startCommand = deployHelper.createServerStartCommand();
        System.out.println("Starting server process on the " + host + ": " + startCommand);
        SSHProcessExecutor executor = SSHProcessExecutor.createExecutorPrintOutput("Server", host,
                startCommand.toArray(new String[startCommand.size()]));
        executor.start();

        return executor;
    }

    protected abstract void waitForServerStartup(String expectedUrl, String clientJavaOptions,
            File clientProActiveConfig) throws Exception;

    public final Map<String, String> startServer() {
        SSHProcessExecutor executor;
        File clientProActiveConfig;

        try {
            serverUrl = deployHelper.prepareForDeployment();

            Map<String, String> clientJavaOptionsMap = deployHelper.getClientProActiveProperties();
            String xmlConfiguration = DeploymentTestUtils.createProActiveConfiguration(clientJavaOptionsMap);
            clientProActiveConfig = new File(schedulingFolder.getTestTmpDir(), clientConfigFileName);
            TestFileUtils.writeStringToFile(clientProActiveConfig, xmlConfiguration);
            System.out.println("Created client configuration: " + clientProActiveConfig.getAbsolutePath());
            System.out.println(xmlConfiguration);

            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                    clientProActiveConfig.getAbsolutePath());

            executor = startServer(serverHost);
        } catch (TestExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new TestExecutionException("Failed to start server process", e);
        }

        Throwable error = null;
        try {
            StringBuilder javaOptionsBuilder = new StringBuilder();
            javaOptionsBuilder.append(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
                clientProActiveConfig.getAbsolutePath());
            for (String option : deployHelper.getClientJavaOptions()) {
                javaOptionsBuilder.append(" " + option);
            }
            String clientJavaOptions = javaOptionsBuilder.toString();

            waitForServerStartup(serverUrl, clientJavaOptions, clientProActiveConfig);

            System.out.println("Server started, url: " + serverUrl);

            Map<String, String> result = new HashMap<String, String>();
            result.put("deploy.result.serverUrl", serverUrl);
            result.put("deploy.result.clientJavaOptions", clientJavaOptions);
            return result;
        } catch (Throwable t) {
            error = t;
            return null;
        } finally {
            executor.killProcess();

            if (error != null) {
                System.out.println("Error during server deployment: " + error);
                error.printStackTrace(System.out);

                System.out.println("Trying to kill server processes");
                killTestProcesses(serverHost);
            }
        }

    }

    public String getServerUrl() {
        return serverUrl;
    }

    public static List<InetAddress> prepareHostsForTest(Collection<String> hostNames, String javaPath,
            String rmPath) throws InterruptedException {
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

    protected static boolean killTestProcesses(InetAddress hostAddr) {
        try {
            return DeploymentTestUtils.killProcessesUsingPgrep(hostAddr, TestRMDeployHelper.TEST_JVM_OPTION);
        } catch (InterruptedException e) {
            throw new TestExecutionException("Main test execution thread was interrupted", e);
        }
    }

    protected static InetAddress prepareHostForTest(String hostName, String javaPath, String schedulerPath)
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
        // killTestProcesses(hostAddr);

        List<String> javaProcesses = DeploymentTestUtils.listProcesses(hostAddr, "java");
        if (!javaProcesses.isEmpty()) {
            System.out.println("WARNING: there are java processes on the host " + hostAddr + ":");
            for (String process : javaProcesses) {
                System.out.println(process);
            }
        }

        return hostAddr;
    }

    public static String getFileName(File parent, String path) {
        File file = new File(parent, path);
        if (!file.exists()) {
            throw new TestExecutionException("Failed to find file: " + file.getAbsolutePath());
        }
        return file.getAbsolutePath();
    }

}
