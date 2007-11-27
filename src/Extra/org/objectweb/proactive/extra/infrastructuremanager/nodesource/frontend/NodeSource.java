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
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.exception.AddingNodesException;


/**
 * abstract class designed to manage a NodeSource.
 *
 * @author ProActive team
 *
 */
public abstract class NodeSource implements Serializable, InitActive, EndActive {

    /**
     *  for a NodeSource At least a name, nodes list and its IMcore
     */
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);
    protected IMCoreSourceInt imCore;
    protected String SourceId = null;
    private boolean Pinger_alive = true;
    static final int DEFAULT_NODE_SOURCE_PING_FREQUENCY = 5000;
    public HashMap<String, Node> nodes;

    /**
     * empty constructor
     */
    public NodeSource() {
    }

    public NodeSource(String id, IMCoreSourceInt nodeManager) {
        this.SourceId = id;
        this.nodes = new HashMap<String, Node>();
        this.imCore = nodeManager;
    }

    /**
     * creating the Ping thread,
     */
    public void initActivity(Body body) {
        this.imCore.addSource((NodeSource) ProActiveObject.getStubOnThis(),
            this.SourceId);
        // TODO gsigety, cdelbe : giving a stub on the source can 
        // lead to a lock if source is blocked
        new Pinger((NodeSource) ProActiveObject.getStubOnThis());
    }

    /**
     *  stop the Pinger thread at the end of NodeSource activity
     */
    public void endActivity(Body body) {
        Pinger_alive = false;
    }

    public String getId() {
        return this.SourceId;
    }

    public abstract NodeSourceEvent getSourceEvent();

    // ----------------------------------------------------------------------//
    // methods called by IMcore 
    // ----------------------------------------------------------------------//

    /**
     * confirm to a NodeSource a node release :
     * NodeSource has asked previously a "soft" remove of a node
     * node is already unregistered in the IMcore (method called by the node IMCore)
     */
    public abstract void confirmRemoveNode(String nodeUrl);

    /**
     * IMnodeManager has receive a removing node request
     * passing the removing request by this method
     */
    public abstract void forwardRemoveNode(String nodeUrl, boolean preempt);

    /**
     * Add new nodes to the node Source
     * throw an exception if adding request send to a DynamicNodeSource
     */
    public abstract void addNodes(ProActiveDescriptor pad, String padName)
        throws AddingNodesException;

    //TODO Germs add shutdown

    // ----------------------------------------------------------------------//
    // protected methods
    // ----------------------------------------------------------------------//

    /**
     * return the node object according to the URL
     */
    protected Node getNodebyUrl(String url) {
        return nodes.get(url);
    }

    /**
     * a new node is available in the NodeSource, register it to the internal list
     * and register the node to the IMCore
     * called by
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
     * Removing a node handled by the NodeSource
     */
    protected void removeFromList(Node node) {
        nodes.remove(node.getNodeInformation().getURL());
    }

    // ----------------------------------------------------------------------//
    // public methods
    // ----------------------------------------------------------------------//

    // Getters --------------------------------------------------------------//
    public IntWrapper getNbNodes() {
        return new IntWrapper(nodes.size());
    }

    public ArrayList<Node> getNodes() {
        return new ArrayList<Node>(nodes.values());
    }

    public String getSourceId() {
        return this.SourceId;
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
     *
     * public method because called via a NodeSource stub
     */
    public abstract void detectedPingedDownNode(String nodeUrl);

    // ----------------------------------------------------------------------//
    // intern class which implements the Pinger thread
    // ----------------------------------------------------------------------//
    public class Pinger extends Thread {
        private NodeSource nodeSource;

        public Pinger(NodeSource source) {
            nodeSource = source;
            start();
        }

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
