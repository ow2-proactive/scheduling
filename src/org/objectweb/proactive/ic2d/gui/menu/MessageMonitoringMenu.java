/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
import org.objectweb.proactive.ic2d.event.MessageMonitoringListener;


public class MessageMonitoringMenu extends javax.swing.JMenu
    implements MessageMonitoringListener {
    protected MessageMonitoringController controller;
    protected javax.swing.JCheckBoxMenuItem monitorRequestSenderMenuItem;
    protected javax.swing.JCheckBoxMenuItem monitorRequestReceiverMenuItem;
    protected javax.swing.JCheckBoxMenuItem monitorReplySenderMenuItem;
    protected javax.swing.JCheckBoxMenuItem monitorReplyReceiverMenuItem;
    protected javax.swing.JCheckBoxMenuItem viewInEventListMenuItem;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MessageMonitoringMenu(String name,
        MessageMonitoringController aController) {
        super(name);
        this.controller = aController;

        //
        // Monitor All
        //
        javax.swing.JMenuItem monitorAllMenuItem = new javax.swing.JMenuItem(
                "Monitor All");
        monitorAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    controller.monitorAll(true);
                }
            });
        this.add(monitorAllMenuItem);

        javax.swing.JMenuItem monitorNoneMenuItem = new javax.swing.JMenuItem(
                "Monitor None");
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
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements MessageMonitoringListener -----------------------------------------------
    //
    public void monitoringRequestReceiverChanged(boolean b) {
        monitorRequestReceiverMenuItem.setState(b);
    }

    public void monitoringRequestSenderChanged(boolean b) {
        monitorRequestSenderMenuItem.setState(b);
    }

    public void monitoringReplyReceiverChanged(boolean b) {
        monitorReplyReceiverMenuItem.setState(b);
    }

    public void monitoringReplySenderChanged(boolean b) {
        monitorReplySenderMenuItem.setState(b);
    }

    public void viewingInEventListChanged(boolean b) {
        viewInEventListMenuItem.setState(b);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void addMenuOptions() {
        //
        // ViewEventList
        //
        viewInEventListMenuItem = new javax.swing.JCheckBoxMenuItem("View in textual list",
                controller.isViewedInEventList());
        viewInEventListMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    controller.viewInEventList(viewInEventListMenuItem.isSelected());
                }
            });
        this.add(viewInEventListMenuItem);
        this.addSeparator();

        //
        // MonitoringRequestSender
        //
        monitorRequestSenderMenuItem = new javax.swing.JCheckBoxMenuItem("Monitor RequestSender",
                controller.isMonitoringRequestSender());
        monitorRequestSenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    controller.monitorRequestSender(monitorRequestSenderMenuItem.isSelected());
                }
            });
        this.add(monitorRequestSenderMenuItem);

        //
        // MonitoringRequestReceiver
        //
        monitorRequestReceiverMenuItem = new javax.swing.JCheckBoxMenuItem("Monitor RequestReceiver",
                controller.isMonitoringRequestReceiver());
        monitorRequestReceiverMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    controller.monitorRequestReceiver(monitorRequestReceiverMenuItem.isSelected());
                }
            });
        this.add(monitorRequestReceiverMenuItem);

        //
        // MonitoringReplySender
        //
        monitorReplySenderMenuItem = new javax.swing.JCheckBoxMenuItem("Monitor ReplySender",
                controller.isMonitoringReplySender());
        monitorReplySenderMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    controller.monitorReplySender(monitorReplySenderMenuItem.isSelected());
                }
            });
        this.add(monitorReplySenderMenuItem);

        //
        // MonitoringReplyReceiver
        //
        monitorReplyReceiverMenuItem = new javax.swing.JCheckBoxMenuItem("Monitor ReplyReceiver",
                controller.isMonitoringReplyReceiver());
        monitorReplyReceiverMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    controller.monitorReplyReceiver(monitorReplyReceiverMenuItem.isSelected());
                }
            });
        this.add(monitorReplyReceiverMenuItem);
    }
}
