package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.*;

import org.objectweb.proactive.ic2d.gui.IC2DGUIController;

public class JobMonitorFrame extends JFrame
{
	private static final int DEFAULT_WIDTH = 830;
	private static final int DEFAULT_HEIGHT = 600;

	private JobMonitorPanel panel;

	public JobMonitorFrame (IC2DGUIController controller)
	{
		super ("Job Monitoring");
		setSize (new Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT));
		
		panel = new JobMonitorPanel (controller);
		
		Container c = getContentPane();
		c.setLayout (new GridLayout (1, 1));
		c.add (panel);
		
		 setJMenuBar (createMenuBar());
	}

	private JMenuBar createMenuBar()
	{
	    JMenuBar menuBar = new JMenuBar();
	    	    
	    JMenu monitoringMenu = new JMenu("Monitoring");

	    JMenuItem b = new JMenuItem("Monitor a new RMI host...");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed(java.awt.event.ActionEvent e)
	        {
				String initialHostValue = "localhost";
			    try {
			      initialHostValue = java.net.InetAddress.getLocalHost().getCanonicalHostName();
			    } catch (java.net.UnknownHostException exc) {
			    }
			    Object result = JOptionPane.showInputDialog(panel, // Component parentComponent,
			        "Please enter the name or the IP of the host to monitor :", // Object message,
			        "Adding a host to monitor", // String title,
			        JOptionPane.PLAIN_MESSAGE, // int messageType,
			        null, // Icon icon,
			        null, // Object[] selectionValues,
			        initialHostValue // Object initialSelectionValue)
			      );
			    if (result == null || (!(result instanceof String)))
			      return;
			    String host = (String) result;
			    
			    panel.addMonitoredHost (host);
			    panel.updateHosts ();
	        }
	    });
	    monitoringMenu.add(b);
	    
	    menuBar.add (monitoringMenu);

	    // control menu
	    JMenu controlMenu = new JMenu ("Control");

	    b = new JMenuItem ("Set Time-To-Refresh...");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed (java.awt.event.ActionEvent e)
	        {
	        	String initialValue = "" + panel.getTtr();
			    Object result = JOptionPane.showInputDialog(panel, // Component parentComponent,
			        "Please enter the time to refresh, in seconds :", // Object message,
			        "Setting the time to refresh", // String title,
			        JOptionPane.PLAIN_MESSAGE, // int messageType,
			        null, // Icon icon,
			        null, // Object[] selectionValues,
			        initialValue // Object initialSelectionValue)
			      );
			    if (result == null || (!(result instanceof String)))
			      return;
			    
			    try
				{
					int ttr = Integer.parseInt ((String) result);
					panel.setTtr (ttr);
				}
				catch (NumberFormatException exc)
				{
//					System.out.println ("The ttr number is invalid !");
					return;
				}
	        }
	    });
	    controlMenu.add(b);
	    
	    b = new JMenuItem ("Set Depth Control...");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed(java.awt.event.ActionEvent e)
	        {
	        	NodeExploration explorator = panel.getNodeExploration();
	        	String initialValue = "" + explorator.getMaxDepth();
			    Object result = JOptionPane.showInputDialog(panel, // Component parentComponent,
			        "Please enter the maximum exploration depth :", // Object message,
			        "Setting the maximum depth", // String title,
			        JOptionPane.PLAIN_MESSAGE, // int messageType,
			        null, // Icon icon,
			        null, // Object[] selectionValues,
			        initialValue // Object initialSelectionValue)
			      );
			    if (result == null || (!(result instanceof String)))
			      return;
			    
			    try
				{
					int depth = Integer.parseInt ((String) result);
					explorator.setMaxDepth(depth);
				}
				catch (NumberFormatException exc)
				{
//					System.out.println ("The depth number is invalid !");
					return;
				}

	        }
	    });
	    controlMenu.add(b);
	    
	    b = new JMenuItem ("Clear deleted elements");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed(java.awt.event.ActionEvent e)
	        {
	        	panel.clearDeleted();

	        }
	    });
	    controlMenu.add(b);
	    
	    menuBar.add (controlMenu);
	    
	    return menuBar;
	}
	
	public void hide()
	{
		super.hide();
		
		panel.hideOwnedFrames();
	}
	
	public void show()
	{
		super.show();
	
		panel.showOwnedFrames();
	}
}