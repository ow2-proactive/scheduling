/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyException;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;


/**
 * SchedulerAuthenticationHelper provides a graphical user interface to connect to the ProActiveScheduler.<br />
 * Use one of the two provided method to connect the scheduler as an administrator or as a user. <br />
 * This class will request username and password to the user using the graphical interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
public class SchedulerAuthenticationGUIHelper {

    /**
     * Create a new instance of SchedulerAuthenticationGUIHelper.
     */
    private SchedulerAuthenticationGUIHelper() {
    }

    /**
     * Connect the scheduler and return the authentication interface, username and password of the current user.
     * Return null if the authentication has been canceled (cancel button) or if username is not mentioned.
     *
     * @param schedulerURL the URL of the scheduler to connect
     * @return the authentication interface, username and password of the current user.
     * @throws LoginException If a problem occurs while logging the user.
     * @throws SchedulerException If a problem occurs at scheduler level.
     */
    private static AuthResultContainer connect(String schedulerURL) throws LoginException, SchedulerException {
        AuthGraphicHelper helper = new AuthGraphicHelper(schedulerURL);
        helper.setVisible(true);
        synchronized (helper) {
            try {
                helper.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //get selected URL
        schedulerURL = helper.getURL();
        //join the scheduler
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(schedulerURL);
        //get username and password
        String username = helper.getUserName();
        String password = String.valueOf(helper.getPassword());
        //send result
        if (helper.cancel || username == null || username.length() == 0) {
            return null;
        } else {
            return new AuthResultContainer(auth, username, password);
        }
    }

    /**
     * This method will log a client to the scheduler by requesting his URL, username and password from a
     * graphical interface.<br/>
     *
     * @param schedulerURL The default URL of the scheduler to connect
     * @return The connection to the scheduler as a {@link Scheduler} if logging successful.
     * 			If the username is empty or if the user cancel the authentication, this method will return null.
     * @throws LoginException If a problem occurs while logging the user.
     * @throws SchedulerException If a problem occurs at scheduler level.
     */
    public static Scheduler login(String schedulerURL) throws LoginException, SchedulerException {
        AuthResultContainer auth = connect(schedulerURL);
        if (auth == null) {
            return null;
        } else {
            SchedulerAuthenticationInterface schedAuth = auth.getAuth();
            Credentials cred = null;
            try {
                cred = Credentials.createCredentials(new CredData(CredData.parseLogin(auth.getUsername()),
                    CredData.parseDomain(auth.getUsername()), auth.getPassword()), schedAuth.getPublicKey());
            } catch (LoginException e) {
                throw new LoginException("Could not retrieve public key from Scheduler " + schedulerURL +
                    ", contact the administrator" + e);
            } catch (KeyException e) {
                throw new LoginException("Could not encrypt credentials " + e);
            }
            return schedAuth.login(cred);
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

    static class AuthGraphicHelper extends JFrame implements ActionListener, WindowListener, KeyListener {

    private static final long serialVersionUID = 60L;

        private static final String TMP_FILE_NAME = "AuthGrapHelpGUI.tmp";
        private static final File TMP_AUTH_FILE = new File(System.getProperty("java.io.tmpdir") +
            File.separator + TMP_FILE_NAME);
        private JPanel jContentPane = null;
        private JButton jButton_OK = null;
        private JButton jButton_Cancel = null;
        private JComboBox jComboBox_url = null;
        private JTextField jTextField_UserName = null;
        private JPasswordField jPasswordField_Password = null;
        private JLabel jLabel_url = null;
        private JLabel jLabel_UserName = null;
        private JLabel jLabel_Password = null;
        private JLabel jLabel_Info = null;
        private boolean cancel = false;
        private String defaultURL = "";
        private String defaultUserName = "";
        private Set<String> URLs = new HashSet<String>();

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

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        /**
         * This is the default constructor
         */
        private AuthGraphicHelper() {
            super();
            initialize();
        }

        /**
         * This is an other constructor that is build with default URL
         */
        private AuthGraphicHelper(String defaultURL) {
            super();
            this.defaultURL = defaultURL;
            initialize();
        }

        /**
         * This is an other constructor that is build with default URL and UserName
         */
        private AuthGraphicHelper(String defaultURL, String defaultUserName) {
            super();
            this.defaultURL = defaultURL;
            this.defaultUserName = defaultUserName;
            initialize();
        }

        /**
         * This method initializes this
         *
         * @return void
         */
        private void initialize() {
            loadURLs();
            int thisH = 200;
            int thisW = 450;
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
                jButton_OK.setBounds(new Rectangle(85, 136, 100, 22));
                jButton_OK.setText("OK");
                jButton_OK.addActionListener(this);
                jButton_OK.setActionCommand("JButton_OK");
                jButton_OK.addKeyListener(this);
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
                jButton_Cancel.setBounds(new Rectangle(260, 136, 100, 22));
                jButton_Cancel.setText("Cancel");
                jButton_Cancel.addActionListener(this);
                jButton_Cancel.setActionCommand("JButton_Cancel");
                jButton_Cancel.addKeyListener(this);
            }
            return jButton_Cancel;
        }

        /**
         * This method initializes JComboBox_url
         *
         * @return javax.swing.getJComboBox
         */
        private JComboBox getJComboBox_url() {
            if (jComboBox_url == null) {
                jComboBox_url = new JComboBox();
                jComboBox_url.setBounds(new Rectangle(130, 40, 300, 22));
                for (String s : URLs) {
                    jComboBox_url.addItem(s);
                }
                if (!"".equals(defaultURL)) {
                    jComboBox_url.setSelectedItem(defaultURL);
                }
                jComboBox_url.setEditable(true);
                jComboBox_url.addKeyListener(this);
            }
            return jComboBox_url;
        }

        /**
         * Get the URL as it has been set
         *
         * @return the URL as it has been set
         */
        public String getURL() {
            return (String) jComboBox_url.getSelectedItem();
        }

        /**
         * This method initializes jTextField_UserName
         *
         * @return javax.swing.JTextField
         */
        private JTextField getJTextField_UserName() {
            if (jTextField_UserName == null) {
                jTextField_UserName = new JTextField();
                jTextField_UserName.setBounds(new Rectangle(130, 70, 300, 22));
                jTextField_UserName.setText(defaultUserName);
                jTextField_UserName.addKeyListener(this);
            }
            return jTextField_UserName;
        }

        /**
         * Get the userName as it has been set
         *
         * @return the userName as it has been set
         */
        public String getUserName() {
            return jTextField_UserName.getText();
        }

        /**
         * This method initializes jPasswordField_Password
         *
         * @return javax.swing.JPasswordField
         */
        private JPasswordField getJPasswordField_Password() {
            if (jPasswordField_Password == null) {
                jPasswordField_Password = new JPasswordField();
                jPasswordField_Password.setBounds(new Rectangle(130, 100, 300, 22));
                jPasswordField_Password.addKeyListener(this);
            }
            return jPasswordField_Password;
        }

        /**
         * Get the password as it has been set
         *
         * @return the password as it has been set
         */
        public char[] getPassword() {
            return jPasswordField_Password.getPassword();
        }

        /**
         * This method initializes jContentPane
         *
         * @return javax.swing.JPanel
         */
        private JPanel getJContentPane() {
            if (jContentPane == null) {
                jLabel_Info = new JLabel();
                jLabel_Info.setBounds(new Rectangle(12, 10, 380, 22));
                jLabel_Info.setHorizontalAlignment(SwingConstants.LEFT);
                jLabel_Info.setText("Identification required to connect the Scheduler :");
                jLabel_url = new JLabel();
                jLabel_url.setBounds(new Rectangle(12, 40, 90, 22));
                jLabel_url.setHorizontalAlignment(SwingConstants.RIGHT);
                jLabel_url.setText("URL : ");
                jLabel_UserName = new JLabel();
                jLabel_UserName.setBounds(new Rectangle(12, 70, 90, 22));
                jLabel_UserName.setHorizontalAlignment(SwingConstants.RIGHT);
                jLabel_UserName.setText("Username : ");
                jLabel_Password = new JLabel();
                jLabel_Password.setBounds(new Rectangle(12, 100, 90, 22));
                jLabel_Password.setHorizontalAlignment(SwingConstants.RIGHT);
                jLabel_Password.setText("Password : ");
                jContentPane = new JPanel();
                jContentPane.setLayout(null);
                jContentPane.add(getJButton_OK(), null);
                jContentPane.add(getJButton_Cancel(), null);
                jContentPane.add(getJComboBox_url(), null);
                jContentPane.add(getJTextField_UserName(), null);
                jContentPane.add(getJPasswordField_Password(), null);
                jContentPane.add(jLabel_UserName, null);
                jContentPane.add(jLabel_Password, null);
                jContentPane.add(jLabel_Info, null);
                jContentPane.add(jLabel_url, null);
            }
            return jContentPane;
        }

        /**
         * Terminate this authentication panel by closing it and unlock awaiting threads
         */
        private void terminate() {
            saveURLs();
            this.setVisible(false);
            synchronized (this) {
                this.notifyAll();
            }
            this.dispose();
        }

        /**
         * Return true if the form has been canceled.
         *
         * @return true if the form has been canceled, false otherwise.
         */
        public boolean hasBeenCanceled() {
            return this.cancel;
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

        /**
         * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {
            if (e.getSource() == this.jButton_Cancel && e.getKeyCode() == KeyEvent.VK_ENTER) {
                this.cancel = true;
                terminate();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                terminate();
            }
        }

        private void saveURLs() {
            if (!"".equals((String) jComboBox_url.getSelectedItem())) {
                URLs.add((String) jComboBox_url.getSelectedItem());
            }
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileOutputStream(TMP_AUTH_FILE));
                for (String url : URLs) {
                    pw.println(url);
                }
            } catch (Exception e) {
                //not a big deal
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }

        private void loadURLs() {
            if (!"".equals(defaultURL)) {
                URLs.add(defaultURL);
            }
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(TMP_AUTH_FILE));
                String url;
                while ((url = br.readLine()) != null) {
                    if (!"".equals(url)) {
                        URLs.add(url);
                    }
                }
            } catch (Exception e) {
                //If file not found, it will be created after
                //if other, we cannot read historic -> not a big deal
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

    }

}
