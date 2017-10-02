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
package functionaltests.nodesource;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.SSHInfrastructureHelper;


/**
 * For convenience, public static members of this class are also used in
 * {@link functionaltests.nodesrecovery.RecoverSSHInfrastructureV2Test}.
 */
public class TestSSHInfrastructureV2 extends RMFunctionalTest {

    public static int port;

    public static SshServer sshd;

    public static String javaExePath;

    public static Object[] infraParams;

    public static Object[] policyParameters;

    public static String nsname = "testSSHInfra";

    public static int NB_NODES = 3;

    private ResourceManager resourceManager;

    @Test
    public void testSSHInfrastructureV2() throws Exception {

        nsname = "testSSHInfra";

        resourceManager = this.rmHelper.getResourceManager();

        RMTHelper.log("Test - Create SSH infrastructure on ssh://localhost on port " + this.port);

        resourceManager.createNodeSource(nsname,
                                         SSHInfrastructureV2.class.getName(),
                                         infraParams,
                                         StaticPolicy.class.getName(),
                                         policyParameters);
        this.rmHelper.waitForNodeSourceCreation(nsname, NB_NODES, this.rmHelper.getMonitorsHandler());

        Thread.sleep(10000);

        RMTHelper.log("Checking scheduler state after node source creation");
        RMState s = resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());
    }

    @After
    public void removeNS() throws Exception {
        RMTHelper.log("Removing node source");
        try {
            resourceManager.removeNodeSource(nsname, true);
        } catch (Exception ignored) {

        }
    }

    @BeforeClass
    public static void startSSHServer() throws Exception {
        // Disable bouncy castle to avoid versions conflict
        System.setProperty("org.apache.sshd.registerBouncyCastle", "false");

        sshd = SshServer.setUpDefaultServer();

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>(1);
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);

        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return username != null && username.equals(password);
            }
        });

        sshd.setCommandFactory(new ScpCommandFactory(new CommandFactory() {
            @Override
            public Command createCommand(String command) {
                String[] splitCommand;
                if (OsUtils.isUNIX()) {
                    splitCommand = SSHInfrastructureHelper.splitCommand(command);
                } else if (OsUtils.isWin32()) {
                    splitCommand = SSHInfrastructureHelper.splitCommandWithoutRemovingQuotes(command);
                } else {
                    throw new IllegalStateException("Operating system is not recognized");
                }
                StringBuilder rebuiltCommand = new StringBuilder();
                for (String commandPiece : splitCommand) {
                    rebuiltCommand.append(commandPiece).append(" ");
                }
                rebuiltCommand.trimToSize();

                EnumSet<ProcessShellFactory.TtyOptions> ttyOptions;
                if (OsUtils.isUNIX()) {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr);
                } else {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.Echo,
                                            ProcessShellFactory.TtyOptions.ICrNl,
                                            ProcessShellFactory.TtyOptions.ONlCr);
                }

                if (OsUtils.isUNIX()) {
                    return new ProcessShellFactory(new String[] { "/bin/sh", "-c", rebuiltCommand.toString() },
                                                   ttyOptions).create();
                } else {
                    return new ProcessShellFactory(new String[] { "cmd.exe", "/C", rebuiltCommand.toString() },
                                                   ttyOptions).create();
                }
            }
        }));

        sshd.start();

        port = sshd.getPort();

        javaExePath = System.getProperty("java.home") + File.separator + "bin" + File.separator +
                      (OsUtils.isWin32() ? "java.exe" : "java");
        javaExePath = "\"" + javaExePath + "\"";

        infraParams = new Object[] { ("localhost " + NB_NODES + "\n").getBytes(), //hosts
                                     60000, //timeout
                                     0, //attempts
                                     10, //wait between failures
                                     port, //ssh server port
                                     "toto", //ssh username
                                     "toto", //ssh password
                                     new byte[0], // optional ssh private key
                                     new byte[0], // optional ssh options file
                                     javaExePath, //java path on the remote machines
                                     PAResourceManagerProperties.RM_HOME.getValueAsString(), //Scheduling path on remote machines
                                     OperatingSystem.getOperatingSystem(), "" }; // extra java options

        policyParameters = new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString(), "20000" };

    }

    @AfterClass
    public static void stopSSHServer() throws Exception {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

}
