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
package org.objectweb.proactive.ic2d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Legend extends JFrame {

  private static Legend uniqueInstance;

  public static Legend uniqueInstance() {
    return uniqueInstance == null ? uniqueInstance = new Legend() : uniqueInstance;
  }

  private Legend() {
    super("Legend");
    setSize(300, 300);
    {
       getContentPane().setLayout(new GridLayout(0, 2, 10, 10));

      {
        JComponent comp = new JPanel() {

          private int w = 100;
          private int h = 50;

          public void paintComponent(Graphics g) {
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;

            g.setColor(Color.white);
            g.fillOval(w / 3, h / 3, w * 3 / 6, h * 3 / 6);
            g.setColor(Color.blue);
          }
        };
        getContentPane().add(comp);
        getContentPane().add(new JLabel("Active Object"));
      }

      {
        JComponent comp = new JPanel() {

          private int w = 100;
          private int h = 50;

          public void paintComponent(Graphics g) {
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;
            g.setColor(new Color(200, 200, 255));
            g.fillOval(w / 3, h / 3, w * 3 / 6, h * 3 / 6);
          }
        };
        getContentPane().add(comp);
        getContentPane().add(new JLabel("Object waiting"));
      }

      {
        JComponent comp = new JPanel() {

          public void paintComponent(Graphics g) {
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;
            g.setColor(new Color(0xd0, 0xd0, 0xe0));
            g.fillRect(0, 0, w, h);
          }
        };
        getContentPane().add(comp);
        getContentPane().add(new JLabel("Rmi VM"));
      }
      
      {
        JComponent comp = new JPanel() {

          public void paintComponent(Graphics g) {
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;
            
            g.setColor(java.awt.Color.cyan);
            g.fillRect(0, 0, w, h);
          }
        };
        getContentPane().add(comp);
        getContentPane().add(new JLabel("Jini VM"));
      }

      {
        JComponent comp = new JPanel() {

          private int w = 100;
          private int h = 50;

          public void paintComponent(Graphics g) {
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;
            g.setColor(new Color(0xd0, 0xd0, 0xd0));
            g.fillRect(0, 0, w, h);
          }
        };
        getContentPane().add(comp);
        getContentPane().add(new JLabel("Standard Host"));
      }

      {
        JComponent comp = new JPanel() {

          private int w = 100;
          private int h = 50;

          public void paintComponent(Graphics g) {
            Dimension dim = getSize();
            int w = dim.width;
            int h = dim.height;
            g.setColor(new Color(0xff, 0xd0, 0xd0));
            g.fillRect(0, 0, w, h);
          }
        };
        getContentPane().add(comp);
        getContentPane().add(new JLabel("Globus Host"));
      }

      getContentPane().validate();
    }

    addWindowListener(new WindowAdapter() {
      public void windowClosing() {
        setVisible(false);
      }
    });
  }

  private void add(String name, JComponent comp) {
    JPanel pan = new JPanel(new FlowLayout());
    pan.add(new JLabel(name));
    pan.add(comp);
    getContentPane().add(pan);
  }

  public static void main(String[] argv) {
    Legend.uniqueInstance().show();
  }
}