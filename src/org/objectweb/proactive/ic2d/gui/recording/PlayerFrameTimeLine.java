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
package org.objectweb.proactive.ic2d.gui.recording;

import org.objectweb.proactive.ic2d.data.DataObjectController;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.data.DataObjectController;
import org.objectweb.proactive.ic2d.gui.EventListsPanel;

public class PlayerFrameTimeLine extends javax.swing.JFrame {

  private boolean record = false;
  private boolean play = false;
  private boolean pause = false;
  private javax.swing.JLabel state;
  private javax.swing.JLabel msg;

  private javax.swing.JButton monitoringControlButton;
  private javax.swing.JButton playButton;
  private javax.swing.JButton pauseButton;
  private javax.swing.JButton stopButton;
  private javax.swing.JButton recordButton;
  private javax.swing.JProgressBar eventReplayProgressBar;

  private javax.swing.JPanel tpb;
  private javax.swing.JLabel lpb;
  private ThreadPlayer threadPlayer;
  private EventListsPanel eventListsPanel;
  private DataObjectController dataObjectController;


  public PlayerFrameTimeLine(EventListsPanel eventListsPanel, DataObjectController dataObjectController) {
    super("SpyEvent Player");
    this.setSize(new java.awt.Dimension(400, 200));
    this.eventListsPanel = eventListsPanel;
    this.dataObjectController = dataObjectController;
    final javax.swing.JButton b1 = new javax.swing.JButton("On");
    final javax.swing.JButton b2 = new javax.swing.JButton("Off");
    final javax.swing.JButton b3 = new javax.swing.JButton("Reset");
    b2.setEnabled(false);

    javax.swing.JPanel c = new javax.swing.JPanel();
    c.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    c.setLayout(new java.awt.GridLayout(0, 1));
    
    javax.swing.JToolBar tools = new javax.swing.JToolBar();
    c.add(tools);

    javax.swing.JPanel t = new javax.swing.JPanel();
    t.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
    javax.swing.JLabel s = new javax.swing.JLabel();
    s.setForeground(java.awt.Color.black);
    s.setText("Status : ");
    t.add(s);
    state = new javax.swing.JLabel("Freezed");
    t.add(state);
    c.add(t);

    tpb = new javax.swing.JPanel();
    tpb.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
    javax.swing.JLabel s3 = new javax.swing.JLabel();
    s3.setForeground(java.awt.Color.black);
    s3.setText("Playing status : ");
    tpb.add(s3);
    eventReplayProgressBar = new javax.swing.JProgressBar(0, 10);
    eventReplayProgressBar.setMaximumSize(new java.awt.Dimension(70, 25));
    eventReplayProgressBar.setStringPainted(true);
    tpb.add(eventReplayProgressBar);
    c.add(tpb);

    javax.swing.JPanel t1 = new javax.swing.JPanel();
    t1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
    javax.swing.JLabel s1 = new javax.swing.JLabel();
    s1.setForeground(java.awt.Color.black);
    s1.setText("Recorded messages count : ");
    t1.add(s1);
    msg = new javax.swing.JLabel(" 0    ");
    t1.add(msg);

    b1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        b1.setEnabled(false);
        b2.setEnabled(true);
      }
    });
    b2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        b2.setEnabled(false);
        b1.setEnabled(true);
      }
    });
    b3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
      }
    });

    c.add(t1);
    setContentPane(c);
    //this.show();
	  addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(! isVisible());
      }
    });
    createListenerButtons(tools);
    this.threadPlayer = new ThreadPlayer(eventListsPanel, eventReplayProgressBar);
  }


  public void toggleMonitoring() {
    if (! dataObjectController.isMonitoring()) {
      dataObjectController.setMonitoring(true);
      monitoringControlButton.setText("Disable Monitoring");
      if (play) stop();
      state.setText("Freeze");
      recordButton.setEnabled(true);
      playButton.setEnabled(false);
    } else {
      state.setText("Freezed");
      monitoringControlButton.setText("Enable Monitoring");
      dataObjectController.setMonitoring(false);
      if (record) stop();
      pauseButton.setEnabled(false);
      recordButton.setEnabled(false);
      stopButton.setEnabled(false);
      playButton.setEnabled(true);
    }
  }


  public void play() {
    state.setText("Playing");
    pause = false;
    play = true;
    threadPlayer.play();
    playButton.setEnabled(false);
    stopButton.setEnabled(true);
    pauseButton.setEnabled(true);

  }


  public void pause() {
    pause = true;
    threadPlayer.pause();
    pauseButton.setEnabled(false);
    if (play) {
      state.setText("Playing: pause");
      playButton.setEnabled(true);
    }
    if (record) {
      recordButton.setEnabled(true);
      state.setText("Recording: pause");
    }
  }


  public void stop() {
    if (pause)
      threadPlayer.pause();
    if (record) {
      record = false;
      threadPlayer.record();
      pauseButton.setEnabled(false);
      recordButton.setEnabled(true);
      stopButton.setEnabled(false);
    }
    if (play) {
      play = false;
      threadPlayer.play();
      pauseButton.setEnabled(false);
      playButton.setEnabled(true);
      stopButton.setEnabled(false);
    }
    eventReplayProgressBar.setString("");
    eventReplayProgressBar.setValue(0);
    pause = false;
  }


  public void record() {
    state.setText("Recording");
    pause = false;
    record = true;
    threadPlayer.record();
    recordButton.setEnabled(false);
    stopButton.setEnabled(true);
    pauseButton.setEnabled(true);
  }


  public void recordEvent(ActiveObject activeObject, SpyEvent evt) {
    int n = threadPlayer.recordEvent(activeObject, evt);
    if (n > -1) {
      msg.setText(" " + n);
    }
  }
  

  private void createListenerButtons(javax.swing.JToolBar tools) {
    javax.swing.JButton button = null;
    javax.swing.Action act = null;

    act = new javax.swing.AbstractAction("play", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        play();
      }
    };
    playButton = tools.add(act);
    playButton.setToolTipText("play");

    act = new javax.swing.AbstractAction("pause", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        pause();
      }
    };
    pauseButton = tools.add(act);
    pauseButton.setToolTipText("pause");
    pauseButton.setEnabled(false);

    act = new javax.swing.AbstractAction("stop", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        stop();
      }
    };
    stopButton = tools.add(act);
    stopButton.setToolTipText("stop");
    stopButton.setEnabled(false);

    act = new javax.swing.AbstractAction("record", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        record();
      }
    };
    recordButton = tools.add(act);
    recordButton.setToolTipText("record");
    recordButton.setEnabled(false);

    act = new javax.swing.AbstractAction("clean TimeLine", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        eventListsPanel.clearAll();
      }
    };
    button = tools.add(act);
    button.setToolTipText("clean all the list");

    act = new javax.swing.AbstractAction("Disable Monitoring", null) {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        toggleMonitoring();
      }
    };
    monitoringControlButton = tools.add(act);
    monitoringControlButton.setToolTipText("Disable the monitoring of events to allow the play back");

    //state = new javax.swing.JLabel("Not Listening");
    //tools.add(state);
  }
}
