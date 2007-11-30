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
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PADNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PadDeployInterface;


public class PADNodeSource extends NodeSource implements PADNSInterface,
    PadDeployInterface {
    private static final long serialVersionUID = 9195674290785820181L;

    /** PADs **/
    private HashMap<String, ProActiveDescriptor> listPad;

    /**
     * empty constructor
     */
    public PADNodeSource() {
        super();
    }

    public PADNodeSource(String id, IMCoreSourceInt nodeManager) {
        super(id, nodeManager);
        this.listPad = new HashMap<String, ProActiveDescriptor>();
    }

    public void initActivity(Body body) {
        super.initActivity(body);
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by IMNodeManager
    // ----------------------------------------------------------------------//
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

    public void forwardRemoveNode(String nodeUrl, boolean preempt) {
        assert this.nodes.containsKey(nodeUrl);
        this.imCore.internalRemoveNode(nodeUrl, preempt);
    }

    /**
     * add nodes by deploying a ProActive Descriptor, recover nodes created,
     * adding them to the node Source and register nodes to the IMNodeManager
     */
    public void addNodes(ProActiveDescriptor pad, String PADName) {
        this.listPad.put(PADName, pad);
        IMDeploymentFactory.deployAllVirtualNodes((PadDeployInterface) ProActiveObject.getStubOnThis(),
            PADName, pad);
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger 
    // ----------------------------------------------------------------------//	

    /**
     * A down node has been detected
     * Inform the IMNode Manager about the broken node,
     * remove the broken node from the list this.nodes
     */
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            removeFromList(node);
            this.imCore.setDownNode(nodeUrl);
        }
    }

    public HashMap<String, ProActiveDescriptor> getListPAD() {
        return listPad;
    }

    public IntWrapper getSizeListPad() {
        return new IntWrapper(listPad.size());
    }

    public IMNodeSourceEvent getSourceEvent() {
        return new IMNodeSourceEvent(this.getSourceId(),
            IMConstants.PAD_NODE_SOURCE_TYPE);
    }

    // ----------------------------------------------------------------------//
    // methods inherited from PadDeployInterface 
    // ----------------------------------------------------------------------//	

    /**
     * called by IMDeploy
     * receive a new node Deployed
     */
    public void receiveDeployedNode(Node node, String VnName, String PADName) {
        this.addNewAvailableNode(node, VnName, PADName);
    }
}
