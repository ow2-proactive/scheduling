package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

public class DataModelNode extends DefaultMutableTreeNode
{
	private HashMap children;
	private String mName;
	private String mKey;
	
	private static final ArrayList EMPTY_LIST = new ArrayList(); 
	
	public DataModelNode (String _name, String _key)
	{
		children = new HashMap();
		mName = _name;
		mKey = _key;
	}
	
	public ArrayList getChildren (String key)
	{
		ArrayList list = (ArrayList) children.get (key);
		
		if (list == null)
			return EMPTY_LIST;
		
		return list;
	}

	public boolean hasChild (DataModelNode child)
	{
//		System.out.println (name + " has child: " + child);
		return getIndex (child.getKey(), child) >= 0;
	}
	
	public boolean hasChild (String _name, String _key)
	{
//		System.out.println (name + " has child - name: " + _name + " , key: " + _key);
		ArrayList list = getChildren (_key);
		for (int i = 0, size = list.size(); i < size; ++i)
		{
			DataModelNode node  = (DataModelNode) list.get (i);
			if (_name.equalsIgnoreCase (node.getName()))
				return true;
		}
		
		return false;
	}
	
	/*public boolean equals (Object obj)
	{
		if (! (obj instanceof DataModelNode))
			return false;

		DataModelNode node = (DataModelNode) obj;
		String otherName = node.getName();
		String otherKey = node.getKey();
		
		if (key == null && otherKey == null)
			return true;
	
		return name.equalsIgnoreCase (otherName) && key.equalsIgnoreCase (otherKey);
	}*/
	
	public DataModelNode getChild (String name, String key)
	{
		ArrayList list = getChildren (key);
		for (int i = 0, size = list.size(); i < size; ++i)
		{
			DataModelNode node  = (DataModelNode) list.get (i);
			if (name.equalsIgnoreCase (node.getName()))
				return node;
		}
		
		DataModelNode child = new DataModelNode (name, key);
		add (child);
		
		return child;
	}
	
	public void add (DataModelNode child)
	{
		if (! hasChild (child))
			add (child.getKey(), child);
		else
			System.out.println ("Node [" + getName() + "] has already the child named [" + child.getName() + "] on its key [" + child.getKey() + "]" );
	}
	
	public void add (String key, Object child)
	{
		ArrayList childrenOfKey = (ArrayList) children.get (key);
		if (childrenOfKey == null)
		{
			childrenOfKey = new ArrayList();
			children.put (key, childrenOfKey);
		}
		
		childrenOfKey.add (child);
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String toString()
	{
//		return toXml (false);
		return getName();
	}
	
	public String toXml()
	{
		return toXml (false);
	}
	
	public String toXml (boolean recurseImmediateChildren)
	{
		String s = "<datamodelnode name='" + mName + "'>\n";
		
		Iterator i = children.keySet().iterator();
		while (i.hasNext())
		{
			String key = (String) i.next();
			s += "<children key='" + key + "'>\n";
			Iterator j = getChildren (key).iterator();
			while (j.hasNext())
			{
				Object next = j.next();
				String child = (next instanceof DataModelNode && recurseImmediateChildren ? ((DataModelNode) next).toXml() : next.toString());
				
				s+= "<child>" + child + "</child>\n";
			}
			
			s += "</children>\n"; 
		}
	
		s += "</datamodelnode>\n";
		
		return s;
	}
	
	public DataModelNode getChildAt (String key, int index)
	{
//		System.out.println("Node " + name + " - key " + key);
		return (DataModelNode) getChildren (key).get (index);
	}

	public int getChildCount (String key)
	{
//		System.out.println("Node " + name + " - key " + key + " - nb sons: " + getChildren (key).size());
		return getChildren (key).size();
	}

	public boolean isLeaf (String key)
	{
		return (getChildCount (key) == 0);
	}
	
	public int getIndex (String key, DataModelNode node)
	{
		return getChildren (key).indexOf (node);
	}
	
	public String getKey()
	{
		return mKey;
	}
	
	/*public TreeNode getChildAt (int index)
	{
//		System.out.println("super - Node " + name + " - key " + key);
		return super.getChildAt (index);
	}
	
	public int getChildCount()
	{
//		System.out.println("super - Node " + name + " - key " + key + " - nb sons: " + super.getChildCount());
		return super.getChildCount();
	}*/
	
	public static DataModelNode createModelRootInstance ()
	{
		return new DataModelNode (null, null);
	}
	
	public void removeAllChildren ()
	{
		children.clear();
	}
}
