package org.objectweb.proactive.ic2d.gui.jobmonitor;

public interface JobMonitorConstants
{
	/* The values are meaningless, but the order is important */
	public static final int NO_KEY = -1;
	public static final int HOST   = 0;
	public static final int JVM    = 1;
	public static final int NODE   = 2;
	public static final int AO     = 3;
	/* The hole is important here because we don't have a real tree */
	public static final int JOB    = 5;
	public static final int VN     = 6;
	
	public final int[] SPECIAL_KEYS = {VN, JOB};
	public static final String[] NAMES = new String[] {"Host", "JVM", "Node", "Active Object", "Job", "Virtual Node"};
	public static final int NB_KEYS = NAMES.length;
	public static final int[] KEY2INDEX = {0, 1, 2, 3, -1, 4, 5};
}
