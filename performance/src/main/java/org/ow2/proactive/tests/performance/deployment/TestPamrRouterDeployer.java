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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestPamrRouterDeployer {

    private final HostTestEnv env;

    private final Integer pamrPort;

    private final List<PamrReservedId> reservedPamrIds;

    private final String[] pamrArgs;

    public static TestPamrRouterDeployer createPamrRouterDeployerUsingSystemProperties() throws Exception {
        TestEnv env = TestEnv.getEnvUsingSystemProperties("pamr");
        String hostName = TestUtils.getRequiredProperty("test.deploy.pamr.startNewRouter.host");
        String reservedIds = TestUtils.getRequiredProperty("test.deploy.pamr.startNewRouter.reservedIds");
        Integer port = null;
        String portProperty = System.getProperty("test.deploy.pamr.startNewRouter.port");
        if (portProperty != null && !portProperty.trim().isEmpty()) {
            port = Integer.valueOf(portProperty);
        }
        String pamrArgsValues = System.getProperty("test.deploy.pamr.startNewRouter.args");
        String[] pamrArgs;
        if (pamrArgsValues != null) {
            pamrArgs = pamrArgsValues.split(" ");
        } else {
            pamrArgs = new String[0];
        }
        return new TestPamrRouterDeployer(env, hostName, port, reservedIds, pamrArgs);
    }

    public TestPamrRouterDeployer(TestEnv env, String hostName, Integer port, String reservedIds,
            String[] pamrArgs) throws Exception {
        this.env = new HostTestEnv(hostName, env);

        reservedPamrIds = new ArrayList<PamrReservedId>();
        if (!reservedIds.isEmpty()) {
            String[] ids = reservedIds.split(",");
            for (String id : ids) {
                String splitted[] = id.split(":");
                if (splitted.length != 2 || splitted[0].isEmpty() || splitted[1].isEmpty()) {
                    throw new TestExecutionException("Invalid format of reservedIds: " + reservedIds);
                }
                reservedPamrIds.add(new PamrReservedId(splitted[0], splitted[1]));
            }
        } else {
            throw new TestExecutionException("Pamr reservedIds are not specified");
        }

        if (port == null) {
            pamrPort = DeploymentTestUtils.findFreePort(this.env);
        } else {
            pamrPort = port;
        }

        this.pamrArgs = pamrArgs;
    }

    public void startRouter() throws Exception {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        for (PamrReservedId id : reservedPamrIds) {
            properties.put(id.getId(), id.getCookie());
        }
        String pamrConfigPath = createPamrConfig(properties);

        List<String> command = new ArrayList<String>();
        command.add(env.getEnv().getJavaPath());
        command.add("-D" + TestDeployer.TEST_JVM_OPTION);
        command.add(PAResourceManagerProperties.RM_HOME.getCmdLine() +
            env.getEnv().getSchedulingFolder().getRootDirPath());
        command.add(CentralPAPropertyRepository.LOG4J.getCmdLine() +
            "file:" +
            TestDeployer.getFileName(env.getEnv().getSchedulingFolder().getTestConfigDir(),
                    "/log4j/log4j-pamr-router"));
        command.add("-cp");
        command.add(env.getEnv().getSchedulingFolder().getRootDirPath() + "/dist/lib/ProActive.jar" +
            File.pathSeparator + env.getEnv().getSchedulingFolder().getRootDirPath() +
            "/dist/lib/ProActive_utils.jar");
        command.add("org.objectweb.proactive.extensions.pamr.router.Main");
        command.add("--port");
        command.add(String.valueOf(pamrPort));
        command.add("--configFile");
        command.add(pamrConfigPath);
        for (String pamrArg : pamrArgs) {
            command.add(pamrArg);
        }

        System.out.println("Starting PAMR router on the " + env.getHost() + ", command: " + command);

        ProcessExecutor pamrProcess = env.runCommandPrintOutput("PAMR router", command);
        pamrProcess.start();

        Thread.sleep(5000);
        if (pamrProcess.isProcessFinished()) {
            throw new TestExecutionException("Failed to start PAMR router");
        }
    }

    public InetAddress getPamrHost() {
        return env.getHost();
    }

    public Integer getPamrPort() {
        return pamrPort;
    }

    private String createPamrConfig(Map<String, String> properties) throws Exception {
        TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();

        File localConfigFile = new File(localEnv.getSchedulingFolder().getTestTmpDir(), "/pamr.router.config");
        PrintWriter writer = new PrintWriter(localConfigFile);
        try {
            writer.println("configuration=performance_test");
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                writer.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        } finally {
            writer.close();
        }

        return env.copyFileFromLocalEnv(localEnv, localConfigFile);
    }

}
