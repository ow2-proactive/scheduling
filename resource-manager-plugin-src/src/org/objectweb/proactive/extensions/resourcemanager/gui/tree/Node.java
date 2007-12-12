package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

import org.objectweb.proactive.extensions.resourcemanager.common.NodeState;


/**
 * @author FRADJ Johann
 */
public class Node extends TreeLeafElement {

	private NodeState state = null;

	public Node(String name, NodeState state) {
		super(name, TreeElementType.NODE);
		this.state = state;
	}

	/**
	 * To get the state
	 * 
	 * @return the state
	 */
	public NodeState getState() {
		return state;
	}

	/**
	 * To set the state
	 * 
	 * @param state the state to set
	 */
	public void setState(NodeState state) {
		this.state = state;
	}
}
