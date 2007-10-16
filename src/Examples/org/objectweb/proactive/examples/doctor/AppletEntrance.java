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
package org.objectweb.proactive.examples.doctor;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * <p>
 * This application simulates the behavior of an hospital. Sick
 * patients are waiting for a doctor to heal them, while doctors go
 * from patient to patient. This application illustrates
 * resource-sharing using ProActive
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class AppletEntrance extends org.objectweb.proactive.examples.StandardFrame {
    public AppletEntrance(String name, int width, int height) {
        super(name, width, height);
    }

    public static void main(String[] arg) {
        ProActiveConfiguration.load();
        new AppletEntrance("The salishan problems", 600, 300);
    }

    @Override
    public void start() {
        receiveMessage("Please wait while initializing remote objects");
        try {
            Office off = (Office) org.objectweb.proactive.api.ProActiveObject.newActive(Office.class.getName(),
                    new Object[] { new Integer(0) });
            Receptionnist recept = (Receptionnist) org.objectweb.proactive.api.ProActiveObject.newActive(Receptionnist.class.getName(),
                    new Object[] { off });
            receiveMessage("The doctors' office is open!");
            off.init(off, recept);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected javax.swing.JPanel createRootPanel() {
        javax.swing.JPanel rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        rootPanel.setBackground(java.awt.Color.white);
        rootPanel.setForeground(java.awt.Color.red);
        rootPanel.add(new javax.swing.JLabel(
                "The salishan problems : Problem 3 - The Doctor's Office"),
            java.awt.BorderLayout.NORTH);
        //javax.swing.JPanel officePanel=new javax.swing.JPanel(new java.awt.GridLayout(2,1));
        return rootPanel;
    }
}
