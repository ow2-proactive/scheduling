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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.tests.performance.deployment.process.ProcessExecutor;
import org.ow2.proactive.tests.performance.deployment.rm.TestRMDeployHelper;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;


public abstract class TestDeployer {

    protected final HostTestEnv serverHostEnv;

    protected final String clientConfigFileName;

    protected final TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();

    protected TestDeployHelper deployHelper;

    private String serverUrl;

    public TestDeployer(HostTestEnv serverHostEnv, String clientConfigFileName) throws InterruptedException {
        this.serverHostEnv = serverHostEnv;
        this.clientConfigFileName = clientConfigFileName;
    }

    public HostTestEnv getServerHostEnv() {
        return serverHostEnv;
    }

    public String getClientConfigFileName() {
        return clientConfigFileName;
    }

    public void setDeployHelper(TestDeployHelper deployHelper) {
        this.deployHelper = deployHelper;
    }

    protected ProcessExecutor runStartServerCommand() throws Exception {
        List<String> startCommand = deployHelper.createServerStartCommand();
        System.out.println("Starting server process on the " + serverHostEnv.getHost().getHostName() + ": " +
            startCommand);
        ProcessExecutor executor = serverHostEnv.runCommandPrintOutput("Server", startCommand);
        executor.start();

        return executor;
    }

    protected abstract void waitForServerStartup(String expectedUrl) throws Exception;

    protected String javaPropertiesAsSingleString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> prop : map.entrySet()) {
            stringBuilder.append("-D").append(prop.getKey()).append("=").append(prop.getValue()).append(" ");
        }
        return stringBuilder.toString();
    }

    public final Map<String, String> startServer() {
        ProcessExecutor executor;
        Map<String, String> clientProperties;

        try {
            serverUrl = deployHelper.prepareForDeployment();

            System.out.println("Setting java properties:");
            clientProperties = deployHelper.getClientJavaProperties(localEnv);
            for (Map.Entry<String, String> prop : clientProperties.entrySet()) {
                System.out.println(String.format("%s=%s", prop.getKey(), prop.getValue()));
                System.setProperty(prop.getKey(), prop.getValue());
            }

            File clientProActiveConfig = new File(localEnv.getSchedulingFolder().getTestTmpDir(),
                clientConfigFileName);
            String xmlConfiguration = DeploymentTestUtils.createProActiveConfiguration(clientProperties);
            TestFileUtils.writeStringToFile(clientProActiveConfig, xmlConfiguration);
            System.out.println("Created client configuration: " + clientProActiveConfig.getAbsolutePath());
            System.out.println(xmlConfiguration);

            executor = runStartServerCommand();
        } catch (TestExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new TestExecutionException("Failed to start server process", e);
        }

        Throwable error = null;
        try {
            waitForServerStartup(serverUrl);

            System.out.println("Server started, url: " + serverUrl);

            Map<String, String> result = new HashMap<String, String>();
            result.put("deploy.result.serverUrl", serverUrl);
            result.put("deploy.result.clientJavaOptions", javaPropertiesAsSingleString(clientProperties));
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
                killTestProcesses(serverHostEnv.getHost());
            }
        }

    }

    public String getServerUrl() {
        return serverUrl;
    }

    protected static boolean killTestProcesses(InetAddress hostAddr) {
        try {
            return DeploymentTestUtils.killProcessesUsingPgrep(hostAddr, TestRMDeployHelper.TEST_JVM_OPTION);
        } catch (InterruptedException e) {
            throw new TestExecutionException("Main test execution thread was interrupted", e);
        }
    }

    public static String getFileName(File parent, String path) {
        File file = new File(parent, path);
        return file.getAbsolutePath();
    }

}
