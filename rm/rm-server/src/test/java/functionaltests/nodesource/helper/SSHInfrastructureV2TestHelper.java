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
package functionaltests.nodesource.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SSHInfrastructureHelper;


public class SSHInfrastructureV2TestHelper {

    private static int port;

    private static SshServer sshd;

    private static String javaExePath;

    private static Object[] infraParams;

    public static void startSSHServer() throws Exception {

        // Disable bouncy castle to avoid versions conflict
        System.setProperty("org.apache.sshd.registerBouncyCastle", "false");

        sshd = SshServer.setUpDefaultServer();

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>(1);
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);

        sshd.setPasswordAuthenticator((username, password, session) -> username != null && username.equals(password));

        sshd.setCommandFactory(new ScpCommandFactory(command -> {

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
        }));

        sshd.start();

        port = sshd.getPort();

        RMTHelper.log("SSH server for SSH infrastructure test created and listening to port " + port);

        javaExePath = System.getProperty("java.home") + File.separator + "bin" + File.separator +
                      (OsUtils.isWin32() ? "java.exe" : "java");
        javaExePath = "\"" + javaExePath + "\"";
    }

    public static Object[] getParameters(int numberOfNodes) {
        if (infraParams == null) {
            infraParams = new Object[] { ("localhost " + numberOfNodes + "\n").getBytes(), //hosts
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
        }
        return infraParams;
    }

    public static void stopSSHServer() throws Exception {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

}
