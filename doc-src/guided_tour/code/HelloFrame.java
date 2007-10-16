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
package org.objectweb.proactive.examples.hello;

/** This class allows the creation of a graphical window
 * with a text field */
public class HelloFrame extends javax.swing.JFrame {
    private javax.swing.JLabel jLabel1;

    /** Creates new form HelloFrame */
    public HelloFrame(String text) {
        initComponents();
        setText(text);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     *        It will perform the initialization of the frame */
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    exitForm(evt);
                }
            });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);
    }

    /** Kill the frame */
    private void exitForm(java.awt.event.WindowEvent evt) {
        //        System.exit(0); would kill the VM !
        dispose(); // this way, the active object agentFrameController stays alive
    }

    /** Sets the text of the label inside the frame */
    private void setText(String text) {
        jLabel1.setText(text);
    }
}
