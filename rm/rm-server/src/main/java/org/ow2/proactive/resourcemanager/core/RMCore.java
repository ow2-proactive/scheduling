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
package org.ow2.proactive.resourcemanager.core;

import static org.ow2.proactive.resourcemanager.common.event.RMEventType.NODE_STATE_CHANGED;
import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.RM_NODES_RECOVERY;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.principals.IdentityPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.permissions.MethodCallPermission;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.policy.ClientsPolicy;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.authentication.RMAuthenticationImpl;
import org.ow2.proactive.resourcemanager.cleaning.NodesCleaner;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.RMStateNodeUrls;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.RMNodeConfigurator;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManagerFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.selection.statistics.ProbablisticSelectionManager;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;
import org.ow2.proactive.resourcemanager.utils.ClientPinger;
import org.ow2.proactive.resourcemanager.utils.RMNodeHelper;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;


/**
 * The main active object of the Resource Manager (RM), the RMCore has to
 * provide nodes to clients.
 * <p>
 * The RMCore functions are:
 * <ul>
 * <li>Create Resource Manager's active objects at its initialization</li>
 * <li>Keep an up-to-date list of nodes able to perform scheduler's tasks.</li>
 * <li>Give nodes to the Scheduler asked by {@link org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface} object, with a node selection mechanism performed by {@link SelectionScript}.</li>
 * <li>Dialog with node sources which add and remove nodes to the Core.</li>
 * <li>Perform creation and removal of NodeSource objects.</li>
 * <li>Treat removing nodes and adding nodes requests</li>
 * <li>Create and launch RMEvents concerning nodes and nodes sources to {@link RMMonitoring} active object.</li>
 * </ul>
 * <p>
 * Nodes in Resource Manager are represented by {@link RMNode objects}. RMcore
 * has to manage different states of nodes:
 * <ul>
 * <li>Free: node is ready to perform a task.</li>
 * <li>Busy: node is executing a task.</li>
 * <li>To be removed: node is busy and have to be removed at the end of the its current task.</li>
 * <li>Down: node is broken, and not anymore able to perform tasks.</li>
 * </ul>
 * <p>
 * RMCore is not responsible of creation, acquisition and monitoring of nodes,
 * these points are performed by {@link NodeSource} objects.
 * <p>
 * WARNING: you must instantiate this class as an Active Object!
 * <p>
 * RmCore should be non-blocking which means:
 * <ul>
 * <li>No direct access to nodes.</li>
 * <li>All method calls to other active objects should be either asynchronous or non-blocking immediate services.</li>
 * <li>Methods which have to return something depending on another active objects should use an automatic continuation.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@ActiveObject
public class RMCore implements ResourceManager, InitActive, RunActive {

    private static final String CONTACT_UPGRADE_MESSAGE = "Number of nodes exceed the limitation from your contract. Please send an email to contact@activeeon.com for an upgrade.";

    /**
     * Limits the number of nodes the Resource Manager accepts. >-1 or null means UNLIMITED, <=0 enforces the limit.
     * Explanation: This software can be licensed to a certain amount of nodes.
     * This variable is not final because the compiler inlines final variables and this variable is changed
     * by the gradle release build inside the .class files (after compilation).
     * Long is used instead of long (primitive) because this variable is replaced inside the byte code after
     * optimization, which didn't work with a long value. Because that long value was initialized inside a
     * static block in the byte code, that interfered with replacing it.
     */
    private static Long maximumNumberOfNodes;

    /**
     * Log4J logger name for RMCore
     */
    private final static Logger logger = Logger.getLogger(RMCore.class);

    /**
     * If RMCore Active object
     */
    private String id;

    /**
     * ProActive Node containing the RMCore
     */
    private Node nodeRM;

    /**
     * stub of RMMonitoring active object of the RM
     */
    private RMMonitoringImpl monitoring;

    /**
     * authentication active object
     */
    private RMAuthenticationImpl authentication;

    /**
     * HashMap of NodeSource active objects
     */
    private Map<String, NodeSource> nodeSources;

    private List<String> brokenNodeSources;

    /**
     * HashMaps of nodes known by the RMCore
     */
    private volatile Map<String, RMNode> allNodes;

    /**
     * List of nodes that are eligible for Scheduling.
     * It corresponds to nodes that are in the `FREE` state and not locked.
     * Nodes which are locked are not part of this list.
     **/
    private List<RMNode> eligibleNodes;

    private SelectionManager selectionManager;

    /**
     * indicates that RMCore must shutdown
     */
    private boolean toShutDown = false;

    private boolean shutedDown = false;

    private Client caller = null;

    /**
     * Any local active object (including a half body) will act as the same single client
     */
    private static final Client localClient = new Client(null, false);

    /**
     * Map of connected clients and internal services that have an access to the core.
     * It is statically used due to drawbacks in the client pinger functionality
     *
     * @see Client
     */
    public static final Map<UniqueID, Client> clients = Collections.synchronizedMap(new HashMap<UniqueID, Client>());

    /**
     * Nodes topology
     */
    public static TopologyManager topologyManager;

    /**
     * Client pinger
     */
    private ClientPinger clientPinger;

    /**
     * an active object used to clean nodes when they are released after computations
     */
    private NodesCleaner nodesCleaner;

    private RMAccountsManager accountsManager;

    private RMJMXHelper jmxHelper;

    /**
     * utility ao used to configure nodes (compute topology, configure dataspaces...)
     */
    private RMNodeConfigurator nodeConfigurator;

    private RMDBManager dbManager;

    private NodesRecoveryManager nodesRecoveryManager;

    /**
     * ProActive Empty constructor
     */
    public RMCore() {
    }

    /**
     * Creates the RMCore object.
     *
     * @param id     Name for RMCOre.
     * @param nodeRM Name of the ProActive Node object containing RM active
     *               objects.
     * @throws ActiveObjectCreationException if creation of the active object failed.
     * @throws NodeException                 if a problem occurs on the target node.
     */
    public RMCore(String id, Node nodeRM) throws ActiveObjectCreationException, NodeException {
        this.id = id;
        this.nodeRM = nodeRM;

        nodeSources = new HashMap<>();
        brokenNodeSources = new ArrayList<>();
        allNodes = new HashMap<>();
        eligibleNodes = Collections.synchronizedList(new ArrayList<RMNode>());

        this.accountsManager = new RMAccountsManager();
        this.jmxHelper = new RMJMXHelper(this.accountsManager);
    }

    public RMCore(Map<String, NodeSource> nodeSources, List<String> brokenNodeSources, Map<String, RMNode> allNodes,
            Client caller, RMMonitoringImpl monitoring, SelectionManager manager, List<RMNode> freeNodesList,
            RMDBManager newDataBaseManager) {
        this.nodeSources = nodeSources;
        this.brokenNodeSources = brokenNodeSources;
        this.allNodes = allNodes;
        this.caller = caller;
        this.monitoring = monitoring;
        this.selectionManager = manager;
        this.eligibleNodes = freeNodesList;
        this.dbManager = newDataBaseManager;
    }

    /**
     * Initialization part of the RMCore active object.
     * Create RM's active objects and the default static Node Source named
     * {@link RMConstants#DEFAULT_STATIC_SOURCE_NAME}. Finally, it throws the RM
     * started event.
     *
     * @param body the active object's body.
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("RMCore start : initActivity");
        }

        try {
            // setting up the policy
            logger.debug("Setting up the resource manager security policy");
            ClientsPolicy.init();

            StubObject rmCoreStub = PAActiveObject.getStubOnThis();

            PAActiveObject.registerByName(rmCoreStub, RMConstants.NAME_ACTIVE_OBJECT_RMCORE);

            dbManager = RMDBManager.getInstance();

            if (logger.isDebugEnabled()) {
                logger.debug("Creating RMAuthentication active object");
            }

            authentication = (RMAuthenticationImpl) PAActiveObject.newActive(RMAuthenticationImpl.class.getName(),
                                                                             new Object[] { rmCoreStub },
                                                                             nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("Creating RMMonitoring active object");
            }

            // Boot the JMX infrastructure
            this.jmxHelper.boot(authentication);

            monitoring = (RMMonitoringImpl) PAActiveObject.newActive(RMMonitoringImpl.class.getName(),
                                                                     new Object[] { rmCoreStub },
                                                                     nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("Creating SelectionManager active object");
            }
            selectionManager = (SelectionManager) PAActiveObject.newActive(ProbablisticSelectionManager.class.getName(),
                                                                           new Object[] { rmCoreStub },
                                                                           nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("Creating ClientPinger active object");
            }
            clientPinger = (ClientPinger) PAActiveObject.newActive(ClientPinger.class.getName(),
                                                                   new Object[] { rmCoreStub },
                                                                   nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("Creating NodeCleaner active object");
            }
            nodesCleaner = (NodesCleaner) PAActiveObject.newActive(NodesCleaner.class.getName(),
                                                                   new Object[] { rmCoreStub },
                                                                   nodeRM);

            topologyManager = new TopologyManager();

            nodeConfigurator = (RMNodeConfigurator) PAActiveObject.newActive(RMNodeConfigurator.class.getName(),
                                                                             new Object[] { rmCoreStub },
                                                                             nodeRM);

            // adding shutdown hook
            final RMCore rmcoreStub = (RMCore) rmCoreStub;
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (!toShutDown) {
                        rmcoreStub.shutdown(true);
                    }

                    synchronized (nodeRM) {
                        if (!shutedDown) {
                            try {
                                // wait for rmcore shutdown (5 min at most)
                                nodeRM.wait(5 * 60 * 60 * 1000);
                            } catch (InterruptedException e) {
                                logger.warn("shutdown hook interrupted", e);
                            }
                        }
                    }
                }
            });

            // Creating RM started event
            this.monitoring.rmEvent(new RMEvent(RMEventType.STARTED));

            authentication.setActivated(true);

            clientPinger.ping();

            initiateRecoveryIfRequired();

        } catch (ActiveObjectCreationException e) {
            logger.error("", e);
        } catch (NodeException e) {
            logger.error("", e);
        } catch (ProActiveException e) {
            logger.error("", e);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("RMCore end: initActivity");
        }
    }

    void initiateRecoveryIfRequired() {
        nodesRecoveryManager = getNodesRecoveryManagerBuilder().apply(this);
        nodesRecoveryManager.initialize();
        if (RM_NODES_RECOVERY.getValueAsBoolean()) {
            logger.info("Starting RM nodes recovery");
            restoreNodesAndNodeSources();
        } else {
            logger.info("RM nodes recovery is disabled. Removing all nodes from database");
            dbManager.removeAllNodes();
        }
    }

    Function<RMCore, NodesRecoveryManager> getNodesRecoveryManagerBuilder() {
        return new Function<RMCore, NodesRecoveryManager>() {
            @Override
            public NodesRecoveryManager apply(RMCore rmCore) {
                return new NodesRecoveryManager(rmCore);
            }
        };
    }

    @VisibleForTesting
    boolean restoreNodesAndNodeSources() {
        Collection<NodeSourceData> nodeSources = dbManager.getNodeSources();

        for (NodeSourceData nodeSourceData : nodeSources) {
            String nodeSourceDataName = nodeSourceData.getName();

            if (NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(nodeSourceDataName)) {
                // will be recreated by SchedulerStarter
                dbManager.removeNodeSource(nodeSourceDataName);
            } else {
                try {
                    logger.info("Restoring node source " + nodeSourceDataName);
                    createNodeSource(nodeSourceData, true);
                } catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                    brokenNodeSources.add(nodeSourceDataName);
                }
            }
        }

        return true;
    }

    private void restoreNodes(NodeSource nodeSource) {
        logger.info("Start to recover nodes");
        int lookUpTimeout = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT.getValueAsInt();
        String nodeSourceName = nodeSource.getName();

        makeSureNodeSourceHasNoNode(nodeSource, nodeSourceName);

        Collection<RMNodeData> nodesData = dbManager.getNodesByNodeSource(nodeSourceName);
        logger.info("Number of nodes found in database for node source " + nodeSourceName + ": " + nodesData.size());

        Map<NodeState, Integer> nodeStates = new HashMap<>();
        int totalEligibleRecoveredNodes = 0;

        // for each node found in database, try to lookup node or recover it
        // as down node
        for (RMNodeData rmNodeData : nodesData) {
            String nodeUrl = rmNodeData.getNodeUrl();
            RMNode rmnode = null;

            Node node = tryToLookupNode(nodeSource, lookUpTimeout, nodeUrl);

            if (node != null) {
                rmnode = recoverInternalNode(nodeSource, rmNodeData, nodeUrl, node);
                nodesRecoveryManager.restoreLocks(rmnode, rmNodeData.getProvider());
                Integer nbNodesInState = nodeStates.get(rmnode.getState());
                int newNbNodesInState = nbNodesInState == null ? 1 : nbNodesInState + 1;
                nodeStates.put(rmnode.getState(), newNbNodesInState);
            } else {
                // the node is not recoverable and does not appear in any data
                // structures: we can remove it safely from database
                dbManager.removeNode(rmNodeData);
                triggerDownNodeHandling(nodeSource, rmNodeData, nodeUrl);
            }
            // we must add the recreated node to the eligible data
            // structure if we want it to be usable by a task
            if (isEligible(rmnode)) {
                eligibleNodes.add(rmnode);
                totalEligibleRecoveredNodes++;
            }
        }
        int totalRecoveredNodes = 0;
        logger.info("Recovered nodes:");
        for (Entry<NodeState, Integer> nodeStateIntEntry : nodeStates.entrySet()) {
            logger.info("- nodes in " + nodeStateIntEntry.getKey() + " state: " + nodeStateIntEntry.getValue());
            totalRecoveredNodes += nodeStateIntEntry.getValue();
        }
        logger.info("Total number of nodes recovered: " + totalRecoveredNodes + ", including eligible nodes: " +
                    totalEligibleRecoveredNodes);
        logger.info("End of nodes recovery");
    }

    private RMNode recoverInternalNode(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl, Node node) {
        RMNode rmNode = null;
        // the node has been successfully looked up, we compare its
        // information to the node data retrieved in database.
        if (rmNodeData.equalsToNode(node)) {
            logger.info("Node to recover could successfully be looked up at URL: " + nodeUrl);
            rmNode = nodeSource.internalAddNodeAfterRecovery(node, rmNodeData);
            this.allNodes.put(rmNode.getNodeURL(), rmNode);
        } else {
            logger.warn("The node that has been looked up does not have the same information as the node to recover: " +
                        node.getNodeInformation().getName() + " is not equal to " + rmNodeData.getName() + " or " +
                        node.getNodeInformation().getURL() + " is not equal to " + rmNodeData.getNodeUrl());
        }
        return rmNode;
    }

    private void triggerDownNodeHandling(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl) {
        // if the node to recover was in deploying state then we have
        // nothing to do as it is going to be redeployed
        if (!rmNodeData.getState().equals(NodeState.DEPLOYING)) {
            // inform the node source that this recreated node is down
            nodeSource.detectedPingedDownNode(rmNodeData.getName(), nodeUrl);
        }
    }

    private Node tryToLookupNode(NodeSource nodeSource, int lookUpTimeout, String nodeUrl) {
        Node node;
        try {
            logger.info("Trying to lookup a node to recover: " + nodeUrl);
            node = nodeSource.lookupNode(nodeUrl, lookUpTimeout);
        } catch (Exception e) {
            logger.warn("Node to recover could not be looked up at URL: " + nodeUrl, e);
            node = null;
        }
        return node;
    }

    private void makeSureNodeSourceHasNoNode(NodeSource nodeSource, String nodeSourceName) {
        int nodesCount = nodeSource.getNodesCount();
        if (nodesCount != 0) {
            logger.warn("Recovered node source " + nodeSourceName + " unexpectedly already has nodes registered.");
        }
    }

    /**
     * RunActivity periodically send "alive" event to listeners
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        // recalculating nodes number only once per policy period
        while (body.isActive()) {

            Request request = null;
            try {
                request = service.blockingRemoveOldest(PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.getValueAsLong());

                if (request != null) {
                    try {
                        try {
                            caller = checkMethodCallPermission(request.getMethodName(), request.getSourceBodyID());
                            service.serve(request);
                        } catch (SecurityException ex) {
                            logger.warn("Cannot serve request: " + request, ex);
                            service.serve(new ThrowExceptionRequest(request, ex));
                        }
                    } catch (Throwable e) {
                        logger.error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("runActivity interrupted", e);
            }
        }
    }

    /**
     * Returns a node object to a corresponding URL.
     *
     * @param url url of the node asked.
     * @return RMNode object containing the node.
     */
    private RMNode getNodebyUrl(String url) {
        return allNodes.get(url);
    }

    protected RMNode getNodeByUrlIncludingDeployingNodes(String url) {
        RMNode nodeByUrl = getNodebyUrl(url);

        if (nodeByUrl != null) {
            return nodeByUrl;
        } else {
            String[] chunks = url.split("/");

            if (chunks.length >= 3) {
                String nodeSourceName = chunks[2];
                NodeSource nodeSource = nodeSources.get(nodeSourceName);

                if (nodeSource != null) {
                    return nodeSource.getDeployingNode(url);
                }
            }
        }

        return null;
    }

    /**
     * Change the state of the node to free, after a Task has been completed by the specified node.
     * The node passed as parameter is moved to the list of nodes which are eligible for scheduling if it is not locked.
     * In all cases, an event informing the node state's change is propagated to RMMonitoring.
     *
     * @param rmNode node to set free.
     * @return true if the node successfully set as free, false if it was down before.
     */
    @VisibleForTesting
    BooleanWrapper internalSetFree(final RMNode rmNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Current node state " + rmNode.getState() + " " + rmNode.getNodeURL());
            logger.debug("Setting node state to free " + rmNode.getNodeURL());
        }

        // If the node is already free no need to go further
        if (rmNode.isFree()) {
            return new BooleanWrapper(true);
        }

        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();

        Client client = rmNode.getOwner();
        if (client == null) {
            // node has been just configured, so the user initiated this action is the node provider
            client = rmNode.getProvider();
        }

        // resetting owner here
        rmNode.setFree();
        // an eligible node is a node that is free and not locked
        if (!rmNode.isLocked()) {
            this.eligibleNodes.add(rmNode);
        }

        // persist the state change to the database
        persistUpdatedRMNode(rmNode);

        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(NODE_STATE_CHANGED, previousNodeState, client.getName()));

        return new BooleanWrapper(true);
    }

    /**
     * Mark nodes as free after cleaning procedure.
     *
     * @param nodes to be free
     * @return true if all successful, false if there is a down node among nodes
     */
    public BooleanWrapper setFreeNodes(List<RMNode> nodes) {
        boolean result = true;
        for (RMNode node : nodes) {
            // getting the correct instance
            RMNode rmnode = this.getNodebyUrl(node.getNodeURL());
            // freeing it
            result &= internalSetFree(rmnode).getBooleanValue();
        }
        return new BooleanWrapper(result);
    }

    /**
     * Mark node to be removed after releasing.
     *
     * @param rmNode node to be removed after node is released.
     */
    void internalSetToRemove(final RMNode rmNode, Client initiator) {
        // If the node is already marked to be removed, so no need to go further
        if (rmNode.isToRemove()) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Prepare to removing the node " + rmNode.getNodeURL());
        }
        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();
        rmNode.setToRemove();

        // persist the state change to the database
        persistUpdatedRMNode(rmNode);

        // create the event
        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(NODE_STATE_CHANGED,
                                                             previousNodeState,
                                                             initiator.getName()));
    }

    /**
     * Performs an RMNode release from the Core At this point the node is at
     * free or 'to be released' state. do the release, and confirm to NodeSource
     * the removal.
     *
     * @param rmnode the node to release
     */
    private void removeNodeFromCoreAndSource(RMNode rmnode, Client initiator) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing node " + rmnode.getNodeURL());
        }
        removeNodeFromCore(rmnode, initiator);
        rmnode.getNodeSource().removeNode(rmnode.getNodeURL(), initiator);
    }

    /**
     * Internal operations to remove the node from Core. RMNode object is
     * removed from {@link RMCore#allNodes}, removal Node event is thrown to
     * RMMonitoring Active object.
     *
     * @param rmnode the node to remove.
     */
    private void removeNodeFromCore(RMNode rmnode, Client initiator) {
        logger.debug("Removing node " + rmnode.getNodeURL() + " provided by " + rmnode.getProvider());
        // removing the node from the HM list
        if (rmnode.isFree()) {
            eligibleNodes.remove(rmnode);
        }
        this.allNodes.remove(rmnode.getNodeURL());

        // persist node removal
        dbManager.removeNode(rmnode);

        // create the event
        this.registerAndEmitNodeEvent(rmnode.createNodeEvent(RMEventType.NODE_REMOVED,
                                                             rmnode.getState(),
                                                             initiator.getName()));
    }

    /**
     * Internal operation of registering a new node in the Core
     * This step is done after node configuration ran by {@link RMNodeConfigurator} active object.
     *
     * @param configuredNode the node that is going to be added.
     */
    public void internalAddNodeToCore(RMNode configuredNode) {
        String nodeURL = configuredNode.getNodeURL();
        if (!this.allNodes.containsKey(nodeURL)) {
            //does nothing, the node has been removed preemptively
            //during its configuration
            logger.debug("internalAddNodeToCore returned immediately because the node " + nodeURL + " was not known");
            return;
        }
        //was added during internalRegisterConfiguringNode
        RMNode rmnode = this.allNodes.remove(nodeURL);
        this.allNodes.put(nodeURL, configuredNode);

        if (toShutDown) {
            logger.warn("Node " + rmnode.getNodeURL() +
                        " will not be added to the core as the resource manager is shutting down");
            removeNodeFromCoreAndSource(rmnode, rmnode.getProvider());
            return;
        }

        //noinspection ConstantConditions
        if (isNumberOfNodesLimited() && isMaximumNumberOfNodesReachedIncludingAskingNode()) {
            logger.warn("Node " + rmnode.getNodeURL() + " is removed because the Resource Manager is limited to " +
                        maximumNumberOfNodes + " nodes." + CONTACT_UPGRADE_MESSAGE);
            removeNodeFromCoreAndSource(rmnode, rmnode.getProvider());
            throw new AddingNodesException("Maximum number of nodes reached: " + maximumNumberOfNodes + ". " +
                                           CONTACT_UPGRADE_MESSAGE);
        }

        //during the configuration process, the rmnode can be removed. Its state would be toRemove
        if (rmnode.isToRemove()) {
            removeNodeFromCoreAndSource(rmnode, rmnode.getProvider());
            return;
        }

        //during the configuration process, the node has been detected down by the nodesource.
        //discarding the registration
        if (rmnode.isDown()) {
            logger.debug("internalAddNodeToCore returned immediately because the node " + nodeURL + " is already down");
            return;
        }

        internalSetFree(configuredNode);
    }

    private boolean isNumberOfNodesLimited() {
        return maximumNumberOfNodes != null && maximumNumberOfNodes >= 0;
    }

    /**
     * Gives total number of alive nodes handled by RM
     *
     * @return total number of alive nodes
     */
    private int getTotalAliveNodesNumber() {
        int count = 0;
        for (RMNode node : allNodes.values()) {
            if (!node.isDown())
                count++;
        }
        return count;
    }

    /**
     * This methods returns true if the maximum number of nodes is reached.
     * This method assumes that the currently asking node is already regsitered
     * inside the allNodes list.
     *
     * @return
     */
    private boolean isMaximumNumberOfNodesReachedIncludingAskingNode() {
        // > because the currently added is is assumed to be already inside the allNodes list.
        return this.getTotalAliveNodesNumber() > maximumNumberOfNodes;
    }

    /**
     * Internal operation of configuring a node. The node is not useable by a final user
     * ( not eligible thanks to getNode methods ) if it is in configuration state.
     * This method is called by {@link RMNodeConfigurator} to notify the core that the
     * process of configuring the rmnode has started. The end of the process will be
     * notified thanks to the method internalAddNodeToCore(RMNode)
     *
     * @param rmnode the node in the configuration state
     */
    public void internalRegisterConfiguringNode(RMNode rmnode) {
        if (toShutDown) {
            logger.warn("The RM core is shutting down, cannot configure the node");
            rmnode.getNodeSource().removeNode(rmnode.getNodeURL(), rmnode.getProvider());
            return;
        }

        rmnode.setConfiguring(rmnode.getProvider());

        //we add the configuring node to the collection to be able to ping it
        this.allNodes.put(rmnode.getNodeURL(), rmnode);

        // save the information of this new node in DB, in particular its state
        persistNewRMNode(rmnode);

        // create the event
        this.registerAndEmitNodeEvent(rmnode.createNodeEvent(RMEventType.NODE_ADDED,
                                                             null,
                                                             rmnode.getProvider().getName()));

        if (logger.isDebugEnabled()) {
            logger.debug("Configuring node " + rmnode.getNodeURL());
        }

        //now configuring the newly looked up node
        nodeConfigurator.configureNode(rmnode);
    }

    public String getId() {
        return this.id;
    }

    /**
     * Returns the url of the node where the rm core is running
     *
     * @return the url of the node where the rm core is running
     */
    private String getUrl() {

        if (System.getProperty("rm.url") != null) {
            return System.getProperty("rm.url");
        }

        try {
            String aoUrl = PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis());
            if (aoUrl != null) {
                String rmUrl = aoUrl.replaceAll(PAResourceManagerProperties.RM_NODE_NAME.getValueAsString(), "");
                System.setProperty("rm.url", rmUrl);
                return rmUrl;
            } else {
                return "No default RM URL";
            }
        } catch (Throwable e) {
            logger.error("Unable to get RM URL", e);
            return "No default RM URL";
        }
    }

    // ----------------------------------------------------------------------
    // Methods called by RMAdmin
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addNode(String nodeUrl) {
        return addNode(nodeUrl, NodeSource.DEFAULT);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addNode(String nodeUrl, String sourceName) {
        if (toShutDown) {
            throw new AddingNodesException("The resource manager is shutting down");
        }

        boolean existingNodeSource = nodeSources.containsKey(sourceName);

        if (!existingNodeSource && sourceName.equals(NodeSource.DEFAULT)) {
            // creating the default node source
            createNodeSource(NodeSource.DEFAULT,
                             DefaultInfrastructureManager.class.getName(),
                             null,
                             StaticPolicy.class.getName(),
                             null).getBooleanValue();
        }

        if (nodeSources.containsKey(sourceName)) {
            NodeSource nodeSource = this.nodeSources.get(sourceName);

            // Known URL, so do some cleanup before replacing it
            if (allNodes.containsKey(nodeUrl)) {

                if (!allNodes.get(nodeUrl).getNodeSourceName().equals(sourceName)) {
                    // trying to already registered node to another node source
                    // do nothing in this case
                    throw new AddingNodesException("An attempt to add a node " + nodeUrl +
                                                   " registered in one node source to another one");
                }
            }
            return nodeSource.acquireNode(nodeUrl, caller);
        } else {
            throw new AddingNodesException("Unknown node source " + sourceName);
        }
    }

    /**
     * Removes a node from the RM. This method also handles deploying node removal ( deploying node's url
     * follow the scheme deploying:// ). In such a case, the preempt parameter is not used.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed. ( ignored if deploying node )
     */
    public BooleanWrapper removeNode(String nodeUrl, boolean preempt) {
        return removeNode(nodeUrl, preempt, false);
    }

    public BooleanWrapper removeNode(String nodeUrl, boolean preempt, boolean isTriggeredFromShutdownHook) {
        //waiting for better integration of deploying node
        //if we get a "deploying node url" we change the flow
        if (RMNodeHelper.isDeployingNodeURL(nodeUrl)) {

            RMNode deployingNode = getNodeByUrlIncludingDeployingNodes(nodeUrl);

            if (!isTriggeredFromShutdownHook && deployingNode != null && deployingNode.isLocked()) {
                dbManager.createLockEntryOrUpdate(deployingNode.getNodeSourceName(),
                                                  RMDBManager.NodeLockUpdateAction.DECREMENT);
            }

            return new BooleanWrapper(removeDeployingNode(nodeUrl));
        }

        if (this.allNodes.containsKey(nodeUrl)) {
            RMNode rmnode = this.allNodes.get(nodeUrl);
            logger.debug("Request to remove node " + rmnode);

            // checking if the caller is the node administrator
            checkNodeAdminPermission(rmnode, caller);

            if (!isTriggeredFromShutdownHook && rmnode.isLocked()) {
                dbManager.createLockEntryOrUpdate(rmnode.getNodeSourceName(),
                                                  RMDBManager.NodeLockUpdateAction.DECREMENT);
            }

            if (rmnode.isDown() || preempt || rmnode.isFree() || rmnode.isLocked()) {
                removeNodeFromCoreAndSource(rmnode, caller);
            } else if (rmnode.isBusy() || rmnode.isConfiguring()) {
                internalSetToRemove(rmnode, caller);
            }
        } else {
            logger.warn("An attempt to remove a non existing node: " + nodeUrl + " was made. Ignoring it");
            return new BooleanWrapper(false);
        }

        return new BooleanWrapper(true);
    }

    /**
     * Removes "number" of nodes from the node source.
     *
     * @param number         amount of nodes to be released
     * @param nodeSourceName a node source name
     * @param preemptive     if true remove nodes immediately without waiting while they will be freed
     */
    public void removeNodes(int number, String nodeSourceName, boolean preemptive) {
        int numberOfRemovedNodes = 0;

        // temporary list to avoid concurrent modification
        List<RMNode> nodelList = new LinkedList<>();
        nodelList.addAll(eligibleNodes);

        logger.debug("Free nodes size " + nodelList.size());
        for (RMNode node : nodelList) {

            if (numberOfRemovedNodes == number) {
                break;
            }

            if (node.getNodeSource().getName().equals(nodeSourceName)) {
                removeNode(node.getNodeURL(), preemptive);
                numberOfRemovedNodes++;
            }
        }

        nodelList.clear();
        nodelList.addAll(allNodes.values());
        logger.debug("All nodes size " + nodelList.size());
        if (numberOfRemovedNodes < number) {
            for (RMNode node : nodelList) {

                if (numberOfRemovedNodes == number) {
                    break;
                }

                if (node.isBusy() && node.getNodeSource().getName().equals(nodeSourceName)) {
                    removeNode(node.getNodeURL(), preemptive);
                    numberOfRemovedNodes++;
                }
            }
        }

        if (numberOfRemovedNodes < number) {
            logger.warn("Cannot remove " + number + " nodes from node source " + nodeSourceName);
        }
    }

    /**
     * Removes all nodes from the specified node source.
     *
     * @param nodeSourceName a name of the node source
     * @param preemptive     if true remove nodes immediately without waiting while they will be freed
     */
    public void removeAllNodes(String nodeSourceName, boolean preemptive) {
        removeAllNodes(nodeSourceName, preemptive, false);
    }

    /**
     * Removes all nodes from the specified node source.
     *
     * @param nodeSourceName              a name of the node source
     * @param preemptive                  if true remove nodes immediately without waiting while they will be freed
     * @param isTriggeredFromShutdownHook boolean saying if the calling is performed from a shutdown hook.
     */
    public void removeAllNodes(String nodeSourceName, final boolean preemptive,
            final boolean isTriggeredFromShutdownHook) {

        removeAllNodes(nodeSourceName, "deploying nodes", new Function<NodeSource, Void>() {
            @Override
            public Void apply(NodeSource nodeSource) {
                for (RMDeployingNode pn : nodeSource.getDeployingNodes()) {
                    removeNode(pn.getNodeURL(), preemptive, isTriggeredFromShutdownHook);
                }
                return null;
            }
        });

        removeAllNodes(nodeSourceName, "alive nodes", new RemoveAllNodes(new Function<NodeSource, LinkedList<Node>>() {
            @Override
            public LinkedList<Node> apply(NodeSource nodeSource) {
                return nodeSource.getAliveNodes();
            }
        }, preemptive, isTriggeredFromShutdownHook));

        removeAllNodes(nodeSourceName, "down nodes", new RemoveAllNodes(new Function<NodeSource, LinkedList<Node>>() {
            @Override
            public LinkedList<Node> apply(NodeSource nodeSource) {
                return nodeSource.getDownNodes();
            }
        }, preemptive, isTriggeredFromShutdownHook));
    }

    private final class RemoveAllNodes implements Function<NodeSource, Void> {

        private final Function<NodeSource, LinkedList<Node>> nodeExtractorFunction;

        private final boolean preemptive;

        private final boolean isTriggeredFromShutdownHook;

        private RemoveAllNodes(Function<NodeSource, LinkedList<Node>> nodeExtractorFunction, boolean preemptive,
                boolean isTriggeredFromShutdownHook) {
            this.nodeExtractorFunction = nodeExtractorFunction;
            this.preemptive = preemptive;
            this.isTriggeredFromShutdownHook = isTriggeredFromShutdownHook;
        }

        @Override
        public Void apply(NodeSource nodeSource) {
            LinkedList<Node> nodes = nodeExtractorFunction.apply(nodeSource);

            if (nodes != null) {
                for (Node node : nodes) {
                    removeNode(node.getNodeInformation().getURL(), preemptive, isTriggeredFromShutdownHook);
                }
            }

            return null;
        }

    }

    /**
     * Wraps the access to the node source to perform a defensive check preventing NPE.
     *
     * @param nodeSourceName the name of the node source to retrieve.
     * @param collectionName the name of the collection to iterate in the node source.
     * @param function       a function that extracts the collection to iterate from the node source.
     */
    private void removeAllNodes(String nodeSourceName, String collectionName, Function<NodeSource, Void> function) {
        NodeSource nodeSource = nodeSources.get(nodeSourceName);

        if (nodeSource != null) {
            function.apply(nodeSource);
        } else {
            logger.warn("Trying to remove  " + collectionName + " from a node source that is no longer known: " +
                        nodeSourceName);
        }
    }

    /**
     * Returns true if the node nodeUrl is registered (i.e. known by the RM). Note that
     * true is returned even if the node is down.
     *
     * @param nodeUrl the tested node.
     * @return true if the node nodeUrl is registered.
     */
    public BooleanWrapper nodeIsAvailable(String nodeUrl) {
        final RMNode node = this.allNodes.get(nodeUrl);
        return new BooleanWrapper(node != null && !node.isDown());
    }

    /**
     * This method is called periodically by ProActive Nodes to inform the
     * Resource Manager of a possible reconnection. The method is also used by
     * ProActive Nodes to know if they are still known by the Resource Manager.
     * For instance a Node which has been removed by a user from the
     * Resource Manager is no longer known.
     * <p>
     * The method is defined as Immediate Service. This way it is executed in
     * a dedicated Thread. It is essential in order to allow other methods to
     * be executed immediately even if incoming connection to the Nodes is stopped
     * or filtered while a timeout occurs when this method tries to send back a reply.
     * <p>
     * The {@code allNodes} data-structure is written by a single Thread only
     * but read by multiple Threads. The data-structure is marked as volatile
     * to ensure visibility.
     * <p>
     * Parallel executions of this method must involves different {@code nodeUrl}s.
     * <p>
     * The underlying calls to {@code setBusyNode} and {@code internalSetFree}
     * are writing to the {@code freeNodes} data-structure. It explains why this last
     * is synchronized (thread-safe).
     *
     * @param nodeUrls the URLs of the workers associated to the node that publishes the update.
     * @return The set of worker node URLs that are unknown to the Resource Manager
     * (i.e. have been removed by a user).
     */
    @ImmediateService
    @Override
    public Set<String> setNodesAvailable(Set<String> nodeUrls) {
        if (logger.isTraceEnabled()) {
            logger.trace("Received availability for the following workers: " + nodeUrls);
        }

        ImmutableSet.Builder<String> nodeUrlsNotKnownByTheRM = new ImmutableSet.Builder<>();

        for (String nodeUrl : nodeUrls) {
            RMNode node = this.allNodes.get(nodeUrl);

            if (node == null) {
                logger.warn("Cannot set node as available, the node is unknown: " + nodeUrl);
                nodeUrlsNotKnownByTheRM.add(nodeUrl);
            } else if (node.isDown()) {
                restoreNodeState(nodeUrl, node);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("The node identified by " + nodeUrl + " is known but not DOWN, no action performed");
                }
            }
        }
        return nodeUrlsNotKnownByTheRM.build();
    }

    @VisibleForTesting
    void restoreNodeState(String nodeUrl, RMNode node) {
        NodeState previousNodeState = node.getLastEvent().getPreviousNodeState();

        if (previousNodeState == NodeState.BUSY) {
            logger.info("Restoring DOWN node to BUSY: " + nodeUrl);
            setBusyNode(nodeUrl, node.getOwner());
        } else {
            logger.info("Restoring DOWN node to FREE: " + nodeUrl);
            internalSetFree(node);
        }

        node.getNodeSource().setNodeAvailable(node);
    }

    public NodeState getNodeState(String nodeUrl) {
        RMNode node = this.allNodes.get(nodeUrl);
        if (node == null) {
            throw new IllegalArgumentException("Unknown node " + nodeUrl);
        }
        return node.getState();
    }

    /**
     * Creates a new node source with specified name, infrastructure manages {@link InfrastructureManager}
     * and acquisition policy {@link NodeSourcePolicy}.
     *
     * @param nodeSourceName           the name of the node source
     * @param infrastructureType       type of the underlying infrastructure
     * @param infrastructureParameters parameters for infrastructure creation
     * @param policyType               name of the policy type. It passed as a string due to pluggable approach {@link NodeSourcePolicyFactory}
     * @param policyParameters         parameters for policy creation
     */

    public BooleanWrapper createNodeSource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters) {

        if (nodeSourceName == null) {
            throw new IllegalArgumentException("Node Source name cannot be null");
        }
        nodeSourceName = nodeSourceName.trim();

        NodeSourceData nodeSourceData = new NodeSourceData(nodeSourceName,
                                                           infrastructureType,
                                                           infrastructureParameters,
                                                           policyType,
                                                           policyParameters,
                                                           caller);
        boolean added = dbManager.addNodeSource(nodeSourceData);

        try {
            return createNodeSource(nodeSourceData, false);
        } catch (RuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            if (added) {
                dbManager.removeNodeSource(nodeSourceName);
            }
            throw ex;
        }
    }

    protected BooleanWrapper createNodeSource(NodeSourceData nodeSourceData, boolean isRecovery) {
        String nodeSourceName = nodeSourceData.getName();

        //checking that nsname doesn't contain invalid characters and doesn't exist yet
        checkNodeSourceName(nodeSourceName);

        logger.info("Creating a node source : " + nodeSourceName);

        // if this is a recovery, we also need to check whether there are
        // nodes in database for this node source, otherwise, we will do a
        // redeployment from scratch. The reason is that when the RM shuts
        // down correctly, it removes all its nodes. Thus if we restart and
        // recover the RM afterwards there will be no nodes in the database,
        // but there will be no redeployment neither, hence the special case here.
        boolean recoverNodes = existNodesToRecover(isRecovery, nodeSourceName);

        InfrastructureManager im;

        // we need to reload the infrastructure variables saved in database if
        // we recover the nodes
        if (recoverNodes) {
            im = InfrastructureManagerFactory.recover(nodeSourceData);
        } else {
            im = InfrastructureManagerFactory.create(nodeSourceData);
        }

        NodeSourcePolicy policy = NodeSourcePolicyFactory.create(nodeSourceData.getPolicyType(),
                                                                 nodeSourceData.getInfrastructureType(),
                                                                 nodeSourceData.getPolicyParameters());

        NodeSource nodeSource;
        Client provider = nodeSourceData.getProvider();

        try {
            nodeSource = new NodeSource(this.getUrl(),
                                        nodeSourceName,
                                        provider,
                                        im,
                                        policy,
                                        (RMCore) PAActiveObject.getStubOnThis(),
                                        this.monitoring);
            nodeSource = PAActiveObject.turnActive(nodeSource, nodeRM);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Cannot create node source " + nodeSourceName, e);
        }

        // finally recover the nodes from a saved state if needed
        if (recoverNodes) {
            restoreNodes(nodeSource);
        }

        // Adding access to the core for node source and policy.
        // In order to do it node source and policy active objects are added to the clients list.
        // They will be removed from this list when node source is unregistered.
        UniqueID nsId = Client.getId(nodeSource);
        UniqueID policyId = Client.getId(policy);
        if (nsId == null || policyId == null) {
            throw new IllegalStateException("Cannot register the node source");
        }

        BooleanWrapper result = nodeSource.activate();
        if (!result.getBooleanValue()) {
            logger.error("Node source " + nodeSourceName + " cannot be activated");
        }

        Client nsService = new Client(provider.getSubject(), false);
        Client policyService = new Client(provider.getSubject(), false);

        nsService.setId(nsId);
        policyService.setId(policyId);

        RMCore.clients.put(nsId, nsService);
        RMCore.clients.put(policyId, policyService);

        this.nodeSources.put(nodeSourceName, nodeSource);

        // generate the event of node source creation
        this.monitoring.nodeSourceEvent(new RMNodeSourceEvent(RMEventType.NODESOURCE_CREATED,
                                                              provider.getName(),
                                                              nodeSource.getName(),
                                                              nodeSource.getDescription(),
                                                              nodeSource.getAdministrator().getName()));

        logger.info("Node source " + nodeSourceName + " has been successfully created by " + provider);

        return new BooleanWrapper(true);
    }

    private boolean existNodesToRecover(boolean isRecovery, String nodeSourceName) {
        boolean recoverNodes = false;
        if (isRecovery) {
            // check what is in database for this node source
            Collection<RMNodeData> nodesData = dbManager.getNodesByNodeSource(nodeSourceName);
            // if there is no node in database for this node source, then
            // abort nodes recovery for this node source false
            if (nodesData.isEmpty()) {
                logger.info("There is no node in database for node source: " + nodeSourceName +
                            ". The node source is going to be redeployed from scratch");
            } else {
                recoverNodes = true;
            }
        }
        return recoverNodes;
    }

    /**
     * Shutdown the resource manager
     */
    public BooleanWrapper shutdown(boolean preempt) {
        // this method could be called twice from shutdown hook and user action
        if (toShutDown)
            return new BooleanWrapper(false);

        logger.info("RMCore shutdown request");
        this.monitoring.rmEvent(new RMEvent(RMEventType.SHUTTING_DOWN));
        this.toShutDown = true;

        if (PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean() || nodeSources.size() == 0) {
            finalizeShutdown();
        } else {
            for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
                removeAllNodes(entry.getKey(), preempt, true);
                entry.getValue().shutdown(caller);
            }
        }
        return new BooleanWrapper(true);
    }

    // ----------------------------------------------------------------------
    // Methods called by RMUser, override RMCoreInterface
    // ----------------------------------------------------------------------

    private static Set<String> nodesListToUrlsSet(Collection<RMNode> nodeList) {
        HashSet<String> nodesUrlsSet = new HashSet<>(nodeList.size());
        for (RMNode node : nodeList) {
            nodesUrlsSet.add(node.getNodeURL());
        }
        return nodesUrlsSet;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper releaseNode(Node node) {
        NodeSet nodes = new NodeSet();
        nodes.add(node);
        return releaseNodes(nodes);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper releaseNodes(NodeSet nodes) {

        if (nodes.getExtraNodes() != null) {
            // do not forget to release extra nodes
            nodes.addAll(nodes.getExtraNodes());
        }

        // exception to throw in case of problems
        RuntimeException exception = null;

        NodeSet nodesReleased = new NodeSet();
        NodeSet nodesFailedToRelease = new NodeSet();

        for (Node node : nodes) {
            String nodeURL = null;
            try {
                nodeURL = node.getNodeInformation().getURL();
                logger.debug("Releasing node " + nodeURL);
            } catch (RuntimeException e) {
                logger.debug("A Runtime exception occurred while obtaining information on the node," +
                             "the node must be down (it will be detected later)", e);
                // node is down, will be detected by pinger
                exception = new IllegalStateException(e.getMessage(), e);
                nodesFailedToRelease.add(node);
            }

            // verify whether the node has not been removed from the RM
            if (this.allNodes.containsKey(nodeURL)) {
                RMNode rmnode = this.getNodebyUrl(nodeURL);

                // prevent Scheduler Error : Scheduler try to render anode already
                // free
                if (rmnode.isFree()) {
                    logger.warn("Client " + caller + " tries to release the already free node " + nodeURL);
                    nodesFailedToRelease.add(node);
                } else if (rmnode.isDown()) {
                    logger.warn("Node was down, it cannot be released");
                    nodesFailedToRelease.add(node);
                } else {
                    Set<? extends IdentityPrincipal> userPrincipal = rmnode.getOwner()
                                                                           .getSubject()
                                                                           .getPrincipals(UserNamePrincipal.class);
                    Permission ownerPermission = new PrincipalPermission(rmnode.getOwner().getName(), userPrincipal);
                    try {
                        caller.checkPermission(ownerPermission,
                                               caller + " is not authorized to free node " +
                                                                node.getNodeInformation().getURL());

                        if (rmnode.isToRemove()) {
                            removeNodeFromCoreAndSource(rmnode, caller);
                            nodesReleased.add(node);
                        } else {
                            internalSetFree(rmnode);
                            nodesReleased.add(node);
                        }
                    } catch (SecurityException ex) {
                        logger.error(ex.getMessage(), ex);
                        nodesFailedToRelease.add(node);
                        exception = ex;
                    }
                }
            } else {
                logger.warn("Cannot release unknown node " + nodeURL);
                nodesFailedToRelease.add(node);
                exception = new IllegalArgumentException("Cannot release unknown node " + nodeURL);
            }
        }

        logger.info("Nodes released : " + nodesReleased);
        if (!nodesFailedToRelease.isEmpty()) {
            logger.warn("Nodes failed to release : " + nodesFailedToRelease);
        }

        if (exception != null) {
            // throwing the latest exception we had
            throw exception;
        }

        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(int nbNodes, SelectionScript selectionScript) {
        List<SelectionScript> selectionScriptList = selectionScript == null ? null
                                                                            : Collections.singletonList(selectionScript);
        return getAtMostNodes(nbNodes, TopologyDescriptor.ARBITRARY, selectionScriptList, null);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript, NodeSet exclusion) {
        List<SelectionScript> selectionScriptList = selectionScript == null ? null
                                                                            : Collections.singletonList(selectionScript);
        return getAtMostNodes(number, TopologyDescriptor.ARBITRARY, selectionScriptList, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(int number, List<SelectionScript> scripts, NodeSet exclusion) {
        return getAtMostNodes(number, TopologyDescriptor.ARBITRARY, scripts, exclusion);
    }

    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScrips,
            NodeSet exclusion) {
        return getNodes(number, descriptor, selectionScrips, exclusion, true);
    }

    public RMDBManager getDbManager() {
        return dbManager;
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getNodes(int number, TopologyDescriptor topology, List<SelectionScript> selectionScrips,
            NodeSet exclusion, boolean bestEffort) {

        Criteria criteria = new Criteria(number);

        criteria.setTopology(topology);
        criteria.setScripts(selectionScrips);
        criteria.setBlackList(exclusion);
        criteria.setBestEffort(bestEffort);

        return getNodes(criteria);
    }

    @Override
    public NodeSet getNodes(Criteria criteria) {
        if (criteria.getSize() <= 0) {
            throw new IllegalArgumentException("Illegal node number " + criteria.getSize());
        } else if (this.toShutDown) {
            // if the resource manager is about to shutdown, do not provide any node
            return new NodeSet();
        } else {
            if (criteria.getTopology() == null) {
                criteria.setTopology(TopologyDescriptor.ARBITRARY);
            }
            return selectionManager.selectNodes(criteria, caller);
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getExactlyNodes(int nb, SelectionScript selectionScript) {
        throw new RuntimeException("Not supported");
    }

    /**
     * Builds and returns a snapshot of RMCore's current state. Initial state
     * must be understood as a new Monitor point of view. A new monitor start to
     * receive RMCore events, so must be informed of the current state of the
     * Core at the beginning of monitoring.
     *
     * @return RMInitialState containing nodes and nodeSources of the RMCore.
     */
    public RMInitialState getRMInitialState() {
        Collection<RMNode> nodes = this.allNodes.values();
        ArrayList<RMNodeEvent> nodesList = new ArrayList<>(nodes.size());

        for (RMNode rmnode : nodes) {
            nodesList.add(rmnode.createNodeEvent());
        }

        Collection<NodeSource> nodeSources = this.nodeSources.values();
        ArrayList<RMNodeSourceEvent> nodeSourcesList = new ArrayList<>(nodeSources.size());
        for (NodeSource s : nodeSources) {
            nodeSourcesList.add(new RMNodeSourceEvent(s.getName(), s.getDescription(), s.getAdministrator().getName()));
            for (RMDeployingNode pn : s.getDeployingNodes()) {
                nodesList.add(pn.createNodeEvent());
            }
        }

        return new RMInitialState(nodesList, nodeSourcesList);
    }

    /**
     * Gets RM monitoring stub
     */
    public RMMonitoring getMonitoring() {
        try {
            // return the stub on RMMonitoring interface to keep avoid using server class on client side
            return PAActiveObject.lookupActive(RMMonitoring.class, PAActiveObject.getUrl(monitoring));
        } catch (Exception e) {
            logger.error("Could not lookup stub for RMMonitoring interface", e);
            return null;
        }
    }

    @Override
    public Set<String> listAliveNodeUrls() {
        HashSet<String> aliveNodes = new HashSet<>();
        for (String nodeurl : allNodes.keySet()) {
            RMNode node = allNodes.get(nodeurl);
            if (!node.isDown()) {
                aliveNodes.add(nodeurl);
            }
        }
        return aliveNodes;
    }

    @Override
    public Set<String> listAliveNodeUrls(Set<String> nodeSourceNames) {
        HashSet<String> aliveNodes = new HashSet<>();
        for (String nodeSource : nodeSourceNames) {
            for (Node node : nodeSources.get(nodeSource).getAliveNodes()) {
                aliveNodes.add(node.getNodeInformation().getURL());
            }
        }
        return aliveNodes;
    }

    /**
     * Unregisters node source from the resource manager core.
     */
    public BooleanWrapper nodeSourceUnregister(String sourceName, RMNodeSourceEvent evt) {
        NodeSource nodeSource = this.nodeSources.remove(sourceName);

        if (nodeSource == null) {
            logger.warn("Attempt to remove non-existing node source " + sourceName);
            new BooleanWrapper(false);
        }

        // remove node source from clients list
        // policy has been already already removed
        UniqueID id = Client.getId(nodeSource);
        if (id != null) {
            disconnect(id);
        } else {
            logger.error("Cannot extract the body id of the node source " + sourceName);
        }
        logger.info("Node Source removed : " + sourceName);
        // create the event
        this.monitoring.nodeSourceEvent(evt);

        if ((this.nodeSources.size() == 0) && this.toShutDown) {
            finalizeShutdown();
        }

        return new BooleanWrapper(true);
    }

    private void finalizeShutdown() {
        // all nodes sources has been removed and RMCore in shutdown state,
        // finish the shutdown
        this.selectionManager.shutdown();
        this.clientPinger.shutdown();
        // waiting while all events will be dispatched to listeners
        PAFuture.waitFor(this.monitoring.shutdown());

        PAActiveObject.terminateActiveObject(false);
        try {
            Thread.sleep(2000);
            synchronized (nodeRM) {
                nodeRM.notifyAll();
                shutedDown = true;
            }

            if (PAResourceManagerProperties.RM_SHUTDOWN_KILL_RUNTIME.getValueAsBoolean())
                this.nodeRM.getProActiveRuntime().killRT(true);

        } catch (Exception e) {
            logger.debug("", e);
        }
    }

    /**
     * Set a node state to busy. Set the node to busy, and move the node to the
     * internal busy nodes list. An event informing the node state's change is
     * thrown to RMMonitoring.
     *
     * @param owner
     * @param nodeUrl node to set
     */
    public void setBusyNode(final String nodeUrl, Client owner) throws NotConnectedException {
        final RMNode rmNode = this.allNodes.get(nodeUrl);
        if (rmNode == null) {
            logger.error("Unknown node " + nodeUrl);
            return;
        }

        if (!clients.containsKey(owner.getId())) {
            logger.warn(nodeUrl + " cannot set busy as the client disconnected " + owner);
            throw new NotConnectedException("Client " + owner + " is not connected to the resource manager");
        }

        // If the node is already busy no need to go further
        if (rmNode.isBusy()) {
            return;
        }
        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();
        rmNode.setBusy(owner);
        this.eligibleNodes.remove(rmNode);

        // persist the state change to the database
        persistUpdatedRMNode(rmNode);

        // create the event
        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(NODE_STATE_CHANGED, previousNodeState, owner.getName()));

    }

    /**
     * Sets a node state to down and updates all internal structures of rm core
     * accordingly. Sends an event indicating that the node is down.
     */
    public void setDownNode(String nodeUrl) {
        RMNode rmNode = getNodebyUrl(nodeUrl);
        if (rmNode != null) {
            // If the node is already down no need to go further
            if (rmNode.isDown()) {
                return;
            }
            logger.info("The node " + rmNode.getNodeURL() + " provided by " + rmNode.getProvider() + " is down");
            // Get the previous state of the node needed for the event
            final NodeState previousNodeState = rmNode.getState();
            if (rmNode.isFree()) {
                eligibleNodes.remove(rmNode);
            }

            rmNode.setDown();

            // persist the state change to the database
            persistUpdatedRMNode(rmNode);

            // create the event
            this.registerAndEmitNodeEvent(rmNode.createNodeEvent(NODE_STATE_CHANGED,
                                                                 previousNodeState,
                                                                 rmNode.getProvider().getName()));
        } else {
            // the nodes has been removed from core asynchronously
            // when pinger of selection manager tried to access it
            // do nothing in this case
            logger.debug("setDownNode returned immediately because the node " + nodeUrl + " was not known");
        }
    }

    private void registerAndEmitNodeEvent(final RMNodeEvent event) {
        this.monitoring.nodeEvent(event);
    }

    /**
     * Removed a node with given url from the internal structures of the core.
     *
     * @param nodeUrl down node to be removed
     * @return true if the nodes was successfully removed, false otherwise
     */
    public BooleanWrapper removeNodeFromCore(String nodeUrl) {
        RMNode rmnode = getNodebyUrl(nodeUrl);
        if (rmnode != null) {
            removeNodeFromCore(rmnode, caller);
            return new BooleanWrapper(true);
        } else {
            return new BooleanWrapper(false);
        }
    }

    public List<RMNode> getFreeNodes() {
        return eligibleNodes;
    }

    /**
     * {@inheritDoc}
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) {
        if (this.nodeSources.containsKey(sourceName)) {
            return this.nodeSources.get(sourceName).getPingFrequency();
        } else {
            throw new IllegalArgumentException("Unknown node source " + sourceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper setNodeSourcePingFrequency(int frequency, String sourceName) {
        if (this.nodeSources.containsKey(sourceName)) {
            this.nodeSources.get(sourceName).setPingFrequency(frequency);
        } else {
            throw new IllegalArgumentException("Unknown node source " + sourceName);
        }
        return new BooleanWrapper(true);
    }

    /**
     * Gives list of existing Node Sources
     *
     * @return list of existing Node Sources
     */
    public List<RMNodeSourceEvent> getExistingNodeSourcesList() {
        return getRMInitialState().getNodeSource();
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public List<RMNodeEvent> getNodesList() {
        return getRMInitialState().getNodesEvents();
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper removeNodeSource(String sourceName, boolean preempt) {
        if (nodeSources.containsKey(sourceName)) {
            // need to have an admin permission to remove the node source
            NodeSource nodeSource = nodeSources.get(sourceName);
            caller.checkPermission(nodeSource.getAdminPermission(),
                                   caller + " is not authorized to remove " + sourceName);

            logger.info(caller + " requested removal of the " + sourceName + " node source");

            //remove down nodes handled by the source
            //because node source doesn't know anymore its down nodes
            removeAllNodes(sourceName, preempt);
            // here we need the shutdown to be completed before the node source is removed from the database
            // so we must ensure that the next call is blocking
            PAFuture.waitFor(nodeSource.shutdown(caller));
            dbManager.removeNodeSource(sourceName);

            return new BooleanWrapper(true);
        } else if (brokenNodeSources.contains(sourceName)) {
            logger.info(caller + " requested removal of the " + sourceName + " node source (broken)");
            brokenNodeSources.remove(sourceName);
            dbManager.removeNodeSource(sourceName);

            return new BooleanWrapper(true);
        } else {
            throw new IllegalArgumentException("Unknown node source " + sourceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public RMState getState() {
        RMStateNodeUrls rmStateNodeUrls = new RMStateNodeUrls(nodesListToUrlsSet(eligibleNodes),
                                                              listAliveNodeUrls(),
                                                              nodesListToUrlsSet(allNodes.values()));
        RMState state = new RMState(rmStateNodeUrls, maximumNumberOfNodes);
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public BooleanWrapper isActive() {
        // return false for non connected clients
        // it should be verified by checkPermissionsMethod but it returns true for
        // local active objects
        return new BooleanWrapper(!toShutDown && clients.containsKey(caller.getId()));
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper disconnect() {
        disconnect(PAActiveObject.getContext().getCurrentRequest().getSender().getID());
        return new BooleanWrapper(true);
    }

    /**
     * Disconnects the client and releases all nodes held by him
     */
    public void disconnect(UniqueID clientId) {
        Client client = RMCore.clients.remove(clientId);
        if (client != null) {
            List<RMNode> nodesToRelease = new LinkedList<>();
            // expensive but relatively rare operation
            for (RMNode rmnode : new ArrayList<>(allNodes.values())) {
                // checking that it is not only the same client but also
                // the same connection
                if (client.equals(rmnode.getOwner()) && clientId.equals(rmnode.getOwner().getId())) {
                    if (rmnode.isToRemove()) {
                        removeNodeFromCoreAndSource(rmnode, client);
                    } else if (rmnode.isBusy()) {
                        nodesToRelease.add(rmnode);
                    }
                }
            }
            // Force the nodes cleaning here to avoid the situation
            // when the disconnected client still uses nodes.
            // In the future we may clean nodes for any release request
            nodesCleaner.cleanAndRelease(nodesToRelease);
            // update the connection info in the DB
            if (client.getHistory() != null) {
                UserHistory userHistory = client.getHistory();
                userHistory.setEndTime(System.currentTimeMillis());
                dbManager.updateUserHistory(userHistory);
            }
            logger.info(client + " disconnected from " + client.getId().shortString());
        } else {
            logger.warn("Trying to disconnect unknown client with id " + clientId.shortString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return getPluginsDescriptor(InfrastructureManagerFactory.getSupportedInfrastructures());
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return getPluginsDescriptor(NodeSourcePolicyFactory.getSupportedPolicies());
    }

    private Collection<PluginDescriptor> getPluginsDescriptor(Collection<Class<?>> plugins) {
        Collection<PluginDescriptor> descriptors = new ArrayList<>(plugins.size());
        for (Class<?> cls : plugins) {
            Map<String, String> defaultValues = new HashMap<>();
            descriptors.add(new PluginDescriptor(cls, defaultValues));
        }
        return descriptors;
    }

    /**
     * Checks if the caller thread has permissions to call particular method name
     *
     * @return client object corresponding to the caller thread
     */
    private Client checkMethodCallPermission(final String methodName, UniqueID clientId) {
        Client client = RMCore.clients.get(clientId);

        if (client == null) {
            // Check if the client id is a local body or half body
            LocalBodyStore lbs = LocalBodyStore.getInstance();
            if (lbs.getLocalBody(clientId) != null || lbs.getLocalHalfBody(clientId) != null) {
                return RMCore.localClient;
            }

            throw new NotConnectedException("Client " + clientId.shortString() +
                                            " is not connected to the resource manager");
        }

        final String fullMethodName = RMCore.class.getName() + "." + methodName;
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);

        client.checkPermission(methodCallPermission, client + " is not authorized to call " + fullMethodName);
        return client;
    }

    public Topology getTopology() {
        if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
            throw new TopologyException("Topology is disabled");
        }
        return topologyManager.getTopology();
    }

    /**
     * Returns true if the given parameter is the representation of
     * a deploying node ( starts with deploying://nsName/nodeName )
     *
     * @param url
     * @return true if the parameter is a deploying node's url, false otherwise
     */
    private boolean isDeployingNodeURL(String url) {
        return url != null && url.startsWith(RMDeployingNode.PROTOCOL_ID + "://");
    }

    /**
     * To handle the deploying node removal
     *
     * @param url the url of the deploying node to remove
     * @return true if successful, false otherwise
     */
    private boolean removeDeployingNode(String url) {
        String nsName = "";
        try {
            URI urlObj = new URI(url);
            nsName = urlObj.getHost();
        } catch (URISyntaxException e) {
            logger.warn("No such deploying node: " + url);
            return false;
        }
        if (nsName == null) {
            //cannot compute the nsName using URI, try using Pattern
            Matcher matcher = Pattern.compile(RMDeployingNode.PROTOCOL_ID + "://([-\\w]+)/.+").matcher(url);
            if (matcher.find()) {
                try {
                    nsName = matcher.group(1);
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("Was not able to determine nodesource's name for url " + url);
                }
            }
        }
        NodeSource ns = this.nodeSources.get(nsName);
        if (ns == null) {
            logger.warn("No such nodesource: " + nsName + ", cannot remove the deploying node with url: " + url);
            return false;
        }
        return ns.removeDeployingNode(url);
    }

    /**
     * Checks if the string parameter is a valid nodesource name.
     * Throws an IllegalArgumentException if it doesn't
     *
     * @param nodeSourceName the name to test
     */
    private void checkNodeSourceName(String nodeSourceName) {
        //we are sure that the parameter isn't null
        if (nodeSourceName.length() == 0) {
            throw new IllegalArgumentException("Node Source Name cannot be empty");
        }
        if (this.nodeSources.containsKey(nodeSourceName)) {
            throw new IllegalArgumentException("Node Source name " + nodeSourceName + " already exist");
        }
        Pattern pattern = Pattern.compile("[^-\\w]");//letters,digits,_and-
        Matcher matcher = pattern.matcher(nodeSourceName);
        if (matcher.find()) {
            throw new IllegalArgumentException("Node Source name \"" + nodeSourceName +
                                               "\" is invalid because it contains invalid characters. Only [-a-zA-Z_0-9] are valid.");
        }
    }

    /**
     * Checks if the client is the node admin.
     *
     * @param rmnode is a node to be checked
     * @param client is a client to be checked
     * @return true if the client is an admin, SecurityException otherwise
     */
    private boolean checkNodeAdminPermission(RMNode rmnode, Client client) {
        NodeSource nodeSource = rmnode.getNodeSource();

        String errorMessage = client.getName() + " is not authorized to manage node " + rmnode.getNodeURL() + " from " +
                              rmnode.getNodeSourceName();

        // in order to be the node administrator a client has to be either
        // an administrator of the RM (with AllPermissions) or
        // an administrator of the node source (creator) or
        // a node provider
        try {
            // checking if the caller is an administrator
            client.checkPermission(nodeSource.getAdminPermission(), errorMessage);
        } catch (SecurityException ex) {
            // the caller is not an administrator, so checking if it is a node provider
            client.checkPermission(rmnode.getAdminPermission(), errorMessage);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper lockNodes(Set<String> urls) {
        return lockNodes(urls, caller);
    }

    protected BooleanWrapper lockNodes(Set<String> urls, final Client caller) {
        return mapOnNodeUrlSet(urls, new Predicate<RMNode>() {
            @Override
            public boolean apply(RMNode node) {
                return internalLockNode(node, caller);
            }
        }, "lock");
    }

    boolean internalLockNode(RMNode rmNode, Client lockInitiator) {
        if (rmNode.isLocked()) {
            logger.warn("Cannot lock a node that is already locked: " + rmNode.getNodeURL());
            // locking a node that is already locked must not update
            // the lock time neither change who has locked the node
            return false;
        }

        try {
            // can throw a security exception if the lockInitiator is not an admin
            this.checkNodeAdminPermission(rmNode, lockInitiator);
            rmNode.lock(lockInitiator);
            this.eligibleNodes.remove(rmNode);
        } catch (SecurityException e) {
            logger.warn("Lock node lockInitiator is not admin", e);
            return false;
        }

        updateNode(rmNode);

        dbManager.createLockEntryOrUpdate(rmNode.getNodeSourceName(), RMDBManager.NodeLockUpdateAction.INCREMENT);

        // sending the following event is required in order to have monitoring information
        // updated in the intermediate RM cache (see RMListenerProxy#nodeEvent)
        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(NODE_STATE_CHANGED,
                                                             rmNode.getState(),
                                                             lockInitiator.getName()));

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper unlockNodes(Set<String> urls) {
        return mapOnNodeUrlSet(urls, new Predicate<RMNode>() {
            @Override
            public boolean apply(RMNode node) {
                return internalUnlockNode(node);
            }
        }, "unlock");
    }

    public BooleanWrapper mapOnNodeUrlSet(Set<String> nodeUrls, Predicate<RMNode> operation, String operationName) {
        boolean result = true;

        for (String url : nodeUrls) {
            RMNode rmnode = getNodeByUrlIncludingDeployingNodes(url);

            if (rmnode == null) {
                logger.warn("Cannot " + operationName + ", unknown node: " + url);
                result &= false;
                continue;
            }

            result &= operation.apply(rmnode);
        }

        return new BooleanWrapper(result);
    }

    boolean internalUnlockNode(RMNode rmNode) {
        if (!rmNode.isLocked()) {
            logger.warn("Cannot unlock a node that is not locked: " + rmNode.getNodeURL());
            return false;
        }

        try {
            // can throw a security exception if the caller is not an admin
            this.checkNodeAdminPermission(rmNode, this.caller);
            rmNode.unlock(this.caller);

            // an eligible node is a node that is free AND not locked
            if (rmNode.isFree()) {
                eligibleNodes.add(rmNode);
            }

            updateNode(rmNode);
        } catch (Exception ex) {
            logger.warn("", ex);
            return false;
        }

        dbManager.createLockEntryOrUpdate(rmNode.getNodeSourceName(), RMDBManager.NodeLockUpdateAction.DECREMENT);

        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(NODE_STATE_CHANGED, rmNode.getState(), caller.getName()));

        return true;
    }

    private void updateNode(RMNode rmNode) {
        if (rmNode.isDeploying()) {
            // A deploying node instance is retrieved from a NodeSource
            // This last is an Active Object which returns a deep copy
            // of the original object.
            // As a consequence, any updates on the rmNode instance are not reflected
            // to the instance stored in the NodeSource. The purpose of the following
            // call is to update the information stored in the NodeSource.
            PAFuture.waitFor(((RMDeployingNode) rmNode).updateOnNodeSource());
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper isNodeAdmin(String nodeUrl) {
        RMNode rmnode = getNodebyUrl(nodeUrl);

        if (rmnode == null) {
            throw new IllegalArgumentException("Unknown node " + nodeUrl);
        }

        try {
            caller.checkPermission(rmnode.getAdminPermission(),
                                   caller + " is not authorized to administrate the node " + rmnode.getNodeURL() +
                                                                " from " + rmnode.getNodeSource().getName());
        } catch (SecurityException e) {
            // client does not have an access to this node
            logger.debug(e.getMessage());
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper isNodeUser(String nodeUrl) {
        RMNode rmnode = getNodebyUrl(nodeUrl);

        if (rmnode == null) {
            throw new IllegalArgumentException("Unknown node " + nodeUrl);
        }

        try {
            caller.checkPermission(rmnode.getUserPermission(),
                                   caller + " is not authorized to run computations on the node " +
                                                               rmnode.getNodeURL() + " from " +
                                                               rmnode.getNodeSource().getName());
        } catch (SecurityException e) {
            // client does not have an access to this node
            logger.debug(e.getMessage());
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    @Override
    public List<ScriptResult<Object>> executeScript(String script, String scriptEngine, String targetType,
            Set<String> targets) {
        try {
            return this.executeScript(new SimpleScript(script, scriptEngine), targetType, targets);
        } catch (InvalidScriptException e) {
            logger.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> List<ScriptResult<T>> executeScript(Script<T> script, String targetType, Set<String> targets) {
        // Depending on the target type, select nodes for script execution
        final TargetType tType = TargetType.valueOf(targetType);
        final HashSet<RMNode> selectedRMNodes = new HashSet<>();
        switch (tType) {
            case NODESOURCE_NAME:
                // If target is a nodesource name select all its nodes
                for (String target : targets) {
                    NodeSource nodeSource = this.nodeSources.get(target);
                    if (nodeSource != null) {
                        for (RMNode candidateNode : this.allNodes.values()) {
                            if (candidateNode.getNodeSource().equals(nodeSource)) {
                                this.selectCandidateNode(selectedRMNodes, candidateNode);
                            }
                        }
                    }
                }
                break;
            case NODE_URL:
                // If target is node url select the node
                for (String target : targets) {
                    RMNode candidateNode = this.allNodes.get(target);
                    if (candidateNode != null) {
                        this.selectCandidateNode(selectedRMNodes, candidateNode);
                    }
                }
                break;
            case HOSTNAME:
                // If target is hostname select first node from that host
                for (String target : targets) {
                    for (RMNode node : this.allNodes.values()) {
                        if (node.getHostName().equals(target)) {
                            this.selectCandidateNode(selectedRMNodes, node);
                            break;
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unable to execute script, unknown target type: " + targetType);
        }

        // Return a ProActive future on the list of results
        return this.selectionManager.executeScript(script, selectedRMNodes, null);

        // To avoid blocking rmcore ao the call is delegated to the selection
        // manager ao and each node is unlocked as soon as the script has
        // finished it's execution.
    }

    private void selectCandidateNode(HashSet<RMNode> selectedRMNodes, RMNode candidateNode) {
        if (this.internalLockNode(candidateNode, caller)) {
            selectedRMNodes.add(candidateNode);
        } else {
            // Unlock all previously locked nodes
            this.unselectNodes(selectedRMNodes);

            throw new IllegalStateException("Script cannot be executed atomically since the node is already locked: " +
                                            candidateNode.getNodeURL());
        }
    }

    private void unselectNodes(final HashSet<RMNode> selectedRMNodes) {
        // Unlock all previously locked nodes
        for (RMNode rmnode : selectedRMNodes) {
            this.internalUnlockNode(rmnode);
        }
    }

    public boolean setDeploying(RMNode rmNode) {
        nodesRecoveryManager.restoreLocks(rmNode, caller);
        return true;
    }

    @Override
    public StringWrapper getCurrentUser() {
        return new StringWrapper(caller.getName());
    }

    @Override
    public UserData getCurrentUserData() {
        UserData userData = new UserData();
        userData.setUserName(caller.getName());
        userData.setGroups(caller.getGroups());
        return userData;
    }

    /**
     * Add the information of the given node to the database.
     *
     * @param rmNode the node to add to the database
     */
    private void persistNewRMNode(RMNode rmNode) {
        RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
        NodeSourceData nodeSourceData = dbManager.getNodeSource(rmNode.getNodeSourceName());
        rmNodeData.setNodeSource(nodeSourceData);
        if (!NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(nodeSourceData.getName())) {
            dbManager.addNode(rmNodeData);
        }
    }

    /**
     * Update the information of the given node in database.
     *
     * @param rmNode the node to update in database
     */
    private void persistUpdatedRMNode(RMNode rmNode) {
        RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
        if (!NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(rmNode.getNodeSourceName())) {
            dbManager.updateNode(rmNodeData);
        }
    }

    private boolean isEligible(RMNode node) {
        if (node != null && node.isFree() && !node.isLocked()) {
            return true;
        }
        return false;
    }

}
