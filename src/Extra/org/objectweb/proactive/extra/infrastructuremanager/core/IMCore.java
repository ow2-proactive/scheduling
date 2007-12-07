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
package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.event.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.event.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.common.event.IMNodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.event.IMNodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.exception.AddingNodesException;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdminImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoringImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUserImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeComparator;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeImpl;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DummyNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * The main active object of the infrastructure Manager (IM),
 * the IMCore has to provide nodes to a scheduler.
 *
 * The IMCore functions are :<BR>
 *  - Create infrastructure Manager's active objects at its initialization ;
 *  {@link IMAdmin}, {@link IMUser}, {@link IMMonitoring}.<BR>
 *  - keep an up-to-date list of nodes able to perform scheduler's tasks.<BR>
 *  - give nodes to the Scheduler asked by {@link IMUser} object,
 *  with a node selection mechanism performed by {@link SelectionScript}.<BR>
 *  - dialog with node sources which add and remove nodes to the Core.
 *  - perform creation and removal of NodeSource objects. <BR>
 *  - treat removing nodes and adding nodes request coming from {@link IMAdmin}.
 *  - create and launch IMEvents concerning nodes and nodes Sources To IMMonitoring
 *  active object.<BR><BR>
 *
 * Nodes in Infrastructure Manager are represented by {@link IMNode objects}.
 * IMcore has to manage different states of nodes :
 * -free : node is ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of the its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR><BR>
 *
 * IMCore is not responsible of creation, acquisition and monitoring of nodes,
 * these points are performed by {@link NodeSource} objects.<BR><BR>
 * IMCore got at least one node Source created at its startup
 * (named {@link IMCore#DEFAULT_STATIC_SOURCE_NAME}),
 * which is a Static node source ({@link PADNodeSource}), able to receive
 * a {@link ProActiveDescriptor} objects and deploy them.<BR><BR>
 *
 * WARNING : you must instantiate this class as an Active Object !
 *
 * @see IMCoreInterface
 * @see IMCoreSourceInt
 *
 * @author ProActive team
 *
 */
public class IMCore implements IMCoreInterface, InitActive, IMCoreSourceInt,
    Serializable {

    /** serial version UID */
    private static final long serialVersionUID = -6005871512766524208L;

    /** Log4J logger name for IMCore */
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);

    /** If IMCore Active object */
    private String id;

    /** ProActive Node containing the IMCore */
    private Node nodeIM;

    /** stub of IMAdmin active object of the IM */
    private IMAdmin admin;

    /** stub of IMMonitoring active object of the IM */
    private IMMonitoringImpl monitoring;

    /** stub of IMuser active object of the IM */
    private IMUser user;

    /** HashMap of NodeSource active objects*/
    private HashMap<String, NodeSource> nodeSources;

    /** HashMaps of nodes known by the IMCore*/
    private HashMap<String, IMNode> allNodes;

    /** list of all free nodes*/
    private ArrayList<IMNode> freeNodes;

    /** list of all busy nodes*/
    private ArrayList<IMNode> busyNodes;

    /** list of all down nodes*/
    private ArrayList<IMNode> downNodes;

    /** list of all 'to be released' nodes*/
    private ArrayList<IMNode> toBeReleased;

    /** Timeout for selection script result */
    private static final int MAX_VERIF_TIMEOUT = 120000;

    /** Name of the default source node, created at the IMCore initialization */
    private static final String DEFAULT_STATIC_SOURCE_NAME = "default";

    /** indicates that IMCore must shutdown*/
    private boolean toShutDown = false;

    /**
     * ProActive Empty constructor
     */
    public IMCore() {
    }

    /**
     * Creates the IMCore object.
     * @param id Name for IMCOre.
     * @param nodeIM Name of the ProActive Node object containing
     * IM active objects.
     * @throws ActiveObjectCreationException.
     * @throws NodeException.
     */
    public IMCore(String id, Node nodeIM)
        throws ActiveObjectCreationException, NodeException {
        this.id = id;
        this.nodeIM = nodeIM;

        nodeSources = new HashMap<String, NodeSource>();
        allNodes = new HashMap<String, IMNode>();

        freeNodes = new ArrayList<IMNode>();
        busyNodes = new ArrayList<IMNode>();
        downNodes = new ArrayList<IMNode>();
        toBeReleased = new ArrayList<IMNode>();
    }

    /**
     * Initialization part of the IMCoe active object.
     * <BR>Create IM's active objects :<BR>
     * -{@link IMAdmin},<BR>
     * -{@link IMUser},<BR>
     * -{@link IMMonitoring},<BR>
     * and creates the default static Node Source named {@link IMCore#DEFAULT_STATIC_SOURCE_NAME}.
     * Finally throws the IM started event.
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore start : initActivity");
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("active object IMAdmin");
            }

            admin = (IMAdminImpl) ProActiveObject.newActive(IMAdminImpl.class.getName(),
                    new Object[] { ProActiveObject.getStubOnThis() }, nodeIM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object IMMonitoring");
            }
            monitoring = (IMMonitoringImpl) ProActiveObject.newActive(IMMonitoringImpl.class.getName(),
                    new Object[] { ProActiveObject.getStubOnThis() }, nodeIM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object IMUser");
            }

            user = (IMUserImpl) ProActiveObject.newActive(IMUserImpl.class.getName(),
                    new Object[] { ProActiveObject.getStubOnThis() }, nodeIM);

            if (logger.isDebugEnabled()) {
                logger.debug("instanciation IMNodeManager");
            }

            this.createStaticNodesource(null, DEFAULT_STATIC_SOURCE_NAME);

            //Creating IM started event 
            this.monitoring.imStartedEvent(new IMEvent());
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore end : initActivity");
        }
    }

    /**
     * Returns a node object to a corresponding URL.
     * @param url url of the node asked.
     * @return IMNode object containing the node.
     */
    private IMNode getNodebyUrl(String url) {
        assert allNodes.containsKey(url);
        return allNodes.get(url);
    }

    /**
     * Set a node's state to free, after a completed task by it.
     * Set the to free and move the node to the internal free nodes list.
     * An event informing the node state's change is thrown to IMMonitoring.
     * @param imnode node to set free.
     */
    private void setFree(IMNode imnode) {
        //the node can only come from a busy state
        assert imnode.isBusy();
        assert this.busyNodes.contains(imnode);
        try {
            imnode.clean(); // cleaning the node, kill all active objects
            this.busyNodes.remove(imnode);
            imnode.setFree();
            this.freeNodes.add(imnode);
        } catch (NodeException e) {
            // Exception on the node, we assume the node is down
            e.printStackTrace();
        }

        //set all dynamic script results to the state of ALREADY_VERIFIED_SCRIPT
        HashMap<SelectionScript, Integer> verifs = imnode.getScriptStatus();
        for (Entry<SelectionScript, Integer> entry : verifs.entrySet()) {
            if (entry.getKey().isDynamic() &&
                    (entry.getValue() == IMNode.VERIFIED_SCRIPT)) {
                entry.setValue(IMNode.ALREADY_VERIFIED_SCRIPT);
            }
        }
        //create the event
        this.monitoring.nodeFreeEvent(imnode.getNodeEvent());
    }

    /**
     * Set a node state to busy.
     * Set the node to busy, and move the node to the internal busy nodes list.
     * An event informing the node state's change is thrown to IMMonitoring.
     * @param imnode node to set
     */
    private void setBusy(IMNode imnode) {
        assert imnode.isFree();
        assert this.freeNodes.contains(imnode);
        imnode.clean();
        try {
            imnode.setBusy();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
        this.freeNodes.remove(imnode);
        busyNodes.add(imnode);
        //create the event
        this.monitoring.nodeBusyEvent(imnode.getNodeEvent());
    }

    /**
     * Set a node state to 'to be released'.
     * mark the node toRelease, and move the node to the internal 'to be released' nodes list.
     * An event informing the node state's change is thrown to IMMonitoring.
     * @param imnode node to set.
     */
    private void setToRelease(IMNode imnode) {
        //the node can only come from a busy state
        assert imnode.isBusy();
        assert this.busyNodes.contains(imnode);
        this.busyNodes.remove(imnode);
        this.toBeReleased.add(imnode);
        try {
            imnode.setToRelease();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
        //create the event
        this.monitoring.nodeToReleaseEvent(imnode.getNodeEvent());
    }

    /**
     * Set a node state to down.
     * Set the node down, and move the node to the internal down nodes list.
     * An event informing the node state's change is thrown to IMMonitoring
     */
    private void setDown(IMNode imnode) {
        logger.info("[IMCORE] down node : " + imnode.getNodeURL() +
            ", from Source : " + imnode.getNodeSourceId());
        assert (this.busyNodes.contains(imnode) ||
        this.freeNodes.contains(imnode) || this.toBeReleased.contains(imnode));
        removeFromAllLists(imnode);
        this.downNodes.add(imnode);
        imnode.setDown();
        //create the event
        this.monitoring.nodeDownEvent(imnode.getNodeEvent());
    }

    /**
     * Internal actions to prepare release of a node.
     * A NodeSource has asked to release a node in a 'softly' way.
     * If the node is free, release immediately the node
     * else mark node's state  to 'to be released',
     * so the node will be released when scheduler will render the node
     */
    private void releaseNode(IMNode imnode) {
        assert (imnode.isFree() || imnode.isBusy());
        assert ((imnode.isFree() && this.freeNodes.contains(imnode)) ||
        (imnode.isBusy() && this.busyNodes.contains(imnode)));
        if (logger.isInfoEnabled()) {
            logger.info("[IMCORE] prepare to release node " +
                imnode.getNodeURL());
        }
        if (imnode.isFree()) {
            doRelease(imnode);
        } else {
            setToRelease(imnode);
        }
    }

    /**
     * Performs an IMNode release from the Core
     * At this point the node is at free or 'to be released' state.
     * do the release, and confirm to NodeSource the removal.
     * @param imnode the node to release
     */
    private void doRelease(IMNode imnode) {
        assert imnode.isFree() || imnode.isToRelease();
        assert ((imnode.isFree() && this.freeNodes.contains(imnode)) ||
        (imnode.isToRelease() && this.toBeReleased.contains(imnode)));

        if (logger.isInfoEnabled()) {
            logger.info("[IMCORE] doing release of node " +
                imnode.getNodeURL());
        }
        removeNodeFromCore(imnode);
        imnode.getNodeSource().confirmRemoveNode(imnode.getNodeURL());
    }

    /**
     * Internal operations to remove the node from Core.
     * IMNode object is removed from {@link IMCore#allNodes},
     * removal Node event is thrown to IMMonitoring Active object.
     * @param imnode the node to remove.
     */
    private void removeNodeFromCore(IMNode imnode) {
        //removing the node from the HM list		
        this.removeFromAllLists(imnode);
        this.allNodes.remove(imnode.getNodeURL());
        //create the event      
        this.monitoring.nodeRemovedEvent(imnode.getNodeEvent());
    }

    /**
     * Remove the IMNode object from all the lists it can appears.
     * @param imnode the node to be removed
     * @return true if the node has been removed form one list, false otherwise
     */

    // TODO gsigety, cdelbe TO BE REMOVED ?
    private boolean removeFromAllLists(IMNode imnode) {
        boolean free = this.freeNodes.remove(imnode);
        boolean busy = this.busyNodes.remove(imnode);
        boolean toBeReleased = this.toBeReleased.remove(imnode);
        boolean down = this.downNodes.remove(imnode);
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
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMCoreInterface#getNodesByScript(org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript)
     */
    private ArrayList<IMNode> getNodesSortedByScript(SelectionScript script) {
        ArrayList<IMNode> result = new ArrayList<IMNode>();
        for (IMNode imnode : this.freeNodes) {
            result.add(imnode);
        }
        if ((script != null)) {
            Collections.sort(result, new IMNodeComparator(script));
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
    private NodeSet selectNodeWithStaticVerifScript(int nb,
        SelectionScript selectionScript, ArrayList<IMNode> nodes) {
        NodeSet result = new NodeSet();
        int found = 0;

        logger.info("[IMCORE] Searching for " + nb +
            " nodes  with static verif script on " +
            this.getSizeListFreeIMNode() + " free nodes.");
        //select nodes where the static script has already be launched and satisfied
        while (!nodes.isEmpty() && (found < nb)) {
            IMNode node = nodes.remove(0);
            if (node.getScriptStatus().containsKey(selectionScript) &&
                    node.getScriptStatus().get(selectionScript)
                            .equals(IMNode.VERIFIED_SCRIPT)) {
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
        Vector<IMNode> nodeResults = new Vector<IMNode>();
        int launched = found;

        //if other nodes needed, launching the script on nodes Remaining 
        while (!nodes.isEmpty() && (launched++ < nb)) {
            nodeResults.add(nodes.get(0));
            ScriptResult<Boolean> sr = nodes.get(0)
                                            .executeScript(selectionScript);

            // if r is not a future, the script has not been executed
            if (MOP.isReifiedObject(sr)) {
                scriptResults.add(sr);
            } else {
                // script has not been executed on remote host
                // nothing to do, just let the node in the free list
                logger.info("Error occured executing verifying script",
                    sr.getException());
            }
            nodes.remove(0);
        }

        // Recupere les resultats
        do {
            try {
                int idx = ProFuture.waitForAny(scriptResults, MAX_VERIF_TIMEOUT);
                IMNode imnode = nodeResults.remove(idx);
                ScriptResult<Boolean> res = scriptResults.remove(idx);
                if (res.errorOccured()) {
                    // nothing to do, just let the node in the free list
                    logger.info("Error occured executing verifying script",
                        res.getException());
                } else if (res.getResult()) {
                    // Result OK
                    try {
                        result.add(imnode.getNode());
                        setBusy(imnode);
                        imnode.setVerifyingScript(selectionScript);
                        found++;
                    } catch (NodeException e) {
                        setDown(imnode);
                        // try on a new node if any
                        if (!nodes.isEmpty()) {
                            nodeResults.add(nodes.get(0));
                            scriptResults.add(nodes.remove(0)
                                                   .executeScript(selectionScript));
                        }
                    }
                } else {
                    // result is false
                    imnode.setNotVerifyingScript(selectionScript);
                    // try on a new node if any
                    if (!nodes.isEmpty()) {
                        nodeResults.add(nodes.get(0));
                        scriptResults.add(nodes.remove(0)
                                               .executeScript(selectionScript));
                    }
                }
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                // Wait For Any Timeout...
                // traitement special
                e.printStackTrace();
            }
        } while ((!scriptResults.isEmpty() || !nodes.isEmpty()) &&
                (found < nb));

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
    private NodeSet selectNodeWithDynamicVerifScript(int nb,
        SelectionScript selectionScript, ArrayList<IMNode> nodes) {
        logger.info("[IMCORE] Searching for " + nb +
            " nodes  with dynamic verif script on " +
            this.getSizeListFreeIMNode() + " free nodes.");

        StringBuffer order = new StringBuffer();
        for (IMNode n : nodes) {
            order.append(n.getHostName() + " ");
        }
        logger.info("[IMCORE] Available nodes are : " + order);
        Vector<ScriptResult<Boolean>> scriptResults = new Vector<ScriptResult<Boolean>>();
        Vector<IMNode> nodeResults = new Vector<IMNode>();
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
                logger.info("Error occured executing verifying script",
                    r.getException());
            }
            nodes.remove(0);
        }

        // testing script results
        do {
            try {
                int idx = ProFuture.waitForAny(scriptResults, MAX_VERIF_TIMEOUT);

                // idx could be -1 if an error occured in wfa (or timeout
                // expires)
                IMNode imnode = nodeResults.remove(idx);
                ScriptResult<Boolean> res = scriptResults.remove(idx);
                if (res.errorOccured()) {
                    // nothing to do, just let the node in the free list
                    logger.info("Error occured executing verifying script",
                        res.getException());
                } else if (res.getResult()) {
                    // Result OK
                    try {
                        result.add(imnode.getNode());
                        setBusy(imnode);
                        imnode.setVerifyingScript(selectionScript);
                        found++;
                    } catch (NodeException e) {
                        setDown(imnode);
                        // try on a new node if any
                        if (!nodes.isEmpty()) {
                            nodeResults.add(nodes.get(0));
                            scriptResults.add(nodes.remove(0)
                                                   .executeScript(selectionScript));
                        }
                    }
                } else {
                    // result is false
                    imnode.setNotVerifyingScript(selectionScript);
                    // try on a new node if any
                    if (!nodes.isEmpty()) {
                        nodeResults.add(nodes.get(0));
                        scriptResults.add(nodes.remove(0)
                                               .executeScript(selectionScript));
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // Wait For Any Timeout...
                // traitement special
                e.printStackTrace();
            }
        } while ((!scriptResults.isEmpty() || !nodes.isEmpty()) &&
                (found < nb));
        return result;
    }

    /**
     * Gives a String representing of the IMCore's ID.
     * @return String representing of the IMCore's ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Creates a static node source Active Object.
     * Creates a new static node source which is a {@link PADNodeSource} active object.
     * @param pad a ProActiveDescriptor object to deploy at the node source creation.
     * @param sourceName name given to the static node source.
     */
    public void createStaticNodesource(ProActiveDescriptor pad,
        String sourceName) {
        logger.info("[IMCORE] Creating a Static source : " + sourceName);
        try {
            NodeSource padSource = (NodeSource) ProActiveObject.newActive(PADNodeSource.class.getName(),
                    new Object[] {
                        sourceName,
                        (IMCoreSourceInt) ProActiveObject.getStubOnThis()
                    }, nodeIM);

            if (pad != null) {
                padSource.addNodes(pad);
            }
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AddingNodesException e) {
            e.printStackTrace();
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
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice,
        int ttr, Vector<String> peerUrls) {
        logger.info("[IMCORE] Creating a P2P source " + id);
        try {
            ProActiveObject.newActive(P2PNodeSource.class.getName(),
                new Object[] {
                    id, (IMCoreSourceInt) ProActiveObject.getStubOnThis(),
                    nbMaxNodes, nice, ttr, peerUrls
                }, nodeIM);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a dummy node source to test a {@link DynamicNodeSource} active object
     * @param id name of the dynamic node source to create.
     * @param nbMaxNodes max number of number the NodeSource has to provide.
     * @param nice nice time in ms, time to wait between a node remove and a new node acquisition.
     * @param ttr Time to release in ms, time during the node will be kept by the Nodesource and the Core.
     */
    public void createDummyNodeSource(String id, int nbMaxNodes, int nice,
        int ttr) {
        logger.info("[IMCORE] Creating a Dummy node source " + id);
        try {
            ProActiveObject.newActive(DummyNodeSource.class.getName(),
                new Object[] {
                    id, (IMCoreSourceInt) ProActiveObject.getStubOnThis(),
                    nbMaxNodes, nice, ttr
                }, nodeIM);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /**
     * add nodes to a static Node Source.
     * ask to a static Node source to deploy a ProActiveDescriptor.
     * nodes deployed will be added after to IMCore, by the NodeSource itself.
     * @param pad ProActiveDescriptor to deploy
     * @param sourceName name of an existing PADNodesource
     */
    public void addNodes(ProActiveDescriptor pad, String sourceName) {
        if (this.nodeSources.containsKey(sourceName)) {
            try {
                this.nodeSources.get(sourceName).addNodes(pad);
            } catch (AddingNodesException e) {
                e.printStackTrace();
            }
        } else {
            addNodes(pad);
        }
    }

    /**
     * add nodes to the default static Node Source.
     * ask to the default static Node source to deploy a ProActiveDescriptor.
     * nodes deployed will be added after to IMCore, by the NodeSource itself.
     * @param pad ProActiveDescriptor to deploy
     */
    public void addNodes(ProActiveDescriptor pad) {
        try {
            this.nodeSources.get(DEFAULT_STATIC_SOURCE_NAME).addNodes(pad);
        } catch (AddingNodesException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a node from the Core and from its node source.
     * perform the removing request of a node
     * asked by {@link IMAdmin} active object.<BR><BR>
     *
     * If the node is down, node is just removed from the Core, and nothing is asked to its related NodeSource,
     * because the node source has already detected the node down (it is its function), informed the IMCore,
     * and removed the node from its list.<BR>
     * Else the removing request is just forwarded to the corresponding NodeSource of the node.<BR><BR>
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy.
     * False the node is removed just after the job ending if the node is busy, or immediately if free
     */
    public void removeNode(String nodeUrl, boolean preempt) {
        if (this.allNodes.containsKey(nodeUrl)) {
            IMNode imnode = this.allNodes.get(nodeUrl);

            //if node already down, just removing from down list and global list
            //Node sources have already removed the node because they have detected the node passing down
            if (imnode.isDown()) {
                assert this.downNodes.contains(imnode);
                this.removeNodeFromCore(imnode);
            } else {
                this.allNodes.get(nodeUrl).getNodeSource()
                             .forwardRemoveNode(nodeUrl, preempt);
            }
        }
    }

    /**
     * Stops the Infrastructure Manager.
     * Stops all {@link NodeSource} active objects
     * Stops {@link IMAdmin}, {@link IMUser}, {@link IMMonitoring} active objects.
     */
    public void shutdown(boolean preempt) {
        this.toShutDown = true;
        this.monitoring.imShuttingDownEvent(new IMEvent());
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            System.out.println("IMCore.shutdown() source : " + entry.getKey());
            entry.getValue().shutdown(preempt);
        }
    }

    /**
     * Stops and remove a NodeSource active object and remove its nodes from the Infrastructure Manager
     * @param sourceName name of the NodeSource object to remove.
     * @param preempt true all the nodes must be removed immediately, without waiting job ending if nodes are busy,
     * false nodes are removed just after the job ending, if busy.
     */
    public void removeSource(String sourceName, boolean preempt) {
        if (nodeSources.containsKey(sourceName)) {
            this.nodeSources.get(sourceName).shutdown(preempt);
        }

        //TODO gsigety cdelbe : throwing an exception if node source not found ?
        // possible to delete default node source ?
    }

    /**
     * Gives an array list of NodeSource objects
     * @return list of NodeSource objects of the IM.
     */
    public ArrayList<NodeSource> getNodeSources() {
        ArrayList<NodeSource> res = new ArrayList<NodeSource>();
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    /**
     * Gives number of nodes handled by the Core.
     * @return IntWrapper number of nodes in the IMCore.
     */
    public IntWrapper getNbAllIMNode() {
        return new IntWrapper(this.allNodes.size());
    }

    /**
     * Gives number of free nodes handled by the Core.
     * @return IntWrapper number of free nodes in the IMCore.
     */
    public IntWrapper getSizeListFreeIMNode() {
        return new IntWrapper(this.freeNodes.size());
    }

    /**
     * Gives number of busy nodes handled by the Core.
     * @return IntWrapper number of busy nodes in the IMCore.
     */
    public IntWrapper getSizeListBusyIMNode() {
        return new IntWrapper(this.busyNodes.size());
    }

    /**
     * Gives number of down nodes handled by the Core.
     * @return IntWrapper number of down nodes in the IMCore.
     */
    public IntWrapper getSizeListDownIMNode() {
        return new IntWrapper(this.downNodes.size());
    }

    /**
     * Gives number of 'to be released' nodes handled by the Core.
     * @return IntWrapper number of 'to be released' nodes in the IMCore.
     */
    public IntWrapper getSizeListToReleaseIMNode() {
        return new IntWrapper(this.toBeReleased.size());
    }

    /**
     * Gives the free nodes list
     * @return free nodes of the IMCore.
     */
    public ArrayList<IMNode> getListFreeIMNode() {
        return this.freeNodes;
    }

    /**
     * Gives the busy nodes list
     * @return busy nodes of the IMCore.
     */
    public ArrayList<IMNode> getListBusyIMNode() {
        return this.busyNodes;
    }

    /**
     * Gives the 'to be released' nodes list
     * @return 'to be released' nodes of the IMCore.
     */
    public ArrayList<IMNode> getListToReleasedIMNodes() {
        return this.toBeReleased;
    }

    /**
     * Gives the list of all nodes handled by th IMCore
     * @return 'to be released' nodes of the IMCore.
     */
    public ArrayList<IMNode> getListAllNodes() {
        ArrayList<IMNode> res = new ArrayList<IMNode>();
        for (Entry<String, IMNode> entry : this.allNodes.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    /**
     * Builds and returns a snapshot of IMCore's current state.
     * Initial state must be understood as a new Monitor point of view.
     * A new monitor start to receive IMCore events, so must be informed of the current
     * state of the Core at the beginning of monitoring.
     * @return IMInitialState containing nodes and nodeSources of the IMCore.
     */
    public IMInitialState getIMInitialState() {
        ArrayList<IMNodeEvent> freeNodesList = new ArrayList<IMNodeEvent>();
        for (IMNode imnode : this.freeNodes) {
            freeNodesList.add(imnode.getNodeEvent());
        }

        ArrayList<IMNodeEvent> busyNodesList = new ArrayList<IMNodeEvent>();
        for (IMNode imnode : this.busyNodes) {
            busyNodesList.add(imnode.getNodeEvent());
        }

        ArrayList<IMNodeEvent> toReleaseNodesList = new ArrayList<IMNodeEvent>();
        for (IMNode imnode : this.toBeReleased) {
            toReleaseNodesList.add(imnode.getNodeEvent());
        }

        ArrayList<IMNodeEvent> downNodeslist = new ArrayList<IMNodeEvent>();
        for (IMNode imnode : this.downNodes) {
            downNodeslist.add(imnode.getNodeEvent());
        }

        ArrayList<IMNodeSourceEvent> nodeSourcesList = new ArrayList<IMNodeSourceEvent>();
        for (NodeSource s : this.nodeSources.values()) {
            nodeSourcesList.add(s.getSourceEvent());
        }

        return new IMInitialState(freeNodesList, busyNodesList,
            toReleaseNodesList, downNodeslist, nodeSourcesList);
    }

    /**
     * Returns the ProActive Node containing the IMCore active object.
     * @return the ProActive Node containing the IMCore active object.
     */
    public Node getNodeIM() {
        return this.nodeIM;
    }

    /**
     * Returns the stub of IMAdmin ProActive object.
     * @return the IMAdmin ProActive object.
     */
    public IMAdmin getAdmin() {
        return this.admin;
    }

    /**
     * Returns the stub of IMMonitoring ProActive object.
     * @return the IMMonitoring ProActive object.
     */
    public IMMonitoring getMonitoring() {
        return this.monitoring;
    }

    /**
     * Returns the stub of IMUser ProActive object.
     * @return the IMUser ProActive object.
     */
    public IMUser getUser() {
        return this.user;
    }

    /**
    * free a node after a work.
    * IMUser active object wants to free a node that ended a task.
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
            logger.debug("A Runtime exception occured " +
                "while obtaining information on the node," +
                "the node must be down (it will be detected later)", e);
            // node is down,
            // will be detected later
            return;
        }

        //verify wether the node has not been removed from the IM
        if (this.allNodes.containsKey(nodeURL)) {
            IMNode imnode = this.getNodebyUrl(nodeURL);
            assert (imnode.isBusy() || imnode.isToRelease() || imnode.isDown());
            // verify that scheduler don't try to render a node detected down
            if (!imnode.isDown()) {
                if (imnode.isToRelease()) {
                    doRelease(imnode);
                } else {
                    setFree(imnode);
                }
            }
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
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript) {
        //if IM is in shutdown state, don't provide nodes
        if (this.toShutDown) {
            return new NodeSet();
        } else {
            ArrayList<IMNode> nodes = getNodesSortedByScript(selectionScript);
            int found = 0;
            NodeSet result;

            //no verifying script
            if (selectionScript == null) {
                logger.info("[IMCORE] Searching for " + nb + " nodes on " +
                    this.getSizeListFreeIMNode() + " free nodes.");
                result = new NodeSet();
                while (!nodes.isEmpty() && (found < nb.intValue())) {
                    IMNode node = nodes.remove(0);
                    try {
                        result.add(node.getNode());
                        setBusy(node);
                        found++;
                    } catch (NodeException e) {
                        setDown(node);
                    }
                }
            } else if (selectionScript.isDynamic()) {
                result = this.selectNodeWithDynamicVerifScript(nb.intValue(),
                        selectionScript, nodes);
            } else {
                result = this.selectNodeWithStaticVerifScript(nb.intValue(),
                        selectionScript, nodes);
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
    public NodeSet getExactlyNodes(IntWrapper nb,
        SelectionScript selectionScript) {
        // TODO gsigety to implement
        return null;
    }

    //----------------------------------------------------------------------
    //  Methods called by NodeSource objects, override IMNodeManagerSourceInt  
    //----------------------------------------------------------------------
    //

    /**
    * add a NodeSource to the core with its Id.
    * NodeSource is registered in the IMcore.
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
     * Nodes source confirms to IMCore by this call its removal.
     * IMCore has previously asked to the source to shutdown,
     * and NodeDource has removed its nodes.
     * IMcore deletes the nodeSource from its source list.
     * @param sourceId name of the {@link NodeSource} to remove.
     * @param evt Remove source event to throw at IMMonitoring
     */
    public void internalRemoveSource(String sourceId, IMNodeSourceEvent evt) {
        //IMNodeSourceEvent evt = nodeSources.get(sourceId).getSourceEvent();
        this.nodeSources.remove(sourceId);
        if (logger.isInfoEnabled()) {
            logger.info("[IMCORE] Node Source removed : " + sourceId);
        }
        //create the event
        this.monitoring.nodeSourceRemovedEvent(evt);

        if ((this.nodeSources.size() == 0) && this.toShutDown) {
            //all nodes sources has been removed and IMCore in shutdown state, 
            //finish the shutdown 
            this.user.shutdown();
            this.monitoring.shutdown();
            ProActiveObject.terminateActiveObject(false);
        }
    }

    /** add a new node to the node Manager.
     * The new node is available for jobs execution.
     * Creates the IMNode object corresponding to the node
     * Add the node to the IMCore node list.
     * set the node to free state.
     * Throws to {@link IMMonitoring} new node's event.
     * @param node {@link Node} object to add.
     * @param VNodeName Virtual node name of the node.
     * @param PADName ProActive descriptor name of the node.
     * @param nodeSource Stub of the {@link NodeSource} object that handle the node.
     */
    public void internalAddNode(Node node, String VNodeName, String PADName,
        NodeSource nodeSource) {
        IMNode imnode = new IMNodeImpl(node, VNodeName, PADName, nodeSource);
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
            logger.info("[IMCORE] New node added, node ID is : " +
                imnode.getNodeURL() + ", node Source : " +
                nodeSource.getSourceId());
        }
    }

    /**
     * Removes a node from the Core.
     * Access point for a node source to remove node.
     * IMCore confirm after to the NodeSource the removing.
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void internalRemoveNode(String nodeUrl, boolean preempt) {
        IMNode imnode = getNodebyUrl(nodeUrl);
        if (preempt) {
            imnode.clean();
            this.removeNodeFromCore(imnode);
            imnode.getNodeSource().confirmRemoveNode(nodeUrl);
        } else { //softly way
            this.releaseNode(imnode);
        }
    }

    /**
     * Informs the IMCore that a node is down.
     * IMcore set the node to down state.
     * @param nodeUrl URL of the down node.
     */
    public void setDownNode(String nodeUrl) {
        IMNode imnode = getNodebyUrl(nodeUrl);
        if (imnode != null) {
            this.setDown(imnode);
        }
    }
}
