package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.Iterator;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

public class DataTreeNode extends DefaultMutableTreeNode implements JobMonitorConstants {
	public static final int STATE_NEW = 0;
	public static final int STATE_REMOVED = 1;
	public static final int STATE_KEPT = 2;
	
	private int state = STATE_NEW;
	private int key = NO_KEY;
	private String name;
	
	public DataTreeNode(DataModelTraversal traversal) {
		key = traversal.getFollowingKey(NO_KEY);
	}
	
	public DataTreeNode(DataTreeModel model, int key, String name) {
		rebuild(model, key, name);
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	public void setAllStates(int state) {
		setState(state);
		for (int i = 0, length = getChildCount(); i < length; i++) {
			DataTreeNode child = (DataTreeNode) getChildAt(i);
			child.setAllStates(state);
		}
	}
	
	public DataTreeNode getChildByName(String childName) {
		for (int i = 0, length = getChildCount(); i < length; i++) {
			DataTreeNode child = (DataTreeNode) getChildAt(i);
			if (child.getName().equals(childName))
				return child;
		}
		return null;
	}
	
	private void handleRemovedChildren(DataTreeModel model) {
		for (int i = 0; i < getChildCount(); i++) {
			DataTreeNode child = (DataTreeNode) getChildAt(i);
			switch (child.state) {
				case STATE_REMOVED:
					model.removeNodeFromParent(child);
					i--;
					break;
					
				case STATE_KEPT:
					child.handleRemovedChildren(model);
					break;
			}
		}
	}
	
	/* key : la cle de cette branche, les fils sont donc des traversal.getFollowingKey(key) */
	public void rebuild(DataTreeModel model, int key, String name) {
		this.key = key;
		this.name = name;
		DataModelTraversal traversal = model.getTraversal();
		if (!traversal.hasFollowingKey(key))
			return;
		
		int nextKey = traversal.getFollowingKey(key);
		if (key == NO_KEY)
			this.key = nextKey;
		
		DataAssociation asso = model.getAssociations();
		Set children = asso.getValues(key, name, nextKey);
		Iterator iter = children.iterator();
		while (iter.hasNext()) {
			String childName = (String) iter.next();
			DataTreeNode child = getChildByName(childName);
			if (child != null) {
				child.state = STATE_KEPT;
				child.rebuild(model, nextKey, childName);
			} else {
				DataTreeNode newChild = new DataTreeNode(model, nextKey, childName);
				model.insertNodeInto(newChild, this, getChildCount());
			}
		}
		
		handleRemovedChildren(model);
	}
	
	public int getKey() {
		/* For the TreeCellRenderer */
		if (name == null)
			return NO_KEY;
		
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		if (name == null)
			return NAMES[key];
		
		return name;
	}
}
