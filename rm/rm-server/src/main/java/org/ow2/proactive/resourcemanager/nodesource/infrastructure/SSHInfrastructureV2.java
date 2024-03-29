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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.InitScriptGenerator;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


/**
 * Acquires nodes over SSH given a list of hosts, this implementation uses jsch.
 * <p>
 * Also assumes JRE and Scheduling installation paths are the same on all hosts.
 * <p>
 * If you need more control over you deployment, you may consider using
 * {@link CLIInfrastructure} instead.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 6.0.0
 */
public class SSHInfrastructureV2 extends HostsFileBasedInfrastructureManager {

    private static final Logger logger = Logger.getLogger(SSHInfrastructureV2.class);

    private static final int PROCESS_STILL_RUNNING_VALUE = -1;

    public static final int DEFAULT_OUTPUT_BUFFER_LENGTH = 1000;

    public static final int DEFAULT_SSH_PORT = 22;

    @Configurable(description = "The port of the ssh server " + DEFAULT_SSH_PORT + " by default", sectionSelector = 2)
    protected int sshPort = DEFAULT_SSH_PORT;

    @Configurable(description = "Specifies the user to log in as on the remote machine", sectionSelector = 2, important = true)
    protected String sshUsername;

    @Configurable(description = "The password to use for authentification (less secure than private key)", password = true, sectionSelector = 2, important = true)
    protected String sshPassword;

    @Configurable(fileBrowser = true, description = "If no password specify the private key file", sectionSelector = 2)
    protected byte[] sshPrivateKey;

    @Configurable(fileBrowser = true, description = "Options file for the ssh to log in the remote hosts, use key=value format, if empty StrictHostKeyChecking is disabled", sectionSelector = 2)
    protected Properties sshOptions;

    @Configurable(description = "Absolute path of the java executable on the remote hosts", sectionSelector = 1)
    protected String javaPath = Utils.getDefaultJavaPath();

    @Configurable(description = "Absolute path of the Resource Manager (or Scheduler)root directory on the remote hosts", sectionSelector = 1)
    protected String schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();

    @Configurable(description = "Linux, Cygwin or Windows depending on the operating system of the remote hosts", sectionSelector = 1)
    protected String targetOs = "Linux";

    @Configurable(textAreaOneLine = true, description = "Options for the java command launching the node on the remote hosts", sectionSelector = 3)
    protected String javaOptions;

    @Configurable(description = "Specifies how the ProActive node command is started. Can be autoGenerated (default), useStartupScript or useNodeJarStartupScript. Please refer to the infrastructure documentation for more information", sectionSelector = 1)
    protected String deploymentMode = "autoGenerated";

    @Configurable(description = "The URL from which the node.jar will be downloaded. Used only when useNodeJarStartupScript mode is selected", sectionSelector = 1)
    protected String nodeJarUrl = InitScriptGenerator.createNodeJarUrl();

    @Configurable(textArea = true, description = "Standard Startup script that will start the ProActive node agents. Used only when useStartupScript mode is selected", sectionSelector = 1)
    protected String startupScriptStandard = InitScriptGenerator.getDefaultLinuxStandardCommand();

    @Configurable(textArea = true, description = "Startup script that will download a Java Runtime Environment, a jar archive containing all ProActive libraries (node.jar) and start ProActive Agent using it. Used only when useNodeJarStartupScript mode is selected", sectionSelector = 1)
    protected String startupScriptWithNodeJarDownload = InitScriptGenerator.getDefaultLinuxNodeJarCommand();

    private static final String TARGET_OS_OBJ_KEY = "targetOSObj";

    /**
     * Indicates whether the infrastructure is shutting down.
     */
    private AtomicBoolean shutDown = new AtomicBoolean(false);

