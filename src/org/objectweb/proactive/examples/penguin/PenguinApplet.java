/**
 * Created on 27 mai 2002
 *
 * To change this generated comment edit the template variable "filecomment":
 * Window>Preferences>Java>Templates.
 */
package org.objectweb.proactive.examples.penguin;

import org.objectweb.proactive.examples.StandardFrame;
import org.objectweb.proactive.core.util.CircularArrayList;

public class PenguinApplet extends StandardFrame implements PenguinMessageReceiver {
  private PenguinControler controler;
  protected PenguinListModel penguinListModel;
  protected javax.swing.JList agentList;

  public PenguinApplet(PenguinControler c, CircularArrayList penguinList) {
    super("Advanced Penguin Controler");
    this.controler = c; 
    this.penguinListModel = new PenguinListModel(penguinList);
    init(500, 300);
  }

  public void start() {
    receiveMessage("Started...");
  }

  protected javax.swing.JPanel createRootPanel() {
    javax.swing.JPanel rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());

    // SOUTH PANEL
    javax.swing.JPanel southPanel = new javax.swing.JPanel();
    javax.swing.JButton bStart = new javax.swing.JButton("Start");
    bStart.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int n = getIndex();
        if (n >= 0) {
          receiveMessage("Start pressed for Agent "+n);
          PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(n);
          p.penguin.startItinerary();
        }
      }
    });
    southPanel.add(bStart);

    javax.swing.JButton bResume = new javax.swing.JButton("Resume");
    bResume.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int n = getIndex();
        if (n >= 0) {
          receiveMessage("Resume pressed for Agent "+n);
          PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(n);
          p.penguin.continueItinerary();
        }
      }
    });
    southPanel.add(bResume);

    javax.swing.JButton bStop = new javax.swing.JButton("Stop");
    bStop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int n = getIndex();
        if (n >= 0) {  
          receiveMessage("Stop pressed for Agent "+n);
          PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(n);
          p.penguin.stopItinerary();
        }
      }
    });
    southPanel.add(bStop);
    rootPanel.add(southPanel, java.awt.BorderLayout.SOUTH);
    
    // WEST PANEL
    javax.swing.JPanel westPanel = new javax.swing.JPanel(new java.awt.GridLayout(3, 1));
    javax.swing.JButton bCall = new javax.swing.JButton("Call Agent");
    bCall.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int n = getIndex();
        if (n >= 0) {  
          receiveMessage("Call agent pressed for Agent "+n);
          PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(n);
          Message m = p.penguin.getPosition();
          receiveMessage(m.getMessage());
        }
      }
    });
    westPanel.add(bCall);
    javax.swing.JButton bCallOther = new javax.swing.JButton("Call Other Agent");
    bCallOther.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int n = getIndex();
        if (n >= 0) {  
          receiveMessage("Call Other pressed for Agent "+n);
          PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(n);
          p.penguin.callOther();
        }
      }
    });
    westPanel.add(bCallOther);
    javax.swing.JButton bAddAgent = new javax.swing.JButton("add agent");
    bAddAgent.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        receiveMessage("Add new agent pressed");
        addAgent();
      } 
    });
    westPanel.add(bAddAgent);
    rootPanel.add(westPanel, java.awt.BorderLayout.WEST);

    // CENTER PANEL
    agentList = new javax.swing.JList(penguinListModel);
    rootPanel.add(new javax.swing.JScrollPane(agentList), java.awt.BorderLayout.CENTER);
    return rootPanel;
  }

  private int getIndex() {
    int n = agentList.getSelectedIndex();
    if (n == -1) {
      receiveMessage("You must select an agent in the list first or add one if none is already present");
    }
    return agentList.getSelectedIndex();
  }



  public void addAgent() {
    int n = penguinListModel.getSize();
    try {
      Penguin newPenguin = controler.createPenguin(n);
      PenguinWrapper pw = new PenguinWrapper(newPenguin, "Agent "+n);
      penguinListModel.addPenguin(pw);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (n > 0) {
      PenguinWrapper p1 = (PenguinWrapper) penguinListModel.getElementAt(n - 1);
      PenguinWrapper p2 = (PenguinWrapper) penguinListModel.getElementAt(n);
      (p1.penguin).setOther(p2.penguin);
    }
  }



  private static class PenguinListModel extends javax.swing.AbstractListModel {
  
    private CircularArrayList penguinList;
  
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public PenguinListModel(CircularArrayList penguinList) {
      this.penguinList = penguinList;
    }

    //
    // -- Public methods -----------------------------------------------
    //

    //
    // -- implements ListModel -----------------------------------------------
    //
    public boolean isEmpty() {
      return penguinList.isEmpty();
    }
    
    public int getSize() {
      return penguinList.size();
    }

    public Object getElementAt(int index) {
      return penguinList.get(index);
    }
    
    public void addPenguin(PenguinWrapper pw) {
      int n = penguinList.size();
      penguinList.add(pw);
      fireIntervalAdded(this, n, n);
    }
    
    public void clear() {
      int n = penguinList.size();
      if (n>0) {
        penguinList.clear();
        fireIntervalRemoved(this, 0, n-1);
      }
    }
    
  } // end inner class PenguinListModel


  private static class PenguinWrapper implements java.io.Serializable {
    Penguin penguin;
    String name;
    PenguinWrapper(Penguin penguin, String name) {
      this.penguin = penguin;
      this.name = name;
    }
    
    public String toString() {
      return name;
    }
  }
}


