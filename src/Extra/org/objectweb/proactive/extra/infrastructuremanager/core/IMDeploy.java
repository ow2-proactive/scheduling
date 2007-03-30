package org.objectweb.proactive.extra.infrastructuremanager.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class IMDeploy implements NodeCreationEventListener, Runnable {
	
	private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DEPLOY);
	
    // Attributes
	private String padName = null;
    private ProActiveDescriptor pad = null;
    private String[] vnNames = null;
    private IMCore imCore = null;

    
    //----------------------------------------------------------------------//
    // Construtors
    
    
    /**
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     */
    public IMDeploy(IMCore imCore, String padName, ProActiveDescriptor pad) {
        this.imCore = imCore;
        this.padName = padName;
        this.pad = pad;
    }

    /**
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     * @param vnNames : the name of the virtual nodes of this pad to deploy
     */
    public IMDeploy(IMCore imCore, String padName, ProActiveDescriptor pad, String[] vnNames) {
        this.imCore = imCore;
        this.padName = padName;
        this.pad = pad;
        this.vnNames = vnNames;
    }

    
    //----------------------------------------------------------------------//

    /**
     * Implementation of the method run to interface Runnable
     */
    public void run() {
        VirtualNode[] vns = null;
        if (vnNames == null) {
            vns = pad.getVirtualNodes();
        } else {
            vns = new VirtualNode[vnNames.length];
            for (int i = 0; i < this.vnNames.length; i++) {
                vns[i] = pad.getVirtualNode(this.vnNames[i]);
            }
        }
        for (VirtualNode vn : vns) {
            ((VirtualNodeImpl) vn).addNodeCreationEventListener(this);
            vn.activate();
        }

        for (VirtualNode vn : vns) {
            try {
                ((VirtualNodeImpl) vn).waitForAllNodesCreation();
            } catch (NodeException e) {
            	logger.warn("NodeException : " + e, e);
            }
        }
        this.imCore.addPAD(padName, pad);
    }

    
    /**
     * When a node is activated this method is call for saving
     * the new activated nodes in the dataresource. 
     * @param event 
     */
    public void nodeCreated(NodeCreationEvent event) {
        Node node = event.getNode();
        ProActiveRuntime par = node.getProActiveRuntime();
        try {
            String vnName = par.getVNName(node.getNodeInformation().getName()); 
            this.imCore.addNode(node, vnName, padName);
        } catch (ProActiveException e) {
        	logger.warn("ProActiveException : " + e, e);
        }
    }
}