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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
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
     * Use the Java from JAVA_HOME if defined
     */
    {
        String jhome = System.getenv("JAVA_HOME");
        File f = new File(jhome);
        if (f.exists() && f.isDirectory()) {
            javaPath = jhome + ((jhome.endsWith("/")) ? "" : "/") + "bin/java";
        }
    }

    /**
     * ShhClient options (@see {@link SSHClient})
     */
    @Configurable
    protected String sshOptions;
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

    /**
     * Shutdown flag
     */
    protected boolean shutdown = false;

    protected static org.apache.log4j.Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

    /**
     * Configures the Infrastructure
     * 
     * @param parameters
     *            parameters[0]: path to the JVM on the remote host
     *            parameters[1]: ssh options
     *            parameters[2]: path to the Scheduling installation on the remote host
     *            parameters[3]: remote protocol
     *            parameters[4]: remote port
     *            parameters[5]: remote java options
     */
    @Override
    public BooleanWrapper configure(Object... parameters) {
        if (parameters != null && parameters.length >= 6) {
            this.javaPath = parameters[0].toString();
            if (!new File(this.javaPath).isAbsolute()) {
                this.javaPath = "java";
            }
            this.sshOptions = parameters[1].toString();
            this.schedulingPath = parameters[2].toString();
            this.protocol = parameters[3].toString();
            this.port = parameters[4].toString();
            this.javaOptions = parameters[5].toString();
        } else {
            throw new IllegalArgumentException("Invalid parameters for infrastructure creation");
        }

        return new BooleanWrapper(true);
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
        sshCmd += " " + sshOptions;
        sshCmd += " " + host.getHostName();
        sshCmd += " \"" + cmd + "\"";

        try {
            if (host.equals(InetAddress.getLocalHost()) || host.equals(InetAddress.getByName("127.0.0.1"))) {
                logger.debug("The command will be executed locally");
                sshCmd = cmd;
            }
        } catch (UnknownHostException e) {
        }

        logger.info("Executing SSH command: '" + sshCmd + "'");

        Process p = null;
        // start the SSH command in a new process and not a thread:
        // easier killing, prevents the client from polluting stdout
        try {
            String[] cmdArray = null;
            switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    cmdArray = new String[] { CentralPAPropertyRepository.PA_GCMD_UNIX_SHELL.getValue(),
                            "-c", sshCmd };
                    break;
                case windows:
                    sshCmd.replaceAll("/", "\\\\");
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

    @Override
    public void shutDown() {
        shutdown = true;
    }

}