    /**
     * Internal node acquisition method
     * <p>
     * Starts a PA runtime on remote host using SSH, register it manually in the
     * nodesource.
     *
     * @param hostTracker The host on which one the node will be started
     * @param nbNodes number of nodes to deploy
     * @param depNodeURLs list of deploying or lost nodes urls created
     * @throws RMException
     *             acquisition failed
     */
    public void startNodeImpl(final HostTracker hostTracker, final int nbNodes, final List<String> depNodeURLs)
            throws RMException {
        String fs = getTargetOSObj().fs;
        // we set the java security policy file
        ArrayList<String> sb = new ArrayList<>();
        final boolean containsSpace = schedulingPath.contains(" ");
        if (!deploymentMode.equals("useNodeJarStartupScript")) {
            if (containsSpace) {
                sb.add("-Dproactive.home=\"" + schedulingPath + "\"");
            } else {
                sb.add("-Dproactive.home=" + schedulingPath);
            }
        }
        String securitycmd = CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine();
        if (!this.javaOptions.contains(securitycmd) && !deploymentMode.equals("useNodeJarStartupScript")) {
            if (containsSpace) {
                securitycmd += "\"";
            }
            securitycmd += this.schedulingPath + fs + "config" + fs;
            securitycmd += "security.java.policy-client";
            if (containsSpace) {
                securitycmd += "\"";
            }
            sb.add(securitycmd);
        }
        // we set the log4j configuration file
        String log4jcmd = CentralPAPropertyRepository.LOG4J.getCmdLine();
        if (!this.javaOptions.contains(log4jcmd) && !deploymentMode.equals("useNodeJarStartupScript")) {
            // log4j only understands urls
            if (containsSpace) {
                log4jcmd += "\"";
            }
            log4jcmd += "file:";
            if (!this.schedulingPath.startsWith("/")) {
                log4jcmd += "/";
            }
            log4jcmd += this.schedulingPath.replace("\\", "/");
            log4jcmd += "/config/log/node.properties";
            if (containsSpace) {
                log4jcmd += "\"";
            }
            sb.add(log4jcmd);
        }
        // we add extra java/PA configuration
        if (this.javaOptions != null && !this.javaOptions.trim().isEmpty()) {
            sb.add(this.javaOptions.trim());
        }

        CommandLineBuilder clb = super.getDefaultCommandLineBuilder(getTargetOSObj());

        final boolean deployNodesInDetachedMode = PAResourceManagerProperties.RM_NODES_RECOVERY.getValueAsBoolean() ||
                                                  PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean();
        if (deployNodesInDetachedMode) {
            // if we do not want to kill the nodes when the RM exits or
            // restarts, then we should launch the nodes in background and
            // ignore the RM termination signal
            clb.setDetached();
        }

        clb.setJavaPath(this.javaPath);
        clb.setRmHome(this.schedulingPath);
        clb.setPaProperties(sb);
        final String nodeName = nodeNameBuilder.generateNodeName(hostTracker);
        clb.setNodeName(nodeName);
        clb.setNumberOfNodes(nbNodes);

        // set the stratup script retrieved from NodeCommandLine.properties
        if (!this.deploymentMode.equals("autoGenerated")) {
            clb.setDeploymentMode(deploymentMode);
            clb.setStartupScript((deploymentMode.equals("useStartupScript") ? startupScriptStandard
                                                                            : startupScriptWithNodeJarDownload));
        }

        if (this.deploymentMode.equals("useNodeJarStartupScript")) {
            clb.setNodeJarUrl(nodeJarUrl);
        }

        // finally, the credential's value

        String credString;
        try {
            Client currentClient = super.nodeSource.getAdministrator();
            credString = new String(currentClient.getCredentials().getBase64());
        } catch (KeyException e) {
            throw new RMException("Could not get base64 credentials", e);
        }
        clb.setCredentialsValueAndNullOthers(credString);

        // add an expected node. every unexpected node will be discarded
        String cmdLine;
        String obfuscatedCmdLine;
        try {
            cmdLine = clb.buildCommandLine(true);
            obfuscatedCmdLine = clb.buildCommandLine(false);
        } catch (IOException e) {
            throw new RMException("Cannot build the " + RMNodeStarter.class.getSimpleName() + "'s command line.", e);
        }

        // one escape the command to make it runnable through ssh
        if (cmdLine.contains("\"")) {
            cmdLine = cmdLine.replaceAll("\"", "\\\\\"");
        }
        final String finalCmdLine = cmdLine;
        // The final addDeployingNode() method will initiate a timeout that
        // will declare node as lost and set the description of the failure
        // with a simplistic message, since there is no way to override this
        // mechanism we consider only 90% of timeout to set custom description
        // in case of failure and still allow global timeout
        final int shorterTimeout = Math.round((90 * super.nodeTimeOut) / 100);

        JSch jsch = new JSch();

        final String msg = "deploy on " + hostTracker.getResolvedAddress();

        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(nodeName, nbNodes);
        depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames, obfuscatedCmdLine, msg, super.nodeTimeOut));
        addTimeouts(depNodeURLs);

        Session session;
        try { // Create ssh session to the hostname
            session = jsch.getSession(this.sshUsername, hostTracker.getResolvedAddress().getHostName(), this.sshPort);
            if (this.sshPassword == null) {
                jsch.addIdentity(this.sshUsername, this.sshPrivateKey, null, null);
            } else {
                session.setPassword(this.sshPassword);
            }
            session.setConfig(this.sshOptions);
            session.connect(shorterTimeout);
        } catch (JSchException e) {
            multipleDeclareDeployingNodeLost(depNodeURLs, "unable to " + msg + "\n" + getStackTraceAsString(e));
            throw new RMException("unable to " + msg, e);
        }

        SSHInfrastructureV2.logger.info("Executing SSH command: '" + finalCmdLine + "'");
        ScheduledExecutorService deployService = Executors.newSingleThreadScheduledExecutor();
        try { // Create ssh channel to run the cmd
            ByteArrayOutputStream baos = new ByteArrayOutputStream(DEFAULT_OUTPUT_BUFFER_LENGTH);
            ChannelExec channel;
            try {
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(finalCmdLine);
                channel.setOutputStream(baos);
                channel.setErrStream(baos);
                channel.connect();
            } catch (JSchException e) {
                multipleDeclareDeployingNodeLost(depNodeURLs, "unable to " + msg + "\n" + getStackTraceAsString(e));
                throw new RMException("unable to " + msg, e);
            }
            final ChannelExec chan = channel;
            Future<Void> deployResult = deployService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    while (!shutDown.get() && !checkAllNodesAreAcquiredAndDo(createdNodeNames, null, null)) {
                        if (anyTimedOut(depNodeURLs)) {
                            throw new IllegalStateException("The upper infrastructure has issued a timeout");
                        }
                        // we check the exit status of the session only in the
                        // case where we link the current process to the one
                        // that spawns the nodes. Otherwise, we let the two
                        // processes live completely independently
                        if (!deployNodesInDetachedMode && chan.getExitStatus() != PROCESS_STILL_RUNNING_VALUE) {
                            throw new IllegalStateException("The jvm process of the node has exited prematurely");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            return null; // we know the cause of this
                            // interruption just exit
                        }
                    }
                    return null; // Victory
                }
            });
            try {
                deployResult.get(shorterTimeout, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                declareLostAndThrow("Unable to " + msg + " due to " + e.getCause(), depNodeURLs, channel, baos, e);
            } catch (InterruptedException e) {
                deployResult.cancel(true);
                declareLostAndThrow("Unable to " + msg + " due to an interruption", depNodeURLs, channel, baos, e);
            } catch (TimeoutException e) {
                deployResult.cancel(true);
                declareLostAndThrow("Unable to " + msg + " due to timeout", depNodeURLs, channel, baos, e);
            } finally {
                channel.disconnect();
            }
        } finally {
            removeTimeouts(depNodeURLs);
            session.disconnect();
            deployService.shutdownNow();
        }
    }

    private void declareLostAndThrow(String errMsg, List<String> nodesUrl, ChannelExec chan, ByteArrayOutputStream baos,
            Exception e) throws RMException {
        String lf = System.lineSeparator();
        StringBuilder sb = new StringBuilder(errMsg);
        sb.append(lf).append(" > Process exit code: ").append(chan.getExitStatus());
        sb.append(lf).append(" > Process output: ").append(lf).append(new String(baos.toByteArray()));
        this.multipleDeclareDeployingNodeLost(nodesUrl, sb.toString());
        throw new RMException(errMsg, e);
    }

    /**
     * Configures the Infrastructure
     *
     * @param parameters
     *            parameters[4] : ssh server port
     *            parameters[5] : ssh username
     *            parameters[6] : ssh password
     *            parameters[7] : ssh private key
     *            parameters[8] : optional ssh options file
     *            parameters[9] : java path on the remote machines
     *            parameters[10] : Scheduling path on remote machines
     *            parameters[11] : target OS' type (Linux, Windows or Cygwin)
     *            parameters[12] : extra java options
     *            parameters[13] : deployment mode
     *            parameters[14] : node.jar URL
     *            parameters[15] : the standard startupScript
     *            parameters[16] : the node.jar startupScript
     * @throws IllegalArgumentException
     *             configuration failed
     */
    @Override
    public void configure(Object... parameters) {
        super.configure(parameters);
        int index = 4;
        if (parameters == null || parameters.length < 12) {
            throw new IllegalArgumentException("Invalid parameters for infrastructure creation");
        }

        try {
            this.sshPort = Integer.parseInt(parameters[index++].toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("A valid port for ssh must be supplied");
        }

        this.sshUsername = parameters[index++].toString();
        if (this.sshUsername == null || this.sshUsername.equals("")) {
            throw new IllegalArgumentException("A valid ssh username must be supplied");
        }

        this.sshPassword = parameters[index++].toString();
        this.sshPrivateKey = (byte[]) parameters[index++];
        if (this.sshPassword.equals("")) {
            if (this.sshPrivateKey.length == 0)
                throw new IllegalArgumentException("If no password a valid private key must be supplied");
            else
                this.sshPassword = null;
        }

        this.sshOptions = new Properties();
        byte[] bytes = (byte[]) parameters[index++];
        if (bytes.length == 0) {
            this.sshOptions.put("StrictHostKeyChecking", "no");
        } else {
            try {
                this.sshOptions.load(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not read ssh options file", e);
            }
        }

        this.javaPath = parameters[index++].toString();
        if (this.javaPath == null || StringUtils.isBlank(this.javaPath)) {
            this.javaPath = Utils.getDefaultJavaPath();
        }

        this.schedulingPath = parameters[index++].toString();
        if (this.schedulingPath == null || StringUtils.isBlank(this.schedulingPath)) {
            this.schedulingPath = PAResourceManagerProperties.RM_HOME.getValueAsString();
        }

        // target OS
        if (parameters[index] == null) {
            throw new IllegalArgumentException("Target OS parameter cannot be null");
        }
        OperatingSystem configuredTargetOs = OperatingSystem.getOperatingSystem(parameters[index++].toString());
        if (configuredTargetOs == null) {
            throw new IllegalArgumentException("Only 'Linux', 'Windows' and 'Cygwin' are valid values for Target OS Property.");
        }
        persistedInfraVariables.put(TARGET_OS_OBJ_KEY, configuredTargetOs);

        this.javaOptions = parameters[index++].toString();

        this.deploymentMode = parameters[index++].toString().trim();
        if (this.deploymentMode.isEmpty()) {
            this.deploymentMode = "autoGenerated";
        } else {
            if (!Arrays.asList("autoGenerated", "useStartupScript", "useNodeJarStartupScript")
                       .contains(this.deploymentMode)) {
                throw new IllegalArgumentException("The value of deployment mode must be autoGenerated, useStartupScript or useNodeJarStartupScript");
            }
        }
        this.nodeJarUrl = parameters[index++].toString();
        this.startupScriptStandard = parameters[index++].toString();
        this.startupScriptWithNodeJarDownload = parameters[index++].toString();
    }

    @Override
    protected void killNodeImpl(final Node node, InetAddress host) {
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    node.getProActiveRuntime().killRT(false);
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
        return "Deploys nodes via SSH with login/password or login/pkey.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s, targetOs: [%s], deploymentMode: [%s]",
                             super.toString(),
                             this.targetOs,
                             this.deploymentMode);
    }

    @Override
    public void shutDown() {
        shutDown.set(true);
    }

    @Override
    protected void initializePersistedInfraVariables() {
        super.initializePersistedInfraVariables();
        persistedInfraVariables.put(TARGET_OS_OBJ_KEY, null);
    }

    // Below are wrapper methods around the runtime variables map

    private OperatingSystem getTargetOSObj() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<OperatingSystem>() {
            @Override
            public OperatingSystem handle() {
                return (OperatingSystem) persistedInfraVariables.get(TARGET_OS_OBJ_KEY);
            }
        });
    }

    @Override
    public Map<Integer, String> getSectionDescriptions() {
        Map<Integer, String> sectionDescriptions = super.getSectionDescriptions();
        sectionDescriptions.put(2, "SSH Configuration");
        return sectionDescriptions;
    }
}
