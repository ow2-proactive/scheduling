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
package org.objectweb.proactive.extensions.resourcemanager.nodesource.pad;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCore;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCoreSourceInt;
import org.objectweb.proactive.extensions.resourcemanager.exception.AddingNodesException;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.PADNSInterface;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.PadDeployInterface;


/** Implementation in InfrastuctureManager of static nodes management.<BR><BR>
 *
 * This Class implements a ProActive object that deploy or acquire
 * (by a ProActive deployment descriptor: PAD) nodes, and register nodes to the {@link RMCore}.
 * The source monitor deployed nodes and inform the core of a down node.
 * Nodes handled here are static ; i.e they are always available to the Core,
 * and are only removed thanks to an administrator request.<BR><R>
 *
 * This source can deploy a PAD at its startup, or during its activity.
 * So it can handle many PADs.
 *
 * @author ProActive team
 *
 */
public class PADNodeSource extends NodeSource implements PADNSInterface, PadDeployInterface {

    /** PADs list of pad handled by the source */
    private HashMap<String, ProActiveDescriptor> listPad;

    /**
     * empty constructor
     */
    public PADNodeSource() {
        super();
    }

    /**
     * Creates a new PADNodeSource.
     * call the upper class constructor.
     * Initialize the PADs list.
     * @param id unique id of the source.
     * @param imCore the {@link RMCoreSourceInt core} already created of the Resource Manager.

     */
    public PADNodeSource(String id, RMCoreSourceInt imCore) {
        super(id, imCore);
        this.listPad = new HashMap<String, ProActiveDescriptor>();
    }

    /**
     * Initialization part of NodeSource Active Object.
     * call the init part of super class {@link NodeSource}.
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);
    }

    /**
     * Terminates PADNodeSource Active Object.
     */
    public void endActivity(Body body) {
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by RMCore
    // ----------------------------------------------------------------------//

    /**
     * Confirms a removing node request asked previously.
     * PAD nodeSource has received the removing request
     * from the RMAdmin and forwarded by the Core
     * an explicit removing node Request (@link forwardRemoveNode}.
     * If the node is already handled by the source, PADNode
     * ask to the Core to remove the node {@link RMCoreSourceInt#internalRemoveNode(String, boolean)}
     * and the Core confirm by this method.<BR>
     *
     * Many calls, but the explicit removing node mechanism has been reduce
     * to a normal removing mechanism as in a generc node source source : removing a node is always initiated by
     * the NodeSource, not by the Core.
     * @param nodeUrl URL of the node to remove.
     *
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource
     */
    @Override
    public void confirmRemoveNode(String nodeUrl) {
        if (logger.isInfoEnabled()) {
            logger.info("[" + this.SourceId + "] removing Node : " + nodeUrl);
        }

        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            Node node = nodes.get(nodeUrl);
            try {
                node.getProActiveRuntime().killRT(false);
            } catch (IOException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.removeFromList(this.getNodebyUrl(nodeUrl));
        }

        //all nodes has been removed and NodeSource has been asked to shutdown:
        //shutdown the Node source
        if (this.toShutdown && (this.nodes.size() == 0)) {
            this.imCore.internalRemoveSource(this.SourceId, this.getSourceEvent());
            // object should be terminated NON preemptively 
            // pinger thread can wait for last results (getNodes)
            PAActiveObject.terminateActiveObject(false);
        }
    }

    /**
     * Receives a removing node request.
     * RMCore has been asked to Remove a node, and forward it.
     * remove mechanism must be initiated by the NodeSource.
     * check if the node is not down. and ask to the Core to make its internal remove.
     * @param nodeUrl URL of the node to remove
     * @param preempt true the node must removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    @Override
    public void forwardRemoveNode(String nodeUrl, boolean preempt) {
        assert this.nodes.containsKey(nodeUrl);
        this.imCore.internalRemoveNode(nodeUrl, preempt);
    }

    /**
     * Add nodes to the Source.
     * add nodes by deploying a ProActive Descriptor,get nodes created,
     *  add them to the node Source and register nodes to the RMCore
     * @param nodeUrl pad ProActive descriptor to deploy.
     * @param PADName name of the ProActive descriptor.
     */
    @Override
    public void addNodes(ProActiveDescriptor pad) {
        this.listPad.put(pad.getUrl(), pad);
        RMDeploymentFactory.deployAllVirtualNodes((PadDeployInterface) PAActiveObject.getStubOnThis(), pad);
    }

    /**
     * Adds an already deployed node to the NodeSource.
     * lookup the node an add the node to the Source
     * Operation unavailable on a dynamic node source
     * @param nodeUrl
     * @throws AddingNodesException if lookup has failed
     */
    public void addNode(String nodeUrl) throws AddingNodesException {
        try {
            Node newNode = NodeFactory.getNode(nodeUrl);
            this.addNewAvailableNode(newNode, "noVN", "no_pad");
        } catch (NodeException e) {
            throw new AddingNodesException(e);
        }
    }

    /**
     * Shutdown the node source
     * All nodes are removed from node source and from RMCore
     * @param preempt true Node source doesn't wait tasks end on its handled nodes,
     * false node source wait end of tasks on its nodes before shutting down
     */
    @Override
    public void shutdown(boolean preempt) {
        super.shutdown(preempt);
        if (this.nodes.size() > 0) {
            for (Entry<String, Node> entry : this.nodes.entrySet()) {
                this.imCore.internalRemoveNode(entry.getKey(), preempt);
            }
        } else {
            //no nodes to remove, shutdown directly the NodeSource
            this.imCore.internalRemoveSource(this.SourceId, this.getSourceEvent());
            // object should be terminated NON preemptively 
            // pinger thread can wait for last results (getNodes)
            PAActiveObject.terminateActiveObject(false);
        }
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger 
    // ----------------------------------------------------------------------//	

    /**
     * A down node has been detected.
     * Inform the RMCore about the broken node,
     * remove the broken node from the nodes list.
     */
    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            removeFromList(node);
            this.imCore.setDownNode(nodeUrl);
        }
    }

    /**
     * Gives the deployed PADs list.
     * @return HashMap associating a PAD with its name.
     */
    public HashMap<String, ProActiveDescriptor> getListPAD() {
        return listPad;
    }

    /**
     * Gives number of deployed PADs.
     * @return number of deployed PADs.
     */
    public IntWrapper getSizeListPad() {
        return new IntWrapper(listPad.size());
    }

    /**
     * Returns event object representing the NodeSource.
     * <BR>Called by {@link RMCore}.<BR>
     * @return {@link RMNodeSourceEvent} object contains properties of the NodeSource.
     */
    @Override
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(), RMConstants.PAD_NODE_SOURCE_TYPE);
    }

    // ----------------------------------------------------------------------//
    // methods inherited from PadDeployInterface 
    // ----------------------------------------------------------------------//	

    /**
     * Receives a new deployed node and adding it to the NodeSource.
     * A new node has been acquired by the deployment mechanism.
     * Register it in the PADNodeSource.
     * @param node new Node object acquired.
     * @param vnName virtual node name of the node.
     * @param vnName ProActive descriptor name of the node.
     */
    public void receiveDeployedNode(Node node, String VnName, String PADName) {
        this.addNewAvailableNode(node, VnName, PADName);
    }
}
