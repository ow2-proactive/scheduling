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
package org.ow2.proactive.scheduler.ext.matsci.client.embedded;

import org.ow2.proactive.scheduler.ext.matsci.client.common.MatSciEnvironment;
import org.ow2.proactive.scheduler.ext.matsci.client.common.exception.PASchedulerException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;


/**
 * LoginFrame
 *
 * @author The ProActive Team
 */
public class LoginFrame<E extends MatSciEnvironment> extends JDialog {

    private JTextField username;
    private JPasswordField password;
    private JTextField key;
    private JFileChooser fc;

    private JButton fcb;

    private E aose;
    private JButton login;
    private boolean loginSuccessful = false;
    private boolean recordListener = false;
    private int nb_attempts = 0;
    public static final int MAX_NB_ATTEMPTS = 3;

    /**
     * Creates a new LoginFrame
     */
    public LoginFrame(E aose, boolean recordListener) {
        this.aose = aose;
        this.recordListener = recordListener;

        initComponents();

        pack();

        // Center the component
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension size = tk.getScreenSize();

        int x = (size.width / 2) - (getWidth() / 2);
        int y = (size.height / 2) - (getHeight() / 2);

        setLocation(x, y);
    }

    public void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel cp = new JPanel(new GridBagLayout());
        cp.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel usernameLabel = new JLabel("Username:");
        cp.add(usernameLabel, getConstraints(0, 0, 1, 1));

        username = new JTextField(15);
        cp.add(username, getConstraints(1, 0, 1, 1));

        JLabel passwordLabel = new JLabel("Password:");
        cp.add(passwordLabel, getConstraints(0, 1, 1, 1));

        password = new JPasswordField(15);
        cp.add(password, getConstraints(1, 1, 1, 1));

        JLabel keyLabel = new JLabel("Key:");
        cp.add(keyLabel, getConstraints(0, 2, 1, 1));

        key = new JTextField(15);
        cp.add(key, getConstraints(1, 2, 1, 1));

        fc = new JFileChooser();
        fc.setFileHidingEnabled(false);

        fcb = new JButton("...");
        cp.add(fcb, getConstraints(2, 2, 1, 1));

        fcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == fcb) {
                    int returnVal = fc.showOpenDialog(LoginFrame.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        key.setText(file.toString());
                    }

                }
            }
        });

        login = new JButton("Login");
        if (recordListener) {
            login.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    checkLogin();
                }
            });
        }

        cp.add(login, getConstraints(1, 3, 1, 1));

        setContentPane(cp);
    }

    public void start() {
        this.setVisible(true);
    }

    public JButton getLoginButton() {
        return login;
    }

    public boolean checkLogin() {
        nb_attempts++;
        String name = username.getText();
        String pwd = new String(password.getPassword());
        String kkey = key.getText();

        try {

            aose.login(name, pwd, kkey);

            dispose();
            return true;
        } catch (PASchedulerException ex) {
            switch (ex.getType()) {
                case KeyException:
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginFrame.this, "Incorrect Credential Key.",
                            "Login Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case LoginException:
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Incorrect username/password combination.", "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                case SchedulerException:
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginFrame.this, ex.getMessage(), "Login Error",
                            JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LoginFrame.this, e.getMessage(), "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (nb_attempts > MAX_NB_ATTEMPTS) {
            dispose();
        }
        return false;
    }

    public int getNbAttempts() {
        return nb_attempts;
    }

    private GridBagConstraints getConstraints(int x, int y, int width, int height) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.anchor = constraints.CENTER;
        constraints.fill = constraints.BOTH;

        return constraints;
    }
}
