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
import java.net.UnknownHostException;
import java.util.*;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.utils.FileToBytesConverter;


/** Abstract infrastructure Manager implementation based on hosts list file. */
public abstract class HostsFileBasedInfrastructureManager extends InfrastructureManager {

    public static final int DEFAULT_NODE_TIMEOUT = 60 * 1000;

    public static final int DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD = 5;

    public static final long DEFAULT_WAIT_TIME_BETWEEN_NODE_DEPLOYMENT_FAILURES = 5000;

    @Configurable(fileBrowser = true, description = "Absolute path of the file containing\nthe list of remote hosts")
    protected File hostsList;

    @Configurable(description = "in ms. After this timeout expired\nthe node is considered to be lost")
    protected int nodeTimeOut = HostsFileBasedInfrastructureManager.DEFAULT_NODE_TIMEOUT;

    @Configurable(description = "Maximum number of failed attempt to deploy on \na host before discarding it")
    protected int maxDeploymentFailure = HostsFileBasedInfrastructureManager.DEFAULT_NODE_DEPLOYMENT_FAILURE_THRESHOLD;

    @Configurable(description = "Milliseconds to wait after each failed attempt to deploy on \na host")
    protected long waitBetweenDeploymentFailures = HostsFileBasedInfrastructureManager.DEFAULT_WAIT_TIME_BETWEEN_NODE_DEPLOYMENT_FAILURES;

    /**
     * map of free hosts with the number of nodes to deploy on each host
     */
    private static final String FREE_HOSTS_KEY = "freeHosts";

    /**
     * The set of nodes for which one the registerAcquiredNode has been run.
     */
    private static final String REGISTERED_NODES_KEY = "registeredNodes";

    /**
     * Nodes previously removed
     */
    private static final String REMOVED_HOSTS_KEY = "removedHosts";

    /**
     * To notify the control loop of the deploying node timeout
     */
    private static final String PN_TIMEOUT_KEY = "pnTimeout";

    private static final String HOST_PER_NODE_URL_KEY = "hostPerNodeUrlKey";

    protected HostsFileBasedInfrastructureManager() {
    }

    /**
     * Acquire one node per available host
     */
    @Override
    public void acquireAllNodes() {

        while (getFreeHostsSize() > 0) {
            acquireNode();
        }

    }

    /**
     * Acquire one node on an available host
     */
    @Override
    public void acquireNode() {
        final InetAddress tmpHost;
        final int nbNodes;

        if (getFreeHostsSize() == 0) {
            logger.info("Attempting to acquire nodes while all hosts are already deployed.");
            return;
        }
        Iterator<Map.Entry<InetAddress, Integer>> iterator = getFreeHostsEntrySetIterator();
        final Map.Entry<InetAddress, Integer> tmpEntry = iterator.next();
        iterator.remove();
        tmpHost = tmpEntry.getKey();
        nbNodes = tmpEntry.getValue();
        logger.info("Acquiring a new node. #freeHosts:" + getFreeHostsSize() + " #registered: " +
                    getRegisteredNodesSize());
        removeExistingNodeUrlsForHost(tmpHost);
        this.nodeSource.executeInParallel(new Runnable() {
            public void run() {
                try {
                    startNodeImplWithRetries(tmpHost, nbNodes, maxDeploymentFailure);

                    //node acquisition went well for host so we update the threshold
                    logger.debug("Node acquisition ended. #freeHosts:" + getFreeHostsSize() + " #registered: " +
                                 getRegisteredNodesSize());

                } catch (Exception e) {

                    String description = "Could not acquire node on host " + tmpHost +
                                         ". NS's state refreshed regarding last checked exception: #freeHosts:" +
                                         getFreeHostsSize() + " #registered: " + getRegisteredNodesSize();
                    logger.error(description, e);
                    return;
                }
            }
        });
    }

    /**
     * Configures the infrastructre.
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
            int num = 1;
            if (elts.length > 1) {
                try {
                    num = Integer.parseInt(elts[1]);
                    if (num < 1) {
                        throw new IllegalArgumentException("Cannot launch less than one runtime per host.");
                    }
                } catch (Exception e) {
                    logger.warn("Error while parsing hosts file: " + e.getMessage(), e);
                    num = 1;
                }
            }
            String host = elts[0];
            try {
                InetAddress addr = InetAddress.getByName(host);
                putFreeHostIfNotExist(addr, num);
            } catch (UnknownHostException ex) {
                throw new RuntimeException("Unknown host: " + host, ex);
            }
        }
    }

    private void putFreeHostIfNotExist(InetAddress addr, int num) {
        // do not use the setPersistedInfraVariable method here because we cannot persist the variable yet: we need
        // the configuration to be over for that
        writeLock.lock();
        try {
            Integer retrieved = getFreeHosts().get(addr);
            if (retrieved == null) {
                getFreeHosts().put(addr, num);
            }
        } catch (RuntimeException e) {
            logger.error("Exception while manipulating free nodes data structure: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This method is called by Infrastructure Manager in case of a deploying node removal.
     * We take advantage of it to specify to the remote process control loop of the removal.
     * This one will then exit.
     */
    @Override
    protected void notifyDeployingNodeLost(String pnURL) {
        putPnTimeout(pnURL, Boolean.TRUE);
    }

