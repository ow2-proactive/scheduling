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
package org.objectweb.proactive.examples.binarytree;

/**
 * <p>
 * The binary tree is an a recursive data structure. A tree is
 * composed of a root node, and each node has two potential child
 * nodes. Here, each node is an active object, allowing large data
 * structures to be distributed aver the network.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class TreeApplet extends org.objectweb.proactive.examples.StandardFrame {

  private javax.swing.JButton bAdd;
  private javax.swing.JButton bSearch;
  private javax.swing.JButton bDump;
  private javax.swing.JTextField tKey;
  private javax.swing.JTextField tValue;
  private TreeDisplay display;
  private javax.swing.JTextArea treeArea;


  public TreeApplet(String name, int width, int height) {
    super(name, width, height);
    // Create the DisplayManager
    try {
      display = new TreeDisplay(this);
      display = (TreeDisplay)org.objectweb.proactive.ProActive.turnActive(display);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String arg[]) {
    new TreeApplet("Binary Tree", 600, 300);
  }


  public void displayTree(final String s) {
    if (treeArea != null) {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {

        public void run() {
          treeArea.setText(s);
        }
      });
    }
  }

  protected void start() {
  }

  protected javax.swing.JPanel createRootPanel() {
    javax.swing.JPanel rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());

    javax.swing.JPanel westPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
    javax.swing.JPanel pCmd = new javax.swing.JPanel(new java.awt.GridLayout(3, 1));

    // Text Area
    javax.swing.JPanel panel = new javax.swing.JPanel();
    tKey = new javax.swing.JTextField("", 15);
    panel.add(new javax.swing.JLabel("Key"));
    panel.add(tKey);
    pCmd.add(panel);

    panel = new javax.swing.JPanel();
    tValue = new javax.swing.JTextField("", 15);
    panel.add(new javax.swing.JLabel("Value"));
    panel.add(tValue);
    pCmd.add(panel);

    // Button placement
    panel = new javax.swing.JPanel();
    javax.swing.JButton bAdd = new javax.swing.JButton("Add");
    bAdd.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(java.awt.event.ActionEvent e) {
        String key = tKey.getText();
        String value = tValue.getText();
        if (key == null || value == null) {
          receiveMessage("You must specify a Key/Value couple!!!");
        } else {
          display.add(key, value);
        }
      }
    });
    panel.add(bAdd);

    javax.swing.JButton bSearch = new javax.swing.JButton("Search");
    bSearch.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(java.awt.event.ActionEvent e) {
        String key = tKey.getText();
        if (key == null)
          return;
        tValue.setText(display.search(key));
      }
    });
    panel.add(bSearch);

    javax.swing.JButton bDump = new javax.swing.JButton("Dump tree");
    bDump.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(java.awt.event.ActionEvent e) {
        display.display();
      }
    });
    panel.add(bDump);

    pCmd.add(panel);
    westPanel.add(pCmd, java.awt.BorderLayout.NORTH);
    rootPanel.add(westPanel, java.awt.BorderLayout.WEST);

    //  tree area
    treeArea = new javax.swing.JTextArea(17, 25);
    treeArea.setEditable(false);
    javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(treeArea);
    rootPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

    return rootPanel;
  }
}
