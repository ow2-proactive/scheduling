/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;
import org.ow2.proactive.utils.FileUtils;


public class TestScheduler {

    public static final int PNP_PORT = TestRM.PA_PNP_PORT;

    private static String schedulerUrl = "pnp://" + ProActiveInet.getInstance().getHostname() + ":" +
        PNP_PORT + "/";
    private static Process schedulerProcess;

    private SchedulerAuthenticationInterface schedulerAuth;
    private RMAuthentication rmAuth;

    private CookieBasedProcessTreeKiller processTreeKiller;
    private SchedulerTestConfiguration startedConfiguration = SchedulerTestConfiguration.defaultConfiguration();

    public synchronized void start(SchedulerTestConfiguration configuration) throws Exception {
        kill();
        cleanTMP();

        startedConfiguration = configuration;

        List<String> commandLine = new ArrayList<>();
        commandLine.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        commandLine.add("-Djava.security.manager");
        // commandLine.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");

        String proactiveHome = CentralPAPropertyRepository.PA_HOME.getValue();
        if (!CentralPAPropertyRepository.PA_HOME.isSet()) {
            proactiveHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
            CentralPAPropertyRepository.PA_HOME.setValue(PAResourceManagerProperties.RM_HOME
                    .getValueAsString());
        }

        commandLine.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + proactiveHome);

        commandLine.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pnp");
        commandLine.add(PNPConfig.PA_PNP_PORT.getCmdLine() + configuration.getPnpPort());

        String securityPolicy = CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue();
        if (!CentralPAPropertyRepository.JAVA_SECURITY_POLICY.isSet()) {
            securityPolicy = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
                "/config/security.java.policy-server";
        }
        commandLine.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + securityPolicy);

        String log4jConfiguration = CentralPAPropertyRepository.LOG4J.getValue();
        if (!CentralPAPropertyRepository.LOG4J.isSet()) {
            log4jConfiguration = SchedulerTHelper.class.getResource("/log4j-junit").toString();
        }
        commandLine.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + log4jConfiguration);

        commandLine.add(PASchedulerProperties.SCHEDULER_HOME.getCmdLine() +
            PASchedulerProperties.SCHEDULER_HOME.getValueAsString());
        commandLine.add(PAResourceManagerProperties.RM_HOME.getCmdLine() +
                PAResourceManagerProperties.RM_HOME.getValueAsString());

        String forkMethodKeyValue = System.getProperty(ForkerUtils.FORK_METHOD_KEY);
        if (forkMethodKeyValue != null) {
            commandLine.add("-D" + ForkerUtils.FORK_METHOD_KEY + "=" +
                System.getProperty(forkMethodKeyValue));
        }
        if (System.getProperty("proactive.test.runAsMe") != null) {
            commandLine.add("-Dproactive.test.runAsMe=true");
        }
        //commandLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8765");

        commandLine.add("-cp");
        commandLine.add(testClasspath());
        commandLine.add("-Djava.awt.headless=true"); // For Mac builds
        commandLine.add("-Djava.library.path=" + System.getProperty("java.library.path"));
        commandLine.add(CentralPAPropertyRepository.PA_TEST.getCmdLine() + "true");
        commandLine.add(SchedulerStartForFunctionalTest.class.getName());
        commandLine.add(String.valueOf(configuration.hasLocalNodes()));
        commandLine.add(configuration.getSchedulerConfigFile());
        commandLine.add(configuration.getRMConfigFile());
        String rmToConnectTo;
        if (configuration.getRMToConnectTo() != null) {
            rmToConnectTo = configuration.getRMToConnectTo();
            commandLine.add(configuration.getRMToConnectTo());
        } else {
            rmToConnectTo = schedulerUrl;
        }

        System.out.println("Starting Scheduler process: " + commandLine);

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        processTreeKiller = CookieBasedProcessTreeKiller.createProcessChildrenKiller("TEST_SCHED",
                processBuilder.environment());
        schedulerProcess = processBuilder.start();

        InputStreamReaderThread outputReader = new InputStreamReaderThread(schedulerProcess.getInputStream(),
            "[Scheduler output]: ");
        outputReader.start();

        System.out.println("Waiting for the Scheduler using URL: " + schedulerUrl);

        rmAuth = RMConnection.waitAndJoin(rmToConnectTo, 120000);
        startLocalNodes(configuration);

        schedulerAuth = SchedulerConnection.waitAndJoin(schedulerUrl, 120000);
        System.out.println("The Scheduler is up and running");
    }

    private void startLocalNodes(
      SchedulerTestConfiguration configuration) throws KeyException, LoginException, InterruptedException {
        if (configuration.hasLocalNodes()) {
            // Waiting while all the nodes will be registered in the RM.
            // Without waiting test can finish earlier than nodes are added.
            // It leads to test execution hang up on windows due to running processes.

            Credentials creds = Credentials.createCredentials(
                    new CredData(CredData.parseLogin(TestUsers.DEMO.username), CredData
                            .parseDomain(TestUsers.DEMO.username), TestUsers.DEMO.password), rmAuth
                            .getPublicKey());
            ResourceManager rm = rmAuth.login(creds);
            while (rm.getState().getTotalAliveNodesNumber() < SchedulerStartForFunctionalTest.RM_NODE_NUMBER) {
                Thread.sleep(50);
            }
            rm.disconnect();
            System.out.println("Nodes are deployed");
        }
    }

    public void kill() throws Exception {
        if (schedulerProcess != null) {
            schedulerProcess.destroy();
            schedulerProcess.waitFor();
            processTreeKiller.kill();
            schedulerProcess = null;
        }
    }

    public static String testClasspath() {
        return System.getProperty("java.class.path");
    }

    /* convenience method to clean TMP from dataspace when executing test */
    private static void cleanTMP() {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        for (File f : tmp.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("PA_JVM");
            }
        })) {
            FileUtils.removeDir(f);
        }
    }

    public synchronized SchedulerAuthenticationInterface getAuth() {
        return schedulerAuth;
    }

    public boolean isStartedWithSameConfiguration(SchedulerTestConfiguration configuration)
            throws URISyntaxException {
        return schedulerProcess != null && startedConfiguration.equals(configuration);
    }

    public synchronized RMAuthentication getRMAuth() {
        return rmAuth;
    }

    public String getUrl() {
        return schedulerUrl;
    }

}
