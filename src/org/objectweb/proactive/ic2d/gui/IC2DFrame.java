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

import org.objectweb.proactive.ic2d.IC2D;
import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;
import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;
import org.objectweb.proactive.ic2d.gui.data.IC2DPanel;
import org.objectweb.proactive.ic2d.data.IC2DObject;
import org.objectweb.proactive.ic2d.data.HostObject;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.event.IC2DObjectListener;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.gui.process.ProcessControlFrame;
import org.objectweb.proactive.ic2d.gui.dialog.FilterDialog;
import org.objectweb.proactive.ic2d.gui.util.MessagePanel;
import org.objectweb.proactive.ic2d.gui.util.DialogUtils;

public class IC2DFrame extends javax.swing.JFrame implements IC2DObjectListener {

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
    ic2dPanel = new IC2DPanel(this, ic2dObject, controller, communicationRecorder, activeObjectFilter, eventListsPanel);
    
    java.awt.Container c = getContentPane();
    c.setLayout(new java.awt.BorderLayout());

    //Create the split pane
    javax.swing.JSplitPane splitPanel = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT, false, ic2dPanel, messagePanel);
    splitPanel.setDividerLocation(DEFAULT_HEIGHT-200);
    splitPanel.setOneTouchExpandable(true);
    c.add(splitPanel, java.awt.BorderLayout.CENTER);

    // Listeners
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        System.exit(0);
      }
    });

    setVisible(true);
    eventListsFrame = createEventListFrame(eventListsPanel);
    processesFrame = createProcessesFrame();
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
    eventListsPanel.removeActiveObject(activeObject);
    communicationRecorder.removeActiveObject(activeObject);
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
    final javax.swing.JFrame frame =  new javax.swing.JFrame("Events lists");
    frame.setLocation(new java.awt.Point(0, DEFAULT_HEIGHT));
    frame.setSize(new java.awt.Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT/2));
    java.awt.Container c = frame.getContentPane();
    c.setLayout(new java.awt.GridLayout(1,1));
    javax.swing.JScrollPane scrollingEventListsPanel = new javax.swing.JScrollPane(panel);
    scrollingEventListsPanel.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    c.add(scrollingEventListsPanel);
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        frame.setVisible(! frame.isVisible());
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
        frame.setVisible(! frame.isVisible());
      }
    });
    return frame;
  }
  
  
  
  private javax.swing.JMenuBar createMenuBar() {
    javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
    
    //
    // monitoring menu
    //
    javax.swing.JMenu monitoringMenu = new javax.swing.JMenu("Monitoring");
    // Add new RMI host
    {
    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor a new RMI host");
    b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          DialogUtils.openNewRMIHostDialog(IC2DFrame.this, ic2dObject.getWorldObject(), logger);
        }
      });
    monitoringMenu.add(b);
    }
    
    // Add new RMI Node
    {
    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor a new RMI Node");
    b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
	  DialogUtils.openNewNodeDialog(IC2DFrame.this, ic2dObject.getWorldObject(), logger);
	}
      });
    monitoringMenu.add(b);
    }
    
 
    {
      javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor all JINI Hosts");
      b.addActionListener(new java.awt.event.ActionListener() {
	  public void actionPerformed(java.awt.event.ActionEvent e) {
	    ic2dObject.getWorldObject().addHosts();
	  }
	});
      monitoringMenu.add(b);
    }

    {
      javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor a new JINI Hosts");
      b.addActionListener(new java.awt.event.ActionListener() {
	  public void actionPerformed(java.awt.event.ActionEvent e) {
	    DialogUtils.openNewJINIHostDialog(IC2DFrame.this, ic2dObject.getWorldObject(), logger);
	  }
	});
      monitoringMenu.add(b);
    }

    
    monitoringMenu.addSeparator();
    
    // Add new GLOBUS host 
    /*
    {
    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Monitor new GLOBUS host");
    b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          DialogUtils.openNewGlobusHostDialog(IC2DFrame.this, ic2dObject.getWorldObject(), logger);
        }
      });
    b.setEnabled((options & IC2D.GLOBUS) != 0);
    monitoringMenu.add(b);
    }
    monitoringMenu.addSeparator();
    */
    
    // Edit the filter list
    {
    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Filter Objects");
    b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          doEditFilter();
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
          System.exit(0);
        }
      });
    monitoringMenu.add(b);
    }
    menuBar.add(monitoringMenu);
    
    
    //
    // look and feel 
    //
    {
    javax.swing.JMenu lookMenu = new javax.swing.JMenu("Look & feel");
    javax.swing.UIManager.LookAndFeelInfo[] infos = javax.swing.UIManager.getInstalledLookAndFeels();
    for (int i = 0; i < infos.length; i++) {
      javax.swing.AbstractAction a = new javax.swing.AbstractAction(infos[i].getName(), null) {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          try {
            String classname = (String)getValue("class");
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
    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Hide/Show EventsList windows");
    b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (eventListsFrame.isVisible()) {
            eventListsFrame.hide();
          } else {
            eventListsFrame.show();
          }
        }
      });
    windowMenu.add(b);
    }
    {
    javax.swing.JMenuItem b = new javax.swing.JMenuItem("Hide/Show Processes windows");
    b.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (processesFrame.isVisible()) {
            processesFrame.hide();
          } else {
            processesFrame.show();
          }
        }
      });
    windowMenu.add(b);
    }
    menuBar.add(windowMenu);

    return menuBar;
  }


  private void doEditFilter() {
    FilterDialog diag = new FilterDialog(this, activeObjectFilter);
    diag.setVisible(true);
    if (diag.success) {
      boolean b = diag.updateFilter(activeObjectFilter);
      if (b)
        ic2dPanel.repaint();
    }
  }

  private void doLegend() {
    Legend legend = Legend.uniqueInstance();
    legend.setVisible(! legend.isVisible());
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

  }
}
