package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.*;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

public class DataModelTraversal implements JobMonitorConstants
{
	private List keys;
	
	public DataModelTraversal (int [] _keys)
	{
		keys = new ArrayList();
		for (int i = 0; i < _keys.length; i++)
			keys.add(new Integer(_keys[i]));
	}
	
	private int indexOf(int key) {
		return keys.indexOf(new Integer(key));
	}
	
	public boolean hasFollowingKey (int key)
	{
		if (key == NO_KEY)
			return true;
		else
		{
			int index = indexOf (key);
			return (index >= 0 && index < keys.size() - 1);
		}
	}

	public int getFollowingKey (int key)
	{
		// root
		if (key == NO_KEY)
			return ((Integer) keys.get (0)).intValue();
		else
		{
			int index = indexOf (key);
			return ((Integer) keys.get (indexOf (key) + 1)).intValue();
		}
	}
}
