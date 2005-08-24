/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.c3d.gui;

import org.objectweb.proactive.core.util.UrlBuilder;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;


/**
 * The GUI class, which when extended gives a nice graphical frontend.
 * The actionPerformed method needs to be overloaded, to handle the protected field events.
 */
public abstract class DispatcherGUI implements ActionListener {
    protected JFrame mainFrame;
    protected JMenuItem exitMenuItem;
    protected JMenuItem benchmarkMenuItem;
    protected JMenuItem clearLogMenuItem;
    protected JTextArea logArea;
    protected DefaultListModel userListModel;
    private DefaultListModel availableEngineListModel;
    private DefaultListModel usedEngineListModel;
    protected JList availableEngineList;
    protected JList usedEngineList;
    protected JButton addEngineButton;
    protected JButton removeEngineButton;

    /** You might wish to add a window listener on the closing event... */
    public DispatcherGUI(String title) {
        this.mainFrame = new JFrame(title);
        this.mainFrame.setContentPane(createMainPanel());
        this.mainFrame.setJMenuBar(createMenuBar());

        this.mainFrame.pack();
        this.mainFrame.setVisible(true);
    }

    /**
     * Creates the menu which is used in the mainFrame
     */
    private JMenuBar createMenuBar() {
        //First, the menu items 
        clearLogMenuItem = new JMenuItem("Clear Log", KeyEvent.VK_C);
        exitMenuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
        benchmarkMenuItem = new JMenuItem("Benchmark", KeyEvent.VK_B);

        // make them responsive
        benchmarkMenuItem.addActionListener(this);
        clearLogMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(this);

        // The menu in the menu Bar.
        JMenu menu = new JMenu("Menu");
        menu.setMnemonic(KeyEvent.VK_M);
        menu.add(clearLogMenuItem);
        menu.add(benchmarkMenuItem);
        menu.add(new JSeparator());
        menu.add(exitMenuItem);

        // Create the wrapping menuBar object
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
    }

    /**
     * Contains all the components, except the menuBar.
     */
    private JComponent createMainPanel() {
        Box mainPanel = Box.createVerticalBox();
        mainPanel.add(createInfoText());
        mainPanel.add(createLogPanel("Application Log", new Dimension(200, 200)));
        mainPanel.add(createUserListPanel());
        mainPanel.add(createEnginePanel());
        return mainPanel;
    }

    /**
     * The top part, stating name of program & machine name
     */
    private JComponent createInfoText() {
        JPanel infoPanel = new JPanel(); // TODO : the box should be enough, but this overwrites the menu!!!
        Box box = Box.createVerticalBox();
        String localhostName = "";
        try {
            localhostName = UrlBuilder.getHostNameorIP(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            localhostName = "unknown host name!";
        }
        Label machine = new Label("on " + localhostName + " (" +
                System.getProperty("os.name") + ")", Label.CENTER);
        Label header = new Label("C3D Dispatcher", Label.CENTER);
        header.setFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 18));

        box.add(header);
        box.add(machine);
        infoPanel.add(box);
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                header.getSize().height + machine.getSize().height));
        return infoPanel;
    }

    private JComponent createUserListPanel() {
        this.userListModel = new DefaultListModel();
        JScrollPane panel = createListPanel(new JList(this.userListModel));
        panel.setBorder(new TitledBorder("List of users"));
        return panel;
    }

    /**
     * The list of users connected.
     */
    private JScrollPane createListPanel(JList jlist) {
        JScrollPane scroll = new JScrollPane(jlist);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        return scroll;
    }

    /**
     * A GridBag containing selectable engines, and buttons to add/remove them.
     */
    private JComponent createEnginePanel() {
        Box panel = Box.createHorizontalBox();
        Box left = Box.createVerticalBox();
        Box right = Box.createVerticalBox();
        panel.add(left);
        panel.add(right);

        left.add(new JLabel("Available engines"));
        right.add(new JLabel("Engines used"));

        this.availableEngineListModel = new DefaultListModel();
        this.availableEngineList = new JList(this.availableEngineListModel);
        JScrollPane availPanel = createListPanel(this.availableEngineList);
        left.add(availPanel);

        this.usedEngineListModel = new DefaultListModel();
        this.usedEngineList = new JList(this.usedEngineListModel);
        JComponent usedPanel = createListPanel(this.usedEngineList);
        right.add(usedPanel);

        this.addEngineButton = new JButton("Add engine");
        this.addEngineButton.addActionListener(this);
        this.removeEngineButton = new JButton("Remove engine");
        this.removeEngineButton.addActionListener(this);
        left.add(this.addEngineButton);
        right.add(this.removeEngineButton);
        return panel;
    }

    private JComponent createLogPanel(String title, Dimension prefSize) {
        this.logArea = new JTextArea();
        this.logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(this.logArea);
        scroll.setBorder(new TitledBorder(title));
        scroll.setPreferredSize(prefSize);
        return scroll;
    }

    /**
     * Destroy the graphical window
     */
    public void trash() {
        this.mainFrame.setVisible(false);
        this.mainFrame.dispose();
    }

    /**
     * Should implement the response to the events generated by the
     * protected fields of the GUI class.
     */
    public abstract void actionPerformed(ActionEvent e);

    /**
     * Add a user to the list of users.
     */
    public void addUser(String userName) {
        this.userListModel.addElement(userName);
    }

    /**
     * Remove a user from the list of users.
     */
    public void removeUser(String userName) {
        this.userListModel.removeElement(userName);
    }

    /**
     * set an engine as available, removing it if needed from the used list
     */
    public void addAvailableEngine(String engineName) {
        this.availableEngineListModel.addElement(engineName);
        this.usedEngineListModel.removeElement(engineName);
    }

    /**
     * set an engine as used, removing it if needed from the available list
     */
    public void addUsedEngine(String engineName) {
        this.usedEngineListModel.addElement(engineName);
        this.availableEngineListModel.removeElement(engineName);
    }

    /**
     * Used for benchmarking: put n  engines as "used"
     * @return the names of all the engines used once set
     */
    public String[] setEngines(int nbEnginesToUse) {
        // remove all engines.
        while (this.usedEngineListModel.size() > nbEnginesToUse) {
            String noMoreUsed = (String) this.usedEngineListModel.get(0);
            addAvailableEngine(noMoreUsed);
        }

        while ((this.usedEngineListModel.size() < nbEnginesToUse) &&
                !this.availableEngineListModel.isEmpty()) {
            String nowUsed = (String) this.availableEngineListModel.get(0);
            addUsedEngine(nowUsed);
        }

        String[] engineNames = new String[nbEnginesToUse];
        for (int i = 0; i < this.usedEngineListModel.size(); i++) {
            engineNames[i] = (String) this.usedEngineListModel.get(i);
        }

        return engineNames;
    }

    /** Append the given log to the GUI log area */
    public void log(String log) {
        logArea.append(log);
        //logArea.setCaretPosition(logArea.getText().length());    // move scrollbar down if needed
    }

    public void clearLog() {
        logArea.setText("");
    }
}
