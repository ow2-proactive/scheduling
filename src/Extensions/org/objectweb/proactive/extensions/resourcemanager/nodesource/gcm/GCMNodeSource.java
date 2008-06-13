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
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#initActivity(org.objectweb.proactive.Body)
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
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#nodeRemovalCoreRequest(java.lang.String, boolean)
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
                node.getProActiveRuntime().killRT(false);
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
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#nodeAddingCoreRequest(java.lang.String)
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
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#nodesAddingCoreRequest(org.objectweb.proactive.gcmdeployment.GCMApplication)
     */
    @Override
    public void nodesAddingCoreRequest(GCMApplication app) throws AddingNodesException {
        Map<String, GCMVirtualNode> virtualNodes = app.getVirtualNodes();
        for (Entry<String, GCMVirtualNode> entry : virtualNodes.entrySet()) {
            try {
                entry.getValue().subscribeNodeAttachment(this, "receiveDeployedNode", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        app.startDeployment();
        this.listGCMApp.put(app.toString(), app);
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
    public void shutdown(boolean preempt) {
        super.shutdown(preempt);
        if (this.nodes.size() > 0) {
            for (Entry<String, Node> entry : this.nodes.entrySet()) {
                this.rmCore.nodeRemovalNodeSourceRequest(entry.getKey(), preempt);
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
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#detectedPingedDownNode(java.lang.String)
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
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#getSourceEvent()
     */
    @Override
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(), RMConstants.PAD_NODE_SOURCE_TYPE);
    }

}
