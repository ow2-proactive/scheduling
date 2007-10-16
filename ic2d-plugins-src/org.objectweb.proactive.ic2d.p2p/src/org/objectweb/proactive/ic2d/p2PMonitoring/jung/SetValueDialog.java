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
package org.objectweb.proactive.ic2d.p2PMonitoring.jung;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class SetValueDialog extends JDialog implements ActionListener {
    private JPanel jContentPane = null;
    private JPanel jPanel = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private JTextField jTextField = null;
    private JPanel jPanel1 = null;
    private JPanel jPanel2 = null;
    private JLabel jLabel = null;
    private String label = null;
    private int value;

    /**
     * This is the default constructor
     */
    public SetValueDialog(String title, String message, int defaultValue) {
        super();
        this.setTitle(title);
        this.label = message;
        initialize();
        this.pack();
        this.jTextField.setText(defaultValue + "");
        this.jTextField.selectAll();
        this.setResizable(false);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        //this.setSize(206, 115);
        this.setModal(true);
        this.setResizable(true);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getButtonPannel(), java.awt.BorderLayout.SOUTH);
            jPanel.add(getLabelPanel(), java.awt.BorderLayout.CENTER);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (okButton == null) {
            okButton = new JButton();
            okButton.setText("OK");
            okButton.addActionListener(this);
        }
        return okButton;
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(this);
        }
        return cancelButton;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField(5);
            //	jTextField.setText("          ");
            jTextField.setBounds(new java.awt.Rectangle(100, 21, 44, 19));
        }
        return jTextField;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPannel() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.add(getJButton(), null);
            jPanel1.add(getJButton1(), null);
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getLabelPanel() {
        if (jPanel2 == null) {
            jLabel = new JLabel();
            jLabel.setText(this.label + "   ");
            jLabel.setBounds(new java.awt.Rectangle(57, 23, 38, 15));
            jPanel2 = new JPanel();
            //	jPanel2.setLayout(new BorderLayout());
            JPanel jPanel3 = new JPanel();
            jPanel3.setLayout(new BorderLayout());
            jPanel3.add(jLabel, BorderLayout.WEST);
            jPanel3.add(getJTextField(), BorderLayout.EAST);
            jPanel2.add(jPanel3, BorderLayout.CENTER);
        }
        return jPanel2;
    }

    public int getValue() {
        return this.value;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            String tmp = jTextField.getText();
            try {
                this.value = Integer.parseInt(tmp);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                this.value = -1;
            }
        }
        if (e.getSource() == cancelButton) {
            this.value = -1;
        }

        this.setVisible(false);
    }

    public static void main(String[] args) {
        SetValueDialog v = new SetValueDialog("Change value", "Noa", 2);
        v.setVisible(true);
        System.out.println(v.getValue());
    }
} //  @jve:decl-index=0:visual-constraint="6,14"
