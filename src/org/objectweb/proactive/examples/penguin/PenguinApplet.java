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
  private Action startAction;
  private Action stopAction;
  private Action resumeAction;
  private Action callAction;
  private Action chainedCallAction;
  protected PenguinListModel penguinListModel;
  protected javax.swing.JList agentList;

  public PenguinApplet(PenguinControler c, CircularArrayList penguinList) {
    super("Advanced Penguin Controler");
    this.controler = c; 
    this.penguinListModel = new PenguinListModel(penguinList);
    this.startAction = new StartAction();
    this.stopAction = new StopAction();
    this.resumeAction = new ResumeAction();
    this.callAction = new CallAction();
    this.chainedCallAction = new ChainedCallAction();
    init(500, 300);
  }

  public void start() {
    receiveMessage("Started...");
  }

  protected javax.swing.JPanel createRootPanel() {
    javax.swing.JPanel rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());

    // WEST PANEL
    javax.swing.JPanel p1 = new javax.swing.JPanel(new java.awt.GridLayout(0, 1));
    javax.swing.JButton bStart = new javax.swing.JButton("Start");
    bStart.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        executeAction(agentList.getSelectedIndices(), startAction);
      }
    });
    p1.add(bStart);

    javax.swing.JButton bResume = new javax.swing.JButton("Resume");
    bResume.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        executeAction(agentList.getSelectedIndices(), resumeAction);
      }
    });
    p1.add(bResume);

    javax.swing.JButton bStop = new javax.swing.JButton("Stop");
    bStop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        executeAction(agentList.getSelectedIndices(), stopAction);
      }
    });
    p1.add(bStop);

    javax.swing.JButton bCall = new javax.swing.JButton("Call");
    bCall.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        executeAction(agentList.getSelectedIndices(), callAction);
      }
    });
    p1.add(bCall);

    rootPanel.add(p1, java.awt.BorderLayout.WEST);
    
    // SOUTH PANEL
    javax.swing.JPanel p2 = new javax.swing.JPanel(new java.awt.GridLayout(1, 0));
    javax.swing.JButton bAddAgent = new javax.swing.JButton("Add one new agent");
    bAddAgent.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        //receiveMessage("Add new agent pressed");
        addAgent();
      } 
    });
    p2.add(bAddAgent);

    javax.swing.JButton bCallOther = new javax.swing.JButton("Chained calls from selected agent");
    bCallOther.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        executeAction(agentList.getSelectedIndices(), chainedCallAction);
      }
    });
    p2.add(bCallOther);

    rootPanel.add(p2, java.awt.BorderLayout.SOUTH);

    // CENTER PANEL
    agentList = new javax.swing.JList(penguinListModel);
    rootPanel.add(new javax.swing.JScrollPane(agentList), java.awt.BorderLayout.CENTER);
    return rootPanel;
  }


  private void addAgent() {
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
    } else {
      agentList.setSelectedIndex(0);
    }
  }

  
  private void executeAction(int[] selection, Action action) {
    if (selection.length == 0) {
      receiveMessage("You must select an agent in the list first or add one if none is already present");
    } else {
      for (int i = 0; i < selection.length; i++) {
        int value = selection[i];
        PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(value);
        action.execute(p.penguin);
      }
    }
  }

  
  private interface Action {
    public void execute(Penguin p);
  }

  private class StartAction implements Action {
    public void execute(Penguin p) {
      p.start();
    }  
  }
  
  private class StopAction implements Action {
    public void execute(Penguin p) {
      p.stop();
    }  
  }

  private class ResumeAction implements Action {
    public void execute(Penguin p) {
      p.resume();
    }  
  }

  private class CallAction implements Action {
    public void execute(Penguin p) {
      receiveMessage(p.call());
    }  
  }

  private class ChainedCallAction implements Action {
    public void execute(Penguin p) {
      p.chainedCall();
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


