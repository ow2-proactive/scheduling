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
package org.objectweb.proactive.core.security.gui;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;


/**
 * @author acontes
 *
 */
public class RuleEditor extends JPanel {
    private JTabbedPane jTabbedPane = null;
    private RuleCommunication ruleCommunicationRequest = null;
    private RuleCommunication ruleCommunicationRequest1 = null;
    private JScrollPane jScrollPane = null;
    private JScrollPane jScrollPane1 = null;
    private JList jListFrom = null;
    private JList jListTo = null;
    private JButton addFrom = null;
    private JButton removeFrom = null;
    private JButton addTo = null;
    private JButton removeTo = null;

    /**
     * This is the default constructor
     */
    public RuleEditor() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setLayout(null);
        this.setSize(613, 454);
        this.add(getJTabbedPane(), null);
        this.add(getJScrollPane(), null);
        this.add(getJScrollPane1(), null);
        this.add(getAddFrom(), null);
        this.add(getRemoveFrom(), null);
        this.add(getAddTo(), null);
        this.add(getRemoveTo(), null);
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setBounds(22, 244, 328, 201);
            jTabbedPane.addTab("Request", null, getRuleCommunicationRequest(),
                null);
            jTabbedPane.addTab("Reply", null, getRuleCommunicationRequest1(),
                null);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes ruleCommunicationRequest
     *
     * @return org.objectweb.proactive.ext.security.gui.RuleCommunication
     */
    private RuleCommunication getRuleCommunicationRequest() {
        if (ruleCommunicationRequest == null) {
            ruleCommunicationRequest = new RuleCommunication();
        }
        return ruleCommunicationRequest;
    }

    /**
     * This method initializes ruleCommunicationRequest1
     *
     * @return org.objectweb.proactive.ext.security.gui.RuleCommunication
     */
    private RuleCommunication getRuleCommunicationRequest1() {
        if (ruleCommunicationRequest1 == null) {
            ruleCommunicationRequest1 = new RuleCommunication();
        }
        return ruleCommunicationRequest1;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setBounds(22, 17, 263, 196);
            jScrollPane.setViewportView(getJListFrom());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jScrollPane1
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setBounds(294, 18, 283, 195);
            jScrollPane1.setViewportView(getJListTo());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jListFrom
     *
     * @return javax.swing.JList
     */
    private JList getJListFrom() {
        if (jListFrom == null) {
            jListFrom = new JList();
        }
        return jListFrom;
    }

    /**
     * This method initializes jListTo
     *
     * @return javax.swing.JList
     */
    private JList getJListTo() {
        if (jListTo == null) {
            jListTo = new JList();
        }
        return jListTo;
    }

    /**
     * This method initializes addFrom
     *
     * @return javax.swing.JButton
     */
    private JButton getAddFrom() {
        if (addFrom == null) {
            addFrom = new JButton();
            addFrom.setBounds(115, 218, 66, 20);
            addFrom.setText("Add");
        }
        return addFrom;
    }

    /**
     * This method initializes removeFrom
     *
     * @return javax.swing.JButton
     */
    private JButton getRemoveFrom() {
        if (removeFrom == null) {
            removeFrom = new JButton();
            removeFrom.setBounds(184, 217, 89, 22);
            removeFrom.setText("Remove");
            removeFrom.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        jListFrom.remove(jListFrom.getSelectedIndex());
                    }
                });
        }
        return removeFrom;
    }

    /**
     * This method initializes addTo
     *
     * @return javax.swing.JButton
     */
    private JButton getAddTo() {
        if (addTo == null) {
            addTo = new JButton();
            addTo.setBounds(422, 218, 61, 20);
            addTo.setText("Add");
            addTo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        System.out.println("actionPerformed()");
                    }
                });
        }
        return addTo;
    }

    /**
     * This method initializes removeTo
     *
     * @return javax.swing.JButton
     */
    private JButton getRemoveTo() {
        if (removeTo == null) {
            removeTo = new JButton();
            removeTo.setBounds(492, 217, 83, 23);
            removeTo.setText("Remove");
            removeTo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        jListTo.remove(jListTo.getSelectedIndex());
                    }
                });
        }
        return removeTo;
    }
} //  @jve:decl-index=0:visual-constraint="24,28"
