package org.ow2.proactive.resourcemanager.nodesource;

import java.util.HashMap;
import java.util.LinkedList;

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
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.utils.Pinger;
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
    /** Default name */
    public static final String DEFAULT_NAME = "Default";

    /** unique name of the source */
    private String name;

    private InfrastructureManager infrastructureManager;
    private NodeSourcePolicy nodeSourcePolicy;
    private RMCore rmcore;
    private Pinger pinger;
    private boolean toShutdown = false;

    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    private LinkedList<Node> downNodes = new LinkedList<Node>();

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
        try {
            pinger = (Pinger) PAActiveObject.newActive(Pinger.class.getName(), new Object[] { PAActiveObject
                    .getStubOnThis() });
            pinger.ping();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds nodes to the node source using infrastructure manager. Notifies node source policy
     * to handle changes.
     *
     * @param parameters information necessary to deploy nodes. Specific to each infrastructure.
     * @throws RMException if any errors occurred
     */
    public void addNodes(Object... parameters) throws RMException {
        infrastructureManager.addNodesAcquisitionInfo(parameters);
    }

    /**
     * Updates internal node source structures.
     */
    private void internalAddNode(Node node) throws RMException {
        if (this.nodes.containsKey(node)) {
            throw new RMException("a node with the same URL is already in this node Source,"
                + "remove this node, before adding a node with a a same URL");
        }

        if (logger.isInfoEnabled()) {
            logger.info("[" + name + "] new node available : " + node.getNodeInformation().getURL());
        }
        nodes.put(node.getNodeInformation().getURL(), node);
    }

    /**
     * Acquires the existing node with specific url. The node have to be up and running.
     *
     * @param nodeUrl the url of the node
     */
    public Node acquireNode(String nodeUrl) throws RMException {
        Node node;
        try {
            node = NodeFactory.getNode(nodeUrl);
            internalAddNode(node);
        } catch (NodeException e) {
            throw new RMException(e);
        }
        return node;
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
    public void removeNode(String nodeUrl, boolean forever) {

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
            Node downNode = null;
            for (Node dn : downNodes) {
                if (dn.getNodeInformation().getURL().equals(nodeUrl)) {
                    downNode = dn;
                    break;
                }
            }

            if (downNode != null) {
                logger.info("[" + name + "] removing down node : " + nodeUrl);
                downNodes.remove(downNode);
            } else {
                logger.error("[" + name + "] removing node : " + nodeUrl +
                    " which is not belong to this node source");
            }
        }

        if (toShutdown && nodes.size() == 0) {
            // shutdown all pending nodes
            finishNodeSourceShutdown();
        }

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
     * Returns the event object representing the NodeSource. <BR>
     * Called by {@link org.ow2.proactive.resourcemanager.core.RMCore}.<BR>
     * Create a {@link RMNodeSourceEvent} object representing the NodeSource
     * State.
     *
     * @return {@link RMNodeSourceEvent} object contains properties of the
     *         NodeSource.
     */
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(name, getDescription());
    }

    /**
     * Creates a node source string representation
     * @return string representation of the node source
     */
    private String getDescription() {
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
    }

    /**
     * Finalizes node source shutdown. Stops pinger, policy and terminates node source active object.
     */
    protected void finishNodeSourceShutdown() {
        logger.info("[" + name + "] Shutdown finalization");
        PAFuture.waitFor(nodeSourcePolicy.disactivate());
        pinger.shutdown();
        rmcore.nodeSourceUnregister(name, this.getSourceEvent());
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
        if (downNode != null)
            downNodes.add(downNode);
        rmcore.setDownNode(nodeUrl);
    }

    /**
     * Gets resource manager core. Used by policies.
     * @return {@link RMCoreInterface}
     */
    public RMCoreInterface getRMCore() {
        return rmcore;
    }
}
