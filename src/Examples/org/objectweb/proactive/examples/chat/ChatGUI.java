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
package org.objectweb.proactive.examples.chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


/**
 * @author Laurent Baduel
 */
public class ChatGUI extends JFrame {
    public JTextField message = new JTextField(); //55
    public JTextField location = new JTextField(); //20
    public JTextArea text = new JTextArea(25, 55); //25,55
    public JTextArea list = new JTextArea(0, 4);
    private JButton quit = new JButton(new QuitAction());
    private JButton send = new JButton(new SendAction());
    private JButton migrate = new JButton(new MigrateAction());
    private Chat oa;

    /*
     * Builds the Graphic User Interface.
     */
    public ChatGUI(final Chat oa, String writerName) {
        super("Chat with ProActive");

        this.oa = oa;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Position of the window (centered)
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        setSize(screenWidth / 2, screenHeight / 2);
        setLocation(screenWidth / 4, screenHeight / 4);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        Box horizontalBoxMigration = Box.createHorizontalBox();
        horizontalBoxMigration.add(new JLabel("    migrate to "));
        horizontalBoxMigration.add(location);
        horizontalBoxMigration.add(migrate);

        Box verticalBoxText = Box.createVerticalBox();
        verticalBoxText.add(new JLabel(" --- History of messages --- "));
        // Add a scroll bar to the text area
        JScrollPane textScrollPanel = new JScrollPane(text);
        text.setEditable(false);
        verticalBoxText.add(textScrollPanel);
        verticalBoxText.setBorder(BorderFactory.createTitledBorder(""));

        Box verticalBoxMessage = Box.createVerticalBox();
        verticalBoxMessage.add(new JLabel("Message to send :"));
        verticalBoxMessage.add(message);
        verticalBoxMessage.setBorder(BorderFactory.createTitledBorder(
                writerName));

        Box horizontalBoxMessage = Box.createHorizontalBox();
        horizontalBoxMessage.add(verticalBoxMessage);
        horizontalBoxMessage.add(send);
        horizontalBoxMessage.add(quit);

        Box verticalBoxConnectedUsers = Box.createVerticalBox();
        verticalBoxConnectedUsers.add(new JLabel("Connected  users"));
        JScrollPane listScrollPanel = new JScrollPane(list);
        list.setEditable(false);
        verticalBoxConnectedUsers.add(listScrollPanel);
        verticalBoxConnectedUsers.setBorder(BorderFactory.createTitledBorder(""));

        panel.add(horizontalBoxMigration, BorderLayout.NORTH);
        panel.add(verticalBoxText, BorderLayout.CENTER);
        panel.add(horizontalBoxMessage, BorderLayout.SOUTH);
        panel.add(verticalBoxConnectedUsers, BorderLayout.EAST);

        getContentPane().add(panel);

        addWindowListener(new WindowAdapter() {
                // Focus on the edit message field
                @Override
                public void windowOpened(WindowEvent e) {
                    message.requestFocus();
                }

                // Pop a windows to confirm the close of the application
                @Override
                public void windowClosing(WindowEvent e) {
                    int reponse = JOptionPane.showConfirmDialog(ChatGUI.this,
                            "Are you sure you want to quit ?", "Quit the chat",
                            JOptionPane.YES_NO_OPTION);
                    if (reponse == JOptionPane.YES_OPTION) {
                        oa.disconnect();
                        ChatGUI.this.dispose();
                    }
                }
            });

        pack();
        setVisible(true);
    }

    /**
     * Action to leave the application (with confirmation)
     */
    private class QuitAction extends AbstractAction {
        public QuitAction() {
            putValue(Action.NAME, "Quit");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
            putValue(ACTION_COMMAND_KEY, "quit");
            putValue(SHORT_DESCRIPTION, "Quit");
            putValue(LONG_DESCRIPTION, "Quit the application");
            //			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            // Pop a windows to confirm the close of the application
            int reponse = JOptionPane.showConfirmDialog(ChatGUI.this,
                    "Are you sure you want to quit ?", "Quit the chat",
                    JOptionPane.YES_NO_OPTION);
            if (reponse == JOptionPane.YES_OPTION) {
                oa.disconnect();
                //				oa.disposeFrame();
                ChatGUI.this.dispose();
            }
        }
    }

    /**
     * Action to send a message
     */
    private class SendAction extends AbstractAction {
        public SendAction() {
            putValue(Action.NAME, "Send");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(ACTION_COMMAND_KEY, "send");
            putValue(SHORT_DESCRIPTION, "Send");
            putValue(LONG_DESCRIPTION, "Send a message");
            //			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if (message.getText().length() != 0) {
                oa.writeMessage(new Message(oa.getName(), message.getText()));
                message.setText("");
            }
            message.requestFocus();
        }
    }

    /**
     * Action to migrate to another node
     */
    private class MigrateAction extends AbstractAction {
        public MigrateAction() {
            putValue(Action.NAME, "Migrate !");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
            putValue(ACTION_COMMAND_KEY, "migrate");
            putValue(SHORT_DESCRIPTION, "Migrate");
            putValue(LONG_DESCRIPTION, "Migrate to a specified node");
            //			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if (location.getText().length() != 0) {
                oa.migrateTo(location.getText());
            } else {
                location.requestFocus();
            }
        }
    }

    public static void main(String[] args) {
        ChatGUI fenetre = new ChatGUI(null, "anonymous");
    }
}
