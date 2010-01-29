/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.nodesource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.network.NetworkCommunicator;
import org.ow2.proactive.network.NetworkCommunicatorImpl;
import org.ow2.proactive.network.Timed;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


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
public class NodeSource implements InitActive, RunActive {

    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);
    private static final int NODE_LOOKUP_TIMEOUT = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT
            .getValueAsInt();
    private int pingFrequency = PAResourceManagerProperties.RM_NODE_SOURCE_PING_FREQUENCY.getValueAsInt();

    /** Default name */
    public static final String GCM_LOCAL = "GCMLocalNodes";
    public static final String DEFAULT = "Default";

    /** unique name of the source */
    private String name;

    private InfrastructureManager infrastructureManager;
    private NodeSourcePolicy nodeSourcePolicy;
    private String description;
    private RMCore rmcore;
    private boolean toShutdown = false;

    // all nodes except down
    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    private HashMap<String, Node> downNodes = new HashMap<String, Node>();

    private static transient NetworkCommunicator networkCommunicator;
    private NodeSource stub;

    /**
     * Creates a new instance of NodeSource.
     * This constructor is used by Proactive as one of requirements for active objects.
     */
    public NodeSource() {
    }

    /**
     * Creates a new instance of NodeSource.
     *
     * @param name node source name
     * @param im underlying infrastructure manager
     * @param policy nodes acquisition policy
     * @param rmcore resource manager core
     */
    public NodeSource(String name, InfrastructureManager im, NodeSourcePolicy policy, RMCore rmcore) {
        this.name = name;
        infrastructureManager = im;
        nodeSourcePolicy = policy;
        this.rmcore = rmcore;
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

        try {
            getNetworkCommunicator();

            // description could be requested when the policy does not exist anymore
            // so initializing it here
            description = "Infrastructure:" + infrastructureManager + ", Policy: " + nodeSourcePolicy;

            // these methods are called from the rm core
            // mark them as immediate services in order to prevent the block of the core
            PAActiveObject.setImmediateService("getName");
            PAActiveObject.setImmediateService("executeInParallel");
            PAActiveObject.setImmediateService("getDescription");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        long timeStamp = System.currentTimeMillis();
        long delta = 0;

        // recalculating nodes number only once per policy period
        while (body.isActive()) {

            service.blockingServeOldest(pingFrequency);

            delta += System.currentTimeMillis() - timeStamp;
            timeStamp = System.currentTimeMillis();

            if (delta > pingFrequency) {
                logger.info("[" + name + "] Pinging alive nodes");
                for (Node node : getAliveNodes()) {
                    pingNode(node.getNodeInformation().getURL());
                }
                delta = 0;
            }
        }

    }

    /**
     * Updates internal node source structures.
     */
    private void internalAddNode(Node node) throws RMException {
        String nodeUrl = node.getNodeInformation().getURL();
        if (this.nodes.containsKey(nodeUrl)) {
            throw new RMException("The node " + nodeUrl + " already added to the node source " + name);
        }

        logger.info("[" + name + "] new node available : " + node.getNodeInformation().getURL());
        infrastructureManager.registerAcquiredNode(node);
        nodes.put(nodeUrl, node);
    }

    /**
     * Acquires the existing node with specific url. The node have to be up and running.
     *
     * @param nodeUrl the url of the node
     */
    public BooleanWrapper acquireNode(String nodeUrl) {

        if (toShutdown) {
            throw new AddingNodesException("[" + name +
                "] addNode request discarded because node source is shutting down");
        }

        // lookup for a new Node
        Node nodeToAdd = null;
        try {
            nodeToAdd = lookupNode(nodeUrl, NODE_LOOKUP_TIMEOUT);
        } catch (Exception e) {
            throw new AddingNodesException(e);
        }

        // cannot lookup node
        if (nodeToAdd == null) {
            throw new AddingNodesException("Cannot lookup node " + nodeUrl);
        }

        // the node with specified url was successfully looked up
        // now checking if this node has been registered before in the node source
        if (downNodes.containsKey(nodeUrl)) {
            // it was registered but detected as down node,
            // so basically the node was restarted.
            // adding a new node and removing old one from the down list
            logger.debug("Removing existing node from down nodes list");
            BooleanWrapper result = rmcore.internalRemoveNodeFromCore(nodeUrl);
            if (result.booleanValue()) {
                logger.debug("[" + name + "] successfully removed node " + nodeUrl + " from the core");
                // just removing it from down nodes list
                removeNode(nodeUrl);
            }
        } else if (nodes.containsKey(nodeUrl)) {
            // adding a node which exists in node source

            Node existingNode = nodes.get(nodeUrl);

            if (nodeToAdd.equals(existingNode)) {
                // adding the same node twice
                // don't do anything
                logger.debug("An attempt to add the same node twice " + nodeUrl + " - ignoring");
                return new BooleanWrapper(false);
            } else {
                // adding another node with the same url
                // replacing the old node by the new one
                logger
                        .debug("Removing existing node from the RM without request propagation to the infrastructure manager");
                BooleanWrapper result = rmcore.internalRemoveNodeFromCore(nodeUrl);
                if (result.booleanValue()) {
                    logger.debug("[" + name + "] successfully removed node " + nodeUrl + " from the core");
                    // removing it from the nodes list but don't propagate
                    // the request the the infrastructure because the restarted node will be killed
                    nodes.remove(nodeUrl);
                }
            }
        }

        // if any exception occurs in internalAddNode(node) do not add the node to the core
        try {
            internalAddNode(nodeToAdd);
        } catch (RMException e) {
            throw new AddingNodesException(e);
        }

        configureForDataSpace(nodeToAdd);
        RMNode rmnode = new RMNodeImpl(nodeToAdd, "noVn", (NodeSource) PAActiveObject.getStubOnThis());
        rmcore.internalAddNodeToCore(rmnode);

        return new BooleanWrapper(true);
    }

    /**
     * Configure node for dataSpaces
     *
     * @param node the node to be configured
     */
    private void configureForDataSpace(Node node) {
        try {
            DataSpaceNodeConfigurationAgent conf = (DataSpaceNodeConfigurationAgent) PAActiveObject
                    .newActive(DataSpaceNodeConfigurationAgent.class.getName(), null, node);
            conf.configureNode();
        } catch (Throwable t) {
            logger.warn("Cannot configure dataSpaces", t);
        }
    }

    /**
     * Close dataSpaces node configuration
     *
     * @param node the node to be unconfigured
     */
    private void closeDataSpaceConfiguration(Node node) {
        try {
            DataSpaceNodeConfigurationAgent conf = (DataSpaceNodeConfigurationAgent) PAActiveObject
                    .newActive(DataSpaceNodeConfigurationAgent.class.getName(), null, node);
            conf.closeNodeConfiguration();
        } catch (Throwable t) {
            logger.warn("Cannot close dataSpaces configuration", t);
        }
    }

    /**
     * Dedicated thread for nodes lookup
     */
    private class NodeLocator implements Timed<Node> {
        private Node result = null;
        private boolean isDone = false;
        private String nodeUrl;

        public NodeLocator(String url) {
            nodeUrl = url;
        }

        public void run() {
            try {
                Node node = NodeFactory.getNode(nodeUrl);

                synchronized (this) {
                    result = node;
                    isDone = true;
                }
            } catch (NodeException e) {
                logger.warn("", e);
            }
        }

        public Node getResult() {
            return result;
        }

        public boolean isDone() {
            return isDone;
        }

        public void timeoutAction() {
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
        logger.debug("Looking up for the node " + nodeUrl + " with " + timeout + " ms timeout");
        Collection<NodeLocator> locator = Collections.singletonList(new NodeLocator(nodeUrl));
        Collection<Node> nodes = getNetworkCommunicator().execute(locator, timeout);
        if (nodes.size() > 0) {
            return nodes.iterator().next();
        }
        return null;
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

    /**
     * Requests all nodes to be acquired from the infrastructure.
     */
    public void acquireAllNodes() {

        if (toShutdown) {
            logger.warn("[" + name +
                "] acquireAllNodes request discarded because node source is shutting down");
            return;
        }

        infrastructureManager.acquireAllNodes();
    }

    /**
     * Removes the node from the node source.
     *
     * @param nodeUrl the url of the node to be released
     */
    public BooleanWrapper removeNode(String nodeUrl) {

        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            logger.info("[" + name + "] removing node : " + nodeUrl);
            Node node = nodes.remove(nodeUrl);
            try {
                closeDataSpaceConfiguration(node);
                infrastructureManager.removeNode(node);
            } catch (RMException e) {
                logger.error(e.getCause().getMessage());
            }
        } else {
            Node downNode = downNodes.remove(nodeUrl);
            if (downNode != null) {
                logger.info("[" + name + "] removing down node : " + nodeUrl);
            } else {
                logger.error("[" + name + "] removing node : " + nodeUrl +
                    " which not belongs to this node source");
                return new BooleanWrapper(false);
            }
        }

        if (toShutdown && nodes.size() == 0) {
            // shutdown all pending nodes
            shutdownNodeSourceServices();
        }

        return new BooleanWrapper(true);
    }

    /**
     * Shutdowns the node source and releases all its nodes.
     */
    public void shutdown() {
        logger.info("[" + name + "] removal request");
        toShutdown = true;

        if (nodes.size() == 0) {
            shutdownNodeSourceServices();
        }
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
    public String getDescription() {
        return description;
    }

    /**
     * Gets name of the node source
     * @return name of the node source
     */
    public String getName() {
        return name;
    }

    /**
     * Activates a node source policy.
     */
    public void activate() {
        logger.info("[" + name + "] Activating the policy " + nodeSourcePolicy);
        nodeSourcePolicy.activate();
    }

    /**
     * Initiates node source services shutdown, such as pinger, policy, thread pool.
     */
    protected void shutdownNodeSourceServices() {
        logger.info("[" + name + "] Shutdown finalization");

        nodeSourcePolicy.shutdown();
        infrastructureManager.shutDown();
    }

    /**
     * Terminates a node source active object when shutdown confirmation is received from the pinger and the policy. 
     */
    public void finishNodeSourceShutdown() {
        PAFuture.waitFor(rmcore.nodeSourceUnregister(name, new RMNodeSourceEvent(this,
            RMEventType.NODESOURCE_REMOVED)));

        // got confirmation from pinger and policy
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * Retrieves a list of alive nodes
     * @return a list of alive nodes
     */
    public LinkedList<Node> getAliveNodes() {
        LinkedList<Node> nodes = new LinkedList<Node>();
        nodes.addAll(this.nodes.values());
        return nodes;
    }

    /**
     * Retrieves a list of down nodes
     * @return a list of down nodes
     */
    public LinkedList<Node> getDownNodes() {
        LinkedList<Node> downNodes = new LinkedList<Node>();
        downNodes.addAll(this.downNodes.values());
        return downNodes;
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
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#detectedPingedDownNode(java.lang.String)
     */
    public void detectedPingedDownNode(String nodeUrl) {

        if (toShutdown) {
            logger.warn("[" + name +
                "] detectedPingedDownNode request discarded because node source is shutting down");
            return;
        }

        logger.info("[" + name + "] Detected down node " + nodeUrl);
        Node downNode = nodes.remove(nodeUrl);
        if (downNode != null) {
            downNodes.put(nodeUrl, downNode);
        }
        rmcore.setDownNode(nodeUrl);
    }

    /**
     * Gets resource manager core. Used by policies.
     * @return {@link RMCoreInterface}
     */
    public RMCoreInterface getRMCore() {
        return rmcore;
    }

    /**
     * Executed command in parallel using thread pool
     * @param command to execute
     */
    public void executeInParallel(Runnable command) {
        getNetworkCommunicator().execute(command);
    }

    /**
     * Instantiates the network communicator if it is null.
     */
    private synchronized static NetworkCommunicator getNetworkCommunicator() {
        if (networkCommunicator == null) {
            networkCommunicator = new NetworkCommunicatorImpl(
                PAResourceManagerProperties.RM_NODESOURCE_MAX_THREAD_NUMBER.getValueAsInt());
        }

        return networkCommunicator;
    }

    /**
     * Pings the node with specified url.
     * If the node is dead sends the request to the node source.
     */
    public void pingNode(final String url) {
        executeInParallel(new Runnable() {
            public void run() {
                try {
                    Node node = NodeFactory.getNode(url);
                    node.getNumberOfActiveObjects();
                    logger.debug("Node " + url + " is alive");
                } catch (Throwable t) {
                    stub.detectedPingedDownNode(url);
                }
            }
        });
    }
}
