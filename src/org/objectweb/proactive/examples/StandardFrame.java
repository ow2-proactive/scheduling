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
package org.objectweb.proactive.examples;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Inria
 * @author Lionel Mestre
 * @version 1.0
 */
public abstract class StandardFrame extends javax.swing.JFrame {

  //javax.swing.JPanel {

  protected final static int MESSAGE_ZONE_HEIGHT = 250;
  protected String name;
  protected int width;
  protected int height;
  protected java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("HH:mm:ss");
  protected transient javax.swing.JTextArea messageArea;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public StandardFrame(String name, int width, int height) {
    super(name);
    this.name = name;
    init(width, height);
  }

  public StandardFrame(String name) {
    super(name);
    this.name = name;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public void receiveMessage(final String s) {
    final String date = dateFormat.format(new java.util.Date());
    final String threadName = Thread.currentThread().getName();
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        messageArea.append(date);
        messageArea.append(" (");
        messageArea.append(threadName);
        messageArea.append(") => ");
        messageArea.append(s);
        messageArea.append("\n");
      }
    });
  }

  
  
  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected void init(int width, int height) {
    initFrame(name, width, height);
    setVisible(true);
    start();
  }
  
  
  protected abstract void start();


  protected abstract javax.swing.JPanel createRootPanel();


  protected javax.swing.JPanel createMessageZonePanel(final javax.swing.JTextArea area) {
    area.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
    javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
    javax.swing.border.TitledBorder border = new javax.swing.border.TitledBorder("Messages");
    panel.setBorder(border);
    javax.swing.JPanel topPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
    // clear log button
    javax.swing.JButton clearLogButton = new javax.swing.JButton("clear messages");
    clearLogButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        area.setText("");
      }
    });
    topPanel.add(clearLogButton, java.awt.BorderLayout.WEST);
    panel.add(topPanel, java.awt.BorderLayout.NORTH);
    javax.swing.JScrollPane pane = new javax.swing.JScrollPane(area);
    panel.add(pane, java.awt.BorderLayout.CENTER);
    return panel;
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
  private void initFrame(String name, int width, int height) {
    java.awt.Container c = getContentPane();
    c.setLayout(new java.awt.GridLayout(1, 1));

    // create topPanel
    javax.swing.JPanel topPanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 1));
    javax.swing.border.TitledBorder border = new javax.swing.border.TitledBorder(name);
    topPanel.setBorder(border);
    topPanel.add(createRootPanel());

    // create bottom Panel
    messageArea = new javax.swing.JTextArea();
    messageArea.setEditable(false);
    javax.swing.JPanel bottomPanel = createMessageZonePanel(messageArea);

    // create an vertical split Panel
    javax.swing.JSplitPane verticalSplitPane = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT);
    verticalSplitPane.setDividerLocation(height);
    verticalSplitPane.setTopComponent(topPanel);
    verticalSplitPane.setBottomComponent(bottomPanel);
    c.add(verticalSplitPane);

    setSize(width, height + MESSAGE_ZONE_HEIGHT);
    setLocation(30, 30);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        System.exit(0);
      }
    });
  }


}