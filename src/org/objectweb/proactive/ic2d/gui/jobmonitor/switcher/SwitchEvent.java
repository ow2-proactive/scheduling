package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

public class SwitchEvent
{
	public static final int ON = 0;
	public static final int OFF = 1;
	
	private int type;
	private String label;
	private int switchedKey;
	
	private SwitchEvent (int _type, String _label, int _switchedKey)
	{
		type = _type;
		label = _label;
		switchedKey = _switchedKey;
	}
	
	public static SwitchEvent getONEvent (String label, int key)
	{
		return new SwitchEvent (ON, label, key);
	}
	
	public static SwitchEvent getOFFEvent (String label, int key)
	{
		return new SwitchEvent (OFF, label, key);
	} 
	
	public int getSwitchedKey()
	{
		return switchedKey;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String toString()
	{
		String s = "<switchevent>";
		
		s += "<label>" + label + "</label>";
		s += "<switchedKey>" + switchedKey + "</switchedKey>";
		s += "</switchEvent>";
		return s;
	}
}
