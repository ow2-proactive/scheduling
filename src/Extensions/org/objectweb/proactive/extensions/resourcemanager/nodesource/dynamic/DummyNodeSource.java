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
package org.objectweb.proactive.extensions.resourcemanager.nodesource.dynamic;

import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCoreSourceInt;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.PadDeployInterface;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.pad.PADNodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.pad.RMDeploymentFactory;


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

    public DummyNodeSource(String id, RMCoreSourceInt nodeManager,
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
            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(
                    "/user/gsigety/home/pa_descriptors/nodes.xml");
            RMDeploymentFactory.deployAllVirtualNodes((PadDeployInterface) PAActiveObject.getStubOnThis(),
                pad);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminates activity of P2PNodeSource Active Object.
     */
    @Override
    public void endActivity(Body body) {
        super.endActivity(body);
        for (Entry<Node, Boolean> entry : this.StaticNodes.entrySet()) {
            try {
                entry.getKey().killAllActiveObjects();
                //TODO gisgety killing the node ?
                entry.getKey().getProActiveRuntime()
                     .killNode(entry.getKey().getNodeInformation().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    protected void killNodeRT(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();
        StaticNodes.remove(nodeUrl);
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(),
            RMConstants.DUMMY_NODE_SOURCE_TYPE);
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger 
    // ----------------------------------------------------------------------//	
    /**
     * A down node has been detected
     * remove the broken node from the list this.nodes
     * remove node from the TTR list
     * Inform the RMcore about the broken node,
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
            //informing RMCore about the broken node
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
     * but not register it to the "true" node list neither to the RMNodeManager
     * (where are in a dynamic node source)
     */
    public void receiveDeployedNode(Node node, String VnName, String PADName) {
        this.StaticNodes.put(node, new Boolean(true));
    }

    /**
     * Shutdown the node source
     * All nodes are removed from node source and from RMCore
     * @param preempt true Node source doesn't wait tasks end on its handled nodes,
     * false node source wait end of tasks on its nodes before shutting down
     */
    @Override
    public void shutdown(boolean preempt) {
        //TODO gsigety : work !
        //		for (Entry<String, Node> entry : this.nodes.entrySet()) {
        //			this.imCore.internalRemoveNode(entry.getKey(), preempt);
        //		}
    }
}
