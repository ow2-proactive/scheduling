package org.objectweb.proactive.p2p.v2.service.messages;

import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;

public abstract class BreadthFirstMessage extends Message{

	public BreadthFirstMessage() {}
	
	public BreadthFirstMessage(int ttl, UniversalUniqueID id, P2PService sender) {
		super(ttl, id, sender);
	}

	@Override
	public void transmit(P2PService acq) {
		acq.message(this);
	}
}
