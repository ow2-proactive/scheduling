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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.matlab.client;

import org.ow2.proactive.scheduler.common.exception.SchedulerException;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/**
 * Class used to display a login window under Matlab for prompting login and passwords
 * @author The ProActive Team
 *
 */
public class LoginFrame extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	private JTextField username;
    private JPasswordField password;
    private AOMatlabEnvironment aome;
    private JButton login;
    private boolean loginSuccessful = false;

    /**
     * Creates a new LoginFrame
     */
    public LoginFrame(AOMatlabEnvironment aome) {
        this.aome = aome;

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

        login = new JButton("Login");

        cp.add(login, getConstraints(1, 2, 1, 1));

        setContentPane(cp);
    }

    public void start() {
        this.setVisible(true);
    }

    public JButton getLoginButton() {
        return login;
    }

    public boolean checkLogin() {
        String name = username.getText();
        String pwd = new String(password.getPassword());

        try {
            aome.login(name, pwd);
            dispose();
            return true;
        } catch (LoginException ex) {
            JOptionPane.showMessageDialog(LoginFrame.this, "Incorrect username/password combination.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
        } catch (SchedulerException ex2) {
            JOptionPane.showMessageDialog(LoginFrame.this, ex2.getMessage(), "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return false;
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
