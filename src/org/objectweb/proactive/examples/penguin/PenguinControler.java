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
package org.objectweb.proactive.examples.penguin;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

import javax.swing.*;
import java.awt.*;

public class PenguinControler implements org.objectweb.proactive.Active {

  //The image panel
  private Penguin penguin;
  private PenguinControlerFrame controlerFrame;


  public PenguinControler() {
  }


  public PenguinControler(Penguin penguin) {
    this.penguin = penguin;

  }


  public void startPenguin() {
    this.penguin.startItinerary();
  }


  public void stopPenguin() {
    this.penguin.stopItinerary();
  }


  public void continuePenguin() {
    this.penguin.continueItinerary();
  }


  public void askLocation() {
    Message m = this.penguin.getPosition();
    controlerFrame.displayAgentMessage(m.getMessage());
  }


  public void askToCallOther() {
    this.penguin.callOther();
  }


  public void receiveMessage(String s) {
    this.controlerFrame.displayMessage(s);
  }


  public void live(Body b) {
    this.controlerFrame = new PenguinControlerFrame((PenguinControler) ProActive.getStubOnThis());
    penguin.setControler((PenguinControler) ProActive.getStubOnThis());
    b.fifoPolicy();
  }


  protected class PenguinControlerFrame extends javax.swing.JFrame implements java.awt.event.ActionListener {
    private PenguinControler controler;
    private javax.swing.JPanel buttonPanel;
    private JPanel textPanel;
    private JPanel agentPanel;
    protected javax.swing.JTextArea textArea;
    protected javax.swing.JTextArea agentTextArea;
    protected javax.swing.JButton bStop;
    protected javax.swing.JButton bStart;
    protected javax.swing.JButton bResume;
    protected javax.swing.JButton bCall;
    protected javax.swing.JButton bCallOther;


    protected PenguinControlerFrame(PenguinControler c) {
      this.controler = c;

      buttonPanel = new JPanel(new java.awt.GridLayout(0, 1));

      bStart = new javax.swing.JButton("Start");
      bStart.addActionListener(this);
      buttonPanel.add(bStart);

      bResume = new javax.swing.JButton("Resume");
      bResume.addActionListener(this);
      buttonPanel.add(bResume);

      bStop = new javax.swing.JButton("Stop");
      bStop.addActionListener(this);
      buttonPanel.add(bStop);


      javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(1, 1));
      textArea = new JTextArea(10, 0);
      panel.add(new JScrollPane(textArea));
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(panel, BorderLayout.NORTH);


      agentPanel = new JPanel(new java.awt.BorderLayout());
      agentTextArea = new JTextArea(5, 30);
      bCall = new javax.swing.JButton("Call Agent");
      bCall.addActionListener(this);
      bCallOther = new javax.swing.JButton("Call Other Agent");
      bCallOther.addActionListener(this);

      //JPanel agentButtonPanel=new JPanel();
      //agentButtonPanel.add(bCall,BorderLayout.NORTH);
      // agentButtonPanel.add(bCallOther,BorderLayout.NORTH);
      agentPanel.add(bCall, BorderLayout.WEST);
      agentPanel.add(new JScrollPane(agentTextArea), BorderLayout.EAST);

      this.getContentPane().add(agentPanel, BorderLayout.CENTER);
      this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
      //setSize(350, 300);
      this.pack();
      this.setVisible(true);
    }


    public void displayMessage(String s) {
      this.textArea.append(s);
    }


    public void displayAgentMessage(String s) {
      this.agentTextArea.append(s);
    }


    public void actionPerformed(java.awt.event.ActionEvent e) {
      Object source = e.getSource();
      if (source == bStart) {
        this.displayMessage("Start pressed\n");
        this.controler.startPenguin();
      } else if (source == bStop) {
        this.displayMessage("Stop pressed\n");
        this.controler.stopPenguin();
      } else if (source == bResume) {
        this.controler.continuePenguin();
      } else if (source == bCall) {
        this.controler.askLocation();
      } else {
        this.controler.askToCallOther();
      }
    }
  }


  public static void main(String args[]) {
    PenguinControler pc = new PenguinControler(null);

  }
}
