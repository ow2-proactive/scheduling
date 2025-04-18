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
package org.ow2.proactive.resourcemanager.nodesource;

import java.io.Serializable;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.principals.IdentityPrincipal;
import org.ow2.proactive.authentication.principals.TokenPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.permissions.NSAdminPermission;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.permissions.RMCoreAllPermission;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.rmnode.AbstractRMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

import com.google.common.annotations.VisibleForTesting;


/**
 * Abstract class designed to manage a NodeSource. A NodeSource active object is
 * designed to manage acquisition, monitoring and removing of a set of
 * {@link Node} objects in the Resource Manager. Each node source consists
 * of two entities {@link InfrastructureManager} and {@link NodeSourcePolicy}. <BR>
 *
 * Each particular {@link InfrastructureManager} defines a specific infrastructure and ways
 * to manipulate nodes. <BR>
 *
 * The node source policy {@link NodeSourcePolicy} defines a strategy of nodes acquisition.
 * It can be static acquiring nodes once and forever or dynamic where nodes acquisition takes
 * into account different external factors such as time, scheduling state, etc.
 *
 */
@ActiveObject
public class NodeSource implements InitActive, RunActive {

    private static Logger logger = Logger.getLogger(NodeSource.class);

    private int pingFrequency = PAResourceManagerProperties.RM_NODE_SOURCE_PING_FREQUENCY.getValueAsInt();

    /** Default recovery mode for NS with local nodes started with the RM by default */
    public static final boolean DEFAULT_LOCAL_NODES_NODE_SOURCE_RECOVERABLE = false;

    public static final boolean DEFAULT_RECOVERABLE = true;

    public static final int INTERNAL_POOL = 0;

    public static final int EXTERNAL_POOL = 1;

    public static final int PINGER_POOL = 2;

    public static final int NUMBER_OF_THREAD_POOLS = 3;

    /** unique name of the source */
    private final String name;

    private InfrastructureManager infrastructureManager;

    /**
     * A reference to the plain old instance of node source policy
     * {@see NodeSource#activeNodeSourcePolicy}. This policy object is not yet
     * configured, and as such it should only be used in the constructor of
     * the node source.
     */
    private final NodeSourcePolicy policy;

    /**
     * Node source policy reference to use when the node source policy has
     * been turned active, i.e. when the node source is deployed.
     */
    private NodeSourcePolicy activePolicy;

    private final RMCore rmcore;

    // The url used by spawn nodes to register themself
    private final String registrationURL;

    private boolean toShutdown = false;

    private static int MAX_REMOVED_NODES_HISTORY = 200;

    // all nodes except down
    private Map<String, Node> nodes;

    private Map<String, Node> downNodes;

    private Map<String, Node> removedNodes;

    private static ThreadPoolHolder threadPoolHolder;

    private NodeSource stub;

    private final Client administrator;

    // to be able to emit rmdeployingnode related events
    private final transient RMMonitoringImpl monitoring;

    // admin can remove node source, add nodes to the node source, remove any node
    // it is a PrincipalPermission of the user who created this node source
    private Permission adminPermission;

    // provider can add nodes to the node source, remove only its nodes
    // level is configured by ns admin at the moment of ns creation
    // NOTE: the administrator is always the provider because each provider is one of those:
    // ns creator, ns creator groups, all
    private Permission providerPermission;

    // user can get nodes for running computations
    // level is configured by ns admin at the moment of ns creation
    private AccessType nodeUserAccessType;

    private NodeSourceDescriptor descriptor;

    private LinkedHashMap<String, String> additionalInformation;

    /**
     * Database manager, used to persist the runtime variables.
     */
    private RMDBManager dbManager;

    /**
     * Information related to node source that are persisted in database
     */
    private NodeSourceData nodeSourceData;

    /**
     * Administrator's tenant
     */
    private String tenant;

    /**
     * Creates a new instance of NodeSource.
     * This constructor is used by Proactive as one of requirements for active objects.
     */
    public NodeSource() {
        registrationURL = null;
        name = null;
        infrastructureManager = null;
        policy = null;
        rmcore = null;
        administrator = null;
        adminPermission = null;
        providerPermission = null;
        monitoring = null;
        descriptor = null;
        additionalInformation = new LinkedHashMap<>();
        tenant = null;
    }

