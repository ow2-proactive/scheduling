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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic;

import java.util.HashMap;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;


/**
 * Implementation of a Peer to Peer dynamic node source.
 * TODO the methods {@link #getNode()} and {@link #releaseNode(IMNode)} must be implemented
 * @author proactive team
 *
 */
public class P2PNodeSource extends DynamicNodeSource implements InitActive {
    private static final long serialVersionUID = -9077907016230441233L;
    private P2PService p2pService;
    private HashMap<String, P2PNodeLookup> lookups;
    private Vector<String> peerUrls;

    public P2PNodeSource(String id, IMCoreSourceInt nodeManager,
        int nbMaxNodes, int nice, int ttr, Vector<String> peerUrls) {
        super(id, nodeManager, nbMaxNodes, nice, ttr);
        lookups = new HashMap<String, P2PNodeLookup>();
        this.peerUrls = peerUrls;
    }

    /**
     * empty constructor
     */
    public P2PNodeSource() {
    }

    public void initActivity(Body body) {
        super.initActivity(body);
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

    // ----------------------------------------------------------------------//
    // definition of abstract methods inherited from dynamicNodeSource 
    // ----------------------------------------------------------------------//	    

    /**
     * internal method, node is given back to the source
     * @Override releaseNode from DynamicNodeSource
     */
    @Override
    protected void releaseNode(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        System.out.println("[DYNAMIC P2P SOURCE] P2PNodeSource.releaseNode(" +
            nodeUrl + ")");
        P2PNodeLookup p2pNodeLookup = this.lookups.get(nodeUrl);
        p2pNodeLookup.killNode(nodeUrl);
        //remove node from the list
        removeFromList(node);
        //remove node and its lookup form lookup HM
        this.lookups.remove(nodeUrl);
        //indicate that a new node has to be got in a [niceTime] future
        newNiceTime();
    }

    /**
     * internal method, acquiring a node from a dynamic node source
     * @Override getNode from DynamicNodeSource
     */
    @Override
    protected Node getNode() {
        // TODO Auto-generated method stub
        P2PNodeLookup p2pNodeLookup = this.p2pService.getNodes(1,
                this.SourceId, "Infrastructure Manager");
        Node n = (Node) ((p2pNodeLookup.getNodes()).firstElement());
        this.lookups.put(n.getNodeInformation().getURL(), p2pNodeLookup);
        return n;
    }

    @Override
    public NodeSourceEvent getSourceEvent() {
        return new NodeSourceEvent(this.getSourceId(),
            "Peer to peer Node Source");
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger 
    // ----------------------------------------------------------------------//

    /**
     * A down node has been detected
     * remove the broken node from the list this.nodes
     * remove node from the TTR list
     * Inform the IMNode Manager about the broken node,
     * create a new nice time
     */
    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            //remove node from the list
            removeFromList(node);
            //remove node and its lookup from lookup HashMap
            this.lookups.remove(nodeUrl);
            //remove the node from the node_ttr HashMap
            this.getNodesTtr_List().remove(nodeUrl);
            //informing IMNode Manager about the broken node
            this.imCore.setDownNode(nodeUrl);
            //indicate that a new node has to be got in a [niceTime] future
            newNiceTime();
        }
    }
}
