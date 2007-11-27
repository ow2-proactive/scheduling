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
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PadDeployInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.IMDeploymentFactory;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;


/**
 * Simple implementation of what can be a {@link DynamicNodeSource}.
 * This Dummy class, create a {@link PADNodeSource}, deploys static nodes,
 * and acts as if it was a dynamic source...
 * @author ProActive team
 *
 */
public class DummyNodeSource extends DynamicNodeSource
    implements PadDeployInterface {
    private static final long serialVersionUID = 8213062492772541033L;

    // static nodes with their states (available=true - not available=false)
    private HashMap<Node, Boolean> StaticNodes;

    /**
     * empty constructor
     */
    public DummyNodeSource() {
    }

    public DummyNodeSource(String id, IMCoreSourceInt nodeManager,
        int nbMaxNodes, int nice, int ttr) {
        super(id, nodeManager, nbMaxNodes, nice, ttr);
        StaticNodes = new HashMap<Node, Boolean>();
    }

    /** deploy a PAD
     *
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);
        try {
            ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(
                    "/user/gsigety/home/pa_descriptors/nodes.xml");
            String PADName = "Dummy_workers";
            IMDeploymentFactory.deployAllVirtualNodes((PadDeployInterface) ProActiveObject.getStubOnThis(),
                PADName, pad);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from dynamicNodeSource 
    // ----------------------------------------------------------------------//	

    /**
     * way to acquire a node from a dynamic source
     * in this this example, just pick a free node
     * from the static deployed nodes list
     */
    @Override
    protected Node getNode() {
        for (Entry<Node, Boolean> entry : this.StaticNodes.entrySet()) {
            if (entry.getValue().booleanValue()) {
                //Node is available
                entry.setValue(false);
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * way to release a node
     * set the node available in the deployed nodes list
     * create a new nice time
     */
    @Override
    protected void releaseNode(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        if (logger.isInfoEnabled()) {
            logger.info("[" + this.SourceId + "] releaseNode " + nodeUrl);
        }
        //remove node from the list
        removeFromList(node);
        //indicate that a new node has to be got in a this.niceTime future
        newNiceTime();
        boolean found = false;
        for (Entry<Node, Boolean> entry : this.StaticNodes.entrySet()) {
            if (entry.getKey().getNodeInformation().getURL() == node.getNodeInformation()
                                                                        .getURL()) {
                //Node is available
                entry.setValue(true);
                found = true;
            }
        }
        if (!found) {
            logger.error("[" + this.SourceId +
                "] dummy node source : an unknown node has been rendered !!");
        }
    }

    @Override
    public NodeSourceEvent getSourceEvent() {
        return new NodeSourceEvent(this.getSourceId(), "Dummy Node Source");
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
     * remove node from the static node list
     */
    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            //remove node from the list
            removeFromList(node);
            //remove the node from the node_ttr HashMap
            this.getNodesTtr_List().remove(nodeUrl);
            //informing IMNode Manager about the broken node
            this.imCore.setDownNode(nodeUrl);
            //indicate that a new node has to be got in a this.niceTime future
            newNiceTime();
            if (this.StaticNodes.containsKey(node)) {
                this.StaticNodes.remove(node);
            }
        }
    }

    // ----------------------------------------------------------------------//
    // method inherited from PadDeployInterface 
    // ----------------------------------------------------------------------//	
    /**
     * a new node is available in the NodeSource, register it to the internal list
     * but not register it to the "true" node list neither to the IMNodeManager
     * (where are in a dynamic node source)
     */
    public void receiveDeployedNode(Node node, String VnName, String PADName) {
        this.StaticNodes.put(node, new Boolean(true));
    }
}
