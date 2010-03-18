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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.authentication.RMAuthenticationImpl;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.jmx.JMXMonitoringHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.DatabaseManager;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManagerFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.ProbablisticSelectionManager;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.utils.ClientPinger;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * The main active object of the Resource Manager (RM), the RMCore has to
 * provide nodes to a scheduler.
 * 
 * The RMCore functions are :<BR>
 * - Create Resource Manager's active objects at its initialization ;
 * {@link RMAdmin}, {@link RMUser}, {@link RMMonitoring}.<BR>
 * - keep an up-to-date list of nodes able to perform scheduler's tasks.<BR>
 * - give nodes to the Scheduler asked by {@link RMUser} object, with a node
 * selection mechanism performed by {@link SelectionScript}.<BR>
 * - dialog with node sources which add and remove nodes to the Core. - perform
 * creation and removal of NodeSource objects. <BR>
 * - treat removing nodes and adding nodes request coming from {@link RMAdmin}.
 * - create and launch RMEvents concerning nodes and nodes Sources To
 * RMMonitoring active object.<BR>
 * <BR>
 * 
 * Nodes in Resource Manager are represented by {@link RMNode objects}. RMcore
 * has to manage different states of nodes : -free : node is ready to perform a
 * task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of the its
 * current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR>
 * <BR>
 * 
 * RMCore is not responsible of creation, acquisition and monitoring of nodes,
 * these points are performed by {@link NodeSource} objects.<BR>
 * <BR>
 * 
 * WARNING : you must instantiate this class as an Active Object !
 * 
 * RmCore should be non-blocking which means <BR>
 * - no direct access to nodes <BR>
 * - all method calls to other active objects should be either asynchronous or non-blocking immediate services <BR>
 * - methods which have to return something depending on another active objects should use an automatic continuation <BR>
 *
 * @see RMCoreInterface
 * @see RMCoreSourceInterface
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMCore implements RMAdmin, RMUser, InitActive {

    /** Log4J logger name for RMCore */
    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.CORE);

    /** If RMCore Active object */
    private String id;

    /** ProActive Node containing the RMCore */
    private Node nodeRM;

    /** stub of RMMonitoring active object of the RM */
    private RMMonitoringImpl monitoring;

    /** authentication active object */
    private RMAuthenticationImpl authentication;

    /** HashMap of NodeSource active objects */
    private HashMap<String, NodeSource> nodeSources;

    /** HashMaps of nodes known by the RMCore */
    private HashMap<String, RMNode> allNodes;

    /** list of all free nodes */
    private ArrayList<RMNode> freeNodes;

    private SelectionManager selectionManager;

    /** indicates that RMCore must shutdown */
    private boolean toShutDown = false;

    private boolean shutedDown = false;

    /**
     * Map of connected clients.
     * It is statically used due to drawbacks in the client pinger functionality
     * @see Client
     */
    public static Map<UniqueID, Client> clients = Collections
            .synchronizedMap(new HashMap<UniqueID, Client>());

    /** Client pinger */
    private ClientPinger clientPinger;

    /**
     * ProActive Empty constructor
     */
    public RMCore() {
    }

    /**
     * Creates the RMCore object.
     * 
     * @param id
     *            Name for RMCOre.
     * @param nodeRM
     *            Name of the ProActive Node object containing RM active
     *            objects.
     * @throws ActiveObjectCreationException
     *             if creation of the active object failed.
     * @throws NodeException
     *             if a problem occurs on the target node.
     */
    public RMCore(String id, Node nodeRM) throws ActiveObjectCreationException, NodeException {
        this.id = id;
        this.nodeRM = nodeRM;

        nodeSources = new HashMap<String, NodeSource>();
        allNodes = new HashMap<String, RMNode>();
        freeNodes = new ArrayList<RMNode>();
    }

    /**
     * Initialization part of the RMCore active object. <BR>
     * Create RM's active objects :<BR>
     * -{@link RMAdmin},<BR>
     * -{@link RMUser},<BR>
     * -{@link RMMonitoring},<BR>
     * and creates the default static Node Source named
     * {@link RMConstants#DEFAULT_STATIC_SOURCE_NAME}. Finally throws the RM
     * started event.
     * 
     * @param body
     *            the active object's body.
     * 
     */
    public void initActivity(Body body) {

        if (logger.isDebugEnabled()) {
            logger.debug("RMCore start : initActivity");
        }
        try {
            PAActiveObject.registerByName(PAActiveObject.getStubOnThis(),
                    RMConstants.NAME_ACTIVE_OBJECT_RMCORE);

            logger.info("Starting Hibernate...");
            boolean drop = PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getValueAsBoolean();
            logger.info("Drop DB : " + drop);
            if (drop) {
                DatabaseManager.getInstance().setProperty("hibernate.hbm2ddl.auto", "create");
            }
            DatabaseManager.getInstance().build();
            logger.info("Hibernate successfully started !");

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMAuthentication");
            }

            authentication = (RMAuthenticationImpl) PAActiveObject.newActive(RMAuthenticationImpl.class
                    .getName(), new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMMonitoring");
            }
            // Boot the JMX monitoring infrastructure            
            JMXMonitoringHelper.getInstance().boot(authentication);

            monitoring = (RMMonitoringImpl) PAActiveObject.newActive(RMMonitoringImpl.class.getName(),
                    new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object SelectionManager");
            }
            selectionManager = (SelectionManager) PAActiveObject.newActive(ProbablisticSelectionManager.class
                    .getName(), new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object ClientPinger");
            }
            clientPinger = (ClientPinger) PAActiveObject.newActive(ClientPinger.class.getName(),
                    new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            final Client internalClient = new Client() {
                public boolean isAlive() {
                    return true;
                }
            };

            // adding shutdown hook
            final RMCore rmcoreStub = (RMCore) PAActiveObject.getStubOnThis();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (!toShutDown) {
                        RMCore.clients.put(PAActiveObject.getBodyOnThis().getID(), internalClient);
                        rmcoreStub.shutdown(true);
                    }

                    synchronized (nodeRM) {
                        if (!shutedDown) {
                            try {
                                // wait for rmcore shutdown (5 min at most)
                                nodeRM.wait(5 * 60 * 60 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            // Creating RM started event
            this.monitoring.rmEvent(new RMEvent(RMEventType.STARTED));

            // registering internal clients of the core
            clients.put(Client.getId(authentication), internalClient);
            clients.put(Client.getId(monitoring), internalClient);
            clients.put(Client.getId(selectionManager), internalClient);

            authentication.setActivated(true);
            clientPinger.ping();

        } catch (ActiveObjectCreationException e) {
            logger.error("", e);
        } catch (NodeException e) {
            logger.error("", e);
        } catch (ProActiveException e) {
            logger.error("", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("RMCore end : initActivity");
        }
    }

    /**
     * Returns a node object to a corresponding URL.
     * 
     * @param url
     *            url of the node asked.
     * @return RMNode object containing the node.
     */
    private RMNode getNodebyUrl(String url) {
        assert allNodes.containsKey(url);
        return allNodes.get(url);
    }

    /**
     * Set a node's state to free, after a completed task by it. Set the to free
     * and move the node to the internal free nodes list. An event informing the
     * node state's change is thrown to RMMonitoring.
     * 
     * @param rmNode
     *            node to set free.
     */
    private void internalSetFree(final RMNode rmNode) {
        // If the node is already free no need to go further
        if (rmNode.isFree()) {
            return;
        }
        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();
        try {
            logger.debug("The node " + rmNode.getNodeURL() + " owned by " + rmNode.getOwner() + " is free");
            Client owner = rmNode.getOwner();
            // reseting owner here
            rmNode.setFree();
            this.freeNodes.add(rmNode);
            // create the event
            this.monitoring.nodeEvent(new RMNodeEvent(rmNode, RMEventType.NODE_STATE_CHANGED,
                previousNodeState, owner.getName()));
        } catch (NodeException e) {
            // Exception on the node, we assume the node is down
            setDownNode(rmNode.getNodeURL());
            logger.debug("", e);
        }
    }

    /**
     * Set a node state to 'to be released'. mark the node toRelease, and move
     * the node to the internal 'to be released' nodes list. An event informing
     * the node state's change is thrown to RMMonitoring.
     * 
     * @param rmNode
     *            node to set to release.
     */
    private void internalSetToRelease(final RMNode rmNode, Client initiator) {
        // If the node is already set to release no need to go further
        if (rmNode.isToRelease()) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Prepare to release node " + rmNode.getNodeURL());
        }
        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();
        try {
            rmNode.setToRelease();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            logger.debug("", e1);
        }
        // create the event
        this.monitoring.nodeEvent(new RMNodeEvent(rmNode, RMEventType.NODE_STATE_CHANGED, previousNodeState,
            initiator.getName()));
    }

    /**
     * Performs an RMNode release from the Core At this point the node is at
     * free or 'to be released' state. do the release, and confirm to NodeSource
     * the removal.
     * 
     * @param rmnode
     *            the node to release
     */
    private void internalDoRelease(RMNode rmnode, Client initiator) {
        if (logger.isInfoEnabled()) {
            logger.info("Releasing node " + rmnode.getNodeURL());
        }
        internalRemoveNodeFromCore(rmnode, initiator);
        rmnode.getNodeSource().removeNode(rmnode.getNodeURL(), initiator);
    }

    /**
     * Internal operations to remove the node from Core. RMNode object is
     * removed from {@link RMCore#allNodes}, removal Node event is thrown to
     * RMMonitoring Active object.
     * 
     * @param rmnode
     *            the node to remove.
     */
    private void internalRemoveNodeFromCore(RMNode rmnode, Client initiator) {
        logger.debug("Removing node " + rmnode.getNodeURL() + " provided by " + rmnode.getProvider());
        // removing the node from the HM list
        if (rmnode.isFree()) {
            freeNodes.remove(rmnode);
        }
        this.allNodes.remove(rmnode.getNodeURL());
        // create the event
        this.monitoring.nodeEvent(new RMNodeEvent(rmnode, RMEventType.NODE_REMOVED, rmnode.getState(),
            initiator.getName()));
    }

    /**
     * Internal operation of registering a new node in the Core ; adding the
     * node to the all nodes list Creating the RMNode object related to the
     * node, and put the node in free state.
     * 
     * @param rmnode
     *            node object to add
     */
    public BooleanWrapper internalAddNodeToCore(RMNode rmnode) {
        if (toShutDown) {
            logger.warn("Node " + rmnode.getNodeURL() +
                " will not be added to the core as the resource manager is shutting down");
            rmnode.getNodeSource().removeNode(rmnode.getNodeURL(), rmnode.getProvider());
            return new BooleanWrapper(false);
        }

        this.freeNodes.add(rmnode);
        this.allNodes.put(rmnode.getNodeURL(), rmnode);
        // create the event
        this.monitoring.nodeEvent(new RMNodeEvent(rmnode, RMEventType.NODE_ADDED, null, rmnode.getProvider()
                .getName()));
        if (logger.isInfoEnabled()) {
            logger.info("New node " + rmnode.getNodeURL() + " added to the node source " +
                rmnode.getNodeSourceId() + " by " + rmnode.getProvider());
        }
        return new BooleanWrapper(true);
    }

    public String getId() {
        return this.id;
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
            try {
                createNodesource(NodeSource.DEFAULT, DefaultInfrastructureManager.class.getName(), null,
                        StaticPolicy.class.getName(), null);
            } catch (RMException e) {
                logger.error("Cannot create the default node source", e);
            }
        }

        if (nodeSources.containsKey(sourceName)) {
            NodeSource nodeSource = this.nodeSources.get(sourceName);

            Client client = checkAccess();
            if (client == null)
                client = nodeSource.getProvider();

            // Known URL, so do some cleanup before replacing it
            if (allNodes.containsKey(nodeUrl)) {

                if (!allNodes.get(nodeUrl).getNodeSourceId().equals(sourceName)) {
                    // trying to already registered node to another node source
                    // do nothing in this case
                    throw new AddingNodesException("An attempt to add a node " + nodeUrl +
                        " registered in one node source to another one");
                }
            }
            return nodeSource.acquireNode(nodeUrl, client);
        } else {
            throw new AddingNodesException("Unknown node source " + sourceName);
        }
    }

    /**
     * Removes a node from the RM.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     */
    public void removeNode(String nodeUrl, boolean preempt) {

        Client initiator = checkAccess();
        if (initiator == null)
            return;

        if (this.allNodes.containsKey(nodeUrl)) {
            RMNode rmnode = this.allNodes.get(nodeUrl);

            if (rmnode.isDown() || preempt || rmnode.isFree()) {
                internalRemoveNodeFromCore(rmnode, initiator);
                rmnode.getNodeSource().removeNode(rmnode.getNodeURL(), initiator);
            } else if (rmnode.isBusy()) {
                internalSetToRelease(rmnode, initiator);
            }
        } else {
            logger.warn("An attempt to remove non existing node " + nodeUrl);
        }
    }

    /**
     * Removes "number" of nodes from the node source.
     *
     * @param number amount of nodes to be released
     * @param name a node source name
     * @param preemptive if true remove nodes immediately without waiting while they will be freed
     */
    public void removeNodes(int number, String nodeSourceName, boolean preemptive) {

        if (checkAccess() == null)
            return;
        int numberOfRemovedNodes = 0;

        // temporary list to avoid concurrent modification
        List<RMNode> nodelList = new LinkedList<RMNode>();
        nodelList.addAll(freeNodes);

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

                if (node.getNodeSource().getName().equals(nodeSourceName)) {
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
     * @param preemptive if true remove nodes immediately without waiting while they will be freed
     */
    public void removeAllNodes(String nodeSourceName, boolean preemptive) {

        if (checkAccess() == null)
            return;
        for (Node node : nodeSources.get(nodeSourceName).getAliveNodes()) {
            removeNode(node.getNodeInformation().getURL(), preemptive);
        }
        for (Node node : nodeSources.get(nodeSourceName).getDownNodes()) {
            removeNode(node.getNodeInformation().getURL(), preemptive);
        }
    }

    /**
     * Creates a new node source with specified name, infrastructure manages {@link InfrastructureManager}
     * and acquisition policy {@link NodeSourcePolicy}.
     *
     * @param nodeSourceName the name of the node source
     * @param infrastructureType type of the underlying infrastructure {@link InfrastructureType}
     * @param infrastructureParameters parameters for infrastructure creation
     * @param policyType name of the policy type. It passed as a string due to pluggable approach {@link NodeSourcePolicyFactory}
     * @param policyParameters parameters for policy creation
     * @throws RMException if any problems occurred
     */
    public void createNodesource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters)
            throws RMException {

        Client client = checkAccess();
        if (client == null)
            return;

        if (this.nodeSources.containsKey(nodeSourceName)) {
            throw new RMException("Node Source name " + nodeSourceName + " is already exist");
        }

        logger.info("Creating a node source : " + nodeSourceName);

        InfrastructureManager im = InfrastructureManagerFactory.create(infrastructureType,
                infrastructureParameters);
        NodeSourcePolicy policy = NodeSourcePolicyFactory.create(policyType, infrastructureType,
                policyParameters);

        NodeSource nodeSource;
        try {
            nodeSource = (NodeSource) PAActiveObject.newActive(NodeSource.class.getName(), new Object[] {
                    nodeSourceName, im, policy, PAActiveObject.getStubOnThis() }, nodeRM);
        } catch (Exception e) {
            throw new RMException(e);
        }

        nodeSource.setProvider(client);
        // adding node source as client to the core
        UniqueID nsId = Client.getId(nodeSource);
        UniqueID policyId = Client.getId(policy);
        if (nsId == null || policyId == null) {
            throw new RMException("Cannot register the node source");
        }

        // adding access to the core for node source and policy
        RMCore.clients.put(nsId, client);
        RMCore.clients.put(policyId, client);

        this.nodeSources.put(nodeSourceName, nodeSource);
        // create the event
        this.monitoring.nodeSourceEvent(new RMNodeSourceEvent(nodeSource, RMEventType.NODESOURCE_CREATED,
            client.getName()));

        nodeSource.activate();

        logger.info("Node source : " + nodeSourceName + " has been successfully created by " + client);
    }

    /**
     * Shutdown the resource manager
     */
    public void shutdown(boolean preempt) {
        Client client = checkAccess();
        if (client == null) {
            return;
        }
        // this method could be called twice from shutdown hook and user action
        if (toShutDown)
            return;

        logger.info("RMCore shutdown request");
        this.monitoring.rmEvent(new RMEvent(RMEventType.SHUTTING_DOWN));
        this.toShutDown = true;

        if (nodeSources.size() == 0) {
            finalizeShutdown();
        } else {
            for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
                removeAllNodes(entry.getKey(), preempt);
                entry.getValue().shutdown(client);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Methods called by RMUser, override RMCoreInterface
    // ----------------------------------------------------------------------

    /**
     * Return number of free nodes available for scheduling
     * @return number of free nodes
     */
    public IntWrapper getFreeNodesNumber() {
        if (checkAccess() == null)
            throw new RuntimeException("Permission denied");
        return new IntWrapper(this.freeNodes.size());
    }

    /**
     * Gives total number of alive nodes handled by RM
     * @return total number of alive nodes
     */
    public IntWrapper getTotalAliveNodesNumber() {
        if (checkAccess() == null)
            throw new RuntimeException("Permission denied");

        // TODO get the number of alive nodes in a more effective way
        int count = 0;
        for (RMNode node : allNodes.values()) {
            if (!node.isDown())
                count++;
        }
        return new IntWrapper(count);
    }

    /**
     * Gives total number of nodes handled by RM (including dead nodes)
     * @return total number of nodes
     */
    public IntWrapper getTotalNodesNumber() {
        if (checkAccess() == null)
            throw new RuntimeException("Permission denied");
        return new IntWrapper(this.allNodes.size());
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#freeNode(org.objectweb.proactive.core.node.Node)
     */
    public void freeNode(Node node) {
        Client client = checkAccess();
        if (client == null)
            return;

        String nodeURL = null;
        try {
            nodeURL = node.getNodeInformation().getURL();
        } catch (RuntimeException e) {
            logger.debug("A Runtime exception occured " + "while obtaining information on the node,"
                + "the node must be down (it will be detected later)", e);
            // node is down,
            // will be detected later
            return;
        }

        // verify whether the node has not been removed from the RM
        if (this.allNodes.containsKey(nodeURL)) {
            RMNode rmnode = this.getNodebyUrl(nodeURL);

            // prevent Scheduler Error : Scheduler try to render anode already
            // free
            if (rmnode.isFree()) {
                logger.warn("[RMCORE] scheduler tried to free a node already free ! Node URL : " + nodeURL);
            } else {
                // verify that scheduler don't try to render a node detected
                // down
                if (!rmnode.isDown()) {

                    if (!rmnode.getOwner().equals(client)) {
                        logger.warn("An attempt to free a node by another user (won't be performed) " +
                            node.getNodeInformation().getURL());
                        return;
                    }

                    if (rmnode.isToRelease()) {
                        internalDoRelease(rmnode, client);
                    } else {
                        internalSetFree(rmnode);
                    }
                }
            }
        } else {
            logger.warn("[RMCORE] scheduler asked to free an unknown node ! Node URL : " + nodeURL);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#freeNodes(org.ow2.proactive.utils.NodeSet)
     */
    public void freeNodes(NodeSet nodes) {
        for (Node node : nodes)
            freeNode(node);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes, SelectionScript selectionScript) {
        List<SelectionScript> selectionScriptList = selectionScript == null ? null : Collections
                .singletonList(selectionScript);
        return getAtMostNodes(nbNodes.intValue(), selectionScriptList, null);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes, SelectionScript selectionScript, NodeSet exclusion) {
        List<SelectionScript> selectionScriptList = selectionScript == null ? null : Collections
                .singletonList(selectionScript);
        return getAtMostNodes(nbNodes.intValue(), selectionScriptList, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(IntWrapper nbNodes, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion) {
        return getAtMostNodes(nbNodes.intValue(), selectionScriptsList, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(int nbNodes, SelectionScript selectionScript) {
        List<SelectionScript> selectionScriptList = selectionScript == null ? null : Collections
                .singletonList(selectionScript);
        return getAtMostNodes(nbNodes, selectionScriptList, null);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(int nbNodes, SelectionScript selectionScript, NodeSet exclusion) {
        List<SelectionScript> selectionScriptList = selectionScript == null ? null : Collections
                .singletonList(selectionScript);
        return getAtMostNodes(nbNodes, selectionScriptList, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getAtMostNodes(int nb, List<SelectionScript> selectionScriptList, NodeSet exclusion) {

        // if RM is in shutdown state, it doesn't give nodes
        if (this.toShutDown) {
            return new NodeSet();
        } else {

            Client client = checkAccess();
            if (client == null)
                throw new RuntimeException("Permission denied");

            logger.info(client + " requested " + nb + " nodes");
            return selectionManager.findAppropriateNodes(nb, selectionScriptList, exclusion, client);
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeSet getExactlyNodes(IntWrapper nbNodes, SelectionScript selectionScript) {
        return getExactlyNodes(nbNodes.intValue(), selectionScript);
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

        ArrayList<RMNodeEvent> nodesList = new ArrayList<RMNodeEvent>();
        for (RMNode rmnode : this.allNodes.values()) {
            nodesList.add(new RMNodeEvent(rmnode));
        }

        ArrayList<RMNodeSourceEvent> nodeSourcesList = new ArrayList<RMNodeSourceEvent>();
        for (NodeSource s : this.nodeSources.values()) {
            nodeSourcesList.add(new RMNodeSourceEvent(s));
        }

        return new RMInitialState(nodesList, nodeSourcesList);
    }

    /**
     * Gets RM monitoring stub
     */
    public RMMonitoringImpl getMonitoring() {
        return this.monitoring;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface#nodeSourceUnregister(java.lang.String,
     *      org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public BooleanWrapper nodeSourceUnregister(String sourceId, RMNodeSourceEvent evt) {
        if (checkAccess() == null)
            throw new RuntimeException("Permission denied");

        NodeSource nodeSource = this.nodeSources.remove(sourceId);

        if (nodeSource == null) {
            logger.warn("Attempt to remove non-existing node source " + sourceId);
            new BooleanWrapper(false);
        }

        // remove just node source from clients
        // policy will be removed by client pinger
        UniqueID id = Client.getId(nodeSource);
        if (id == null) {
            RMCore.clients.remove(id);
        } else {
            logger.error("Cannot extract the body id of the node source " + sourceId);
        }
        logger.info("Node Source removed : " + sourceId);
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

            this.nodeRM.getProActiveRuntime().killRT(true);
        } catch (Exception e) {
            logger.debug("", e);
        }
    }

    /**
     * Set a node state to busy. Set the node to busy, and move the node to the
     * internal busy nodes list. An event informing the node state's change is
     * thrown to RMMonitoring.
     * @param client
     *
     * @param rmnode
     *            node to set
     * @throws NodeException if the node can't be set busy
     */
    public void setBusyNode(final String nodeUrl, Client owner) throws NodeException {
        if (checkAccess() == null)
            return;

        final RMNode rmNode = this.allNodes.get(nodeUrl);
        if (rmNode == null) {
            logger.error("Unknown node " + nodeUrl);
            return;
        }
        // If the node is already busy no need to go further
        if (rmNode.isBusy()) {
            return;
        }
        // Get the previous state of the node needed for the event
        final NodeState previousNodeState = rmNode.getState();
        try {
            rmNode.setBusy(owner);
        } catch (NodeException e) {
            // A down node shouldn't be busied...
            logger.error("Unable to set the node " + rmNode.getNodeURL() + " busy", e);
            // Since this method throws a NodeException re-throw e to inform the caller 
            throw e;
        }
        this.freeNodes.remove(rmNode);
        // create the event
        this.monitoring.nodeEvent(new RMNodeEvent(rmNode, RMEventType.NODE_STATE_CHANGED, previousNodeState,
            owner.getName()));
    }

    /**
     * Sets a node state to down and updates all internal structures of rm core
     * accordingly. Sends an event indicating that the node is down.
     */
    public void setDownNode(String nodeUrl) {
        if (checkAccess() == null)
            return;

        RMNode rmNode = getNodebyUrl(nodeUrl);
        if (rmNode != null) {
            // If the node is already down no need to go further
            if (rmNode.isDown()) {
                return;
            }
            logger.info("The node " + rmNode.getNodeURL() + " provided by " + rmNode.getProvider() +
                " is down");
            // Get the previous state of the node needed for the event
            final NodeState previousNodeState = rmNode.getState();
            if (rmNode.isFree()) {
                freeNodes.remove(rmNode);
            }
            rmNode.setDown();
            // create the event
            this.monitoring.nodeEvent(new RMNodeEvent(rmNode, RMEventType.NODE_STATE_CHANGED,
                previousNodeState, rmNode.getProvider().getName()));
        } else {
            // the nodes has been removed from core asynchronously
            // when pinger of selection manager tried to access it
            // do nothing in this case
        }
    }

    /**
     * Removed a node with given url from the internal structures of the core.
     *
     * @param nodeUrl down node to be removed
     * @return true if the nodes was successfully removed, false otherwise
     */
    public BooleanWrapper internalRemoveNodeFromCore(String nodeUrl) {
        Client initiator = checkAccess();
        if (initiator == null)
            throw new RuntimeException("Permission denied");

        RMNode rmnode = getNodebyUrl(nodeUrl);
        if (rmnode != null) {
            internalRemoveNodeFromCore(rmnode, initiator);
            return new BooleanWrapper(true);
        } else {
            return new BooleanWrapper(false);
        }
    }

    public ArrayList<RMNode> getFreeNodes() {
        return freeNodes;
    }

    /**
     * {@inheritDoc}
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) throws RMException {
        if (checkAccess() == null)
            throw new RuntimeException("Permission denied");

        if (this.nodeSources.containsKey(sourceName)) {
            return this.nodeSources.get(sourceName).getPingFrequency();
        } else {
            throw new RMException("unknown node source : " + sourceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setNodeSourcePingFrequency(int frequency, String sourceName) throws RMException {
        if (checkAccess() == null)
            return;

        if (this.nodeSources.containsKey(sourceName)) {
            this.nodeSources.get(sourceName).setPingFrequency(frequency);
        } else {
            throw new RMException("unknown node source : " + sourceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAllNodeSourcesPingFrequency(int frequency) {
        if (checkAccess() == null)
            return;
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            entry.getValue().setPingFrequency(frequency);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultNodeSourcePingFrequency(int frequency) throws RMException {
        if (checkAccess() == null)
            return;
        setNodeSourcePingFrequency(frequency, NodeSource.DEFAULT);
    }

    /**
     * {@inheritDoc}
     */
    public List<RMNodeSourceEvent> getNodeSourcesList() {
        return getRMInitialState().getNodeSource();
    }

    /**
     * {@inheritDoc}
     */
    public List<RMNodeEvent> getNodesList() {
        return getRMInitialState().getNodesEvents();
    }

    /**
     * {@inheritDoc}
     */
    public void removeSource(String sourceName, boolean preempt) throws RMException {

        if (nodeSources.containsKey(sourceName)) {

            Client client = checkAccess();
            if (client == null)
                return;
            logger.info(client + " requested removal of the " + sourceName + " node source");

            //remove down nodes handled by the source
            //because node source doesn't know anymore its down nodes
            removeAllNodes(sourceName, preempt);
            nodeSources.get(sourceName).shutdown(client);
        } else {
            throw new RMException("unknown node source : " + sourceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public RMState getRMState() {
        if (checkAccess() == null)
            throw new RuntimeException("Permission denied");

        RMState state = new RMState();
        state.setNumberOfFreeResources(getFreeNodesNumber());
        state.setNumberOfAllResources(getTotalNodesNumber());
        return state;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAlive() {
        return !toShutDown;
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() {
        disconnect(PAActiveObject.getContext().getCurrentRequest().getSender().getID());
    }

    /**
     * Disconnects the client and releases all nodes held by him
     */
    public void disconnect(UniqueID clientId) {
        Client client = RMCore.clients.remove(clientId);
        if (client != null) {
            // expensive but relatively rare operation
            for (RMNode rmnode : allNodes.values()) {
                if (rmnode.getOwner().equals(client)) {
                    if (rmnode.isToRelease()) {
                        internalDoRelease(rmnode, client);
                    } else if (rmnode.isBusy()) {
                        internalSetFree(rmnode);
                    }
                }
            }
            logger.info(client + " disconnected");
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

        Collection<PluginDescriptor> descriptors = new ArrayList<PluginDescriptor>();

        for (Class<?> cls : plugins) {
            descriptors.add(new PluginDescriptor(cls));
        }
        return descriptors;
    }

    /**
     * Checks if the caller thread has an access to the core
     * @return client object corresponding to the caller thread
     */
    private Client checkAccess() {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        Client client = RMCore.clients.get(id);
        if (client != null) {
            return client;
        }
        logger.warn("Access denied for client " +
            PAActiveObject.getContext().getCurrentRequest().getSourceBodyID() + "\n", new Exception(
            "Access denied"));

        return null;
    }
}
