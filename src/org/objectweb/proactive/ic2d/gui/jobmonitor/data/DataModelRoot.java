package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.ArrayList;

import javax.swing.tree.TreeNode;

public class DataModelRoot extends DataModelNode
{
	private String firstKey;
	private DataModelNode decoratedRoot;
	private DataModelTraversal traversal;
	private NodeHelper helper;
	
	public DataModelRoot (String name, DataModelTraversal _traversal, NodeHelper _helper, DataModelNode _decoratedRoot)
	{
		super (name, null);
		
		traversal = _traversal;
		firstKey = traversal.getFollowingKey (null);
		
		helper = _helper;
		
		decoratedRoot = _decoratedRoot;
//		System.out.println ("[" + name + "] - decorated root: " + _decoratedRoot.getName() + "." + _decoratedRoot.hashCode());
	}
	
	public TreeNode getChildAt (int index)
	{
		return decoratedRoot.getChildAt (firstKey, index, traversal, helper);
	}

	public int getChildCount ()
	{
		return decoratedRoot.getChildCount (firstKey, traversal, helper);
	}
	
	public boolean isLeaf ()
	{
		return decoratedRoot.isLeaf (firstKey, traversal, helper);
	}
	
	public DataModelNode getChildAt (String key, int index, DataModelTraversal traversal, NodeHelper helper)
	{
		return decoratedRoot.getChildAt (key, index, traversal, helper);
	}
	public int getChildCount (String key, DataModelTraversal traversal, NodeHelper helper)
	{
		return decoratedRoot.getChildCount (key, traversal, helper);
	}
	public ArrayList getChildren (String key)
	{
		return decoratedRoot.getChildren (key);
	}
	public boolean isLeaf (String key, DataModelTraversal traversal, NodeHelper helper)
	{
		return decoratedRoot.isLeaf (key, traversal, helper);
	}
	public DataModelNode getChild (String name, String key)
	{
		return decoratedRoot.getChild (name, key);
	}
	public int getIndex (String key, DataModelNode node)
	{
		return decoratedRoot.getIndex (key, node);
	}
	public boolean hasChild (DataModelNode child)
	{
		return decoratedRoot.hasChild (child);
	}
	public boolean hasChild (String _name, String _key)
	{
		return decoratedRoot.hasChild (_name, _key);
	}
	public void removeAllChildren ()
	{
		decoratedRoot.removeAllChildren ();
	}
}