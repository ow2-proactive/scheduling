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
package org.objectweb.proactive.ic2d.gui.menu;

import org.objectweb.proactive.ic2d.data.MessageMonitoringController;

public abstract class AbstractMessageMonitoringMenu extends javax.swing.JMenu {

  protected MessageMonitoringController controller;  

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public AbstractMessageMonitoringMenu(String name, MessageMonitoringController aController) {
    super(name);
    this.controller = aController;

    //
    // Monitor All
    //
    javax.swing.JMenuItem monitorAllMenuItem = new javax.swing.JMenuItem("Monitor All");
    monitorAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorAll(true);
        }
      });
    this.add(monitorAllMenuItem);
    
    javax.swing.JMenuItem monitorNoneMenuItem = new javax.swing.JMenuItem("Monitor None");
    monitorNoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorAll(false);
        }
      });
    this.add(monitorNoneMenuItem);
    
    this.addSeparator();

    addMenuOptions();
  }  
  
  
  
  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected abstract void addMenuOptions();
  
}