    /**
     * Creates a new instance of NodeSource.
     *
     * @param name node source name
     * @param registrationURL the url used by the spawn nodes to register
     * @param im underlying infrastructure manager
     * @param policy nodes acquisition policy
     * @param rmcore resource manager core
     */
    public NodeSource(String registrationURL, String name, InfrastructureManager im, NodeSourcePolicy policy,
            RMCore rmcore, RMMonitoringImpl monitor, NodeSourceDescriptor nodeSourceDescriptor) {
        this.registrationURL = registrationURL;
        this.name = name;
        this.administrator = nodeSourceDescriptor.getProvider();
        this.infrastructureManager = im;
        this.policy = policy;
        this.rmcore = rmcore;
        this.monitoring = monitor;

        this.nodes = Collections.synchronizedMap(new HashMap<String, Node>());
        this.downNodes = Collections.synchronizedMap(new HashMap<String, Node>());
        // removedNodes is initialized using a LRU cache, it simply stores a fixed amount of removed nodes. After the maximum capacity is reached, it will replace oldest entries.
        this.removedNodes = Collections.synchronizedMap(new LinkedHashMap<String, Node>(10, 0.75f, true) {
            @Override
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_REMOVED_NODES_HISTORY;
            }
        });

        // node source admin permission
        // it's the PrincipalPermission of the user who created the node source
        this.adminPermission = new PrincipalPermission(this.administrator.getName(),
                                                       this.administrator.getSubject()
                                                                         .getPrincipals(UserNamePrincipal.class));
        // creating node source provider permission
        // could be one of the following: PrincipalPermission (NS creator) or PrincipalPermission (NS creator groups)
        // or PrincipalPermission (anyone)
        this.providerPermission = new PrincipalPermission(this.administrator.getName(),
                                                          this.policy.getProviderAccessType()
                                                                     .getIdentityPrincipals(this.administrator));
        this.nodeUserAccessType = this.policy.getUserAccessType();

        this.descriptor = nodeSourceDescriptor;

        this.additionalInformation = Optional.ofNullable(nodeSourceDescriptor.getAdditionalInformation())
                                             .orElse(new LinkedHashMap<>());

