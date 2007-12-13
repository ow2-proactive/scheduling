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
package org.objectweb.proactive.extensions.scilab.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class DialogResult extends javax.swing.JDialog {

    /**
     *
     */
    private JPanel pnlMain;
    private JLabel lblPath;
    private JPanel pnlPath;
    private JPanel pnlOutParam;
    private JScrollPane scrollInit;
    private JTextField txtPath;
    private JPanel pnlPathSouth;
    private JPanel pnlPathNorth;
    private JTextArea txtInit;
    private JFileChooser chooserSave;
    private JButton btnQuit;
    private JButton btnSave;
    private JTextArea txtOutParam;
    private JScrollPane scrollOutParam;
    private JPanel pnlQuit;
    private JPanel pnlInit;
    private JPanel pnlTask;

    public DialogResult(JFrame frame) {
        super(frame);
        initGUI();
    }

    private void initGUI() {
        try {
            chooserSave = new JFileChooser();
            chooserSave.setDialogTitle("Save Scilab Result");
            {
                pnlMain = (JPanel) getContentPane();
                BorderLayout pnlMainLayout = new BorderLayout();
                pnlMain.setLayout(pnlMainLayout);
                pnlMain.setSize(new java.awt.Dimension(510, 560));
                {
                    pnlTask = new JPanel();
                    BorderLayout pnlTaskLayout = new BorderLayout();
                    pnlMain.add(pnlTask, BorderLayout.CENTER);
                    pnlTask.setLayout(pnlTaskLayout);
                    pnlTask.setSize(new java.awt.Dimension(510, 635));
                    {
                        pnlInit = new JPanel();
                        BorderLayout pnlInitLayout = new BorderLayout();
                        pnlTask.add(pnlInit, BorderLayout.NORTH);

                        pnlInit.setLayout(pnlInitLayout);
                        pnlInit.setSize(570, 235);
                        pnlInit.setBorder(BorderFactory.createTitledBorder("Initialize Script"));
                        scrollInit = new JScrollPane();
                        pnlInit.add(scrollInit, BorderLayout.CENTER);
                        scrollInit.setPreferredSize(new java.awt.Dimension(500, 210));
                        scrollInit.setSize(500, 210);
                        txtInit = new JTextArea();
                        scrollInit.setViewportView(txtInit);
                        txtInit.setEditable(false);
                        txtInit.setEnabled(false);
                    }

                    {
                        pnlOutParam = new JPanel();
                        BorderLayout pnlOutParamLayout = new BorderLayout();
                        pnlTask.add(pnlOutParam, BorderLayout.CENTER);
                        pnlOutParam.setLayout(pnlOutParamLayout);
                        pnlOutParam.setBorder(BorderFactory.createTitledBorder("Out Parameters"));
                        scrollOutParam = new JScrollPane();
                        pnlOutParam.add(scrollOutParam, BorderLayout.CENTER);
                        scrollOutParam.setPreferredSize(new java.awt.Dimension(553, 256));
                        scrollOutParam.setSize(500, 210);
                        txtOutParam = new JTextArea();
                        scrollOutParam.setViewportView(txtOutParam);
                        txtOutParam.setEditable(false);
                    }
                }

                {
                    pnlQuit = new JPanel();
                    FlowLayout pnlQuitLayout = new FlowLayout();
                    pnlQuitLayout.setAlignment(FlowLayout.RIGHT);
                    pnlMain.add(pnlQuit, BorderLayout.SOUTH);
                    pnlQuit.setLayout(pnlQuitLayout);
                    pnlQuit.setPreferredSize(new java.awt.Dimension(571, 32));
                    btnSave = new JButton();
                    pnlQuit.add(btnSave);
                    btnSave.setText("Save");
                    btnSave.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            btnSaveActionPerformed(evt);
                        }
                    });
                    btnQuit = new JButton();
                    pnlQuit.add(btnQuit);
                    btnQuit.setText("Quit");
                    btnQuit.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            btnQuitActionPerformed(evt);
                        }
                    });
                }

                {
                    pnlPath = new JPanel();
                    pnlMain.add(pnlPath, BorderLayout.NORTH);
                    BorderLayout pnlPathLayout = new BorderLayout();
                    pnlPathLayout.setHgap(5);
                    pnlPath.setLayout(pnlPathLayout);
                    lblPath = new JLabel();
                    lblPath.setLayout(null);
                    pnlPath.add(lblPath, BorderLayout.WEST);
                    lblPath.setText("Select Script:");
                    lblPath.setSize(new java.awt.Dimension(90, 14));
                    lblPath.setPreferredSize(new java.awt.Dimension(81, 23));
                    txtPath = new JTextField();
                    pnlPath.add(txtPath, BorderLayout.CENTER);
                    txtPath.setEditable(false);
                    txtPath.setEnabled(false);
                    txtPath.setSize(380, 15);
                    txtPath.setPreferredSize(new java.awt.Dimension(494, 15));
                    pnlPathNorth = new JPanel();
                    pnlPath.add(pnlPathNorth, BorderLayout.NORTH);
                    pnlPathSouth = new JPanel();
                    pnlPath.add(pnlPathSouth, BorderLayout.SOUTH);
                }
            }

            this.setTitle("Display Scilab Result");
            this.setSize(579, 614);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void btnSaveActionPerformed(ActionEvent evt) {
        if (this.chooserSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File f = this.chooserSave.getSelectedFile().getAbsoluteFile();
        if (f.exists() && !f.canWrite()) {
            return;
        }

        try {
            FileWriter fw = new FileWriter(f);
            fw.write(this.txtOutParam.getText());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSaveEnable(boolean isEnable) {
        this.btnSave.setEnabled(isEnable);
    }

    private void btnQuitActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }

    public void setPathScript(String pathScript) {
        this.txtPath.setText(pathScript);
    }

    public void setJobInit(String jobInit) {
        this.txtInit.setText(jobInit);
    }

    public void setDataOut(String dataOut) {
        this.txtOutParam.setText(dataOut);
    }
}
