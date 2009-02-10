/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.nodesource.gcm;

import java.io.IOException;
import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource;


/**
 * GCM Node source is a static node source which handle deployment and management of
 * of virtual nodes provided by GCMApplication objects.
 * @author The ProActive Team
 *
 */
public class GCMNodeSource extends NodeSource {

    /** PADs list of pad handled by the source */
    private HashMap<String, GCMApplication> listGCMApp;

    /** Stub of GCMNOde source AO 
     * bugFix GCMVirtualNode.subscribeNodeAttachment doesn't support AO stub */
    private GCMNodeSource myStub;

    /**
     * ProActive empty constructor
     */
    public GCMNodeSource() {
        super();
    }

    /**
     * Constructor of a GCM node source.
     * @param id Node source unique name
     * @param rmCore Stub of the RMCore active object
     */
    public GCMNodeSource(String id, RMCoreSourceInterface rmCore) {
        super(id, rmCore);
        listGCMApp = new HashMap<String, GCMApplication>();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        super.initActivity(body);
        this.myStub = (GCMNodeSource) PAActiveObject.getStubOnThis();
    }

    /**
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by RMCore
    // ----------------------------------------------------------------------//

    /**
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#nodeRemovalCoreRequest(java.lang.String, boolean)
     */
    @Override
    public void nodeRemovalCoreRequest(String nodeUrl, boolean killNode) {
        if (logger.isInfoEnabled()) {
            logger.info("[" + this.SourceId + "] removing Node : " + nodeUrl);
        }

        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            //for a static GCM node Source the runtime is always killed,
            //without evaluating the preempt value
            Node node = nodes.get(nodeUrl);
            try {
                if (!this.isThereNodesInSameJVM(node)) {
                    node.getProActiveRuntime().killRT(false);
                }
            } catch (IOException e) {
                //do nothing, no exception treatment for node just killed before
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.removeFromList(this.getNodebyUrl(nodeUrl));
        }

        //all nodes has been removed and NodeSource has been asked to shutdown:
        //shutdown the Node source
        if (this.toShutdown && (this.nodes.size() == 0)) {
            terminateNodeSourceShutdown();
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#nodeAddingCoreRequest(java.lang.String)
     */
    @Override
    public void nodeAddingCoreRequest(String nodeUrl) throws AddingNodesException {
        try {
            if (this.nodes.containsKey(nodeUrl)) {
                throw new AddingNodesException("a node with the same URL is already in this node Source,"
                    + "remove this node, before adding a node with a a same URL");
            }

            Node newNode = NodeFactory.getNode(nodeUrl);
            if (logger.isInfoEnabled()) {
                logger.info("[" + this.SourceId + "] new node available : " +
                    newNode.getNodeInformation().getURL());
            }
            this.nodes.put(newNode.getNodeInformation().getURL(), newNode);
        } catch (NodeException e) {
            throw new AddingNodesException(e);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#nodesAddingCoreRequest(org.objectweb.proactive.gcmdeployment.GCMApplication)
     */
    @Override
    public void nodesAddingCoreRequest(GCMApplication app) throws AddingNodesException {
        Map<String, GCMVirtualNode> virtualNodes = app.getVirtualNodes();
        for (Entry<String, GCMVirtualNode> entry : virtualNodes.entrySet()) {
            try {
                entry.getValue().subscribeNodeAttachment(this, "receiveDeployedNode", true);
                app.startDeployment();
                this.listGCMApp.put(app.toString(), app);
            } catch (ProActiveException e) {
                throw new AddingNodesException(e);
            }
        }
    }

    /**
     * Receives a new deployed node and adding it to the NodeSource.
     * A new node has been acquired by the deployment mechanism.
     * Register it in the GCMNodeSource.
     * @param node deployed
     * @param vnodeName virtual node name of the node.
     */
    public synchronized void receiveDeployedNode(Node node, String vnodeName) {
        this.myStub.addNewAvailableNode(node, vnodeName, vnodeName);
    }

    /**
     * Shutdown the node source
     * All nodes are removed from node source and from RMCore
     * @param preempt true Node source doesn't wait tasks end on its handled nodes,
     * false node source wait end of tasks on its nodes before shutting down.
     */
    @Override
    public Boolean shutdown(boolean preempt) {
        super.shutdown(preempt);
        if (this.nodes.size() > 0) {
            Iterator<Entry<String, Node>> it = this.nodes.entrySet().iterator();

            while (it.hasNext()) {
                Entry<String, Node> entry = it.next();

                this.rmCore.nodeRemovalNodeSourceRequest(entry.getKey(), preempt);
                if (preempt) {
                    //preemptive shutdown, kill the node now
                    try {
                        if (!this.isThereNodesInSameJVM(entry.getValue())) {
                            entry.getValue().getProActiveRuntime().killRT(false);
                        }
                    } catch (IOException e) {
                        //do nothing, no exception treatment for node just killed before
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //preemptive shutdown remove the node from 
                    //the node list, because no confirmation is awaited from RMCore 
                    this.removeFromList(this.getNodebyUrl(entry.getKey()));
                    it = this.nodes.entrySet().iterator();
                }
            }
            //preemptive shutdown, no need to wait preemptive removals
            //shutdown immediately
            if (preempt) {
                terminateNodeSourceShutdown();
            }
        } else {
            //no nodes handled by the node source, 
            //so node source can be stopped and removed immediately
            terminateNodeSourceShutdown();
        }

        return new Boolean(true);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#detectedPingedDownNode(java.lang.String)
     */
    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            removeFromList(node);
            this.rmCore.setDownNode(nodeUrl);
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#getSourceEvent()
     */
    @Override
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(), RMConstants.GCM_NODE_SOURCE_TYPE);
    }

    /**
     * Check if there are any other nodes handled by the NodeSource in the same JVM of the node
     * passed in parameter.
     * @param node Node to check if there any other node of the NodeSource in the same JVM
     * @return true there is another node in the node's JVM handled by the nodeSource, false otherwise. 
     */
    public boolean isThereNodesInSameJVM(Node node) {
        VMID nodeID = node.getVMInformation().getVMID();
        String nodeToTestUrl = node.getNodeInformation().getURL();
        Collection<Node> nodesList = nodes.values();
        for (Node n : nodesList) {
            if (!n.getNodeInformation().getURL().equals(nodeToTestUrl) &&
                n.getVMInformation().getVMID().equals(nodeID)) {
                return true;
            }
        }
        return false;
    }
}
