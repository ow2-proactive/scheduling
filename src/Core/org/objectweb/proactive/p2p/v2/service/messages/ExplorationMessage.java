package org.objectweb.proactive.p2p.v2.service.messages;

import java.io.Serializable;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class ExplorationMessage extends BreadthFirstMessage
    implements Serializable {
    public ExplorationMessage(int ttl, UniversalUniqueID id, P2PService sender) {
        super(ttl, id, sender);
    }

    @Override
    public void execute(P2PService target) {
        // This should be register
        if (target.shouldBeAcquaintance(this.sender)) {
            //  target.register(this.sender);
            //we cannot resister the sender as one of our peer yet
            //because he might have received replies
            //and have reached its NOA
            try {
                String[] result = (String[]) null; //this.sender.registerRequest(target.stubOnThis).toArray(new String[] {});

                if (result == null) {
                    logger.info("ExplorationMessage me = " +
                        P2PService.getHostNameAndPortFromUrl(
                            ProActiveObject.getActiveObjectNodeUrl(target.stubOnThis)) +
                        " adding " +
                        P2PService.getHostNameAndPortFromUrl(
                            ProActiveObject.getActiveObjectNodeUrl(this.sender)));
                    //indeed, the peer really wants us
                    //     target.registerRequest(this.sender);
                    target.getAcquaintanceManager()
                          .startAcquaintanceHandShake(P2PService.getHostNameAndPortFromUrl(
                            ProActiveObject.getActiveObjectNodeUrl(this.sender)),
                        this.sender);
                }
            } catch (Exception e) {
                logger.info("Trouble with registering remote peer", e);
                target.acquaintanceManager_active.remove(this.sender, null);
            }
        }
    }
}
