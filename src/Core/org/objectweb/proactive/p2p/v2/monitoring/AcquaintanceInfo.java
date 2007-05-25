package org.objectweb.proactive.p2p.v2.monitoring;

import java.io.Serializable;

public class AcquaintanceInfo implements Serializable{

	protected String sender;
	protected String[] acq;
	protected int noa;
	protected int currentNoa;
	protected String[] awaitedReplies;
	
	public AcquaintanceInfo() {}
	
	public AcquaintanceInfo(String sender, String[] acq) {
		this.sender = sender;
		this.acq =acq;
	}

	public AcquaintanceInfo(String sender, String[] acq, int noa, int currentNoa) {
		this.sender = sender;
	    this.acq =acq;
	    this.noa=noa;
	    this.currentNoa=currentNoa;
	}
	
	public AcquaintanceInfo(String sender, String[] acq, int noa, int currentNoa,String[] awaited) {
		this(sender,acq,noa,currentNoa);
		this.awaitedReplies= awaited;
	}
	
	
	public String[] getAcq() {
		return acq;
	}

	public String getSender() {
		return sender;
	}

	public int getNoa() {
		return noa;
	}
	
	public int getCurrentNoa() {
		return this.currentNoa;
	}

	public String[] getAwaitedReplies() {
		return awaitedReplies;
	}
 	
	
}
