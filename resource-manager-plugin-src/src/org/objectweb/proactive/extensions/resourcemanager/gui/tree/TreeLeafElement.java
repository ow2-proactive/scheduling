package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

/**
 * @author FRADJ Johann
 */
public abstract class TreeLeafElement {

	private String name = null;
	private TreeElementType type = null;
	private TreeParentElement parent;

	public TreeLeafElement(String name, TreeElementType type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * To get the name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * To get the type
	 * 
	 * @return the type
	 */
	public TreeElementType getType() {
		return type;
	}

	public void setParent(TreeParentElement parent) {
		this.parent = parent;
	}

	public TreeParentElement getParent() {
		return parent;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
}
