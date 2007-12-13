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
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.objectweb.proactive.p2p.v2.monitoring.Dumper;


public class TestGUI extends JFrame implements ActionListener, ChangeListener {
    private JMenuBar jJMenuBar = null;
    private JMenu fileMenu = null;
    private JMenu editMenu = null;
    private JMenu helpMenu = null;
    private JMenuItem exitMenuItem = null;
    private JMenuItem aboutMenuItem = null;
    private JMenuItem cutMenuItem = null;
    private JMenuItem copyMenuItem = null;
    private JMenuItem pasteMenuItem = null;
    private JMenuItem saveMenuItem = null;
    private JPanel jPanel = null;
    private JTextField jTextField = null;
    private JungGUI jung;
    private JTabbedPane jTabbedPane = null;
    private JPanel jPanel2 = null;
    private JLabel jLabel = null;
    private JPanel jPanel3 = null;

    /**
     * This is the default constructor
     */
    public TestGUI() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJPanel());
        this.setJMenuBar(getJJMenuBar());
        this.setSize(500, 400);
        this.setTitle("Application");
    }

    /**
     * This method initializes jJMenuBar
     *
     * @return javax.swing.JMenuBar
     */
    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getEditMenu());
            jJMenuBar.add(getHelpMenu());
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setText("File");
            fileMenu.add(getSaveMenuItem());
            fileMenu.add(getExitMenuItem());
        }
        return fileMenu;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getEditMenu() {
        if (editMenu == null) {
            editMenu = new JMenu();
            editMenu.setText("Edit");
            editMenu.add(getCutMenuItem());
            editMenu.add(getCopyMenuItem());
            editMenu.add(getPasteMenuItem());
        }
        return editMenu;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setText("Help");
            helpMenu.add(getAboutMenuItem());
        }
        return helpMenu;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getExitMenuItem() {
        if (exitMenuItem == null) {
            exitMenuItem = new JMenuItem();
            exitMenuItem.setText("Exit");
            exitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return exitMenuItem;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getAboutMenuItem() {
        if (aboutMenuItem == null) {
            aboutMenuItem = new JMenuItem();
            aboutMenuItem.setText("About");
            aboutMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new JDialog(TestGUI.this, "About", true).show();
                }
            });
        }
        return aboutMenuItem;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getCutMenuItem() {
        if (cutMenuItem == null) {
            cutMenuItem = new JMenuItem();
            cutMenuItem.setText("Cut");
            cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, true));
        }
        return cutMenuItem;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getCopyMenuItem() {
        if (copyMenuItem == null) {
            copyMenuItem = new JMenuItem();
            copyMenuItem.setText("Copy");
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK, true));
        }
        return copyMenuItem;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getPasteMenuItem() {
        if (pasteMenuItem == null) {
            pasteMenuItem = new JMenuItem();
            pasteMenuItem.setText("Paste");
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK, true));
        }
        return pasteMenuItem;
    }

    /**
     * This method initializes jMenuItem
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getSaveMenuItem() {
        if (saveMenuItem == null) {
            saveMenuItem = new JMenuItem();
            saveMenuItem.setText("Save");
            saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
        }
        return saveMenuItem;
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
            jPanel.add(getJTabbedPane(), java.awt.BorderLayout.EAST);
        }
        return jPanel;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.setPreferredSize(new java.awt.Dimension(50, 20));
            jTextField.addActionListener(this);
        }
        return jTextField;
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setPreferredSize(new java.awt.Dimension(150, 7));
            jTabbedPane.addTab("Spring", null, getJPanel2(), null);
            jTabbedPane.addTab("KK", null, getJPanel3(), null);
            jTabbedPane.addChangeListener(this);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jLabel = new JLabel();
            jLabel.setText("Repulsion Range");
            jPanel2 = new JPanel();
            jPanel2.setPreferredSize(new java.awt.Dimension(120, 30));
            jPanel2.add(jLabel, null);
            jPanel2.add(getJTextField(), null);
        }
        return jPanel2;
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            jPanel3 = new JPanel();
        }
        return jPanel3;
    }

    /**
     * Launches this application
     */
    public static void main(String[] args) {
        TestGUI application = new TestGUI();
        JungGUI g = new JungGUI(null);
        application.setJungGUI(g);
        application.pack();
        application.setVisible(true);
        Dumper d = new Dumper();

        d.createGraphFromFile(args[0]);
        //application.show();
    }

    protected void setJungGUI(JungGUI j) {
        this.jung = j;
        this.add(this.jung.getPanel());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jTextField) {
            //System.out.println("actionPerformed() " + e); // TODO Auto-generated Event stub actionPerformed()
            //((TestGUI) e.getSource()).jung.setRepulsionRange(Integer.parseInt(e.))
            this.jung.setRepulsionRange(Integer.parseInt(jTextField.getText()));
        }
    }

    public void stateChanged(ChangeEvent arg0) {
        JTabbedPane pane = (JTabbedPane) arg0.getSource();

        // Get current tab
        int sel = pane.getSelectedIndex();
        System.out.println(sel);
    }
}
