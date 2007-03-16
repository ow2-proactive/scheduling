/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui.jobmonitor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.rmi.registry.Registry;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;


public class JobMonitorFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 830;
    private static final int DEFAULT_HEIGHT = 630;
    private JobMonitorPanel panel;

    public JobMonitorFrame(IC2DGUIController controller) {
        super("Job Monitoring");
        setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        panel = new JobMonitorPanel(controller);

        Container c = getContentPane();
        c.setLayout(new GridLayout(1, 1));
        c.add(panel);

        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu monitoringMenu = new JMenu("Monitoring");

        JMenuItem b = new JMenuItem("Monitor a new RMI host...");
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String initialHostValue = "localhost";
                    try {
                        initialHostValue = UrlBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()) +
                            ":" + System.getProperty("proactive.rmi.port");
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
                    if ((result == null) || (!(result instanceof String))) {
                        return;
                    }
                    String host = (String) result;
                    int port;
                    int portIndex = host.indexOf(':');
                    if (portIndex < 0) {
                        port = Registry.REGISTRY_PORT;
                    } else {
                        port = Integer.parseInt(host.substring(portIndex + 1));
                        host = host.substring(0, portIndex);
                    }
                    panel.addMonitoredHost(host, port);
                    panel.updateHosts();
                }
            });
        monitoringMenu.add(b);

        b = new JMenuItem("Show monitored objects...");
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    MonitoredObjectsList dialog = new MonitoredObjectsList(JobMonitorFrame.this,
                            getPanel());
                    dialog.setVisible(true);
                }
            });
        monitoringMenu.add(b);

        menuBar.add(monitoringMenu);

        // control menu
        JMenu controlMenu = new JMenu("Control");

        b = new JMenuItem("Set Time-To-Refresh...");
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String initialValue = "" + panel.getTtr();
                    Object result = JOptionPane.showInputDialog(panel, // Component parentComponent,
                            "Please enter the time to refresh, in seconds :", // Object message,
                            "Setting the time to refresh", // String title,
                            JOptionPane.PLAIN_MESSAGE, // int messageType,
                            null, // Icon icon,
                            null, // Object[] selectionValues,
                            initialValue // Object initialSelectionValue)
                        );
                    if ((result == null) || (!(result instanceof String))) {
                        return;
                    }

                    try {
                        int ttr = Integer.parseInt((String) result);
                        panel.setTtr(ttr);
                    } catch (NumberFormatException exc) {
                        //					System.out.println ("The ttr number is invalid !");
                        return;
                    }
                }
            });
        controlMenu.add(b);

        b = new JMenuItem("Set Depth Control...");
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
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
                    if ((result == null) || (!(result instanceof String))) {
                        return;
                    }

                    try {
                        int depth = Integer.parseInt((String) result);
                        explorator.setMaxDepth(depth);
                    } catch (NumberFormatException exc) {
                        //					System.out.println ("The depth number is invalid !");
                        return;
                    }
                }
            });
        controlMenu.add(b);

        b = new JMenuItem("Clear deleted elements");
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    panel.clearDeleted();
                }
            });
        controlMenu.add(b);

        menuBar.add(controlMenu);

        /////////////////////
        // added in 08/2005

        //        javax.swing.JMenu launcherMenu = new javax.swing.JMenu("Launcher");
        //		b = new javax.swing.JMenuItem("Launch a new application");
        //		b.addActionListener(new java.awt.event.ActionListener() {
        //			public void actionPerformed(java.awt.event.ActionEvent e) {
        //				// DialogUtils.launchApplicationDialog() ;
        //				// String host = "localhost";
        //				int port = Registry.REGISTRY_PORT;
        //				panel.addMonitoredHost(host, port);
        //				panel.updateHosts();
        //
        //			}
        //
        //		});
        //		launcherMenu.add(b);
        //
        //		menuBar.add(launcherMenu);
        return menuBar;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            panel.showOwnedFrames();
        } else {
            panel.hideOwnedFrames();
        }
    }

    public JobMonitorPanel getPanel() {
        return panel;
    }
}
