package org.objectweb.proactive.p2p.v2.service.messages;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;

public abstract class Message implements Serializable {
	
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_MESSAGE);

    protected int TTL;
    protected UniversalUniqueID uuid;
	protected P2PService sender;
    
	public Message() {}
	
	
	public Message(int ttl) {
		this.TTL=ttl;
	}
	
    public Message(int ttl, UniversalUniqueID id, P2PService sender) {
    	this.TTL = ttl;
    	this.uuid = id;
    	this.sender=sender;
    }

	public int getTTL() {
		return TTL;
	}

	public void setTTL(int ttl) {
		TTL = ttl;
	}

	public UniversalUniqueID getUuid() {
		return uuid;
	}

	public void setUuid(UniversalUniqueID uuid) {
		this.uuid = uuid;
	}

	public P2PService getSender() {
		return sender;
	}
	
	public void setSender(P2PService s) {
		this.sender = s;
	}
	/**
	 * Execute the message on the given local target
	 * @param target
	 */
	public abstract void execute(P2PService target);
	
	/**
	 * Transmits the message to the next peer
	 * @param acq
	 */
	public abstract void transmit (P2PService acq);
		
}
