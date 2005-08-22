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
package org.objectweb.proactive.ext.security.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;


/*
 * This class exists solely to show you what menus look like.
 * It has no menu-related event handling.
 */
public class SecurityGui extends JFrame {
    JTextArea output;
    JScrollPane scrollPane;
    JTextField sender;
    JComboBox senderFromAuth;
    JComboBox senderFromInt;
    JComboBox senderFromConf;
    JComboBox senderToAuth;
    JComboBox senderToInt;
    JComboBox senderToConf;
    JTextField receiver;
    JComboBox receiverFromAuth;
    JComboBox receiverFromInt;
    JComboBox receiverFromConf;
    JComboBox receiverToAuth;
    JComboBox receiverToInt;
    JComboBox receiverToConf;

    public SecurityGui() {
        JMenuBar menuBar;
        JMenu menu;
        JMenu submenu;
        JMenuItem menuItem;
        JCheckBoxMenuItem cbMenuItem;
        JRadioButtonMenuItem rbMenuItem;

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        //Suggest that the L&F (rather than the system)
        //decorate all windows.  This must be invoked before
        //creating the JFrame.  Native look and feels will
        //ignore this hint.
        setDefaultLookAndFeelDecorated(false);

        //Add regular components to the window, using the default BorderLayout.
        Container contentPane = getContentPane();
        output = new JTextArea(5, 30);
        output.setEditable(false);
        scrollPane = new JScrollPane(output);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        //Create the menu bar.
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        //Build the first menu.
        menu = new JMenu("A Menu");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);

        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        menu.add(menuItem);

        menuItem = new JMenuItem("Both text and icon",
                new ImageIcon("images/middle.gif"));
        menuItem.setMnemonic(KeyEvent.VK_B);
        menu.add(menuItem);

        menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
        menuItem.setMnemonic(KeyEvent.VK_D);
        menu.add(menuItem);

        //a group of radio button menu items
        menu.addSeparator();

        ButtonGroup group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Another one");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //a group of check box menu items
        menu.addSeparator();
        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
        cbMenuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        menu.add(cbMenuItem);

