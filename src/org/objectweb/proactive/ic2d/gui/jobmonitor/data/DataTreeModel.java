package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import javax.swing.tree.DefaultTreeModel;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitcherModel;

public class DataTreeModel extends DefaultTreeModel implements JobMonitorConstants
{
	private DataAssociation asso;
	private DataModelTraversal traversal;
	private SwitcherModel smodel;
	private boolean[] highlighted;
	
	public DataTreeModel (DataAssociation _asso, SwitcherModel _smodel, DataModelTraversal _traversal)
	{
		super(new DataTreeNode(_traversal));
		asso = _asso;
		traversal = _traversal;
		smodel = _smodel;
		highlighted = new boolean[NB_KEYS];
		
		smodel.setTreeModel(this);
	}
	
	public DataTreeNode root() {
		return (DataTreeNode) getRoot();
	}
	
	public void rebuild() {
		rebuild(root());
	}
	
	public void rebuild(DataTreeNode node) {
		node.setAllStates(DataTreeNode.STATE_REMOVED);
		node.rebuild(this, NO_KEY, null);
	}

	public DataModelTraversal getTraversal() {
		return traversal;
	}
	
	public DataAssociation getAssociations() {
		return asso;
	}
		
	public SwitcherModel getSwitcherModel ()
	{
		return smodel;
	}
	
	public void toggleHighlighted(int key) {
		if (key != NO_KEY)
			highlighted[KEY2INDEX[key]] = !highlighted[KEY2INDEX[key]];
	}
	
	public boolean isHighlighted(int key) {
		if (key != NO_KEY)
			return highlighted[KEY2INDEX[key]];

		return false;
	}
	
	public void setHidden(int key, boolean hide) {
		if (key != NO_KEY) {
			traversal.setHidden(key, hide);
			rebuild();
		}
	}
	
	public boolean isHidden(int key) {
		return traversal.isHidden(key);
	}
	
	public void exchange(int fromKey, int toKey) {
		traversal.exchange(fromKey, toKey);
		rebuild();
	}
}
