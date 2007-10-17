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

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * @author acontes
 *
 */
public class SecurityGui extends JFrame {
    private JMenuBar jJMenuBar = null;
    private JMenu jMenu = null;
    private JMenuItem Menu = null;
    private JPanel jPanel = null;
    private JList jList = null;
    private DefaultListModel listModel = new DefaultListModel();
    private JScrollPane jScrollPane = null;
    private RuleEditor ruleEditor = null;

    /**
     * This is the default constructor
     */
    public SecurityGui() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setContentPane(getJPanel());
        this.setVisible(true);
        this.setJMenuBar(getJJMenuBar());
        this.setSize(859, 599);
    }

    /**
     * This method initializes jJMenuBar
     *
     * @return javax.swing.JMenuBar
     */
    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getJMenu());
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJMenu() {
        if (jMenu == null) {
            jMenu = new JMenu();
            jMenu.setText("File");
            jMenu.add(getJMenuItem());
        }
        return jMenu;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem() {
        if (Menu == null) {
            Menu = new JMenuItem();
            Menu.setName("Menu");
            Menu.setText("Connect");
        }
        return Menu;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setBorder(javax.swing.BorderFactory.createLineBorder(
                    java.awt.Color.gray, 5));
            jPanel.add(getJScrollPane(), null);
            jPanel.add(getRuleEditor(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    private JList getJList() {
        if (jList == null) {
            jList = new JList(listModel);
        }
        return jList;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setBounds(20, 40, 144, 455);
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes ruleEditor
     *
     * @return org.objectweb.proactive.ext.security.gui.RuleEditor
     */
    private RuleEditor getRuleEditor() {
        if (ruleEditor == null) {
            ruleEditor = new RuleEditor();
            ruleEditor.setBounds(181, 38, 631, 455);
        }
        return ruleEditor;
    }

    public static void main(String[] args) {
        SecurityGui s = new SecurityGui();
    }
} //  @jve:decl-index=0:visual-constraint="25,19"
