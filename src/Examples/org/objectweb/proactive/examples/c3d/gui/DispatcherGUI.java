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
package org.objectweb.proactive.examples.c3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.examples.c3d.DispatcherLogic;


/**
 * The GUI class, which when extended gives a nice graphical frontend.
 * The actionPerformed method needs to be overloaded, to handle the protected field events.
 */
public class DispatcherGUI implements ActionListener {
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

    /** The real logic-centric dispatcher Active Object */
    private DispatcherLogic c3dDispatcher;

    /** Creates a working GUI for C3D Dispatchers. See how we use a DispatcherLogic class?
     * This is made to avoid using the rendering capabilities of a C3DDispatcher, which
     * the GUI classes are not allowed to use! */
    public DispatcherGUI(String title, final DispatcherLogic c3dDispatcher) {
        this.mainFrame = new JFrame(title);
        this.mainFrame.setContentPane(createMainPanel());
        this.mainFrame.setJMenuBar(createMenuBar());

        this.mainFrame.pack();
        this.mainFrame.setVisible(true);
        this.c3dDispatcher = c3dDispatcher;
        mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    c3dDispatcher.exit();
                    trash();
                }
            });
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
        // The BorderLayout was used to have the log use as much space as possible.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(createInfoText(), BorderLayout.NORTH);
        mainPanel.add(createLogPanel("Application Log", new Dimension(200, 200)),
            BorderLayout.CENTER);

        JPanel extraPanel = new JPanel();
        extraPanel.setLayout(new GridLayout(2, 1));
        extraPanel.add(createUserListPanel());
        extraPanel.add(createEnginePanel());
        mainPanel.add(extraPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    /**
     * The top part, stating name of program & machine name
     */
    private JComponent createInfoText() {
        JPanel infoPanel = new JPanel(); // the box should have been enough, but the label overwrites the menu!!!
        Box box = Box.createVerticalBox();
        String localhostName = "";
        localhostName = URIBuilder.getHostNameorIP(ProActiveInet.getInstance()
                                                                .getInetAddress());
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
        // two parallel boxes, which contain used & available engines
        Box left = Box.createVerticalBox();
        Box right = Box.createVerticalBox();

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
        left.add(this.addEngineButton);

        this.removeEngineButton = new JButton("Remove engine");
        this.removeEngineButton.addActionListener(this);
        right.add(this.removeEngineButton);

        JPanel engineChoicePanel = new JPanel();
        engineChoicePanel.setLayout(new GridLayout(1, 2));
        engineChoicePanel.add(left);
        engineChoicePanel.add(right);
        return engineChoicePanel;
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

    /** Removes all engines from the GUI. */
    public void noEngines() {
        this.availableEngineListModel.removeAllElements();
        this.usedEngineListModel.removeAllElements();
    }

    /** handles all events generated by DispatcherGUI */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == clearLogMenuItem) {
            logArea.setText("");
        } else if (source == benchmarkMenuItem) {
            if (benchmarkMenuItem.getText().equals("Stop benchmark")) {
                benchmarkMenuItem.setText("Benchmark");
                c3dDispatcher.doBenchmarks(); // this call says "Stop the benchmark,
                                              // it is read by the doBenchmark method which checks the request queue for this...
            } else {
                benchmarkMenuItem.setText("Stop benchmark");
                c3dDispatcher.doBenchmarks();
            }
        } else if (source == exitMenuItem) {
            c3dDispatcher.exit();
            trash();
        } else if (source == addEngineButton) {
            Object[] sel = availableEngineList.getSelectedValues();
            for (int i = 0; i < sel.length; i++) {
                addUsedEngine((String) sel[i]);
                c3dDispatcher.turnOnEngine((String) sel[i]);
            }
        } else if (source == removeEngineButton) {
            Object[] sel = usedEngineList.getSelectedValues();
            for (int i = 0; i < sel.length; i++) {
                addAvailableEngine((String) sel[i]);
                c3dDispatcher.turnOffEngine((String) sel[i]);
            }
        }
    }
}
