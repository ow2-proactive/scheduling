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

import io.github.pixee.security.BoundedLineReader;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * Abstract infrastructure Manager implementation based on hosts list file.
 */
public abstract class HostsFileBasedInfrastructureManager extends InfrastructureManager {

    protected static final Logger logger = Logger.getLogger(HostsFileBasedInfrastructureManager.class);

    protected static final int DEFAULT_NODE_TIMEOUT = 60 * 1000;

    protected static final int DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD = 5;

    protected static final long DEFAULT_WAIT_TIME_BETWEEN_NODE_DEPLOYMENT_FAILURES = 5000;

    @Configurable(fileBrowser = true, description = "Absolute path of the file containing\nthe list of remote hosts", sectionSelector = 1, important = true)
    protected File hostsList;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost", sectionSelector = 3)
    protected int nodeTimeOut = HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT;

    @Configurable(description = "Maximum number of failed attempt to deploy on \na host before discarding it", sectionSelector = 3)
    protected int maxDeploymentFailure = HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD;

    @Configurable(description = "Milliseconds to wait after each failed attempt to deploy on \na host", sectionSelector = 3)
    protected long waitBetweenDeploymentFailures = HostsFileBasedInfrastructureManager.DEFAULT_WAIT_TIME_BETWEEN_NODE_DEPLOYMENT_FAILURES;

    /**
     * Key to retrieve the information about hosts (host tracker per host map)
     * in the persisted infrastructure variable map
     */
    private static final String HOST_TRACKER_PER_HOST_KEY = "hostTrackerPerHostKey";

    /**
     * Key to retrieve the timeout information (timeout flag per node URL map)
     * in the persisted infrastructure variable map
     */
    private static final String PN_TIMEOUT_KEY = "pnTimeout";

    @Override
    public Map<Integer, String> getSectionDescriptions() {
        Map<Integer, String> sectionDescriptions = super.getSectionDescriptions();
        sectionDescriptions.put(1, "Deployment Configuration");
        sectionDescriptions.put(3, "Node Configuration");
        return sectionDescriptions;
    }

    protected NodeNameBuilder nodeNameBuilder = new NodeNameBuilder();

    @Override
    protected void initializePersistedInfraVariables() {
        persistedInfraVariables.put(HOST_TRACKER_PER_HOST_KEY, new HashMap<String, HostTracker>());
        persistedInfraVariables.put(PN_TIMEOUT_KEY, new HashMap<String, Boolean>());
    }

    /**
     * Configures the infrastructure.
     * 	parameters[0] = hosts list file content
     * 	parameters[1] = timeout of the node deployment
     * 	parameters[2] = max deployment failure
     *  parameters[3] = wait time between failures
     */
    @Override
    protected void configure(Object... parameters) {
        if (parameters == null || parameters.length < 4) {
            throw new IllegalArgumentException("Not enough parameter provided to the infrastructure.");
        }
        int index = 0;
        try {
            byte[] bytes = (byte[]) parameters[index++];
            this.hostsList = File.createTempFile("hosts", "list");
            FileToBytesConverter.convertByteArrayToFile(bytes, this.hostsList);
            readHosts(this.hostsList);
            this.hostsList.delete();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read hosts file", e);
        }
        try {
            this.nodeTimeOut = Integer.parseInt(parameters[index++].toString());
        } catch (NumberFormatException e) {
            logger.warn("Number format exception occurred at ns configuration, default acq timeout value set: " +
                        HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT + "ms");
            this.nodeTimeOut = HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT;
        }

        try {
            this.maxDeploymentFailure = Integer.parseInt(parameters[index++].toString());
        } catch (NumberFormatException e) {
            logger.warn("Number format exception occurred at ns configuration, default attemp value set: " +
                        HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD);
            this.maxDeploymentFailure = HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD;
        }

        try {
            this.waitBetweenDeploymentFailures = Integer.parseInt(parameters[index++].toString());
        } catch (NumberFormatException e) {
            logger.warn("Number format exception occurred at ns configuration, default wait time between failures value set: " +
                        HostsFileBasedInfrastructureManager.DEFAULT_WAIT_TIME_BETWEEN_NODE_DEPLOYMENT_FAILURES);
            this.waitBetweenDeploymentFailures = HostsFileBasedInfrastructureManager.DEFAULT_WAIT_TIME_BETWEEN_NODE_DEPLOYMENT_FAILURES;
        }
    }

