package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataTreeNode;

public class JobMonitorTreeCellRenderer extends DefaultTreeCellRenderer implements JobMonitorConstants
{
	private static final String IMAGES_DIRECTORY	= "images/";
	
	private static final String NODE_ICON_GIF 		= "node_icon.gif";
	private static final String VN_ICON_GIF 			= "vn_icon.gif";
	private static final String JVM_ICON_GIF 			= "jvm_icon.gif";
	private static final String AO_ICON_GIF 			= "ao_icon.gif";
	private static final String JOB_ICON_GIF 			= "job_icon.gif";
	private static final String HOST_ICON_GIF 		= "host_icon.gif";
	
	private static final String NODE_ICON 				= IMAGES_DIRECTORY + NODE_ICON_GIF;
	private static final String VN_ICON 					= IMAGES_DIRECTORY + VN_ICON_GIF;
	private static final String JVM_ICON 					= IMAGES_DIRECTORY + JVM_ICON_GIF;
	private static final String AO_ICON 					= IMAGES_DIRECTORY + AO_ICON_GIF;
	private static final String JOB_ICON 					= IMAGES_DIRECTORY + JOB_ICON_GIF;
	private static final String HOST_ICON 				= IMAGES_DIRECTORY + HOST_ICON_GIF;
	
	private static Icon job, host, jvm, vn, node, ao;
	
	public JobMonitorTreeCellRenderer ()
	{
		if (job == null)
		{
			host = createImageIcon (HOST_ICON);
			job = createImageIcon (JOB_ICON);
			ao = createImageIcon (AO_ICON);
			jvm = createImageIcon (JVM_ICON);
			vn = createImageIcon (VN_ICON);
			node = createImageIcon (NODE_ICON);
		}
	}
	
	 public ImageIcon createImageIcon (String path)
	 {
       java.net.URL imgURL = JobMonitorPanel.class.getResource (path);
       if (imgURL != null)
           return new ImageIcon (imgURL);
       else
       {
//           System.err.println ("Couldn't find file: " + path);
           return null;
       }
   }
	 
	public Component getTreeCellRendererComponent (JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,boolean hasFocus)
	{
		super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf, row, hasFocus);
		
		Icon icon = null;

		DataTreeNode currentNode = (DataTreeNode) value;
		int key = currentNode.getKey();
		
		if (key == HOST)
			icon = host;
		else if (key == JOB)
			icon = job;
		else if (key == AO)
			icon = ao;
		else if (key ==JVM)
			icon = jvm;
		else if (key == VN)
			icon = vn;
		else if (key == NODE)
			icon = node;

		if (icon != null)
			setIcon (icon);
		
		return this;
	}
}