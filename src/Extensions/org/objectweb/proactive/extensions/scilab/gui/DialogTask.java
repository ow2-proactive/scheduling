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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;


public class DialogTask extends javax.swing.JDialog {

    /**
         *
         */
    private static final long serialVersionUID = 1087324934691115701L;
    private JPanel pnlPath;
    private JLabel lblPath;
    private JTextField txtOutParam;
    private JTextField txtPath;
    private JLabel lblPriority;
    private JLabel lblOutParam;
    private JComboBox cmbPriority;
    private JPanel pnlTaskPriority;
    private JPanel pnlPathSouth;
    private JPanel pnlPathNorth;
    private JFileChooser chooserScript;
    private JButton btnCancel;
    private JButton btnStart;
    private JTextArea txtInit;
    private JScrollPane scrollInit;
    private JPanel pnlValid;
    private JPanel pnlTask;
    private JPanel pnlMain;
    private JButton btnPath;
    private JPanel pnlTaskSouth;
    private JPanel pnlInit;
    public static final int VALIDATE = 1;
    public static final int CANCEL = 0;
    private File scriptFile;
    private int state;

    public DialogTask(JFrame frame) {
        super(frame);
        initGUI();
    }

    private void initGUI() {
        try {
            {
                chooserScript = new JFileChooser();
                FileFilter filter_all = new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            if (f == null) {
                                return false;
                            }

                            if (f.isDirectory()) {
                                return true;
                            }

                            return f.getName().endsWith(".sci") ||
                            f.getName().endsWith(".sce") ||
                            f.getName().endsWith(".m");
                        }

                        @Override
                        public String getDescription() {
                            return "Scilab & Matlab Files";
                        }
                    };

                FileFilter filter_sci = new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            if (f == null) {
                                return false;
                            }

                            if (f.isDirectory()) {
                                return true;
                            }

                            return f.getName().endsWith(".sci") ||
                            f.getName().endsWith(".sce");
                        }

