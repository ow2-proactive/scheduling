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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.CommandLineBuilder;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;


/**
 *
 * Represents underlying infrastructure and defines ways to acquire / release
 * nodes.<br>
 *
 * Acquisition requests are supposed to be asynchronous. When new new node is
 * available it should register itself into the resource manager by calling
 * {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}<br>
 *
 * To define a new infrastructure manager - define a way to add information
 * about further node acquisition implementing
 * {@link InfrastructureManager#configure(Object...)} - define a way to acquire
 * a single node from underlying infrastructure in
 * {@link InfrastructureManager#acquireNode()} - define a way to acquire all
 * available nodes from the infrastructure in the method
 * {@link InfrastructureManager#acquireAllNodes()} - register available nodes in
 * the resource manager using
 * {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}, so they
 * till be taken into account. - add the name of new class to the resource
 * manager configuration file (config/rm/nodesource/infrastructures).
 *
 */
public abstract class InfrastructureManager implements Serializable {

    /** class' logger */
    protected static final Logger logger = Logger.getLogger(InfrastructureManager.class);

    /** manager's node source */
    protected NodeSource nodeSource;

    /** key to retrieve the deploying nodes URL set (Set<String>) */
    private static final String DEPLOYING_NODES_URL_KEY = "infrastructureManagerDeployingNodes";

    private final Map<String, RMDeployingNode> deployingNodesMap = new HashMap<>();

    /** key to retrieve the lost nodes URL list: (Set<String>) */
    private static final String LOST_NODES_URL_KEY = "infrastructureManagerLostNodes";

    private final Map<String, RMDeployingNode> lostNodesMap = new HashMap<>();

    /** key to retrieve the acquired nodes name set (Set<String>) */
    private static final String ACQUIRED_NODES_NAME_KEY = "infrastructureManagerAcquiredNodesNameSetKey";

    private static final String USING_DEPLOYING_NODES_KEY = "usingDeployingNodes";

    protected static final String RM_URL_KEY = "infrastructureManagerRmUrl";

    /**
     * Indicates whether the infrastructure is shutting down.
     */
    private AtomicBoolean shutDown = new AtomicBoolean(false);

    // used to timeout the nodes
    private transient Timer timeouter = null;

    /**
     * Store information about the running infrastructure. The map holds the name of monitored information and its
     * value. The variables stored in this map should allow the full recovery of an infrastructure state.
     * All accesses to this map must be synchronized.
     */
    protected Map<String, Serializable> persistedInfraVariables = new HashMap<>();

    private transient ReadWriteLock reentrantLock = new ReentrantReadWriteLock();

    /** Use this lock to wrap a read access to runtime variables */
    protected transient Lock readLock = reentrantLock.readLock();

    /** Use this lock to wrap a write access to runtime variables */
    protected transient Lock writeLock = reentrantLock.writeLock();

    /**
     * Database manager, used to persist the runtime variables.
     */
    private RMDBManager dbManager;

    /**
     * Information related to node source that are persisted in database
     */
    private NodeSourceData nodeSourceData;

    public InfrastructureManager() {
    }

    /**
     * Acquire the read lock and call the handle method of the handler given in parameter.
     * @return the value returned by the handle method
     */
    protected <T> T getPersistedInfraVariable(PersistedInfraVariablesHandler<T> t) {
        T variable = null;
        readLock.lock();
        try {
            variable = t.handle();
        } catch (RuntimeException e) {
            logger.error("Exception while getting runtime variable: " + e.getMessage());
            throw e;
        } finally {
            readLock.unlock();
        }
        return variable;
    }

