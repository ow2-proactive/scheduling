package org.objectweb.proactive.ic2d.gui.jobmonitor;

public interface JobMonitorConstants
{
	/* The values are meaningless, but the order is important */
	public static final int NO_KEY = -1;
	public static final int JOB    = 0;
	public static final int VN     = 1;
	public static final int HOST   = 2;
	public static final int JVM    = 3;
	public static final int NODE   = 4;
	public static final int AO     = 5;
	
	public static final String[] NAMES = new String[] {"Job", "VN", "Host", "JVM", "Node", "Active Object"};
	public static final int NB_KEYS = NAMES.length;
	public static final int FIRST_KEY = 0;
	public static final int LAST_KEY = NB_KEYS - 1;
}
