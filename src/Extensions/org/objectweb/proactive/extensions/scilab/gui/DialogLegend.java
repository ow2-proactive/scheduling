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

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


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
public class DialogLegend extends javax.swing.JDialog {

    /**
     *
     */
    private JPanel pnlTaskWait;
    private JPanel pnlTaskRun;
    private JPanel pnlTaskEnd;
    private JPanel pnlLegendRun;
    private JPanel pnlLegendWait;
    private JLabel lblLegendCancel;
    private JLabel lblLegendRun;
    private JPanel pnlIconAbort;
    private JPanel pnlIconSuccess;
    private JPanel pnlIconKill;
    private JPanel pnlIconRun;
    private JPanel pnlIconCancel;
    private JPanel pnlIconWait;
    private JLabel lblLegendAbort;
    private JLabel lblLegendSuccess;
    private JLabel lblLegendKill;
    private JLabel lblLegendWait;
    private JPanel pnlLegendEnd;

    public DialogLegend(JFrame frame) {
        super(frame);
        initGUI();
    }

    private void initGUI() {
        try {
            GridLayout thisLayout = new GridLayout(3, 1);
            thisLayout.setColumns(1);
            thisLayout.setHgap(5);
            thisLayout.setVgap(5);
            thisLayout.setRows(3);
            getContentPane().setLayout(thisLayout);
            pnlTaskWait = new JPanel();
            getContentPane().add(pnlTaskWait);
            pnlTaskWait.setBorder(BorderFactory.createTitledBorder("Pending Tasks"));
            {
                pnlLegendWait = new JPanel();
                GridLayout pnlLegendWaitLayout = new GridLayout(2, 2);
                pnlLegendWaitLayout.setColumns(2);
                pnlLegendWaitLayout.setHgap(5);
                pnlLegendWaitLayout.setVgap(5);
                pnlLegendWaitLayout.setRows(2);
                pnlLegendWait.setLayout(pnlLegendWaitLayout);
                pnlTaskWait.add(pnlLegendWait);
                pnlIconWait = new JPanel() {

                    /**
                     *
                     */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("img/runTask.gif")), 0, 0, this);
                    }
                };
                pnlLegendWait.add(pnlIconWait);
                pnlIconWait.setPreferredSize(new java.awt.Dimension(100, 28));
                pnlIconWait.setSize(100, 28);
                lblLegendWait = new JLabel();
                pnlLegendWait.add(lblLegendWait);
                lblLegendWait.setText("Pending");
                lblLegendWait.setPreferredSize(new java.awt.Dimension(100, 28));
                lblLegendWait.setSize(100, 28);
                pnlIconCancel = new JPanel() {

                    /**
                     *
                     */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("img/stopTask.gif")), 0, 0, this);
                    }
                };
                pnlLegendWait.add(pnlIconCancel);
                pnlIconCancel.setPreferredSize(new java.awt.Dimension(100, 28));
                pnlIconCancel.setSize(100, 28);
                lblLegendCancel = new JLabel();
                pnlLegendWait.add(lblLegendCancel);
                lblLegendCancel.setText("Cancelled");
                lblLegendCancel.setBounds(-49, 96, 60, 30);
                lblLegendCancel.setPreferredSize(new java.awt.Dimension(100, 28));
                lblLegendCancel.setSize(100, 28);
            }

            pnlTaskRun = new JPanel();
            getContentPane().add(pnlTaskRun);
            pnlTaskRun.setBorder(BorderFactory.createTitledBorder("Executing Tasks"));
            pnlTaskRun.setPreferredSize(new java.awt.Dimension(100, 28));
            pnlTaskRun.setSize(100, 28);
            {
                pnlLegendRun = new JPanel();
                GridLayout pnlLegendRunLayout = new GridLayout(2, 2);
                pnlLegendRunLayout.setColumns(2);
                pnlLegendRunLayout.setHgap(5);
                pnlLegendRunLayout.setVgap(5);
                pnlLegendRunLayout.setRows(2);
                pnlLegendRun.setLayout(pnlLegendRunLayout);
                pnlTaskRun.add(pnlLegendRun);
                pnlIconRun = new JPanel() {

                    /**
                     *
                     */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("img/runTask.gif")), 0, 0, this);
                    }
                };
                pnlLegendRun.add(pnlIconRun);
                pnlIconRun.setPreferredSize(new java.awt.Dimension(100, 28));
                pnlIconRun.setSize(100, 28);
                lblLegendRun = new JLabel();
                pnlLegendRun.add(lblLegendRun);
                lblLegendRun.setText("Executing");
                lblLegendRun.setPreferredSize(new java.awt.Dimension(100, 28));
                lblLegendRun.setSize(100, 28);
                pnlIconKill = new JPanel() {

                    /**
                     *
                     */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("img/stopTask.gif")), 0, 0, this);
                    }
                };
                pnlLegendRun.add(pnlIconKill);
                pnlIconKill.setPreferredSize(new java.awt.Dimension(100, 28));
                pnlIconKill.setSize(100, 28);
                lblLegendKill = new JLabel();
                pnlLegendRun.add(lblLegendKill);
                lblLegendKill.setText("Killed");
                lblLegendKill.setBounds(-34, 67, 60, 30);
                lblLegendKill.setPreferredSize(new java.awt.Dimension(100, 28));
                lblLegendKill.setSize(100, 28);
            }

            pnlTaskEnd = new JPanel();
            getContentPane().add(pnlTaskEnd);
            pnlTaskEnd.setBorder(BorderFactory.createTitledBorder("Terminated Tasks"));
            {
                pnlLegendEnd = new JPanel();
                GridLayout pnlLegendEndLayout = new GridLayout(2, 2);
                pnlLegendEndLayout.setColumns(2);
                pnlLegendEndLayout.setHgap(5);
                pnlLegendEndLayout.setVgap(5);
                pnlLegendEndLayout.setRows(2);
                pnlLegendEnd.setLayout(pnlLegendEndLayout);
                pnlTaskEnd.add(pnlLegendEnd);
                pnlIconSuccess = new JPanel() {

                    /**
                     *
                     */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("img/successTask.gif")), 0, 0, this);
                    }
                };
                pnlLegendEnd.add(pnlIconSuccess);
                pnlIconSuccess.setPreferredSize(new java.awt.Dimension(100, 28));
                pnlIconSuccess.setSize(100, 28);
                lblLegendSuccess = new JLabel();
                pnlLegendEnd.add(lblLegendSuccess);
                lblLegendSuccess.setText("Succeeded");
                lblLegendSuccess.setPreferredSize(new java.awt.Dimension(100, 28));
                lblLegendSuccess.setSize(100, 28);
                pnlIconAbort = new JPanel() {

                    /**
                     *
                     */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("img/abortTask.gif")), 0, 0, this);
                    }
                };
                pnlLegendEnd.add(pnlIconAbort);
                pnlIconAbort.setPreferredSize(new java.awt.Dimension(100, 28));
                pnlIconAbort.setSize(100, 28);
                lblLegendAbort = new JLabel();
                pnlLegendEnd.add(lblLegendAbort);
                lblLegendAbort.setText("Aborted");
                lblLegendAbort.setBounds(-83, 19, 78, 14);
                lblLegendAbort.setPreferredSize(new java.awt.Dimension(100, 28));
                lblLegendAbort.setSize(100, 28);
            }

            this.setTitle("ToolBox Legend");
            this.setSize(300, 380);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
