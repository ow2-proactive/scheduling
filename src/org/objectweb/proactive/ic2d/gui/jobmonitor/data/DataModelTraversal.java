package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import java.util.*;

import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;

public class DataModelTraversal implements JobMonitorConstants
{
	private List keys;
	private boolean[] hidden;
	
	public DataModelTraversal (int [] _keys)
	{
		keys = new ArrayList();
		for (int i = 0; i < _keys.length; i++)
			keys.add(new Integer(_keys[i]));
		
		hidden = new boolean[_keys.length];
	}
	
	private int indexOf(int key) {
		return keys.indexOf(new Integer(key));
	}
	
	public boolean hasFollowingKey (int key)
	{
		return getFollowingKey(key) != NO_KEY;
	}

	public int getFollowingKey (int key)
	{
		int newIndex, newKey;
		if (key == NO_KEY)
			// root
			newIndex = 0;
		else {
			int index = indexOf (key);
			newIndex = index + 1;
		}
		
		do {
			if (newIndex == keys.size())
				return NO_KEY;
			newKey = ((Integer) keys.get(newIndex)).intValue();
			newIndex++;
		} while (isHidden(newKey));
		
		return newKey;
	}
	
	public void setHidden(int key, boolean hide) {
		if (key != NO_KEY)
			hidden[KEY2INDEX[key]] = hide;
	}
	
	public boolean isHidden(int key) {
		if (key != NO_KEY)
			return hidden[KEY2INDEX[key]];
		
		return false;
	}
}
