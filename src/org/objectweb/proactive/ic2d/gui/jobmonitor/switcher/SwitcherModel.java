package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class SwitcherModel
{
	private List labels;
	private List classes;
	private boolean [] states;
	
	private Vector listeners;
	
	public SwitcherModel (String [] _labels, Class [] _classes) throws RuntimeException
	{
		if (_labels.length != _classes.length)
			throw new RuntimeException ("Trying to create a SwitcherModel with different array sizes");
		
		labels = Arrays.asList (_labels);
		classes = Arrays.asList (_classes);
		
		states = new boolean [labels.size()];
		for (int i = 0; i < states.length; ++i)
			states [i] = true;
	}

	public Class get (String label)
	{
		int i = labels.indexOf (label);
		return getSwitchClass (i);
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

	public boolean isStateON (String label)
	{
		int i = labels.indexOf (label);
		return getStateAt (i);
	}

	public boolean isStateON (Class c)
	{
		int i = classes.indexOf (c);
		return getStateAt (i);
	}

	private boolean getStateAt (int i)
	{
		if (i < 0)
			return false;
			
		return states [i];
	}

	public boolean switchState (String label)
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

	public boolean switchState (Class _class)
	{
		int i = classes.indexOf (_class);
		return switchAndNotify (i);
	}

	protected SwitchEvent createSwitchEvent (String label, boolean isNewStateON)
	{
		int i = labels.indexOf (label);

		return (isNewStateON ? SwitchEvent.getONEvent (label, getSwitchClass (i)) : SwitchEvent.getOFFEvent (label, getSwitchClass (i)));
	}

	public String getLabel (int i)
	{
		if (i < 0 || i >= size())
			return null;
		
		return (String) labels.get (i);
	}

	public Class getSwitchClass (int i)
	{
		if (i < 0 || i >= size())
			return null;
			
		return (Class) classes.get (i);
	}
}
