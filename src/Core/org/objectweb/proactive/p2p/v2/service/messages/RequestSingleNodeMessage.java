package org.objectweb.proactive.p2p.v2.service.messages;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.node.P2PNode;
import org.objectweb.proactive.p2p.v2.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class RequestSingleNodeMessage extends RandomWalkMessage {
    protected String vnName;
    protected String jobId;
    protected P2PNodeLookup lookup;
    
    public RequestSingleNodeMessage(int ttl, UniversalUniqueID uuid, P2PService service, P2PNodeLookup lookup, String vnName, String jobId
        ) {
    	this.TTL = ttl;
    	this.uuid = uuid;
        this.sender = service;
        this.vnName = vnName;
        this.jobId = jobId;
        this.lookup=lookup;
    }

    @Override
    public void execute(P2PService target) {
        P2PNode askedNode = target.nodeManager.askingNode(null);
        Node nodeAvailable = askedNode.getNode();
        if (nodeAvailable != null) {
            if (vnName != null) {
                try {
                    nodeAvailable.getProActiveRuntime()
                                 .registerVirtualNode(vnName, true);
                } catch (Exception e) {
                    logger.warn("Couldn't register " + vnName + " in the PAR", e);
                }
            }
            if (jobId != null) {
                nodeAvailable.getNodeInformation().setJobID(jobId);
            }
            try {
              lookup.giveNode(nodeAvailable,
                        askedNode.getNodeManager());
            } catch (Exception lookupExcption) {
                logger.info("Cannot contact the remote lookup",
                    lookupExcption);
                target.nodeManager.noMoreNodeNeeded(nodeAvailable);
                return;
            }
        }
        
        
    }

//    @Override
//    public void transmit(P2PService acq) {
//    	System.out.println("RequestSingleNodeMessage.transmit()");
//        acq.randomPeer().getANode(this);
//    }
}
