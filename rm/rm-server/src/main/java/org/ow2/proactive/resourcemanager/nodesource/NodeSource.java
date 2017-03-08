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

import java.security.Permission;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.principals.IdentityPrincipal;
import org.ow2.proactive.authentication.principals.TokenPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
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

    /** Default name for NS with local nodes started with the Scheduler by default */
    public static final String DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME = "LocalNodes";

    public static final String DEFAULT = "Default";

    public static final int INTERNAL_POOL = 0;

    public static final int EXTERNAL_POOL = 1;

    /** unique name of the source */
    private final String name;

    private final InfrastructureManager infrastructureManager;

    private final NodeSourcePolicy nodeSourcePolicy;

    private final String description;

    private final RMCore rmcore;

    // The url used by spawn nodes to register themself
    private final String registrationURL;

    private boolean toShutdown = false;

    // all nodes except down
    private Map<String, Node> nodes;

    private Map<String, Node> downNodes;

    private static ThreadPoolHolder threadPoolHolder;

    private NodeSource stub;

    private final Client administrator;

    // to be able to emit rmdeployingnode related events
    private final transient RMMonitoringImpl monitoring;

    // admin can remove node source, add nodes to the node source, remove any node
    // it is a PrincipalPermission of the user who created this node source
    private final Permission adminPermission;

    // provider can add nodes to the node source, remove only its nodes
    // level is configured by ns admin at the moment of ns creation
    // NOTE: the administrator is always the provider because each provider is one of those:
    // ns creator, ns creator groups, all
    private final Permission providerPermission;

    // user can get nodes for running computations
    // level is configured by ns admin at the moment of ns creation
    private AccessType nodeUserAccessType;

    static {
        try {
            int maxThreads = PAResourceManagerProperties.RM_NODESOURCE_MAX_THREAD_NUMBER.getValueAsInt();
            if (maxThreads < 2) {
                maxThreads = 2;
            }

            // executor service initialization
            NodeSource.threadPoolHolder = new ThreadPoolHolder(new int[] { maxThreads / 2, maxThreads / 2 });
        } catch (Exception e) {
            logger.error("Could not initialize threadPoolHolder", e);
        }
    }

    /**
     * Creates a new instance of NodeSource.
     * This constructor is used by Proactive as one of requirements for active objects.
     */
    public NodeSource() {
        registrationURL = null;
        name = null;
        infrastructureManager = null;
        nodeSourcePolicy = null;
        description = null;
        rmcore = null;
        administrator = null;
        adminPermission = null;
        providerPermission = null;
        monitoring = null;
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
    public NodeSource(String registrationURL, String name, Client provider, InfrastructureManager im,
            NodeSourcePolicy policy, RMCore rmcore, RMMonitoringImpl monitor) {
        this.registrationURL = registrationURL;
        this.name = name;
        this.administrator = provider;
        this.infrastructureManager = im;
        this.nodeSourcePolicy = policy;
        this.rmcore = rmcore;
        this.monitoring = monitor;
        this.description = "Infrastructure: " + im + ", Policy: " + policy;

        this.nodes = Collections.synchronizedMap(new HashMap<String, Node>());
        this.downNodes = Collections.synchronizedMap(new HashMap<String, Node>());

        // node source admin permission
        // it's the PrincipalPermission of the user who created the node source
        this.adminPermission = new PrincipalPermission(provider.getName(),
                                                       provider.getSubject().getPrincipals(UserNamePrincipal.class));
        // creating node source provider permission
        // could be one of the following: PrincipalPermission (NS creator) or PrincipalPermission (NS creator groups)
        // or PrincipalPermission (anyone)
        this.providerPermission = new PrincipalPermission(provider.getName(),
                                                          nodeSourcePolicy.getProviderAccessType()
                                                                          .getIdentityPrincipals(provider));
        this.nodeUserAccessType = nodeSourcePolicy.getUserAccessType();
    }

    /**
     * Initialization of node source. Creates and activates a pinger to monitor nodes.
     *
     * @param body active object body
     */
    public void initActivity(Body body) {

        stub = (NodeSource) PAActiveObject.getStubOnThis();
        infrastructureManager.setNodeSource(this);
        nodeSourcePolicy.setNodeSource((NodeSource) PAActiveObject.getStubOnThis());

        Thread.currentThread().setName("Node Source \"" + name + "\"");
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
        return rmDeployingNode;
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
                                 provider + " is not authorized to add node " + nodeUrl + " to " + name);

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

        //we notify the configuration of the node to the rmcore
        //it then will be seen as "configuring"
        rmcore.internalRegisterConfiguringNode(rmNode);

        return new BooleanWrapper(true);
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
    private Node lookupNode(String nodeUrl, long timeout) throws Exception {
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

    public void acquireNodes(int n, Map<String, ?> nodeConfiguration) {
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
        infrastructureManager.acquireAllNodes(nodeConfiguration);
    }

    /**
     * Removes the node from the node source.
     *
     * @param nodeUrl the url of the node to be released
     */
    public BooleanWrapper removeNode(String nodeUrl, Client initiator) {

        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            logger.info("[" + name + "] removing node : " + nodeUrl);
            Node node = nodes.remove(nodeUrl);
            RMCore.topologyManager.removeNode(node);
            try {
                infrastructureManager.internalRemoveNode(node, false);
            } catch (RMException e) {
                logger.error(e.getCause().getMessage(), e);
            }
        } else {
            Node downNode = downNodes.remove(nodeUrl);
            if (downNode != null) {
                logger.info("[" + name + "] removing down node : " + nodeUrl);
            } else {
                logger.error("[" + name + "] removing node : " + nodeUrl + " which not belongs to this node source");
                return new BooleanWrapper(false);
            }
        }

        if (toShutdown && nodes.size() == 0) {
            // shutdown all pending nodes
            shutdownNodeSourceServices(initiator);
        }

        return new BooleanWrapper(true);
    }

    /**
     * Shutdowns the node source and releases all its nodes.
     */
    public void shutdown(Client initiator) {
        logger.info("[" + name + "] is shutting down by " + initiator);
        toShutdown = true;

        if (nodes.size() == 0) {
            shutdownNodeSourceServices(initiator);
        }
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
        return description;
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
     * Activates a node source policy.
     */
    public BooleanWrapper activate() {
        logger.info("[" + name + "] Activating the policy " + nodeSourcePolicy);
        return nodeSourcePolicy.activate();
    }

    /**
     * Initiates node source services shutdown, such as pinger, policy, thread pool.
     * @param initiator
     */
    protected void shutdownNodeSourceServices(Client initiator) {
        logger.info("[" + name + "] Shutdown finalization");

        nodeSourcePolicy.shutdown(initiator);
        infrastructureManager.internalShutDown();
    }

    /**
     * Terminates a node source active object when the policy is shutdown. 
     */
    public void finishNodeSourceShutdown(Client initiator) {
        PAFuture.waitFor(rmcore.nodeSourceUnregister(name,
                                                     new RMNodeSourceEvent(RMEventType.NODESOURCE_REMOVED,
                                                                           initiator.getName(),
                                                                           this.getName(),
                                                                           this.getDescription(),
                                                                           this.getAdministrator().getName())));

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
    public LinkedList<RMDeployingNode> getDeployingNodes() {
        LinkedList<RMDeployingNode> result = new LinkedList<>();
        result.addAll(this.infrastructureManager.getDeployingNodes());
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
    public RMDeployingNode getDeployingNode(String nodeUrl) {
        return infrastructureManager.getDeployingNode(nodeUrl);
    }

    /**
     * Gets the nodes size excluding down nodes.
     * @return the node size
     */
    public int getNodesCount() {
        return this.nodes.values().size();
    }

    /**
     * Marks node as down. Remove it from node source node set. It remains in rmcore nodes list until
     * user decides to remove them or node source is shutdown.
     * @see NodeSource#detectedPingedDownNode(String)
     */
    public void detectedPingedDownNode(String nodeUrl) {

        if (toShutdown) {
            logger.warn("[" + name + "] detectedPingedDownNode request discarded because node source is shutting down");
            return;
        }

        logger.info("[" + name + "] Detected down node: " + nodeUrl);
        Node downNode = nodes.remove(nodeUrl);
        if (downNode != null) {
            downNodes.put(nodeUrl, downNode);
            try {
                RMCore.topologyManager.removeNode(downNode);
                infrastructureManager.internalRemoveNode(downNode, true);
            } catch (RMException e) {
                logger.error("Error while removing down node: " + nodeUrl, e);
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
        executeInParallel(new Runnable() {
            public void run() {
                String nodeUrl = node.getNodeInformation().getURL();

                try {
                    node.getNumberOfActiveObjects();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Node " + nodeUrl + " is alive");
                    }
                } catch (Throwable t) {
                    stub.detectedPingedDownNode(nodeUrl);
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
}
