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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.tests.performance.deployment.DeploymentTestUtils;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.deployment.process.SSHProcessExecutor;


public class TestPamrRMDeployHelper extends TestRMDeployHelper {

    public static final String RM_PAMR_ID = "0";

    public static final String RM_PAMR_COOKIE = "test_resource_manager";

    private Integer pamrPort;

    private final InetAddress pamrHost;

    public TestPamrRMDeployHelper(SchedulingFolder schedulingFolder, InetAddress rmHost,
            InetAddress pamrHost, String javaPath) {
        super(schedulingFolder, rmHost, javaPath);
        this.pamrHost = pamrHost;
    }

    @Override
    public String prepareForDeployment() throws Exception {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(RM_PAMR_ID, RM_PAMR_COOKIE);
        String pamrConfigPath = createPamrConfig(schedulingFolder, properties);

        pamrPort = DeploymentTestUtils.findFreePort(pamrHost, javaPath, schedulingFolder
                .getPerformanceClassesDir().getAbsolutePath());

        List<String> command = new ArrayList<String>();
        command.add(javaPath);
        command.add("-D" + TEST_JVM_OPTION);
        command.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        command.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-pamr-router"));
        command.add("-cp");
        command.add(schedulingFolder.getRootDirPath() + "/dist/lib/ProActive.jar" + File.pathSeparator +
            schedulingFolder.getRootDirPath() + "/dist/lib/ProActive_utils.jar");
        command.add("org.objectweb.proactive.extensions.pamr.router.Main");
        command.add("--port");
        command.add(String.valueOf(pamrPort));
        command.add("--configFile");
        command.add(pamrConfigPath);
        command.add("--verbose");

        System.out.println("Starting PAMR on the " + pamrHost + ", command: " + command);
        SSHProcessExecutor pamrProcess = SSHProcessExecutor.createExecutorPrintOutput("PAMR Router",
                pamrHost, command.toArray(new String[command.size()]));
        pamrProcess.start();

        Thread.sleep(5000);
        if (pamrProcess.isProcessFinished()) {
            throw new TestExecutionException("Failed to start PAMR ROUTER");
        }
        pamrProcess.killProcess();

        return String.format("pamr://%s/", RM_PAMR_ID);
    }

    @Override
    public Map<String, String> getClientProActiveProperties() {
        if (pamrPort == null) {
            throw new IllegalStateException("TestPamrRMDeployHelper didn't prepare deployment");
        }
        Map<String, String> properties = super.getClientProActiveProperties();
        properties.put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), "pamr");
        properties.put(PAMRConfig.PA_NET_ROUTER_ADDRESS.getName(), pamrHost.getHostName());
        properties.put(PAMRConfig.PA_NET_ROUTER_PORT.getName(), String.valueOf(pamrPort));
        return properties;
    }

    @Override
    protected List<String> getAdditionalRMJavaOptions() {
        if (pamrPort == null) {
            throw new IllegalStateException("TestPamrRMDeployHelper didn't prepare deployment");
        }

        List<String> options = new ArrayList<String>();

        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pamr");
        options.add(PAMRConfig.PA_NET_ROUTER_ADDRESS.getCmdLine() + pamrHost.getHostName());
        options.add(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + String.valueOf(pamrPort));

        options.add(PAMRConfig.PA_PAMR_AGENT_ID.getCmdLine() + RM_PAMR_ID);
        options.add(PAMRConfig.PA_PAMR_AGENT_MAGIC_COOKIE.getCmdLine() + RM_PAMR_COOKIE);

        return options;
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
