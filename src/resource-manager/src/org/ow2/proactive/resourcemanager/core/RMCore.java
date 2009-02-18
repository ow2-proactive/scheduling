/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.ow2.proactive.authentication.RestrictedService;
import org.ow2.proactive.resourcemanager.authentication.RMAuthenticationImpl;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMAdminImpl;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.frontend.RMUserImpl;
import org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.gcm.GCMNodeSource;
import org.ow2.proactive.resourcemanager.nodesource.p2p.P2PNodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;
import org.ow2.proactive.resourcemanager.selection.ProbablisticSelectionManager;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.ScriptWithResult;
import org.ow2.proactive.scripting.SelectionScript;


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
 * RMCore got at least one node Source created at its startup (named
 * {@link RMConstants#DEFAULT_STATIC_SOURCE_NAME}), which is a Static node
 * source ({@link GCMNodeSource}), able to receive a {@link GCMApplication}
 * objects and deploy them.<BR>
 * <BR>
 * 
 * WARNING : you must instantiate this class as an Active Object !
 * 
 * @see RMCoreInterface
 * @see RMCoreSourceInterface
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class RMCore extends RestrictedService implements RMCoreInterface, InitActive, RMCoreSourceInterface,
        Serializable {

    /** Log4J logger name for RMCore */
    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.CORE);

    /** If RMCore Active object */
    private String id;

    /** ProActive Node containing the RMCore */
    private Node nodeRM;

    /** stub of RMAdmin active object of the RM */
    private RMAdminImpl admin;

    /** stub of RMMonitoring active object of the RM */
    private RMMonitoringImpl monitoring;

    /** stub of RMuser active object of the RM */
    private RMUserImpl user;

    /** authentication active object */
    private RMAuthenticationImpl authentication;

    /** HashMap of NodeSource active objects */
    private HashMap<String, NodeSource> nodeSources;

    /** HashMaps of nodes known by the RMCore */
    private HashMap<String, RMNode> allNodes;

    /** list of all free nodes */
    private ArrayList<RMNode> freeNodes;

    private SelectionManager selectionManager;

    /** Timeout for selection script result */
    private static final int MAX_VERIF_TIMEOUT = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT
            .getValueAsInt();

    /** Name of the static node source created at startup */
    private static final String DEFAULT_NODE_SOURCE_NAME = RMConstants.DEFAULT_STATIC_SOURCE_NAME;

    /** indicates that RMCore must shutdown */
    private boolean toShutDown = false;

    /** nodes to deploy during startup of resource manager */
    private Collection<String> localGCMDeploymentFiles = null;

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
        selectionManager = new ProbablisticSelectionManager();
    }

    /**
     * Creates the RMCore object with further deployment of given data.
     * 
     * @param id
     *            Name for RMCOre.
     * @param nodeRM
     *            Name of the ProActive Node object containing RM active
     *            objects.
     * @param gcmDeploymentData
     *            data to deploy.
     * @throws ActiveObjectCreationException
     *             if creation of the active object failed.
     * @throws NodeException
     *             if a problem occurs on the target node.
     */
    public RMCore(String id, Node nodeRM, Collection<String> localGCMDeploymentFiles)
            throws ActiveObjectCreationException, NodeException {
        this(id, nodeRM);
        this.localGCMDeploymentFiles = localGCMDeploymentFiles;
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
            PAActiveObject.register(PAActiveObject.getStubOnThis(), "//" +
                PAActiveObject.getNode().getVMInformation().getHostName() + "/" +
                RMConstants.NAME_ACTIVE_OBJECT_RMCORE);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMAuthentication");
            }

            authentication = (RMAuthenticationImpl) PAActiveObject.newActive(RMAuthenticationImpl.class
                    .getName(), new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMAdmin");
            }

            admin = (RMAdminImpl) PAActiveObject.newActive(RMAdminImpl.class.getName(), new Object[] {
                    PAActiveObject.getStubOnThis(), authentication }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMUser");
            }

            user = (RMUserImpl) PAActiveObject.newActive(RMUserImpl.class.getName(), new Object[] {
                    PAActiveObject.getStubOnThis(), authentication }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMMonitoring");
            }
            monitoring = (RMMonitoringImpl) PAActiveObject.newActive(RMMonitoringImpl.class.getName(),
                    new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            // register objects which are allowed to call methods of RMCore
            registerTrustedService(authentication);
            registerTrustedService(admin);
            registerTrustedService(user);
            registerTrustedService(monitoring);

            if (logger.isDebugEnabled()) {
                logger.debug("instanciation RMNodeManager");
            }

            this.createGCMNodesource(null, DEFAULT_NODE_SOURCE_NAME);

            // deployment of required nodes 
            if (localGCMDeploymentFiles != null) {
                String GCMD_PROPERTY_NAME = PAResourceManagerProperties.RM_GCMD_PATH_PROPERTY_NAME
                        .getValueAsString();
                String applicationDescriptor = PAResourceManagerProperties.RM_HOME.getValueAsString() +
                    File.separator +
                    PAResourceManagerProperties.RM_GCM_TEMPLATE_APPLICATION_FILE.getValueAsString();

                for (String gcmDeploymentFile : localGCMDeploymentFiles) {
                    GCMApplication appl;
                    try {
                        System.setProperty(GCMD_PROPERTY_NAME, gcmDeploymentFile);
                        appl = PAGCMDeployment.loadApplicationDescriptor(new File(applicationDescriptor));
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    addingNodesAdminRequest(appl, DEFAULT_NODE_SOURCE_NAME);
                }
                localGCMDeploymentFiles = null; // don't need it anymore

                // adding shutdown hook
                // the shutdown hook is needed only for more convenient way to
                // shutdown the resource manages when it was started by "startRM.sh"
                // script (so the owner does not have to provide login information)
                // for other cases don't use the hook because it has 
                // concurrency issues when it is used with other shutdown technics
                // like gcmAllp.kill()
                final RMCore rmcoreStub = (RMCore) PAActiveObject.getStubOnThis();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        RMCore.this.registerTrustedService(PAActiveObject.getBodyOnThis().getID());
                        if (!RMCore.this.toShutDown) {
                            PAFuture.waitFor(rmcoreStub.shutdown(true));
                        }
                    }
                });
            }

            // Creating RM started event
            this.monitoring.rmStartedEvent(new RMEvent());

            authentication.setActivated(true);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (RMException e) {
            e.printStackTrace();
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
     * @param rmnode
     *            node to set free.
     */
    private void internalSetFree(RMNode rmnode) {
        // the node can only come from a busy state or down state
        assert rmnode.isBusy();
        try {
            rmnode.clean(); // cleaning the node, kill all active objects
            rmnode.setFree();
            this.freeNodes.add(rmnode);

            // create the event
            this.monitoring.nodeFreeEvent(rmnode.getNodeEvent());
        } catch (NodeException e) {
            // Exception on the node, we assume the node is down
            internalSetDown(rmnode);
            e.printStackTrace();
        }
    }

    /**
     * Set a node state to busy. Set the node to busy, and move the node to the
     * internal busy nodes list. An event informing the node state's change is
     * thrown to RMMonitoring.
     * 
     * @param rmnode
     *            node to set
     */
    private void internalSetBusy(RMNode rmnode) {
        assert rmnode.isFree();
        assert this.freeNodes.contains(rmnode);
        rmnode.clean();
        try {
            rmnode.setBusy();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
        this.freeNodes.remove(rmnode);
        // create the event
        this.monitoring.nodeBusyEvent(rmnode.getNodeEvent());
    }

    /**
     * Set a node state to 'to be released'. mark the node toRelease, and move
     * the node to the internal 'to be released' nodes list. An event informing
     * the node state's change is thrown to RMMonitoring.
     * 
     * @param rmnode
     *            node to set.
     */
    private void internalSetToRelease(RMNode rmnode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Prepare to release node " + rmnode.getNodeURL());
        }
        // the node can only come from a busy state
        assert rmnode.isBusy();
        try {
            rmnode.setToRelease();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
        // create the event
        this.monitoring.nodeToReleaseEvent(rmnode.getNodeEvent());
    }

    /**
     * Set a node state to down, and move the node to the internal down nodes
     * list. An event informing the node state's change is thrown to
     * RMMonitoring
     */
    private void internalSetDown(RMNode rmnode) {
        logger.info("Down node : " + rmnode.getNodeURL() + ", from Source : " + rmnode.getNodeSourceId());
        assert (!rmnode.isDown());

        if (rmnode.isFree()) {
            freeNodes.remove(rmnode);
        }
        rmnode.setDown();
        // create the event
        this.monitoring.nodeDownEvent(rmnode.getNodeEvent());
    }

    /**
     * Performs an RMNode release from the Core At this point the node is at
     * free or 'to be released' state. do the release, and confirm to NodeSource
     * the removal.
     * 
     * @param rmnode
     *            the node to release
     */
    private void internalDoRelease(RMNode rmnode) {
        if (logger.isInfoEnabled()) {
            logger.info("Releasing node " + rmnode.getNodeURL());
        }
        rmnode.clean();
        internalRemoveNodeFromCore(rmnode);
        rmnode.getNodeSource().nodeRemovalCoreRequest(rmnode.getNodeURL(), false);
    }

    /**
     * Internal operations to remove the node from Core. RMNode object is
     * removed from {@link RMCore#allNodes}, removal Node event is thrown to
     * RMMonitoring Active object.
     * 
     * @param rmnode
     *            the node to remove.
     */
    private void internalRemoveNodeFromCore(RMNode rmnode) {
        // removing the node from the HM list
        if (rmnode.isFree()) {
            freeNodes.remove(rmnode);
        }
        this.allNodes.remove(rmnode.getNodeURL());
        // create the event
        this.monitoring.nodeRemovedEvent(rmnode.getNodeEvent());
    }

    /**
     * Internal operation of registering a new node in the Core ; adding the
     * node to the all nodes list Creating the RMNode object related to the
     * node, and put the node in free state.
     * 
     * @param node
     *            node object to add
     * @param VNodeName
     *            name of the Virtual node if eventually the node has been
     *            deployed by a GCM deployment descriptor.
     * @param nodeSource
     *            Stub of Active object node source responsible of the
     *            management of this node.
     */
    private void internalAddNodeToCore(Node node, String VNodeName, NodeSource nodeSource) {
        RMNode rmnode = new RMNodeImpl(node, VNodeName, nodeSource);
        try {
            rmnode.clean();
            rmnode.setFree();
            this.freeNodes.add(rmnode);
            this.allNodes.put(rmnode.getNodeURL(), rmnode);
            // create the event
            this.monitoring.nodeAddedEvent(rmnode.getNodeEvent());
        } catch (NodeException e) {
            // Exception on the node, we assume the node is down
            e.printStackTrace();
        }
        if (logger.isInfoEnabled()) {
            logger.info("New node added, node ID is : " + rmnode.getNodeURL() + ", node Source : " +
                nodeSource.getSourceId());
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getId()
     */
    public String getId() {
        return this.id;
    }

    // ----------------------------------------------------------------------
    // Methods called by RMAdmin, override RMCoreInterface
    // ----------------------------------------------------------------------

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#createGCMNodesource(org.objectweb.proactive.gcmdeployment.GCMApplication,
     *      java.lang.String)
     */
    public void createGCMNodesource(GCMApplication GCMApp, String sourceName) throws RMException {
        logger.info("Creating a GCM Node source : " + sourceName);
        if (this.nodeSources.containsKey(sourceName)) {
            throw new RMException("Node Source name " + sourceName + " is already existing");
        } else {
            try {
                NodeSource gcmSource = (NodeSource) PAActiveObject.newActive(GCMNodeSource.class.getName(),
                        new Object[] { sourceName, (RMCoreSourceInterface) PAActiveObject.getStubOnThis() },
                        nodeRM);

                nodeSourceRegister(gcmSource, sourceName);

                if (GCMApp != null) {
                    ((GCMNodeSource) gcmSource).nodesAddingCoreRequest(GCMApp);
                }
            } catch (Exception e) {
                throw new RMException(e);
            }
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#createDynamicNodeSource(java.lang.String,
     *      int, int, int, java.util.Vector)
     */
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice, int ttr, Vector<String> peerUrls)
            throws RMException {
        logger.info("Creating a P2P source " + id);

        // check that a Node Source with a same name is not already existing
        if (this.nodeSources.containsKey(id)) {
            throw new RMException("Node Source name " + id + " is already existing");
        } else {
            // check that a P2P node source is not already existing
            // It's a drawback, but for the moment we are just able to start one
            // peer to peer service per JVM
            for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
                if (entry.getValue().getSourceEvent().getSourceType()
                        .equals(RMConstants.P2P_NODE_SOURCE_TYPE)) {
                    throw new RMException("A P2P node source is already existing");
                }
            }
        }
        try {
            PAActiveObject.newActive(P2PNodeSource.class.getName(),
                    new Object[] { id, (RMCoreSourceInterface) PAActiveObject.getStubOnThis(), nbMaxNodes,
                            nice, ttr, peerUrls }, nodeRM);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#addingNodesAdminRequest(org.objectweb.proactive.gcmdeployment.GCMApplication,
     *      java.lang.String)
     */
    public void addingNodesAdminRequest(GCMApplication GCMApp, String sourceName) throws RMException {
        if (this.nodeSources.containsKey(sourceName)) {
            try {
                this.nodeSources.get(sourceName).nodesAddingCoreRequest(GCMApp);
            } catch (AddingNodesException e) {
                throw new RMException(e);
            }
        } else {
            throw new RMException("unknown node source " + sourceName);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#addingNodesAdminRequest(org.objectweb.proactive.gcmdeployment.GCMApplication)
     */
    public void addingNodesAdminRequest(GCMApplication GCMApp) {
        try {
            this.nodeSources.get(DEFAULT_NODE_SOURCE_NAME).nodesAddingCoreRequest(GCMApp);
        } catch (AddingNodesException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#addingNodeAdminRequest(java.lang.String)
     */
    public void addingNodeAdminRequest(String nodeUrl) throws RMException {
        addingNodeAdminRequest(nodeUrl, DEFAULT_NODE_SOURCE_NAME);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#addingNodeAdminRequest(java.lang.String,
     *      java.lang.String)
     */
    public void addingNodeAdminRequest(String nodeUrl, String sourceName) throws RMException {
        if (this.nodeSources.containsKey(sourceName)) {
            try {
                // URL node already known, remove previous node with the same
                // URL
                // and add the new one
                if (this.allNodes.containsKey(nodeUrl)) {
                    RMNode previousNode = this.allNodes.get(nodeUrl);

                    // unregister internally the node to the Core
                    internalRemoveNodeFromCore(previousNode);
                    // unregister the node to its node Source
                    previousNode.getNodeSource().nodeRemovalCoreRequest(nodeUrl, true);
                }

                Node nodeToAdd = NodeFactory.getNode(nodeUrl);
                NodeSource nodeSource = this.nodeSources.get(sourceName);

                // register the node to its node Source
                nodeSource.nodeAddingCoreRequest(nodeUrl);

                // register internally the node to the Core
                this.internalAddNodeToCore(nodeToAdd, "noVn", nodeSource);

            } catch (AddingNodesException e) {
                throw new RMException(e);
            } catch (NodeException e) {
                throw new RMException(e);
            }
        } else {
            throw new RMException("unknown node source " + sourceName);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#nodeRemovalAdminRequest(java.lang.String,
     *      boolean)
     */
    public void nodeRemovalAdminRequest(String nodeUrl, boolean preempt) {
        if (this.allNodes.containsKey(nodeUrl)) {
            RMNode rmnode = this.allNodes.get(nodeUrl);

            // if node already down, just removing from down list and global
            // list
            // Node sources have already removed the node because they have
            // detected the node down
            if (rmnode.isDown()) {
                assert rmnode.isDown();
                this.internalRemoveNodeFromCore(rmnode);
            } else if (preempt || rmnode.isFree()) {
                internalRemoveNodeFromCore(rmnode);
                rmnode.getNodeSource().nodeRemovalCoreRequest(nodeUrl, preempt);
            } else if (rmnode.isBusy()) {
                internalSetToRelease(rmnode);
            }
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#nodeSourceRemovalAdminRequest(java.lang.String,
     *      boolean)
     */
    public void nodeSourceRemovalAdminRequest(String sourceName, boolean preempt) throws RMException {
        if (sourceName.equals(DEFAULT_NODE_SOURCE_NAME)) {
            throw new RMException("Default static node source cannot be removed");
        } else if (nodeSources.containsKey(sourceName)) {
            //remove down nodes handled by the source
            //because node source doesn't know anymore its down nodes

            Iterator<RMNode> it = allNodes.values().iterator();
            while (it.hasNext()) {
                RMNode rmnode = it.next();
                if (rmnode.isDown() && rmnode.getNodeSourceId().equals(sourceName)) {
                    internalRemoveNodeFromCore(rmnode);
                    it = allNodes.values().iterator();
                }
            }
            nodeSources.get(sourceName).shutdown(preempt);
        } else {
            throw new RMException("unknown node source : " + sourceName);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#shutdown(boolean)
     */
    public Boolean shutdown(boolean preempt) {
        this.monitoring.rmShuttingDownEvent(new RMEvent());
        this.toShutDown = true;

        List<Boolean> shutdownStatus = new LinkedList<Boolean>();
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            shutdownStatus.add(entry.getValue().shutdown(preempt));
        }

        PAFuture.waitForAll(shutdownStatus);
        return new Boolean(true);
    }

    // ----------------------------------------------------------------------
    // Methods called by RMUser, override RMCoreInterface
    // ----------------------------------------------------------------------

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getNbAllRMNodes()
     */
    public IntWrapper getNbAllRMNodes() {
        return new IntWrapper(this.allNodes.size());
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getSizeListFreeRMNodes()
     */
    public IntWrapper getSizeListFreeRMNodes() {
        return new IntWrapper(this.freeNodes.size());
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#freeNode(org.objectweb.proactive.core.node.Node)
     */
    public void freeNode(Node node) {
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

        // verify wether the node has not been removed from the RM
        if (this.allNodes.containsKey(nodeURL)) {
            RMNode rmnode = this.getNodebyUrl(nodeURL);

            assert (rmnode.isBusy() || rmnode.isToRelease() || rmnode.isDown());
            // prevent Scheduler Error : Scheduler try to render anode already
            // free
            if (rmnode.isFree()) {
                logger.warn("[RMCORE] scheduler tried to free a node already free ! Node URL : " + nodeURL);
            } else {
                // verify that scheduler don't try to render a node detected
                // down
                if (!rmnode.isDown()) {
                    if (rmnode.isToRelease()) {
                        internalDoRelease(rmnode);
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
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#freeNodes(org.ow2.proactive.resourcemanager.frontend.NodeSet)
     */
    public void freeNodes(NodeSet nodes) {
        for (Node node : nodes)
            freeNode(node);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getAtMostNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper,
     *      org.ow2.proactive.scripting.SelectionScript,
     *      org.ow2.proactive.resourcemanager.frontend.NodeSet)
     */
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript, NodeSet exclusion) {
        ArrayList<SelectionScript> list = null;
        if (selectionScript != null) {
            list = new ArrayList<SelectionScript>();
            list.add(selectionScript);
        }
        return getAtMostNodes(nb, list, exclusion);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getAtMostNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper,
     *      java.util.List, org.ow2.proactive.resourcemanager.frontend.NodeSet)
     */
    public NodeSet getAtMostNodes(IntWrapper nb, List<SelectionScript> selectionScriptList, NodeSet exclusion) {

        // if RM is in shutdown state, it doesn't give nodes
        if (this.toShutDown) {
            return new NodeSet();
        } else {

            logger.info("Number of requested nodes is " + nb.intValue());
            NodeSet result = new NodeSet();
            // getting sorted by probability candidates nodes from selection manager
            Collection<RMNode> candidatesNodes = selectionManager.findAppropriateNodes(selectionScriptList,
                    freeNodes, exclusion);
            boolean scriptSpecified = selectionScriptList != null && selectionScriptList.size() > 0;

            // if no script specified no execution is required
            // in this case selection manager just return a list of free nodes
            if (!scriptSpecified) {
                for (RMNode rmnode : candidatesNodes) {
                    if (result.size() == nb.intValue()) {
                        break;
                    }
                    try {
                        internalSetBusy(rmnode);
                        result.add(rmnode.getNode());
                    } catch (NodeException e) {
                        internalSetDown(rmnode);
                    }
                }
                return result;
            }

            // scripts were specified
            // start execution on candidates set until we have enough
            // candidates or test each node
            Iterator<RMNode> nodesIterator = candidatesNodes.iterator();
            HashMap<RMNode, List<ScriptWithResult>> scriptsExecutionResults;

            while (nodesIterator.hasNext() && result.size() < nb.intValue()) {
                scriptsExecutionResults = executeScripts(selectionScriptList, nodesIterator, nb.intValue() -
                    result.size());
                result.addAll(processScriptResults(scriptsExecutionResults));
            }

            logger.info("Number of found nodes is " + result.size());
            return result;
        }
    }

    /**
     * Executes set of scripts on "number" nodes.
     * Returns "future" script results for further analysis.
     */
    private HashMap<RMNode, List<ScriptWithResult>> executeScripts(List<SelectionScript> selectionScriptList,
            Iterator<RMNode> nodesIterator, int number) {
        HashMap<RMNode, List<ScriptWithResult>> scriptExecutionResults = new HashMap<RMNode, List<ScriptWithResult>>();
        while (nodesIterator.hasNext() && scriptExecutionResults.keySet().size() < number) {
            RMNode rmnode = nodesIterator.next();
            scriptExecutionResults.put(rmnode, executeScripts(rmnode, selectionScriptList));
        }
        return scriptExecutionResults;
    }

    /**
    * Executes set of scripts on a given node.
    * Returns "future" script results for further analysis.
    */
    private List<ScriptWithResult> executeScripts(RMNode rmnode, List<SelectionScript> selectionScriptList) {
        List<ScriptWithResult> scriptExecitionResults = new LinkedList<ScriptWithResult>();

        for (SelectionScript script : selectionScriptList) {
            if (selectionManager.scriptWillPassOnTheNode(script, rmnode)) {
                // already executed static script
                logger.info("Skipping script execution " + script.hashCode() + " on node " +
                    rmnode.getNodeURL());
                scriptExecitionResults.add(new ScriptWithResult(script, new ScriptResult<Boolean>(true)));
                continue;
            }

            logger.info("Executing script " + script.hashCode() + " on node " + rmnode.getNodeURL());
            ScriptResult<Boolean> scriptResult = rmnode.executeScript(script);

            // if r is not a future, the script has not been executed
            //TODO check if that code is always needed
            // because exception in script handler creation/execution
            // produce an exception in ScriptResult
            if (MOP.isReifiedObject(scriptResult)) {
                scriptExecitionResults.add(new ScriptWithResult(script, scriptResult));
            } else {
                scriptExecitionResults.add(new ScriptWithResult(script, null));
                // script has not been executed on remote host
                logger.info("Error occured executing verifying script : " +
                    scriptResult.getException().getMessage());
            }
        }

        return scriptExecitionResults;
    }

    /**
     * Processes script execution results, updating selection manager knowledge base.
     * Returns a set of selected nodes. 
     */
    private NodeSet processScriptResults(HashMap<RMNode, List<ScriptWithResult>> scriptsExecutionResults) {

        // deadline for scripts execution
        long deadline = System.currentTimeMillis() + MAX_VERIF_TIMEOUT;

        NodeSet result = new NodeSet();
        for (RMNode rmnode : scriptsExecutionResults.keySet()) {
            assert (scriptsExecutionResults.containsKey(rmnode));

            // checking whether all scripts are passed or not for the node
            boolean scriptPassed = true;
            for (ScriptWithResult swr : scriptsExecutionResults.get(rmnode)) {
                ScriptResult<Boolean> scriptResult = swr.getScriptResult();
                try {
                    // calculating time to wait script result
                    long timeToWait = deadline - System.currentTimeMillis();
                    if (timeToWait <= 0)
                        timeToWait = 1; //ms
                    PAFuture.waitFor(scriptResult, timeToWait);
                } catch (ProActiveTimeoutException e) {
                    // no script result was obtained
                    logger.info("Time out expired in waiting ends of script execution: " + e.getMessage());
                    scriptResult = null;
                }

                // processing script result and updating knowledge base of 
                // selection manager at the same time. Returns whether node is selected.
                if (!selectionManager.processScriptResult(swr.getScript(), scriptResult, rmnode)) {
                    scriptPassed = false;
                }
            }

            if (scriptPassed) {
                try {
                    internalSetBusy(rmnode);
                    result.add(rmnode.getNode());
                } catch (NodeException e) {
                    internalSetDown(rmnode);
                }
            }
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getExactlyNodes(org.objectweb.proactive.core.util.wrapper.IntWrapper,
     *      org.ow2.proactive.scripting.SelectionScript)
     */
    public NodeSet getExactlyNodes(IntWrapper nb, SelectionScript selectionScript) {
        // not implemented
        return null;
    }

    // ----------------------------------------------------------------------
    // Methods called by RMMonitoring, override RMCoreInterface
    // ----------------------------------------------------------------------

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
            nodesList.add(rmnode.getNodeEvent());
        }

        ArrayList<RMNodeSourceEvent> nodeSourcesList = new ArrayList<RMNodeSourceEvent>();
        for (NodeSource s : this.nodeSources.values()) {
            nodeSourcesList.add(s.getSourceEvent());
        }

        return new RMInitialState(nodesList, nodeSourcesList);
    }

    // ----------------------------------------------------------------------
    // Methods called by RMFactory, override RMCoreInterface
    // ----------------------------------------------------------------------

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getAdmin()
     */
    public RMAdminImpl getAdmin() {
        return this.admin;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getMonitoring()
     */
    public RMMonitoring getMonitoring() {
        return this.monitoring;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getUser()
     */
    public RMUserImpl getUser() {
        return this.user;
    }

    // ----------------------------------------------------------------------
    // Methods called by NodeSource objects, override RMNodeManagerSourceInt
    // ----------------------------------------------------------------------

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface#nodeSourceRegister(org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource,
     *      java.lang.String)
     */
    public void nodeSourceRegister(NodeSource source, String sourceId) {
        this.nodeSources.put(sourceId, source);
        // create the event
        this.monitoring.nodeSourceAddedEvent(source.getSourceEvent());
        registerTrustedService(source);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface#nodeSourceUnregister(java.lang.String,
     *      org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public void nodeSourceUnregister(String sourceId, RMNodeSourceEvent evt) {
        NodeSource nodeSource = this.nodeSources.remove(sourceId);

        if (logger.isInfoEnabled()) {
            logger.info("Node Source removed : " + sourceId);
        }
        // create the event
        this.monitoring.nodeSourceRemovedEvent(evt);
        unregisterTrustedService(nodeSource);

        if ((this.nodeSources.size() == 0) && this.toShutDown) {
            // all nodes sources has been removed and RMCore in shutdown state,
            // finish the shutdown
            this.user.shutdown();
            this.monitoring.shutdown();
            PAActiveObject.terminateActiveObject(admin, false);
            PAActiveObject.terminateActiveObject(true);
            try {
                Thread.sleep(2000);
                this.nodeRM.getProActiveRuntime().killRT(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface#addingNodeNodeSourceRequest(org.objectweb.proactive.core.node.Node,
     *      java.lang.String,
     *      org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource)
     */
    public void addingNodeNodeSourceRequest(Node node, String VNodeName, NodeSource nodeSource) {
        internalAddNodeToCore(node, VNodeName, nodeSource);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface#nodeRemovalNodeSourceRequest(java.lang.String,
     *      boolean)
     */
    public void nodeRemovalNodeSourceRequest(String nodeUrl, boolean preempt) {
        RMNode rmnode = getNodebyUrl(nodeUrl);
        if (preempt) {
            internalRemoveNodeFromCore(rmnode);
        } else {
            if (rmnode.isDown()) {
                internalRemoveNodeFromCore(rmnode);
            } else if (rmnode.isFree()) {
                internalDoRelease(rmnode);
            } else if (rmnode.isBusy()) {
                internalSetToRelease(rmnode);
            }
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface#setDownNode(java.lang.String)
     */
    public void setDownNode(String nodeUrl) {
        RMNode rmnode = getNodebyUrl(nodeUrl);
        if (rmnode != null) {
            this.internalSetDown(rmnode);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#getPingFrequency(java.lang.String)
     */
    public IntWrapper getPingFrequency(String sourceName) throws RMException {
        if (this.nodeSources.containsKey(sourceName)) {
            return this.nodeSources.get(sourceName).getPingFrequency();
        } else {
            throw new RMException("unknown node source : " + sourceName);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#setAllPingFrequency(int)
     */
    public void setAllPingFrequency(int frequency) {
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            entry.getValue().setPingFrequency(frequency);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#setPingFrequency(int)
     */
    public void setPingFrequency(int frequency) {
        this.nodeSources.get(DEFAULT_NODE_SOURCE_NAME).setPingFrequency(frequency);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.RMCoreInterface#setPingFrequency(int,
     *      java.lang.String)
     */
    public void setPingFrequency(int frequency, String sourceName) throws RMException {
        if (this.nodeSources.containsKey(sourceName)) {
            this.nodeSources.get(sourceName).setPingFrequency(frequency);
        } else {
            throw new RMException("unknown node source : " + sourceName);
        }
    }

    /**
     * Logger used in RestrictedService
     */
    public Logger getLogger() {
        return logger;
    }

}
