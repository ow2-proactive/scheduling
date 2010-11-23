/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


public class Utils {

    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);;

    /**
     * Execute a specific command on a remote host through SSH
     *
     * @param host the remote host on which to execute the command
     * @param cmd the command to execute on the remote host
     * @param sshOptions a string for passing options especially to the ssh command. See {@link SSHClient}
     * @return the Process in which the SSH command is running;
     *          NOT the actual process on the remote host, although it can be used
     *          to read the remote process' output.
     * @throws IOException if the ssh process cannot be starter
     */
    public static Process runSSHCommand(InetAddress host, String cmd, String sshOptions) throws IOException {
        // build the SSH command using ProActive's SSH client:
        // will recover keys/identities if they exist
        String sshCmd = System.getProperty("java.home") + "/bin/java";
        if (sshCmd.contains(" ")) {
            switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    sshCmd = sshCmd.replaceAll(" ", "\\ ");
                    break;
                case windows:
                    sshCmd = "\"" + sshCmd + "\"";
                    break;
            }
        }
        sshCmd += " -cp " + PAResourceManagerProperties.RM_HOME.getValueAsString() +
            "/dist/lib/ProActive.jar";
        sshCmd += " -Dproactive.useIPaddress=true ";
        sshCmd += SSHClient.class.getName();
        sshCmd += " " + sshOptions;
        sshCmd += " " + host.getHostName();
        sshCmd += " \"" + cmd + "\"";

        try {
            if (host.equals(InetAddress.getLocalHost()) || host.equals(InetAddress.getByName("127.0.0.1"))) {
                logger.debug("The command will be executed locally");
                sshCmd = cmd.replaceAll("\\\\", "");
            }
        } catch (UnknownHostException e) {
            logger
                    .warn(
                            "A problem occurred while determining if the ssh command was sshing localhost. Cannot bypass ssh connection.",
                            e);
        }

        logger.info("Executing SSH command: '" + sshCmd + "'");

        Process p = null;
        // start the SSH command in a new process and not a thread:
        // easier killing, prevents the client from polluting stdout
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                p = Runtime.getRuntime().exec(
                        new String[] { CentralPAPropertyRepository.PA_GCMD_UNIX_SHELL.getValue(), "-c",
                                sshCmd });
                break;
            case windows:
                p = Runtime.getRuntime().exec(sshCmd);
                break;
        }

        return p;
    }

}
