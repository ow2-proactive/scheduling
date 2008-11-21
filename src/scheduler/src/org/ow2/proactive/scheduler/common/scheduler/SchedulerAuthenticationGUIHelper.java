/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.scheduler;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.ow2.proactive.scheduler.common.exception.SchedulerException;


/**
 * SchedulerAuthenticationHelper provides a graphical user interface to connect to the ProActiveScheduler.<br />
 * Use one of the two provided method to connect the scheduler as an administrator or as a user. <br />
 * This class will request username and password to the user using the graphical interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class SchedulerAuthenticationGUIHelper {

    private SchedulerAuthenticationGUIHelper() {
    }

    /**
     * Connect the scheduler and return the authentication interface, username and password of the current user.
     * 
     * @param schedulerURL the URL of the scheduler to connect
     * @return the authentication interface, username and password of the current user.
     * @throws LoginException
     * @throws SchedulerException
     */
    private static AuthResultContainer connect(String schedulerURL) throws LoginException, SchedulerException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(schedulerURL);
        AuthGraphicHelper helper = new AuthGraphicHelper();
        helper.setVisible(true);
        synchronized (helper) {
            try {
                helper.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String username = helper.jTextField_UserName.getText();
        String password = String.valueOf(helper.getJPasswordField_Password().getPassword());
        if (helper.cancel || username == null || username.length() == 0) {
            return null;
        } else {
            return new AuthResultContainer(auth, username, password);
        }
    }

    /**
     * This method will log a user to the scheduler by requesting his username and password from a 
     * graphical interface.<br/>
     * The URL of the scheduler to connect is required.
     * 
     * @param schedulerURL The URL of the scheduler to connect
     * @return The connection to the scheduler as a {@link UserSchedulerInterface} if logging successful.
     * 			If the username is empty or if the user cancel the authentication, this method will return null.
     * @throws LoginException If a problem occurs while logging the user.
     * @throws SchedulerException If a problem occurs at scheduler level.
     */
    public static UserSchedulerInterface logAsUser(String schedulerURL) throws LoginException,
            SchedulerException {
        AuthResultContainer auth = connect(schedulerURL);
        if (auth == null) {
            return null;
        } else {
            return auth.getAuth().logAsUser(auth.getUsername(), auth.getPassword());
        }
    }

    /**
     * This method will log an administrator to the scheduler by requesting his username and password from a 
     * graphical interface.<br/>
     * The URL of the scheduler to connect is required.
     * 
     * @param schedulerURL The URL of the scheduler to connect
     * @return The connection to the scheduler as a {@link AdminSchedulerInterface} if logging successful.
     * 			If the username is empty or if the user cancel the authentication, this method will return null.
     * @throws LoginException If a problem occurs while logging the administrator.
     * @throws SchedulerException If a problem occurs at scheduler level.
     */
    public static AdminSchedulerInterface logAsAdmin(String schedulerURL) throws LoginException,
            SchedulerException {
        AuthResultContainer auth = connect(schedulerURL);
        if (auth == null) {
            return null;
        } else {
            return auth.getAuth().logAsAdmin(auth.getUsername(), auth.getPassword());
        }
    }

    static class AuthResultContainer {
        private SchedulerAuthenticationInterface auth;
        private String username;
        private String password;

        /**
         * Create a new instance of AuthResultContainer.
         * 
         * @param auth
         * @param username
         * @param password
         */
        public AuthResultContainer(SchedulerAuthenticationInterface auth, String username, String password) {
            this.auth = auth;
            this.username = username;
            this.password = password;
        }

        /**
         * Get the auth.
         *
         * @return the auth.
         */
        public SchedulerAuthenticationInterface getAuth() {
            return auth;
        }

        /**
         * Get the username.
         *
         * @return the username.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Get the password.
         *
         * @return the password.
         */
        public String getPassword() {
            return password;
        }
    }

    static class AuthGraphicHelper extends JFrame implements ActionListener, WindowListener {

        private JPanel jContentPane = null;
        private JButton jButton_OK = null;
        private JButton jButton_Cancel = null;
        private JTextField jTextField_UserName = null;
        private JPasswordField jPasswordField_Password = null;
        private JLabel jLabel_UserName = null;
        private JLabel jLabel_Password = null;
        private JLabel jLabel_Info = null;
        private boolean cancel = false;

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        /**
         * This is the default constructor
         */
        private AuthGraphicHelper() {
            super();
            initialize();
        }

        /**
         * This method initializes this
         * 
         * @return void
         */
        private void initialize() {
            int thisH = 169;
            int thisW = 327;
            this.setSize(thisW, thisH);
            this.setContentPane(getJContentPane());
            this.setTitle("ProActive Scheduler Authentication");
            this.setResizable(false);
            this.setAlwaysOnTop(true);
            this.addWindowListener(this);
            int h = Toolkit.getDefaultToolkit().getScreenSize().height;
            int w = Toolkit.getDefaultToolkit().getScreenSize().width;
            this.setLocation((w - thisW) / 2, (h - thisH) / 2);
        }

        /**
         * This method initializes jButton_OK
         * 	
         * @return javax.swing.JButton	
         */
        private JButton getJButton_OK() {
            if (jButton_OK == null) {
                jButton_OK = new JButton();
                jButton_OK.setBounds(new Rectangle(35, 106, 100, 22));
                jButton_OK.setText("OK");
                jButton_OK.addActionListener(this);
                jButton_OK.setActionCommand("JButton_OK");
            }
            return jButton_OK;
        }

        /**
         * This method initializes jButton_Cancel	
         * 	
         * @return javax.swing.JButton	
         */
        private JButton getJButton_Cancel() {
            if (jButton_Cancel == null) {
                jButton_Cancel = new JButton();
                jButton_Cancel.setBounds(new Rectangle(180, 106, 100, 22));
                jButton_Cancel.setText("Cancel");
                jButton_Cancel.addActionListener(this);
                jButton_Cancel.setActionCommand("JButton_Cancel");
            }
            return jButton_Cancel;
        }

        /**
         * This method initializes jTextField_UserName	
         * 	
         * @return javax.swing.JTextField	
         */
        private JTextField getJTextField_UserName() {
            if (jTextField_UserName == null) {
                jTextField_UserName = new JTextField();
                jTextField_UserName.setBounds(new Rectangle(130, 40, 170, 22));
            }
            return jTextField_UserName;
        }

        /**
         * This method initializes jPasswordField_Password	
         * 	
         * @return javax.swing.JPasswordField	
         */
        private JPasswordField getJPasswordField_Password() {
            if (jPasswordField_Password == null) {
                jPasswordField_Password = new JPasswordField();
                jPasswordField_Password.setBounds(new Rectangle(130, 70, 170, 22));
            }
            return jPasswordField_Password;
        }

        /**
         * This method initializes jContentPane
         * 
         * @return javax.swing.JPanel
         */
        private JPanel getJContentPane() {
            if (jContentPane == null) {
                jLabel_Info = new JLabel();
                jLabel_Info.setBounds(new Rectangle(12, 10, 287, 22));
                jLabel_Info.setHorizontalAlignment(SwingConstants.RIGHT);
                jLabel_Info.setText("Identification required to connect the Scheduler :");
                jLabel_UserName = new JLabel();
                jLabel_UserName.setBounds(new Rectangle(12, 40, 90, 22));
                jLabel_UserName.setHorizontalAlignment(SwingConstants.RIGHT);
                jLabel_UserName.setText("Username : ");
                jLabel_Password = new JLabel();
                jLabel_Password.setBounds(new Rectangle(12, 70, 90, 22));
                jLabel_Password.setHorizontalAlignment(SwingConstants.RIGHT);
                jLabel_Password.setText("Password : ");
                jContentPane = new JPanel();
                jContentPane.setLayout(null);
                jContentPane.add(getJButton_OK(), null);
                jContentPane.add(getJButton_Cancel(), null);
                jContentPane.add(getJTextField_UserName(), null);
                jContentPane.add(getJPasswordField_Password(), null);
                jContentPane.add(jLabel_UserName, null);
                jContentPane.add(jLabel_Password, null);
                jContentPane.add(jLabel_Info, null);
            }
            return jContentPane;
        }

        /**
         * Terminate this authentication panel by closing it and unlock awaiting threads
         */
        private void terminate() {
            this.setVisible(false);
            synchronized (this) {
                this.notifyAll();
            }
            this.dispose();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            if (arg0.getActionCommand().equals(jButton_Cancel.getActionCommand())) {
                this.cancel = true;
            }
            terminate();
        }

        /**
         * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
         */
        public void windowClosing(WindowEvent e) {
            this.cancel = true;
            terminate();
        }

    }

}
