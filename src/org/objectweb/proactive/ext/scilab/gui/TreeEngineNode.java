package org.objectweb.proactive.ext.scilab.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeEngineNode extends DefaultMutableTreeNode{

	public static final int VALID = 0, SUSPECT = 1;
	private int state = VALID;
	
	public TreeEngineNode(String strNode){
		super(strNode);
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public int getState(){
		return state;
	}

}