    /**
     * Acquire the write lock, and then:
     * 1) call the handle method of the handler given in parameter
     * 2) call the method that persist in database the runtime variables.
     * @return the return value of the handle method
     */
    protected <T> T setPersistedInfraVariable(PersistedInfraVariablesHandler<T> t) {
        T variable = null;
        writeLock.lock();
        try {
            variable = t.handle();
            persistInfrastructureVariables();
        } catch (RuntimeException e) {
            logger.error("Exception while setting runtime variable: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }
        return variable;
    }

    /**
     * Sets an infrastructure node source. Also sets the field rmUrl if it is
     * not provided by users.
     *
     * @param nodeSource
     *            policy node source
     */
    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
        // we do not set this variable using the setPersistedInfraVariable method because we do not want to persist
        // at this time: the node source has not been persisted yet.
        writeLock.lock();
        try {
            persistedInfraVariables.put(RM_URL_KEY, nodeSource.getRegistrationURL());
        } catch (RuntimeException e) {
            logger.error("Exception while putting RM URL in runtime variables: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * Copy all the runtime variables map given in parameter to the runtime
     * variables map of this infrastructure.
     * @param persistedInfrastructureVariables the runtime variables to recover.
     */
    public void recoverPersistedInfraVariables(Map<String, Serializable> persistedInfrastructureVariables) {
        writeLock.lock();
        try {
            logger.info("Recover persisted infrastructure variables");
            if (logger.isDebugEnabled()) {
                for (Map.Entry<String, Serializable> entry : persistedInfrastructureVariables.entrySet()) {
                    logger.debug("[" + entry.getKey() + " ; " + entry.getValue() + "]");
                }
            }
            persistedInfraVariables.putAll(persistedInfrastructureVariables);
        } catch (RuntimeException e) {
            logger.error("Exception while recovering infrastructure variables", e);
            throw e;
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * To retrieve nodes whose registration status is deploying or lost.
     *
     * @return nodes whose registration status is deploying or lost.
     */
    public List<RMDeployingNode> getDeployingAndLostNodes() {
        List<RMDeployingNode> deployingAndLostNodes = new LinkedList<>();
        readLock.lock();
        try {
            deployingAndLostNodes.addAll(getDeployingNodesWithLock());
            deployingAndLostNodes.addAll(getLostNodesWithLock());
        } catch (RuntimeException e) {
            logger.error("Exception while getting deploying and lost nodes: " + e.getMessage());
            throw e;
        } finally {
            readLock.unlock();
        }
        return deployingAndLostNodes;
    }

    public RMDeployingNode getDeployingOrLostNode(String nodeUrl) {
        RMDeployingNode deployingNode = getDeployingNodeWithLock(nodeUrl);
        if (deployingNode == null) {
            return getLostNodeWithLock(nodeUrl);
        } else {
            return deployingNode;
        }
    }

    /**
     * To remove a deploying node given its url
     *
     * @param nodeUrl
     *            the url of the deploying node to remove.
     * @return true if successful, false otherwise
     */
    public final boolean internalRemoveDeployingNode(String nodeUrl) {
        RMDeployingNode removedDeployingNode;
        boolean isLost = false;

        writeLock.lock();
        try {
            removedDeployingNode = removeDeployingNode(nodeUrl);
            if (removedDeployingNode == null) {
                removedDeployingNode = removeLostNode(nodeUrl);
                isLost = true;
            }
        } catch (RuntimeException e) {
            logger.error("Exception while removing deploying node: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }

        // if such a deploying or lost node exists
        if (removedDeployingNode != null) {
            RMNodeEvent event = removedDeployingNode.createNodeEvent(RMEventType.NODE_REMOVED,
                                                                     removedDeployingNode.getState(),
                                                                     removedDeployingNode.getProvider().getName());
            emitEvent(event);
            logger.trace("DeployingNode " + nodeUrl + " removed from IM");
            // one notifies listeners about the deploying node removal
            // if the node is not lost
            if (!isLost) {
                this.notifyDeployingNodeLost(nodeUrl);
            }
            return true;
        } else {
            logger.trace("DeployingNode: " + nodeUrl + " no more managed by IM, cannot remove it");
            return false;
        }
    }

    /**
     * Performs some cleanup ( essentially removal of the cached node ) and call
     * removeNodeImpl
     * 
     * @param node
     *            the node to be removed
     * @throws RMException
     */
    public final void internalRemoveNode(Node node) throws RMException {
        writeLock.lock();
        try {
            unregisterAcquiredNodeWithLockAndPersist(node.getNodeInformation().getName());
            this.removeNode(node);
        } catch (Exception e) {
            logger.warn("Exception occurred while removing node " + node);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This method is called by the system when a Node is detected as DOWN.
     *
     * @param node the ProActive Programming Node that is down. This parameter
     *             can be null if no information about the node can be
     *             retrieved apart from its url.
     * @param nodeUrl the URL of the node that is down.
     *
     * @throws RMException if any problems occurred.
     */
    public void internalNotifyDownNode(String nodeName, String nodeUrl, Node node) throws RMException {
        writeLock.lock();
        try {
            removeDownNodePriorToNotify(nodeName);
            this.notifyDownNode(nodeName, nodeUrl, node);
        } catch (Exception e) {
            logger.warn("Exception occurred while removing node " + node, e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This method can be overriden by subclasses, especially if they do not
     * want the node to be removed from the InfrastructureManager map when
     * detected as down.
     *
     * @param nodeName the node to remove
     */
    protected void removeDownNodePriorToNotify(String nodeName) {
        unregisterAcquiredNodeWithLockAndPersist(nodeName);
    }

    /**
     * Define what needs to be done in an infrastructure implementation when a node is detected as down.
     *
     * @param node the ProActive Programming Node that is down.
     *
     * @throws RMException if any problems occurred.
     */
    public abstract void notifyDownNode(String nodeName, String nodeUrl, Node node) throws RMException;

    /**
     * This method is called by the RMCore to notify the InfrastructureManager
     * that a new node was added to the core. If the IM throws an exception, the
     * node is not added. Note that this method is in mutual exclusion with
     * {@link #checkNodeIsAcquiredAndDo(String, Runnable, Runnable)} At this
     * point, if a previous call to
     * {@link InfrastructureManager#addDeployingNode(String, String, String, long)}
     * was made (means that implementor uses deploying nodes features), this
     * method ensures that the implementation method (see
     * {@link InfrastructureManager#notifyAcquiredNode(Node)} is only called if
     * no timeout has occurred for the associated deploying node.
     *
     * @param node
     *            the newly added node
     * @throws RMException
     */
    public final void internalRegisterAcquiredNode(Node node) throws RMException {
        // if implementation doesn't have the notion of deploying nodes (and
        // in this case, useDeployingNodes should be set to false), we just
        // trigger the node acquisition and return
        if (!isUsingDeployingNode()) {
            this.notifyAcquiredNode(node);
            return;
        }

        // otherwise, we use a timeout logic for deploying nodes
        String deployingNodeURL = this.buildDeployingNodeURL(node.getNodeInformation().getName());

        writeLock.lock();
        try {
            RMDeployingNode removedDeployingNode = removeDeployingNode(deployingNodeURL);
            // if a deploying node with this name exists, one runs the
            // implementation callback
            if (removedDeployingNode != null) {
                RMNodeEvent event = removedDeployingNode.createNodeEvent(RMEventType.NODE_REMOVED,
                                                                         removedDeployingNode.getState(),
                                                                         removedDeployingNode.getProvider().getName());
                emitEvent(event);
                this.notifyAcquiredNode(node);
                registerAcquiredNodeWithLockAndPersist(node.getNodeInformation().getName());
            } else {
                String url = node.getNodeInformation().getURL();
                logger.warn("Not expected node registered, discarding it: " + url);
                throw new RMException("Not expected node registered, discarding it: " + url);
            }
        } catch (RuntimeException e) {
            logger.error("Exception while moving deploying node to acquired node", e);
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Add multiple deploying nodes
     * @param nodeNames node names which will be created
     * @param obfuscatedCmd obfuscated command used to start the node
     * @param message user message
     * @param nodeTimeout node timeout used
     * @return a list of deploying nodes urls
     */
    protected List<String> addMultipleDeployingNodes(List<String> nodeNames, String obfuscatedCmd, String message,
            long nodeTimeout) {
        List<String> answer = new ArrayList<>(nodeNames.size());
        for (String nodeName : nodeNames) {
            String depNodeURL = this.addDeployingNode(nodeName, obfuscatedCmd, message, nodeTimeout);
            answer.add(depNodeURL);
        }
        return answer;
    }

    /**
     * Declare multiple nodes lost
     *
     * @param deployingNodes list of urls of deploying nodes
     * @param message        user message
     */
    protected void multipleDeclareDeployingNodeLost(List<String> deployingNodes, String message) {
        for (String node : deployingNodes) {
            this.declareDeployingNodeLost(node, message);
        }

    }

    /**
     * called by the node source at configuration time. Shifts the parameter
     * array once done to let implementation only care about their own
     * configurable parameters. This method acquires the write lock to manipulate runtime variables.
     *
     * @param parameters
     *            the parameters of the infrastructure manager
     * @throws IllegalArgumentException
     *             if the parameters are invalid
     */
    public final void internalConfigure(Object... parameters) {
        writeLock.lock();
        try {
            internalInitializePersistedInfraVariables();
            initializePersistedInfraVariables();
            this.configure(parameters);
        } catch (RuntimeException e) {
            logger.error("Exception while initializing runtime variables and configuring infrastructure: " +
                         e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Called by the node source at shutdown time. First removes every
     * registered Deploying and Lost nodes and delegates the call to the
     * implementation thanks to the method {@link #shutDown()}
     */
    public final void internalShutDown() {
        shutDown.set(true);
        // first removing deploying nodes
        for (RMDeployingNode deployingNode : getDeployingNodesWithLock()) {
            this.internalRemoveDeployingNode(deployingNode.getNodeURL());
        }
        // no need to remove lost nodes, implementation is notified
        // at the timeout of the deploying node
        clearDeployingNodesWithLockAndPersist();
        // delegating the call to the implementation
        this.shutDown();

        if (timeouter != null) {
            timeouter.cancel();
        }
    }

    // **********************************************************************************************\\
    // **************************************** SPI methods
    // *****************************************\\
    // **********************************************************************************************\\
    /**
     *
     * @return The Description of the implemented Infrastructure
     */
    public abstract String getDescription();

    /**
     * Adds information required to deploy nodes in the future. Do not initiate
     * a real nodes deployment/acquisition as it's up to the policy.
     * The runtime variables of an infrastructure should not be explicitly persisted in this method as the link with
     * the node source is not properly set at the time of calling this method. They will be persisted at the end of the
     * initialization in RMCore#createNodeSource
     *
     * @param parameters
     *            of the infrastructure manager
     * @throws IllegalArgumentException
     *             if the parameters are invalid
     */
    protected abstract void configure(Object... parameters);

    /**
     * Asynchronous node acquisition request. Proactive node should be
     * registered by calling
     * {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}
     */
    public abstract void acquireNode();

    public void acquireNodes(int n, Map<String, ?> nodeConfiguration) {
        throw new UnsupportedOperationException("Node configuration is not implemented for this infrastructure manager.");
    }

    /**
     * Asynchronous request of all nodes acquisition. Proactive nodes should be
     * registered by calling
     * {@link InfrastructureManager#internalRegisterAcquiredNode(Node)}
     */
    public abstract void acquireAllNodes();

    public void acquireAllNodes(Map<String, ?> nodeConfiguration) {
        throw new UnsupportedOperationException("Node configuration is not implemented for this infrastructure manager.");
    }

    /**
     * Removes the node from the resource manager.
     * 
     * @param node
     *            the node to release.
     * @throws RMException
     *             if any problems occurred.
     */
    public abstract void removeNode(Node node) throws RMException;

    /**
     * Notifies the user that the deploying node was lost or removed (because of
     * a timeout, user interaction...) Default empty implementation is provided
     * because implementors don't necessary use this feature. Anyway, if they
     * decide to do so, they can override this method, for instance, to change a
     * flag that would get a control loop to exit...
     *
     * @param pnURL
     *            the deploying node's URL for which one the timeout occurred.
     */
    protected void notifyDeployingNodeLost(String pnURL) {
    }

    /**
     * Notifies the implementation of the infrastructure manager that a new node
     * has been registered. If this method throws an exception, the node
     * registration will be discarded. This method is always called if
     * implementor doesn't use deploying nodes (no call to
     * {@link InfrastructureManager#addDeployingNode(String, String, String, long)}
     * was made), and is called only for deploying nodes for which one no
     * timeout occurred.
     *
     * @param node
     *            the newly registered node
     * @throws RMException
     *             if the implementation does not approve the node acquisition
     *             request
     */
    protected abstract void notifyAcquiredNode(Node node) throws RMException;

    /**
     * Notify this infrastructure it is going to be shut down along with its
     * nodesource. All necessary cleanup should be done here.
     */
    protected void shutDown() {
    }

    /**
     * If nodes recovery is enabled, persist the current state of the node
     * source.
     * If the node source data of this infrastructure is not know, it is first
     * fetched in database.
     * The node source data is never persisted if the current infrastructure is
     * the {@link NodeSource#DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME}.
     */
    public void persistInfrastructureVariables() {
        if (recoveryActivated()) {
            String nodeSourceName = nodeSource.getName();
            readLock.lock();
            try {
                getAndPersistNodeSourceData(nodeSourceName);
            } catch (RuntimeException e) {
                logger.error("Exception while persisting infrastructure variables", e);
                throw e;
            } finally {
                readLock.unlock();
            }
        }
    }

    private boolean recoveryActivated() {
        return PAResourceManagerProperties.RM_NODES_RECOVERY.getValueAsBoolean() && nodeSource.nodesRecoverable();
    }

    private void getAndPersistNodeSourceData(String nodeSourceName) {
        createDBManagerIfNull();
        if (nodeSourceData == null) {
            logger.debug("Node source data of node source " + nodeSourceName + " needs to be retrieved from database");
            nodeSourceData = dbManager.getNodeSource(nodeSourceName);
        }
        if (nodeSourceData != null) {
            nodeSourceData.setInfrastructureVariables(persistedInfraVariables);
            dbManager.updateNodeSource(nodeSourceData);
        } else {
            logger.warn("Node source " + nodeSourceName + " is unknown. Cannot persist infrastructure variables");
        }
    }

    private void createDBManagerIfNull() {
        if (dbManager == null) {
            setRmDbManager(RMDBManager.getInstance());
        }
    }

    protected void setRmDbManager(RMDBManager dbManager) {
        this.dbManager = dbManager;
    }

    // **********************************************************************************************\\
    // **************************************** API methods
    // *****************************************\\
    // **********************************************************************************************\\

    /**
     * This method returns a
     * {@link org.ow2.proactive.resourcemanager.utils.CommandLineBuilder} filled
     * in with "default" settings. That means that the returned
     * CommandLineBuilder is useable as such.
     * <ul>
     * <li>It tries to set the Java Path to use, either JAVA_HOME retrieved from
     * your environment or java.home set by Java itself.</li>
     * <li>The target operating system is set to {@link OperatingSystem#UNIX}
     * </li>
     * <li>If a ProActive configuration file is provided, it is used as such.
     * </li>
     * <li>Finally, it tries to set the nodesource's name, the rm's URL and the
     * node's name.</li>
     * </ul>
     *
     * @param targetOS
     *            the operating system on which one the node will be deployed
     */
    protected final CommandLineBuilder getDefaultCommandLineBuilder(OperatingSystem targetOS) {
        CommandLineBuilder result = new CommandLineBuilder();
        String javaPath = System.getProperty("java.home") + targetOS.fs + "bin" + targetOS.fs + "java";
        result.setJavaPath(javaPath);
        result.setTargetOS(targetOS);
        if (CentralPAPropertyRepository.PA_CONFIGURATION_FILE.isSet()) {
            try {
                result.setPaProperties(new File(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getValue()));
            } catch (IOException e) {
                logger.debug("Cannot set default pa configuration file for " + CommandLineBuilder.class.getSimpleName(),
                             e);
            }
        }
        result.setRmURL(getRmUrl());
        if (this.nodeSource != null) {
            String nsName = this.nodeSource.getName();
            result.setSourceName(nsName);
            result.setNodeName(nsName + "_DefaultNodeName");
        }
        return result;
    }

    /**
     * Returns an empty
     * {@link org.ow2.proactive.resourcemanager.utils.CommandLineBuilder}
     *
     * @return Returns an empty
     *         {@link org.ow2.proactive.resourcemanager.utils.CommandLineBuilder}
     */
    protected final CommandLineBuilder getEmptyCommandLineBuilder() {
        return new CommandLineBuilder();
    }

    /**
     * Creates a new RMDeployingNode's, stores it in a local ArrayList and
     * notify the owning NodeSource of the RMDeployingNode creation
     *
     * @param name
     *            The RMDeployingNode's name.
     * @param description
     *            The RMDeployingNode's description
     * @param timeout
     *            after which one the deploying node will be declared lost. (
     *            node acquisition after this timeout is discarded )
     * @return The newly created RMDeployingNode's URL.
     * @throws UnsupportedOperationException
     *             if the infrastructure manager is shuting down
     */
    protected final String addDeployingNode(String name, String command, String description, final long timeout) {
        checkName(name);
        checkTimeout(timeout);
        if (shutDown.get()) {
            throw new UnsupportedOperationException("The infrastructure manager is shuting down.");
        }
        // if the user calls this method, we use the require nodes/timeout
        // mecanism
        setUsingDeployingNodesWithLockAndPersist();
        NodeSource nsStub = this.nodeSource.getStub();
        RMDeployingNode deployingNode = RMDeployingNodeAccessor.getDefault().newRMDeployingNode(name,
                                                                                                nsStub,
                                                                                                command,
                                                                                                nsStub.getAdministrator(),
                                                                                                description);
        final String deployingNodeUrl = deployingNode.getNodeURL();
        addDeployingNode(deployingNode);

        RMNodeEvent event = deployingNode.createNodeEvent(RMEventType.NODE_ADDED,
                                                          null,
                                                          deployingNode.getProvider().getName());
        emitEvent(event);
        this.sched(new TimerTask() {
            @Override
            public void run() {
                InfrastructureManager.this.timeout(deployingNodeUrl, timeout);
            }
        }, timeout);
        return deployingNode.getNodeURL();
    }

    /**
     * Declares a deploying node lost. Future attempts to modify the deploying
     * node will be ignored.
     *
     * @param toUpdateURL
     *            The RMDeployingNode's URL which is to be declared as lost
     * @param description
     *            the new rmdeployingnode's description, can be null.
     * @return true if the method ran successfully, false otherwise.
     */
    protected final boolean declareDeployingNodeLost(String toUpdateURL, String description) {
        RMDeployingNode removedDeployingNode;
        // we need to atomically move the node from the deploying collection to
        // the lost one.
        writeLock.lock();
        try {
            removedDeployingNode = removeDeployingNode(toUpdateURL);
            if (removedDeployingNode != null) {
                addLostNode(removedDeployingNode);
            }
        } catch (RuntimeException e) {
            logger.error("Exception while moving a node from deploying to lost: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }

        if (removedDeployingNode != null) {
            logger.warn("Declaring node as lost: " + toUpdateURL + ", " + description);
            NodeState previousState = removedDeployingNode.getState();
            RMDeployingNodeAccessor.getDefault().setLost(removedDeployingNode);
            if (description != null) {
                RMDeployingNodeAccessor.getDefault().setDescription(removedDeployingNode, description);
            }

            persistUpdatedRMDeployingNodeIfRecoveryEnabled(removedDeployingNode);

            RMNodeEvent event = removedDeployingNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                                     previousState,
                                                                     removedDeployingNode.getProvider().getName());
            emitEvent(event);

            if (logger.isTraceEnabled()) {
                logger.trace(RMDeployingNode.class.getSimpleName() + " " + toUpdateURL + " declared lost in IM");
            }
            return true;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace(RMDeployingNode.class.getSimpleName() + " " + toUpdateURL +
                             " no more managed by IM, cannot declare it as lost");
            }
            return false;
        }
    }

    /**
     * Check if the Node with the given name is already acquired ( usefull when
     * the launcher of the process which is supposed to launch a RMNode waits
     * for its registration ). At the same time, this methode executes the first
     * runnable is the node has been found amoung acquired nodes and executes
     * the second runnable if the node has not been found. Note that this method
     * is in mutual exclusion with
     * {@link InfrastructureManager#internalRegisterAcquiredNode(Node)} to avoid
     * races.
     *
     * @param nodeName
     *            the node's name
     * @param toRunWhenOK
     *            the Runnable that will be launched if the node is already
     *            acquired (can be null)
     * @param toRunWhenKO
     *            the Runnable that will be launched if the node has not been
     *            found among nodesource.getAliveNodes(). (can be null)
     * @return true if the node with such name is already acquired false
     *         otherwise.
     */
    protected final boolean checkNodeIsAcquiredAndDo(String nodeName, Runnable toRunWhenOK, Runnable toRunWhenKO) {
        writeLock.lock();
        try {
            if (containsAcquiredNodeWithLock(nodeName)) {
                checkNodePostAction(toRunWhenOK,
                                    "An exception occurred while running implementation's code whereas the node " +
                                                 nodeName + " was found.");
                return true;
            } else {
                checkNodePostAction(toRunWhenKO,
                                    "An exception occurred while running implementation's code whereas the node " +
                                                 nodeName + " was not found.");
                return false;
            }
        } catch (RuntimeException e) {
            logger.error("Exception while checking acquired node and doing post action: " + e.getMessage());
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    private void checkNodePostAction(Runnable toRunWhenOK, String message) {
        if (toRunWhenOK != null) {
            try {
                toRunWhenOK.run();
            } catch (Exception e) {
                logger.warn(message, e);
            }
        }
    }

    protected final boolean checkAllNodesAreAcquiredAndDo(List<String> nodeNames, Runnable toRunWhenOK,
            Runnable toRunWhenKO) {
        boolean answer = true;
        for (String nodeName : nodeNames) {
            answer = answer && checkNodeIsAcquiredAndDo(nodeName, toRunWhenOK, toRunWhenKO);
        }
        return answer;
    }

    // **********************************************************************************************//
    // *********************** Package private accessors & Helpers
    // **********************************//
    // **********************************************************************************************//

    /**
     * To emit an event and register it in the database
     */
    private void emitEvent(final RMNodeEvent event) {
        NodeSource nsStub = this.nodeSource.getStub();
        nsStub.internalEmitDeployingNodeEvent(event);
    }

    private void timeout(String pnURL, long timeout) {
        if (this.declareDeployingNodeLost(pnURL, "Timeout occurred after " + timeout + " ms.")) {
            this.notifyDeployingNodeLost(pnURL);
        }
    }

    private synchronized void sched(TimerTask task, long delay) {
        if (this.timeouter == null) {
            this.timeouter = new Timer("InfrastructureManager Timer");
        }
        this.timeouter.schedule(task, delay);
    }

    private void checkTimeout(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }

    }

    private void checkName(String name) {
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Deploying node name cannot contain white spaces: \"" + name +
                                               "\" is forbidden");
        }
        String pnURL = this.buildDeployingNodeURL(name);
        if (containsDeployingNodeUrlWithLock(pnURL) || containsLostNodeWithLock(pnURL)) {
            throw new IllegalArgumentException(RMDeployingNode.class.getSimpleName() + " with the same name (\"" +
                                               name + "\") has already been created");
        }
    }

    /**
     * Builds the name of the deploying node given its name
     *
     * @param pnName
     *            The name of the deploying node
     * @return the URL of the deploying node
     */
    private String buildDeployingNodeURL(String pnName) {
        return RMDeployingNode.PROTOCOL_ID + "://" + this.nodeSource.getName() + "/" + pnName;
    }

    /**
     * Called by the system every time a node that is DOWN reconnects
     * and changes its status to FREE or BUSY.
     *
     * @param node the node that has reconnected.
     */
    public void onDownNodeReconnection(Node node) {
        // to be overridden by children
    }

    protected void addDeployingNode(RMDeployingNode deployingNode) {
        RMDeployingNode previousDeployingNode = addDeployingNodeWithLockAndPersist(deployingNode.getNodeURL(),
                                                                                   deployingNode);
        if (previousDeployingNode == null) {
            persistNewRMDeployingNodeIfRecoveryEnabled(deployingNode);
        } else {
            persistUpdatedRMDeployingNodeIfRecoveryEnabled(deployingNode);
        }
    }

    protected RMDeployingNode removeDeployingNode(String nodeUrl) {
        RMDeployingNode removedDeployingNode = removeDeployingNodeWithLockAndPersist(nodeUrl);
        persistRemovedRMDeployingNodeIfRecoveryEnabled(nodeUrl);
        return removedDeployingNode;
    }

    protected void addLostNode(RMDeployingNode lostNode) {
        RMDeployingNode previousLostNode = addLostNodeWithLockAndPersist(lostNode.getNodeURL(), lostNode);
        if (previousLostNode == null) {
            persistNewRMDeployingNodeIfRecoveryEnabled(lostNode);
        } else {
            persistUpdatedRMDeployingNodeIfRecoveryEnabled(lostNode);
        }
    }

    protected RMDeployingNode removeLostNode(String nodeUrl) {
        RMDeployingNode removedLostNode = removeLostNodeWithLockAndPersist(nodeUrl);
        persistRemovedRMDeployingNodeIfRecoveryEnabled(nodeUrl);
        return removedLostNode;
    }

    public void setPersistedNodeSourceData(NodeSourceData nodeSourceData) {
        this.nodeSourceData = nodeSourceData;
    }

    /**
     * This method should initialize a value in the runtime variables map for all the runtime variables that will be
     * used in the class. It is called at initialization time of the infrastructure, just before the
     * {@link InfrastructureManager#configure(Object...)} method.
     *
     * This method runs with the write lock acquired.
     */
    protected abstract void initializePersistedInfraVariables();

    private void internalInitializePersistedInfraVariables() {
        persistedInfraVariables.put(DEPLOYING_NODES_URL_KEY, new HashSet<>());
        persistedInfraVariables.put(LOST_NODES_URL_KEY, new HashSet<>());
        persistedInfraVariables.put(ACQUIRED_NODES_NAME_KEY, new HashSet<>());
        persistedInfraVariables.put(USING_DEPLOYING_NODES_KEY, false);
        persistedInfraVariables.put(RM_URL_KEY, "");
    }

    /**
     * Add the information of the given deploying node to the database.
     *
     * @param rmNode the node to add to the database
     */
    private void persistNewRMDeployingNodeIfRecoveryEnabled(RMDeployingNode rmNode) {
        createDBManagerIfNull();
        if (nodeRecoveryEnabled()) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            NodeSourceData persistedNodeSourceData = dbManager.getNodeSource(rmNode.getNodeSourceName());
            rmNodeData.setNodeSource(persistedNodeSourceData);
            if (!NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(persistedNodeSourceData.getName())) {
                dbManager.addNode(rmNodeData);
            }
        }
    }

    /**
     * Update the information of the given deploying node in database.
     *
     * @param rmNode the node to update in database
     */
    private void persistUpdatedRMDeployingNodeIfRecoveryEnabled(RMNode rmNode) {
        createDBManagerIfNull();
        if (nodeRecoveryEnabled()) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            if (!NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(rmNode.getNodeSourceName())) {
                dbManager.updateNode(rmNodeData);
            }
        }
    }

    /**
     * Update the information of the given deploying node in database.
     *
     * @param nodeUrl the URL of the node to remove in database
     */
    private void persistRemovedRMDeployingNodeIfRecoveryEnabled(String nodeUrl) {
        createDBManagerIfNull();
        if (nodeRecoveryEnabled()) {
            RMNodeData rmNodeData = new RMNodeData.Builder().nodeUrl(nodeUrl).build();
            dbManager.removeNode(rmNodeData);
        }
    }

    private boolean nodeRecoveryEnabled() {
        return PAResourceManagerProperties.RM_NODES_RECOVERY.getValueAsBoolean();
    }

    /**
     * Helper nested class. Used not to expose methods that should be package
     * private of the {@link RMDeployingNode} object.
     */
    public static abstract class RMDeployingNodeAccessor implements Serializable {
        private static volatile RMDeployingNodeAccessor DEFAULT;

        public static void setDefault(RMDeployingNodeAccessor d) {
            RMDeployingNodeAccessor.DEFAULT = d;
        }

        private static RMDeployingNodeAccessor getDefault() {
            if (RMDeployingNodeAccessor.DEFAULT != null) {
                return RMDeployingNodeAccessor.DEFAULT;
            }
            try {
                Class.forName(RMDeployingNode.class.getName(), true, RMDeployingNode.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return RMDeployingNodeAccessor.DEFAULT;
        }

        /**
         * Instantiate a new {@link RMDeployingNode} with the given parameters
         *
         * @param name
         *            The deploying node's name
         * @param ns
         *            The node source owning the deploying node
         * @param command
         *            The command that has been used to launch the
         *            {@link RMNode}
         * @param provider
         *            The node's provider
         * @param description
         *            A first description of the node
         * @return The newly created deploying node
         */
        protected abstract RMDeployingNode newRMDeployingNode(String name, NodeSource ns, String command,
                Client provider, String description);

        /**
         * To set the Description of the {@link RMDeployingNode}
         *
         * @param pn
         *            the deploying node to update
         * @param newDescription
         *            the new description
         */
        protected abstract void setDescription(RMDeployingNode pn, String newDescription);

        /**
         * To update the lost field of the deploying node
         *
         * @param pn
         *            The deploying node to update
         */
        protected abstract void setLost(RMDeployingNode pn);
    }

    /**
     * Interface that allows handling the runtime variables through a functional point of view.
     * This allows protecting sensitive actions within one locked block.
     *
     * @param <T> the type of the retrieved variable if so.
     */
    protected interface PersistedInfraVariablesHandler<T> {

        /**
         * Typically, handles a sequence of actions involving the runtime variables.
         *
         * @return the manipulated variable.
         */
        T handle();

    }

    // Below are wrapper methods around the runtime variables map

    protected Set<String> getPersistedDeployingNodesUrl() {
        return (Set<String>) persistedInfraVariables.get(DEPLOYING_NODES_URL_KEY);
    }

    /**
     * @return The deploying node that was mapped by that URL before, or null.
     */
    public RMDeployingNode addDeployingNodeWithLockAndPersist(final String nodeUrl,
            final RMDeployingNode deployingNode) {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<RMDeployingNode>() {
            @Override
            public RMDeployingNode handle() {
                RMDeployingNode previousDeployingNode = deployingNodesMap.put(nodeUrl, deployingNode);
                getPersistedDeployingNodesUrl().add(nodeUrl);
                return previousDeployingNode;
            }
        });
    }

    /**
     * @return The deploying node that has been removed, or null
     */
    private RMDeployingNode removeDeployingNodeWithLockAndPersist(final String nodeUrl) {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<RMDeployingNode>() {
            @Override
            public RMDeployingNode handle() {
                RMDeployingNode removedDeployingNode = deployingNodesMap.remove(nodeUrl);
                getPersistedDeployingNodesUrl().remove(nodeUrl);
                return removedDeployingNode;
            }
        });
    }

    private void clearDeployingNodesWithLockAndPersist() {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                deployingNodesMap.clear();
                getPersistedDeployingNodesUrl().clear();
                return null;
            }
        });
    }

    private boolean containsDeployingNodeUrlWithLock(final String nodeUrl) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getPersistedDeployingNodesUrl().contains(nodeUrl);
            }
        });
    }

    private RMDeployingNode getDeployingNodeWithLock(final String nodeUrl) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<RMDeployingNode>() {
            @Override
            public RMDeployingNode handle() {
                return deployingNodesMap.get(nodeUrl);
            }
        });
    }

    private List<RMDeployingNode> getDeployingNodesWithLock() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<List<RMDeployingNode>>() {
            @Override
            public List<RMDeployingNode> handle() {
                List<RMDeployingNode> deployingNodesCopy = new LinkedList<>();
                Collection<RMDeployingNode> deployingNodes = deployingNodesMap.values();
                deployingNodesCopy.addAll(deployingNodes);
                return deployingNodesCopy;
            }
        });
    }

    protected Set<String> getPersistedLostNodesUrl() {
        return (Set<String>) persistedInfraVariables.get(LOST_NODES_URL_KEY);
    }

    /**
     * @return The lost node that was mapped by that URL before, or null.
     */
    public RMDeployingNode addLostNodeWithLockAndPersist(final String nodeUrl, final RMDeployingNode lostNode) {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<RMDeployingNode>() {
            @Override
            public RMDeployingNode handle() {
                RMDeployingNode previousLostNode = lostNodesMap.put(nodeUrl, lostNode);
                getPersistedLostNodesUrl().add(nodeUrl);
                return previousLostNode;
            }
        });
    }

    /**
     * @return The lost node that has been removed, or null
     */
    private RMDeployingNode removeLostNodeWithLockAndPersist(final String nodeUrl) {
        return setPersistedInfraVariable(new PersistedInfraVariablesHandler<RMDeployingNode>() {
            @Override
            public RMDeployingNode handle() {
                RMDeployingNode removedLostNode = lostNodesMap.remove(nodeUrl);
                getPersistedLostNodesUrl().remove(nodeUrl);
                return removedLostNode;
            }
        });
    }

    private boolean containsLostNodeWithLock(final String nodeUrl) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getPersistedLostNodesUrl().contains(nodeUrl);
            }
        });
    }