        this.tenant = administrator.getTenant();
    }

    @ImmediateService
    public AccessType getNodeUserAccessType() {
        return nodeUserAccessType;
    }

    @ImmediateService
    public String getTenant() {
        return tenant;
    }

    public static void initThreadPools() {
        if (threadPoolHolder == null) {
            try {
                int maxThreads = PAResourceManagerProperties.RM_NODESOURCE_MAX_THREAD_NUMBER.getValueAsInt();
                if (maxThreads < NUMBER_OF_THREAD_POOLS) {
                    maxThreads = NUMBER_OF_THREAD_POOLS;
                }

                // executor service initialization
                NodeSource.threadPoolHolder = new ThreadPoolHolder(new int[] { maxThreads / NUMBER_OF_THREAD_POOLS,
                                                                               maxThreads / NUMBER_OF_THREAD_POOLS,
                                                                               maxThreads / NUMBER_OF_THREAD_POOLS },
                                                                   new NamedThreadFactory[] { new NamedThreadFactory("Node Source threadpool # internal",
                                                                                                                     false,
                                                                                                                     7),
                                                                                              new NamedThreadFactory("Node Source threadpool # external",
                                                                                                                     false,
                                                                                                                     2),
                                                                                              new NamedThreadFactory("Node Source threadpool # pinger",
                                                                                                                     false,
                                                                                                                     2) });
            } catch (Exception e) {
                logger.fatal("Could not initialize threadPoolHolder", e);
                throw new IllegalStateException("Could not initialize threadPoolHolder", e);
            }
            logger.info("Thread pools started");
        }
    }

    public static void shutdownThreadPools() {
        if (threadPoolHolder != null) {
            threadPoolHolder.shutdownNow(PINGER_POOL);
            logger.info("Pinger Thread Pool terminated");
            threadPoolHolder.shutdown(EXTERNAL_POOL,
                                      PAResourceManagerProperties.RM_SHUTDOWN_TIMEOUT.getValueAsInt() - 1);
            logger.info("External Thread Pool terminated");
            threadPoolHolder.shutdownNow(INTERNAL_POOL);
            logger.info("Internal Thread Pool terminated");
            threadPoolHolder = null;
        }
    }

    /**
     * Initialization of node source. Creates and activates a pinger to monitor nodes.
     *
     * @param body active object body
     */
    public void initActivity(Body body) {

        this.stub = (NodeSource) PAActiveObject.getStubOnThis();
        this.infrastructureManager.setNodeSource(this);
        // Infrastructure has been configured and linked to the node source, so we can now persist the runtime
        // variables of the infrastructure for the first time (they have been initialized during the creation of the
        // infrastructure, in its configuration.
        this.infrastructureManager.persistInfrastructureVariables();
        this.activePolicy.setNodeSource((NodeSource) PAActiveObject.getStubOnThis());

        // Set permissions again according to the activated node source policy

        // node source admin permission
        // it's the PrincipalPermission of the user who created the node source
        this.adminPermission = new PrincipalPermission(this.administrator.getName(),
                                                       this.administrator.getSubject()
                                                                         .getPrincipals(UserNamePrincipal.class));
        // creating node source provider permission
        // could be one of the following: PrincipalPermission (NS creator) or PrincipalPermission (NS creator groups)
        // or PrincipalPermission (anyone)
        this.providerPermission = new PrincipalPermission(this.administrator.getName(),
                                                          this.activePolicy.getProviderAccessType()
                                                                           .getIdentityPrincipals(this.administrator));
        this.nodeUserAccessType = this.activePolicy.getUserAccessType();

        Thread.currentThread().setName("Node Source \"" + this.name + "\"");
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        long timeStamp = System.currentTimeMillis();
        long delta = 0;

        // recalculating nodes number only once per policy period
        while (body.isActive()) {

            try {
                service.blockingServeOldest(pingFrequency);
                delta += System.currentTimeMillis() - timeStamp;
                timeStamp = System.currentTimeMillis();

                if (delta > pingFrequency) {
                    logger.info("[" + name + "] Pinging alive nodes : " + getAliveNodes().size());
                    for (Node node : getAliveNodes()) {
                        pingNode(node);
                    }
                    delta = 0;
                }
            } catch (InterruptedException e) {
                logger.warn("runActivity interrupted", e);
            }
        }
    }

    /**
     * Updates internal node source structures.
     */
    @VisibleForTesting
    RMDeployingNode internalAddNode(Node node) throws RMException {
        String nodeUrl = node.getNodeInformation().getURL();
        if (this.nodes.containsKey(nodeUrl)) {
            throw new RMException("The node " + nodeUrl + " already added to the node source " + name);
        }

        logger.info("[" + name + "] new node available : " + node.getNodeInformation().getURL());
        RMDeployingNode rmDeployingNode = infrastructureManager.internalRegisterAcquiredNode(node);
        nodes.put(nodeUrl, node);
        if (removedNodes.containsKey(nodeUrl)) {
            removedNodes.remove(nodeUrl);
        }
        return rmDeployingNode;
    }

    /**
     * Recover the internal data structure of the node source so that
     * recovered nodes and RMnodes are linked again to the node source.
     * @return the {@link RMNode} that could be recovered.
     */
    public RMNode internalAddNodeAfterRecovery(Node node, RMNodeData rmNodeData) {
        RMNode recoveredRmNode;
        String nodeUrl = rmNodeData.getNodeUrl();
        // the infrastructure manager is up to date already because it was
        // saved in database, contrarily to the node source internal data structures
        RMNode rmNode = infrastructureManager.searchForNotAcquiredRmNode(nodeUrl);
        if (rmNode != null) {
            // Deploying or lost RMNodes can be directly found in the saved infrastructure.
            recoveredRmNode = rmNode;
        } else {
            // the node is acquired in the infrastructure. We can recover the
            // RMNode representation on top of it and save it in the node source
            recoveredRmNode = buildRMNodeAfterRecovery(node, rmNodeData);
        }
        // we finally put back the node in the data structure of the node source
        if (rmNodeData.getState().equals(NodeState.DOWN)) {
            downNodes.put(nodeUrl, node);
        } else {
            nodes.put(nodeUrl, node);
        }
        return recoveredRmNode;
    }

    /**
     * Acquires the existing node with specific url. The node have to be up and running.
     *
     * @param nodeUrl the url of the node
     * @param provider
     */
    public BooleanWrapper acquireNode(String nodeUrl, Client provider) {

        if (toShutdown) {
            throw new AddingNodesException("[" + name + "] node " + nodeUrl +
                                           " adding request discarded because node source is shutting down");
        }

        // checking that client has a right to change this node source
        // if the provider is the administrator of the node source it always has this permission
        provider.checkPermission(providerPermission,
                                 provider + " is not authorized to add node " + nodeUrl + " to " + name,
                                 new RMCoreAllPermission(),
                                 new NSAdminPermission());

        // lookup for a new Node
        int lookUpTimeout = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT.getValueAsInt();
        Node nodeToAdd = null;
        try {
            logger.info("Looking up the node " + nodeUrl + " with " + lookUpTimeout + " ms timeout");
            nodeToAdd = lookupNode(nodeUrl, lookUpTimeout);
            logger.info("The node " + nodeUrl + " has been successfully looked up");
        } catch (Exception e) {
            logger.warn("Cannot look up the node " + nodeUrl + " within " + lookUpTimeout + " ms due to " +
                        e.getMessage(), e);
            throw new AddingNodesException(e);
        }

        // node should be not null at this point...
        if (nodeToAdd == null) {
            throw new AddingNodesException("Cannot lookup node for unknown reason : " + nodeUrl);
        }

        // the node with specified url was successfully looked up
        // now checking if this node has been registered before in the node source
        if (downNodes.containsKey(nodeUrl)) {
            // it was registered but detected as down node,
            // so basically the node was restarted.
            // adding a new node and removing old one from the down list
            logger.debug("Removing existing node from down nodes list");
            BooleanWrapper result = rmcore.removeNodeFromCore(nodeUrl);
            if (result.getBooleanValue()) {
                if (logger.isDebugEnabled())
                    logger.debug("[" + name + "] successfully removed node " + nodeUrl + " from the core");
                // just removing it from down nodes list
                removeNode(nodeUrl, provider);
            }
        } else if (nodes.containsKey(nodeUrl)) {
            // adding a node which exists in node source

            Node existingNode = nodes.get(nodeUrl);

            if (nodeToAdd.equals(existingNode)) {
                // adding the same node twice
                // don't do anything
                if (logger.isDebugEnabled())
                    logger.debug("An attempt to add the same node twice " + nodeUrl + " - ignoring");
                return new BooleanWrapper(false);
            } else {
                // adding another node with the same url
                // replacing the old node by the new one
                logger.debug("Removing existing node from the RM without request propagation to the infrastructure manager");
                BooleanWrapper result = rmcore.removeNodeFromCore(nodeUrl);
                if (result.getBooleanValue()) {
                    if (logger.isDebugEnabled())
                        logger.debug("[" + name + "] successfully removed node " + nodeUrl + " from the core");
                    // removing it from the nodes list but don't propagate
                    // the request the the infrastructure because the restarted node will be killed
                    nodes.remove(nodeUrl);
                }
            }
        }

        // if any exception occurs in internalAddNode(node) do not add the node to the core
        RMDeployingNode deployingNode;
        try {
            deployingNode = internalAddNode(nodeToAdd);
        } catch (RMException e) {
            throw new AddingNodesException(e);
        }
        //we build the rmnode
        RMNode rmNode = buildRMNode(nodeToAdd, provider);

        if (deployingNode != null) {
            // inherit locking status from associated deploying node created before
            ((AbstractRMNode) rmNode).copyLockStatusFrom(deployingNode);
        }

        BooleanWrapper nodeAdded = rmcore.registerAvailableNode(rmNode);

        rmcore.internalRegisterConfiguringNode(rmNode);

        return nodeAdded;
    }

    /**
     * Builds a RMNode from a raw Node
     * @param node the node object
     * @param provider the client of the request
     * @return the expected RMNode
     */
    private RMNode buildRMNode(Node node, Client provider) {
        // creating a node access permission
        // it could be either PROVIDER/PROVIDER_GROUPS and in this case
        // the provider principals will be taken or
        // ME/MY_GROUPS (ns creator/ns creator groups) and in this case
        // creator's principals will be used
        Client permissionOwner = administrator;
        if (nodeUserAccessType.equals(AccessType.PROVIDER) || nodeUserAccessType.equals(AccessType.PROVIDER_GROUPS)) {
            permissionOwner = provider;
        }
        // now selecting the type (user or group) and construct the permission
        Set<IdentityPrincipal> principals = (Set<IdentityPrincipal>) nodeUserAccessType.getIdentityPrincipals(permissionOwner);

        boolean tokenInNode = false;
        boolean tokenInNodeSource = nodeUserAccessType.getTokens() != null && nodeUserAccessType.getTokens().length > 0;

        try {
            String nodeAccessToken = node.getProperty(RMNodeStarter.NODE_ACCESS_TOKEN);
            tokenInNode = nodeAccessToken != null && nodeAccessToken.length() > 0;

            if (tokenInNode) {
                logger.debug("Node " + node.getNodeInformation().getURL() + " is protected by access token " +
                             nodeAccessToken);
                // it overrides all other principals
                principals.clear();
                principals.add(new TokenPrincipal(nodeAccessToken));
            }
        } catch (Exception e) {
            throw new AddingNodesException(e);
        }

        PrincipalPermission nodeAccessPermission = new PrincipalPermission(node.getNodeInformation().getURL(),
                                                                           principals);
        RMNodeImpl rmnode = new RMNodeImpl(node, stub, provider, nodeAccessPermission);

        rmnode.setProtectedByToken(tokenInNode || tokenInNodeSource);
        return rmnode;
    }

    /**
     * Rebuild a RMNode from a node that could be looked up again after a
     * recovery of the RM. This builder configures nothing for the node
     * because it is configured already as it suppoesed to be recovered from
     * the database.
     * @return the expected RMNode
     */
    private RMNode buildRMNodeAfterRecovery(Node node, RMNodeData rmNodeData) {
        RMNodeImpl rmNode = new RMNodeImpl(node,
                                           stub,
                                           rmNodeData.getName(),
                                           rmNodeData.getNodeUrl(),
                                           rmNodeData.getProvider(),
                                           rmNodeData.getHostname(),
                                           rmNodeData.getJmxUrls(),
                                           rmNodeData.getJvmName(),
                                           rmNodeData.getUserPermission(),
                                           rmNodeData.getState(),
                                           rmNodeData.getTags());
        if (rmNodeData.getState().equals(NodeState.BUSY)) {
            logger.info("Node " + rmNodeData.getName() + " was found busy after scheduler recovery with owner " +
                        rmNodeData.getOwner());
            rmNode.setBusy(rmNodeData.getOwner(), rmNodeData.getUsageInfo());
        }
        return rmNode;
    }

    public boolean setNodeAvailable(RMNode node) {
        Node proactiveProgrammingNode = node.getNode();
        String proactiveProgrammingNodeUrl = proactiveProgrammingNode.getNodeInformation().getURL();
        Node downNode = downNodes.remove(proactiveProgrammingNodeUrl);

        if (downNode != null) {
            logger.info("Setting node as available: " + proactiveProgrammingNodeUrl);
            nodes.put(proactiveProgrammingNodeUrl, proactiveProgrammingNode);
            infrastructureManager.onDownNodeReconnection(proactiveProgrammingNode);

            return true;
        } else {
            logger.info("Node state not changed since it is unknown: " + proactiveProgrammingNodeUrl);
            return false;
        }
    }

    public RMDeployingNode update(RMDeployingNode rmNode) {
        return infrastructureManager.update(rmNode);
    }

    public boolean setDeploying(RMDeployingNode deployingNode) {
        return rmcore.setDeploying(deployingNode);
    }

    public void setInfrastructureManager(InfrastructureManager infrastructureManager) {
        this.infrastructureManager = infrastructureManager;
    }

    public NodeSourcePolicy getPolicy() {
        return this.policy;
    }

    public void setActivePolicy(NodeSourcePolicy policy) {
        this.activePolicy = policy;
    }

    public NodeSourceStatus getStatus() {
        return this.descriptor.getStatus();
    }

    public void setStatus(NodeSourceStatus status) {
        this.getDescriptor().setStatus(status);
        this.infrastructureManager.setPersistedNodeSourceData(NodeSourceData.fromNodeSourceDescriptor(this.descriptor));
    }

    public LinkedHashMap<String, String> getAdditionalInformation() {
        return additionalInformation;
    }

    public void putAndPersistAdditionalInformation(String key, String value) {
        String valueToUpdate = this.additionalInformation.get(key);
        if (valueToUpdate == null || (valueToUpdate != null && !valueToUpdate.equals(value))) {

            // Put additional information
            this.additionalInformation.put(key, value);
            this.descriptor.getAdditionalInformation().put(key, value);

            // Persist additional information
            persistAdditionalInformation();

            // Notify the rm portal that the node source changed
            this.monitoring.nodeSourceEvent(new RMNodeSourceEvent(RMEventType.NODESOURCE_UPDATED,
                                                                  this.administrator.getName(),
                                                                  this.name,
                                                                  this.getDescription(),
                                                                  this.additionalInformation,
                                                                  this.administrator.getName(),
                                                                  this.getStatus().toString(),
                                                                  this.getDescriptor().getInfrastructureType(),
                                                                  this.getDescriptor().getPolicyType(),
                                                                  this.nodeUserAccessType.getTokens(),
                                                                  this.administrator.getTenant()));
        }
    }

    private void persistAdditionalInformation() {
        // Update nodeSourceData data from DB
        if (this.dbManager == null) {
            this.dbManager = RMDBManager.getInstance();
        }
        if (this.nodeSourceData == null) {
            this.nodeSourceData = this.dbManager.getNodeSource(this.name);
        }

        if (nodeSourceData != null) {
            this.nodeSourceData.setAdditionalInformation(this.additionalInformation);
            this.dbManager.updateNodeSource(this.nodeSourceData);
        } else {
            logger.warn("Node source " + this.name + " is unknown. Cannot persist infrastructure variables");
        }
    }

    public NodeSourceDescriptor updateDynamicParameters(List<Serializable> infrastructureParamsWithDynamicUpdated,
            List<Serializable> policyParamsWithDynamicUpdated) {

        this.descriptor.setInfrastructureParameters(infrastructureParamsWithDynamicUpdated);
        this.descriptor.setPolicyParameters(policyParamsWithDynamicUpdated);

        this.infrastructureManager.setPersistedNodeSourceData(NodeSourceData.fromNodeSourceDescriptor(this.descriptor));

        return this.descriptor;
    }

    public void reconfigure(Object[] updatedInfrastructureParams, Object[] updatedPolicyParams) throws Exception {
        this.infrastructureManager.reconfigure(updatedInfrastructureParams);
        this.activePolicy.reconfigure(updatedPolicyParams);
    }

    /**
     * Looks up the node
     */
    private class NodeLocator implements Callable<Node> {
        private String nodeUrl;

        public NodeLocator(String url) {
            nodeUrl = url;
        }

        public Node call() throws Exception {
            Node node = NodeFactory.getNode(nodeUrl);
            return node;
        }
    }

    /**
     * Lookups a node with specified timeout.
     *
     * @param nodeUrl a url of the node
     * @param timeout to wait in ms
     * @return node if it was successfully obtained, null otherwise
     * @throws Exception if node was not looked up
     */
    public Node lookupNode(String nodeUrl, long timeout) throws Exception {
        Future<Node> futureNode = threadPoolHolder.submit(INTERNAL_POOL, new NodeLocator(nodeUrl));
        return futureNode.get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Requests one node to be acquired from the underlying infrastructure.
     */
    public void acquireNode() {

        if (toShutdown) {
            logger.warn("[" + name + "] acquireNode request discarded because node source is shutting down");
            return;
        }

        infrastructureManager.acquireNode();
    }

    public void acquireNodes(int n, long timeout, Map<String, ?> nodeConfiguration) {
        if (toShutdown) {
            logger.warn("[" + name + "] acquireNodes request discarded because node source is shutting down");
            return;
        }

        infrastructureManager.acquireNodes(n, timeout, nodeConfiguration);
    }

    public void acquireNodes(int n, Map<String, ?> nodeConfiguration) {
        if (toShutdown) {
            logger.warn("[" + name + "] acquireNodes request discarded because node source is shutting down");
            return;
        }

        infrastructureManager.acquireNodes(n, nodeConfiguration);
    }

    /**
     * Requests all nodes to be acquired from the infrastructure.
     */
    public void acquireAllNodes() {

        if (toShutdown) {
            logger.warn("[" + name + "] acquireAllNodes request discarded because node source is shutting down");
            return;
        }

        infrastructureManager.acquireAllNodes();
    }

    public void acquireAllNodes(Map<String, ?> nodeConfiguration) {
        if (toShutdown) {
            logger.warn("[" + name + "] acquireAllNodes request discarded because node source is shutting down");
            return;
        }

        infrastructureManager.acquireAllNodes(nodeConfiguration);
    }

    /**
     * Removes the node from the node source.
     *
     * @param nodeUrl the url of the node to be released
     */
    public BooleanWrapper removeNode(String nodeUrl, Client initiator) {
        Node node = null;
        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            logger.info("[" + this.name + "] removing node: " + nodeUrl);
            node = this.nodes.remove(nodeUrl);
        } else if (this.downNodes.containsKey(nodeUrl)) {
            logger.info("[" + this.name + "] removing down node: " + nodeUrl);
            node = this.downNodes.remove(nodeUrl);
        }

        if (node == null) {
            logger.error("[" + this.name + "] cannot remove node: " + nodeUrl + " because it is unknown");
            return new BooleanWrapper(false);
        } else {
            this.removedNodes.put(nodeUrl, node);
            RMCore.topologyManager.removeNode(node);
            try {
                this.infrastructureManager.internalRemoveNode(node);
            } catch (RMException e) {
                logger.error(e.getCause().getMessage(), e);
            }
            return new BooleanWrapper(true);
        }
    }

    /**
     * Shutdowns the node source and releases all its nodes.
     */
    public BooleanWrapper shutdown(Client initiator) {
        logger.info("[" + this.name + "] is shutting down by " + initiator);
        this.toShutdown = true;

        if (this.nodes.size() == 0) {
            this.shutdownNodeSourceServices(initiator);
        } else {
            logger.warn("[" + this.name + "] actual shutdown is skipped, because there are still some alive nodes: " +
                        this.nodes);
        }
        return new BooleanWrapper(true);
    }

    /**
     * To emit a deploying node event
     * @param event the deploying node event to emit
     */
    @ImmediateService
    public void internalEmitDeployingNodeEvent(final RMNodeEvent event) {
        this.monitoring.nodeEvent(event);
    }

    /**
     * Removes the deploying node from the nodesource's infrastructure manager.
     * @param pnUrl the deploying url
     * @return true in case of succes, false otherwise
     */
    public boolean removeDeployingNode(String pnUrl) {
        return this.infrastructureManager.internalRemoveDeployingNode(pnUrl);
    }

    /**
     * Gets the ping frequency.
     * @return ping frequency
     */
    public IntWrapper getPingFrequency() {
        return new IntWrapper(pingFrequency);
    }

    /**
     * Sets the ping frequency (in ms)
     * @param frequency new value of monitoring period
     */
    public void setPingFrequency(int frequency) {
        pingFrequency = frequency;
    }

    /**
     * Creates a node source string representation
     * @return string representation of the node source
     */
    @ImmediateService
    public String getDescription() {
        return "Infrastructure: " + this.infrastructureManager.toString() + ", Policy: " + this.policy.toString();
    }

    /**
     * Gets name of the node source
     * @return name of the node source
     */
    @ImmediateService
    public String getName() {
        return name;
    }

    /**
     * Get the information that describe the node source and that are needed
     * to instantiate it
     * @return the node source descriptor
     */
    @ImmediateService
    public NodeSourceDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * @return whether nodes recovery is activated for this node source
     */
    @ImmediateService
    public boolean nodesRecoverable() {
        return descriptor.nodesRecoverable();
    }

    /**
     * Activates a node source policy.
     */
    public BooleanWrapper activate() {
        logger.info("[" + this.name + "] Activating the policy " + this.activePolicy);
        return this.activePolicy.activate();
    }

    /**
     * Initiates node source services shutdown, such as pinger, policy, thread pool.
     */
    protected void shutdownNodeSourceServices(Client initiator) {
        logger.info("[" + this.name + "] Shutdown finalization");

        this.activePolicy.shutdown(initiator);
        this.infrastructureManager.internalShutDown();
        this.finishNodeSourceShutdown(initiator);
    }

    /**
     * Terminates a node source active object when the policy is shutdown. 
     */
    public void finishNodeSourceShutdown(Client initiator) {
        this.rmcore.disconnect(Client.getId(PAActiveObject.getStubOnThis()));
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * Retrieves a list of alive nodes
     * @return a list of alive nodes
     */
    @ImmediateService
    public LinkedList<Node> getAliveNodes() {
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(this.nodes.values());
        return nodes;
    }

    /**
     * Retrieves a list of down nodes
     * @return a list of down nodes
     */
    @ImmediateService
    public LinkedList<Node> getDownNodes() {
        LinkedList<Node> downNodes = new LinkedList<>();
        downNodes.addAll(this.downNodes.values());
        return downNodes;
    }

    /**
     * Retrieves the list of deploying nodes handled by the infrastructure manager
     * @return the list of deploying nodes handled by the infrastructure manager
     */
    @ImmediateService
    public LinkedList<RMDeployingNode> getDeployingAndLostNodes() {
        LinkedList<RMDeployingNode> result = new LinkedList<>();
        result.addAll(this.infrastructureManager.getDeployingAndLostNodes());
        return result;
    }

    /**
     * Returns the deploying node identified by the specified {@code nodeUrl}.
     *
     * @param nodeUrl the URL of the deploying node to lookup.
     * @return the deploying node found, or {@code null}. Since a node source
     * is an Active Object, the caller will receive a deep copy of the original object.
     */
    @ImmediateService
    public RMDeployingNode getNodeInDeployingOrLostNodes(String nodeUrl) {
        return infrastructureManager.getDeployingOrLostNode(nodeUrl);
    }

    /**
     * Gets the nodes size excluding down nodes.
     * @return the node size
     */
    @ImmediateService
    public int getNodesCount() {
        return this.nodes.values().size();
    }

    /**
     * Marks node as down. Remove it from node source node set. It remains in rmcore nodes list until
     * user decides to remove them or node source is shutdown.
     * @see NodeSource#detectedPingedDownNode(String, String)
     */
    public void detectedPingedDownNode(String nodeName, String nodeUrl) {

        if (toShutdown) {
            logger.warn("[" + name + "] detectedPingedDownNode request discarded because node source is shutting down");
            return;
        }

        Node downNode = nodes.remove(nodeUrl);
        if (downNode != null) {
            logger.warn("[" + name + "] Detected down node: " + nodeUrl);
            downNodes.put(nodeUrl, downNode);
            try {
                RMCore.topologyManager.removeNode(downNode);
                infrastructureManager.internalNotifyDownNode(nodeName, nodeUrl, downNode);
            } catch (RMException e) {
                logger.error("Error while removing down node: " + nodeUrl, e);
            }
        } else if (removedNodes.containsKey(nodeUrl)) {
            logger.info("[" + name + "] Detected down node has already been removed: " + nodeUrl);
            // node has been removed, ping detection is ignored
        } else {
            logger.warn("[" + name + "] Detected down node: " + nodeUrl);
            // the node could not be found in the nodes map so we are trying
            // here to restore the nodes after a recovery of the RM: we have
            // almost no information about the node apart from its name and url
            try {
                infrastructureManager.internalNotifyDownNode(nodeName, nodeUrl, null);
            } catch (RMException e) {
                logger.error("New empty node " + nodeUrl + " could not be created to handle down node", e);
            }
        }

        rmcore.setDownNode(nodeUrl);
    }

    /**
     * Gets resource manager core. Used by policies.
     * @return {@link RMCore} instance.
     */
    @ImmediateService
    public RMCore getRMCore() {
        return rmcore;
    }

    /**
     * Executed command in parallel using thread pool.
     * @param task to execute
     */
    @ImmediateService
    public void executeInParallel(Runnable task) {
        NodeSource.threadPoolHolder.execute(EXTERNAL_POOL, task);
    }

    /**
     * Pings the node with specified url.
     * If the node is dead sends the request to the node source.
     */
    public void pingNode(final Node node) {
        NodeSource.threadPoolHolder.execute(PINGER_POOL, () -> {
            String nodeName = node.getNodeInformation().getName();
            String nodeUrl = node.getNodeInformation().getURL();

            try {
                node.getNumberOfActiveObjects();
                if (logger.isDebugEnabled()) {
                    logger.debug("Node " + nodeUrl + " is alive");
                }
            } catch (Throwable t) {
                if (!this.toShutdown) {
                    logger.warn("Error occurred when trying to ping node " + nodeUrl, t);
                    try {
                        stub.detectedPingedDownNode(nodeName, nodeUrl);
                    } catch (Exception e) {
                        logger.warn("Could not send detectedPingedDownNode message", e);
                    }
                }
            }
        });
    }

    /**
     * The provider of the node source is the resource manager client initiated
     * the node source creation.
     *
     * @return the node source provider
     */
    @ImmediateService
    public Client getAdministrator() {
        return administrator;
    }

    /**
     * Returns the the node source stub
     */
    public NodeSource getStub() {
        return stub;
    }

    /**
     * Returns the permission which administrator must have.
     * Administrator of the node source can remove it, add nodes to this node source and remove any node.
     */
    @ImmediateService
    public Permission getAdminPermission() {
        return adminPermission;
    }

    /**
     * Returns the permission required to add/remove nodes to/from the node source.
     * Provider can remove only its one nodes.
     */
    @ImmediateService
    public Permission getProviderPermission() {
        return providerPermission;
    }

    /**
     * Returns the registration url the node spawn by this nodesource
     * must use.
     * @return the registration url the node spawn by this nodesource
     * must use.
     */
    @ImmediateService
    public String getRegistrationURL() {
        return this.registrationURL;
    }

    @ImmediateService
    public RMNodeSourceEvent createNodeSourceEvent() {
        return new RMNodeSourceEvent(this.name,
                                     getDescription(),
                                     this.additionalInformation,
                                     this.administrator.getName(),
                                     this.getStatus().toString(),
                                     this.descriptor.getInfrastructureType(),
                                     this.descriptor.getPolicyType(),
                                     this.nodeUserAccessType.getTokens(),
                                     this.administrator.getTenant());
    }
}
