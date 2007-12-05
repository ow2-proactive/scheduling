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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad;

import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PADNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PadDeployInterface;


/** Implementation in InfrastuctureManager of static nodes management.<BR><BR>
 *
 * This Class implements a ProActive object that deploy or acquire
 * (by a ProActive deployment descriptor: PAD) nodes, and register nodes to the {@link IMCore}.
 * The source monitor deployed nodes and inform the core of a down node.
 * Nodes handled here are static ; i.e they are always available to the Core,
 * and are only removed thanks to an administrator request.<BR><R>
 *
 * This source can deploy a PAD at its startup, or during its activity.
 * So it can handle many PAD.
 *
 * @author ProActive team
 *
 */
public class PADNodeSource extends NodeSource implements PADNSInterface,
    PadDeployInterface {

    /** serial version UID */
    private static final long serialVersionUID = 9195674290785820181L;

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
     * @param imCore the {@link IMCoreSourceInt core} already created of the Infrastructure Manager.

     */
    public PADNodeSource(String id, IMCoreSourceInt imCore) {
        super(id, imCore);
        this.listPad = new HashMap<String, ProActiveDescriptor>();
    }

    /**
     * Initialization part of NodeSource Active Object.
     * call the init part of super class {@link NodeSource}.
     */
    public void initActivity(Body body) {
        super.initActivity(body);
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by IMNodeManager
    // ----------------------------------------------------------------------//

    /**
     * Confirms a removing node request asked previously.
     * PAD nodeSource has received from the Admin and by the Core
     * an explicit removing node Request (@link forwardRemoveNode}.
     * If the node is already handled by the source, PADNode
     * ask to the Core to remove the node {@link IMCoreSourceInt#internalRemoveNode(String, boolean)}
     * and the Core confirm by this method.<BR>
     *
     * Many calls, but the explicit removing node mechanism has been reduce
     * to a normal removing mechanism as in a generc node source source : removing a node is always initiated by
     * the NodeSource, not by the Core.
     * @param nodeUrl URL of the node to remove.
     *
     * @see org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource
     */
    public void confirmRemoveNode(String nodeUrl) {
        if (logger.isInfoEnabled()) {
            logger.info("[" + this.SourceId + "] removing Node : " + nodeUrl);
        }

        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            Node node = nodes.get(nodeUrl);
            try {
                node.killAllActiveObjects();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.removeFromList(this.getNodebyUrl(nodeUrl));
        }
    }

    /**
     * Receives a removing node request.
     * IMCore has been asked to Remove a node, and forward it.
     * remove mechanism must be initiated by the NodeSource.
     * check if the node is not down. and ask to the Core to make its internal remove.
     * @param nodeUrl URL of the node to remove
     * @param preempt true the node must removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void forwardRemoveNode(String nodeUrl, boolean preempt) {
        assert this.nodes.containsKey(nodeUrl);
        this.imCore.internalRemoveNode(nodeUrl, preempt);
    }

    /**
     * Add nodes to the Source.
     * add nodes by deploying a ProActive Descriptor,get nodes created,
     *  add them to the node Source and register nodes to the IMCore
     * @param nodeUrl pad ProActive descriptor to deploy.
     * @param PADName name of the ProActive descriptor.
     */
    public void addNodes(ProActiveDescriptor pad) {
        this.listPad.put(pad.getUrl(), pad);
        IMDeploymentFactory.deployAllVirtualNodes((PadDeployInterface) ProActiveObject.getStubOnThis(),
            pad);
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger 
    // ----------------------------------------------------------------------//	

    /**
     * A down node has been detected.
     * Inform the IMCore about the broken node,
     * remove the broken node from the nodes list.
     */
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
     * <BR>Called by {@link IMCore}.<BR>
     * @return {@link IMNodeSourceEvent} object contains properties of the NodeSource.
     */
    public IMNodeSourceEvent getSourceEvent() {
        return new IMNodeSourceEvent(this.getSourceId(),
            IMConstants.PAD_NODE_SOURCE_TYPE);
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
