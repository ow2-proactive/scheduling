package org.objectweb.proactive.extra.p2pTest.messages;

import org.objectweb.proactive.extra.p2pTest.p2p.Tracer;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.messages.BreadthFirstMessage;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class TracerWithCallbackMessage extends BreadthFirstMessage {
    private static final long serialVersionUID = 1L;
    Tracer tracer = null;

    public TracerWithCallbackMessage(int ttl, UniversalUniqueID id, Tracer t) {
        this.TTL = ttl;
        this.uuid = id;
        this.tracer = t;
    }

    @Override
    public void execute(P2PService target) {
        int nb = target.getAcquaintanceList().size();
        tracer.trace(target.getAddress().toString(), nb);
    }
}
