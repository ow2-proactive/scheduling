package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.Arrays;
import java.util.List;

public class DataModelTraversal
{
	private List keys;
	
	public DataModelTraversal (String [] _keys)
	{
		keys = Arrays.asList (_keys);
	}
	
	public boolean hasFollowingKey (String key)
	{
		if (key == null)
			return true;
		else
		{
			int index = keys.indexOf (key);
			return (index >= 0 && index < keys.size() - 1);
		}
	}

	public String getFollowingKey (String key)
	{
		// root
		if (key == null)
			return (String) keys.get (0);
		else
		{
			int index = keys.indexOf (key);
			return (String) keys.get (keys.indexOf (key) + 1);
		}
	}
}