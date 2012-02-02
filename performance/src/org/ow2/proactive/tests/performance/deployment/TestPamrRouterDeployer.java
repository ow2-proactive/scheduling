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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.tests.performance.deployment.process.SSHProcessExecutor;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestPamrRouterDeployer {

    private final String javaPath;

    private final SchedulingFolder schedulingFolder;

    private final InetAddress pamrHost;

    private final Integer pamrPort;

    private final List<PamrReservedId> reservedPamrIds;

    public static TestPamrRouterDeployer createPamrRouterDeployerUsingSystemProperties() throws Exception {
        String javaPath = TestUtils.getRequiredProperty("test.javaPath");
        String schedulingPath = TestUtils.getRequiredProperty("test.schedulingPath");
        String hostName = TestUtils.getRequiredProperty("test.deploy.pamr.startNewRouter.host");
        String reservedIds = TestUtils.getRequiredProperty("test.deploy.pamr.startNewRouter.reservedIds");
        return new TestPamrRouterDeployer(javaPath, schedulingPath, hostName, reservedIds);
    }

    public TestPamrRouterDeployer(String javaPath, String schedulingPath, String hostName, String reservedIds)
            throws Exception {
        this.javaPath = javaPath;
        schedulingFolder = new SchedulingFolder(schedulingPath);
        Collection<String> hosts = Collections.singleton(hostName);

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

        pamrHost = TestDeployer.prepareHostsForTest(hosts, javaPath, schedulingFolder.getRootDirPath())
                .get(0);

        pamrPort = DeploymentTestUtils.findFreePort(pamrHost, javaPath, schedulingFolder
                .getPerformanceClassesDir().getAbsolutePath());
    }

    public void startRouter() throws Exception {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        for (PamrReservedId id : reservedPamrIds) {
            properties.put(id.getId(), id.getCookie());
        }
        String pamrConfigPath = createPamrConfig(schedulingFolder, properties);

        List<String> command = new ArrayList<String>();
        command.add(javaPath);
        command.add("-D" + TestDeployHelper.TEST_JVM_OPTION);
        command.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        command.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-pamr-router"));
        command.add("-cp");
        command.add(schedulingFolder.getRootDirPath() + "/dist/lib/ProActive.jar" + File.pathSeparator +
            schedulingFolder.getRootDirPath() + "/dist/lib/ProActive_utils.jar");
        command.add("org.objectweb.proactive.extensions.pamr.router.Main");
        command.add("--port");
        command.add(String.valueOf(pamrPort));
        command.add("--configFile");
        command.add(pamrConfigPath);
        command.add("--verbose");

        System.out.println("Starting PAMR router on the " + pamrHost + ", command: " + command);
        SSHProcessExecutor pamrProcess = SSHProcessExecutor.createExecutorPrintOutput("PAMR Router",
                pamrHost, command.toArray(new String[command.size()]));
        pamrProcess.start();

        Thread.sleep(5000);
        if (pamrProcess.isProcessFinished()) {
            throw new TestExecutionException("Failed to start PAMR router");
        }
    }

    public InetAddress getPamrHost() {
        return pamrHost;
    }

    public Integer getPamrPort() {
        return pamrPort;
    }

    private String createPamrConfig(SchedulingFolder schedulingFolder, Map<String, String> properties)
            throws IOException {
        File file = new File(schedulingFolder.getTestTmpDir(), "/pamr.router.config");
        PrintWriter writer = new PrintWriter(file);
        try {
            writer.println("configuration=performance_test");
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                writer.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        } finally {
            writer.close();
        }
        return file.getAbsolutePath();
    }

}
