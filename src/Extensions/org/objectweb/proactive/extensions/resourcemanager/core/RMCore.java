/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.resourcemanager.core;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.exception.AddingNodesException;
import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdminImpl;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoringImpl;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUser;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUserImpl;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.gcm.GCMNodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.pad.PADNodeSource;
import org.objectweb.proactive.extensions.resourcemanager.rmnode.RMNode;
import org.objectweb.proactive.extensions.resourcemanager.rmnode.RMNodeComparator;
import org.objectweb.proactive.extensions.resourcemanager.rmnode.RMNodeImpl;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extensions.scheduler.common.scripting.SelectionScript;


/**
 * The main active object of the Resource Manager (RM),
 * the RMCore has to provide nodes to a scheduler.
 *
 * The RMCore functions are :<BR>
 *  - Create Resource Manager's active objects at its initialization ;
 *  {@link RMAdmin}, {@link RMUser}, {@link RMMonitoring}.<BR>
 *  - keep an up-to-date list of nodes able to perform scheduler's tasks.<BR>
 *  - give nodes to the Scheduler asked by {@link RMUser} object,
 *  with a node selection mechanism performed by {@link SelectionScript}.<BR>
 *  - dialog with node sources which add and remove nodes to the Core.
 *  - perform creation and removal of NodeSource objects. <BR>
 *  - treat removing nodes and adding nodes request coming from {@link RMAdmin}.
 *  - create and launch RMEvents concerning nodes and nodes Sources To RMMonitoring
 *  active object.<BR><BR>
 *
 * Nodes in Resource Manager are represented by {@link RMNode objects}.
 * RMcore has to manage different states of nodes :
 * -free : node is ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of the its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR><BR>
 *
 * RMCore is not responsible of creation, acquisition and monitoring of nodes,
 * these points are performed by {@link NodeSource} objects.<BR><BR>
 * RMCore got at least one node Source created at its startup
 * (named {@link RMCore#DEFAULT_STATIC_SOURCE_NAME}),
 * which is a Static node source ({@link PADNodeSource}), able to receive
 * a {@link ProActiveDescriptor} objects and deploy them.<BR><BR>
 *
 * WARNING : you must instantiate this class as an Active Object !
 *
 * @see RMCoreInterface
 * @see RMCoreSourceInterface
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 */
public class RMCore implements RMCoreInterface, InitActive, RMCoreSourceInterface, Serializable {

