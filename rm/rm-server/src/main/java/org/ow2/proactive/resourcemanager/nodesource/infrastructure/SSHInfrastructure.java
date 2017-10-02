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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import static com.google.common.base.Throwables.getStackTraceAsString;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyException;
import java.util.List;

import org.apache.log4j.Logger;
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

    private static final String CREDENTIALS_KEY = "credentials";

    private static final String TARGET_OS_OBJ_KEY = "targetOSObj";

    /** key of the shutdown flag */
    private static final String SHUTDOWN_FLAG_KEY = "shutdownFlag";

    /**
     * Internal node acquisition method
     * <p>
     * Starts a PA runtime on remote host using SSH, register it manually in the
     * nodesource.
     *
     * @param host The host on which one the node will be started
     * @param nbNodes number of nodes to deploy
     * @param depNodeURLs list of deploying or lost nodes urls created      
     * @throws RMException
     *             acquisition failed
     */
    protected void startNodeImpl(InetAddress host, int nbNodes, final List<String> depNodeURLs) throws RMException {
        String fs = getTargetOSObj().fs;
        CommandLineBuilder clb = super.getDefaultCommandLineBuilder(getTargetOSObj());
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
            credString = new String(getCredentials().getBase64());
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
            throw new RMException("Cannot build the " + RMNodeStarter.class.getSimpleName() + "'s command line.", e2);
        }

        // one escape the command to make it runnable through ssh
        if (cmdLine.contains("\"")) {
            cmdLine = cmdLine.replaceAll("\"", "\\\\\"");
        }

        // we create a new deploying node before ssh command ran
        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(nodeName, nbNodes);
        depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames,
                                                     obfuscatedCmdLine,
                                                     "Deploying nodes on host " + host,
                                                     super.nodeTimeOut));
        addTimeouts(depNodeURLs);

        Process p = null;
        try {
            p = Utils.runSSHCommand(host, cmdLine, sshOptions);
        } catch (IOException e1) {
            multipleDeclareDeployingNodeLost(depNodeURLs,
                                             "Cannot run command: " + cmdLine + ", with ssh options: " + sshOptions +
                                                          " -\n The following exception occutred:\n " +
                                                          getStackTraceAsString(e1));
            throw new RMException("Cannot run command: " + cmdLine + ", with ssh options: " + sshOptions, e1);
        }

        String lf = System.lineSeparator();

        int circuitBreakerThreshold = 5;
        while (!anyTimedOut(depNodeURLs) && circuitBreakerThreshold > 0) {
            try {
                int exitCode = p.exitValue();
                if (exitCode != 0) {
                    logger.error("SSH subprocess at " + host.getHostName() + " exited abnormally (" + exitCode + ").");
                } else {
                    logger.error("Launching node process has exited normally whereas it shouldn't.");
                }
                String pOutPut = Utils.extractProcessOutput(p);
                String pErrPut = Utils.extractProcessErrput(p);
                final String description = "SSH command failed to launch node on host " + host.getHostName() + lf +
                                           "   >Error code: " + exitCode + lf + "   >Errput: " + pErrPut +
                                           "   >Output: " + pOutPut;
                logger.error(description);
                if (super.checkAllNodesAreAcquiredAndDo(createdNodeNames, null, new Runnable() {
                    public void run() {
                        SSHInfrastructure.this.multipleDeclareDeployingNodeLost(depNodeURLs, description);
                    }
                })) {
                    return;
                } else {
                    // there isn't any race regarding node registration
                    throw new RMException("SSH Node " + nodeName + " is not expected anymore because of an error.");
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
     *            parameters[4] : ssh Options, see {@link SSHClient}
     *            parameters[5] : java path on the remote machines parameters[6]
     *            : Scheduling path on remote machines parameters[7] : target
     *            OS' type (Linux, Windows or Cygwin) parameters[8] : extra java
     *            options parameters[9] : rm cred
     * @throws IllegalArgumentException
     *             configuration failed
     */
    @Override
    public void configure(Object... parameters) {
        super.configure(parameters);
        int index = 4;
        if (parameters != null && parameters.length >= 10) {
            this.sshOptions = parameters[index++].toString();
            this.javaPath = parameters[index++].toString();
            if (this.javaPath == null || this.javaPath.equals("")) {
                throw new IllegalArgumentException("A valid Java path must be supplied.");
            }
            this.schedulingPath = parameters[index++].toString();
            // target OS
            if (parameters[index] != null) {
                OperatingSystem configuredTargetOs = OperatingSystem.getOperatingSystem(parameters[index++].toString());
                if (configuredTargetOs == null) {
                    throw new IllegalArgumentException("Only 'Linux', 'Windows' and 'Cygwin' are valid values for Target OS Property.");
                }
                persistedInfraVariables.put(TARGET_OS_OBJ_KEY, configuredTargetOs);
            } else {
                throw new IllegalArgumentException("Target OS parameter cannot be null");
            }

            this.javaOptions = parameters[index++].toString();

            // credentials
            if (parameters[index] == null) {
                throw new IllegalArgumentException("Credentials must be specified");
            }
            try {
                persistedInfraVariables.put(CREDENTIALS_KEY,
                                            Credentials.getCredentialsBase64((byte[]) parameters[index++]));
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
        setShutdownFlag(true);
    }

    @Override
    protected void initializePersistedInfraVariables() {
        super.initializePersistedInfraVariables();
        persistedInfraVariables.put(CREDENTIALS_KEY, null);
        persistedInfraVariables.put(TARGET_OS_OBJ_KEY, null);
        persistedInfraVariables.put(SHUTDOWN_FLAG_KEY, false);
    }

    // Below are wrapper methods around the runtime variables map

    private Credentials getCredentials() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Credentials>() {
            @Override
            public Credentials handle() {
                return (Credentials) persistedInfraVariables.get(CREDENTIALS_KEY);
            }
        });
    }

    private OperatingSystem getTargetOSObj() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<OperatingSystem>() {
            @Override
            public OperatingSystem handle() {
                return (OperatingSystem) persistedInfraVariables.get(TARGET_OS_OBJ_KEY);
            }
        });
    }

    private void setShutdownFlag(final boolean isShutdown) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                persistedInfraVariables.put(SHUTDOWN_FLAG_KEY, isShutdown);
                return null;
            }
        });
    }

}
