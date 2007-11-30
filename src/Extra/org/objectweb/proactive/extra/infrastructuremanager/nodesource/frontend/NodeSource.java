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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.exception.AddingNodesException;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;


/**
 * Abstract class designed to manage a NodeSource.
 * A NodeSource active object is designed to manage acquisition, monitoring
 * and removing of a set of {@link Node} objects in the Infrastructure Manager.
 * This set of nodes could be nodes deployed by a ProActive Descriptor,
 * or be nodes acquired dynamically from a dynamic source,
 * such as a peer to peer infrastructure, or a cluster.<BR>
 * As the {@link IMCore} manage nodes provide them to Scheduler (with nodes selection, and node states handling),
 * a NodeSource has just to : acquire nodes, add them to {@link IMCore}, and finally remove them from IMCore,
 * and monitor those acquired nodes. NodeSource communications to IMCore are defined in {@link org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt}.<BR><BR><BR>
 *
 *
 * There is a mechanism of giving-removing nodes between the NodeSource and {@link IMCoreSourceInt} :<BR><BR>
 *
 * 1- Giving Node to IMCore when new node is available :<BR>
 * {@link org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#addNewAvailableNode(Node,String,String)}
 * (method to call when a new node is available).<BR>
 * NodeSource add node to the {@link IMCoreSourceInt} with the method<BR>
 * {@link org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt#internalAddNode(Node, String, String, NodeSource)}<BR><BR>
 *
 * 2- NodeSource remove the Node to the {@link IMCoreSourceInt} with the method : <BR>
 * {@link org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt#internalRemoveNode(String, boolean)}<BR><BR>
 *
 * 3- Finally the {@link IMCore} confirm the removing request by calling <BR>
 * {@link org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#confirmRemoveNode(String)} <BR><BR>
 *
 *
 * A NodeSource has to treat explicit removing and adding nodes Requests asked by {@link IMAdmin} and forwarded by {@link IMCore}<BR>
 * - {@link IMCore} receive an adding nodes request, it just forwards the request to the appropriate NodeSource by calling :<BR>
 *                 {@link org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#addNodes(ProActiveDescriptor, String)}<BR>
 * - {@link IMCore} receive a removing node request, it just forwards the request to the appropriate NodeSource by calling :<BR>
 *                 {@link org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#forwardRemoveNode(String, boolean)}<BR>
 * - The NodeSource receive the removing node request and perform the removing as the Steps 2 and 3 of the remove mechanism presented above<BR><BR>
 *
 *
 * This class implements a Nodes monitoring mechanism implemented by an inner class {@link Pinger}. This object run a Pinger thread,
 * which will ping  {@link Node} objects each {@link NodeSource#DEFAULT_NODE_SOURCE_PING_FREQUENCY} ms.
 * When a down node is detected, the Pinger object call @see org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#detectedPingedDownNode(String);
 *
 * @author ProActive team
 *
 */
public abstract class NodeSource implements Serializable, InitActive, EndActive {

    /** serial version UID */
    private static final long serialVersionUID = 1L;

