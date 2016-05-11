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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.ssh.SSHClient;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.apache.log4j.Logger;

import static com.google.common.base.Throwables.getStackTraceAsString;


/**
 * Acquires nodes over SSH given a list of hosts
 * <p>
 * Assumes all necessary ProActive configuration has already been performed: ssh
 * username, key directory, etc. This class won't handle it, see
 * {@link org.objectweb.proactive.core.ssh.SSHClient}.
 * <p>
 * Also assumes JRE and Scheduling installation paths are the same on all hosts.
 * <p>
 * If you need more control over you deployment, you may consider using
 * {@code GCMInfrastructure} instead, which contains the functionalities of this
 * Infrastructure, but requires more configuration.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class SSHInfrastructure extends HostsFileBasedInfrastructureManager {

    private static final Logger logger = Logger.getLogger(SSHInfrastructure.class);

    /**
     * SshClient options (@see {@link SSHClient})
     */
    @Configurable(description = "Options for the ssh command\nto log in the remote hosts")
    protected String sshOptions;
    /**
     * Path to the Java executable on the remote hosts
     */
    @Configurable(description = "Absolute path of the java\nexecutable on the remote hosts")
    protected String javaPath = System.getProperty("java.home") + "/bin/java";

    /**
     * Use the Java from JAVA_HOME if defined
     */
    {
        String jhome = System.getenv("JAVA_HOME");
        if (jhome != null) {
            File f = new File(jhome);
            if (f.exists() && f.isDirectory()) {
                javaPath = jhome + ((jhome.endsWith("/")) ? "" : "/") + "bin/java";
            }
        }
    }

    /**
     * Path to the Scheduling installation on the remote hosts
     */
    @Configurable(description = "Absolute path of the Resource Manager (or Scheduler)\nroot directory on the remote hosts")
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
    /**
     * The type of the OS on the remote machine, 'Linux', 'Windows' or 'Cygwin'
     */
    @Configurable(description = "Linux, Cygwin or Windows depending on\nthe operating system of the remote hosts")
    protected String targetOs = "Linux";
    protected OperatingSystem targetOSObj = null;
    /**
     * Additional java options to append to the command executed on the remote
     * host
     */
    @Configurable(description = "Options for the java command\nlaunching the node on the remote hosts")
    protected String javaOptions;
    /**
     * Path to the credentials file user for RM authentication
     */
    @Configurable(credential = true, description = "Absolute path of the credential file")
    protected File rmCredentialsPath;
    protected Credentials credentials = null;
    /**
     * Shutdown flag
     */
    protected boolean shutdown = false;

    /**
     * Internal node acquisition method
     * <p>
     * Starts a PA runtime on remote host using SSH, register it manually in the
     * nodesource.
     *
     * @param host
     *            hostname of the node on which a node should be started
     * @throws RMException
     *             acquisition failed
     */
    protected void startNodeImpl(InetAddress host, int nbNodes) throws RMException {
        String fs = this.targetOSObj.fs;
        CommandLineBuilder clb = super.getDefaultCommandLineBuilder(this.targetOSObj);
        // we take care of spaces in java path
        clb.setJavaPath(this.javaPath);
        // we set the rm.home prop
        clb.setRmHome(schedulingPath);
        // we set the java security policy file
        StringBuilder sb = new StringBuilder();
        final boolean containsSpace = schedulingPath.contains(" ");
        String securitycmd = CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine();
        if (!this.javaOptions.contains(securitycmd)) {
            sb.append(securitycmd);
            if (containsSpace) {
                sb.append("\"");
            }
            sb.append(schedulingPath);
            sb.append(fs);
            sb.append("config");
            sb.append(fs);
            sb.append("security.java.policy-client");
            if (containsSpace) {
                sb.append("\"");
            }
            sb.append(" ");
        }
        // we set the log4j configuration file
        String log4jcmd = CentralPAPropertyRepository.LOG4J.getCmdLine();
        if (!this.javaOptions.contains(log4jcmd)) {
            sb.append(log4jcmd);
            if (containsSpace) {
                sb.append("\"");
            }
            // log4j only understands urls
            sb.append("file:");
            if (!schedulingPath.startsWith("/")) {
                sb.append("/" + schedulingPath.replace("\\", "/"));
            } else {
                sb.append(schedulingPath.replace("\\", "/"));
            }
            sb.append("/");
            sb.append("config");
            sb.append("/");
            sb.append("log");
            sb.append("/");
            sb.append("node.properties");
            if (containsSpace) {
                sb.append("\"");
            }
            sb.append(" ");
        }
        // we add extra java/PA configuration
        sb.append(this.javaOptions);
        clb.setPaProperties(sb.toString());
        // afterwards, node's name
        // generate the node name
        // current rmcore shortID should be added to ensure uniqueness
        final String nodeName = "SSH-" + this.nodeSource.getName() + "-" + ProActiveCounter.getUniqID();
        clb.setNodeName(nodeName);
        clb.setNumberOfNodes(nbNodes);
        // finally, the credential's value
        String credString = null;
        try {
            credString = new String(this.credentials.getBase64());
        } catch (KeyException e1) {
            throw new RMException("Could not get base64 credentials", e1);
        }
        clb.setCredentialsValueAndNullOthers(credString);

        // add an expected node. every unexpected node will be discarded
        String cmdLine;
        String obfuscatedCmdLine;
        try {
            cmdLine = clb.buildCommandLine(true);
            obfuscatedCmdLine = clb.buildCommandLine(false);
        } catch (IOException e2) {
            throw new RMException(
                "Cannot build the " + RMNodeStarter.class.getSimpleName() + "'s command line.", e2);
        }

        // one escape the command to make it runnable through ssh
        if (cmdLine.contains("\"")) {
            cmdLine = cmdLine.replaceAll("\"", "\\\\\"");
        }

        // we create a new deploying node before ssh command ran
        final List<String> depNodeURLs = new ArrayList<>(nbNodes);
        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(nodeName, nbNodes);
        depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames, obfuscatedCmdLine, "Deploying nodes on host " + host, super.nodeTimeOut));
        addTimeouts(depNodeURLs);


        Process p = null;
        try {
            p = Utils.runSSHCommand(host, cmdLine, sshOptions);
        } catch (IOException e1) {
            multipleDeclareDeployingNodeLost(depNodeURLs, "Cannot run command: " + cmdLine + ", with ssh options: " +
                    sshOptions + " -\n The following exception occutred:\n " + getStackTraceAsString(e1));
            throw new RMException("Cannot run command: " + cmdLine + ", with ssh options: " + sshOptions, e1);
        }

        String lf = System.lineSeparator();

        int circuitBreakerThreshold = 5;
        while (!anyTimedOut(depNodeURLs) && circuitBreakerThreshold > 0) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0) {
                    logger.error("SSH subprocess at " + host.getHostName() + " exited abnormally (" +
                        exitCode + ").");
                } else {
                    logger.error("Launching node process has exited normally whereas it shouldn't.");
                }
                String pOutPut = Utils.extractProcessOutput(p);
                String pErrPut = Utils.extractProcessErrput(p);
                final String description = "SSH command failed to launch node on host " + host.getHostName() +
                    lf + "   >Error code: " + exitCode + lf + "   >Errput: " + pErrPut + "   >Output: " +
                    pOutPut;
                logger.error(description);
                if (super.checkAllNodesAreAcquiredAndDo(createdNodeNames, null, new Runnable() {
                    public void run() {
                        SSHInfrastructure.this.multipleDeclareDeployingNodeLost(depNodeURLs, description);
                    }
                })) {
                    return;
                } else {
                    // there isn't any race regarding node registration
                    throw new RMException(
                        "SSH Node " + nodeName + " is not expected anymore because of an error.");
                }
            } catch (IllegalThreadStateException e) {
                logger.trace("IllegalThreadStateException while waiting for " + nodeName + " registration");
            }

            if (super.checkNodeIsAcquiredAndDo(nodeName, null, null)) {
                // registration is ok, we destroy the process
                p.destroy();
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                circuitBreakerThreshold--;
                logger.trace("An exception occurred while monitoring ssh subprocess", e);
            }
        }

        // if we exit because of a timeout
        if (anyTimedOut(depNodeURLs)) {
            // we remove it
            removeTimeouts(depNodeURLs);
            // we destroy the process
            p.destroy();
            throw new RMException("Deploying Node " + nodeName + " not expected any more");
        }
        if (circuitBreakerThreshold <= 0) {
            logger.error("Circuit breaker threshold reached while monitoring ssh subprocess.");
            throw new RMException("Several exceptions occurred while monitoring ssh subprocess.");
        }
    }

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *            parameters[3] : ssh Options, see {@link SSHClient}
     *            parameters[4] : java path on the remote machines parameters[5]
     *            : Scheduling path on remote machines parameters[6] : target
     *            OS' type (Linux, Windows or Cygwin) parameters[7] : extra java
     *            options parameters[8] : rm cred
     * @throws IllegalArgumentException
     *             configuration failed
     */
    @Override
    public void configure(Object... parameters) {
        super.configure(parameters);
        int index = 3;
        if (parameters != null && parameters.length >= 9) {
            this.sshOptions = parameters[index++].toString();
            this.javaPath = parameters[index++].toString();
            if (this.javaPath == null || this.javaPath.equals("")) {
                throw new IllegalArgumentException("A valid Java path must be supplied.");
            }
            this.schedulingPath = parameters[index++].toString();
            // target OS
            if (parameters[index] != null) {
                this.targetOSObj = OperatingSystem.getOperatingSystem(parameters[index++].toString());
                if (this.targetOSObj == null) {
                    throw new IllegalArgumentException(
                        "Only 'Linux', 'Windows' and 'Cygwin' are valid values for Target OS Property.");
                }
            } else {
                throw new IllegalArgumentException("Target OS parameter cannot be null");
            }

            this.javaOptions = parameters[index++].toString();

            // credentials
            if (parameters[index] == null) {
                throw new IllegalArgumentException("Credentials must be specified");
            }
            try {
                this.credentials = Credentials.getCredentialsBase64((byte[]) parameters[index++]);
            } catch (KeyException e) {
                throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
            }
        } else {
            throw new IllegalArgumentException("Invalid parameters for infrastructure creation");
        }
    }

    @Override
    protected void killNodeImpl(Node node, InetAddress host) {
        final Node n = node;
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    n.getProActiveRuntime().killRT(false);
                } catch (Exception e) {
                    logger.trace("An exception occurred during node removal", e);
                }
            }
        });
    }

    /**
     * @return short description of the IM
     */
    @Override
    public String getDescription() {
        return "Creates remote runtimes using SSH";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SSH Infrastructure";
    }

    @Override
    public void shutDown() {
        this.shutdown = true;
    }
}
