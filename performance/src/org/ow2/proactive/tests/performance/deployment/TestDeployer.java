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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public abstract class TestDeployer {

    public static final String TEST_JVM_OPTION_NAME = "org.ow2.proactive.tests.performance";

    public static final String TEST_JVM_OPTION = TEST_JVM_OPTION_NAME + "=true";

    protected final HostTestEnv serverHostEnv;

    protected final String clientConfigFileName;

    protected final TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();

    private String serverUrl;

    protected final TestProtocolHelper protocolHelper;

    static final String[] requiredJARs = { "gson-2.1.jar", "jruby.jar", "jython-2.5.4-rc1.jar",
            "groovy-all-2.1.5.jar", "commons-logging-1.1.1.jar", "ProActive_Scheduler-core.jar",
            "ProActive_SRM-common.jar", "ProActive_ResourceManager.jar", "ProActive_Scheduler-worker.jar",
            "ProActive_Scheduler-mapreduce.jar", "commons-httpclient-3.1.jar", "commons-codec-1.3.jar",
            "ProActive.jar" };

    public TestDeployer(HostTestEnv serverHostEnv, String clientConfigFileName, String protocol)
            throws InterruptedException {
        this.serverHostEnv = serverHostEnv;
        this.clientConfigFileName = clientConfigFileName;
        this.protocolHelper = createProtocolHelper(protocol, serverHostEnv);
    }

    public HostTestEnv getServerHostEnv() {
        return serverHostEnv;
    }

    public String getClientConfigFileName() {
        return clientConfigFileName;
    }

    protected ProcessExecutor runStartServerCommand() throws Exception {
        List<String> startCommand = createServerStartCommand();
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
            serverUrl = prepareForDeployment();

            System.out.println("Setting java properties:");
            clientProperties = getClientJavaProperties(localEnv);
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
            if (executor.isRunningRemotely()) {
                executor.killProcess();
            }

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
            return DeploymentTestUtils.killProcessesUsingPgrep(hostAddr, TEST_JVM_OPTION);
        } catch (InterruptedException e) {
            throw new TestExecutionException("Main test execution thread was interrupted", e);
        }
    }

    public static String getFileName(File parent, String path) {
        File file = new File(parent, path);
        return file.getAbsolutePath();
    }

    private TestProtocolHelper createProtocolHelper(String protocol, HostTestEnv serverHostEnv)
            throws InterruptedException {
        if (protocol.equalsIgnoreCase("multi")) {

            String baseProtocolProperty = TestUtils.getRequiredProperty("test.deploy.multiprotocol.protocol");
            String additionalProtocolsProperty = TestUtils
                    .getRequiredProperty("test.deploy.multiprotocol.additional_protocols");
            String protocolsOrderProperty = TestUtils
                    .getRequiredProperty("test.deploy.multiprotocol.protocols_order");

            for (String protocolValue : protocolsOrderProperty.split(",")) {
                String trimmed = protocolValue.trim();
                if (!trimmed.equals("rmi") && !trimmed.equals("pnp") && !trimmed.equals("pamr")) {
                    throw new IllegalArgumentException(
                        "Invalid protocol in the test.deploy.multiprotocol.protocols_order: " + protocolValue);
                }
            }

            TestProtocolHelper baseProtocol = createBasicProtocolHelper(baseProtocolProperty, serverHostEnv);

            List<TestProtocolHelper> additionalProtocols = new ArrayList<TestProtocolHelper>();
            for (String additionalProtocol : additionalProtocolsProperty.split(",")) {
                additionalProtocols.add(createBasicProtocolHelper(additionalProtocol.trim(), serverHostEnv));
            }
            if (additionalProtocols.isEmpty()) {
                throw new IllegalArgumentException(
                    "Additional protocols are not specified for multi-protocol");
            }

            return new TestMultiprotocolHelper(serverHostEnv, baseProtocol, additionalProtocols,
                protocolsOrderProperty);
        } else {
            return createBasicProtocolHelper(protocol, serverHostEnv);
        }
    }

    private TestProtocolHelper createBasicProtocolHelper(String protocol, HostTestEnv serverHostEnv) {
        if (protocol.equalsIgnoreCase("pnp")) {
            return new TestPnpProtocolHelper(serverHostEnv);
        } else if (protocol.equalsIgnoreCase("pamr")) {
            return new TestPamrProtocolHelper(serverHostEnv, getPamrServedReservedId());
        } else if (protocol.equalsIgnoreCase("rmi")) {
            return new TestRMIProtocolHelper(serverHostEnv);
        } else if (protocol.equals("amqp")) {
            return TestAmqpProtocolHelper.createUsingSystemProperties(serverHostEnv);
        } else {
            throw new IllegalArgumentException("Test doesn't support protocol " + protocol);
        }
    }

    protected abstract String getPamrServedReservedId();

    public abstract List<String> createServerStartCommand();

    public String prepareForDeployment() throws Exception {
        return protocolHelper.prepareForDeployment();
    }

    public Map<String, String> getClientJavaProperties(TestEnv env) {
        Map<String, String> properties = new LinkedHashMap<String, String>();

        properties.put(CentralPAPropertyRepository.PA_HOME.getName(), env.getSchedulingFolder()
                .getRootDirPath());
        properties.put(CentralPAPropertyRepository.LOG4J.getName(), "file:" +
            new File(env.getSchedulingFolder().getTestConfigDir(), "/log4j/log4j-client").getAbsolutePath());

        Map<String, String> protocolSpecificOptions = protocolHelper.getClientProActiveProperties();
        properties.putAll(protocolSpecificOptions);

        properties.put(TEST_JVM_OPTION_NAME, "true");

        return properties;
    }

    protected String buildSchedulingClasspath() {
        TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();

        List<String> distLibJars = new ArrayList<String>();
        for (String jar : requiredJARs) {
            distLibJars.add(localEnv.getSchedulingFolder().getRootDir() + "/dist/lib/" + jar);
        }
        List<String> allJars = new ArrayList<String>(distLibJars);

        File addons = new File(localEnv.getSchedulingFolder().getRootDir(), "/addons");

        if (addons.isDirectory()) {
            List<String> addonsJars = TestFileUtils.listDirectoryJars(addons.getAbsolutePath());
            allJars.addAll(addonsJars);
        }

        StringBuilder result = new StringBuilder();
        for (String jar : allJars) {
            jar = localEnv.convertFileNameForEnv(jar, serverHostEnv.getEnv());
            result.append(jar).append(File.pathSeparatorChar);
        }

        return result.toString();
    }

}
