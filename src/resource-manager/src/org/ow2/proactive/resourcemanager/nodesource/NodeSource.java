/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.nodesource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.utils.Pinger;
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
public class NodeSource implements InitActive {

    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);
    private static final int NODE_LOOKUP_TIMEOUT = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT
            .getValueAsInt();

    /** Default name */
    public static final String DEFAULT_NAME = "Default";

    /** unique name of the source */
    private String name;

    private InfrastructureManager infrastructureManager;
    private NodeSourcePolicy nodeSourcePolicy;
    private RMCore rmcore;
    private Pinger pinger;
    private boolean toShutdown = false;

    // all nodes except down
    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    private HashMap<String, Node> downNodes = new HashMap<String, Node>();

    private transient BoundedNonRejectedThreadPool threadPool;
    private transient NodeLookupThread nodeLookupThread;

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
        infrastructureManager.setNodeSource(this);
        nodeSourcePolicy.setNodeSource((NodeSource) PAActiveObject.getStubOnThis());
        threadPool = new BoundedNonRejectedThreadPool(0,
            PAResourceManagerProperties.RM_NODESOURCE_MAX_THREAD_NUMBER.getValueAsInt(), 1L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        try {
            pinger = (Pinger) PAActiveObject.newActive(Pinger.class.getName(), new Object[] { PAActiveObject
                    .getStubOnThis() });
            nodeLookupThread = new NodeLookupThread();

            // these methods are called from the rm core
            // mark them as immediate services in order to prevent the block of the core
            PAActiveObject.setImmediateService("getName");
            PAActiveObject.setImmediateService("getDescription");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds nodes to the node source using infrastructure manager. Notifies node source policy
     * to handle changes.
     *
     * @param parameters information necessary to deploy nodes. Specific to each infrastructure.
     */
    public BooleanWrapper addNodes(Object... parameters) {
        try {
            infrastructureManager.addNodesAcquisitionInfo(parameters);
            rmcore.getMonitoring().nodeSourceEvent(
                    new RMNodeSourceEvent(this, RMEventType.NODESOURCE_NODES_ACQUISTION_INFO_ADDED));

            return new BooleanWrapper(true);
        } catch (RMException e) {
            throw new AddingNodesException(e.getMessage());
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

        if (logger.isInfoEnabled()) {
            logger.info("[" + name + "] new node available : " + node.getNodeInformation().getURL());
        }
        infrastructureManager.registerAcquiredNode(node);
        nodes.put(nodeUrl, node);
    }

    /**
     * Acquires the existing node with specific url. The node have to be up and running.
     *
     * @param nodeUrl the url of the node
     */
    public BooleanWrapper acquireNode(String nodeUrl) {

        // lookup for a new Node
        Node nodeToAdd = null;
        try {
            nodeToAdd = lookupNode(nodeUrl, NODE_LOOKUP_TIMEOUT);
        } catch (Exception e) {
            throw new AddingNodesException(e);
        }

        // Known URL, so do some cleanup before replacing it
        boolean isDown = false;
        if (nodes.containsKey(nodeUrl) || (isDown = downNodes.containsKey(nodeUrl))) {
            /**
             *  two potential scenario
             *  - adding the node with the same url which was restarted. In this case it's the different node from
             *    proactive point of view. So remove old node from everywhere and add new one.
             *  - adding the same node twice. Do not do any actions in this case.
             */
            Node registeredNode = isDown ? downNodes.get(nodeUrl) : nodes.get(nodeUrl);
            if (nodeToAdd != null && nodeToAdd.equals(registeredNode)) {
                logger.warn("The node " + nodeUrl + " already exist");
                return new BooleanWrapper(true);
            }

            BooleanWrapper result = rmcore.internalRemoveNodeFromCore(nodeUrl);
            if (result.booleanValue()) {
                logger.debug("[" + name + "] successfully removed node " + nodeUrl + " from the core");
                this.removeNode(nodeUrl, false);
            }
        }

        // if any exception occurs in internalAddNode(node) do not add the node to the core
        try {
            internalAddNode(nodeToAdd);
        } catch (RMException e) {
            throw new AddingNodesException(e);
        }

        RMNode rmnode = new RMNodeImpl(nodeToAdd, "noVn", (NodeSource) PAActiveObject.getStubOnThis());
        rmcore.internalAddNodeToCore(rmnode);

        return new BooleanWrapper(true);
    }

    /**
     * Dedicated thread for nodes lookup
     */
    private class NodeLookupThread extends Thread {
        private Node node;
        private String nodeUrl;

        private boolean shutDown = false;
        private Object monitor = new Object();
        private boolean isAlive = true;
        private boolean timeoutReached = false;

        public NodeLookupThread() {
            setDaemon(true);
            start();
        }

        public void run() {
            while (!shutDown) {
                synchronized (monitor) {
                    try {
                        monitor.notifyAll();
                        monitor.wait();
                    } catch (InterruptedException e) {
                    }
                }

                if (shutDown) {
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                    return;
                }

                try {
                    isAlive = false;
                    // looking for a node
                    if (nodeUrl != null) {
                        node = NodeFactory.getNode(nodeUrl);

                        synchronized (monitor) {
                            if (timeoutReached) {
                                node = null;
                            }
                        }
                    }
                } catch (NodeException e) {
                    logger.info(e.getMessage());
                }
                isAlive = true;
            }
        }

        public Node lookup(String nodeUrl, long timeout) throws Exception {

            synchronized (monitor) {
                this.nodeUrl = nodeUrl;
                timeoutReached = false;

                monitor.notifyAll();
                monitor.wait(timeout);

                if (node == null) {
                    timeoutReached = true;
                    throw new NodeException("Cannot lookup node " + nodeUrl);
                } else {
                    Node n = node;
                    node = null;
                    return n;
                }
            }
        }

        public void shutDown() {
            shutDown = true;
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }

        public boolean isThreadAlive() {
            return isAlive;
        }
    }

    /**
     * Lookups a node with specified timeout.
     *
     * @param nodeUrl a url of the node
     * @param timeout to wait in ms
     * @return node is it was successfully obtained
     * @throws Exception if node was not looked up
     */
    private Node lookupNode(String nodeUrl, long timeout) throws Exception {

        if (!nodeLookupThread.isThreadAlive()) {
            logger.debug("Lookup thread is dead - recreating it");
            // do all what is possible to stop the thread
            nodeLookupThread.shutDown();
            nodeLookupThread.interrupt();

            // creating a new one
            nodeLookupThread = new NodeLookupThread();
        }

        logger.debug("Looking up for the node " + nodeUrl + " with " + timeout + " ms timeout");
        return nodeLookupThread.lookup(nodeUrl, timeout);
    }

    /**
     * Requests one node to be acquired from the underlying infrastructure.
     */
    public void acquireNode() {
        infrastructureManager.acquireNode();
    }

    /**
     * Requests all nodes to be acquired from the infrastructure.
     */
    public void acquireAllNodes() {
        infrastructureManager.acquireAllNodes();
    }

    /**
     * Removes the node from the node source.
     *
     * @param nodeUrl the url of the node to be released
     * @param forever if true removes the node from underlying infrastructure forever without
     * an ability to re-acquire node in the future
     */
    public BooleanWrapper removeNode(String nodeUrl, boolean forever) {

        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            logger.info("[" + name + "] removing node : " + nodeUrl);
            Node node = nodes.remove(nodeUrl);
            try {
                infrastructureManager.removeNode(node, forever);
            } catch (RMException e) {
                logger.error(e.getCause().getMessage());
            }
        } else {
            Node downNode = downNodes.get(nodeUrl);
            if (downNode != null) {
                logger.info("[" + name + "] removing down node : " + nodeUrl);
                downNodes.remove(downNode);
            } else {
                logger.error("[" + name + "] removing node : " + nodeUrl +
                    " which is not belong to this node source");
                return new BooleanWrapper(false);
            }
        }

        if (toShutdown && nodes.size() == 0) {
            // shutdown all pending nodes
            finishNodeSourceShutdown();
        }

        return new BooleanWrapper(true);
    }

    /**
     * Shutdowns the node source and releases all its nodes.
     * @return true if shutdown is successful
     */
    public BooleanWrapper shutdown() {
        logger.info("[" + name + "] removal request");
        toShutdown = true;

        if (nodes.size() == 0) {
            finishNodeSourceShutdown();
        }

        return new BooleanWrapper(true);
    }

    /**
     * Gets the ping frequency.
     * @return ping frequency
     */
    public IntWrapper getPingFrequency() {
        return pinger.getPingFrequency();
    }

    /**
     * Sets the ping frequency (in ms)
     * @param frequency new value of monitoring period
     */
    public void setPingFrequency(int frequency) {
        pinger.setPingFrequency(frequency);
    }

    /**
     * Creates a node source string representation
     * @return string representation of the node source
     */
    public String getDescription() {
        return "Infrastructure:" + infrastructureManager + ", Policy: " + nodeSourcePolicy;
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
        pinger.ping();
    }

    /**
     * Finalizes node source shutdown. Stops pinger, policy and terminates node source active object.
     */
    protected void finishNodeSourceShutdown() {
        logger.info("[" + name + "] Shutdown finalization");

        PAFuture.waitFor(rmcore.nodeSourceUnregister(name, new RMNodeSourceEvent(this,
            RMEventType.NODESOURCE_REMOVED)));

        nodeSourcePolicy.shutdown();

        try {
            threadPool.shutdown();
            // join emulation
            threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nodeLookupThread.shutDown();
        pinger.shutdown();
        // object should be terminated NON preemptively
        // pinger thread can wait for last results (getNodes)
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
        threadPool.execute(command);
    }

}
