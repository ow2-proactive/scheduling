package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.objectweb.proactive.ic2d.gui.jobmonitor.Icons;
import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorConstants;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeModel;

public class SwitcherModel implements JobMonitorConstants
{
	private List labels;
	private List keys;
	
	private DataTreeModel treeModel;
	
	public SwitcherModel (int[] _keys)
	{
		labels = new ArrayList();
		keys = new ArrayList();
		
		for (int i = 0; i < _keys.length; i++) {
			labels.add(NAMES[KEY2INDEX[_keys[i]]]);
			keys.add(new Integer(_keys[i]));
		}
	}

	public int get (String label)
	{
		int i = labels.indexOf (label);
		return getSwitchKey (i);
	}
	
	public int size()
	{
		return labels.size();
	}
	
	public String getLabel (int i)
	{
		if (i < 0 || i >= size())
			return null;
		
		return (String) labels.get (i);
	}

	public int getSwitchKey (int i)
	{
		if (i < 0 || i >= size())
			return NO_KEY;
			
		return ((Integer) keys.get (i)).intValue();
	}
	
	public Icon getIcon(int i) {
		if (i < 0 || i >= size())
			return null;
		
		int key = getSwitchKey(i);
		return Icons.getIconForKey(key);
	}
	
	public void setTreeModel(DataTreeModel treeModel) {
		this.treeModel = treeModel;
	}
	
	private int labelToKey(String label) {
		return ((Integer) keys.get(labels.indexOf(label))).intValue();
	}
	
	public void toggleHighlighted(String label) {
		int key = labelToKey(label);
		treeModel.toggleHighlighted(key);
	}
	
	public boolean isHighlighted(String label) {
		int key = labelToKey(label);
		return treeModel.isHighlighted(key);
	}
	
	public void setHidden(String label, boolean hide) {
		int key = labelToKey(label);
		treeModel.setHidden(key, hide);
	}
	
	public boolean isHidden(String label) {
		int key = labelToKey(label);
		return treeModel.isHidden(key);
	}
	
	public void exchange(String fromLabel, String toLabel) {
		int fromKey = labelToKey(fromLabel);
		int toKey = labelToKey(toLabel);
		treeModel.exchange(fromKey, toKey);
	}
}
