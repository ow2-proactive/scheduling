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
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;

import javax.swing.*;
import java.awt.*;

public class AdvancedPenguinControler extends PenguinControler implements org.objectweb.proactive.Active, java.io.Serializable {

  //The image panel
  private Penguin penguin;
  private transient AdvancedPenguinControlerFrame controlerFrame;
  protected DefaultListModel listModel;
  private Penguin[] penguinArray = new Penguin[10];
  String[] args;
  private AdvancedPenguinControler activeRef;
  private MigrationStrategyManagerImpl myStrategyManager;


  public AdvancedPenguinControler() {
  }


  public AdvancedPenguinControler(String[] args) {
    this.listModel = new DefaultListModel();

    this.args = args;
    try {
      activeRef = (AdvancedPenguinControler) ProActive.turnActive(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //	this.rebuld();
    //	this.controlerFrame=new AdvancedPenguinControlerFrame(activeRef, listModel);
  }


  public AdvancedPenguinControler(Penguin penguin) {
    this.penguin = penguin;
  }


  public void rebuild() {

    this.controlerFrame = new AdvancedPenguinControlerFrame((AdvancedPenguinControler) ProActive.getStubOnThis(), listModel);
  }


  public void clean() {
    if (controlerFrame != null) {
      controlerFrame.dispose();
      controlerFrame = null;
    }
  }


  public void startPenguin() {
    int index = controlerFrame.agentList.getSelectedIndex();
    this.penguinArray[index].startItinerary();
    //	this.penguin.startItinerary();
  }


  public void stopPenguin() {
    int index = controlerFrame.agentList.getSelectedIndex();
    this.penguinArray[index].stopItinerary();
  }


  public void continuePenguin() {
    int index = controlerFrame.agentList.getSelectedIndex();
    this.penguinArray[index].continueItinerary();
  }


  public void askLocation() {
    int index = controlerFrame.agentList.getSelectedIndex();
    //System.out.println("Calling " + this.penguinArray[index]);
    Message m = this.penguinArray[index].getPosition();
    controlerFrame.displayAgentMessage(m.getMessage());
  }


  public void askToCallOther() {
    int index = controlerFrame.agentList.getSelectedIndex();
    //System.out.println("Calling " + index + " " + this.penguinArray[index]);
    this.penguinArray[index].callOther();

  }


  public void receiveMessage(String s) {
    this.controlerFrame.displayMessage(s);
  }


  public void addAgent() {
    Object[] arg = new Object[1];
    arg[0] = new Integer(listModel.size() + 1);
    try {
      penguinArray[listModel.size()] = (Penguin) org.objectweb.proactive.ProActive.newActive(
              Penguin.class.getName(), arg);
      penguinArray[listModel.size()].initialize(args);
      penguinArray[listModel.size()].setControler(activeRef);

      listModel.addElement("Agent " + (listModel.size() + 1));
    } catch (Exception e) {
      e.printStackTrace();
    }
    createLink();
  }


  public void createLink() {
    int size = listModel.size();
    if (size > 1) {
      penguinArray[size - 2].setOther(penguinArray[size - 1]);
    }

  }


  public void live(Body b) {
    //	this.listModel= new DefaultListModel();
    //	this.controlerFrame=new AdvancedPenguinControlerFrame((AdvancedPenguinControler) ProActive.getStubOnThis(), listModel);
    //	penguin.setControler((AdvancedPenguinControler)ProActive.getStubOnThis());
    myStrategyManager = new org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl(
            (org.objectweb.proactive.core.body.migration.Migratable) org.objectweb.proactive.ProActive.getBodyOnThis());
    myStrategyManager.onDeparture("clean");
    this.rebuild();
    b.fifoPolicy();
  }


  protected class AdvancedPenguinControlerFrame extends javax.swing.JFrame implements java.awt.event.ActionListener {
    private AdvancedPenguinControler controler;
    private javax.swing.JPanel buttonPanel;
    private JPanel textPanel;
    private JPanel agentPanel;
    //	private DefaultListModel listModel;
    protected javax.swing.JTextArea textArea;
    protected javax.swing.JTextArea agentTextArea;
    protected JList agentList;
    protected javax.swing.JButton bStop;
    protected javax.swing.JButton bStart;
    protected javax.swing.JButton bResume;
    protected javax.swing.JButton bCall;
    protected javax.swing.JButton bCallOther;
    protected javax.swing.JButton bAddAgent;
    protected DefaultListModel listModel;


    protected AdvancedPenguinControlerFrame(AdvancedPenguinControler c, DefaultListModel listModel) {
      super("Advanced Penguin Controler");
      this.controler = c;
      this.listModel = listModel;

      buttonPanel = new JPanel();

      bStart = new javax.swing.JButton("Start");
      bStart.addActionListener(this);
      buttonPanel.add(bStart);


      bResume = new javax.swing.JButton("Resume");
      bResume.addActionListener(this);
      buttonPanel.add(bResume);


      bStop = new javax.swing.JButton("Stop");
      bStop.addActionListener(this);
      buttonPanel.add(bStop);


      textPanel = new JPanel();
      textArea = new JTextArea(20, 20);
      textPanel.add(new JScrollPane(textArea));
      this.getContentPane().add(textPanel, BorderLayout.NORTH);


      agentPanel = new JPanel();
      agentTextArea = new JTextArea(5, 30);
      bCall = new javax.swing.JButton("Call Agent");
      bCall.addActionListener(this);
      bCallOther = new javax.swing.JButton("Call Other Agent");
      bCallOther.addActionListener(this);
      bAddAgent = new javax.swing.JButton("add agent");
      bAddAgent.addActionListener(this);


      JPanel agentButtonPanel = new JPanel();
      agentButtonPanel.setLayout(new GridLayout(3, 1));
      agentButtonPanel.add(bCall);
      agentButtonPanel.add(bAddAgent);
      agentButtonPanel.add(bCallOther);

      //agentButtonPanel.add(bCallOther,BorderLayout.NORTH);

      // listModel = new DefaultListModel();
      agentList = new JList(listModel);
      agentPanel.add(agentButtonPanel, BorderLayout.WEST);
      agentPanel.add(new JScrollPane(agentList), BorderLayout.EAST);
      this.getContentPane().add(agentPanel);


      this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
      //setSize(350, 300);
      this.pack();
      this.setVisible(true);
    }


    public void displayMessage(String s) {
      this.textArea.append(s);
    }


    public void displayAgentMessage(String s) {
      this.textArea.append(s);
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
      } else if (source == bCallOther) {
        this.controler.askToCallOther();
      } else if (source == bAddAgent) {
        //this.createAgent();
        this.controler.addAgent();
      }
    }


    public void createAgent() {
      listModel.addElement("Agent");
    }
  }


  public static void main(String args[]) {
    try {
      //AdvancedPenguinControler pc  = (AdvancedPenguinControler) ProActive.newActive(AdvancedPenguinControler.class.getName(),null,(Node) null);
      AdvancedPenguinControler pc = new AdvancedPenguinControler(args);


    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