    /**
     * Parent IM notifies about a new node registration
     */
    @Override
    protected void notifyAcquiredNode(Node node) throws RMException {
        String nodeName = node.getNodeInformation().getName();
        String nodeUrl = node.getNodeInformation().getURL();
        InetAddress nodeAddress = node.getVMInformation().getInetAddress();
        putRegisteredNodes(nodeName, nodeAddress);
        putHostForNodeUrl(nodeUrl, nodeAddress);
        if (logger.isDebugEnabled()) {
            logger.debug("New expected node registered: #freeHosts:" + getFreeHostsSize() + " #registered: " +
                         getRegisteredNodesSize());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        removeNodeAndReturnHost(node.getNodeInformation().getName(), node.getNodeInformation().getURL(), node);
    }

    @Override
    public void notifyDownNode(final String nodeName, final String nodeUrl, final Node node) {
        InetAddress host = removeNodeAndReturnHost(nodeName, nodeUrl, node);
        if (host == null) {
            logger.warn("Removed node " + nodeName + " was not registered before");
            host = getHostAddressForNode(nodeUrl);
            addRemovedHost(host);
        }
        if (host != null) {
            int removedNbNodesForHost = getRemovedNodesNumberPerHost(host);
            int expectedNbNodesForHost = computeExpectedNbNodesForHost(host);
            logger.info("Host " + host + " should have " + expectedNbNodesForHost +
                        " registered nodes. Number of nodes removed for this host is " + removedNbNodesForHost);
            if (removedNbNodesForHost == expectedNbNodesForHost) {
                logger.info("Host " + host + " is going to be redeployed");
                removeRemovedHostAndPutIfAbsentFreeHosts(host);
            }
            logger.info("Node " + nodeName + " removed. #freeHosts:" + getFreeHostsSize() + " #registered nodes: " +
                        getRegisteredNodesSize());
        } else {
            logger.error("Removed node " + nodeName + " could not be taken into account");
        }
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        InetAddress host = node.getNodeInformation().getVMInformation().getInetAddress();

        // Yes, this method may experience race conditions
        // like most of the other methods of this class...
        // See https://github.com/ow2-proactive/scheduling/issues/2811

        Integer nbNodesRemoved = getRemovedNodesNumberPerHost(host);

        if (nbNodesRemoved != null) {
            decrementRemovedNodes(host);
            putRegisteredNodes(node.getNodeInformation().getName(), host);
        }
    }

    protected boolean anyTimedOut(List<String> nodesUrl) {
        for (String nodeUrl : nodesUrl) {
            if (getPnTimeout(nodeUrl)) {
                return true;
            }
        }
        return false;
    }

    protected void removeTimeouts(List<String> nodesUrl) {
        for (String nodeUrl : nodesUrl) {
            removePnTimeout(nodeUrl);
        }
    }

    protected void addTimeouts(List<String> nodesUrl) {
        for (String pnUrl : nodesUrl) {
            putPnTimeout(pnUrl, false);
        }
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
     * @param depNodeURLs
     */
    private void removeNodes(List<String> depNodeURLs) {
        for (String node : depNodeURLs) {
            internalRemoveDeployingNode(node);
        }

    }

    @Override
    protected void initializePersistedInfraVariables() {
        persistedInfraVariables.put(FREE_HOSTS_KEY, new HashMap<InetAddress, Integer>());
        persistedInfraVariables.put(REGISTERED_NODES_KEY, new HashMap<String, InetAddress>());
        persistedInfraVariables.put(REMOVED_HOSTS_KEY, new HashMap<InetAddress, Integer>());
        persistedInfraVariables.put(PN_TIMEOUT_KEY, new HashMap<String, Boolean>());
        persistedInfraVariables.put(HOST_PER_NODE_URL_KEY, new HashMap<String, InetAddress>());
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
     * @param host
     * @throws RMException if a problem occurred while removing
     */
    protected abstract void killNodeImpl(Node node, InetAddress host) throws RMException;

    /**
     * Removes a node from the registered nodes and adds one node removed to the removed host map.
     * In case all nodes relative to this host were removed, the JVM is killed.
     *
     * @param node the node to remove
     *
     * @return the {@see InetAddress} of the host of the removed node
     */
    private InetAddress removeNodeAndReturnHost(final String nodeName, final String nodeUrl, final Node node) {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<InetAddress>() {
            @Override
            public InetAddress handle() {
                InetAddress host = getRegisteredNodes().remove(nodeName);
                if (host != null) {
                    logger.debug("Removing node " + nodeUrl + " from " + this.getClass().getSimpleName());
                    addRemovedHost(host);
                    if (!getRegisteredNodes().containsValue(host) && node != null) {
                        try {
                            killNodeImpl(node, host);
                        } catch (Exception e) {
                            logger.trace("An exception occurred during node kill", e);
                        }
                    }
                }
                return host;
            }
        });
    }

    private void addRemovedHost(InetAddress host) {
        Integer retrieved = getRemovedHosts().get(host);
        if (retrieved == null) {
            retrieved = 0;
        }
        getRemovedHosts().put(host, ++retrieved);
    }

    private int computeExpectedNbNodesForHost(InetAddress host) {
        Collection<InetAddress> address = getHostAddressPerNodeUrl().values();
        int nbNodesForHost = 0;
        for (InetAddress inetAddress : address) {
            if (inetAddress.equals(host)) {
                nbNodesForHost++;
            }
        }
        return nbNodesForHost;
    }

    // Below are wrapper methods around the runtime variables map

    private Map<String, InetAddress> getHostAddressPerNodeUrl() {
        return (Map<String, InetAddress>) persistedInfraVariables.get(HOST_PER_NODE_URL_KEY);
    }

    private InetAddress getHostAddressForNode(final String nodeUrl) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<InetAddress>() {
            @Override
            public InetAddress handle() {
                return getHostAddressPerNodeUrl().get(nodeUrl);
            }
        });
    }

