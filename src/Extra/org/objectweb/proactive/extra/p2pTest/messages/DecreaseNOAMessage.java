package org.objectweb.proactive.extra.p2pTest.messages;

import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.messages.Message;


public class DecreaseNOAMessage extends Message {
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(P2PService target) {
        target.acquaintanceManager_active.setMaxNOA(target.acquaintanceManager_active.getMaxNOA() / 2);
    }

    @Override
    public void transmit(P2PService acq) {
        //NOTHING
    }
}
