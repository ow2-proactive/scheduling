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

public class StatelessMessageMonitoringMenu extends AbstractMessageMonitoringMenu {  

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public StatelessMessageMonitoringMenu(String name, MessageMonitoringController controller) {
    super(name, controller);
  }  
  
  
  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected void addMenuOptions() {
    //
    // MonitoringRequestSender
    //
    javax.swing.JMenuItem startMonitoringRequestSenderMenuItem = new javax.swing.JMenuItem("Start Monitoring RequestSender");
    startMonitoringRequestSenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorRequestSender(true);
        }
      });
    this.add(startMonitoringRequestSenderMenuItem);
    
    javax.swing.JMenuItem stopMonitoringRequestSenderMenuItem = new javax.swing.JMenuItem("Stop Monitoring RequestSender");
    stopMonitoringRequestSenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorRequestSender(false);
        }
      });
    this.add(stopMonitoringRequestSenderMenuItem);
    
    this.addSeparator();

    //
    // MonitoringRequestReceiver
    //
    javax.swing.JMenuItem startMonitoringRequestReceiverMenuItem = new javax.swing.JMenuItem("Start Monitoring RequestReceiver");
    startMonitoringRequestReceiverMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorRequestReceiver(true);
        }
      });
    this.add(startMonitoringRequestReceiverMenuItem);
    
    javax.swing.JMenuItem stopMonitoringRequestReceiverMenuItem = new javax.swing.JMenuItem("Stop Monitoring RequestReceiver");
    stopMonitoringRequestReceiverMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorRequestReceiver(false);
        }
      });
    this.add(stopMonitoringRequestReceiverMenuItem);
    
    this.addSeparator();

    //
    // MonitoringReplySender
    //
    javax.swing.JMenuItem startMonitoringReplySenderMenuItem = new javax.swing.JMenuItem("Start Monitoring ReplySender");
    startMonitoringReplySenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorReplySender(true);
        }
      });
    this.add(startMonitoringReplySenderMenuItem);
    
    javax.swing.JMenuItem stopMonitoringReplySenderMenuItem = new javax.swing.JMenuItem("Stop Monitoring ReplySender");
    stopMonitoringReplySenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorReplySender(false);
        }
      });
    this.add(stopMonitoringReplySenderMenuItem);
    
    this.addSeparator();

    //
    // MonitoringReplyReceiver
    //
    javax.swing.JMenuItem startMonitoringReplyReceiverMenuItem = new javax.swing.JMenuItem("Start Monitoring ReplyReceiver");
    startMonitoringReplyReceiverMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorReplyReceiver(true);
        }
      });
    this.add(startMonitoringReplyReceiverMenuItem);
    
    javax.swing.JMenuItem stopMonitoringReplyReceiverMenuItem = new javax.swing.JMenuItem("Stop Monitoring ReplyReceiver");
    stopMonitoringReplyReceiverMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent event) {
          controller.monitorReplyReceiver(false);
        }
      });
    this.add(stopMonitoringReplyReceiverMenuItem);
  }
}