    private void putHostForNodeUrl(final String nodeUrl, final InetAddress hostAddress) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getHostAddressPerNodeUrl().put(nodeUrl, hostAddress);
                return null;
            }
        });
    }

    private void removeExistingNodeUrlsForHost(final InetAddress host) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                List<String> nodeUrlsToRemove = new LinkedList<>();
                for (Map.Entry<String, InetAddress> entry : getHostAddressPerNodeUrl().entrySet()) {
                    if (entry.getValue().equals(host)) {
                        nodeUrlsToRemove.add(entry.getKey());
                    }
                }
                for (String nodeUrlToRemove : nodeUrlsToRemove) {
                    getHostAddressPerNodeUrl().remove(nodeUrlToRemove);
                }
                return null;
            }
        });
    }

    private Map<InetAddress, Integer> getFreeHosts() {
        return (Map<InetAddress, Integer>) persistedInfraVariables.get(FREE_HOSTS_KEY);
    }

    private void removeRemovedHostAndPutIfAbsentFreeHosts(final InetAddress host) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int value = getRemovedHosts().remove(host);
                Integer retrievedNbFreeHosts = getFreeHosts().get(host);
                if (retrievedNbFreeHosts == null) {
                    getFreeHosts().put(host, value);
                }
                return null;
            }
        });
    }

    private int getFreeHostsSize() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return getFreeHosts().size();
            }
        });
    }

    private Iterator<Map.Entry<InetAddress, Integer>> getFreeHostsEntrySetIterator() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Iterator<Map.Entry<InetAddress, Integer>>>() {
            @Override
            public Iterator<Map.Entry<InetAddress, Integer>> handle() {
                return getFreeHosts().entrySet().iterator();
            }
        });
    }

    private Map<String, InetAddress> getRegisteredNodes() {
        return (Map<String, InetAddress>) persistedInfraVariables.get(REGISTERED_NODES_KEY);
    }

    private int getRegisteredNodesSize() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return getRegisteredNodes().size();
            }
        });
    }

    private void putRegisteredNodes(final String nodeName, final InetAddress inetAddress) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getRegisteredNodes().put(nodeName, inetAddress);
                return null;
            }
        });
    }

    private Map<InetAddress, Integer> getRemovedHosts() {
        return (Map<InetAddress, Integer>) persistedInfraVariables.get(REMOVED_HOSTS_KEY);
    }

    private int getRemovedNodesNumberPerHost(final InetAddress inetAddress) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Integer>() {
            @Override
            public Integer handle() {
                return getRemovedHosts().get(inetAddress);
            }
        });
    }

    private void decrementRemovedNodes(final InetAddress inetAddress) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                int updated = getRemovedHosts().get(inetAddress) - 1;
                getRemovedHosts().put(inetAddress, updated);
                return null;
            }
        });
    }

    private Map<String, Boolean> getPnTimeoutMap() {
        return (Map<String, Boolean>) persistedInfraVariables.get(PN_TIMEOUT_KEY);
    }

    private Boolean getPnTimeout(final String key) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getPnTimeoutMap().get(key);
            }
        });
    }

    private void putPnTimeout(final String key, final Boolean value) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getPnTimeoutMap().put(key, value);
                return null;
            }
        });
    }

    private void removePnTimeout(final String key) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getPnTimeoutMap().remove(key);
                return null;
            }
        });
    }

}
