package org.objectweb.proactive.ic2d.gui.jobmonitor;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.*;

public class NodeExploration implements JobMonitorConstants {
	private int maxDepth;
	private DataAssociation asso;
	
	public NodeExploration(DataAssociation asso) {
		this.maxDepth = 1;
		this.asso = asso;
	}
	
	public int getMaxDepth() {
		return maxDepth;
	}
	
	public void setMaxDepth(int maxDepth) {
		if (maxDepth > 0)
			this.maxDepth = maxDepth;
	}
	
	public void explore() {
		
	}
}