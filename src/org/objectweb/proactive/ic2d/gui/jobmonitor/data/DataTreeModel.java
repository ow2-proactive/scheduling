package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import javax.swing.tree.DefaultTreeModel;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitchEvent;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitchListener;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitcherModel;

public class DataTreeModel extends DefaultTreeModel implements JobMonitorConstants, SwitchListener
{
	private DataAssociation asso;
	private DataModelTraversal traversal;
	private SwitcherModel smodel;
	private NodeHelper helper;
	
	public DataTreeModel (DataAssociation _asso, SwitcherModel _smodel, DataModelTraversal _traversal, NodeHelper _helper)
	{
		super(new DataTreeNode(_traversal));
		asso = _asso;
		traversal = _traversal;
		smodel = _smodel;
		helper = _helper;
		
		smodel.addSwitchListener (this);
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
	
	public void switchPerformed (SwitchEvent e)
	{
		nodeStructureChanged (root);
	}
	
	public SwitcherModel getSwitcherModel ()
	{
		return smodel;
	}
}
