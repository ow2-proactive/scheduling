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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public static final long DEFAULT_TIMEOUT = 30000;

    @Configurable(description = "Absolute path to credentials file\nused to add the node to the Resource Manager", credential = true)
    private Credentials credentials;

    @Configurable(description = "Maximum number of nodes to\nbe deployed on Resource Manager machine")
    private int maxNodes = DEFAULT_NODE_NUMBER;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    private long nodeTimeout = DEFAULT_TIMEOUT;

    @Configurable(description = "Additional ProActive properties")
    private String paProperties = "";

    /**
     * key to retrieve the number of nodes which are acquired in the persisted
     * infrastructure variables
     */
    private static final String NB_ACQUIRED_NODES_KEY = "nbAcquiredNodes";

    private static final String NB_LOST_NODES_KEY = "nbLostNodes";

    private static final String NB_HANDLED_NODES_KEY = "nbHandledNodes";

    /**
     * Index of deployment, if startNodesOnTheFly is called multiple times, each time a new process will be created.
     * The index is used to prevent conflicts in nodes urls
     */
    private static final String INDEX_KEY = "index";

    /**
     * A map containing process executors, associated with their corresponding deployment node urls.
     */
    private transient Map<ProcessExecutor, List<String>> processExecutors = new ConcurrentHashMap<>();

    public LocalInfrastructure() {
    }

    @Override
    public String getDescription() {
        return "Deploys nodes on Resource Manager's machine";
    }

    @Override
    public void acquireAllNodes() {
        readLock.lock();
        try {
            logger.info("Starting node acquisition. Acquired nodes=" + getAcquiredNodes() + ", Handled nodes=" +
                        getHandledNodes());
            if (getAcquiredNodes() < getHandledNodes()) {
                int differenceBetweenHandledAndAcquiredNodes = getDifferenceBetweenHandledAndAcquiredNodes();
                logger.info("Starting " + differenceBetweenHandledAndAcquiredNodes + " nodes");
                startNodes(differenceBetweenHandledAndAcquiredNodes);
            }
        } catch (RuntimeException e) {
            logger.error("Could not start nodes of local infrastructure " + nodeSource.getName(), e);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void acquireNode() {
        startNodesOnTheFly(1);
    }

    @Override
    public void acquireNodes(int n, Map<String, ?> nodeConfiguration) {
        startNodesOnTheFly(n);
    }

    private void startNodes(final int n) {
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                LocalInfrastructure.this.startNodeProcess(n);
            }
        });
    }

    private void startNodesOnTheFly(final int n) {
        this.nodeSource.executeInParallel(new Runnable() {
            @Override
            public void run() {
                LocalInfrastructure.this.startNodeProcessOnTheFly(n);
            }
        });
    }

    private void startNodeProcess(int numberOfNodes) {
        int currentIndex = getIndexAndIncrement();
        String baseNodeName = "local-" + this.nodeSource.getName() + "-" + currentIndex;
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
        clb.setNumberOfNodes(numberOfNodes);
        try {
            clb.setCredentialsValueAndNullOthers(new String(this.credentials.getBase64()));
        } catch (KeyException e) {
            createLostNodes(baseNodeName, numberOfNodes, "Cannot decrypt credentials value", e);
            return;
        }
        List<String> cmd;
        try {
            cmd = clb.buildCommandLineAsList(false);
        } catch (IOException e) {
            createLostNodes(baseNodeName, numberOfNodes, "Cannot build command line", e);
            return;
        }

        // The printed cmd with obfuscated credentials
        final String obfuscatedCmd = Joiner.on(' ').join(cmd);

        List<String> depNodeURLs = new ArrayList<>(numberOfNodes);
        final List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseNodeName, numberOfNodes);
        ProcessExecutor processExecutor = null;
        try {
            depNodeURLs.addAll(addMultipleDeployingNodes(createdNodeNames,
                                                         obfuscatedCmd,
                                                         "Node launched locally",
                                                         this.nodeTimeout));

            // Deobfuscate the cred value
            Collections.replaceAll(cmd, CommandLineBuilder.OBFUSC, clb.getCredentialsValue());

            processExecutor = new ProcessExecutor(baseNodeName, cmd, false, true);
            processExecutor.start();

            processExecutors.put(processExecutor, depNodeURLs);

            final ProcessExecutor tmpProcessExecutor = processExecutor;

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (tmpProcessExecutor != null && !tmpProcessExecutor.isProcessFinished()) {
                        tmpProcessExecutor.killProcess();
                    }
                }
            }));

            logger.info("Local Nodes command started : " + obfuscatedCmd);

        } catch (IOException e) {
            String lf = System.lineSeparator();
            String mess = "Cannot launch rm node " + baseNodeName + lf + Throwables.getStackTraceAsString(e);
            multipleDeclareDeployingNodeLost(depNodeURLs, mess);
            if (processExecutor != null) {
                cleanProcess(processExecutor);
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

        if (allNodesLost(numberOfNodes)) {
            // clean up the process
            cleanProcess(processExecutor);
        }
    }

    private void startNodeProcessOnTheFly(int numberOfNodes) {
        addHandledNodes(numberOfNodes);
        startNodeProcess(numberOfNodes);
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
     * @param numberOfNodes
     * @param message
     *            a message
     * @param e
     */
    private void createLostNodes(String baseName, int numberOfNodes, String message, Throwable e) {
        List<String> createdNodeNames = RMNodeStarter.getWorkersNodeNames(baseName, numberOfNodes);
        for (int nodeIndex = 0; nodeIndex < numberOfNodes; nodeIndex++) {
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

    private boolean allNodesLost(int numberOfNodes) {
        return getLostNodes() == numberOfNodes;
    }

    private void cleanProcess(ProcessExecutor processExecutor) {
        if (processExecutor != null) {
            processExecutor.killProcess();
            processExecutors.remove(processExecutor);
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
            persistedInfraVariables.put(NB_HANDLED_NODES_KEY, maxNodes);
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
        logger.debug("A node is removed " + node.getNodeInformation().getURL() + " from " +
                     this.getClass().getSimpleName());

        // the node was removed intentionally, so we decrement the number of handled nodes
        decrementHandledNodes();
        // and the number of acquired nodes is decremented too
        removeAcquiredNodesAndShutDownIfNeeded();
    }

    @Override
    public void notifyDownNode(String nodeName, String nodeUrl, Node node) {
        logger.debug("A down node is removed: " + nodeUrl + " from " + this.getClass().getSimpleName());
        removeAcquiredNodesAndShutDownIfNeeded();
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        incrementAcquiredNodes();
    }

    @Override
    public void shutDown() {
        for (ProcessExecutor processExecutor : processExecutors.keySet()) {
            processExecutor.killProcess();
        }
        processExecutors.clear();
        // do not set processExecutor to null here or NPE can appear in the startProcess method, running in a different thread.
        logger.info("Process associated with node source " + nodeSource.getName() + " destroyed");
    }

    @Override
    public String toString() {
        return "Local Infrastructure";
    }

    @Override
    protected void initializePersistedInfraVariables() {
        persistedInfraVariables.put(NB_ACQUIRED_NODES_KEY, 0);
        persistedInfraVariables.put(NB_LOST_NODES_KEY, 0);
        persistedInfraVariables.put(NB_HANDLED_NODES_KEY, 0);
        persistedInfraVariables.put(INDEX_KEY, 0);
    }

    private void removeAcquiredNodesAndShutDownIfNeeded() {
        int remainingNodesCount = decrementAndGetAcquiredNodes();
        // if there is no remaining node, kill the JVM process
        if (remainingNodesCount == 0) {
            shutDown();
        }
    }

    // Below are wrapper methods around the runtime variables map

    private int getAcquiredNodes() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY);
            }
        });
    }

    private int getLostNodes() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) persistedInfraVariables.get(NB_LOST_NODES_KEY);
            }
        });
    }

    private void incrementAcquiredNodes() {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY) + 1;
                persistedInfraVariables.put(NB_ACQUIRED_NODES_KEY, updated);
                return null;
            }
        });
    }

    private void incrementLostNodes() {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) persistedInfraVariables.get(NB_LOST_NODES_KEY) + 1;
                persistedInfraVariables.put(NB_LOST_NODES_KEY, updated);
                return null;
            }
        });
    }

    private int decrementAndGetAcquiredNodes() {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                int updated = (int) persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY) - 1;
                persistedInfraVariables.put(NB_ACQUIRED_NODES_KEY, updated);
                return updated;
            }
        });
    }

    private int getHandledNodes() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) persistedInfraVariables.get(NB_HANDLED_NODES_KEY);
            }
        });
    }

    private void decrementHandledNodes() {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) persistedInfraVariables.get(NB_HANDLED_NODES_KEY) - 1;
                persistedInfraVariables.put(NB_HANDLED_NODES_KEY, updated);
                return null;
            }
        });
    }

    private void addHandledNodes(final int numberOfNodes) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = (int) persistedInfraVariables.get(NB_HANDLED_NODES_KEY) + numberOfNodes;
                persistedInfraVariables.put(NB_HANDLED_NODES_KEY, updated);
                return null;
            }
        });
    }

    private int getIndexAndIncrement() {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                int deployedNodeIndex = (int) persistedInfraVariables.get(INDEX_KEY);
                persistedInfraVariables.put(INDEX_KEY, deployedNodeIndex + 1);
                return deployedNodeIndex;
            }
        });
    }

    public int getDifferenceBetweenHandledAndAcquiredNodes() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return (int) persistedInfraVariables.get(NB_HANDLED_NODES_KEY) -
                       (int) persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY);
            }
        });
    }
}
