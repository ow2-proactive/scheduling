/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.nodesource;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
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
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;


public class TestSSHInfrastructureV2 extends RMFunctionalTest {

    @Test
    public void action() throws Exception {
        ResourceManager resourceManager = this.rmHelper.getResourceManager();

        RMTHelper.log("Test - Create SSH infrastructure on ssh://localhost on port " + this.port);

        String javaExePath = System.getProperty("java.home") + File.separator + "bin" + File.separator +
            (OsUtils.isWin32() ? "java.exe" : "java");
        javaExePath = "\"" + javaExePath + "\"";

        Object[] infraParams = new Object[] { "localhost 1\n".getBytes(), //hosts
                60000, //timeout
                1, //attempts
                this.port, //ssh server port
                "toto", //ssh username
                "toto", //ssh password
                new byte[0], // optional ssh private key
                new byte[0], // optional ssh options file
                javaExePath, //java path on the remote machines
                PAResourceManagerProperties.RM_HOME.getValueAsString(), //Scheduling path on remote machines
                OperatingSystem.getOperatingSystem(), "" }; // extra java options

        final Object[] policyParameters = new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString() };

        String nsname = "testSSHInfra";
        resourceManager.createNodeSource(nsname, SSHInfrastructureV2.class.getName(), infraParams,
                StaticPolicy.class.getName(), policyParameters);
        this.rmHelper.waitForNodeSourceCreation(nsname, 1);

        RMState s = resourceManager.getState();
        assertEquals(1, s.getTotalNodesNumber());
        assertEquals(1, s.getFreeNodesNumber());
    }

    private int port;
    private SshServer sshd;

    @Before
    public void startSSHServer() throws Exception {
        // Disable bouncy castle to avoid versions conflict
        System.setProperty("org.apache.sshd.registerBouncyCastle", "false");

        sshd = SshServer.setUpDefaultServer();

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        if (OsUtils.isUNIX()) {
            sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" }, EnumSet
                    .of(ProcessShellFactory.TtyOptions.ONlCr)));
        } else {
            sshd.setShellFactory(new ProcessShellFactory(new String[] { "cmd.exe " }, EnumSet.of(
                    ProcessShellFactory.TtyOptions.Echo, ProcessShellFactory.TtyOptions.ICrNl,
                    ProcessShellFactory.TtyOptions.ONlCr)));
        }

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
                EnumSet<ProcessShellFactory.TtyOptions> ttyOptions;
                if (OsUtils.isUNIX()) {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr);
                } else {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.Echo,
                            ProcessShellFactory.TtyOptions.ICrNl, ProcessShellFactory.TtyOptions.ONlCr);
                }
                return new ProcessShellFactory(splitCommand(command), ttyOptions).create();
            }
        }));

        sshd.start();

        this.port = sshd.getPort();
    }

    @After
    public void stopSSHServer() throws Exception {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

    private static String[] splitCommand(String command) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\\\\\"]\\S*|\\\\\".+?\\\\\")\\s*").matcher(command);
        while (m.find()) {
            list.add(m.group(1).replaceAll("\\\\\"", ""));
        }
        return list.toArray(new String[list.size()]);
    }
}