    /**
     * Internal host file parser
     * <p>
     * File format:
     * one host per line, optionally followed by a space and an integer describing the maximum
     * number of runtimes (1 if not specified). Example:
     * <pre>
     * example.com
     * example.org 5
     * example.net 3
     * </pre>
     * @param f the file from which hosts names are to be extracted
     * @throws IOException parsing failed
     */
    protected void readHosts(File f) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String line = "";

            while ((line = BoundedLineReader.readLine(in, 5_000_000)) != null) {
                if (line == "" || line.trim().length() == 0)
                    continue;

                String[] elts = line.split(" ");
                int configuredNodeNumber = 1;
                if (elts.length > 1) {
                    try {
                        configuredNodeNumber = Integer.parseInt(elts[1]);
                        if (configuredNodeNumber < 1) {
                            throw new IllegalArgumentException("Cannot launch less than one runtime per host.");
                        }
                    } catch (Exception e) {
                        logger.warn("Error while parsing hosts file: " + e.getMessage(), e);
                        configuredNodeNumber = 1;
                    }
                }
                String hostNameInFile = elts[0];
                try {
                    InetAddress reifiedHost = InetAddress.getByName(hostNameInFile);
                    HostTracker hostTracker = new HostTracker(hostNameInFile, configuredNodeNumber, reifiedHost);
                    putHostTrackerForHost(hostNameInFile, hostTracker);
                } catch (UnknownHostException ex) {
                    throw new RuntimeException("Unknown host: " + hostNameInFile, ex);
                }
            }
        }
    }

    /**
     * Check if any host is available and acquire the nodes of available hosts
     */
    @Override
    public void acquireAllNodes() {
        while (nodesNeedToBeDeployed()) {
            acquireNode();
        }
    }

    /**
     * Acquire the nodes of available hosts
     */
    @Override
    public void acquireNode() {
        if (!nodesNeedToBeDeployed()) {
            logger.info("Attempting to acquire nodes while nodes are already deployed on all hosts.");
            return;
        }

        for (Map.Entry<String, HostTracker> hostEntry : getHostTrackerPerHostEntrySetWithLock()) {
            final String host = hostEntry.getKey();
            final HostTracker hostTracker = hostEntry.getValue();

            if (needsNodes(host)) {
                final int neededNodeNumber = getNeededNodesNumberWithLock(host);
                logger.info("Acquiring " + neededNodeNumber + " nodes on host " + hostTracker);

                this.nodeSource.executeInParallel(() -> {
                    try {
                        startNodeImplWithRetries(hostTracker, neededNodeNumber, maxDeploymentFailure);
                    } catch (Exception e) {
                        logger.error("Could not acquire nodes on host " + hostTracker, e);
                    }
                });

                setNeedsNodesWithLockAndPersist(host, false);
            }
        }
    }

    /**
     * This method is called by Infrastructure Manager in case of a deploying node removal.
     * We take advantage of it to specify to the remote process control loop of the removal.
     * This one will then exit.
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        putPnTimeoutWithLockAndPersist(pnURL, Boolean.TRUE);
        String host = nodeNameBuilder.extractHostFromNode(pnURL);
        int lostNodeNotifications = addAndGetLostNodeNotificationWithLockAndPersist(host);
        if (maxDeploymentFailure >= 0 &&
            lostNodeNotifications >= getConfiguredNodeNumberWithLockAndPersist(host) * maxDeploymentFailure) {
            logger.info("Received " + lostNodeNotifications + " LOST Node notifications. New nodes will be required.");
            resetLostNodeNotificationsWithLockAndPersist(host);
            setNeedsNodesWithLockAndPersist(host, true);
        }
    }

    /**
     * Parent IM notifies about a new node registration
     */
    @Override
    protected void notifyAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeHost = node.getVMInformation().getInetAddress();

        String parsedHost = nodeNameBuilder.extractHostFromNode(nodeName);
        putAliveNodeUrlWithLockAndPersist(parsedHost, nodeUrl);
        logger.info("New acquired node " + nodeUrl + " on host " + nodeHost);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        String nodeName = node.getNodeInformation().getName();
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeHost = node.getVMInformation().getInetAddress();

        String parsedHost = nodeNameBuilder.extractHostFromNode(nodeName);
        putRemovedNodeUrlWithLockAndPersist(parsedHost, node.getNodeInformation().getURL());
        logger.info("Removed node " + nodeUrl + " on host " + nodeHost);

        if (!hasAliveNodes(parsedHost)) {
            killNodeProcess(node, nodeHost);
            setNeedsNodesWithLockAndPersist(parsedHost, true);
            logger.info("Host " + nodeHost + " has no more alive nodes. Need nodes flag is set.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDownNode(final String nodeName, final String nodeUrl, final Node node) {
        InetAddress nodeHost = null;
        if (node != null) {
            nodeHost = node.getVMInformation().getInetAddress();
        }
        String parsedHost = nodeNameBuilder.extractHostFromNode(nodeName);
        putDownNodeUrlWithLockAndPersist(parsedHost, nodeUrl);
        logger.info("Down node " + nodeUrl + " on host " + nodeHost);

        if (!hasAliveNodes(parsedHost)) {
            if (node != null) { // the node object can be null in case of a recovery
                killNodeProcess(node, nodeHost);
            }
            setNeedsNodesWithLockAndPersist(parsedHost, true);
            logger.info("Host " + parsedHost + " has no more alive nodes. Need nodes flag is set.");
        }
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        String nodeName = node.getNodeInformation().getName();
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeHost = node.getVMInformation().getInetAddress();

        String parsedHost = nodeNameBuilder.extractHostFromNode(nodeName);
        putAliveNodeUrlWithLockAndPersist(parsedHost, node.getNodeInformation().getURL());
        logger.info("Reconnected node " + nodeUrl + " on host " + nodeHost);
    }

    protected void startNodeImplWithRetries(final HostTracker hostTracker, final int nbNodes, int retries)
            throws RMException {
        while (!isShutDown()) {
            final List<String> depNodeURLs = new ArrayList<>(nbNodes);
            try {
                startNodeImpl(hostTracker, nbNodes, depNodeURLs);
                return;
            } catch (Exception e) {
                logger.warn("Failed nodes deployment in host : " + hostTracker.getResolvedAddress() +
                            ", retries left : " + retries);
                if (isInfiniteRetries(retries) || retries > 0) {
                    removeNodes(depNodeURLs);
                    waitPeriodBeforeRetry();
                    retries = getRetriesLeft(retries);
                } else {
                    logger.error("Tries threshold reached for host " + hostTracker.getResolvedAddress() +
                                 ". This host is not part of the deployment process anymore.");

                    throw e;
                }
            }
        }

    }

    protected boolean anyTimedOut(List<String> nodesUrl) {
        for (String nodeUrl : nodesUrl) {
            if (getPnTimeoutWithLock(nodeUrl)) {
                return true;
            }
        }
        return false;
    }

    protected void removeTimeouts(List<String> nodesUrl) {
        for (String nodeUrl : nodesUrl) {
            removePnTimeoutWithLockAndPersist(nodeUrl);
        }
    }

    protected void addTimeouts(List<String> nodesUrl) {
        for (String pnUrl : nodesUrl) {
            putPnTimeoutWithLockAndPersist(pnUrl, false);
        }
    }

    /**
     * Check whether any host needs nodes to be deployed
     */
    private boolean nodesNeedToBeDeployed() {
        for (Map.Entry<String, HostTracker> entry : getHostTrackerPerHostEntrySetWithLock()) {
            String host = entry.getKey();
            if (needsNodes(host)) {
                return true;
            }
        }
        return false;
    }

    private void killNodeProcess(Node node, InetAddress nodeHost) {
        try {
            killNodeImpl(node, nodeHost);
        } catch (Exception e) {
            logger.trace("An exception occurred during node kill", e);
        }
    }

    private boolean isInfiniteRetries(int retries) {
        return retries == -1;
    }

    private void waitPeriodBeforeRetry() {
        try {

            Thread.sleep(waitBetweenDeploymentFailures);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    private int getRetriesLeft(int retries) {
        int retriesLeft = (retries > 0) ? --retries : retries;
        return retriesLeft;
    }

    /**
     * Removes nodes from the deploying nodes
     */
    private void removeNodes(List<String> depNodeURLs) {
        for (String node : depNodeURLs) {
            internalRemoveDeployingNode(node);
        }
    }

    /**
     * Launch the node on the host passed as parameter
     * @param hostTracker The host on which one the node will be started
     * @param nbNodes number of nodes to deploy
     * @param depNodeURLs list of deploying or lost nodes urls created 
     * @throws RMException If the node hasn't been started. Very important to take care of that
     * in implementations to keep the infrastructure in a coherent state.
     */
    protected abstract void startNodeImpl(HostTracker hostTracker, int nbNodes, List<String> depNodeURLs)
            throws RMException;

    /**
     * Kills the node passed as parameter
     * @param node The node to kill
     * @param host The host of the node
     * @throws RMException if a problem occurred while removing
     */
    protected abstract void killNodeImpl(Node node, InetAddress host) throws RMException;

    // Below are wrapper methods around the map that holds all the persisted
    // infrastructure variables. Some of them acquire a read or write lock
    // before manipulating the variables. In this case, the name of the method
    // suggests it by ending with "WithLock". The methods that write variables
    // also persist them to database at the end. In this case, the name of the
    // method is further suffixed with "AndPersist". However, few method names
    // voluntarily do not make such a distinction because it entails code
    // readability.

    private Map<String, HostTracker> getHostTrackerPerHost() {
        return (Map<String, HostTracker>) persistedInfraVariables.get(HOST_TRACKER_PER_HOST_KEY);
    }

    private void putHostTrackerForHost(final String configuredHostAddress, final HostTracker hostTracker) {
        getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
    }

    private Set<Map.Entry<String, HostTracker>> getHostTrackerPerHostEntrySetWithLock() {
        return getPersistedInfraVariable(getHostTrackerPerHost()::entrySet);
    }

    private boolean needsNodes(final String configuredHostAddress) {
        return getPersistedInfraVariable(getHostTrackerPerHost().get(configuredHostAddress)::needsNodes);
    }

    private int getNeededNodesNumberWithLock(final String configuredHostAddress) {
        return getPersistedInfraVariable(getHostTrackerPerHost().get(configuredHostAddress)::getNeededNodesNumber);
    }

    private void setNeedsNodesWithLockAndPersist(final String configuredHostAddress, final boolean needsNodes) {
        setPersistedInfraVariable(() -> {
            HostTracker hostTracker = getHostTrackerPerHost().get(configuredHostAddress);
            hostTracker.setNeedsNodes(needsNodes);
            getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
            return null;
        });
    }

    private void putAliveNodeUrlWithLockAndPersist(final String configuredHostAddress, final String aliveNodeUrl) {
        setPersistedInfraVariable(() -> {
            HostTracker hostTracker = getHostTrackerPerHost().get(configuredHostAddress);
            hostTracker.putAliveNodeUrl(aliveNodeUrl);
            getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
            return null;
        });
    }

    private boolean hasAliveNodes(final String configuredHostAddress) {
        return getPersistedInfraVariable(getHostTrackerPerHost().get(configuredHostAddress)::hasAliveNodes);
    }

    private void putRemovedNodeUrlWithLockAndPersist(final String configuredHostAddress, final String removedNodeUrl) {
        setPersistedInfraVariable(() -> {
            HostTracker hostTracker = getHostTrackerPerHost().get(configuredHostAddress);
            hostTracker.putRemovedNodeUrl(removedNodeUrl);
            getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
            return null;
        });
    }

    private void putDownNodeUrlWithLockAndPersist(final String configuredHostAddress, final String downNodeUrl) {
        setPersistedInfraVariable(() -> {
            HostTracker hostTracker = getHostTrackerPerHost().get(configuredHostAddress);
            hostTracker.putDownNodeUrl(downNodeUrl);
            getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
            return null;
        });
    }

    private int addAndGetLostNodeNotificationWithLockAndPersist(final String configuredHostAddress) {
        return setPersistedInfraVariable(() -> {
            HostTracker hostTracker = getHostTrackerPerHost().get(configuredHostAddress);
            int lostNodeNotificationsNumber = hostTracker.addAndGetLostNodeNotification();
            getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
            return lostNodeNotificationsNumber;
        });
    }

    private void resetLostNodeNotificationsWithLockAndPersist(final String configuredHostAddress) {
        setPersistedInfraVariable(() -> {
            HostTracker hostTracker = getHostTrackerPerHost().get(configuredHostAddress);
            hostTracker.resetLostNodeNotifications();
            getHostTrackerPerHost().put(configuredHostAddress, hostTracker);
            return null;
        });
    }

    private int getConfiguredNodeNumberWithLockAndPersist(final String configuredHostAddress) {
        return getPersistedInfraVariable(() -> getHostTrackerPerHost().get(configuredHostAddress)
                                                                      .getConfiguredNodeNumber());
    }

    private Map<String, Boolean> getPnTimeoutMap() {
        return (Map<String, Boolean>) persistedInfraVariables.get(PN_TIMEOUT_KEY);
    }

    private Boolean getPnTimeoutWithLock(final String key) {
        return getPersistedInfraVariable(() -> getPnTimeoutMap().get(key));
    }

    private void putPnTimeoutWithLockAndPersist(final String key, final Boolean value) {
        setPersistedInfraVariable(() -> getPnTimeoutMap().put(key, value));
    }

    private void removePnTimeoutWithLockAndPersist(final String key) {
        setPersistedInfraVariable(() -> getPnTimeoutMap().remove(key));
    }

    /**
     * Utility class to encapsulate node name building and node name parsing.
     * The node name involves the host that was written in the host file, and
     * that si used as an identifying key to track the node afterwards.
     *
     * Configured host addresses need to be converted to compliant host
     * addresses in terms of node name building rules (e.g. prevent dots in
     * node names)
     */
    protected class NodeNameBuilder implements Serializable {

        private static final char CONFIGURED_ADDRESS_COMPONENTS_SEPARATOR = '.';

        private static final char COMPLIANT_ADDRESS_COMPONENTS_SEPARATOR = '_';

        private static final String COMPLIANT_ADDRESS_DELIMITER = "__";

        private static final String COMPLIANT_ADDRESS_DELIMITER_REGEX = COMPLIANT_ADDRESS_DELIMITER + "(.*)" +
                                                                        COMPLIANT_ADDRESS_DELIMITER;

        protected String generateNodeName(HostTracker hostTracker) {
            String configuredAddress = hostTracker.getConfiguredAddress();
            String compliantAddress = convertToCompliantAddress(configuredAddress);
            return nodeSource.getName() + COMPLIANT_ADDRESS_DELIMITER + compliantAddress + COMPLIANT_ADDRESS_DELIMITER +
                   0;
        }

        protected String extractHostFromNode(String nodeNameOrUrl) {
            String compliantNodeName = "";
            Pattern pattern = Pattern.compile(COMPLIANT_ADDRESS_DELIMITER_REGEX);
            Matcher matcher = pattern.matcher(nodeNameOrUrl);
            if (matcher.find()) {
                try {
                    compliantNodeName = matcher.group(1);
                    logger.debug("Extracted configured host address " + compliantNodeName);
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalStateException("Configured host address could not be extracted from the node name " +
                                                    nodeNameOrUrl, e);
                }
            } else {
                throw new IllegalStateException("Configured host address could not be found when parsing node name " +
                                                nodeNameOrUrl);
            }
            return convertToConfiguredAddress(compliantNodeName);
        }

        private String convertToCompliantAddress(String configuredAddress) {
            return configuredAddress.replace(CONFIGURED_ADDRESS_COMPONENTS_SEPARATOR,
                                             COMPLIANT_ADDRESS_COMPONENTS_SEPARATOR);
        }

        private String convertToConfiguredAddress(String compliantAddress) {
            return compliantAddress.replace(COMPLIANT_ADDRESS_COMPONENTS_SEPARATOR,
                                            CONFIGURED_ADDRESS_COMPONENTS_SEPARATOR);
        }

    }

    @Override
    public String toString() {
        return String.format("%s nodeTimeOut: [%s], maxDeploymentFailure: [%s], waitBetweenDeploymentFailures: [%s]",
                             this.getClass().getSimpleName(),
                             nodeTimeOut,
                             maxDeploymentFailure,
                             waitBetweenDeploymentFailures);
    }
}
