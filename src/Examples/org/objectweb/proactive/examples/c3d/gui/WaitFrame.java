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
package org.objectweb.proactive.examples.c3d.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;


/**
 * A Message frame to ask the user for patience.
 * The message can be customized, as well as the frame title.
 */
public class WaitFrame {
    private JFrame frame;

    /**
     * Create a JFrame asking people to be patient
     * @param title Title of window
     * @param text the First line to show
     * @param text2 the Second line to show
     */
    public WaitFrame(String title, String text, String text2) {
        // Create the frame
        frame = new JFrame(title);

        // Create the text component to add to the frame
        JLabel comp = new JLabel(text, SwingConstants.CENTER);
        JLabel comp2 = new JLabel(text2, SwingConstants.CENTER);

        // Add the component to the frame's content pane;
        // by default, the content pane has a border layout
        frame.getContentPane().add(comp, BorderLayout.CENTER);
        frame.getContentPane().add(comp2, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Show the frame
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Create a JFrame asking people to be patient
     * Default values for text is "Please wait... A new window will soon appear!"
     */
    public WaitFrame() {
        // call the other constructor
        this("Please wait...", "Please wait...", "A new window will soon appear!");
    }

    /** Clean up when no longer needed */
    public void destroy() {
        frame.setVisible(false);
        frame.dispose();
    }
}
