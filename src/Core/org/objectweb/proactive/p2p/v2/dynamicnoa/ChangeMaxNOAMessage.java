package org.objectweb.proactive.p2p.v2.dynamicnoa;

import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.messages.Message;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class ChangeMaxNOAMessage extends Message {
    protected int noa;

    public ChangeMaxNOAMessage(int ttl, int noa) {
        super(ttl);
        this.noa = noa;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute(P2PService target) {
        target.getAcquaintanceManager().setMaxNOA(this.noa);
    }

    /**
     * Nothing to do, the message should not be transmited
     */
    public void transmit(P2PService acq) {
    }
}
