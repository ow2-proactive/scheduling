package org.objectweb.proactive.p2p.v2.service.messages;

import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;

public abstract class RandomWalkMessage extends Message {

	public RandomWalkMessage(){}
	
	public RandomWalkMessage(int ttl, UniversalUniqueID id, P2PService sender) {
		super(ttl, id, sender);
	}

	@Override
	public void transmit(P2PService acq) {
	    	System.out.println("RequestSingleNodeMessage.transmit()");
	        acq.randomPeer().message(this);
	}

}
