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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;


public class LocalInfrastructure extends InfrastructureManager {

    public static final int DEFAULT_NODE_NUMBER = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);

    public static final long DEFAULT_TIMEOUT = 30000;

    @Configurable(description = "Absolute path to credentials file\nused to add the node to the Resource Manager", credential = true, sectionSelector = 3)
    private Credentials credentials;

    @Configurable(description = "Maximum number of nodes to\nbe deployed on Resource Manager machine", sectionSelector = 1, important = true)
    private int maxNodes = DEFAULT_NODE_NUMBER;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost", sectionSelector = 3)
    private long nodeTimeout = DEFAULT_TIMEOUT;

    @Configurable(description = "Additional ProActive properties", sectionSelector = 3)
    private String paProperties = "";

    private Map<String, String> meta = new HashMap<>();

    {
        meta.putAll(super.getMeta());
        meta.put(InfrastructureManager.ELASTIC, "true");
    }

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
    private static final String LAST_NODE_STARTED_INDEXES_KEY = "lastNodeStartedIndexes";

    /**
     * Maps containing process executors, associated with their corresponding deployment node urls.
     */
    private transient ConcurrentHashMap<ProcessExecutor, List<String>> processExecutorsToDeploying = new ConcurrentHashMap<>();

    private transient ConcurrentHashMap<String, ProcessExecutor> deployingToProcessExecutors = new ConcurrentHashMap<>();

    /**
     * Maps containing process executors, associated with their corresponding acquired node urls.
     */

    private transient ConcurrentHashMap<ProcessExecutor, List<String>> processExecutorsToAcquired = new ConcurrentHashMap<>();

    private transient ConcurrentHashMap<String, ProcessExecutor> acquiredToProcessExecutors = new ConcurrentHashMap<>();

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
            clb.setCredentialsValueAndNullOthers(getCredentials());
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

            this.processExecutorsToDeploying.put(processExecutor, depNodeURLs);
            for (String deployingNodeUrl : depNodeURLs) {
                this.deployingToProcessExecutors.put(deployingNodeUrl, processExecutor);
            }

            processExecutor.start();

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
                this.processExecutorsToDeploying.remove(processExecutor);
                for (String deployingNodeUrl : depNodeURLs) {
                    this.deployingToProcessExecutors.remove(deployingNodeUrl);
                }
            }
        }
    }

    private String getCredentials() throws KeyException {
        if (this.credentials == null) {
            this.credentials = super.nodeSource.getAdministrator().getCredentials();
        }

        return new String(this.credentials.getBase64());
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
            byte[] possibleCredentials = (byte[]) args[index++];
            if (possibleCredentials != null && possibleCredentials.length > 0) {
                this.credentials = Credentials.getCredentialsBase64(possibleCredentials);
            }
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
        removeNodeAndMaybeKillProcessIfEmpty(deployingToProcessExecutors, processExecutorsToDeploying, pnURL, true, -1);
    }

    private ProcessExecutor removeNodeAndMaybeKillProcessIfEmpty(Map<String, ProcessExecutor> urlToProcess,
            Map<ProcessExecutor, List<String>> processToUrl, String url, boolean killProcessIfEmpty,
            int deploymentIndex) {
        ProcessExecutor executor = urlToProcess.remove(url);
        if (executor != null) {
            List<String> urlsAssociatedWithTheProcess = processToUrl.get(executor);
            urlsAssociatedWithTheProcess.remove(url);
            if (killProcessIfEmpty && urlsAssociatedWithTheProcess.isEmpty()) {
                processToUrl.remove(executor);
                executor.killProcess();
                if (deploymentIndex >= 0) {
                    removeIndexWithLockAndPersist(deploymentIndex);
                }
            } else if (urlsAssociatedWithTheProcess.isEmpty()) {
                processToUrl.remove(executor);
            }
        }
        return executor;
    }

    @Override
    protected void notifyAcquiredNode(Node node) throws RMException {
        incrementNumberOfAcquiredNodesWithLockAndPersist();
        String deployingNodeURL = this.buildDeployingNodeURL(node.getNodeInformation().getName());
        ProcessExecutor associatedProcess = removeNodeAndMaybeKillProcessIfEmpty(deployingToProcessExecutors,
                                                                                 processExecutorsToDeploying,
                                                                                 deployingNodeURL,
                                                                                 false,
                                                                                 -1);
        acquiredToProcessExecutors.put(node.getNodeInformation().getURL(), associatedProcess);
        List<String> nodesUrls = processExecutorsToAcquired.putIfAbsent(associatedProcess,
                                                                        Lists.newArrayList(node.getNodeInformation()
                                                                                               .getURL()));
        if (nodesUrls != null) {
            nodesUrls.add(node.getNodeInformation().getURL());
        }
    }

    @Override
    public void removeNode(Node node) throws RMException {
        logger.info("The node " + node.getNodeInformation().getURL() + " is removed from " +
                    this.getClass().getSimpleName());
        removeNodeAndShutdownProcessIfNeeded(node.getNodeInformation().getURL(), extractDeploymentIndex(node));
    }

    private int extractDeploymentIndex(Node node) {
        try {
            String name = node.getNodeInformation().getName();
            String prefix = "local-" + this.nodeSource.getName() + "-";
            name = name.replace(prefix, "");
            if (name.contains("_")) {
                name = name.substring(0, name.indexOf('_'));
            }
            return Integer.parseInt(name);
        } catch (Exception e) {
            logger.warn("Could node extract index from node " + node, e);
            return -1;
        }
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
        for (ProcessExecutor processExecutor : this.processExecutorsToDeploying.keySet()) {
            if (processExecutor != null) {
                processExecutor.killProcess();
            }
        }
        this.processExecutorsToDeploying.clear();
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
        this.persistedInfraVariables.put(LAST_NODE_STARTED_INDEXES_KEY, new TreeSet<>());
    }

    private void removeNodeAndShutdownProcessIfNeeded(String nodeUrlToRemove, int deploymentIndex) {

        // Update handle & acquire/lost nodes persistent counters
        decrementNumberOfHandledNodesWithLockAndPersist();
        if (nodeSource.getNodeInDeployingOrLostNodes(nodeUrlToRemove) == null) {
            decrementNumberOfAcquiredNodesWithLockAndPersist();
        } else {
            decrementNumberOfLostNodesWithLockAndPersist();
        }

        if (acquiredToProcessExecutors.containsKey(nodeUrlToRemove)) {
            removeNodeAndMaybeKillProcessIfEmpty(acquiredToProcessExecutors,
                                                 processExecutorsToAcquired,
                                                 nodeUrlToRemove,
                                                 true,
                                                 deploymentIndex);
        } else if (deployingToProcessExecutors.containsKey(nodeUrlToRemove)) {
            removeNodeAndMaybeKillProcessIfEmpty(deployingToProcessExecutors,
                                                 processExecutorsToDeploying,
                                                 nodeUrlToRemove,
                                                 true,
                                                 deploymentIndex);
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
            // This method aims at finding the first integer index not already used by a current deployment
            // The algorithm is the following:
            // set deployedNodeIndexes = [all currently used indexes]
            // set allIndexes = [0..max(deployedNodeIndexes)+1]
            // set availableIndexes = allIndexes - deployedNodeIndexes
            // newIndex = min(availableIntegers)
            TreeSet<Integer> deployedNodeIndexes = (TreeSet<Integer>) this.persistedInfraVariables.get(LAST_NODE_STARTED_INDEXES_KEY);
            int maxIndex;
            if (deployedNodeIndexes.isEmpty()) {
                maxIndex = -1;
            } else {
                maxIndex = deployedNodeIndexes.last();
            }
            ContiguousSet<Integer> allIndexes = ContiguousSet.create(Range.closed(0, maxIndex + 1),
                                                                     DiscreteDomain.integers());
            TreeSet<Integer> availableIndexes = new TreeSet(Sets.difference(allIndexes, deployedNodeIndexes));
            int newIndex = availableIndexes.first();
            deployedNodeIndexes.add(newIndex);
            this.persistedInfraVariables.put(LAST_NODE_STARTED_INDEXES_KEY, deployedNodeIndexes);
            return newIndex;
        });
    }

    private void removeIndexWithLockAndPersist(int indexToRemove) {
        setPersistedInfraVariable(() -> {
            TreeSet<Integer> deployedNodeIndexes = (TreeSet<Integer>) this.persistedInfraVariables.get(LAST_NODE_STARTED_INDEXES_KEY);
            deployedNodeIndexes.remove(indexToRemove);
            this.persistedInfraVariables.put(LAST_NODE_STARTED_INDEXES_KEY, deployedNodeIndexes);
            return deployedNodeIndexes;
        });
    }

    private int getDifferenceBetweenNumberOfHandledAndAcquiredNodesWithLock() {
        return getPersistedInfraVariable(() -> (int) this.persistedInfraVariables.get(NB_HANDLED_NODES_KEY) -
                                               (int) this.persistedInfraVariables.get(NB_ACQUIRED_NODES_KEY));
    }

    @Override
    public Map<Integer, String> getSectionDescriptions() {
        Map<Integer, String> sectionDescriptions = super.getSectionDescriptions();
        sectionDescriptions.put(1, "Deployment Configuration");
        sectionDescriptions.put(3, "Node Configuration");
        return sectionDescriptions;
    }

    @Override
    public Map<String, String> getMeta() {
        return meta;
    }
}
