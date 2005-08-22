/*
 * Created on Jun 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.process.unicore;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * @author mleyton
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UnicorePasswordGUI extends JFrame implements ActionListener {
    String keypassword = "";
    JTextField password;

    public synchronized String getKeyPassword() {
        while (keypassword.length() <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        notifyAll();

        return keypassword;
    }

    public synchronized void setKeyPassword(String keypassword) {
        this.keypassword = keypassword;
        notifyAll();
    }

    public UnicorePasswordGUI() {
        super("ProActive Unicore Client");

        JButton ok = new JButton("OK");
        ok.addActionListener(this);

        password = new JPasswordField(12);
        //password.setEchoChar('*');
        Container panel = this.getContentPane();
        panel.setLayout(new BorderLayout());

        panel.add(new JLabel("Input Unicore Keystore Password"),
            BorderLayout.NORTH);
        panel.add(password, BorderLayout.CENTER);
        panel.add(ok, BorderLayout.SOUTH);

        this.pack();
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        setKeyPassword(password.getText());
        this.dispose();
    }

    public static void main(String[] args) {
        UnicorePasswordGUI upGUI = new UnicorePasswordGUI();
        System.out.println("password:" + upGUI.getKeyPassword());
    }
}
