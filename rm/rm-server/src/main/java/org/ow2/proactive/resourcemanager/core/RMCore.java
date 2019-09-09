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

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.ow2.proactive.permissions.NodeUserAllPermission;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.permissions.RMCoreAllPermission;
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
import org.ow2.proactive.resourcemanager.common.event.RMNodeHistory;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.core.recovery.NodesRecoveryManager;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.housekeeping.NodesHouseKeepingService;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceStatus;
import org.ow2.proactive.resourcemanager.nodesource.PluginNotFoundException;
import org.ow2.proactive.resourcemanager.nodesource.RMNodeConfigurator;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.NodeSourceConfiguration;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManagerFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.ThreadDumpNotAccessibleException;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.selection.statistics.ProbablisticSelectionManager;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;
import org.ow2.proactive.resourcemanager.utils.ClientPinger;
import org.ow2.proactive.resourcemanager.utils.RMNodeHelper;
import org.ow2.proactive.resourcemanager.utils.TargetType;
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

    public static final String NODE_SOURCE_STRING = "Node source ";

    public static final String REQUESTED_BY_STRING = " requested by ";

    public static final String HAS_BEEN_SUCCESSFULLY = " has been successfully ";

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
    private static final Logger logger = Logger.getLogger(RMCore.class);

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
     * stores delayed NodeSource removal events and NodeSource itself. Used when NodeSource has some toBeRemoved nodes.
     * In this case, we postpone NodeSource removal event and NodeSource shutdown until last node was removed,
     */
    private Map<String, Map.Entry<RMNodeSourceEvent, NodeSource>> delayedNodeSourceRemovalEvents = new ConcurrentHashMap<>();

    /**
     * When we undeploy NodeSource which has some busy nodes, in this case, we put NodeSource to this map.
     * Then when last node was removed, we call NodeSource shutdown method,
     */
    private Map<String, NodeSource> delayedNodeSourceUndeploying = new ConcurrentHashMap<>();

    /**
     * authentication active object
     */
    private RMAuthenticationImpl authentication;

    /**
     * HashMap of all node sources by name. It contains both deployed and
     * undeployed node sources. Contrarily to undeployed node sources,
     * deployed node sources have the node source activity and the node source
     * policy activity running.
     */
    private Map<String, NodeSource> definedNodeSources;

    /**
     * HashMap of deployed node sources by name {@link #definedNodeSources}
     */
    private Map<String, NodeSource> deployedNodeSources;

    /**
     * HashMaps of nodes known by the RMCore
     */
    private Map<String, RMNode> allNodes;

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

    private Client caller = localClient;

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

    private NodeSourceParameterHelper nodeSourceParameterHelper;

    /**
     * A barrier to prevent the {@link RMCore#setNodesAvailable} immediate
     * service to run before the initActivity of the RM is finished.
     */
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private NodesHouseKeepingService nodesHouseKeepingService;

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
        this.deployedNodeSources = new HashMap<>();
        this.definedNodeSources = new HashMap<>();
        this.allNodes = new ConcurrentHashMap<>();
        this.delayedNodeSourceRemovalEvents = new ConcurrentHashMap<>();
        this.delayedNodeSourceUndeploying = new ConcurrentHashMap<>();
        this.eligibleNodes = Collections.synchronizedList(new ArrayList<RMNode>());

        this.accountsManager = new RMAccountsManager();
        this.jmxHelper = new RMJMXHelper(this.accountsManager);
    }

    public RMCore(Map<String, NodeSource> deployedNodeSources, Map<String, RMNode> allNodes, Client caller,
            RMMonitoringImpl monitoring, SelectionManager manager, List<RMNode> freeNodesList,
            RMDBManager newDataBaseManager) {
        this.deployedNodeSources = deployedNodeSources;
        this.definedNodeSources = this.deployedNodeSources;
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
                        PAFuture.waitFor(rmcoreStub.shutdown(true),
                                         PAResourceManagerProperties.RM_SHUTDOWN_TIMEOUT.getValueAsInt() * 1000);
                    }
                }
            });

            // Creating RM started event
            this.monitoring.rmEvent(new RMEvent(RMEventType.STARTED));

            authentication.setActivated(true);

            clientPinger.ping();

            nodeSourceParameterHelper = new NodeSourceParameterHelper();

            nodesHouseKeepingService = new NodesHouseKeepingService(rmcoreStub);
            nodesHouseKeepingService.start();

            initiateRecoveryIfRequired();

        } catch (ActiveObjectCreationException e) {
            logger.error("", e);
        } catch (NodeException e) {
            logger.error("", e);
        } catch (ProActiveException e) {
            logger.error("", e);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        } finally {
            signalRMCoreIsInitialized();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("RMCore end: initActivity");
        }
    }

    protected void signalRMCoreIsInitialized() {
        logger.info("Resource Manager is initialized");
        countDownLatch.countDown();
    }

    protected void initiateRecoveryIfRequired() {
        nodesRecoveryManager = getNodesRecoveryManagerBuilder().apply(this);
        nodesRecoveryManager.initialize();
        if (isNodesRecoveryEnabled()) {
            logger.info("Starting Nodes Recovery");
        } else {
            logger.info("Nodes Recovery is disabled. Removing all nodes from database");
            dbManager.removeAllNodes();
        }
        nodesRecoveryManager.recoverNodeSourcesAndNodes();
    }

    Function<RMCore, NodesRecoveryManager> getNodesRecoveryManagerBuilder() {
        return new Function<RMCore, NodesRecoveryManager>() {
            @Override
            public NodesRecoveryManager apply(RMCore rmCore) {
                return new NodesRecoveryManager(rmCore);
            }
        };
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
                NodeSource nodeSource = this.deployedNodeSources.get(nodeSourceName);

                if (nodeSource != null) {
                    return nodeSource.getNodeInDeployingOrLostNodes(url);
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

        persistUpdatedRMNodeIfRecoveryEnabled(rmNode);

        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                             previousNodeState,
                                                             client.getName()));

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

        persistUpdatedRMNodeIfRecoveryEnabled(rmNode);

        // create the event
        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
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
        RMNode rmnode = this.allNodes.get(nodeURL);
        registerAvailableNode(configuredNode);

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
    public int getTotalAliveNodesNumber() {
        return listAliveNodeUrls().size();
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
     * Add the node in the list of all nodes to make it pingable.
     *
     * @param rmNode the node to make available
     */
    public BooleanWrapper registerAvailableNode(RMNode rmNode) {
        this.allNodes.put(rmNode.getNodeURL(), rmNode);
        return new BooleanWrapper(true);
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
        registerAvailableNode(rmnode);

        // save the information of this new node in DB, in particular its state
        persistNewRMNodeIfRecoveryEnabled(rmnode);

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

        boolean existingNodeSource = this.deployedNodeSources.containsKey(sourceName);

        if (!existingNodeSource && sourceName.equals(NodeSource.DEFAULT)) {
            // creating the default node source
            createNodeSource(NodeSource.DEFAULT,
                             DefaultInfrastructureManager.class.getName(),
                             null,
                             StaticPolicy.class.getName(),
                             null,
                             NodeSource.DEFAULT_RECOVERABLE).getBooleanValue();
        }

        if (this.deployedNodeSources.containsKey(sourceName)) {
            NodeSource nodeSource = this.deployedNodeSources.get(sourceName);

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
                for (RMDeployingNode pn : nodeSource.getDeployingAndLostNodes()) {
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

    public void setEligibleNodesToRecover(List<RMNode> eligibleNodes) {
        this.eligibleNodes = eligibleNodes;
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
        NodeSource nodeSource = this.deployedNodeSources.get(nodeSourceName);

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
     * but read by multiple Threads.
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

        waitForRMCoreToBeInitializedIfNeeded();

        if (logger.isTraceEnabled()) {
            logger.trace("Received availability for the following workers: " + nodeUrls);
        }

        ImmutableSet.Builder<String> nodeUrlsNotKnownByTheRM = new ImmutableSet.Builder<>();

        for (String nodeUrl : nodeUrls) {
            RMNode node = this.allNodes.get(nodeUrl);

            if (node == null) {
                logger.warn("Cannot set node as available, the node is unknown: " + nodeUrl);
                if (logger.isDebugEnabled()) {
                    logger.debug("Known nodes are: " + Arrays.toString(allNodes.keySet().toArray()));
                }
                nodeUrlsNotKnownByTheRM.add(nodeUrl);
            } else if (node.isDown()) {
                restoreNodeState(nodeUrl, node);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("The node identified by " + nodeUrl + " is known and not DOWN, no action performed");
                }
            }
        }
        return nodeUrlsNotKnownByTheRM.build();
    }

    private void waitForRMCoreToBeInitializedIfNeeded() {
        try {
            if (countDownLatch.getCount() != 0) {
                logger.info("Waiting for Resource Manager to be initialized");
                countDownLatch.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Resource Manager to be initialized", e);
        }
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

        if (RMCore.topologyManager != null) {
            RMCore.topologyManager.addNode(node.getNode());
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
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper defineNodeSource(String nodeSourceName, String infrastructureType, Object[] infraParams,
            String policyType, Object[] policyParams, boolean nodesRecoverable) {

        logger.info("Define node source " + nodeSourceName + REQUESTED_BY_STRING + this.caller.getName());

        this.validateNodeSourceNameOrFail(nodeSourceName);

        nodeSourceName = nodeSourceName.trim();

        NodeSourceData nodeSourceData = this.getNodeSourceToPersist(nodeSourceName,
                                                                    infrastructureType,
                                                                    infraParams,
                                                                    policyType,
                                                                    policyParams,
                                                                    nodesRecoverable);

        this.dbManager.addNodeSource(nodeSourceData);

        NodeSourceDescriptor nodeSourceDescriptor = nodeSourceData.toNodeSourceDescriptor();
        NodeSource nodeSource = this.createNodeSourceInstance(nodeSourceDescriptor);

        this.definedNodeSources.put(nodeSourceName, nodeSource);

        this.emitNodeSourceEvent(nodeSource, RMEventType.NODESOURCE_DEFINED);

        logger.info(NODE_SOURCE_STRING + nodeSourceName + HAS_BEEN_SUCCESSFULLY + "defined");

        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper editNodeSource(String nodeSourceName, String infrastructureType, Object[] infraParams,
            String policyType, Object[] policyParams, boolean nodesRecoverable) {

        logger.info("Edit node source " + nodeSourceName + REQUESTED_BY_STRING + this.caller.getName());
        NodeSource oldNodeSource = this.getEditableNodeSourceOrFail(nodeSourceName);
        NodeSourceData nodeSourceData = this.getNodeSourceToPersist(nodeSourceName,
                                                                    infrastructureType,
                                                                    infraParams,
                                                                    policyType,
                                                                    policyParams,
                                                                    nodesRecoverable);
        this.dbManager.updateNodeSource(nodeSourceData);

        try {
            NodeSource newNodeSource = this.createNodeSourceInstance(nodeSourceData.toNodeSourceDescriptor());
            this.definedNodeSources.put(nodeSourceName, newNodeSource);
            this.emitNodeSourceEvent(newNodeSource, RMEventType.NODESOURCE_DEFINED);
            logger.info(NODE_SOURCE_STRING + nodeSourceName + " has been successfully edited");
            return new BooleanWrapper(true);
        } catch (Exception e) {
            this.dbManager.updateNodeSource(NodeSourceData.fromNodeSourceDescriptor(oldNodeSource.getDescriptor()));
            this.definedNodeSources.put(nodeSourceName, oldNodeSource);
            logger.warn(NODE_SOURCE_STRING + nodeSourceName + " failed to be edited. Infrastructure parameters: " +
                        Arrays.toString(infraParams) + ". Policy parameters: " + Arrays.toString(policyParams) + ".",
                        e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper updateDynamicParameters(String nodeSourceName, String infrastructureType,
            Object[] infraParams, String policyType, Object[] policyParams) {

        logger.info("Update dynamic parameters of node source " + nodeSourceName + REQUESTED_BY_STRING +
                    this.caller.getName());
        NodeSource definedNodeSource = getDefinedNodeSourceOrFail(nodeSourceName);

        // needed to rollback in case of an issue
        List<Serializable> oldInfrastructureParameters = definedNodeSource.getDescriptor()
                                                                          .getSerializableInfrastructureParameters();
        List<Serializable> oldInfrastructureParametersCopy = new ArrayList<>(oldInfrastructureParameters.size());
        oldInfrastructureParametersCopy.addAll(oldInfrastructureParameters);
        List<Serializable> oldPolicyParameters = definedNodeSource.getDescriptor().getSerializablePolicyParameters();
        List<Serializable> oldPolicyParametersCopy = new ArrayList<>(oldPolicyParameters.size());
        oldPolicyParametersCopy.addAll(oldPolicyParameters);

        // merge old not dynamic parameters and parameters to update
        NodeSource deployedNodeSource = getDeployedNodeSourceOrFail(nodeSourceName);
        NodeSourceDescriptor oldDescriptor = deployedNodeSource.getDescriptor();
        NodeSourceDescriptor newDescriptor = getUpdatedNodeSourceDescriptor(nodeSourceName,
                                                                            infraParams,
                                                                            policyParams,
                                                                            deployedNodeSource,
                                                                            oldDescriptor);
        persistNodeSourceWithNewDescriptor(newDescriptor);

        try {
            deployedNodeSource.reconfigure(newDescriptor.getInfrastructureParameters(),
                                           newDescriptor.getPolicyParameters());
            this.emitNodeSourceEvent(deployedNodeSource, RMEventType.NODESOURCE_UPDATED);
            logger.info(NODE_SOURCE_STRING + nodeSourceName + " has been successfully updated with dynamic parameters");
            return new BooleanWrapper(true);
        } catch (Exception e) {
            updateNodeSourceDescriptor(nodeSourceName,
                                       deployedNodeSource,
                                       oldInfrastructureParametersCopy,
                                       oldPolicyParametersCopy);
            persistNodeSourceWithNewDescriptor(oldDescriptor);
            logger.warn(NODE_SOURCE_STRING + nodeSourceName +
                        " failed to be updated with dynamic parameters. Infrastructure parameters: " +
                        Arrays.toString(newDescriptor.getInfrastructureParameters()) + ". Policy parameters: " +
                        Arrays.toString(newDescriptor.getPolicyParameters()) + ".", e);
            throw new RuntimeException(e);
        }
    }

    private NodeSourceDescriptor getUpdatedNodeSourceDescriptor(String nodeSourceName,
            Object[] newInfrastructureParameters, Object[] newPolicyParameters, NodeSource deployedNodeSource,
            NodeSourceDescriptor descriptor) {

        List<Serializable> updatedInfrastructureParams = getUpdatedParameters(descriptor.getInfrastructureType(),
                                                                              descriptor.getSerializableInfrastructureParameters(),
                                                                              newInfrastructureParameters,
                                                                              nodeSourceName);
        List<Serializable> updatedPolicyParams = getUpdatedParameters(descriptor.getPolicyType(),
                                                                      descriptor.getSerializablePolicyParameters(),
                                                                      newPolicyParameters,
                                                                      nodeSourceName);

        return updateNodeSourceDescriptor(nodeSourceName,
                                          deployedNodeSource,
                                          updatedInfrastructureParams,
                                          updatedPolicyParams);
    }

    private List<Serializable> getUpdatedParameters(String pluginClassName, List<Serializable> oldParameters,
            Object[] newParameters, String nodeSourceName) {

        Collection<ConfigurableField> configurableFields;

        try {
            configurableFields = this.nodeSourceParameterHelper.getPluginConfigurableFields(pluginClassName);
        } catch (PluginNotFoundException e) {
            throw new IllegalArgumentException(e.getMessageWithContext(nodeSourceName), e);
        }

        return this.nodeSourceParameterHelper.getParametersWithDynamicParametersUpdatedOnly(configurableFields,
                                                                                            newParameters,
                                                                                            oldParameters);
    }

    private void persistNodeSourceWithNewDescriptor(NodeSourceDescriptor updatedDescriptor) {

        NodeSourceData nodeSourceData = NodeSourceData.fromNodeSourceDescriptor(updatedDescriptor);

        this.dbManager.updateNodeSource(nodeSourceData);
    }

    private NodeSourceDescriptor updateNodeSourceDescriptor(String nodeSourceName, NodeSource deployedNodeSource,
            List<Serializable> updatedInfrastructureParams, List<Serializable> updatedPolicyParams) {

        deployedNodeSource.updateDynamicParameters(updatedInfrastructureParams, updatedPolicyParams);
        NodeSource definedNodeSource = getDefinedNodeSourceOrFail(nodeSourceName);

        return definedNodeSource.updateDynamicParameters(updatedInfrastructureParams, updatedPolicyParams);
    }

    private NodeSourceData getNodeSourceToPersist(String nodeSourceName, String infrastructureType,
            Object[] infraParams, String policyType, Object[] policyParams, boolean nodesRecoverable) {

        List<Serializable> serializableInfraParams = this.getSerializableParamsOrFail(infraParams);
        List<Serializable> serializablePolicyParams = this.getSerializableParamsOrFail(policyParams);

        return new NodeSourceData(nodeSourceName,
                                  infrastructureType,
                                  serializableInfraParams,
                                  policyType,
                                  serializablePolicyParams,
                                  this.caller,
                                  nodesRecoverable,
                                  NodeSourceStatus.NODES_UNDEPLOYED);
    }

    private List<Serializable> getSerializableParamsOrFail(Object[] parameters) {

        List<Serializable> serializableParameters = null;

        if (parameters != null) {

            serializableParameters = new ArrayList<>(parameters.length);

            for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
                this.castParameterIntoSerializableOrFail(parameterIndex,
                                                         parameters[parameterIndex],
                                                         serializableParameters);
            }
        }

        return serializableParameters;
    }

    private void castParameterIntoSerializableOrFail(int parameterIndex, Object parameter,
            List<Serializable> serializableParameters) {

        try {
            serializableParameters.add(parameterIndex, (Serializable) parameter);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Parameter " + parameter + " is not Serializable", e);
        }
    }

    private NodeSource createNodeSourceInstance(NodeSourceDescriptor nodeSourceDescriptor) {

        InfrastructureManager infrastructureManager = InfrastructureManagerFactory.create(nodeSourceDescriptor);

        NodeSourcePolicy notActivePolicy = NodeSourcePolicyFactory.create(nodeSourceDescriptor.getPolicyType());

        return new NodeSource(this.getUrl(),
                              nodeSourceDescriptor.getName(),
                              infrastructureManager,
                              notActivePolicy,
                              (RMCore) PAActiveObject.getStubOnThis(),
                              this.monitoring,
                              nodeSourceDescriptor);
    }

    /**
     * @deprecated  As of version 8.1, replaced by {@link #defineNodeSource(String, String, Object[], String, Object[],
     * boolean)} and {@link #deployNodeSource(String)}
     */
    @Deprecated
    @Override
    public BooleanWrapper createNodeSource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters, boolean nodesRecoverable) {

        this.defineNodeSource(nodeSourceName,
                              infrastructureType,
                              infrastructureParameters,
                              policyType,
                              policyParameters,
                              nodesRecoverable);

        this.deployNodeSource(nodeSourceName);

        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper deployNodeSource(String nodeSourceName) {
        logger.info("Deploy node source " + nodeSourceName + REQUESTED_BY_STRING + this.caller.getName());
        if (!this.deployedNodeSources.containsKey(nodeSourceName)) {
            NodeSourceDescriptor nodeSourceDescriptor = this.getDefinedNodeSourceDescriptorOrFail(nodeSourceName);
            this.updateNodeSourceDescriptorWithStatusAndPersist(nodeSourceDescriptor, NodeSourceStatus.NODES_DEPLOYED);
            deployNodeSourceOrFail(nodeSourceName, nodeSourceDescriptor);
        } else {
            logger.debug(NODE_SOURCE_STRING + nodeSourceName + " is already deployed");
        }
        return new BooleanWrapper(true);
    }

    private void deployNodeSourceOrFail(String nodeSourceName, NodeSourceDescriptor nodeSourceDescriptor) {
        try {
            NodeSource nodeSourceToDeploy = this.createNodeSourceInstance(nodeSourceDescriptor);
            NodeSourcePolicy nodeSourcePolicyStub = this.createNodeSourcePolicyActivity(nodeSourceDescriptor,
                                                                                        nodeSourceToDeploy);
            NodeSource nodeSourceStub = this.createNodeSourceActivity(nodeSourceName, nodeSourceToDeploy);
            this.configureDeployedNodeSource(nodeSourceName,
                                             nodeSourceDescriptor,
                                             nodeSourceStub,
                                             nodeSourcePolicyStub);
            this.deployedNodeSources.put(nodeSourceName, nodeSourceStub);
            this.emitNodeSourceEvent(nodeSourceStub, RMEventType.NODESOURCE_CREATED);
            logger.info(NODE_SOURCE_STRING + nodeSourceName + " has been successfully deployed");
        } catch (Exception e) {
            this.updateNodeSourceDescriptorWithStatusAndPersist(nodeSourceDescriptor,
                                                                NodeSourceStatus.NODES_UNDEPLOYED);
            logger.error(NODE_SOURCE_STRING + nodeSourceName + " failed to be deployed", e);
            throw e;
        }
    }

    private void configureDeployedNodeSource(String nodeSourceName, NodeSourceDescriptor nodeSourceDescriptor,
            NodeSource nodeSourceStub, NodeSourcePolicy nodeSourcePolicyStub) {

        // Adding access to the core for node source and policy.
        // In order to do it node source and policy active objects are added to the clients list.
        // They will be removed from this list when node source is unregistered.
        UniqueID nsId = Client.getId(nodeSourceStub);
        UniqueID policyId = Client.getId(nodeSourcePolicyStub);

        if (nsId == null || policyId == null) {
            throw new IllegalStateException("Cannot register the node source");
        }

        BooleanWrapper result = nodeSourceStub.activate();

        if (!result.getBooleanValue()) {
            logger.error(NODE_SOURCE_STRING + nodeSourceName + " cannot be activated");
        }

        Client provider = nodeSourceDescriptor.getProvider();
        Client nsService = new Client(provider.getSubject(), false);
        Client policyService = new Client(provider.getSubject(), false);

        nsService.setId(nsId);
        policyService.setId(policyId);

        RMCore.clients.put(nsId, nsService);
        RMCore.clients.put(policyId, policyService);
    }

    private NodeSourcePolicy createNodeSourcePolicyActivity(NodeSourceDescriptor nodeSourceDescriptor,
            NodeSource nodeSourceToDeploy) {

        NodeSourcePolicy nodeSourcePolicyStub = NodeSourcePolicyFactory.activate(nodeSourceToDeploy.getPolicy(),
                                                                                 nodeSourceDescriptor.getPolicyParameters());

        nodeSourceToDeploy.setActivePolicy(nodeSourcePolicyStub);

        return nodeSourcePolicyStub;
    }

    private NodeSource createNodeSourceActivity(String nodeSourceName, NodeSource nodeSourceToDeploy) {
        try {
            nodeSourceToDeploy = PAActiveObject.turnActive(nodeSourceToDeploy, this.nodeRM);
        } catch (Exception e) {
            String errorMessage = "Failed to create node source activity " + nodeSourceName;
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
        return nodeSourceToDeploy;
    }

    private void updateNodeSourceDescriptorWithStatusAndPersist(NodeSourceDescriptor descriptor,
            NodeSourceStatus status) {
        descriptor.setStatus(status);
        persistNodeSourceWithNewDescriptor(descriptor);
    }

    private NodeSource getEditableNodeSourceOrFail(String nodeSourceName) {

        NodeSource nodeSource = getDefinedNodeSourceOrFail(nodeSourceName);

        if (!nodeSource.getStatus().equals(NodeSourceStatus.NODES_UNDEPLOYED)) {
            throw new IllegalArgumentException("A node source must be undeployed to be edited");
        }

        return nodeSource;
    }

    private NodeSourceDescriptor getDefinedNodeSourceDescriptorOrFail(String nodeSourceName) {

        NodeSource nodeSource = this.getDefinedNodeSourceOrFail(nodeSourceName);

        return nodeSource.getDescriptor();
    }

    // toto
    private NodeSource getDefinedNodeSourceOrFail(String nodeSourceName) {

        NodeSource nodeSource = this.definedNodeSources.get(nodeSourceName);

        if (nodeSource == null) {
            throw new IllegalStateException(NODE_SOURCE_STRING + nodeSourceName + " is not defined");
        }

        return nodeSource;
    }

    private NodeSource getDeployedNodeSourceOrFail(String nodeSourceName) {

        NodeSource deployedNodeSource = this.deployedNodeSources.get(nodeSourceName);

        if (deployedNodeSource == null) {
            throw new IllegalArgumentException(NODE_SOURCE_STRING + nodeSourceName + " is not deployed");
        }

        return deployedNodeSource;
    }

    /**
     * Recreate a node source from a node source descriptor and deploy it if
     * the node source descriptor specifies a deployed status. The flow is
     * inspired from {@link #defineNodeSource} and {@link #deployNodeSource}
     * but this version does not emit events as the recovered node sources are
     * supposed to have been created and deployed already in the past.
     *
     * @param nodeSourceDescriptor the descriptor of the node source to recover
     */
    public void recoverNodeSource(NodeSourceDescriptor nodeSourceDescriptor) {

        String nodeSourceName = nodeSourceDescriptor.getName();
        NodeSource nodeSource = this.createNodeSourceInstance(nodeSourceDescriptor);

        this.definedNodeSources.put(nodeSourceDescriptor.getName(), nodeSource);

        if (nodeSourceDescriptor.getStatus().equals(NodeSourceStatus.NODES_DEPLOYED)) {
            NodeSource nodeSourceToDeploy = this.createNodeSourceInstance(nodeSourceDescriptor);

            boolean recoverNodes = false;

            if (this.isNodeRecoveryEnabledForNodeSource(nodeSourceToDeploy)) {
                recoverNodes = this.nodesRecoveryManager.recoverFullyDeployedInfrastructureOrReset(nodeSourceName,
                                                                                                   nodeSourceToDeploy,
                                                                                                   nodeSourceDescriptor);
            } else {
                this.nodesRecoveryManager.logRecoveryAbortedReason(nodeSourceName,
                                                                   "Recovery is not enabled for this node source");
            }

            NodeSourcePolicy nodeSourcePolicyStub = this.createNodeSourcePolicyActivity(nodeSourceDescriptor,
                                                                                        nodeSourceToDeploy);
            NodeSource nodeSourceStub = this.createNodeSourceActivity(nodeSourceName, nodeSourceToDeploy);

            if (recoverNodes) {
                this.nodesRecoveryManager.recoverNodes(nodeSourceStub);
            }

            this.configureDeployedNodeSource(nodeSourceName,
                                             nodeSourceDescriptor,
                                             nodeSourceStub,
                                             nodeSourcePolicyStub);

            this.deployedNodeSources.put(nodeSourceName, nodeSourceStub);
        } else {
            this.nodesRecoveryManager.logRecoveryAbortedReason(nodeSourceName, "This node source is undeployed");
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper undeployNodeSource(String nodeSourceName, boolean preempt) {

        logger.info("Undeploy node source " + nodeSourceName + " with preempt=" + preempt + REQUESTED_BY_STRING +
                    this.caller.getName());

        if (!this.definedNodeSources.containsKey(nodeSourceName)) {
            throw new IllegalArgumentException("Unknown node source " + nodeSourceName);
        }

        if (this.deployedNodeSources.containsKey(nodeSourceName)) {

            NodeSource nodeSourceToRemove = this.deployedNodeSources.get(nodeSourceName);

            this.caller.checkPermission(nodeSourceToRemove.getAdminPermission(),
                                        this.caller + " is not authorized to remove " + nodeSourceName,
                                        new RMCoreAllPermission());

            nodeSourceToRemove.setStatus(NodeSourceStatus.NODES_UNDEPLOYED);

            this.removeAllNodes(nodeSourceName, preempt);

            this.updateNodeSourceDescriptorWithStatusAndPersist(this.definedNodeSources.get(nodeSourceName)
                                                                                       .getDescriptor(),
                                                                NodeSourceStatus.NODES_UNDEPLOYED);

            this.nodeSourceUnregister(nodeSourceName,
                                      NodeSourceStatus.NODES_UNDEPLOYED,
                                      new RMNodeSourceEvent(RMEventType.NODESOURCE_SHUTDOWN,
                                                            this.caller.getName(),
                                                            nodeSourceName,
                                                            nodeSourceToRemove.getDescription(),
                                                            nodeSourceToRemove.getAdministrator().getName(),
                                                            NodeSourceStatus.NODES_UNDEPLOYED.toString()));

            // asynchronously delegate the removal process to the node source
            nodeSourceToRemove.shutdown(this.caller);
        }

        return new BooleanWrapper(true);
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

        this.nodesHouseKeepingService.stop();

        if (PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean() ||
            this.deployedNodeSources.size() == 0) {
            finalizeShutdown();
        } else {
            this.deployedNodeSources.forEach((nodeSourceName, nodeSource) -> {
                removeAllNodes(nodeSourceName, preempt, true);
                nodeSource.shutdown(this.caller);
            });
            waitForAllNodeSourcesToBeShutdown();
            finalizeShutdown();
        }
        return new BooleanWrapper(true);
    }

    private void waitForAllNodeSourcesToBeShutdown() {
        boolean atLeastOneAlive = false;
        int millisBeforeHardShutdown = 0;
        try {
            do {
                millisBeforeHardShutdown++;
                Thread.sleep(100);
                for (Entry<String, NodeSource> entry : this.deployedNodeSources.entrySet()) {
                    atLeastOneAlive = atLeastOneAlive || isNodeSourceAlive(entry);
                }
            } while (atLeastOneAlive &&
                     millisBeforeHardShutdown < PAResourceManagerProperties.RM_SHUTDOWN_TIMEOUT.getValueAsInt() * 10);
        } catch (InterruptedException e) {
            Thread.interrupted();
            logger.warn("", e);
        }
    }

    private boolean isNodeSourceAlive(Entry<String, NodeSource> entry) {
        try {
            return PAActiveObject.pingActiveObject(entry.getValue());
        } catch (Exception e) {
            return false;
        }
    }

    private void emitNodeSourceEvent(NodeSource nodeSource, RMEventType eventType) {

        NodeSourceDescriptor descriptor = nodeSource.getDescriptor();

        this.monitoring.nodeSourceEvent(new RMNodeSourceEvent(eventType,
                                                              this.caller.getName(),
                                                              descriptor.getName(),
                                                              nodeSource.getDescription(),
                                                              descriptor.getProvider().getName(),
                                                              nodeSource.getStatus().toString()));
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
                                                                node.getNodeInformation().getURL(),
                                               new RMCoreAllPermission(),
                                               new NodeUserAllPermission());

                        if (rmnode.isToRemove()) {
                            removeNodeFromCoreAndSource(rmnode, caller);
                            nodesReleased.add(node);
                            if (delayedNodeSourceRemovalEvents.containsKey(rmnode.getNodeSourceName()) &&
                                nodeSourceCanBeRemoved(rmnode.getNodeSourceName())) {

                                logger.debug(NODE_SOURCE_STRING + rmnode.getNodeSourceName() +
                                             " is eligible to remove.");

                                final Entry<RMNodeSourceEvent, NodeSource> remove = delayedNodeSourceRemovalEvents.remove(rmnode.getNodeSourceName());

                                final RMNodeSourceEvent removedEvent = remove.getKey();
                                final NodeSource nodeSource = remove.getValue();

                                logger.info(NODE_SOURCE_STRING + rmnode.getNodeSourceName() + HAS_BEEN_SUCCESSFULLY +
                                            removedEvent.getEventType().getDescription());

                                this.monitoring.nodeSourceEvent(removedEvent);
                                nodeSource.shutdown(this.caller);
                            } else if (delayedNodeSourceUndeploying.containsKey(rmnode.getNodeSourceName()) &&
                                       nodeSourceCanBeRemoved(rmnode.getNodeSourceName())) {
                                logger.debug(NODE_SOURCE_STRING + rmnode.getNodeSourceName() +
                                             " is eligible to undeploy.");

                                final NodeSource nodeSource = delayedNodeSourceUndeploying.remove(rmnode.getNodeSourceName());

                                logger.info(NODE_SOURCE_STRING + rmnode.getNodeSourceName() + HAS_BEEN_SUCCESSFULLY +
                                            "undeployed.");
                                nodeSource.shutdown(this.caller);
                            }
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
     * Core at the beginning of monitoring. The monitor is built in three parts
     * first with all the nodes knows by the RMCore, then with all deploying
     * nodes known by the deployed node sources and then with all the defined
     * node sources.
     *
     * @return RMInitialState containing nodes and nodeSources of the RMCore.
     */
    public RMInitialState getRMInitialState() {

        final Map<String, RMNodeEvent> nodeEvents = this.allNodes.values()
                                                                 .stream()
                                                                 .map(RMNode::createNodeEvent)
                                                                 .collect(Collectors.toMap(RMNodeEvent::getNodeUrl,
                                                                                           event -> event));

        for (NodeSource source : this.deployedNodeSources.values()) {
            for (RMDeployingNode node : source.getDeployingAndLostNodes()) {
                final RMNodeEvent nodeEvent = node.createNodeEvent();
                nodeEvents.put(nodeEvent.getNodeUrl(), nodeEvent);
            }
        }

        final List<RMNodeSourceEvent> nodeSourceEvents = new ArrayList<>(this.definedNodeSources.values()
                                                                                                .stream()
                                                                                                .map(NodeSource::createNodeSourceEvent)
                                                                                                .collect(Collectors.toList()));

        long eventCounter = 0;
        for (RMNodeSourceEvent nodeSourceEvent : nodeSourceEvents) {
            nodeSourceEvent.setCounter(eventCounter++);
        }
        for (RMNodeEvent nodeEvent : nodeEvents.values()) {
            nodeEvent.setCounter(eventCounter++);
        }

        final RMInitialState rmInitialState = new RMInitialState();
        rmInitialState.addAll(nodeEvents.values());
        rmInitialState.addAll(nodeSourceEvents);

        return rmInitialState;
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
    @ImmediateService
    public StringWrapper getRMThreadDump() {
        String threadDump;
        try {
            threadDump = this.nodeRM.getThreadDump();
        } catch (ProActiveException e) {
            logger.error("Could not get Resource Manager thread dump", e);
            throw new ThreadDumpNotAccessibleException(this.getUrl(), "Failed fetching thread dump", e);
        }

        logger.debug("Resource Manager thread dump: " + threadDump);
        return new StringWrapper(threadDump);
    }

    @Override
    @ImmediateService
    public StringWrapper getNodeThreadDump(String nodeUrl) {
        RMNode node;
        try {
            node = getAliveNodeOrFail(nodeUrl);
        } catch (RuntimeException e) {
            logger.warn("Could not get node thread dump for node " + nodeUrl + ": " + e.getMessage());
            throw new ThreadDumpNotAccessibleException(nodeUrl, e.getMessage());
        }

        String threadDump;
        try {
            threadDump = node.getNode().getThreadDump();
        } catch (ProActiveException e) {
            logger.error("Could not get node thread dump for node " + nodeUrl, e);
            throw new ThreadDumpNotAccessibleException(nodeUrl, "Failed fetching thread dump", e);
        }

        logger.debug("Thread dump for node " + nodeUrl + ": " + threadDump);
        return new StringWrapper(threadDump);
    }

    private RMNode getAliveNodeOrFail(String nodeUrl) {
        if (nodeUrl == null) {
            throw new IllegalArgumentException("The given node URL is null");
        }
        if (!this.allNodes.containsKey(nodeUrl)) {
            throw new IllegalArgumentException("The node is not managed by the Resource Manager");
        }
        RMNode node = this.allNodes.get(nodeUrl);
        if (node.isDown()) {
            throw new IllegalArgumentException("The node is DOWN");
        }
        return node;
    }

    @Override
    public Set<String> listAliveNodeUrls() {
        HashSet<String> aliveNodes = new HashSet<>();
        for (RMNode node : this.allNodes.values()) {
            if (!node.isDown()) {
                aliveNodes.add(node.getNodeURL());
            }
        }
        return aliveNodes;
    }

    @Override
    public Set<String> listAliveNodeUrls(Set<String> nodeSourceNames) {
        HashSet<String> aliveNodes = new HashSet<>();
        for (String nodeSource : nodeSourceNames) {
            if (this.deployedNodeSources.containsKey(nodeSource)) {
                for (Node node : this.deployedNodeSources.get(nodeSource).getAliveNodes()) {
                    aliveNodes.add(node.getNodeInformation().getURL());
                }
            }
        }
        return aliveNodes;
    }

    /**
     * Unregisters node source from the resource manager core.
     */
    public BooleanWrapper nodeSourceUnregister(String nodeSourceName, NodeSourceStatus nodeSourceStatus,
            RMNodeSourceEvent evt) {

        NodeSource nodeSource = this.deployedNodeSources.remove(nodeSourceName);

        if (nodeSource == null) {
            logger.warn("Attempt to remove non-existing node source " + nodeSourceName);
            return new BooleanWrapper(false);
        }

        logger.info(NODE_SOURCE_STRING + nodeSourceName + HAS_BEEN_SUCCESSFULLY + evt.getEventType().getDescription());

        this.monitoring.nodeSourceEvent(evt);

        this.emitRemovedEventIfNodeSourceWasNotUndeployed(nodeSource, nodeSourceStatus);

        return new BooleanWrapper(true);
    }

    private void emitRemovedEventIfNodeSourceWasNotUndeployed(NodeSource nodeSource,
            NodeSourceStatus nodeSourceStatus) {

        String nodeSourceAdministratorName = nodeSource.getAdministrator().getName();
        String nodeSourceName = nodeSource.getName();

        switch (nodeSourceStatus) {
            case NODES_DEPLOYED:

                RMNodeSourceEvent removedEvent = new RMNodeSourceEvent(RMEventType.NODESOURCE_REMOVED,
                                                                       nodeSourceAdministratorName,
                                                                       nodeSourceName,
                                                                       nodeSource.getDescription(),
                                                                       nodeSourceAdministratorName,
                                                                       nodeSourceStatus.toString());

                if (nodeSourceCanBeRemoved(nodeSourceName)) {
                    logger.info(NODE_SOURCE_STRING + nodeSourceName + HAS_BEEN_SUCCESSFULLY +
                                removedEvent.getEventType().getDescription());

                    this.monitoring.nodeSourceEvent(removedEvent);
                } else {
                    logger.info(NODE_SOURCE_STRING + nodeSourceName +
                                " cannot be removed NOW, so its removing is delayed.");
                    AbstractMap.SimpleEntry<RMNodeSourceEvent, NodeSource> pair = new AbstractMap.SimpleEntry<>(removedEvent,
                                                                                                                nodeSource);
                    // postpone node source removal event
                    delayedNodeSourceRemovalEvents.put(nodeSourceName, pair);
                }
                break;
            case NODES_UNDEPLOYED:
                if (nodeSourceCanBeRemoved(nodeSourceName)) {
                    logger.info(NODE_SOURCE_STRING + nodeSourceName + HAS_BEEN_SUCCESSFULLY + "undeployed");
                } else {
                    logger.info(NODE_SOURCE_STRING + nodeSourceName +
                                " cannot be undeployed NOW, so its undeploying is delayed.");
                    delayedNodeSourceUndeploying.put(nodeSourceName, nodeSource);
                }
                break;
        }

    }

    private boolean nodeSourceCanBeRemoved(String nodeSourceName) {
        return allNodes.values().stream().noneMatch(rmNode -> rmNode.getNodeSourceName().equals(nodeSourceName));
    }

    private void finalizeShutdown() {

        this.selectionManager.shutdown();
        this.clientPinger.shutdown();

        PAFuture.waitFor(this.monitoring.shutdown());

        PAActiveObject.terminateActiveObject(false);

        try {
            Thread.sleep(2000);
            if (PAResourceManagerProperties.RM_SHUTDOWN_KILL_RUNTIME.getValueAsBoolean()) {
                this.nodeRM.getProActiveRuntime().killRT(true);
            }
        } catch (Exception e) {
            logger.debug("", e);
        }
    }

    public void setBusyNode(final String nodeUrl, Client owner) throws NotConnectedException {
        setBusyNode(nodeUrl, owner, Collections.EMPTY_MAP);
    }

    /**
     * Set a node state to busy. Set the node to busy, and move the node to the
     * internal busy nodes list. An event informing the node state's change is
     * thrown to RMMonitoring.
     *
     * @param owner
     * @param nodeUrl node to set
     */
    public void setBusyNode(final String nodeUrl, Client owner, Map<String, String> usageInfo)
            throws NotConnectedException {
        final RMNode rmNode = this.allNodes.get(nodeUrl);
        if (rmNode == null) {
            logger.error("Unknown node " + nodeUrl);
            return;
        }

        if (!clients.containsKey(owner.getId()) && owner != localClient) {
            logger.warn(nodeUrl + " cannot set busy as the client disconnected " + owner);
            throw new NotConnectedException("Client " + owner + " is not connected to the resource manager");
        }

        // If the node is already busy no need to go further
        if (rmNode.isBusy()) {
            return;
        }
        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();
        rmNode.setBusy(owner, usageInfo);

        this.eligibleNodes.remove(rmNode);

        persistUpdatedRMNodeIfRecoveryEnabled(rmNode);

        // create the event
        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                             previousNodeState,
                                                             owner.getName()));

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

            persistUpdatedRMNodeIfRecoveryEnabled(rmNode);

            // create the event
            this.registerAndEmitNodeEvent(rmNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                                 previousNodeState,
                                                                 rmNode.getProvider().getName()));
        } else {
            // the nodes has been removed from core asynchronously
            // when pinger of selection manager tried to access it
            // do nothing in this case
            logger.debug("setDownNode returned immediately because the node " + nodeUrl + " was not known");
        }
    }

    public void registerAndEmitNodeEvent(final RMNodeEvent event) {
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
        if (this.deployedNodeSources.containsKey(sourceName)) {
            return this.deployedNodeSources.get(sourceName).getPingFrequency();
        } else {
            throw new IllegalArgumentException("Unknown node source " + sourceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper setNodeSourcePingFrequency(int frequency, String sourceName) {
        if (deployedNodeSources.containsKey(sourceName)) {
            deployedNodeSources.get(sourceName).setPingFrequency(frequency);
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
        return getRMInitialState().getNodeSourceEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public List<RMNodeEvent> getNodesList() {
        return getRMInitialState().getNodeEvents();
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper removeNodeSource(String nodeSourceName, boolean preempt) {

        logger.info("Remove node source " + nodeSourceName + " with preempt=" + preempt + REQUESTED_BY_STRING +
                    this.caller.getName());

        NodeSource nodeSourceToRemove;
        if (this.definedNodeSources.containsKey(nodeSourceName)) {
            nodeSourceToRemove = this.definedNodeSources.get(nodeSourceName);
        } else if (this.deployedNodeSources.containsKey(nodeSourceName)) {
            nodeSourceToRemove = this.deployedNodeSources.get(nodeSourceName);
        } else {
            throw new IllegalArgumentException("Unknown node source " + nodeSourceName);
        }
        this.caller.checkPermission(nodeSourceToRemove.getAdminPermission(),
                                    this.caller + " is not authorized to remove " + nodeSourceName,
                                    new RMCoreAllPermission());

        this.shutDownNodeSourceIfDeployed(nodeSourceName, preempt);
        this.removeDefinedNodeSource(nodeSourceName, nodeSourceToRemove);
        return new BooleanWrapper(true);
    }

    private void removeDefinedNodeSource(String nodeSourceName, NodeSource nodeSourceToRemove) {

        this.definedNodeSources.remove(nodeSourceName);
        this.dbManager.removeNodeSource(nodeSourceName);

        if (nodeSourceToRemove.getStatus().equals(NodeSourceStatus.NODES_UNDEPLOYED)) {

            logger.info(NODE_SOURCE_STRING + nodeSourceName + " has been successfully removed");

            // if the node source to remove is not deployed, we need to issue
            // the node source removed event right now, because we will not
            // receive it from the node source shutdown call back
            this.monitoring.nodeSourceEvent(new RMNodeSourceEvent(RMEventType.NODESOURCE_REMOVED,
                                                                  this.caller.getName(),
                                                                  nodeSourceToRemove.getName(),
                                                                  nodeSourceToRemove.getDescription(),
                                                                  nodeSourceToRemove.getAdministrator().getName(),
                                                                  nodeSourceToRemove.getStatus().toString()));
        }
    }

    private void shutDownNodeSourceIfDeployed(String nodeSourceName, boolean preempt) {

        if (this.deployedNodeSources.containsKey(nodeSourceName)) {

            NodeSource nodeSourceToRemove = this.deployedNodeSources.get(nodeSourceName);

            this.removeAllNodes(nodeSourceName, preempt);

            NodeSourceStatus status = nodeSourceToRemove.getStatus();

            this.nodeSourceUnregister(nodeSourceName,
                                      status,
                                      new RMNodeSourceEvent(RMEventType.NODESOURCE_SHUTDOWN,
                                                            this.caller.getName(),
                                                            nodeSourceName,
                                                            nodeSourceToRemove.getDescription(),
                                                            nodeSourceToRemove.getAdministrator().getName(),
                                                            status.toString()));

            // asynchronously delegate the removal process to the node source
            nodeSourceToRemove.shutdown(this.caller);
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

    public List<String> getToBeRemovedUnavailableNodesUrls() {

        List<String> unavailableNodesUrl = new LinkedList<>();

        unavailableNodesUrl.addAll(this.allNodes.values()
                                                .stream()
                                                .filter(this::isNodeUnavailableForTooLong)
                                                .map(RMNode::getNodeURL)
                                                .collect(Collectors.toList()));

        unavailableNodesUrl.addAll(this.deployedNodeSources.entrySet()
                                                           .stream()
                                                           .map(Entry::getValue)
                                                           .map(NodeSource::getDeployingAndLostNodes)
                                                           .flatMap(list -> list.stream()
                                                                                .filter(this::isNodeUnavailableForTooLong)
                                                                                .map(RMDeployingNode::getNodeURL))
                                                           .collect(Collectors.toList()));

        return unavailableNodesUrl;
    }

    private boolean isNodeUnavailableForTooLong(RMNode node) {
        if (PAResourceManagerProperties.RM_UNAVAILABLE_NODES_MAX_PERIOD.isSet()) {
            int periodInMinutes = PAResourceManagerProperties.RM_UNAVAILABLE_NODES_MAX_PERIOD.getValueAsInt();
            int periodInMilliseconds = periodInMinutes * 60 * 1000;
            return (node.getState().equals(NodeState.DOWN) || node.getState().equals(NodeState.LOST)) &&
                   (node.millisSinceStateChanged() > periodInMilliseconds);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public BooleanWrapper isActive() {
        // return false for non connected clients
        // it should be verified by checkPermissionsMethod but it returns true for
        // local active objects
        final Client sourceBodyCaller = checkPermissionAndGetClientIsSuccessful();
        return new BooleanWrapper(!toShutDown && clients.containsKey(sourceBodyCaller.getId()));
    }

    private Client checkPermissionAndGetClientIsSuccessful() {
        final Request currentRequest = PAActiveObject.getContext().getCurrentRequest();
        String methodName = currentRequest.getMethodName();
        return checkMethodCallPermission(methodName, currentRequest.getSourceBodyID());
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
        return this.nodeSourceParameterHelper.getPluginsDescriptor(InfrastructureManagerFactory.getSupportedInfrastructures());
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return this.nodeSourceParameterHelper.getPluginsDescriptor(NodeSourcePolicyFactory.getSupportedPolicies());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeSourceConfiguration getNodeSourceConfiguration(String nodeSourceName) {

        NodeSource nodeSource = getDefinedNodeSourceOrFail(nodeSourceName);

        NodeSourceDescriptor nodeSourceDescriptor = nodeSource.getDescriptor();

        PluginDescriptor infrastructurePluginDescriptor = this.nodeSourceParameterHelper.getPluginDescriptor(nodeSourceDescriptor.getInfrastructureType(),
                                                                                                             nodeSourceDescriptor.getInfrastructureParameters(),
                                                                                                             nodeSourceName);
        PluginDescriptor policyPluginDescriptor = this.nodeSourceParameterHelper.getPluginDescriptor(nodeSourceDescriptor.getPolicyType(),
                                                                                                     nodeSourceDescriptor.getPolicyParameters(),
                                                                                                     nodeSourceName);

        return new NodeSourceConfiguration(nodeSourceName,
                                           nodeSource.nodesRecoverable(),
                                           infrastructurePluginDescriptor,
                                           policyPluginDescriptor);
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

        client.checkPermission(methodCallPermission,
                               client + " is not authorized to call " + fullMethodName,
                               new RMCoreAllPermission());
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
        NodeSource ns = this.deployedNodeSources.get(nsName);
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
    private void validateNodeSourceNameOrFail(String nodeSourceName) {
        if (nodeSourceName == null) {
            throw new IllegalArgumentException("Node Source name cannot be null");
        }
        if (nodeSourceName.length() == 0) {
            throw new IllegalArgumentException("Node Source name cannot be empty");
        }
        if (this.definedNodeSources.containsKey(nodeSourceName) ||
            this.deployedNodeSources.containsKey(nodeSourceName) ||
            this.delayedNodeSourceRemovalEvents.containsKey(nodeSourceName) ||
            this.delayedNodeSourceUndeploying.containsKey(nodeSourceName)) {
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
            client.checkPermission(nodeSource.getAdminPermission(), errorMessage, new RMCoreAllPermission());
        } catch (SecurityException ex) {
            // the caller is not an administrator, so checking if it is a node source provider
            client.checkPermission(nodeSource.getProviderPermission(), errorMessage, new RMCoreAllPermission());
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

    public BooleanWrapper lockNodes(Set<String> urls, final Client caller) {
        return mapOnNodeUrlSet(urls, node -> internalLockNode(node, caller), "lock");
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
        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                             rmNode.getState(),
                                                             lockInitiator.getName()));

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanWrapper unlockNodes(Set<String> urls) {
        return mapOnNodeUrlSet(urls, this::internalUnlockNode, "unlock");
    }

    public BooleanWrapper mapOnNodeUrlSet(Set<String> nodeUrls, Predicate<RMNode> operation, String operationName) {
        boolean result = true;

        for (String url : nodeUrls) {
            try {
                RMNode rmnode = getNodeByUrlIncludingDeployingNodes(url);

                if (rmnode == null) {
                    logger.warn("Cannot " + operationName + ", unknown node: " + url);
                    result = false;
                    continue;
                }

                result &= operation.apply(rmnode);
            } catch (Exception e) {
                logger.error("Error during " + operationName + " on node " + url, e);
                result = false;
            }
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

        this.registerAndEmitNodeEvent(rmNode.createNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                             rmNode.getState(),
                                                             caller.getName()));

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
                                                                " from " + rmnode.getNodeSource().getName(),
                                   new RMCoreAllPermission());
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
                                                               rmnode.getNodeSource().getName(),
                                   new NodeUserAllPermission());
        } catch (SecurityException e) {
            // client does not have an access to this node
            logger.debug(e.getMessage());
            return new BooleanWrapper(false);
        }
        return new BooleanWrapper(true);
    }

    @Override
    @ImmediateService
    public List<ScriptResult<Object>> executeScript(String script, String scriptEngine, String targetType,
            Set<String> targets) {
        try {
            return this.executeScript(new SimpleScript(script, scriptEngine), targetType, targets);
        } catch (Exception e) {
            logger.error("Error while executing node script", e);
            return Collections.singletonList(new ScriptResult<>(new ScriptException(e)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public <T> List<ScriptResult<T>> executeScript(Script<T> script, String targetType, Set<String> targets) {
        Client client;
        try {
            client = checkPermissionAndGetClientIsSuccessful();
        } catch (Exception e) {
            logger.error("Error while checking permission to execute node script", e);
            return Collections.singletonList(new ScriptResult<>(new ScriptException(e)));
        }
        // Depending on the target type, select nodes for script execution
        final TargetType tType = TargetType.valueOf(targetType);
        final HashSet<RMNode> selectedRMNodes = new HashSet<>();
        switch (tType) {
            case NODESOURCE_NAME:
                // If target is a nodesource name select one node, not busy if possible
                for (String target : targets) {
                    NodeSource nodeSource = this.deployedNodeSources.get(target);
                    if (nodeSource != null) {
                        Set<String> scriptExecutionHostNames = new HashSet<>();
                        for (RMNode candidateNode : this.allNodes.values()) {
                            String candidateNodeHostName = candidateNode.getHostName();
                            if (candidateNode.getNodeSource().equals(nodeSource) &&
                                !scriptExecutionHostNames.contains(candidateNodeHostName)) {
                                scriptExecutionHostNames.add(candidateNodeHostName);
                                this.selectCandidateNode(selectedRMNodes, candidateNode, client);
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
                        this.selectCandidateNode(selectedRMNodes, candidateNode, client);
                    }
                }
                break;
            case HOSTNAME:
                // If target is hostname select first node from that host
                for (String target : targets) {
                    for (RMNode node : this.allNodes.values()) {
                        if (node.getHostName().equals(target)) {
                            this.selectCandidateNode(selectedRMNodes, node, client);
                            break;
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unable to execute node script, unknown target type: " + targetType);
        }

        // Return a ProActive future on the list of results
        return this.selectionManager.executeScript(script, selectedRMNodes, null);

        // To avoid blocking rmcore ao the call is delegated to the selection
        // manager ao and each node is unlocked as soon as the script has
        // finished it's execution.
    }

    private void selectCandidateNode(HashSet<RMNode> selectedRMNodes, RMNode candidateNode, Client clientCaller) {
        if (this.internalLockNode(candidateNode, clientCaller)) {
            selectedRMNodes.add(candidateNode);
        } else {
            // Unlock all previously locked nodes
            this.unselectNodes(selectedRMNodes);

            throw new IllegalStateException("Node script cannot be executed atomically since the node is already locked: " +
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
        nodesRecoveryManager.restoreLock(rmNode, caller);
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

    @Override
    public void releaseBusyNodesNotInList(List<NodeSet> nodesToNotRelease) {
        Set<String> nodesUrlToNotRelease = new HashSet<>();

        for (NodeSet nodeSet : nodesToNotRelease) {
            nodesUrlToNotRelease.addAll(nodeSet.getAllNodesUrls());
        }

        List<RMNode> busyNodesToRelease = findBusyNodesNotInSet(nodesUrlToNotRelease);

        nodesCleaner.cleanAndRelease(busyNodesToRelease);
    }

    private List<RMNode> findBusyNodesNotInSet(Set<String> nodesURL) {
        if (logger.isDebugEnabled()) {
            logger.debug("Given set of busy nodes URL: " + Arrays.toString(nodesURL.toArray()));
        }
        List<RMNode> busyNodesNotInGivenSet = new LinkedList<>();

        for (Entry<String, RMNode> nodeEntry : allNodes.entrySet()) {
            String nodeUrl = nodeEntry.getKey();
            RMNode node = nodeEntry.getValue();

            if (node.isBusy() && !nodesURL.contains(nodeUrl)) {
                logger.debug("Found busy node not in given set: " + node.getNodeName());
                busyNodesNotInGivenSet.add(node);
            }
        }

        return busyNodesNotInGivenSet;
    }

    @Override
    public boolean areNodesKnown(NodeSet nodes) {
        Set<String> nodesURL = nodes.getAllNodesUrls();
        if (logger.isDebugEnabled()) {
            logger.debug("Check whether RM knows nodes URL: " + Arrays.toString(nodesURL.toArray()));
        }

        for (String nodeURL : nodesURL) {
            if (!allNodes.containsKey(nodeURL)) {
                logger.info("RM is not aware of node URL " + nodeURL);
                return false;
            }
        }

        logger.info("All given nodes are managed by the RM");
        return true;
    }

    @Override
    public boolean areNodesRecoverable(NodeSet nodes) {
        Set<String> nodesURL = nodes.getAllNodesUrls();

        for (String nodeURL : nodesURL) {
            RMNode rmNode = allNodes.get(nodeURL);
            if (rmNode == null || !rmNode.getNodeSource().nodesRecoverable()) {
                logger.debug("RMNode corresponding to URL " + nodeURL + " is not recoverable");
                return false;
            }
        }

        logger.debug("All given nodes are recoverable");
        return true;
    }

    @Override
    public void setNeededNodes(int neededNodes) {
        this.monitoring.setNeededNodes(neededNodes);
    }

    @Override
    public Map<String, List<String>> getInfrasToPoliciesMapping() {
        Map<String, List<String>> mapping = new HashMap<>();
        String fileName = null;
        try {
            fileName = PAResourceManagerProperties.RM_NODESOURCE_INFRA_POLICY_MAPPING.getValueAsString();
            if (!(new File(fileName).isAbsolute())) {
                // file path is relative, so we complete the path with the prefix RM_Home constant
                fileName = PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + fileName;
            }

            mapping = Files.readAllLines(Paths.get(fileName))
                           .stream()
                           .map(line -> line.split(","))
                           .filter(array -> array.length > 1)
                           .map(Arrays::asList)
                           .collect(Collectors.toMap(list -> list.get(0).trim(), list -> list.subList(1, list.size())
                                                                                             .stream()
                                                                                             .map(String::trim)
                                                                                             .collect(Collectors.toList())));
        } catch (Exception e) {
            logger.error("Error when loading infrastructure definition file : " + fileName, e);
        }
        return mapping;
    }

    @Override
    public List<RMNodeHistory> getNodesHistory(long windowStart, long windowEnd) {
        List<NodeHistory> nodesHistory = dbManager.getNodesHistory(windowStart, windowEnd);

        return nodesHistory.stream().map(nodeHistory -> {
            RMNodeHistory rmNodeHistory = new RMNodeHistory();
            rmNodeHistory.setEndTime(nodeHistory.getEndTime());
            rmNodeHistory.setHost(nodeHistory.getHost());
            rmNodeHistory.setNodeSource(nodeHistory.getNodeSource());
            rmNodeHistory.setNodeState(nodeHistory.getNodeState());
            rmNodeHistory.setNodeUrl(nodeHistory.getNodeUrl());
            rmNodeHistory.setProviderName(nodeHistory.getProviderName());
            rmNodeHistory.setUserName(nodeHistory.getUserName());
            rmNodeHistory.setStartTime(nodeHistory.getStartTime());
            rmNodeHistory.setDefaultJmxUrl(nodeHistory.getDefaultJmxUrl());
            rmNodeHistory.setUsageInfo(nodeHistory.getUsageInfo());
            return rmNodeHistory;
        }).collect(Collectors.toList());
    }

    /**
     * Add the information of the given node to the database.
     *
     * @param rmNode the node to add to the database
     */
    private void persistNewRMNodeIfRecoveryEnabled(RMNode rmNode) {
        if (nodesRecoveryEnabledForNode(rmNode)) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            dbManager.addNode(rmNodeData, rmNode.getNodeSourceName());
        }
    }

    /**
     * Update the information of the given node in database.
     *
     * @param rmNode the node to update in database
     */
    private void persistUpdatedRMNodeIfRecoveryEnabled(RMNode rmNode) {
        if (nodesRecoveryEnabledForNode(rmNode)) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            dbManager.updateNode(rmNodeData, rmNode.getNodeSourceName());
        }
    }

    private boolean isNodesRecoveryEnabled() {
        return PAResourceManagerProperties.RM_NODES_RECOVERY.getValueAsBoolean();
    }

    private boolean isNodeRecoveryEnabledForNodeSource(NodeSource nodeSource) {
        return isNodesRecoveryEnabled() && nodeSource.nodesRecoverable();
    }

    private boolean nodesRecoveryEnabledForNode(RMNode rmNode) {
        return isNodesRecoveryEnabled() && rmNode.getNodeSource().nodesRecoverable();
    }

    @Override
    public void addNodeToken(String nodeUrl, String token) throws RMException {
        if (allNodes.containsKey(nodeUrl)) {
            RMNode rmNode = allNodes.get(nodeUrl);
            rmNode.addToken(token);
        } else {
            throw new RMException("Unknown node " + nodeUrl);
        }
    }

    @Override
    public void removeNodeToken(String nodeUrl, String token) throws RMException {
        if (allNodes.containsKey(nodeUrl)) {
            RMNode rmNode = allNodes.get(nodeUrl);
            rmNode.removeToken(token);
        } else {
            throw new RMException("Unknown node " + nodeUrl);
        }
    }
}