    private RMDeployingNode getLostNodeWithLock(final String nodeUrl) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<RMDeployingNode>() {
            @Override
            public RMDeployingNode handle() {
                return lostNodesMap.get(nodeUrl);
            }
        });
    }

    private List<RMDeployingNode> getLostNodesWithLock() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<List<RMDeployingNode>>() {
            @Override
            public List<RMDeployingNode> handle() {
                List<RMDeployingNode> lostNodesCopy = new LinkedList<>();
                Collection<RMDeployingNode> lostNodes = lostNodesMap.values();
                lostNodesCopy.addAll(lostNodes);
                return lostNodesCopy;
            }
        });
    }

    private Set<String> getAcquiredNodesNameSet() {
        return (Set<String>) persistedInfraVariables.get(ACQUIRED_NODES_NAME_KEY);
    }

    private void registerAcquiredNodeWithLockAndPersist(final String nodeName) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getAcquiredNodesNameSet().add(nodeName);
                return null;
            }
        });
    }

    private void unregisterAcquiredNodeWithLockAndPersist(final String nodeName) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                getAcquiredNodesNameSet().remove(nodeName);
                return null;
            }
        });
    }

    private boolean containsAcquiredNodeWithLock(final String nodeName) {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return getAcquiredNodesNameSet().contains(nodeName);
            }
        });
    }

    public boolean isUsingDeployingNode() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<Boolean>() {
            @Override
            public Boolean handle() {
                return (boolean) persistedInfraVariables.get(USING_DEPLOYING_NODES_KEY);
            }
        });
    }

    private void setUsingDeployingNodesWithLockAndPersist() {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                persistedInfraVariables.put(USING_DEPLOYING_NODES_KEY, true);
                return null;
            }
        });
    }

    protected void setRmUrl(final String rmUrl) {
        setPersistedInfraVariable(new PersistedInfraVariablesHandler<Void>() {
            @Override
            public Void handle() {
                persistedInfraVariables.put(RM_URL_KEY, rmUrl);
                return null;
            }
        });
    }

    protected String getRmUrl() {
        return getPersistedInfraVariable(new PersistedInfraVariablesHandler<String>() {
            @Override
            public String handle() {
                return (String) persistedInfraVariables.get(RM_URL_KEY);
            }
        });
    }

}
