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

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * @author acontes
 *
 */
public class RuleCommunication extends JPanel {
    private JRadioButton communicationAuthorizedYes = null;
    private JRadioButton communicationAuthorizedNo = null;
    private ButtonGroup groupAuthentication = new ButtonGroup();
    private ButtonGroup groupConfidentiality = new ButtonGroup();
    private ButtonGroup groupIntegrity = new ButtonGroup();
    private String[] attributes = { "Authorized", "Optional", "Denied" };
    private JComboBox comboAuth = null;
    private JComboBox comboConf = null;
    private JComboBox comboInt = null;

    /**
     * This is the default constructor
     */
    public RuleCommunication() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        javax.swing.JLabel jLabel11 = new JLabel();
        javax.swing.JLabel jLabel7 = new JLabel();
        javax.swing.JLabel jLabel3 = new JLabel();
        javax.swing.JLabel jLabel2 = new JLabel();
        javax.swing.JLabel jLabel1 = new JLabel();
        javax.swing.JLabel jLabel = new JLabel();
        this.setLayout(null);
        this.setSize(337, 204);
        jLabel.setBounds(11, 8, 190, 26);
        jLabel.setText("Communication authorized ? :");
        jLabel1.setBounds(63, 35, 30, 26);
        jLabel1.setText("Yes");
        jLabel2.setBounds(144, 35, 26, 26);
        jLabel2.setText("No");
        jLabel3.setBounds(13, 64, 108, 28);
        jLabel3.setText("Authentication");
        jLabel7.setBounds(15, 96, 108, 28);
        jLabel7.setText("Confidentiality");
        jLabel11.setBounds(16, 132, 108, 28);
        jLabel11.setText("Integrity");
        this.add(jLabel, null);
        this.add(getCommunicationAuthorizedYes(), null);
        this.add(getCommunicationAuthorizedNo(), null);
        this.add(jLabel3, null);
        this.add(jLabel7, null);
        this.add(jLabel11, null);
        this.add(getComboAuth(), null);
        this.add(getComboConf(), null);
        this.add(getComboInt(), null);
        this.add(jLabel1, null);
        this.add(jLabel2, null);
    }

    /**
     * This method initializes communicationAuthorizedYes
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getCommunicationAuthorizedYes() {
        if (communicationAuthorizedYes == null) {
            communicationAuthorizedYes = new JRadioButton();
            communicationAuthorizedYes.setBounds(36, 40, 21, 21);
        }
        return communicationAuthorizedYes;
    }

    /**
     * This method initializes communicationAuthorizedNo
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getCommunicationAuthorizedNo() {
        if (communicationAuthorizedNo == null) {
            communicationAuthorizedNo = new JRadioButton();
            communicationAuthorizedNo.setBounds(109, 40, 21, 21);
        }
        return communicationAuthorizedNo;
    }

    /**
     * This method initializes comboAuth
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getComboAuth() {
        if (comboAuth == null) {
            comboAuth = new JComboBox(attributes);
            comboAuth.setBounds(139, 68, 169, 23);
        }
        return comboAuth;
    }

    /**
     * This method initializes comboConf
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getComboConf() {
        if (comboConf == null) {
            comboConf = new JComboBox(attributes);
            comboConf.setBounds(140, 100, 169, 23);
        }
        return comboConf;
    }

    /**
     * This method initializes comboInt
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getComboInt() {
        if (comboInt == null) {
            comboInt = new JComboBox(attributes);
            comboInt.setBounds(141, 134, 169, 23);
        }
        return comboInt;
    }
} //  @jve:decl-index=0:visual-constraint="74,84"