    /** Log4J logger name for RMCore */
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.RM_CORE);

    /** If RMCore Active object */
    private String id;

    /** ProActive Node containing the RMCore */
    private Node nodeRM;

    /** stub of RMAdmin active object of the RM */
    private RMAdmin admin;

    /** stub of RMMonitoring active object of the RM */
    private RMMonitoringImpl monitoring;

    /** stub of RMuser active object of the RM */
    private RMUser user;

    /** HashMap of NodeSource active objects*/
    private HashMap<String, NodeSource> nodeSources;

    /** HashMaps of nodes known by the RMCore*/
    private HashMap<String, RMNode> allNodes;

    /** list of all free nodes*/
    private ArrayList<RMNode> freeNodes;

    /** list of all busy nodes*/
    private ArrayList<RMNode> busyNodes;

    /** list of all down nodes*/
    private ArrayList<RMNode> downNodes;

    /** list of all 'to be released' nodes*/
    private ArrayList<RMNode> toBeReleased;

    /** Timeout for selection script result */
    private static final int MAX_VERIF_TIMEOUT = 120000;

    /** indicates that RMCore must shutdown*/
    private boolean toShutDown = false;

    /**
     * ProActive Empty constructor
     */
    public RMCore() {
    }

    /**
     * Creates the RMCore object.
     * @param id Name for RMCOre.
     * @param nodeRM Name of the ProActive Node object containing
     * RM active objects.
     * @throws ActiveObjectCreationException.
     * @throws NodeException.
     */
    public RMCore(String id, Node nodeRM) throws ActiveObjectCreationException, NodeException {
        this.id = id;
        this.nodeRM = nodeRM;

        nodeSources = new HashMap<String, NodeSource>();
        allNodes = new HashMap<String, RMNode>();

        freeNodes = new ArrayList<RMNode>();
        busyNodes = new ArrayList<RMNode>();
        downNodes = new ArrayList<RMNode>();
        toBeReleased = new ArrayList<RMNode>();
    }

    /**
     * Initialization part of the RMCore active object.
     * <BR>Create RM's active objects :<BR>
     * -{@link RMAdmin},<BR>
     * -{@link RMUser},<BR>
     * -{@link RMMonitoring},<BR>
     * and creates the default static Node Source named {@link RMCore#DEFAULT_STATIC_SOURCE_NAME}.
     * Finally throws the RM started event.
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("RMCore start : initActivity");
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("active object RMAdmin");
            }

            admin = (RMAdminImpl) PAActiveObject.newActive(RMAdminImpl.class.getName(),
                    new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMMonitoring");
            }
            monitoring = (RMMonitoringImpl) PAActiveObject.newActive(RMMonitoringImpl.class.getName(),
                    new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object RMUser");
            }

            user = (RMUserImpl) PAActiveObject.newActive(RMUserImpl.class.getName(),
                    new Object[] { PAActiveObject.getStubOnThis() }, nodeRM);

            if (logger.isDebugEnabled()) {
                logger.debug("instanciation RMNodeManager");
            }

            this.createStaticNodesource(null, RMConstants.DEFAULT_STATIC_SOURCE_NAME);

            //Creating RM started event 
            this.monitoring.rmStartedEvent(new RMEvent());
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
     * @param url url of the node asked.
     * @return RMNode object containing the node.
     */
    private RMNode getNodebyUrl(String url) {
        assert allNodes.containsKey(url);
        return allNodes.get(url);
    }

    /**
     * Set a node's state to free, after a completed task by it.
     * Set the to free and move the node to the internal free nodes list.
     * An event informing the node state's change is thrown to RMMonitoring.
     * @param rmnode node to set free.
     */
    private void setFree(RMNode rmnode) {
        //the node can only come from a busy state
        assert rmnode.isBusy();
        assert this.busyNodes.contains(rmnode);
        try {
            rmnode.clean(); // cleaning the node, kill all active objects
            this.busyNodes.remove(rmnode);
            rmnode.setFree();
            this.freeNodes.add(rmnode);
        } catch (NodeException e) {
            // Exception on the node, we assume the node is down
            e.printStackTrace();
        }

        //set all dynamic script results to the state of ALREADY_VERIFIED_SCRIPT
        HashMap<SelectionScript, Integer> verifs = rmnode.getScriptStatus();
        for (Entry<SelectionScript, Integer> entry : verifs.entrySet()) {
            if (entry.getKey().isDynamic() && (entry.getValue() == RMNode.VERIFIED_SCRIPT)) {
                entry.setValue(RMNode.ALREADY_VERIFIED_SCRIPT);
            }
        }
        //create the event
        this.monitoring.nodeFreeEvent(rmnode.getNodeEvent());
    }

    /**
     * Set a node state to busy.
     * Set the node to busy, and move the node to the internal busy nodes list.
     * An event informing the node state's change is thrown to RMMonitoring.
     * @param rmnode node to set
     */
    private void setBusy(RMNode rmnode) {
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
        busyNodes.add(rmnode);
        //create the event
        this.monitoring.nodeBusyEvent(rmnode.getNodeEvent());
    }

    /**
     * Set a node state to 'to be released'.
     * mark the node toRelease, and move the node to the internal 'to be released' nodes list.
     * An event informing the node state's change is thrown to RMMonitoring.
     * @param rmnode node to set.
     */
    private void setToRelease(RMNode rmnode) {
        //the node can only come from a busy state
        assert rmnode.isBusy();
        assert this.busyNodes.contains(rmnode);
        this.busyNodes.remove(rmnode);
        this.toBeReleased.add(rmnode);
        try {
            rmnode.setToRelease();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
        //create the event
        this.monitoring.nodeToReleaseEvent(rmnode.getNodeEvent());
    }

    /**
     * Set a node state to down.
     * Set the node down, and move the node to the internal down nodes list.
     * An event informing the node state's change is thrown to RMMonitoring
     */
    private void setDown(RMNode rmnode) {
        logger.info("[RMCORE] down node : " + rmnode.getNodeURL() + ", from Source : " +
            rmnode.getNodeSourceId());
        assert (this.busyNodes.contains(rmnode) || this.freeNodes.contains(rmnode) || this.toBeReleased
                .contains(rmnode));
        removeFromAllLists(rmnode);
        this.downNodes.add(rmnode);
        rmnode.setDown();
        //create the event
        this.monitoring.nodeDownEvent(rmnode.getNodeEvent());
    }

    /**
     * Internal actions to prepare release of a node.
     * A NodeSource has asked to release a node in a 'softly' way.
     * If the node is free, release immediately the node
     * else mark node's state  to 'to be released',
     * so the node will be released when scheduler will render the node
     */
    private void releaseNode(RMNode rmnode) {
        assert (rmnode.isFree() || rmnode.isBusy());
        assert ((rmnode.isFree() && this.freeNodes.contains(rmnode)) || (rmnode.isBusy() && this.busyNodes
                .contains(rmnode)));
        if (logger.isInfoEnabled()) {
            logger.info("[RMCORE] prepare to release node " + rmnode.getNodeURL());
        }
        if (rmnode.isFree()) {
            doRelease(rmnode);
        } else {
            setToRelease(rmnode);
        }
    }

    /**
     * Performs an RMNode release from the Core
     * At this point the node is at free or 'to be released' state.
     * do the release, and confirm to NodeSource the removal.
     * @param rmnode the node to release
     */
    private void doRelease(RMNode rmnode) {
        assert rmnode.isFree() || rmnode.isToRelease();
        assert ((rmnode.isFree() && this.freeNodes.contains(rmnode)) || (rmnode.isToRelease() && this.toBeReleased
                .contains(rmnode)));

        if (logger.isInfoEnabled()) {
            logger.info("[RMCORE] releasing node " + rmnode.getNodeURL());
        }
        rmnode.clean();
        removeNodeFromCore(rmnode);
        rmnode.getNodeSource().confirmRemoveNode(rmnode.getNodeURL());
    }

    /**
     * Internal operations to remove the node from Core.
     * RMNode object is removed from {@link RMCore#allNodes},
     * removal Node event is thrown to RMMonitoring Active object.
     * @param rmnode the node to remove.
     */
    private void removeNodeFromCore(RMNode rmnode) {
        //removing the node from the HM list		
        this.removeFromAllLists(rmnode);
        this.allNodes.remove(rmnode.getNodeURL());
        //create the event      
        this.monitoring.nodeRemovedEvent(rmnode.getNodeEvent());
    }

    /**
     * Remove the RMNode object from all the lists it can appears.
     * @param rmnode the node to be removed
     * @return true if the node has been removed form one list, false otherwise
     */

    // TODO gsigety, cdelbe TO BE REMOVED ?
    private boolean removeFromAllLists(RMNode rmnode) {
        boolean free = this.freeNodes.remove(rmnode);
        boolean busy = this.busyNodes.remove(rmnode);
        boolean toBeReleased = this.toBeReleased.remove(rmnode);
        boolean down = this.downNodes.remove(rmnode);
        return free || busy || toBeReleased || down;
    }

    /**
     * Returns free nodes sorted by a SelectionScript test result.
     * Returns free nodes list in a specific order :
     * - if there is no script to verify, just return the free nodes ;
     * - if there is a script, tries to give the nodes in an efficient order :
     *                 -> First the nodes that verified the script before ;
     *                 -> Next, the nodes that haven't been tested ;
     *                 -> Next, the nodes that have already verified the script, but no longer ;
     *                 -> To finish, the nodes that don't verify the script.
     * @see org.objectweb.proactive.extensions.resourcemanager.rmnode.RMCoreInterface#getNodesByScript(org.objectweb.proactive.extensions.scheduler.common.scripting.VerifyingScript)
     */
    private ArrayList<RMNode> getNodesSortedByScript(SelectionScript script) {
        ArrayList<RMNode> result = new ArrayList<RMNode>();
        for (RMNode imnode : this.freeNodes) {
            result.add(imnode);
        }
        if ((script != null)) {
            Collections.sort(result, new RMNodeComparator(script));
        }
        return result;
    }

    /**
     * Selects nodes which verify a static Selection script test.
     * Tries to give nodes that have already verified selection script.
     * if these nodes are not enough, launch the Selection script on other free nodes,
     * gather result, and return at most nb nodes which verify the script.
     * @param nb number of of nodes asked
     * @param selectionScript selectionScript that must be verified by nodes
     * @param nodes list of free nodes.
     * @return NodeSet of nodes verifying the SelectionScript.
     */
    private NodeSet selectNodeWithStaticVerifScript(int nb, SelectionScript selectionScript,
            ArrayList<RMNode> nodes) {
        NodeSet result = new NodeSet();
        int found = 0;

        logger.info("[RMCORE] Searching for " + nb + " nodes  with static verif script on " +
            this.getSizeListFreeRMNodes() + " free nodes.");
        //select nodes where the static script has already be launched and satisfied
        while (!nodes.isEmpty() && (found < nb)) {
            RMNode node = nodes.remove(0);
            if (node.getScriptStatus().containsKey(selectionScript) &&
                node.getScriptStatus().get(selectionScript).equals(RMNode.VERIFIED_SCRIPT)) {
                try {
                    result.add(node.getNode());
                    setBusy(node);
                    found++;
                } catch (NodeException e) {
                    setDown(node);
                }
            } else {
                break;
            }
        }

        Vector<ScriptResult<Boolean>> scriptResults = new Vector<ScriptResult<Boolean>>();
        Vector<RMNode> nodeResults = new Vector<RMNode>();
        int launched = found;

        //if other nodes needed, launching the script on nodes Remaining 
        while (!nodes.isEmpty() && (launched++ < nb)) {
            nodeResults.add(nodes.get(0));
            ScriptResult<Boolean> sr = nodes.get(0).executeScript(selectionScript);

            // if r is not a future, the script has not been executed
            if (MOP.isReifiedObject(sr)) {
                scriptResults.add(sr);
            } else {
                // script has not been executed on remote host
                // nothing to do, just let the node in the free list
                logger.info("Error occured executing verifying script", sr.getException());
            }
            nodes.remove(0);
        }

        // Recupere les resultats
        do {
            try {
                if (!scriptResults.isEmpty()) {
                    int idx = PAFuture.waitForAny(scriptResults, MAX_VERIF_TIMEOUT);
                    RMNode imnode = nodeResults.remove(idx);
                    ScriptResult<Boolean> res = scriptResults.remove(idx);
                    if (res.errorOccured()) {
                        // nothing to do, just let the node in the free list
                        logger.info("Error occured executing verifying script", res.getException());
                    } else if (res.getResult()) {
                        // Result OK
                        try {
                            result.add(imnode.getNode());
                            setBusy(imnode);
                            imnode.setVerifyingScript(selectionScript);
                            found++;
                        } catch (NodeException e) {
                            setDown(imnode);
                            //try on a new node if any
                            if (!nodes.isEmpty()) {
                                nodeResults.add(nodes.get(0));
                                scriptResults.add(nodes.remove(0).executeScript(selectionScript));
                            }
                        }
                    } else {
                        // result is false
                        imnode.setNotVerifyingScript(selectionScript);
                        // try on a new node if any
                        if (!nodes.isEmpty()) {
                            nodeResults.add(nodes.get(0));
                            scriptResults.add(nodes.remove(0).executeScript(selectionScript));
                        }
                    }
                } else {
                    if (!nodes.isEmpty()) {
                        nodeResults.add(nodes.get(0));
                        scriptResults.add(nodes.remove(0).executeScript(selectionScript));
                    }
                }
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                // Wait For Any Timeout...
                // traitement special
                e.printStackTrace();
            }
        } while ((!scriptResults.isEmpty() || !nodes.isEmpty()) && (found < nb));

        return result;
    }

    /**
     * Selects at most nb nodes which verify a dynamic selection script test.
     * Tries to give nodes that have already verified selection script.
     * if these nodes are not enough, launch the Selection script on other free nodes,
     * gather result, and return at most nb nodes which verify the script.
     * @param nb number of of nodes asked
     * @param selectionScript selectionScript that must be verified by nodes
     * @param nodes list of free nodes.
     * @return NodeSet of nodes verifying the SelectionScript.
     */
    private NodeSet selectNodeWithDynamicVerifScript(int nb, SelectionScript selectionScript,
            ArrayList<RMNode> nodes) {
        logger.info("[RMCORE] Searching for " + nb + " nodes  with dynamic verif script on " +
            this.getSizeListFreeRMNodes() + " free nodes.");

        StringBuffer order = new StringBuffer();
        for (RMNode n : nodes) {
            order.append(n.getHostName() + " ");
        }
        logger.info("[RMCORE] Available nodes are : " + order);
        Vector<ScriptResult<Boolean>> scriptResults = new Vector<ScriptResult<Boolean>>();
        Vector<RMNode> nodeResults = new Vector<RMNode>();
        NodeSet result = new NodeSet();
        int found = 0;

        // launch verification on n(needed) first nodes
        int launched = 0;
        while (!nodes.isEmpty() && (launched++ < nb)) {
            nodeResults.add(nodes.get(0));
            ScriptResult<Boolean> r = nodes.get(0).executeScript(selectionScript);

            // if r is not a future, the script has not been executed
            if (MOP.isReifiedObject(r)) {
                scriptResults.add(r);
            } else {
                // script has not been executed on remote host
                // nothing to do, just let the node in the free list
                logger.info("Error occured executing verifying script", r.getException());
            }
            nodes.remove(0);
        }
        do {
            try {
                if (!scriptResults.isEmpty()) {
                    int idx = PAFuture.waitForAny(scriptResults, MAX_VERIF_TIMEOUT);
                    // idx could be -1 if an error occured in wfa (or timeout
                    // expires)
                    RMNode imnode = nodeResults.remove(idx);
                    ScriptResult<Boolean> res = scriptResults.remove(idx);
                    if (res.errorOccured()) {
                        // nothing to do, just let the node in the free list
                        logger.info("Error occured executing verifying script", res.getException());
                    } else if (res.getResult()) {
                        // Result OK
                        try {
                            result.add(imnode.getNode());
                            setBusy(imnode);
                            imnode.setVerifyingScript(selectionScript);
                            found++;
                        } catch (NodeException e) {
                            setDown(imnode);
                            //try on a new node if any
                            if (!nodes.isEmpty()) {
                                nodeResults.add(nodes.get(0));
                                scriptResults.add(nodes.remove(0).executeScript(selectionScript));
                            }
                        }
                    } else {
                        // result is false
                        imnode.setNotVerifyingScript(selectionScript);
                        // try on a new node if any
                        if (!nodes.isEmpty()) {
                            nodeResults.add(nodes.get(0));
                            scriptResults.add(nodes.remove(0).executeScript(selectionScript));
                        }
                    }
                } else {
                    if (!nodes.isEmpty()) {
                        nodeResults.add(nodes.get(0));
                        scriptResults.add(nodes.remove(0).executeScript(selectionScript));
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // Wait For Any Timeout...
                // traitement special
                e.printStackTrace();
            }
        } while ((!scriptResults.isEmpty() || !nodes.isEmpty()) && (found < nb));
        return result;
    }

    /**
     * Gives a String representing of the RMCore's ID.
     * @return String representing of the RMCore's ID.
     */
    public String getId() {
        return this.id;
    }

    //----------------------------------------------------------------------
    //  Methods called by RMAdmin, override RMCoreInterface  
    //----------------------------------------------------------------------

    /**
     * Creates a static node source Active Object.
     * Creates a new static node source which is a {@link PADNodeSource} active object.
     * @param pad a ProActiveDescriptor object to deploy at the node source creation.
     * @param sourceName name given to the static node source.
     */
    public void createStaticNodesource(List<ProActiveDescriptor> padList, String sourceName)
            throws RMException {
        logger.info("[RMCORE] Creating a Static source : " + sourceName);
        if (this.nodeSources.containsKey(sourceName)) {
            throw new RMException("Node Source name already existing");
        } else {
            try {
                NodeSource padSource = (NodeSource) PAActiveObject.newActive(PADNodeSource.class.getName(),
                        new Object[] { sourceName, (RMCoreSourceInterface) PAActiveObject.getStubOnThis() },
                        nodeRM);
                if (padList != null) {
                    for (ProActiveDescriptor pad : padList) {
                        padSource.addNodes(pad);
                    }
                }
            } catch (Exception e) {
                throw new RMException(e);
            }
        }
    }

    public void createGCMNodesource(File descriptorPad, String sourceName) throws RMException {
        logger.info("[RMCORE] Creating a GCM Node source : " + sourceName);
        if (this.nodeSources.containsKey(sourceName)) {
            throw new RMException("Node Source name already existing");
        } else {
            try {
                NodeSource gcmSource = (NodeSource) PAActiveObject.newActive(GCMNodeSource.class.getName(),
                        new Object[] { sourceName, (RMCoreSourceInterface) PAActiveObject.getStubOnThis() },
                        nodeRM);
                if (descriptorPad != null) {

                    ((GCMNodeSource) gcmSource).addNodes(descriptorPad);
                }
            } catch (Exception e) {
                throw new RMException(e);
            }
        }
    }

    /**
     * Creates a Dynamic Node source Active Object.
     * Creates a new dynamic node source which is a {@link P2PNodeSource} active object.
     * Other dynamic node source (PBS, OAR) are not yet implemented.
     * @param id name of the dynamic node source to create.
     * @param nbMaxNodes max number of nodes the NodeSource has to provide.
     * @param nice nice time in ms, time to wait between a node remove and a new node acquisition.
     * @param ttr Time to release in ms, time during the node will be kept by the nodes source and the Core.
     * @param peerUrls vector of ProActive P2P living peers and able to provide nodes.
     */
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice, int ttr, Vector<String> peerUrls)
            throws RMException {
        logger.info("[RMCORE] Creating a P2P source " + id);
        if (this.nodeSources.containsKey(id)) {
            throw new RMException("Node Source name already existing");
        } else {
            try {
                final String P2PNodeSourceClassname = "org.objectweb.proactive.extra.p2p.scheduler.P2PNodeSource";

                try {
                    Class.forName(P2PNodeSourceClassname);
                } catch (ClassNotFoundException e) {
                    throw new RMException("P2P extension is not supported in this version.", e);
                }

                PAActiveObject.newActive(P2PNodeSourceClassname, new Object[] { id,
                        (RMCoreSourceInterface) PAActiveObject.getStubOnThis(), nbMaxNodes, nice, ttr,
                        peerUrls }, nodeRM);
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * add nodes to a static Node Source.
     * ask to a static Node source to deploy a ProActiveDescriptor.
     * nodes deployed will be added after to RMCore, by the NodeSource itself.
     * @param pad ProActiveDescriptor to deploy
     * @param sourceName name of an existing PADNodesource
     */
    public void addNodes(ProActiveDescriptor pad, String sourceName) throws RMException {
        if (this.nodeSources.containsKey(sourceName)) {
            try {
                this.nodeSources.get(sourceName).addNodes(pad);
            } catch (AddingNodesException e) {
                throw new RMException(e);
            }
        } else {
            throw new RMException("unknown node source");
        }
    }

    /**
     * add nodes to the default static Node Source.
     * ask to the default static Node source to deploy a ProActiveDescriptor.
     * nodes deployed will be added after to RMCore, by the NodeSource itself.
     * @param pad ProActiveDescriptor to deploy
     */
    public void addNodes(ProActiveDescriptor pad) {
        try {
            this.nodeSources.get(RMConstants.DEFAULT_STATIC_SOURCE_NAME).addNodes(pad);
        } catch (AddingNodesException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a deployed node to the default static nodes source of the RM
     * @param nodeUrl Url of the node.
     */
    public void addNode(String nodeUrl) throws RMException {
        try {
            this.nodeSources.get(RMConstants.DEFAULT_STATIC_SOURCE_NAME).addNode(nodeUrl);
        } catch (AddingNodesException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add nodes to a StaticNodeSource represented by sourceName.
     * SourceName must exist and must be a static source
     * @param pad ProActive deployment descriptor to deploy.
     * @param sourceName name of the static node source that perform the deployment.
     */
    public void addNode(String nodeUrl, String sourceName) throws RMException {
        if (this.nodeSources.containsKey(sourceName)) {
            try {
                this.nodeSources.get(sourceName).addNode(nodeUrl);
            } catch (AddingNodesException e) {
                throw new RMException(e);
            }
        } else {
            throw new RMException("unknown node source");
        }
    }

    /**
     * Remove a node from the Core and from its node source.
     * perform the removing request of a node
     * asked by {@link RMAdmin} active object.<BR><BR>
     *
     * If the node is down, node is just removed from the Core, and nothing is asked to its related NodeSource,
     * because the node source has already detected the node down (it is its function), informed the RMCore,
     * and removed the node from its list.<BR>
     * Else the removing request is just forwarded to the corresponding NodeSource of the node.<BR><BR>
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy.
     * False the node is removed just after the job ending if the node is busy, or immediately if free
     */
    public void removeNode(String nodeUrl, boolean preempt) {
        if (this.allNodes.containsKey(nodeUrl)) {
            RMNode imnode = this.allNodes.get(nodeUrl);

            //if node already down, just removing from down list and global list
            //Node sources have already removed the node because they have detected the node passing down
            if (imnode.isDown()) {
                assert this.downNodes.contains(imnode);
                this.removeNodeFromCore(imnode);
            } else {
                this.allNodes.get(nodeUrl).getNodeSource().forwardRemoveNode(nodeUrl, preempt);
            }
        }
    }

    /**
     * Stops the Resource Manager.
     * Stops all {@link NodeSource} active objects
     * Stops {@link RMAdmin}, {@link RMUser}, {@link RMMonitoring} active objects.
     */
    public void shutdown(boolean preempt) {
        this.toShutDown = true;
        this.monitoring.rmShuttingDownEvent(new RMEvent());
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            entry.getValue().shutdown(preempt);
        }
    }

    /**
     * Stops and remove a NodeSource active object and remove its nodes from the Resource Manager
     * @param sourceName name of the NodeSource object to remove.
     * @param preempt true all the nodes must be removed immediately, without waiting job ending if nodes are busy,
     * false nodes are removed just after the job ending, if busy.
     */
    public void removeSource(String sourceName, boolean preempt) throws RMException {
        if (sourceName.equals(RMConstants.DEFAULT_STATIC_SOURCE_NAME)) {
            throw new RMException("Default static node source cannot be removed");
        } else if (nodeSources.containsKey(sourceName)) {
            this.nodeSources.get(sourceName).shutdown(preempt);
        } else {
            throw new RMException("unknown node source");
        }
    }

    //----------------------------------------------------------------------
    //  Methods called by RMUser, override RMCoreInterface  
    //----------------------------------------------------------------------

    /**
     * Gives number of nodes handled by the Core.
     * @return IntWrapper number of nodes in the RMCore.
     */
    public IntWrapper getNbAllRMNodes() {
        return new IntWrapper(this.allNodes.size());
    }

    /**
     * Gives number of free nodes handled by the Core.
     * @return IntWrapper number of free nodes in the RMCore.
     */
    public IntWrapper getSizeListFreeRMNodes() {
        return new IntWrapper(this.freeNodes.size());
    }

    /**
     * free a node after a work.
     * RMUser active object wants to free a node that ended a task.
     * If the node is 'to be released', perform the removing mechanism with
     * the {@link NodeSource} object corresponding to the node,
     * otherwise just set the node to free.
     * @param node node that has terminated a task and must be freed.
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

        //verify wether the node has not been removed from the RM
        if (this.allNodes.containsKey(nodeURL)) {
            RMNode imnode = this.getNodebyUrl(nodeURL);

            assert (imnode.isBusy() || imnode.isToRelease() || imnode.isDown());
            //prevent Scheduler Error : Scheduler try to render anode already free
            if (imnode.isFree()) {
                logger.warn("[RMCORE] scheduler tried to free a node already free ! Node URL : " + nodeURL);
            } else {
                // verify that scheduler don't try to render a node detected down
                if (!imnode.isDown()) {
                    if (imnode.isToRelease()) {
                        doRelease(imnode);
                    } else {
                        setFree(imnode);
                    }
                }
            }
        } else {
            logger.warn("[RMCORE] scheduler asked to free an unknown node ! Node URL : " + nodeURL);
        }
    }

    /**
     * Free a set of nodes.
     * @param nodes a set of nodes to set free.
     */
    public void freeNodes(NodeSet nodes) {
        for (Node node : nodes)
            freeNode(node);
    }

    /**
     * Return true if ns contains the node rmn.
     * 
     * @param ns a list of nodes
     * @param rmn a RM node
     * @return true if ns contains the node rmn.
     */
    private boolean contains(NodeSet ns, RMNode rmn) {
        for (Node n : ns) {
            try {
                if (n.getNodeInformation().getURL().equals(rmn.getNodeInformation().getURL())) {
                    return true;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return false;
    }

    /**
     * Gives a set of nodes that verify a selection script.
     * This method has three way to handle the request :<BR>
     *  - if there is no script, it returns at most the
     * first nb free nodes asked.<BR>
     * - If the script is a dynamic script, the method will
     * test the resources, until nb nodes verify the script or if there is no
     * node left.<BR>
     * - If the script is a static script, it will return in priority the
     * nodes on which the given script has already been verified,
     * and test on other nodes if needed.<BR>
     *
     * @param nb number of node to provide
     * @param selectionScript selection script that nodes must verify.
     */
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript, NodeSet exclusion) {

        //if RM is in shutdown state, don't provide nodes
        if (this.toShutDown) {
            return new NodeSet();
        } else {
            ArrayList<RMNode> nodes = getNodesSortedByScript(selectionScript);
            //delete nodes that are in exclusion list.
            //TODO to be checked by cdelbe or gsigety
            if (exclusion != null && exclusion.size() > 0) {
                Iterator<RMNode> it = nodes.iterator();
                while (it.hasNext()) {
                    if (contains(exclusion, it.next())) {
                        it.remove();
                    }
                }
            }

            int found = 0;
            NodeSet result;
            //no verifying script
            if (selectionScript == null) {
                logger.info("[RMCORE] Searching for " + nb + " nodes on " + this.getSizeListFreeRMNodes() +
                    " free nodes.");
                result = new NodeSet();
                while (!nodes.isEmpty() && (found < nb.intValue())) {
                    RMNode node = nodes.remove(0);
                    try {
                        result.add(node.getNode());
                        setBusy(node);
                        found++;
                    } catch (NodeException e) {
                        setDown(node);
                    }
                }
            } else if (selectionScript.isDynamic()) {
                result = this.selectNodeWithDynamicVerifScript(nb.intValue(), selectionScript, nodes);
            } else {
                result = this.selectNodeWithStaticVerifScript(nb.intValue(), selectionScript, nodes);
            }
            return result;
        }
    }

    /**
     * Gives an exactly number of nodes
     * not yet implemented.
     * @param nb exactly number of nodes to provide.
     * @param selectionScript  that nodes must verify.
     */
    public NodeSet getExactlyNodes(IntWrapper nb, SelectionScript selectionScript) {
        // TODO
        return null;
    }

    //----------------------------------------------------------------------
    //  Methods called by RMMonitoring, override RMCoreInterface  
    //----------------------------------------------------------------------

    /**
     * Builds and returns a snapshot of RMCore's current state.
     * Initial state must be understood as a new Monitor point of view.
     * A new monitor start to receive RMCore events, so must be informed of the current
     * state of the Core at the beginning of monitoring.
     * @return RMInitialState containing nodes and nodeSources of the RMCore.
     */
    public RMInitialState getRMInitialState() {
        ArrayList<RMNodeEvent> freeNodesList = new ArrayList<RMNodeEvent>();
        for (RMNode imnode : this.freeNodes) {
            freeNodesList.add(imnode.getNodeEvent());
        }

        ArrayList<RMNodeEvent> busyNodesList = new ArrayList<RMNodeEvent>();
        for (RMNode imnode : this.busyNodes) {
            busyNodesList.add(imnode.getNodeEvent());
        }

        ArrayList<RMNodeEvent> toReleaseNodesList = new ArrayList<RMNodeEvent>();
        for (RMNode imnode : this.toBeReleased) {
            toReleaseNodesList.add(imnode.getNodeEvent());
        }

        ArrayList<RMNodeEvent> downNodeslist = new ArrayList<RMNodeEvent>();
        for (RMNode imnode : this.downNodes) {
            downNodeslist.add(imnode.getNodeEvent());
        }

        ArrayList<RMNodeSourceEvent> nodeSourcesList = new ArrayList<RMNodeSourceEvent>();
        for (NodeSource s : this.nodeSources.values()) {
            nodeSourcesList.add(s.getSourceEvent());
        }

        return new RMInitialState(freeNodesList, busyNodesList, toReleaseNodesList, downNodeslist,
            nodeSourcesList);
    }

    //----------------------------------------------------------------------
    //  Methods called by RMFactory, override RMCoreInterface  
    //----------------------------------------------------------------------    

    /**
     * Returns the stub of RMAdmin ProActive object.
     * @return the RMAdmin ProActive object.
     */
    public RMAdmin getAdmin() {
        return this.admin;
    }

    /**
     * Returns the stub of RMMonitoring ProActive object.
     * @return the RMMonitoring ProActive object.
     */
    public RMMonitoring getMonitoring() {
        return this.monitoring;
    }

    /**
     * Returns the stub of RMUser ProActive object.
     * @return the RMUser ProActive object.
     */
    public RMUser getUser() {
        return this.user;
    }

    //----------------------------------------------------------------------
    //  Methods called by NodeSource objects, override RMNodeManagerSourceInt  
    //----------------------------------------------------------------------

    /**
     * add a NodeSource to the core with its Id.
     * NodeSource is registered in the RMcore.
     * @param source Stub of the {@link NodeSource} object to add.
     * @param sourceId name of the {@link NodeSource} object to add.
     */
    public void internalAddSource(NodeSource source, String sourceId) {
        this.nodeSources.put(sourceId, source);
        //create the event
        this.monitoring.nodeSourceAddedEvent(source.getSourceEvent());
    }

    /**
     * Removes a NodeSource to the core.
     * Nodes source confirms to RMCore by this call its removal.
     * RMCore has previously asked to the source to shutdown,
     * and NodeDource has removed its nodes.
     * RMcore deletes the nodeSource from its source list.
     * @param sourceId name of the {@link NodeSource} to remove.
     * @param evt Remove source event to throw at RMMonitoring
     */
    public void internalRemoveSource(String sourceId, RMNodeSourceEvent evt) {
        this.nodeSources.remove(sourceId);
        if (logger.isInfoEnabled()) {
            logger.info("[RMCORE] Node Source removed : " + sourceId);
        }
        //create the event
        this.monitoring.nodeSourceRemovedEvent(evt);

        if ((this.nodeSources.size() == 0) && this.toShutDown) {
            //all nodes sources has been removed and RMCore in shutdown state, 
            //finish the shutdown 
            this.user.shutdown();
            this.monitoring.shutdown();
            PAActiveObject.terminateActiveObject(true);
            try {
                Thread.sleep(2000);
                this.nodeRM.getProActiveRuntime().killRT(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** add a new node to the node Manager.
     * The new node is available for jobs execution.
     * Creates the RMNode object corresponding to the node
     * Add the node to the RMCore node list.
     * set the node to free state.
     * Throws to {@link RMMonitoring} new node's event.
     * @param node {@link Node} object to add.
     * @param VNodeName Virtual node name of the node.
     * @param PADName ProActive descriptor name of the node.
     * @param nodeSource Stub of the {@link NodeSource} object that handle the node.
     */
    public void internalAddNode(Node node, String VNodeName, String PADName, NodeSource nodeSource) {
        RMNode imnode = new RMNodeImpl(node, VNodeName, PADName, nodeSource);
        try {
            imnode.clean();
            imnode.setFree();
            this.freeNodes.add(imnode);
            this.allNodes.put(imnode.getNodeURL(), imnode);
            //create the event
            this.monitoring.nodeAddedEvent(imnode.getNodeEvent());
        } catch (NodeException e) {
            // Exception on the node, we assume the node is down
            e.printStackTrace();
        }
        if (logger.isInfoEnabled()) {
            logger.info("[RMCORE] New node added, node ID is : " + imnode.getNodeURL() + ", node Source : " +
                nodeSource.getSourceId());
        }
    }

    /**
     * Removes a node from the Core.
     * Access point for a node source to remove node.
     * RMCore confirm after to the NodeSource the removing.
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void internalRemoveNode(String nodeUrl, boolean preempt) {
        RMNode imnode = getNodebyUrl(nodeUrl);
        if (preempt) {
            imnode.clean();
            this.removeNodeFromCore(imnode);
            imnode.getNodeSource().confirmRemoveNode(nodeUrl);
        } else { //softly way
            this.releaseNode(imnode);
        }
    }

    /**
     * Informs the RMCore that a node is down.
     * RMcore set the node to down state.
     * @param nodeUrl URL of the down node.
     */
    public void setDownNode(String nodeUrl) {
        RMNode imnode = getNodebyUrl(nodeUrl);
        if (imnode != null) {
            this.setDown(imnode);
        }
    }
}
