package org.objectweb.proactive.p2p.v2.service.messages;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.p2p.v2.service.P2PService;


public class AcquaintanceRequest extends Message implements Serializable {
    public AcquaintanceRequest(int i) {
        super(i);
    }

    /**
     * Generates an acquaintance reply
     */
    public void execute(P2PService target) {
        if (!target.stubOnThis.equals(this.sender)) {
            Vector<String> result = target.acquaintanceManager_active.add(this.sender);
            result = (Vector<String>) ProFuture.getFutureValue(result);

            if (result == null) {
                //we have accepted the acquaintance request
                logger.info("Register request from " +
                    ProActiveObject.getActiveObjectNodeUrl(this.sender) +
                    " accepted");
                this.sender.message(new AcquaintanceReply(1,
                        target.generateUuid(), target.stubOnThis,
                        ProActiveObject.getActiveObjectNodeUrl(
                            target.stubOnThis)));
                //service.registerAnswer(ProActive.getActiveObjectNodeUrl(target.stubOnThis),target.stubOnThis);
            } else {
                logger.info("Register request from " +
                    ProActiveObject.getActiveObjectNodeUrl(this.sender) +
                    " rejected");
                //service.registerAnswer(ProActive.getActiveObjectNodeUrl(target.stubOnThis), result);
                this.sender.message(new AcquaintanceReply(1,
                        target.generateUuid(), target.stubOnThis,
                        ProActiveObject.getActiveObjectNodeUrl(
                            target.stubOnThis), result));
            }
        }
    }

    /**
     * This is message should not be forwarded
     */
    public void transmit(P2PService acq) {
    }
}
