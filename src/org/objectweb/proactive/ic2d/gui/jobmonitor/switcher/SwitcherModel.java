package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import org.objectweb.proactive.ic2d.gui.jobmonitor.*;

public class SwitcherModel implements JobMonitorConstants
{
	private List labels;
	private List keys;
	private boolean [] states;
	
	private Vector listeners;
	
	public SwitcherModel (int[] _keys)
	{
		labels = new ArrayList();
		keys = new ArrayList();
		
		for (int i = 0; i < _keys.length; i++) {
			labels.add(NAMES[_keys[i]]);
			keys.add(new Integer(_keys[i]));
		}

		states = new boolean [labels.size()];
		for (int i = 0; i < states.length; ++i)
			states [i] = true;
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
	
	protected void fireSwitchEvent (SwitchEvent e)
	{
		if (listeners == null)
			return;
		
		for (int i = 0, size = listeners.size(); i < size; ++i)
			((SwitchListener) listeners.get(i)).switchPerformed (e); 
	}
	
	public void addSwitchListener (SwitchListener listener)
	{
		if (listener == null)
			return;
			
		if (listeners == null)
			listeners = new Vector();
		
		listeners.add (listener);
	}

	public void removeSwitchListener (SwitchListener listener)
	{
		if (listener == null || listeners == null)
			return;
					
		listeners.remove (listener);
	}

	public SwitchListener[] getSwitchListeners()
	{
		return (listeners == null ? new SwitchListener [] {} : (SwitchListener []) listeners.toArray (new SwitchListener[]{}));
	}

	public boolean isStateONLabel (String label)
	{
		int i = labels.indexOf (label);
		return getStateAt (i);
	}

	public boolean isStateONKey (int key)
	{
		int i = keys.indexOf (new Integer(key));
		return getStateAt (i);
	}

	private boolean getStateAt (int i)
	{
		if (i < 0)
			return false;
			
		return states [i];
	}

	public boolean switchStateLabel (String label)
	{
		int i = labels.indexOf (label);
		return switchAndNotify (i);
	}

	private boolean switchAndNotify(int i)
	{
		if (i < 0)
			return false;
		
		states [i] = !states[i];
		
		SwitchEvent e = createSwitchEvent (getLabel (i), states[i]);
		fireSwitchEvent (e);
		
		return true;
	}

	public boolean switchStateKey (String _key)
	{
		int i = keys.indexOf (_key);
		return switchAndNotify (i);
	}

	protected SwitchEvent createSwitchEvent (String label, boolean isNewStateON)
	{
		int i = labels.indexOf (label);

		return (isNewStateON ? SwitchEvent.getONEvent (label, getSwitchKey (i)) : SwitchEvent.getOFFEvent (label, getSwitchKey (i)));
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
}