                        @Override
                        public String getDescription() {
                            return "Scilab Files";
                        }
                    };

                FileFilter filter_mat = new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            if (f == null) {
                                return false;
                            }

                            if (f.isDirectory()) {
                                return true;
                            }

                            return f.getName().endsWith(".m");
                        }

                        @Override
                        public String getDescription() {
                            return "Matlab Files";
                        }
                    };

                chooserScript.addChoosableFileFilter(filter_sci);
                chooserScript.addChoosableFileFilter(filter_mat);
                chooserScript.addChoosableFileFilter(filter_all);
            }

            {
                pnlMain = (JPanel) getContentPane();
                BorderLayout pnlMainLayout = new BorderLayout();
                pnlMain.setLayout(pnlMainLayout);

                pnlMain.setPreferredSize(new java.awt.Dimension(510, 330));
                {
                    pnlTask = new JPanel();
                    BorderLayout pnlTaskLayout = new BorderLayout();
                    pnlTask.setLayout(pnlTaskLayout);
                    pnlMain.add(pnlTask, BorderLayout.CENTER);
                    {
                        pnlInit = new JPanel();
                        pnlTask.add(pnlInit, BorderLayout.CENTER);
                        BorderLayout pnlInitLayout = new BorderLayout();
                        pnlInit.setLayout(pnlInitLayout);
                        pnlInit.setPreferredSize(new java.awt.Dimension(396, 228));
                        pnlInit.setSize(393, 23);
                        pnlInit.setBorder(BorderFactory.createTitledBorder(
                                "Initialize Script"));
                        scrollInit = new JScrollPane();
                        pnlInit.add(scrollInit, BorderLayout.CENTER);
                        scrollInit.setPreferredSize(new java.awt.Dimension(
                                502, 203));
                        txtInit = new JTextArea();
                        scrollInit.setViewportView(txtInit);
                    }

                    {
                        pnlTaskSouth = new JPanel();
                        pnlTask.add(pnlTaskSouth, BorderLayout.SOUTH);
                        BorderLayout pnlOutParamLayout = new BorderLayout();
                        pnlOutParamLayout.setHgap(5);
                        pnlTaskSouth.setLayout(pnlOutParamLayout);
                        pnlTaskSouth.setPreferredSize(new java.awt.Dimension(
                                512, 31));
                        pnlTaskSouth.setBorder(BorderFactory.createTitledBorder(
                                ""));
                        txtOutParam = new JTextField();
                        pnlTaskSouth.add(txtOutParam, BorderLayout.CENTER);
                        txtOutParam.setPreferredSize(new java.awt.Dimension(
                                213, 15));
                        {
                            pnlTaskPriority = new JPanel();
                            BorderLayout pnlTaskPriorityLayout = new BorderLayout();
                            pnlTaskPriorityLayout.setHgap(5);
                            pnlTaskPriority.setLayout(pnlTaskPriorityLayout);
                            pnlTaskSouth.add(pnlTaskPriority, BorderLayout.EAST);
                            lblPriority = new JLabel();
                            pnlTaskPriority.add(lblPriority, BorderLayout.WEST);
                            lblPriority.setText("Priority Task:");
                            lblPriority.setPreferredSize(new java.awt.Dimension(
                                    84, 16));
                            {
                                ComboBoxModel cmbPriorityModel = new DefaultComboBoxModel(new String[] {
                                            "Low", "Normal", "High"
                                        });
                                cmbPriority = new JComboBox();
                                pnlTaskPriority.add(cmbPriority,
                                    BorderLayout.CENTER);
                                cmbPriority.setModel(cmbPriorityModel);
                                cmbPriority.setPreferredSize(new java.awt.Dimension(
                                        79, 19));
                                cmbPriority.setSelectedIndex(1);
                            }
                        }

                        lblOutParam = new JLabel();
                        pnlTaskSouth.add(lblOutParam, BorderLayout.WEST);
                        lblOutParam.setText("Out Parameters:");
                        lblOutParam.setPreferredSize(new java.awt.Dimension(
                                111, 17));
                    }

                    {
                        pnlPath = new JPanel();
                        pnlTask.add(pnlPath, BorderLayout.NORTH);
                        BorderLayout pnlPathLayout = new BorderLayout();
                        pnlPathLayout.setHgap(5);
                        pnlPath.setLayout(pnlPathLayout);
                        pnlPath.setPreferredSize(new java.awt.Dimension(512, 40));
                        lblPath = new JLabel();
                        pnlPath.add(lblPath, BorderLayout.WEST);
                        lblPath.setText("Select Script:");
                        lblPath.setPreferredSize(new java.awt.Dimension(81, 14));
                        txtPath = new JTextField();
                        pnlPath.add(txtPath, BorderLayout.CENTER);
                        txtPath.setPreferredSize(new java.awt.Dimension(325, 22));
                        txtPath.setSize(72, 22);
                        btnPath = new JButton();
                        pnlPath.add(btnPath, BorderLayout.EAST);
                        btnPath.setText("Browse");
                        btnPath.setPreferredSize(new java.awt.Dimension(85, 22));
                        btnPath.setSize(72, 22);
                        btnPath.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    btnPathActionPerformed(evt);
                                }
                            });
                        pnlPathNorth = new JPanel();
                        pnlPath.add(pnlPathNorth, BorderLayout.NORTH);
                        pnlPathSouth = new JPanel();
                        pnlPath.add(pnlPathSouth, BorderLayout.SOUTH);
                    }
                }

                {
                    pnlValid = new JPanel();
                    FlowLayout pnlValidLayout = new FlowLayout();
                    pnlValidLayout.setAlignment(FlowLayout.RIGHT);
                    pnlValid.setLayout(pnlValidLayout);
                    pnlMain.add(pnlValid, BorderLayout.SOUTH);
                    pnlValid.setPreferredSize(new java.awt.Dimension(512, 33));
                    btnCancel = new JButton();
                    pnlValid.add(btnCancel);
                    btnCancel.setText("Cancel");
                    btnCancel.setPreferredSize(new java.awt.Dimension(75, 22));
                    btnCancel.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                btnCancelActionPerformed(evt);
                            }
                        });
                    btnStart = new JButton();
                    pnlValid.add(btnStart);
                    btnStart.setText("Start");
                    btnStart.setPreferredSize(new java.awt.Dimension(75, 22));
                    btnStart.setToolTipText(
                        "Create and launch the defined task");
                    btnStart.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                btnStartActionPerformed(evt);
                            }
                        });
                }
            }

            this.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        state = DialogTask.CANCEL;
                    }
                });

            this.setTitle("Open Scilab/Matlab Task");
            this.setSize(520, 354);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void btnPathActionPerformed(ActionEvent evt) {
        if (this.chooserScript.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        scriptFile = this.chooserScript.getSelectedFile();

        this.txtPath.setText(scriptFile.getAbsolutePath());
    }

    private void btnCancelActionPerformed(ActionEvent evt) {
        this.state = DialogTask.CANCEL;
        this.setVisible(false);
    }

    private void btnStartActionPerformed(ActionEvent evt) {
        state = DialogTask.VALIDATE;
        this.setVisible(false);
    }

    public int getState() {
        return state;
    }

    public File getFile() {
        return this.scriptFile;
    }

    public String getJobInit() {
        return this.txtInit.getText();
    }

    public String[] getDataOut() {
        return this.txtOutParam.getText().split(";");
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTaskPriority() {
        return (String) this.cmbPriority.getSelectedItem();
    }
}
