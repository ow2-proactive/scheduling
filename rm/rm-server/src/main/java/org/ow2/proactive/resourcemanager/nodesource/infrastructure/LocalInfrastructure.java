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
import java.util.Iterator;
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
     * Index of deployment, if startNodes is called multiple times, each time a new process will be created.
     * The index is used to prevent conflicts in nodes urls
     */
    private static final String LAST_NODE_STARTED_INDEX_KEY = "lastNodeStartedIndex";

    /**
     * A map containing process executors, associated with their corresponding deployment node urls.
     */
    private transient ConcurrentHashMap<ProcessExecutor, List<String>> processExecutors = new ConcurrentHashMap<>();

    @Override
    public String getDescription() {
        return "Deploys nodes on Resource Manager's machine";
    }

    @Override
    public void acquireAllNodes() {
        this.readLock.lock();
        try {
            // Check if we need to handle more nodes (we want to reach the max)
            int amountOfNewNodesToHandle = 0;
            int differenceBetweenHandledAndMaxNodes = maxNodes - getNumberOfHandledNodesWithLock();
            if (differenceBetweenHandledAndMaxNodes > 0) {
                amountOfNewNodesToHandle = differenceBetweenHandledAndMaxNodes;
            }

            // Check if some current *and future* handled nodes are not acquired (lost or new) and acquire them
            int differenceBetweenHandledAndAcquiredNodes = getDifferenceBetweenNumberOfHandledAndAcquiredNodesWithLock() +
                                                           amountOfNewNodesToHandle;
            if (differenceBetweenHandledAndAcquiredNodes > 0) {
                logger.info("Starting " + differenceBetweenHandledAndAcquiredNodes + " nodes");
                startNodes(differenceBetweenHandledAndAcquiredNodes);
            }
        } catch (RuntimeException e) {
            logger.error("Could not start nodes of local infrastructure " + this.nodeSource.getName(), e);
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void acquireNode() {
        if (maxNodes - getNumberOfAcquiredNodesWithLock() > 0) {
            logger.info("Starting a node from " + this.getClass().getSimpleName());
            startNodes(1);
        }
    }

    @Override
    public void acquireNodes(int numberOfNodes, Map<String, ?> nodeConfiguration) {
        if (numberOfNodes > 0 && (maxNodes - (getNumberOfAcquiredNodesWithLock() + numberOfNodes)) >= 0) {
            logger.info("Starting " + numberOfNodes + " nodes from " + this.getClass().getSimpleName());
            startNodes(numberOfNodes);
        }
    }

    private void startNodes(final int numberOfNodes) {
        this.nodeSource.executeInParallel(() -> {
            increaseNumberOfHandledNodesWithLockAndPersist(numberOfNodes);
            LocalInfrastructure.this.startNodeProcess(numberOfNodes);
        });
    }

    private void startNodeProcess(int numberOfNodes) {
        logger.debug("Starting a new process to acquire " + numberOfNodes + " nodes");
        int currentIndex = getIndexAndIncrementWithLockAndPersist();
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
        if (!this.paProperties.isEmpty()) {
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

            this.processExecutors.put(processExecutor, depNodeURLs);

            final ProcessExecutor tmpProcessExecutor = processExecutor;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (tmpProcessExecutor != null && !tmpProcessExecutor.isProcessFinished()) {
                    tmpProcessExecutor.killProcess();
                }
            }));

            logger.debug("Local Nodes command started : " + obfuscatedCmd);

        } catch (IOException e) {
            String lf = System.lineSeparator();
            String mess = "Cannot launch rm node " + baseNodeName + lf + Throwables.getStackTraceAsString(e);
            multipleDeclareDeployingNodeLost(depNodeURLs, mess);
            if (processExecutor != null) {
                processExecutor.killProcess();
                this.processExecutors.remove(processExecutor);
            }
        }
    }

    /**
     * Creates a lost node to indicate that the deployment has failed while
     * building the command line.
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
            this.persistedInfraVariables.put(NB_HANDLED_NODES_KEY, 0);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot determine max node");
        }

        try {
            this.nodeTimeout = Integer.parseInt(args[index++].toString());
        } catch (Exception e) {
            logger.warn("Cannot determine node timeout, using default:" + this.nodeTimeout, e);
        }

        this.paProperties = args[index].toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        incrementNumberOfLostNodesWithLockAndPersist();
    }

    @Override
    protected void notifyAcquiredNode(Node arg0) throws RMException {
        incrementNumberOfAcquiredNodesWithLockAndPersist();
    }

    @Override
    public void removeNode(Node node) throws RMException {
        logger.info("The node " + node.getNodeInformation().getURL() + " is removed from " +
                    this.getClass().getSimpleName());
        removeNodeAndShutdownProcessIfNeeded(node.getNodeInformation().getURL());
    }

    @Override
    public void notifyDownNode(String nodeName, String nodeUrl, Node node) {
        logger.debug("A down node is detected: " + nodeUrl + " from " + this.getClass().getSimpleName());
        decrementNumberOfAcquiredNodesWithLockAndPersist();
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        incrementNumberOfAcquiredNodesWithLockAndPersist();
    }

    @Override
    public void shutDown() {
        for (ProcessExecutor processExecutor : this.processExecutors.keySet()) {
            if (processExecutor != null) {
                processExecutor.killProcess();
            }
        }
        this.processExecutors.clear();
        // do not set processExecutor to null here or NPE can appear in the startProcess method, running in a different thread.
        logger.info("All process associated with node source '" + this.nodeSource.getName() + "' are being destroyed.");
    }

    @Override
    public String toString() {
        return "Local Infrastructure";
    }

    @Override
    protected void initializePersistedInfraVariables() {
        this.persistedInfraVariables.put(NB_ACQUIRED_NODES_KEY, 0);
        this.persistedInfraVariables.put(NB_LOST_NODES_KEY, 0);
        this.persistedInfraVariables.put(NB_HANDLED_NODES_KEY, 0);
        this.persistedInfraVariables.put(LAST_NODE_STARTED_INDEX_KEY, 0);
    }

    private void removeNodeAndShutdownProcessIfNeeded(String nodeUrlToRemove) {

        // Update handle & acquire/lost nodes persistent counters
        decrementNumberOfHandledNodesWithLockAndPersist();
        if (nodeSource.getNodeInDeployingOrLostNodes(nodeUrlToRemove) == null) {
            decrementNumberOfAcquiredNodesWithLockAndPersist();
        } else {
            decrementNumberOfLostNodesWithLockAndPersist();
        }

        Iterator<Map.Entry<ProcessExecutor, List<String>>> processIterator = processExecutors.entrySet().iterator();
        while (processIterator.hasNext()) {
            Map.Entry<ProcessExecutor, List<String>> processExecutor = processIterator.next();
            // Remove the nodeUrl if present
            Iterator<String> nodesIterator = processExecutor.getValue().iterator();
            boolean nodeFound = false;
            while (nodesIterator.hasNext()) {
                String nodeUrl = nodesIterator.next();
                String nodeName = nodeUrl.substring(nodeUrl.lastIndexOf('/'));
                if (nodeUrlToRemove.endsWith(nodeName)) {
                    nodeFound = true;
                    nodesIterator.remove();
                    break;
                }
            }
            // Kill the associated JVM process if it doesn't have remaining node
            if (nodeFound) {
                if (processExecutor.getValue().isEmpty()) {
                    logger.debug("No nodes remaining after deleting node " + nodeUrlToRemove +
                                 ", killing process from " + this.getClass().getSimpleName());
                    if (processExecutor.getKey() != null) {
                        processExecutor.getKey().killProcess();
                    }
                    processIterator.remove();
                }
                break;
            }
        }
    }

    // Below are wrapper methods around the runtime variables map

    private int getNumberOfHandledNodesWithLock() {
        return getPersistedInfraVariable(() -> (int) this.persistedInfraVariables.get(NB_HANDLED_NODES_KEY));
    }

    private int getNumberOfAcquiredNodesWithLock() {
        return getPersistedInfraVariable(() -> (int) this.persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY));
    }

    private void increaseNumberOfHandledNodesWithLockAndPersist(final int additionalNumberOfNodes) {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NB_HANDLED_NODES_KEY) + additionalNumberOfNodes;
            this.persistedInfraVariables.put(NB_HANDLED_NODES_KEY, updated);
            return updated;
        });
    }

    private void incrementNumberOfAcquiredNodesWithLockAndPersist() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY) + 1;
            this.persistedInfraVariables.put(NB_ACQUIRED_NODES_KEY, updated);
            return updated;
        });
    }

    private void incrementNumberOfLostNodesWithLockAndPersist() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NB_LOST_NODES_KEY) + 1;
            this.persistedInfraVariables.put(NB_LOST_NODES_KEY, updated);
            return updated;
        });
    }

    private void decrementNumberOfHandledNodesWithLockAndPersist() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NB_HANDLED_NODES_KEY) - 1;
            this.persistedInfraVariables.put(NB_HANDLED_NODES_KEY, updated);
            return updated;
        });
    }

    private void decrementNumberOfAcquiredNodesWithLockAndPersist() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY) - 1;
            this.persistedInfraVariables.put(NB_ACQUIRED_NODES_KEY, updated);
            return updated;
        });
    }

    private void decrementNumberOfLostNodesWithLockAndPersist() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NB_LOST_NODES_KEY) - 1;
            this.persistedInfraVariables.put(NB_LOST_NODES_KEY, updated);
            return updated;
        });
    }

    private int getIndexAndIncrementWithLockAndPersist() {
        return setPersistedInfraVariable(() -> {
            int deployedNodeIndex = (int) this.persistedInfraVariables.get(LAST_NODE_STARTED_INDEX_KEY);
            this.persistedInfraVariables.put(LAST_NODE_STARTED_INDEX_KEY, deployedNodeIndex + 1);
            return deployedNodeIndex;
        });
    }

    private int getDifferenceBetweenNumberOfHandledAndAcquiredNodesWithLock() {
        return getPersistedInfraVariable(() -> (int) this.persistedInfraVariables.get(NB_HANDLED_NODES_KEY) -
                                               (int) this.persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY));
    }
}
