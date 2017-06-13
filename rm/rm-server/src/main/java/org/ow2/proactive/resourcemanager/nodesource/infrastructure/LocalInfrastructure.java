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

import java.io.IOException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.utils.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;


public class LocalInfrastructure extends InfrastructureManager {

    public static final int DEFAULT_NODE_NUMBER = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);

    public static final int DEFAULT_TIMEOUT = 30000;

    @Configurable(description = "Absolute path to credentials file\nused to add the node to the Resource Manager", credential = true)
    private Credentials credentials;

    @Configurable(description = "Maximum number of nodes to\nbe deployed on Resource Manager machine")
    private int maxNodes = DEFAULT_NODE_NUMBER;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    private int nodeTimeout = DEFAULT_TIMEOUT;

    @Configurable(description = "Additional ProActive properties")
    private String paProperties = "";

    // key to retrieve the number of nodes which can still be acquired
    private static final String NB_ACQUIRED_NODES_KEY = "nbAcquiredNodes";

    private static final String NB_LOST_NODES_KEY = "nbLostNodes";

    private static final String NB_HANDLED_NODES_KEY = "nbHandledNodes";

    private static final String COMMAND_LINE_STARTED_KEY = "commandLineStarted";

    // FIX-ME the process is not saved in the runtimeVariables. In case the infrastructure is recreated from the
    // persisted runtime variables, we need to handle the process manually.
    private transient ProcessExecutor processExecutor;

    public LocalInfrastructure() {
    }

    @Override
    public String getDescription() {
        return "Deploys nodes on Resource Manager's machine";
    }

    @Override
    public void acquireAllNodes() {
        this.acquireNode();
    }

    @Override
    public void acquireNode() {
        if (compareAndSetCommandLineStarted(false, true)) {
            this.nodeSource.executeInParallel(new Runnable() {
                public void run() {
                    LocalInfrastructure.this.startNodeProcess();
                }
            });
        } else {
            logger.debug("Cannot acquire more nodes");
        }
    }

    private void startNodeProcess() {
        reinitializeAcquiredAndLostNodes();

        String baseNodeName = "local-" + this.nodeSource.getName();
        OperatingSystem os = OperatingSystem.UNIX;
        // assuming no cygwin, windows or the "others"...
        if (System.getProperty("os.name").contains("Windows")) {
            os = OperatingSystem.WINDOWS;
        }
        String rmHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
        if (!rmHome.endsWith(os.fs)) {
            rmHome += os.fs;
        }
        CommandLineBuilder clb = this.getDefaultCommandLineBuilder(os);
        // RM_Home set in bin/unix/env script
        clb.setRmHome(rmHome);
        ArrayList<String> paPropList = new ArrayList<>();
        if (!this.paProperties.contains(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getName())) {
            paPropList.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + rmHome + "config" + os.fs +
                           "security.java.policy-client");
        }
        if (!this.paProperties.contains(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName())) {
            paPropList.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() + rmHome + "config" + os.fs +
                           "network" + os.fs + "node.ini");
        }
        if (!this.paProperties.contains(PAResourceManagerProperties.RM_HOME.getKey())) {
            paPropList.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + rmHome);
        }
        if (!this.paProperties.contains("java.library.path")) {
            paPropList.add("-Djava.library.path=" + System.getProperty("java.library.path"));
        }
        if (!paProperties.isEmpty()) {
            Collections.addAll(paPropList, this.paProperties.split(" "));
        }
        clb.setPaProperties(paPropList);
        clb.setNodeName(baseNodeName);
        clb.setNumberOfNodes(getHandledNodes());
        try {
            clb.setCredentialsValueAndNullOthers(new String(this.credentials.getBase64()));
        } catch (KeyException e) {
            createLostNodes(baseNodeName, "Cannot decrypt credentials value", e);
            return;
        }
        List<String> cmd;
        try {
            cmd = clb.buildCommandLineAsList(false);
        } catch (IOException e) {
            createLostNodes(baseNodeName, "Cannot build command line", e);
            return;
        }

        // The printed cmd with obfuscated credentials
        final String obfuscatedCmd = Joiner.on(' ').join(cmd);

        List<String> depNodeURLs = new ArrayList<>(getHandledNodes());
        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseNodeName, getHandledNodes());
        try {
            depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames,
                                                         obfuscatedCmd,
                                                         "Node launched locally",
                                                         this.nodeTimeout));

            // Deobfuscate the cred value
            Collections.replaceAll(cmd, CommandLineBuilder.OBFUSC, clb.getCredentialsValue());

            processExecutor = new ProcessExecutor(baseNodeName, cmd, false, true);
            processExecutor.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (processExecutor != null && !processExecutor.isProcessFinished()) {
                        processExecutor.killProcess();
                    }
                }
            }));

            logger.info("Local Nodes command started : " + obfuscatedCmd);

        } catch (IOException e) {
            String lf = System.lineSeparator();
            String mess = "Cannot launch rm node " + baseNodeName + lf + Throwables.getStackTraceAsString(e);
            multipleDeclareDeployingNodeLost(depNodeURLs, mess);
            if (processExecutor != null) {
                cleanProcess();
            }
            return;
        }

        // watching process
        int threshold = 10;
        while (!allNodesAcquiredOrLost()) {
            if (processExecutor.isProcessFinished()) {
                int exit = processExecutor.getExitCode();
                if (exit != 0) {
                    String lf = System.lineSeparator();
                    String message = "RMNode exit code == " + exit + lf;
                    message += "Command: " + obfuscatedCmd + lf;
                    String out = Joiner.on('\n').join(processExecutor.getOutput());
                    String err = Joiner.on('\n').join(processExecutor.getErrorOutput());
                    message += "stdout: " + out + lf + "stderr: " + err;
                    multipleDeclareDeployingNodeLost(depNodeURLs, message);
                }
            } else {
                logger.debug("Waiting for nodes " + baseNodeName + " acquisition");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for local process status", e);
                threshold--;
                if (threshold <= 0) {
                    break;
                }
            }
        }

        logger.debug("Local Infrastructure manager exits watching loop for nodes " + baseNodeName);
        logNodeOutput(baseNodeName + " stdout: ", processExecutor.getOutput());
        logNodeOutput(baseNodeName + " stderr: ", processExecutor.getErrorOutput());

        if (allNodesLost()) {
            // clean up the process
            cleanProcess();
        }
    }

    private void logNodeOutput(final String prefix, List<String> nodeOutputLines) {
        if (nodeOutputLines != null) {
            for (String processOutputLine : nodeOutputLines) {
                logger.debug(prefix + processOutputLine);
            }
        }
    }

    /**
     * Creates a lost node. The deployment has failed while building the command
     * line
     * 
     * @param message
     *            a message
     * @param e
     *            the cause
     */
    private void createLostNodes(String baseName, String message, Throwable e) {
        List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseName, getHandledNodes());
        for (int nodeIndex = 0; nodeIndex < getHandledNodes(); nodeIndex++) {
            String name = createdNodeNames.get(nodeIndex);
            String lf = System.lineSeparator();
            String url = super.addDeployingNode(name,
                                                "deployed as daemon",
                                                "Deploying a local infrastructure node",
                                                this.nodeTimeout);
            String st = Throwables.getStackTraceAsString(e);
            super.declareDeployingNodeLost(url, message + lf + st);
        }
    }

    private boolean allNodesAcquiredOrLost() {
        return (getAcquiredNodes() + getLostNodes()) == getHandledNodes();
    }

    private boolean allNodesLost() {
        return getLostNodes() == getHandledNodes();
    }

    private void cleanProcess() {
        if (processExecutor != null) {
            processExecutor.killProcess();
            setCommandLineStarted(false);
            processExecutor = null;
        }
    }

    /**
     * args[0] = credentials args[1] = max nodes args[2] = timeout args[3] = pa
     * props
     */
    @Override
    protected void configure(Object... args) {
        int index = 0;
        try {
            this.credentials = Credentials.getCredentialsBase64((byte[]) args[index++]);
        } catch (KeyException e1) {
            throw new IllegalArgumentException("Cannot decrypt credentials", e1);
        }

        try {
            this.maxNodes = Integer.parseInt(args[index++].toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot determine max node");
        }

        try {
            this.nodeTimeout = Integer.parseInt(args[index++].toString());
        } catch (Exception e) {
            logger.warn("Cannot determine node timeout, using default:" + this.nodeTimeout, e);
        }

        this.paProperties = args[index++].toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        incrementLostNodes();
    }

    @Override
    protected void notifyAcquiredNode(Node arg0) throws RMException {
        incrementAcquiredNodes();
    }

    @Override
    public void removeNode(Node node) throws RMException {
        logger.debug("Removing node " + node.getNodeInformation().getURL() + " from " +
                     this.getClass().getSimpleName());

        if (!this.nodeSource.getDownNodes().contains(node)) {
            // the node was manually removed
            decrementHandledNodes();
        }

        int remainingNodesCount = decrementAndGetAcquiredNodes();
        // If there is no remaining node, kill the JVM process
        if (remainingNodesCount == 0 && getCommandLineStarted()) {
            shutDown();
        }
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        incrementAcquiredNodes();
    }

    @Override
    public void shutDown() {
        if (processExecutor != null) {
            processExecutor.killProcess();
        }
        setCommandLineStarted(false);
        // do not set processExecutor to null here or NPE can appear in the startProcess method, running in a different thread.
        logger.info("Process associated with node source " + nodeSource.getName() + " destroyed");
    }

    @Override
    public String toString() {
        return "Local Infrastructure";
    }

    @Override
    protected void initializeRuntimeVariables() {
        runtimeVariables.put(NB_ACQUIRED_NODES_KEY, 0);
        runtimeVariables.put(NB_LOST_NODES_KEY, 0);
        runtimeVariables.put(COMMAND_LINE_STARTED_KEY, false);
        runtimeVariables.put(NB_HANDLED_NODES_KEY, maxNodes);
    }

    // Below are wrapper methods around the runtime variables map

    private int getAcquiredNodes() {
        return getRuntimeVariable(new RuntimeVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) runtimeVariables.get(NB_ACQUIRED_NODES_KEY);
            }
        });
    }

    private int getLostNodes() {
        return getRuntimeVariable(new RuntimeVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) runtimeVariables.get(NB_LOST_NODES_KEY);
            }
        });
    }

    private int getHandledNodes() {
        return getRuntimeVariable(new RuntimeVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) runtimeVariables.get(NB_HANDLED_NODES_KEY);
            }
        });
    }

    private void incrementAcquiredNodes() {
        setRuntimeVariable(new RuntimeVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) runtimeVariables.get(NB_ACQUIRED_NODES_KEY) + 1;
                runtimeVariables.put(NB_ACQUIRED_NODES_KEY, updated);
                return null;
            }
        });
    }

    private void incrementLostNodes() {
        setRuntimeVariable(new RuntimeVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) runtimeVariables.get(NB_LOST_NODES_KEY) + 1;
                runtimeVariables.put(NB_LOST_NODES_KEY, updated);
                return null;
            }
        });
    }

    private int decrementAndGetAcquiredNodes() {
        return setRuntimeVariable(new RuntimeVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                int updated = (int) runtimeVariables.get(NB_ACQUIRED_NODES_KEY) - 1;
                runtimeVariables.put(NB_ACQUIRED_NODES_KEY, updated);
                return updated;
            }
        });
    }

    private void decrementHandledNodes() {
        setRuntimeVariable(new RuntimeVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) runtimeVariables.get(NB_HANDLED_NODES_KEY) - 1;
                runtimeVariables.put(NB_HANDLED_NODES_KEY, updated);
                return null;
            }
        });
    }

    private void reinitializeAcquiredAndLostNodes() {
        setRuntimeVariable(new RuntimeVariablesHandler<Void>() {
            @Override
            public Void handle() {
                runtimeVariables.put(NB_ACQUIRED_NODES_KEY, 0);
                runtimeVariables.put(NB_LOST_NODES_KEY, 0);
                return null;
            }
        });
    }

    private boolean getCommandLineStarted() {
        return getRuntimeVariable(new RuntimeVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return (boolean) runtimeVariables.get(COMMAND_LINE_STARTED_KEY);
            }
        });
    }

    private void setCommandLineStarted(final boolean isCommandLineStarted) {
        setRuntimeVariable(new RuntimeVariablesHandler<Void>() {
            @Override
            public Void handle() {
                runtimeVariables.put(COMMAND_LINE_STARTED_KEY, isCommandLineStarted);
                return null;
            }
        });
    }

    private boolean compareAndSetCommandLineStarted(final boolean expected, final boolean updated) {
        return setRuntimeVariable(new RuntimeVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                boolean commandLineStarted = (boolean) runtimeVariables.get(COMMAND_LINE_STARTED_KEY);
                if (commandLineStarted == expected) {
                    runtimeVariables.put(COMMAND_LINE_STARTED_KEY, updated);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

}
