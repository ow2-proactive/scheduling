package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;

import org.objectweb.proactive.ic2d.gui.IC2DGUIController;

public class JobMonitorFrame extends JFrame
{
	private static final int DEFAULT_WIDTH = 800;
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

	private javax.swing.JMenuBar createMenuBar()
	{
	    javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
	    	    
	    javax.swing.JMenu monitoringMenu = new javax.swing.JMenu("Monitoring");

	    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor a new RMI host");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed(java.awt.event.ActionEvent e)
	        {
				String initialHostValue = "localhost";
			    try {
			      initialHostValue = java.net.InetAddress.getLocalHost().getCanonicalHostName();
			    } catch (java.net.UnknownHostException exc) {
			    }
			    Object result = javax.swing.JOptionPane.showInputDialog(panel, // Component parentComponent,
			        "Please enter the name or the IP of the host to monitor :", // Object message,
			        "Adding a host to monitor", // String title,
			        javax.swing.JOptionPane.PLAIN_MESSAGE, // int messageType,
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
	    javax.swing.JMenu controlMenu = new javax.swing.JMenu ("Control");

	    b = new javax.swing.JMenuItem ("Set Time-To-Refresh");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed (java.awt.event.ActionEvent e)
	        {
	        	String initialValue = "" + panel.getTtr();
			    Object result = javax.swing.JOptionPane.showInputDialog(panel, // Component parentComponent,
			        "Please enter the time to refresh, in seconds :", // Object message,
			        "Setting the time to refresh", // String title,
			        javax.swing.JOptionPane.PLAIN_MESSAGE, // int messageType,
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
	    
	    b = new javax.swing.JMenuItem ("Set Depth Control");
	    b.addActionListener(new java.awt.event.ActionListener()
	    {
	        public void actionPerformed(java.awt.event.ActionEvent e)
	        {
//	        	System.out.println ("Set depth control asked");
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