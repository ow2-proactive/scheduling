/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.gui.util;

import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;


public class MessagePanel extends javax.swing.JPanel {
    private javax.swing.JTextPane messageArea;
    private IC2DMessageLogger messageLogger;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MessagePanel(String title) {
        setLayout(new java.awt.BorderLayout());
        messageArea = new javax.swing.JTextPane();
        messageLogger = createMessageLogger(messageArea);
        setBorder(javax.swing.BorderFactory.createTitledBorder(title));
        javax.swing.JPanel topPanel = new javax.swing.JPanel(new java.awt.BorderLayout());

        // clear log button
        javax.swing.JButton clearLogButton = new javax.swing.JButton(
                "clear messages");
        clearLogButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    messageArea.setText("");
                }
            });
        topPanel.add(clearLogButton, java.awt.BorderLayout.WEST);
        add(topPanel, java.awt.BorderLayout.NORTH);
        javax.swing.JScrollPane pane = new javax.swing.JScrollPane(messageArea);
        add(pane, java.awt.BorderLayout.CENTER);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public IC2DMessageLogger getMessageLogger() {
        return messageLogger;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected IC2DMessageLogger createMessageLogger(
        javax.swing.JTextPane messageArea) {
        return new TextPaneMessageLogger(messageArea);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- INNER CLASSES -----------------------------------------------
    //
}