    /** IM logger */
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);

    /** {@link IMCoreSourceInt} interface for the NodeSource */
    protected IMCoreSourceInt imCore;

    /** unique id of the source */
    protected String SourceId = null;

    /** boolean indicating status of nodes pinger */
    private boolean Pinger_alive = true;

    /** Ping frequency of the Node Pinger thread */
    static final int DEFAULT_NODE_SOURCE_PING_FREQUENCY = 5000;

    /** HashMap of nodes available and managed by this NodeSource
     * All nodes in this HashMap are registered in {@link IMCoreSourceInt} too */
    public HashMap<String, Node> nodes;

    /**
     * Pro Active empty constructor
     */
    public NodeSource() {
    }

    /**
     * Create a new NodeSource.
     * @param id unique id of the source.
     * @param imCore the {@link IMCoreSourceInt} already created of the Infrastructure Manager.
     */
    public NodeSource(String id, IMCoreSourceInt imCore) {
        this.SourceId = id;
        this.nodes = new HashMap<String, Node>();
        this.imCore = imCore;
    }

    /**
     * Initialization part of NodeSource Active Object.
     * Create the pinger thread which monitor nodes handled by the source.
     * register itself to the {@link IMCore}.
     */
    public void initActivity(Body body) {
        this.imCore.addSource((NodeSource) ProActiveObject.getStubOnThis(),
            this.SourceId);
        // TODO gsigety, cdelbe : giving a stub on the source can 
        // lead to a lock if source is blocked
        new Pinger((NodeSource) ProActiveObject.getStubOnThis());
    }

    /**
     * Terminate activity of NodeSource Active Object.
     */
    public void endActivity(Body body) {
        Pinger_alive = false;
    }

    public String getId() {
        return this.SourceId;
    }

    /**
     * Method called by {@link IMCore}.
     * Create a {@link IMNodeSourceEvent} object representing the NodeSource State.
     * @return {@link IMNodeSourceEvent} object contains properties of the NodeSource.
     */
    public abstract IMNodeSourceEvent getSourceEvent();

    /**
     * Method called by {@link IMCore}.
     * {@link IMAdmin} asked an explicit removing node request to {@link IMCore}.
     * {@link IMCore} just forward a removing Node request to the appropriate NodeSource,
     * This abstract method must be defined to perform removing actions of a node.
     * @param nodeUrl Url of the node to remove.
     * @param preempt true : the node remove must be done immediately,
     * false : NodeSource will wait the job termination if the node is Busy.
     */
    public abstract void forwardRemoveNode(String nodeUrl, boolean preempt);

    /**
     * Method called by {@link IMCore}.
     * the way to add a static nodes on a NodeSource, those new nodes are deployed or acquired
     * by the NodeSource itself.
     * This method is useful for static sources only (see {@link PADNodeSource}),
     * an exception is thrown when this request is asked to a {@link DynamicNodeSource}.
     * @param pad ProActive Deployment descriptor representing nodes to deploy.
     * @param padName a name associated with the ProActive Descriptor.
     * @throws AddingNodesException thrown if this method is asked on a {@link DynamicNodeSource}.
     */
    public abstract void addNodes(ProActiveDescriptor pad, String padName)
        throws AddingNodesException;

    /**
     * Method called by {@link IMCore}.
     * Confirm to a NodeSource a node release :
     * NodeSource has asked previously a remove of a node to {@link IMCore},
     * so the {@link IMCore} has removed the Node and confirm the action to the NodeSource.
     * This abstract method must be defined to perform last actions in node removing mechanism
     * @param nodeUrl Url of the node on which IMCore confirm the remove.
     */
    public abstract void confirmRemoveNode(String nodeUrl);

    // Getters --------------------------------------------------------------//

    /**
     *
     * @return number of nodes handled by the NodeSource
     */
    public IntWrapper getNbNodes() {
        return new IntWrapper(nodes.size());
    }

    /**
     * @return an ArrayList of all {@link Node} Handled by the NodeSource
     */
    public ArrayList<Node> getNodes() {
        return new ArrayList<Node>(nodes.values());
    }

    /**
     * @return a {@link String} which represent Id of th NodeSource
     */
    public String getSourceId() {
        return this.SourceId;
    }

    //TODO Germs add shutdown

    // ----------------------------------------------------------------------//
    // protected methods
    // ----------------------------------------------------------------------//

    /**
     * internal method
     * return the {@link Node} object according to the URL
     * @param url unique name of node
     */
    protected Node getNodebyUrl(String url) {
        return nodes.get(url);
    }

    /**
     * internal method
     * Must be called when a new node is available in the NodeSource,
     * so the NodeSource register it to the internal list
     * and provide the new node to the {@link IMCore}
     * @param node new node object available
     * @param VnName VirtualNode name of the node
     * @param PADName ProActiveDescriptor name of the node
     */
    protected void addNewAvailableNode(Node node, String VnName, String PADName) {
        if (logger.isInfoEnabled()) {
            logger.info("[" + this.SourceId + "] new node available : " +
                node.getNodeInformation().getURL());
        }
        this.nodes.put(node.getNodeInformation().getURL(), node);
        NodeSource s = (NodeSource) ProActiveObject.getStubOnThis();
        this.imCore.internalAddNode(node, VnName, PADName, s);
    }

    /**
         * internal method
     * Removing from internal list a node handled by the NodeSource
     * @param node {@link Node} to remove
     */
    protected void removeFromList(Node node) {
        nodes.remove(node.getNodeInformation().getURL());
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger 
    // ----------------------------------------------------------------------//	
    /**
     * method called when the Pinger has detected a down node, each NodeSource
     * has to specify what to do when a down node is detected
     * (certainly inform the IMCore about the broken node,
     * remove the broken node from the list this.nodes
     * and for a dynamic node source, adding a new nice time for example)
     * public method because called via a NodeSource stub
     * @param nodeUrl url of the detected down {@link Node}
     */
    public abstract void detectedPingedDownNode(String nodeUrl);

    /**
     * inner class which implements the Pinger thread
     */
    public class Pinger extends Thread {

        /** stub of the NodeSource Active Object*/
        private NodeSource nodeSource;

        /**
         * Pinger constructor,
         * launch the nodes monitoring thread
         * @param source stub of the NodeSource Active Object
         */
        public Pinger(NodeSource source) {
            nodeSource = source;
            start();
        }

        /**
         * Activity thread of the Pinger
         * each {@link NodeSource#DEFAULT_NODE_SOURCE_PING_FREQUENCY} time
         * the Pinger get the NodeList of the NodeSource,
         * and verify if nodes are always reachable
         * if one of them is unreachable, it will be said "down",
         * and must be removed from the NodeSource.
         * {@link NodeSource#detectedPingedDownNode(String)} is called when a down node is detected.
         */
        public void run() {
            while (!isInterrupted() && Pinger_alive) {
                try {
                    sleep(DEFAULT_NODE_SOURCE_PING_FREQUENCY);
                } catch (InterruptedException ex) {
                }
                for (Node node : this.nodeSource.getNodes()) {
                    String nodeURL = node.getNodeInformation().getURL();
                    try {
                        node.getNumberOfActiveObjects();
                    } catch (Exception e) {
                        this.nodeSource.detectedPingedDownNode(nodeURL);
                    } //catch
                } //for
            } //while interrupted
        } //run
    }
}
