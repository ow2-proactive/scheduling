/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */

package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * Codebase for all SSH-based Instrastructures
 * <p>
 * Defines a set of Configurable parameters as well
 * as a method to execute commands on remote hosts through SSH.
 * <p>
 * Does not provide any configuration on how remote hosts
 * are contacted through SSH, identities and keys are handled by 
 * {@link org.objectweb.proactive.core.ssh.SSHClient}
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 * 
 */
public abstract class AbstractSSHInfrastructure extends InfrastructureManager {

    /**
     * Path to the Java executable on the remote hosts
     */
    @Configurable
    protected String javaPath = System.getProperty("java.home") + "/bin/java";
    /**
     * Path to the Scheduling installation on the remote hosts
     */
    @Configurable
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
    /**
     * Protocol used by remote hosts to expose themselves
     */
    @Configurable
    protected String protocol = Constants.DEFAULT_PROTOCOL_IDENTIFIER;
    /**
     * Port used by remote hosts to expose themselves
     */
    @Configurable
    protected String port = "1099";
    /**
     * Additional java options to append to the command executed on the remote host
     */
    @Configurable
    protected String javaOptions;

    protected static org.apache.log4j.Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * Configures the Infrastructure
     * 
     * @param parameters
     *            parameters[0]: path to the JVM on the remote host 
     *            parameters[1]: path to the Scheduling installation on the remote host
     * @throws RMException configuration failed
     */
    @Override
    public void configure(Object... parameters) throws RMException {
        if (parameters != null && parameters.length >= 5) {
            this.javaPath = parameters[0].toString();
            if (!new File(this.javaPath).isAbsolute()) {
                this.javaPath = "java";
            }
            this.schedulingPath = parameters[1].toString();
            this.protocol = parameters[2].toString();
            this.port = parameters[3].toString();
            this.javaOptions = parameters[4].toString();
        } else {
            throw new RMException("Invalid parameters for infrastructure creation");
        }
    }

    /**
     * Execute a specific command on a remote host through SSH
     * 
     * @param host the remote host on which to execute the command
     * @param cmd the command to execute on the remote host
     * @return the Process in which the SSH command is running; 
     *          NOT the actual process on the remote host, although it can be used
     *          to read the remote process' output.
     * @throws RMException SSH command execution failed
     */
    protected Process runSSHCommand(InetAddress host, String cmd) throws RMException {
        // build the SSH command using ProActive's SSH client:
        // will recover keys/identities if they exist
        String sshCmd = System.getProperty("java.home") + "/bin/java";
        sshCmd += " -cp " + PAResourceManagerProperties.RM_HOME.getValueAsString() +
            "/dist/lib/ProActive.jar";
        sshCmd += " -Dproactive.useIPaddress=true ";
        sshCmd += " org.objectweb.proactive.core.ssh.SSHClient";
        sshCmd += " " + host.getHostName();
        sshCmd += " \"" + cmd + "\"";

        try {
            if (host.equals(InetAddress.getLocalHost()) || host.equals(InetAddress.getByName("127.0.0.1"))) {
                sshCmd = cmd;
            }
        } catch (UnknownHostException e) {
        }

        Process p = null;
        // start the SSH command in a new process and not a thread:
        // easier killing, prevents the client from polluting stdout
        try {
            String[] cmdArray = null;
            switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    cmdArray = new String[] { PAProperties.PA_GCMD_UNIX_SHELL.getValue(), "-c", sshCmd };
                    break;
                case windows:
                    sshCmd.replaceAll("/", "\\");
                    cmdArray = new String[] { sshCmd };
                    break;
            }

            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            p = pb.start();

        } catch (Exception e1) {
            throw new RMException("Failed to execute SSH command: " + sshCmd, e1);
        }
        return p;
    }
}
