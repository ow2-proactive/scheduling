package org.objectweb.proactive.extensions.resourcemanager.nodesource.gcm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCoreSourceInterface;
import org.objectweb.proactive.extensions.resourcemanager.exception.AddingNodesException;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class GCMNodeSource extends NodeSource {

    /** PADs list of pad handled by the source */
    private HashMap<String, GCMApplication> listPad;

    /** Stub of GCMNOde source AO 
     * bugFix GCMVirtualNode.subscribeNodeAttachment doesn't support AO stub */
    private GCMNodeSource myStub;

    /**
     * ProActive empty constructor
     */
    public GCMNodeSource() {
        super();
    }

    public GCMNodeSource(String id, RMCoreSourceInterface rmCore) {
        super(id, rmCore);
        listPad = new HashMap<String, GCMApplication>();
    }

    public void initActivity(Body body) {
        super.initActivity(body);
        this.myStub = (GCMNodeSource) PAActiveObject.getStubOnThis();
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
            this.rmCore.internalRemoveSource(this.SourceId, this.getSourceEvent());
            // object should be terminated NON preemptively 
            // pinger thread can wait for last results (getNodes)
            PAActiveObject.terminateActiveObject(false);
        }
    }

    @Override
    public void forwardRemoveNode(String nodeUrl, boolean preempt) {
        assert this.nodes.containsKey(nodeUrl);
        this.rmCore.internalRemoveNode(nodeUrl, preempt);

    }

    @Override
    public void addNode(String nodeUrl) throws AddingNodesException {
        try {
            Node newNode = NodeFactory.getNode(nodeUrl);
            this.addNewAvailableNode(newNode, "noVN", "no_pad");
        } catch (NodeException e) {
            throw new AddingNodesException(e);
        }
    }

    @Override
    public void addNodes(ProActiveDescriptor descriptorPad) throws AddingNodesException {
        throw new AddingNodesException("Node source : " + this.SourceId + " Cannot add PAD to GCMNodeSource");
    }

    public void addNodes(File descriptorPad) throws AddingNodesException {
        GCMApplication desc;
        try {
            desc = PAGCMDeployment.loadApplicationDescriptor(descriptorPad);
        } catch (ProActiveException e) {
            e.printStackTrace();
            throw new AddingNodesException(e);
        }

        Map<String, ? extends GCMVirtualNode> virtualNodes = desc.getVirtualNodes();
        for (Entry<String, ? extends GCMVirtualNode> entry : virtualNodes.entrySet()) {
            entry.getValue().subscribeNodeAttachment(this, "receiveDeployedNode", true);
        }

        desc.startDeployment();
        this.listPad.put(desc.toString(), desc);
    }

    /**
     * Receives a new deployed node and adding it to the NodeSource.
     * A new node has been acquired by the deployment mechanism.
     * Register it in the PADNodeSource.
     */
    public synchronized void receiveDeployedNode(Node node, String vnodeName) {
        this.myStub.addNewAvailableNode(node, vnodeName, vnodeName);
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
                this.rmCore.internalRemoveNode(entry.getKey(), preempt);
            }
        } else {
            //no nodes to remove, shutdown directly the NodeSource
            this.rmCore.internalRemoveSource(this.SourceId, this.getSourceEvent());
            // object should be terminated NON preemptively 
            // pinger thread can wait for last results (getNodes)
            PAActiveObject.terminateActiveObject(false);
        }
    }

    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            removeFromList(node);
            this.rmCore.setDownNode(nodeUrl);
        }
    }

    @Override
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(), RMConstants.PAD_NODE_SOURCE_TYPE);
    }

}
