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
package functionaltests.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;


public class TestRM {

    public static final URL FUNCTIONAL_TEST_RM_PROPERTIES = RMTHelper.class.getResource("/functionaltests/config/functionalTRMProperties.ini");

    // default RMI port
    // do not use the one from proactive config to be able to
    // keep the RM running after the test with rmi registry is killed
    public static int PA_PNP_PORT = 1199;

    private CookieBasedProcessTreeKiller processTreeKiller;

    private static String DEFAULT_CONFIGURATION;
    static {
        try {
            DEFAULT_CONFIGURATION = new File(FUNCTIONAL_TEST_RM_PROPERTIES.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Process rmProcess;

    private int pnpPort = PA_PNP_PORT;

    private String startedConfiguration = "";

    private RMAuthentication rmAuth;

    public boolean isStartedWithSameConfiguration(String configurationFile) {
        return isStarted() && (startedConfiguration.equals(configurationFile) ||
                               configurationFile == null && startedConfiguration.equals(DEFAULT_CONFIGURATION));
    }

    public synchronized void start(String configurationFile, int pnpPort, String... jvmArgs) throws Exception {
        if (configurationFile == null) {
            configurationFile = DEFAULT_CONFIGURATION;
        }

        kill();
        this.pnpPort = pnpPort;
        startedConfiguration = configurationFile;

        PAResourceManagerProperties.updateProperties(configurationFile);

        List<String> commandLine = new ArrayList<>();
        commandLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        commandLine.add("-Djava.security.manager");

        String proactiveHome = CentralPAPropertyRepository.PA_HOME.getValue();
        if (!CentralPAPropertyRepository.PA_HOME.isSet()) {
            proactiveHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
        }
        commandLine.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pnp");
        commandLine.add(PNPConfig.PA_PNP_PORT.getCmdLine() + this.pnpPort);

        commandLine.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + proactiveHome);

        String securityPolicy = CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue();
        if (!CentralPAPropertyRepository.JAVA_SECURITY_POLICY.isSet()) {
            securityPolicy = PAResourceManagerProperties.RM_HOME.getValueAsString() +
                             "/config/security.java.policy-server";
        }
        commandLine.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + securityPolicy);

        String log4jConfiguration = CentralPAPropertyRepository.LOG4J.getValue();
        if (!CentralPAPropertyRepository.LOG4J.isSet()) {
            log4jConfiguration = RMTHelper.class.getResource("/log4j-junit").toString();
        }
        commandLine.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + log4jConfiguration);

        // commandLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8765");

        commandLine.add(PAResourceManagerProperties.RM_HOME.getCmdLine() +
                        PAResourceManagerProperties.RM_HOME.getValueAsString());
        commandLine.add(CentralPAPropertyRepository.PA_RUNTIME_PING.getCmdLine() + false);

        commandLine.add("-cp");
        commandLine.add(testClasspath());
        commandLine.add("-Djava.library.path=" + System.getProperty("java.library.path"));
        commandLine.add(CentralPAPropertyRepository.PA_TEST.getCmdLine() + "true");
        commandLine.add("-Djava.awt.headless=true"); // For Mac builds
        Collections.addAll(commandLine, jvmArgs);
        commandLine.add(RMStarterForFunctionalTest.class.getName());
        commandLine.add(configurationFile);
        System.out.println("Starting RM process: " + commandLine);

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        processTreeKiller = CookieBasedProcessTreeKiller.createProcessChildrenKiller("TEST_RM",
                                                                                     processBuilder.environment());
        rmProcess = processBuilder.start();

        InputStreamReaderThread outputReader = new InputStreamReaderThread(rmProcess.getInputStream(), "[RM output]: ");
        outputReader.start();

        String url = getUrl();
        rmAuth = RMConnection.waitAndJoin(url, 120000);
    }

    public void kill() throws Exception {
        if (rmProcess != null) {
            System.out.println("Destroying RM process.");
            rmProcess.destroy();
            rmProcess.waitFor();
            processTreeKiller.kill();
            rmProcess = null;
        }
    }

    public String getUrl() {
        return "pnp://localhost:" + pnpPort + "/";
    }

    private static String testClasspath() {
        String home = PAResourceManagerProperties.RM_HOME.getValueAsString();
        String classpathToLibFolderWithWildcard = home + File.separator + "dist" + File.separator + "lib" +
                                                  File.separator + "*";
        if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.windows)) {
            // required by windows otherwise wildcard is expanded
            classpathToLibFolderWithWildcard = "\"" + classpathToLibFolderWithWildcard + "\"";
        }
        return classpathToLibFolderWithWildcard;
    }

    public synchronized RMAuthentication getAuth() {
        return rmAuth;
    }

    public boolean isStarted() {
        return rmProcess != null;
    }
}
