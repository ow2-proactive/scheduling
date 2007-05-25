package org.objectweb.proactive.p2p.v2.monitoring;

public class P2PNode {

	protected String name;
	//the current node index
	//if -1, indicates that this node has not been seen
	//as a sender
	protected int index;
	protected int maxNOA;
	protected int noa;
	
	public P2PNode(String name) {
		this.name =name;
		this.index=-1;
	}
	public P2PNode(String name, int index) {
		this.name = name;
		this.index = index;
	}
	
	public P2PNode(String name, int index,  int noa, int maxNOA) {
		this(name, index);
		this.noa = noa;
		this.maxNOA = maxNOA;
	}
	
	public int getIndex() {
		return index;
	}
	public String getName() {
		return name;
	}
	public int getMaxNOA() {
		return maxNOA;
	}
	public int getNoa() {
		return noa;
	}
	public void setIndex(int i) {
		this.index= i;
	}
	public void setMaxNOA(int maxNOA) {
		this.maxNOA = maxNOA;
	}
	public void setNoa(int noa) {
		this.noa = noa;
	}
	
	
	
	
}
