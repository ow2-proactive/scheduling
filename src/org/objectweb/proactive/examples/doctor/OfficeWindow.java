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
package org.objectweb.proactive.examples.doctor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class OfficeWindow extends javax.swing.JFrame implements ActionListener {

  DisplayPanel pan;
  javax.swing.JButton bLegend,bExit;
  Legend legendDlg;


  public OfficeWindow() {
    java.awt.Container c = getContentPane();
    legendDlg = null;
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints constr = new GridBagConstraints();
    c.setLayout(lay);

    constr.gridwidth = GridBagConstraints.REMAINDER;
    constr.fill = GridBagConstraints.BOTH;
    constr.weightx = 0.0;
    pan = new DisplayPanel();
    lay.setConstraints(pan, constr);
    c.add(pan);

    constr.gridwidth = 1;
    constr.weightx = 1.0;
    bLegend = new javax.swing.JButton("Legend");
    lay.setConstraints(bLegend, constr);
    c.add(bLegend);
    bLegend.addActionListener(this);

    constr.gridwidth = GridBagConstraints.REMAINDER;
    bExit = new javax.swing.JButton("Exit");
    lay.setConstraints(bExit, constr);
    c.add(bExit);
    bExit.addActionListener(this);

    this.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }


  public void actionPerformed(ActionEvent e) {
    System.out.println("Action performed:\n" + e.toString());
    if (e.getSource() == bExit) {
      System.exit(0);
    }

    if (e.getSource() == bLegend) {
      if (legendDlg == null)
        legendDlg = new Legend(this, pan);
      if (legendDlg.isVisible())
        legendDlg.setVisible(false);
      else
        legendDlg.show();
    }

  }


  public DisplayPanel getDisplay() {
    return pan;
  }
}
