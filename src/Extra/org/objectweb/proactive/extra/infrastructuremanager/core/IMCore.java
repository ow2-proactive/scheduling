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
import java.util.ListIterator;
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
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMDataResource;
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
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * Implementation of the {@link IMDataResource} interface, using a
 * {@link IMNodeSource} that provides the nodes to handle.
 *
 * @author proactive team
 *
 */
public class IMCore implements IMCoreInterface, InitActive, IMCoreSourceInt,
    Serializable {

    /**  */
    private static final long serialVersionUID = -6005871512766524208L;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);
    private String id;

    // Attributes
    private Node nodeIM;
    private IMAdmin admin;
    private IMMonitoringImpl monitoring;
    private IMUser user;

    // test mkris
    IMActivityNode act;

    /* HashMaps of nodes and nodeSource */
    private HashMap<String, NodeSource> nodeSources;
    private HashMap<String, IMNode> allNodes;

    /* list of nodes sorted by states */
    private ArrayList<IMNode> freeNodes;
    private ArrayList<IMNode> busyNodes;
    private ArrayList<IMNode> downNodes;
    private ArrayList<IMNode> toBeReleased;
    private static final int MAX_VERIF_TIMEOUT = 120000;
    private static final String DEFAULT_STATIC_SOURCE_NAME = "default";

    // ----------------------------------------------------------------------//
    // CONSTRUCTORS
    /**
         * Empty constructor
         */
    public IMCore() {
    }

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

            act = new IMActivityNode((IMCoreInterface) (ProActiveObject.getStubOnThis()));
            new Thread(act).start();

            this.createStaticNodesource(null, DEFAULT_STATIC_SOURCE_NAME);

            //Creating IM started event 
            this.monitoring.imStartedEvent();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore end : initActivity");
        }
    }

    // ----------------------------------------------------------------------//
    // private methods
    // ----------------------------------------------------------------------//
    private IMNode getNodebyUrl(String url) {
        assert allNodes.containsKey(url);
        return allNodes.get(url);
    }

    /**
     * Set the free state after a task completed on a node, and move the node to the internal free list.
     * verifying if the node must be released to its node Source
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMCoreInterface#setFree(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    private void setFree(IMNode imnode) {
        //the node can only come from a busy state
        assert imnode.isBusy();
        assert this.busyNodes.contains(imnode);
        try {
            imnode.clean(); // cleaning the node, kill all active objects
            this.removeFromAllLists(imnode);
            imnode.setFree();
            this.freeNodes.add(imnode);
            //create the event
            this.monitoring.nodeFreeEvent(imnode.getNodeEvent());
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
    }

    /**
     * Set the busy state, and move the node to the internal busy list.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMCoreInterface#setBusy(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    private void setBusy(IMNode imnode) {
        assert imnode.isFree();
        assert this.freeNodes.contains(imnode);
        imnode.clean();
        removeFromAllLists(imnode);
        busyNodes.add(imnode);
        //create the event
        this.monitoring.nodeBusyEvent(imnode.getNodeEvent());
        try {
            imnode.setBusy();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
    }

    /**
     * mark the node toRelease
     *
     */
    private void setToRelease(IMNode imnode) {
        //the node can only come from a busy state
        assert imnode.isBusy();
        assert this.busyNodes.contains(imnode);
        this.removeFromAllLists(imnode);
        this.toBeReleased.add(imnode);
        //create the event
        this.monitoring.nodeToReleaseEvent(imnode.getNodeEvent());
        try {
            imnode.setToRelease();
        } catch (NodeException e1) {
            // A down node shouldn't be busied...
            e1.printStackTrace();
        }
    }

    /**
     * Set the down state, and move the node to the internal down list.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMCoreInterface#setDown(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    private void setDown(IMNode imnode) {
        logger.info("[IMCORE] down node : " + imnode.getNodeURL() +
            ", from Source : " + imnode.getNodeSourceId());
        removeFromAllLists(imnode);
        this.downNodes.add(imnode);
        //create the event
        this.monitoring.nodeDownEvent(imnode.getNodeEvent());
        imnode.setDown();
    }

    /**
     * a NodeSource has asked to release a node
     * if the node is free, releasing immediately the node
     * else mark the node to release, so the node will be released when scheduler will render the node
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
     * doing the node soft release, imnode removed from imnode list and confirm to NodeSource
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
     * doing internal operation to remove the node from Core
     */
    private void removeNodeFromCore(IMNode imnode) {
        //removing the node from the HM list		
        this.removeFromAllLists(imnode);
        this.allNodes.remove(imnode.getNodeURL());
        //create the event
        this.monitoring.nodeRemovedEvent(imnode.getNodeEvent());
    }

    /**
     * Remove the imnode from all the lists it can appears.
     * @param imnode
     * @return
     */
    private boolean removeFromAllLists(IMNode imnode) {
        // Free
        boolean free = this.freeNodes.remove(imnode);

        // Busy
        boolean busy = this.busyNodes.remove(imnode);

        // toBeReleased
        boolean toBeReleased = this.toBeReleased.remove(imnode);

        // down
        boolean down = this.downNodes.remove(imnode);

        return free || busy || toBeReleased || down;
    }

    /**
     * Return the nodes in a specific order :
     * - if there is no script to verify, just return the free nodes ;
     * - if there is a script, tries to give the nodes in an efficient order :
     *                 -> First the nodes that verified the script before ;
     *                 -> Next, the nodes that haven't been tested ;
     *                 -> Next, the nodes that have allready verified the script, but no longer ;
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
                // int idx = ProActive.waitForAny(scriptResults,
                // MAX_VERIF_TIMEOUT);
                int idx = ProFuture.waitForAny(scriptResults);

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

    // ----------------------------------------------------------------------//
    // methods called by IMAdmin, override interface IMCore
    // ----------------------------------------------------------------------//
    public String getId() {
        return this.id;
    }

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
                padSource.addNodes(pad, pad.getUrl());
            }
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AddingNodesException e) {
            e.printStackTrace();
        }
    }

    //temporary method, to remove when dynamic source creation methds will be defined
    public void createP2PNodeSource(String id, int nbMaxNodes, int nice,
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

    //temporary method, to remove when dynamic source creation methds will be defined
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

    public void addNodes(ProActiveDescriptor pad, String sourceName) {
        if (this.nodeSources.containsKey(sourceName)) {
            try {
                this.nodeSources.get(sourceName).addNodes(pad, pad.getUrl());
            } catch (AddingNodesException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNodes(ProActiveDescriptor pad) {
        try {
            this.nodeSources.get(DEFAULT_STATIC_SOURCE_NAME)
                            .addNodes(pad, pad.getUrl());
        } catch (AddingNodesException e) {
            e.printStackTrace();
        }
    }

    public void removeNode(String nodeUrl, boolean killNode) {
        if (this.allNodes.containsKey(nodeUrl)) {
            IMNode imnode = this.allNodes.get(nodeUrl);

            //if node already down, just removing from down list and global list
            //Node sources have already removed the node because they have detected the node passing down
            if (imnode.isDown()) {
                assert this.downNodes.contains(imnode);
                this.removeNodeFromCore(imnode);
            } else {
                this.allNodes.get(nodeUrl).getNodeSource()
                             .forwardRemoveNode(nodeUrl, killNode);
            }
        }
    }

    public BooleanWrapper shutdown() {
        //Germs TODO remove all NodeSources
        //kill all nodes, softly or not 
        return null;
    }

    public void removeSource(String sourceName, boolean killNodes) {
        //TODO Germs
    }

    public ArrayList<NodeSource> getNodeSources() {
        ArrayList<NodeSource> res = new ArrayList<NodeSource>();
        for (Entry<String, NodeSource> entry : this.nodeSources.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    public IntWrapper getNbAllIMNode() {
        return new IntWrapper(this.allNodes.size());
    }

    public IntWrapper getSizeListFreeIMNode() {
        return new IntWrapper(this.freeNodes.size());
    }

    public IntWrapper getSizeListBusyIMNode() {
        return new IntWrapper(this.busyNodes.size());
    }

    public IntWrapper getSizeListDownIMNode() {
        return new IntWrapper(this.downNodes.size());
    }

    public IntWrapper getSizeListToReleaseIMNode() {
        return new IntWrapper(this.toBeReleased.size());
    }

    public ArrayList<IMNode> getListFreeIMNode() {
        return this.freeNodes;
    }

    public ArrayList<IMNode> getListBusyIMNode() {
        return this.busyNodes;
    }

    public ArrayList<IMNode> getListToReleasedIMNodes() {
        return this.toBeReleased;
    }

    public ArrayList<IMNode> getListAllNodes() {
        ArrayList<IMNode> res = new ArrayList<IMNode>();
        for (Entry<String, IMNode> entry : this.allNodes.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    public IMInitialState getIMInitialState() {
        Vector<NodeEvent> freeNodesVector = new Vector<NodeEvent>();
        for (IMNode imnode : this.freeNodes) {
            freeNodesVector.add(imnode.getNodeEvent());
        }

        Vector<NodeEvent> busyNodesVector = new Vector<NodeEvent>();
        for (IMNode imnode : this.busyNodes) {
            busyNodesVector.add(imnode.getNodeEvent());
        }

        Vector<NodeEvent> toReleaseNodesVector = new Vector<NodeEvent>();
        for (IMNode imnode : this.toBeReleased) {
            toReleaseNodesVector.add(imnode.getNodeEvent());
        }

        Vector<NodeEvent> downNodesVector = new Vector<NodeEvent>();
        for (IMNode imnode : this.downNodes) {
            downNodesVector.add(imnode.getNodeEvent());
        }

        Vector<NodeSourceEvent> nodeSourcesVector = new Vector<NodeSourceEvent>();
        for (NodeSource s : this.nodeSources.values()) {
            nodeSourcesVector.add(s.getSourceEvent());
        }

        return new IMInitialState(freeNodesVector, busyNodesVector,
            toReleaseNodesVector, downNodesVector, nodeSourcesVector);
    }

    // ACCESSORS
    public Node getNodeIM() {
        return this.nodeIM;
    }

    public IMAdmin getAdmin() {
        return this.admin;
    }

    public IMMonitoring getMonitoring() {
        return this.monitoring;
    }

    public IMUser getUser() {
        return this.user;
    }

    //USER

    /**
    * release a node after a work
    * called by the Core
    * find which {@link IMNode} correspond to the {@link Node} given in
    * parameter and change its state to 'free'.
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

        //verify wether the not has not been removed to the IM
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

    public void freeNodes(NodeSet nodes) {
        for (Node node : nodes)
            freeNode(node);
    }

    public void freeNodes(VirtualNode vnode) {
        ListIterator<IMNode> iterator = this.busyNodes.listIterator();
        while (iterator.hasNext()) {
            IMNode imnode = iterator.next();
            if (imnode.getVNodeName().equals(vnode.getName())) {
                setFree(imnode);
            }
        }
    }

    /**
         * The {@link #getAtMostNodes(IntWrapper, VerifyingScript)} method has three
         * way to handle the request : if there is no script, it returns at most the
         * first nb free nodes. If the script is a dynamic script, the method will
         * test the resources, until nb nodes verifies the script or if there is no
         * node left. In the case of a static script, it will return in priority the
         * nodes on which the given script has already been verified.
         */
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript) {
        ArrayList<IMNode> nodes = getNodesSortedByScript(selectionScript);
        NodeSet result;
        int found = 0;

        // no verifying script
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

    public NodeSet getExactlyNodes(IntWrapper nb,
        SelectionScript selectionScript) {
        // TODO Auto-generated method stub
        return null;
    }

    //----------------------------------------------------------------------
    //  Methods called by NodeSource objects, override IMNodeManagerSourceInt  
    //----------------------------------------------------------------------
    //

    /**
     * adding a NodeSource to the core with its Id
     */
    public void addSource(NodeSource source, String sourceId) {
        this.nodeSources.put(sourceId, source);
        System.out.println("IMCore.addSource() " + sourceId);
        //create the event
        this.monitoring.nodeSourceAddedEvent(source.getSourceEvent());
    }

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

    /** release a node as soon as possible
    * if the node is busy, waiting the job end
    * a call back is awaited by the node source to confirm this node unregistering
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

    public void setDownNode(String nodeUrl) {
        System.out.println("IMCoreImpl.setDownNode() node Url" + nodeUrl);
        IMNode imnode = getNodebyUrl(nodeUrl);
        if (imnode != null) {
            this.setDown(imnode);
        }
    }
}
