/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.readers;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


public class AppletReader extends org.objectweb.proactive.examples.StandardFrame {
    public ReadCanvas readerPanel;
    private ReaderDisplay display;
    private javax.swing.JComboBox policy;
    public boolean isActive = true; //By default when the application is launched, R/W activities start.

    public AppletReader(String name, int width, int height) {
        super(name, width, height);
    }

    public static void main(String[] arg) {
        ProActiveConfiguration.load();
        new AppletReader("Reader/Writer", 400, 300);
    }

    @Override
    public void start() {
        receiveMessage("Creating active objects");
        try {
            display = new ReaderDisplay(this);
            display = (ReaderDisplay) org.objectweb.proactive.api.PAActiveObject.turnActive(display);
            receiveMessage("Running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected javax.swing.JPanel createRootPanel() {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
        readerPanel = new ReadCanvas();
        panel.add(readerPanel, java.awt.BorderLayout.CENTER);

        // Controls
        javax.swing.JPanel pControls = new javax.swing.JPanel(new java.awt.BorderLayout());
        javax.swing.JPanel pPolicy = new javax.swing.JPanel(new java.awt.BorderLayout());

        pPolicy.add(new javax.swing.JLabel("Synchronization Policy"),
            java.awt.BorderLayout.WEST);
        policy = new javax.swing.JComboBox(new String[] {
                    "Even Policy", "Priority to Writers", "Priority to Readers"
                });
        policy.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    display.setPolicy(policy.getSelectedIndex());
                }
            });
        pPolicy.add(policy, java.awt.BorderLayout.CENTER);

        pControls.add(pPolicy, java.awt.BorderLayout.CENTER);
        panel.add(pControls, java.awt.BorderLayout.SOUTH);
        return panel;
    }
}
