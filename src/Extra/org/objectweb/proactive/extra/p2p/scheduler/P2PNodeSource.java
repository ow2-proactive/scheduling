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
package org.objectweb.proactive.extra.p2p.scheduler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCore;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCoreSourceInterface;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.StartP2PService;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeLookup;


/**
 * Implementation of a ProActive Peer to Peer dynamic node source. A peer to
 * peer DynamicNodeSource object acquire nodes from a ProActive P2P
 * infrastructure. So a ProActive peer to peer network is considered as a
 * dynamic NodeSource, on which nodes can be acquired to submit jobs.<BR>
 * <BR>
 * 
 * WARNING : you must instantiate this class as an Active Object !
 * 
 * @author The ProActive Team
 * 
 */
public class P2PNodeSource extends DynamicNodeSource implements InitActive {

    /** Lookup frequency */
    private static final int LOOKUP_FREQ = 1000;

    /** Number of tries */
    private static final int NUM_TRIES = 10;

    /** Peer to peer Service object which is interface to peer to peer network */
    private P2PService p2pService;

    /** hashMap associate each acquired Node to its p2p lookup object */
    private HashMap<String, P2PNodeLookup> lookups;

    /** Vector of known peers used at startup */
    private Vector<String> peerUrls;

    /**
     * ProActive empty constructor
     */
    public P2PNodeSource() {
    }

    /**
     * Active object constructor.
     * 
     * @param id
     *            Name of node source.
     * @param rmCore
     *            Stub of Active object {@link RMCore}.
     * @param nbMaxNodes
     *            Max number of nodes that the source has to provide.
     * @param nice
     *            Time to wait before acquire a new node just after a node
     *            release.
     * @param ttr
     *            Node keeping duration before releasing it.
     * @param peerUrls
     *            Vector containing known peers .
     * 
     */
    public P2PNodeSource(String id, RMCoreSourceInterface rmCore, int nbMaxNodes, int nice, int ttr,
            Vector<String> peerUrls) {
        super(id, rmCore, nbMaxNodes, nice, ttr);
        lookups = new HashMap<String, P2PNodeLookup>();
        this.peerUrls = peerUrls;
    }

    /**
     * Initialization part of P2PNodeSource Active Object. call the init part of
     * super class {@link DynamicNodeSource} Launching
     * {@link StartP2PService P2P Service}, which connect to an existing P2P
     * infrastructure.
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);

        // set this method in Immediate service in order to avoid avoid
        // waiting time for a monitor, if the P2PNode is blocked by
        // the acquisition delay of a peer to peer node.
        PAActiveObject.setImmediateService("getSourceEvent");

        try {
            StartP2PService startServiceP2P = new StartP2PService(this.peerUrls);
            startServiceP2P.start();
            this.p2pService = startServiceP2P.getP2PService();
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Terminates activity of P2PNodeSource Active Object.
     */
    @Override
    public void endActivity(Body body) {
        super.endActivity(body);
        // TODO gsigety cdelbe : how to stop P2PService ?
    }

    // ----------------------------------------------------------------------//
    // definition of abstract methods inherited from dynamicNodeSource
    // ----------------------------------------------------------------------//

    /**
     * Gives back a node to P2P infrastructure. Internal method, node is given
     * back to the source Kill the node given by the source and its active
     * objects. Kill the {@link P2PNodeLookup} object, remove the node from
     * internal node list and create a new Nice Time
     * 
     * @param node
     *            Node object to release
     * @Override releaseNode from DynamicNodeSource
     */
    @Override
    protected void releaseNode(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        System.out.println("[DYNAMIC P2P SOURCE] P2PNodeSource.releaseNode(" + nodeUrl + ")");
        P2PNodeLookup p2pNodeLookup = this.lookups.get(nodeUrl);
        p2pNodeLookup.killNode(nodeUrl);
        // terminate AOs, remove node and its lookup form lookup HM
        PAActiveObject.terminateActiveObject(this.lookups.remove(nodeUrl), false);
        // indicate that a new node has to be got in a [niceTime] future
    }

    /**
     * Get a node from a P2P infrastructure. internal method, acquiring a node
     * from a P2PNodeSource Get a {@link P2PNodeLookup} object from P2P
     * infrastructure Get the node from the lookup
     * 
     * @Override getNode from DynamicNodeSource
     */
    @Override
    protected Node getNode() {
        // TODO Auto-generated method stub
        P2PNodeLookup p2pNodeLookup = this.p2pService.getNodes(1, this.SourceId, "Resource Manager");
        Node n = null;
        int i = 0;
        try {
            while (!p2pNodeLookup.allArrived() && i < NUM_TRIES) {
                i++;
                Vector<Node> nodes = p2pNodeLookup.getAndRemoveNodes();
                if (nodes.size() > 0) {
                    n = nodes.get(0);
                    break;
                }
                try {
                    Thread.sleep(LOOKUP_FREQ);
                } catch (InterruptedException e) {

                }
            }
            if (n == null) {
                Vector<Node> nodes = p2pNodeLookup.getAndRemoveNodes();
                if (nodes.size() > 0) {
                    n = nodes.get(0);
                }
            }
        } catch (Throwable e) {
            // ... communication error we ignore it
        }
        // Node n = (Node) ((p2pNodeLookup.getNodes()).firstElement());
        if (n != null)
            this.lookups.put(n.getNodeInformation().getURL(), p2pNodeLookup);
        return n;
    }

    protected void killNodeRT(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.lookups.remove(nodeUrl);
    }

    /**
     * Get the RMNodeSourceEvent object of the source. Create the
     * {@link RMNodeSourceEvent} object related to the P2PNodeSource
     * 
     * @return event representing the source.
     */
    @Override
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(), RMConstants.P2P_NODE_SOURCE_TYPE);
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger
    // ----------------------------------------------------------------------//

    /**
     * Manages a down node. A down node has been detected remove the broken node
     * from the list this.nodes remove node from the TTR list Inform the RMNode
     * Manager about the broken node, Create a new nice time.
     */
    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            // remove node from the list
            removeFromList(node);
            // remove node and its lookup from lookup HashMap
            this.lookups.remove(nodeUrl);
            // remove the node from the node_ttr HashMap
            this.getNodesTtr_List().remove(nodeUrl);
            // informing RMNode Manager about the broken node
            this.rmCore.setDownNode(nodeUrl);
            // indicate that a new node has to be got in a [niceTime] future
            newNiceTime();
        }
    }

    /**
     * Shutdown the node source All nodes are removed from node source and from
     * RMCore
     * 
     * @param preempt
     *            true Node source doesn't wait tasks end on its handled nodes,
     *            false node source wait end of tasks on its nodes before
     *            shutting down
     */
    @Override
    public void shutdown(boolean preempt) {
        super.shutdown(preempt);
    }
}
