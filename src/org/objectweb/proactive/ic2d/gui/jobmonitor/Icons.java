package org.objectweb.proactive.ic2d.gui.jobmonitor;

import javax.swing.*;

public class Icons implements JobMonitorConstants {
	private static final String IMAGES_DIRECTORY	= "images/";
	
	private static final String JOB_ICON_GIF  = "job_icon.gif";
	private static final String VN_ICON_GIF   = "vn_icon.gif";
	private static final String HOST_ICON_GIF = "host_icon.gif";
	private static final String JVM_ICON_GIF  = "jvm_icon.gif";
	private static final String NODE_ICON_GIF = "node_icon.gif";
	private static final String AO_ICON_GIF   = "ao_icon.gif";
	
	private static Icon JOB_ICON = createImageIcon (JOB_ICON_GIF);
	private static Icon VN_ICON = createImageIcon (VN_ICON_GIF);
	private static Icon HOST_ICON = createImageIcon (HOST_ICON_GIF);
	private static Icon JVM_ICON = createImageIcon (JVM_ICON_GIF);
	private static Icon NODE_ICON = createImageIcon (NODE_ICON_GIF);
	private static Icon AO_ICON = createImageIcon (AO_ICON_GIF);
	
	private static Icon[] ICONS = new Icon[] {JOB_ICON, VN_ICON, HOST_ICON, JVM_ICON, NODE_ICON, AO_ICON};
	
	public static ImageIcon createImageIcon (String path)
	{
       java.net.URL imgURL = JobMonitorPanel.class.getResource (IMAGES_DIRECTORY + path);
       if (imgURL != null)
           return new ImageIcon (imgURL);
       else
       {
//           System.err.println ("Couldn't find file: " + path);
           return null;
       }
	}

	public static Icon getIconForKey(int key) {
		if (key >= 0 && key < ICONS.length)
			return ICONS[key];
		
		return null;
	}
}
