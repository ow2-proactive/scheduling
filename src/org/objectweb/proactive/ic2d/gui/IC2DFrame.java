/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.ic2d.gui;

import org.globus.ogce.gui.gram.gui.SubmitJobPanel;
import org.objectweb.fractal.gui.FractalGUI;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventListener;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.IC2D;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.data.IC2DObject;
import org.objectweb.proactive.ic2d.event.IC2DObjectListener;
import org.objectweb.proactive.ic2d.gui.data.IC2DPanel;
import org.objectweb.proactive.ic2d.gui.jobmonitor.JobMonitorFrame;
import org.objectweb.proactive.ic2d.gui.process.ProcessControlFrame;
import org.objectweb.proactive.ic2d.gui.util.DialogUtils;
import org.objectweb.proactive.ic2d.gui.util.HostDialog;
import org.objectweb.proactive.ic2d.gui.util.MessagePanel;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;
import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;
import org.objectweb.proactive.ic2d.util.MonitorThread;


public class IC2DFrame extends javax.swing.JFrame implements IC2DObjectListener,
    RuntimeRegistrationEventListener {
    private static final int DEFAULT_WIDTH = 850;
    private static final int DEFAULT_HEIGHT = 600;
    private int options;
    private IC2DPanel ic2dPanel;
    private IC2DObject ic2dObject;
    private IC2DMessageLogger logger;
    private IC2DGUIController controller;
    private ActiveObjectFilter activeObjectFilter;
    private ActiveObjectCommunicationRecorder communicationRecorder;
    private EventListsPanel eventListsPanel;
    private javax.swing.JFrame eventListsFrame;
    private javax.swing.JFrame processesFrame;
    private javax.swing.JFrame globusProcessFrame;
    private javax.swing.JFrame fileChooserFrame;
    private ExternalProcess externalProcess;
    private ProActiveRuntimeImpl proActiveRuntimeImpl;
    private static final String HOME = System.getProperty("user.home");
    private int depthMonitor = 10;
    private javax.swing.JFrame jobMonitorFrame;

    //
    // -- CONTRUCTORS -----------------------------------------------
    //
    public IC2DFrame(IC2DObject ic2dObject) {
        this(ic2dObject, IC2D.NOTHING);
    }

    public IC2DFrame(IC2DObject object, int options) {
        super("IC2D");
        this.options = options;
        this.setSize(new java.awt.Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        this.ic2dObject = object;
        setJMenuBar(createMenuBar());

        activeObjectFilter = new ActiveObjectFilter();
        controller = new MyController();
        communicationRecorder = new ActiveObjectCommunicationRecorder();
        MessagePanel messagePanel = new MessagePanel("Messages");
        logger = messagePanel.getMessageLogger();
        ic2dObject.registerLogger(logger);
        ic2dObject.registerListener(this);
        eventListsPanel = new EventListsPanel(ic2dObject, controller);
        ic2dPanel = new IC2DPanel(this, ic2dObject, controller,
                communicationRecorder, activeObjectFilter, eventListsPanel);

        java.awt.Container c = getContentPane();
        c.setLayout(new java.awt.BorderLayout());

        //Create the split pane
        javax.swing.JSplitPane splitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,
                false, ic2dPanel, messagePanel);
        splitPanel.setDividerLocation(DEFAULT_HEIGHT - 200);
        splitPanel.setOneTouchExpandable(true);
        c.add(splitPanel, java.awt.BorderLayout.CENTER);

        // Listeners
        proActiveRuntimeImpl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();

        proActiveRuntimeImpl.addRuntimeRegistrationEventListener(this);
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    ic2dObject.getWorldObject().destroyObject();
                    System.exit(0);
                }
            });

        jobMonitorFrame = createJobMonitorFrame();
        processesFrame = createProcessesFrame();

        setVisible(true);

        eventListsFrame = createEventListFrame(eventListsPanel);

        //fileChooserFrame = createFileChooserFrame();
        logger.log("IC2D ready !");
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements NodeObjectListener -----------------------------------------------
    //
    public void activeObjectAdded(ActiveObject activeObject) {
    }

    public void activeObjectRemoved(ActiveObject activeObject) {
        communicationRecorder.removeActiveObject(activeObject);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.event.RuntimeRegistrationEventListener#runtimeRegistered(org.objectweb.proactive.core.event.RuntimeRegistrationEvent)
     */
    public void runtimeRegistered(RuntimeRegistrationEvent event) {
        ProActiveRuntime proActiveRuntimeRegistered;

        String protocol;
        String host;
        int port = 0;
        String nodeName = null;
        String url = null;
        protocol = event.getProtocol();
        proActiveRuntimeRegistered = event.getRegisteredRuntime();
        host = UrlBuilder.getHostNameorIP(proActiveRuntimeRegistered.getVMInformation()
                                                                    .getInetAddress());

        port = UrlBuilder.getPortFromUrl(proActiveRuntimeRegistered.getURL());

        nodeName = "IC2DNode-" +
            Integer.toString(new java.util.Random(System.currentTimeMillis()).nextInt());
        if (port != 0) {
            url = UrlBuilder.buildUrl(host, nodeName, protocol, port);
        } else {
            url = UrlBuilder.buildUrl(host, nodeName, protocol);
        }
        try {
            proActiveRuntimeRegistered.createLocalNode(url, false, null,
                this.getName(), ProActive.getJobId());
        } catch (NodeException e1) {
            logger.log(e1, false);
        }
        new MonitorThread(protocol, host, "1", ic2dObject.getWorldObject(),
            logger).start();
    }

    //
    // -- implements CommunicationEventListener -----------------------------------------------
    //
    public void objectWaitingForRequest(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.objectWaitingForRequest(object, spyEvent);
        eventListsPanel.objectWaitingForRequest(object, spyEvent);
    }

    public void objectWaitingByNecessity(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.objectWaitingByNecessity(object, spyEvent);
        eventListsPanel.objectWaitingByNecessity(object, spyEvent);
    }

    public void requestMessageSent(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.requestMessageSent(object, spyEvent);
        eventListsPanel.requestMessageSent(object, spyEvent);
    }

    public void replyMessageSent(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.replyMessageSent(object, spyEvent);
        eventListsPanel.replyMessageSent(object, spyEvent);
    }

    public void requestMessageReceived(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.requestMessageReceived(object, spyEvent);
        eventListsPanel.requestMessageReceived(object, spyEvent);
    }

    public void replyMessageReceived(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.replyMessageReceived(object, spyEvent);
        eventListsPanel.replyMessageReceived(object, spyEvent);
    }

    public void voidRequestServed(ActiveObject object, SpyEvent spyEvent) {
        ic2dPanel.voidRequestServed(object, spyEvent);
        eventListsPanel.voidRequestServed(object, spyEvent);
    }

    public void allEventsProcessed() {
        ic2dPanel.allEventsProcessed();
        eventListsPanel.allEventsProcessed();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private javax.swing.JFrame createEventListFrame(javax.swing.JPanel panel) {
        // Create the timeLine panel
        final javax.swing.JFrame frame = new javax.swing.JFrame("Events lists");
        frame.setLocation(new java.awt.Point(0, DEFAULT_HEIGHT));
        frame.setSize(new java.awt.Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT / 2));
        java.awt.Container c = frame.getContentPane();
        c.setLayout(new java.awt.GridLayout(1, 1));
        javax.swing.JScrollPane scrollingEventListsPanel = new javax.swing.JScrollPane(panel);
        scrollingEventListsPanel.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        c.add(scrollingEventListsPanel);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    frame.setVisible(!frame.isVisible());
                }
            });
        frame.setVisible(true);
        return frame;
    }

    private javax.swing.JFrame createProcessesFrame() {
        final javax.swing.JFrame frame = new ProcessControlFrame();
        frame.setLocation(new java.awt.Point(DEFAULT_WIDTH, 0));
        // Listeners
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    frame.setVisible(!frame.isVisible());
                }
            });
        return frame;
    }

    private JobMonitorFrame createJobMonitorFrame() {
        final JobMonitorFrame frame = new JobMonitorFrame(controller);
        frame.setLocation(new java.awt.Point(DEFAULT_WIDTH, 0));
        // Listeners
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    frame.setVisible(!frame.isVisible());
                }
            });
        return frame;
    }

    //  private javax.swing.JFrame createFileChooserFrame() {
    //    final javax.swing.JFrame frame = new DeployFileChooserFrame();
    //    //frame.setLocation(new java.awt.Point(DEFAULT_WIDTH, 0));
    //    // Listeners
    //    frame.addWindowListener(new java.awt.event.WindowAdapter() {
    //      public void windowClosing(java.awt.event.WindowEvent e) {
    //        frame.setVisible(! frame.isVisible());
    //      }
    //    });
    //    return frame;
    //  }
    //  private javax.swing.JFrame createGlobusProcessFrame() {
    //    final javax.swing.JFrame frame = new GlobusProcessControlFrame(externalProcess);
    //    //frame.setLocation(new java.awt.Point(DEFAULT_WIDTH, 0));
    //    // Listeners
    //    frame.addWindowListener(new java.awt.event.WindowAdapter() {
    //      public void windowClosing(java.awt.event.WindowEvent e) {
    //        frame.setVisible(! frame.isVisible());
    //      }
    //    });
    //    return frame;
    // }
    private javax.swing.JMenuBar createMenuBar() {
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();

        //
        // monitoring menu
        //
        javax.swing.JMenu monitoringMenu = new javax.swing.JMenu("Monitoring");
        // Add new RMI host
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Monitor a new RMI host");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        DialogUtils.openNewRMIHostDialog(IC2DFrame.this,
                            ic2dObject.getWorldObject(), logger);
                    }
                });
            monitoringMenu.add(b);
        }
        // Add new RMI Node
        //        {
        //            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
        //                    "Monitor a new RMI Node");
        //            b.addActionListener(new java.awt.event.ActionListener() {
        //                    public void actionPerformed(java.awt.event.ActionEvent e) {
        //                        DialogUtils.openNewNodeDialog(IC2DFrame.this,
        //                            ic2dObject.getWorldObject(), logger);
        //                    }
        //                });
        //            monitoringMenu.add(b);
        //        }
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Monitor a new Ibis host");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        DialogUtils.openNewIbisHostDialog(IC2DFrame.this,
                            ic2dObject.getWorldObject(), logger);
                    }
                });
            monitoringMenu.add(b);
        }

        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Monitor all JINI Hosts");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        DialogUtils.openNewJINIHostsDialog(IC2DFrame.this,
                            ic2dObject.getWorldObject(), controller);
                    }
                });
            monitoringMenu.add(b);
        }

        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Monitor a new JINI Host");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        DialogUtils.openNewJINIHostDialog(IC2DFrame.this,
                            ic2dObject.getWorldObject(), logger);
                    }
                });
            monitoringMenu.add(b);
        }
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Monitor a new HTTP Host");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        DialogUtils.openNewHTTPHostDialog(IC2DFrame.this,
                            ic2dObject.getWorldObject(), logger);
                    }
                });
            monitoringMenu.add(b);
        }
        monitoringMenu.addSeparator();
        // Add new GLOBUS host 
        //deprecated
        //    {
        //    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor new GLOBUS host");
        //    b.addActionListener(new java.awt.event.ActionListener() {
        //        public void actionPerformed(java.awt.event.ActionEvent e) {
        //          DialogUtils.openNewGlobusHostDialog((java.awt.Component) IC2DFrame.this, ic2dObject.getWorldObject(), logger);
        //        }
        //      });
        //    	//b.setEnabled((options & IC2D.GLOBUS) != 0);
        //    	monitoringMenu.add(b);
        //    }
        //
        //    monitoringMenu.addSeparator();
        // Edit the filter list
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Show filtered classes");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        DialogUtils.openFilteredClassesDialog(IC2DFrame.this,
                            ic2dPanel, activeObjectFilter);
                    }
                });
            b.setToolTipText("Filter active objects");
            monitoringMenu.add(b);
        }
        monitoringMenu.addSeparator();
        // Display the legend 
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem("Legend");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        doLegend();
                    }
                });
            b.setToolTipText("Display the legend");
            monitoringMenu.add(b);
        }
        monitoringMenu.addSeparator();
        // exit 
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem("Quit");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        ic2dObject.getWorldObject().destroyObject();
                        System.exit(0);
                    }
                });
            monitoringMenu.add(b);
        }
        menuBar.add(monitoringMenu);

        //	//Deploy
        //	   javax.swing.JMenu DeployMenu = new javax.swing.JMenu("Deploy");
        //		   {
        //		   javax.swing.JMenuItem b = new javax.swing.JMenuItem("Deploy with descriptors");
        //		   b.addActionListener(new java.awt.event.ActionListener() {
        //			   public void actionPerformed(java.awt.event.ActionEvent e) {
        //			   	JFrame frame = createFileChooserFrame();
        //			   	frame.setVisible(true);
        //			 	}
        //			   });
        //			 
        //		   DeployMenu.add(b);
        //		   }   
        //	menuBar.add(DeployMenu);
        //ebe 20/08/2004  control menu
        javax.swing.JMenu controlMenu = new javax.swing.JMenu("Control");
        // Add depth control menu
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Set depth Control...");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        HostDialog.openSetDepthControlDialog(IC2DFrame.this);
                    }
                });
            controlMenu.add(b);
        }
        menuBar.add(controlMenu);
        //
        // look and feel 
        //
        {
            javax.swing.JMenu lookMenu = new javax.swing.JMenu("Look & feel");
            javax.swing.UIManager.LookAndFeelInfo[] infos = javax.swing.UIManager.getInstalledLookAndFeels();
            for (int i = 0; i < infos.length; i++) {
                javax.swing.AbstractAction a = new javax.swing.AbstractAction(infos[i].getName(),
                        null) {
                        public void actionPerformed(
                            java.awt.event.ActionEvent e) {
                            try {
                                String classname = (String) getValue("class");

                                //javax.swing.JFrame frame = (javax.swing.JFrame)getValue("frame");
                                javax.swing.UIManager.setLookAndFeel(classname);
                                javax.swing.SwingUtilities.updateComponentTreeUI(IC2DFrame.this);
                                javax.swing.SwingUtilities.updateComponentTreeUI(eventListsFrame);
                                javax.swing.SwingUtilities.updateComponentTreeUI(processesFrame);
                            } catch (Exception ex) {
                            }
                        }
                    };
                a.putValue("frame", this);
                a.putValue("class", infos[i].getClassName());
                lookMenu.add(a);
            }
            menuBar.add(lookMenu);
        }

        //
        // Window
        //
        javax.swing.JMenu windowMenu = new javax.swing.JMenu("Window");
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Hide/Show EventsList windows");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (eventListsFrame.isVisible()) {
                            eventListsFrame.setVisible(false);
                        } else {
                            eventListsFrame.setVisible(true);
                        }
                    }
                });
            windowMenu.add(b);
        }

        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Hide/Show Processes windows");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (processesFrame.isVisible()) {
                            processesFrame.setVisible(false);
                        } else {
                            processesFrame.setVisible(true);
                        }
                    }
                });
            windowMenu.add(b);
        }

        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Hide/Show Job Monitor windows");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (jobMonitorFrame.isVisible()) {
                            jobMonitorFrame.setVisible(false);
                        } else {
                            jobMonitorFrame.setVisible(true);
                        }
                    }
                });
            windowMenu.add(b);
        }

        menuBar.add(windowMenu);

        //
        // Globus
        //
        javax.swing.JMenu globusMenu = new javax.swing.JMenu("Globus");
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Start a new Node with Globus");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        SubmitJobPanel.main(new String[0]);
                        //		if (fileChooserFrame.isVisible()) {
                        //		    fileChooserFrame.hide();
                        //		} else {
                        //		    fileChooserFrame.show();
                        //		}
                    }
                });
            globusMenu.add(b);
        }

        //    {
        //	javax.swing.JMenuItem b = new javax.swing.JMenuItem("Initialize proxy");
        //	b.addActionListener(new java.awt.event.ActionListener() {
        //	    public void actionPerformed(java.awt.event.ActionEvent e) {
        //	    	GridProxyInit.main(new String[0]);
        ////		if (((FileChooser)fileChooserFrame).ready()){
        ////		    ((FileChooser)fileChooserFrame).changeVisibilityGlobusProcessFrame();
        ////		}
        ////		else {
        ////		    logger.log("Please select the deployment file with the file chooser in the window menu!");
        ////		}
        //		
        //	    }
        //	});
        //	globusMenu.add(b);
        //    }
        menuBar.add(globusMenu);

        //*******************************************************
        // Components gui
        //*******************************************************
        javax.swing.JMenu componentsMenu = new javax.swing.JMenu("Components");
        {
            javax.swing.JMenuItem b = new javax.swing.JMenuItem(
                    "Start the components GUI");
            b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        try {
                            System.setProperty("fractal.provider",
                                "org.objectweb.fractal.julia.Julia");
                            System.setProperty("julia.loader",
                                "org.objectweb.fractal.julia.loader.DynamicLoader");
                            System.setProperty("julia.config",
                                "org/objectweb/fractal/gui/julia.cfg");
                            FractalGUI.main(new String[] {
                                    "org.objectweb.proactive.ic2d.gui.components.ProActiveGUI"
                                });
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            componentsMenu.add(b);
        }

        menuBar.add(componentsMenu);
        //*******************************************************
        return menuBar;
    }

    private void doLegend() {
        Legend legend = Legend.uniqueInstance();
        legend.setVisible(!legend.isVisible());
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class MyController implements IC2DGUIController {
        private boolean isLayoutAutomatic = true;

        public MyController() {
        }

        public boolean isLayoutAutomatic() {
            return isLayoutAutomatic;
        }

        public void setAutomaticLayout(boolean b) {
            isLayoutAutomatic = b;
        }

        public void warn(String message) {
            logger.warn(message);
        }

        public void log(String message) {
            logger.log(message);
        }

        public void log(String message, Throwable e) {
            logger.log(message, e);
        }

        public void log(Throwable e) {
            logger.log(e);
        }

        public void log(String message, Throwable e, boolean dialog) {
            logger.log(message, e, dialog);
        }

        public void log(Throwable e, boolean dialog) {
            logger.log(e, dialog);
        }
    }
}
