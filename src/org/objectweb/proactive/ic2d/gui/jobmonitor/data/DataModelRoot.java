package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.ArrayList;

import javax.swing.tree.TreeNode;

public class DataModelRoot extends DataModelNode
{
	private String firstKey;
	DataModelNode decoratedRoot;
	
	public DataModelRoot (String name, DataModelTraversal traversal, DataModelNode _decoratedRoot)
	{
		super (name, null);
		firstKey = traversal.getFollowingKey (null);
		decoratedRoot = _decoratedRoot;
//		System.out.println ("[" + name + "] - decorated root: " + _decoratedRoot.getName() + "." + _decoratedRoot.hashCode());
	}
	
	public TreeNode getChildAt (int index)
	{
		return decoratedRoot.getChildAt (firstKey, index);
	}

	public int getChildCount ()
	{
		return decoratedRoot.getChildCount (firstKey);
	}
	
	public boolean isLeaf ()
	{
		return decoratedRoot.isLeaf (firstKey);
	}
	
	public DataModelNode getChildAt (String key, int index)
	{
		return decoratedRoot.getChildAt (key, index);
	}
	public int getChildCount (String key)
	{
		return decoratedRoot.getChildCount (key);
	}
	public ArrayList getChildren (String key)
	{
		return decoratedRoot.getChildren (key);
	}
	public boolean isLeaf (String key)
	{
		return decoratedRoot.isLeaf (key);
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