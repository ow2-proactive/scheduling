package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.ArrayList;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitchEvent;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitchListener;
import org.objectweb.proactive.ic2d.gui.jobmonitor.switcher.SwitcherModel;

public class DataTreeModel extends DefaultTreeModel implements JobMonitorConstants, SwitchListener
{
	private DataModelTraversal traversal;
	private SwitcherModel smodel;
	private NodeHelper helper;
	
	public DataTreeModel (DataModelRoot root, SwitcherModel _smodel, DataModelTraversal _traversal, NodeHelper _helper)
	{
		super (root);
		
		traversal = _traversal;
		
		smodel = _smodel;
		
		helper = _helper;
		
		smodel.addSwitchListener (this);
	}
		
	public Object getChild (Object _parent, int index)
	{
//		System.out.println("getChild: " + _parent.hashCode() + " - parent == root: " + (_parent == root));
		DataModelNode parent = (DataModelNode) _parent;
		String nextKey = traversal.getFollowingKey (parent.getKey());
		return parent.getChildAt (nextKey, index, traversal, helper);
	}

	public int getChildCount (Object _parent)
	{
		DataModelNode parent = (DataModelNode) _parent;
//		System.out.println("getChildCount: " + ((DataModelNode2) parent).getName() + " - parent == root: " + (parent == root));
		String parentKey = parent.getKey();
//		System.out.println ("key: " + parentKey + " - has following key: " + traversal.hasFollowingKey (parentKey));
		if (!traversal.hasFollowingKey (parentKey))
			return 0;
		
		return parent.getChildCount (traversal.getFollowingKey (parentKey), traversal, helper);
	}

	public int getIndexOfChild (Object _parent, Object _child)
	{
		if (_parent == null || _child == null)
			return -1;
		
		DataModelNode parent = (DataModelNode) _parent;
		DataModelNode child = (DataModelNode) _child;
		String nextKey = child.getKey();
		
		return parent.getIndex (nextKey, child);
	}
	
	public boolean isLeaf (Object _node)
	{
		DataModelNode node = (DataModelNode) _node;
		String key = node.getKey();
		if (!traversal.hasFollowingKey (key))
			return true;
		
		return node.isLeaf (traversal.getFollowingKey (key), traversal, helper);
	}
	
	public void update ()
	{
		nodeStructureChanged ((TreeNode) getRoot());
	}
	
	public void removeHostChildren (String host)
	{		
		DataModelNode root = (DataModelNode) getRoot();
		
		String key = traversal.getFollowingKey (null); 
		
		if (key == HOST)
		{
			// Host view
			boolean hasChild = root.hasChild (host, HOST);
			if (hasChild)
			{
				DataModelNode h = root.getChild (host, HOST);
				h.removeAllChildren();
			}
		}
		else if (key == JOB)
		{
			key = traversal.getFollowingKey (key);
			
			ArrayList jobs = root.getChildren (JOB);
			for (int i = 0, size = jobs.size(); i < size; ++i)
			{
				DataModelNode job = (DataModelNode) jobs.get (i);
				if (key == VN)
				{
					ArrayList vns = job.getChildren (VN);
					for (int j = 0, sizej = vns.size(); j < sizej; ++j)
					{
						DataModelNode vn = (DataModelNode) vns.get (j);
						if (vn.hasChild (host, HOST))
							vn.getChild (host, HOST).removeAllChildren();
					}
				}
				else
				{
					if (job.hasChild (host, HOST))
						job.getChild (host, HOST).removeAllChildren();
				}			
			}
		}
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