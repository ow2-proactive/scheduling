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
package org.objectweb.proactive.ic2d.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NewGlobusHostDialog extends JDialog implements ActionListener {

  public boolean success = true;
  public String host;
  public int port;
  private JButton btOk;
  private JButton btCancel;
  private JTextField tfHost;
  private JTextField tfPort;


  public void actionPerformed(ActionEvent e) {
    if (tfHost.getText().length() != 0) {
      host = tfHost.getText();
      // System.out.println("PORT :::::::::::::::::::::::::::::::::::::::::" + tfPort.getText());
      if (tfPort.getText() == null)
        port = 754;
      else
        port = Integer.parseInt(tfPort.getText());
    }
    closeDialog(true);
  }


  /** 
   * Creates new form NewGlobusHostDialog
   *
   * @param parent The parent frame if any
   * @param modal Lets you decide if you want this dialog to be modal or not
   */
  public NewGlobusHostDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
    pack();
  }


  /**
   * Describe 'initComponents' method here.
   *
   * @param nil a value of type ''
   */
  private void initComponents() {
    setTitle("GLOBUS host to ic2d");
    setName("Acquire GLOBUS Host Dialog");

    addWindowListener(new java.awt.event.WindowAdapter() {

      public void windowClosing(java.awt.event.WindowEvent evt) {
        closeDialog(false);
      }
    });

    Container content = getContentPane();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    content.setLayout(gb);

    // The hostname
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 10, 5, 10);
    JLabel label = new JLabel("Url");
    gb.setConstraints(label, c);
    content.add(label);
    
    // Textfields
    // 	tfHost=new JTextField("localhost");
    try {
      tfHost = new JTextField(InetAddress.getLocalHost().getHostName());
    } catch (Exception e) {
      tfHost = new JTextField("localhost");
    }
    tfHost.addActionListener(this);
    c.gridx = 1;
    c.fill = c.HORIZONTAL;
    gb.setConstraints(tfHost, c);
    content.add(tfHost);

    tfPort = new JTextField("754");
    tfPort.addActionListener(this);
    c.gridx = 2;
    c.fill = c.HORIZONTAL;
    gb.setConstraints(tfPort, c);
    content.add(tfPort);


    // The buttons
    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    // Ok button 
    btOk = new JButton("OK");
    btOk.addMouseListener(new MouseAdapter() {

      public void mouseReleased(MouseEvent evt) {
        if (tfHost.getText().length() != 0) {
          host = tfHost.getText();
        }
        closeDialog(true);
      }
    });
    p.add(btOk, BorderLayout.EAST);
    
    // Cancel button
    btCancel = new JButton("Cancel");
    btCancel.addMouseListener(new MouseAdapter() {

      public void mouseReleased(MouseEvent e) {
        closeDialog(false);
      }
    });
    p.add(btCancel, BorderLayout.WEST);

    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 2;
    gb.setConstraints(p, c);
    content.add(p);
    p = null;
  }


  private void closeDialog(boolean val) {
    success = val;
    setVisible(false);
    dispose();
  }
}
