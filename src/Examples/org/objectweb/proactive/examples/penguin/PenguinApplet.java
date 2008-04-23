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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.penguin;

import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.examples.StandardFrame;


public class PenguinApplet extends StandardFrame implements PenguinMessageReceiver {
    private PenguinControler controler;
    private Action startAction;
    private Action suspendAction;
    private Action resumeAction;
    private Action callAction;
    private Action chainedCallAction;
    protected PenguinListModel penguinListModel;
    protected javax.swing.JList agentList;
    protected javax.swing.JTextArea itineraryField;

    public PenguinApplet(PenguinControler c, CircularArrayList<PenguinWrapper> penguinList) {
        super("Advanced Penguin Controler");
        this.controler = c;
        this.penguinListModel = new PenguinListModel(penguinList);
        this.startAction = new StartAction();
        this.suspendAction = new SuspendAction();
        this.resumeAction = new ResumeAction();
        this.callAction = new CallAction();
        this.chainedCallAction = new ChainedCallAction();
        init(600, 300);
    }

    @Override
    public void start() {
        receiveMessage("Started...");
    }

    @Override
    protected javax.swing.JPanel createRootPanel() {
        javax.swing.JPanel rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());

        // WEST PANEL
        javax.swing.JPanel p1 = new javax.swing.JPanel(new java.awt.GridLayout(0, 1));
        javax.swing.JButton bStart = new javax.swing.JButton("Start");
        bStart.setToolTipText("Start itinerary");
        bStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeAction(agentList.getSelectedIndices(), startAction);
            }
        });
        p1.add(bStart);

        javax.swing.JButton bSuspend = new javax.swing.JButton("Suspend");
        bSuspend.setToolTipText("Suspend itinerary");
        bSuspend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeAction(agentList.getSelectedIndices(), suspendAction);
            }
        });
        p1.add(bSuspend);

        javax.swing.JButton bResume = new javax.swing.JButton("Resume");
        bResume.setToolTipText("Resume itinerary");
        bResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeAction(agentList.getSelectedIndices(), resumeAction);
            }
        });
        p1.add(bResume);

        javax.swing.JButton bSet = new javax.swing.JButton("Set itinerary");
        bSet.setToolTipText("Set new itinerary");
        bSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setItinerary(agentList.getSelectedIndices());
            }
        });
        p1.add(bSet);

        rootPanel.add(p1, java.awt.BorderLayout.WEST);

        // EAST PANEL
        javax.swing.JPanel p2 = new javax.swing.JPanel(new java.awt.GridLayout(0, 1));
        javax.swing.JButton bAddAgent = new javax.swing.JButton("Add agent");
        bAddAgent.setToolTipText("Add one new agent");
        bAddAgent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                //receiveMessage("Add new agent pressed");
                addAgent();
            }
        });
        p2.add(bAddAgent);

        javax.swing.JButton bCall = new javax.swing.JButton("Call");
        bCall.setToolTipText("Call");
        bCall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeAction(agentList.getSelectedIndices(), callAction);
            }
        });
        p2.add(bCall);

        javax.swing.JButton bCallOther = new javax.swing.JButton("Chained calls");
        bCallOther.setToolTipText("Chained calls from selected agent");
        bCallOther.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeAction(agentList.getSelectedIndices(), chainedCallAction);
            }
        });
        p2.add(bCallOther);

        javax.swing.JButton bGet = new javax.swing.JButton("Get itinerary");
        bGet.setToolTipText("Get current itinerary");
        bGet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                itineraryField.setText(getItinerary(agentList.getSelectedIndices()));
            }
        });
        p2.add(bGet);

        rootPanel.add(p2, java.awt.BorderLayout.EAST);

        // CENTER PANEL
        javax.swing.JPanel p3 = new javax.swing.JPanel(new java.awt.GridLayout(1, 0));

        agentList = new javax.swing.JList(penguinListModel);
        agentList.setToolTipText("Agent list");
        p3.add(new javax.swing.JScrollPane(agentList));

        itineraryField = new javax.swing.JTextArea();
        itineraryField.setToolTipText("Itinerary of selected agent");
        p3.add(new javax.swing.JScrollPane(itineraryField));

        rootPanel.add(p3, java.awt.BorderLayout.CENTER);
        return rootPanel;
    }

    private void addAgent() {
        int n = penguinListModel.getSize();
        try {
            Penguin newPenguin = controler.createPenguin(n);
            PenguinWrapper pw = new PenguinWrapper(newPenguin, "Agent " + n);
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

    private String getItinerary(int[] selection) {
        if (selection.length == 0) {
            receiveMessage("You must select an agent in the list first or add one if none is already present");
        } else if (selection.length > 1) {
            receiveMessage("You must select one agent");
        } else {
            int value = selection[0];
            PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(value);
            String[] tabItinerary = p.getItinerary();
            String itinerary = new String();
            if (tabItinerary.length != 0) {
                itinerary = tabItinerary[0];
                for (int i = 1; i < tabItinerary.length; i++) {
                    itinerary = itinerary + "\n" + tabItinerary[i];
                }
            }
            return itinerary;
        }

        //return null if problem of selection
        return null;
    }

    private void setItinerary(int[] selection) {
        if (selection.length == 0) {
            receiveMessage("You must select an agent in the list first or add one if none is already present");
        } else {
            for (int i = 0; i < selection.length; i++) {
                int value = selection[i];
                PenguinWrapper p = (PenguinWrapper) penguinListModel.getElementAt(value);
                p.setItinerary(itineraryField.getText());
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

    private class SuspendAction implements Action {
        public void execute(Penguin p) {
            p.suspend();
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
        private CircularArrayList<PenguinWrapper> penguinList;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public PenguinListModel(CircularArrayList<PenguinWrapper> penguinList) {
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
            if (n > 0) {
                penguinList.clear();
                fireIntervalRemoved(this, 0, n - 1);
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

        public String[] getItinerary() {
            return penguin.getItinerary();
        }

        public void setItinerary(String itinerary) {
            java.util.ArrayList<String> itineraryList = new java.util.ArrayList<String>();
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(itinerary, "\n");
            while (tokenizer.hasMoreTokens())
                itineraryList.add(tokenizer.nextToken());
            String[] intineraryArray = itineraryList.toArray(new String[0]);
            penguin.setItinerary(intineraryArray);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
