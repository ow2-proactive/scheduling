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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
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

    @Configurable(fileBrowser = true, description = "Absolute path of the file containing\nthe list of remote hosts")
    protected File hostsList;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected int nodeTimeOut = HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT;

    @Configurable(description = "Maximum number of failed attempt to deploy on \na host before discarding it")
    protected int maxDeploymentFailure = HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD;

    @Configurable(description = "Milliseconds to wait after each failed attempt to deploy on \na host")
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
    protected void initializePersistedInfraVariables() {
        persistedInfraVariables.put(HOST_TRACKER_PER_HOST_KEY, new HashMap<InetAddress, HostTracker>());
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
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line = "";

        while ((line = in.readLine()) != null) {
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
            String host = elts[0];
            try {
                InetAddress reifiedHost = InetAddress.getByName(host);
                HostTracker hostTracker = new HostTracker(reifiedHost, configuredNodeNumber);
                putHostTrackerForHost(reifiedHost, hostTracker);
            } catch (UnknownHostException ex) {
                throw new RuntimeException("Unknown host: " + host, ex);
            }
        }
    }

    /**
     * Check if any host is available and acquire the nodes of available hosts
     */
    @Override
    public void acquireAllNodes() {
        while (hostTrackersNeedNodes()) {
            acquireNode();
        }
    }

    /**
     * Acquire the nodes of available hosts
     */
    @Override
    public void acquireNode() {
        if (!hostTrackersNeedNodes()) {
            logger.info("Attempting to acquire nodes while nodes are already deployed on all hosts.");
            return;
        }

        for (Map.Entry<InetAddress, HostTracker> hostEntry : getHostTrackerPerHostEntrySetWithLock()) {
            final InetAddress host = hostEntry.getKey();
            final HostTracker hostTracker = hostEntry.getValue();

            if (getHostNeedsNodesWithLock(host)) {
                final int neededNodeNumber = getHostNeededNodeNumberWithLock(host);
                logger.info("Acquiring " + neededNodeNumber + " nodes on host " + hostTracker);

                this.nodeSource.executeInParallel(new Runnable() {
                    public void run() {
                        try {
                            startNodeImplWithRetries(host, neededNodeNumber, maxDeploymentFailure);
                        } catch (Exception e) {
                            logger.error("Could not acquire nodes on host " + hostTracker, e);
                        }
                    }
                });

                setHostDoesNotNeedsNodesWithLockAndPersist(host);
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
    }

    /**
     * Parent IM notifies about a new node registration
     */
    @Override
    protected void notifyAcquiredNode(Node node) throws RMException {
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeHost = node.getVMInformation().getInetAddress();

        InetAddress configuredNodeHost = findConfiguredHostForNodeHost(nodeHost);
        putAliveNodeUrlForHostWithLockAndPersist(configuredNodeHost, nodeUrl);
        logger.info("New acquired node " + nodeUrl + " on host " + nodeHost);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeHost = node.getVMInformation().getInetAddress();
        InetAddress configuredNodeHost = findConfiguredHostForNodeHost(nodeHost);
        putRemovedNodeUrlForHostWithLockAndPersist(configuredNodeHost, node.getNodeInformation().getURL());
        logger.info("Removed node " + nodeUrl + " on host " + nodeHost);

        if (!getHostHasAliveNodesWithLock(configuredNodeHost)) {
            killNodeProcess(node, nodeHost);
            setHostNeedsNodesWithLockAndPersist(configuredNodeHost);
            logger.info("Host " + nodeHost + " has no more alive nodes. Need nodes flag is set.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDownNode(final String nodeName, final String nodeUrl, final Node node) {
        InetAddress configuredNodeHost;
        InetAddress nodeHost = null;
        if (node != null) {
            nodeHost = node.getVMInformation().getInetAddress();
            configuredNodeHost = findConfiguredHostForNodeHost(nodeHost);
        } else {
            configuredNodeHost = findConfiguredHostForNodeUrl(nodeUrl);
        }
        if (configuredNodeHost != null) {
            putDownNodeUrlForHostWithLockAndPersist(configuredNodeHost, nodeUrl);
            logger.info("Down node " + nodeUrl + " on host " + nodeHost);

            if (!getHostHasAliveNodesWithLock(configuredNodeHost)) {
                if (node != null) { // the node object can be null in case of a recovery
                    killNodeProcess(node, nodeHost);
                }
                setHostNeedsNodesWithLockAndPersist(configuredNodeHost);
                logger.info("Host " + configuredNodeHost + " has no more alive nodes. Need nodes flag is set.");
            }
        } else {
            logger.warn("Down node " + nodeUrl + " could not be taken into account. Node is unknown");
        }
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeHost = node.getVMInformation().getInetAddress();
        InetAddress configuredNodeHost = findConfiguredHostForNodeHost(nodeHost);
        putAliveNodeUrlForHostWithLockAndPersist(configuredNodeHost, node.getNodeInformation().getURL());
        logger.info("Reconnected node " + nodeUrl + " on host " + nodeHost);
    }

    protected void startNodeImplWithRetries(final InetAddress host, final int nbNodes, int retries) throws RMException {
        while (true) {
            final List<String> depNodeURLs = new ArrayList<>(nbNodes);
            try {
                startNodeImpl(host, nbNodes, depNodeURLs);
                return;
            } catch (Exception e) {
                logger.warn("Failed nodes deployment in host : " + host + ", retries left : " + retries);
                if (isInfiniteRetries(retries) || retries > 0) {
                    removeNodes(depNodeURLs);
                    waitPeriodBeforeRetry();
                    retries = getRetriesLeft(retries);
                } else {
                    logger.error("Tries threshold reached for host " + host +
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
    private boolean hostTrackersNeedNodes() {
        for (Map.Entry<InetAddress, HostTracker> entry : getHostTrackerPerHostEntrySetWithLock()) {
            InetAddress host = entry.getKey();
            if (getHostNeedsNodesWithLock(host)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the {@link InetAddress} under which the host was registered the
     * first time (this is the host written in the file)
     *
     * @param nodeHost the host address that is searched
     * @return the host address that was configured initially and that
     * corresponds to the host address given in parameter, or the host address
     * given in parameter if the host that was configured initially could not
     * be inferred.
     */
    private InetAddress findConfiguredHostForNodeHost(InetAddress nodeHost) {
        InetAddress configuredNodeHost = null;
        for (Map.Entry<InetAddress, HostTracker> hostEntry : getHostTrackerPerHostEntrySetWithLock()) {
            final InetAddress host = hostEntry.getKey();
            if (areSameHost(host, nodeHost)) {
                configuredNodeHost = host;
                break;
            }
        }
        if (configuredNodeHost == null) {
            logger.warn("Node host " + nodeHost + " could not be mapped to any of the configured hosts");
            configuredNodeHost = nodeHost;
        }
        return configuredNodeHost;
    }

    /**
     * Find the {@link InetAddress} of the host that manages the given node URL.
     *
     * @param nodeUrl the URL of the node for which the host is searched
     * @return the host of the node identified by its URL
     */
    private InetAddress findConfiguredHostForNodeUrl(String nodeUrl) {
        InetAddress configuredNodeHost = null;
        for (Map.Entry<InetAddress, HostTracker> hostEntry : getHostTrackerPerHostEntrySetWithLock()) {
            final InetAddress host = hostEntry.getKey();
            if (getHostTrackerManagesNodeUrlWithLock(host, nodeUrl)) {
                configuredNodeHost = host;
            }
        }
        if (configuredNodeHost == null) {
            logger.error("Node URL " + nodeUrl + " could not be mapped to any of the configured hosts");
        }
        return configuredNodeHost;
    }

    /**
     * Determines whether two {@link InetAddress} lead to the same host. The
     * host configured initially in the file may not appear the same in nodes
     * URL because of multiple network interfaces.
     */
    private boolean areSameHost(InetAddress host, InetAddress nodeHost) {
        boolean areSameHost = false;
        if (host.equals(nodeHost)) {
            logger.debug("Host " + host + " and node host " + nodeHost + " are the same");
            areSameHost = true;
        } else {
            if (isLocalAddress(host) && isLocalAddress(nodeHost)) {
                logger.debug("Host " + host + " and node host " + nodeHost + " refer to localhost");
                areSameHost = true;
            }
        }
        return areSameHost;
    }

    private boolean isLocalAddress(InetAddress address) {
        boolean localAddress = false;
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            localAddress = true;
        }
        try {
            if (NetworkInterface.getByInetAddress(address) != null) {
                localAddress = true;
            }
        } catch (SocketException e) {
            return false;
        }
        return localAddress;
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
     * @param host The host on which one the node will be started
     * @param nbNodes number of nodes to deploy
     * @param depNodeURLs list of deploying or lost nodes urls created 
     * @throws RMException If the node hasn't been started. Very important to take care of that
     * in implementations to keep the infrastructure in a coherent state.
     */
    protected abstract void startNodeImpl(InetAddress host, int nbNodes, List<String> depNodeURLs) throws RMException;

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
    // method is further suffixed with "AndPersist"

    private Map<InetAddress, HostTracker> getHostTrackerPerHost() {
        return (Map<InetAddress, HostTracker>) persistedInfraVariables.get(HOST_TRACKER_PER_HOST_KEY);
    }

    private void putHostTrackerForHost(final InetAddress host, final HostTracker hostTracker) {
        getHostTrackerPerHost().put(host, hostTracker);
    }

    private Set<Map.Entry<InetAddress, HostTracker>> getHostTrackerPerHostEntrySetWithLock() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Set<Map.Entry<InetAddress, HostTracker>>>() {
            @Override
            public Set<Map.Entry<InetAddress, HostTracker>> handle() {
                return getHostTrackerPerHost().entrySet();
            }
        });
    }

    private boolean getHostNeedsNodesWithLock(final InetAddress host) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getHostTrackerPerHost().get(host).getNeedNodesFlag();
            }
        });
    }

    private int getHostNeededNodeNumberWithLock(final InetAddress host) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return getHostTrackerPerHost().get(host).getNeededNodeNumber();
            }
        });
    }

    private void setHostNeedsNodesWithLockAndPersist(final InetAddress host) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                HostTracker hostTracker = getHostTrackerPerHost().get(host);
                hostTracker.setNeedNodesFlag(true);
                getHostTrackerPerHost().put(host, hostTracker);
                return null;
            }
        });
    }

    private void setHostDoesNotNeedsNodesWithLockAndPersist(final InetAddress host) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                HostTracker hostTracker = getHostTrackerPerHost().get(host);
                hostTracker.setNeedNodesFlag(false);
                getHostTrackerPerHost().put(host, hostTracker);
                return null;
            }
        });
    }

    private boolean getHostTrackerManagesNodeUrlWithLock(final InetAddress host, final String nodeUrl) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getHostTrackerPerHost().get(host).managesNodeUrl(nodeUrl);
            }
        });
    }

    private void putAliveNodeUrlForHostWithLockAndPersist(final InetAddress host, final String aliveNodeUrl) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                HostTracker hostTracker = getHostTrackerPerHost().get(host);
                hostTracker.putAliveNodeUrl(aliveNodeUrl);
                getHostTrackerPerHost().put(host, hostTracker);
                return null;
            }
        });
    }

    private boolean getHostHasAliveNodesWithLock(final InetAddress host) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getHostTrackerPerHost().get(host).hasAliveNodes();
            }
        });
    }

    private void putRemovedNodeUrlForHostWithLockAndPersist(final InetAddress host, final String removedNodeUrl) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                HostTracker hostTracker = getHostTrackerPerHost().get(host);
                hostTracker.putRemovedNodeUrl(removedNodeUrl);
                getHostTrackerPerHost().put(host, hostTracker);
                return null;
            }
        });
    }

    private void putDownNodeUrlForHostWithLockAndPersist(final InetAddress host, final String downNodeUrl) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                HostTracker hostTracker = getHostTrackerPerHost().get(host);
                hostTracker.putDownNodeUrl(downNodeUrl);
                getHostTrackerPerHost().put(host, hostTracker);
                return null;
            }
        });
    }

    private Map<String, Boolean> getPnTimeoutMap() {
        return (Map<String, Boolean>) persistedInfraVariables.get(PN_TIMEOUT_KEY);
    }

    private Boolean getPnTimeoutWithLock(final String key) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getPnTimeoutMap().get(key);
            }
        });
    }

    private void putPnTimeoutWithLockAndPersist(final String key, final Boolean value) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getPnTimeoutMap().put(key, value);
                return null;
            }
        });
    }

    private void removePnTimeoutWithLockAndPersist(final String key) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getPnTimeoutMap().remove(key);
                return null;
            }
        });
    }

}
