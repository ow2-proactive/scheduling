package org.objectweb.proactive.ic2d.gui.jobmonitor.switcher;

public class SwitchEvent
{
	public static final int ON = 0;
	public static final int OFF = 1;
	
	private int type;
	private String label;
	private Class switchedClass;
	
	private SwitchEvent (int _type, String _label, Class _switchedClass)
	{
		type = _type;
		label = _label;
		switchedClass = _switchedClass;
	}
	
	public static SwitchEvent getONEvent (String label, Class _class)
	{
		return new SwitchEvent (ON, label, _class);
	}
	
	public static SwitchEvent getOFFEvent (String label, Class _class)
	{
		return new SwitchEvent (OFF, label, _class);
	} 
	
	public Class getSwitchedClass()
	{
		return switchedClass;
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
		s += "<switchedClass>" + switchedClass.getName() + "</switchedClass>";
		s += "</switchEvent>";
		return s;
	}
}