        //a submenu
        menu.addSeparator();
        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
                ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);
        menu.add(submenu);

        //Build second menu in the menu bar.
        menu = new JMenu("Another Menu");
        menu.setMnemonic(KeyEvent.VK_N);
        menu.getAccessibleContext().setAccessibleDescription("This menu does nothing");
        menuBar.add(menu);

        // table
        String[] columnNames = {
                "First Name", "Last Name", "Sport", "# of Years", "Vegetarian"
            };

        Object[][] data = new Object[0][];
        JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Container contentPane = 
        GridBagLayout editorLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel editorPanel = new JPanel();
        editorPanel.setBorder(BorderFactory.createTitledBorder(
                "Move the Mouse to Move Duke"));
        editorPanel.setLayout(editorLayout);

        String[] attributesModeStrings = { "Required", "Optional", "Disallowed" };

        //		  Create the combo box, select item at index 4.
        //		  Indices start at 0, so 4 specifies the pig.
        JLabel name = new JLabel("Target");
        c.gridx = 0;
        c.gridy = 0;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        sender = new JTextField(10);
        sender.setActionCommand("Target");
        c.gridx = 1;
        c.gridy = 0;
        editorLayout.setConstraints(sender, c);
        editorPanel.add(sender);

        name = new JLabel("Authentication");
        c.gridx = 0;
        c.gridy = 5;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        senderToAuth = new JComboBox(attributesModeStrings);
        senderToAuth.setSelectedIndex(0);

        //attributeModeList.addActionListener(this);
        c.gridx = 1;
        c.gridy = 5;
        editorLayout.setConstraints(senderToAuth, c);
        editorPanel.add(senderToAuth);

        name = new JLabel("integrity");
        c.gridx = 0;
        c.gridy = 6;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        senderFromInt = new JComboBox(attributesModeStrings);
        senderFromInt.setSelectedIndex(0);
        c.gridx = 1;
        c.gridy = 6;
        editorLayout.setConstraints(senderFromInt, c);
        editorPanel.add(senderFromInt);

        name = new JLabel("confidentiality");
        c.gridx = 0;
        c.gridy = 7;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        senderFromConf = new JComboBox(attributesModeStrings);
        senderFromConf.setSelectedIndex(0);
        c.gridx = 1;
        c.gridy = 7;
        editorLayout.setConstraints(senderFromConf, c);
        editorPanel.add(senderFromConf);

        name = new JLabel(" >>>> ");
        c.gridx = 2;
        c.gridy = 5;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        name = new JLabel("authentification");
        c.gridx = 3;
        c.gridy = 5;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiverFromAuth = new JComboBox(attributesModeStrings);
        receiverFromAuth.setSelectedIndex(0);
        c.gridx = 4;
        c.gridy = 5;
        editorLayout.setConstraints(receiverFromAuth, c);
        editorPanel.add(receiverFromAuth);
        name = new JLabel("integrity");
        c.gridx = 3;
        c.gridy = 6;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiverFromInt = new JComboBox(attributesModeStrings);
        receiverFromInt.setSelectedIndex(0);
        c.gridx = 4;
        c.gridy = 6;
        editorLayout.setConstraints(receiverFromInt, c);
        editorPanel.add(receiverFromInt);

        name = new JLabel("confidentiality");
        c.gridx = 3;
        c.gridy = 7;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiverFromInt = new JComboBox(attributesModeStrings);
        receiverFromInt.setSelectedIndex(0);
        c.gridx = 4;
        c.gridy = 7;
        editorLayout.setConstraints(receiverFromInt, c);
        editorPanel.add(receiverFromInt);

        name = new JLabel("Authentication");
        c.gridx = 0;
        c.gridy = 1;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        senderFromAuth = new JComboBox(attributesModeStrings);
        senderFromAuth.setSelectedIndex(0);

        //attributeModeList.addActionListener(this);
        c.gridx = 1;
        c.gridy = 1;
        editorLayout.setConstraints(senderFromAuth, c);
        editorPanel.add(senderFromAuth);

        name = new JLabel("integrity");
        c.gridx = 0;
        c.gridy = 2;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        senderFromInt = new JComboBox(attributesModeStrings);
        senderFromInt.setSelectedIndex(0);
        c.gridx = 1;
        c.gridy = 2;
        editorLayout.setConstraints(senderFromInt, c);
        editorPanel.add(senderFromInt);

        name = new JLabel("confidentiality");
        c.gridx = 0;
        c.gridy = 3;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        senderFromConf = new JComboBox(attributesModeStrings);
        senderFromConf.setSelectedIndex(0);
        c.gridx = 1;
        c.gridy = 3;
        editorLayout.setConstraints(senderFromConf, c);
        editorPanel.add(senderFromConf);

        name = new JLabel(" <<<< ");
        c.gridx = 2;
        c.gridy = 2;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        name = new JLabel("Target");
        c.gridx = 3;
        c.gridy = 0;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiver = new JTextField(10);
        receiver.setActionCommand("Target");
        c.gridx = 4;
        c.gridy = 0;
        editorLayout.setConstraints(receiver, c);
        editorPanel.add(receiver);

        name = new JLabel("authentification");
        c.gridx = 3;
        c.gridy = 1;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiverFromAuth = new JComboBox(attributesModeStrings);
        receiverFromAuth.setSelectedIndex(0);
        c.gridx = 4;
        c.gridy = 1;
        editorLayout.setConstraints(receiverFromAuth, c);
        editorPanel.add(receiverFromAuth);
        name = new JLabel("integrity");
        c.gridx = 3;
        c.gridy = 2;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiverFromInt = new JComboBox(attributesModeStrings);
        receiverFromInt.setSelectedIndex(0);
        c.gridx = 4;
        c.gridy = 2;
        editorLayout.setConstraints(receiverFromInt, c);
        editorPanel.add(receiverFromInt);

        name = new JLabel("confidentiality");
        c.gridx = 3;
        c.gridy = 3;
        editorLayout.setConstraints(name, c);
        editorPanel.add(name);

        receiverFromConf = new JComboBox(attributesModeStrings);
        receiverFromConf.setSelectedIndex(0);
        c.gridx = 4;
        c.gridy = 3;
        editorLayout.setConstraints(receiverFromConf, c);
        editorPanel.add(receiverFromConf);

        JButton button = new JButton("Add");
        c.gridx = 5;
        c.gridy = 4;
        editorLayout.setConstraints(button, c);
        editorPanel.add(button);

        // editor Scroll Pane
        JScrollPane editScrollPane = new JScrollPane(editorPanel);

        // textField.addActionListener(this);
        //Create the scroll pane and add the table to it. 
        JScrollPane tableScrollPane = new JScrollPane(table);

        //	   Create a split pane with the two scroll panes in it.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                editScrollPane, tableScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

        //	 Add the scroll pane to this window.
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);

        SecurityGui window = new SecurityGui();

        window.setTitle("MenuLookDemo");
        window.setSize(450, 260);
        window.setVisible(true);
    }
}
