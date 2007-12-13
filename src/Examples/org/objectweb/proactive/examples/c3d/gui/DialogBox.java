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

import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A Dialog box, with a title, displaying two lines of text.
 * Nothing fancy at all. Used to display the ProActive "about" window
 */
public class DialogBox extends Dialog implements ActionListener, java.io.Serializable {
    public DialogBox(Frame parent, String frametitle, String line1, String line2) {
        super(parent, frametitle, true);

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gb);

        // line 1 
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        Label line1Label = new Label(line1, Label.CENTER);
        gb.setConstraints(line1Label, c);
        line1Label.setForeground(Color.blue);
        line1Label.setFont(new Font("arial", Font.BOLD | Font.ITALIC, 16));
        add(line1Label);

        // line 2
        c.gridy = 1;
        Label line2Label = new Label(line2, Label.CENTER);
        gb.setConstraints(line2Label, c);
        add(line2Label);

        //Button
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        Button okButton = new Button("OK");
        gb.setConstraints(okButton, c);
        okButton.addActionListener(this);
        add(okButton);

        setLocation(400, 200);
        pack();
        setVisible(true);
        toFront();
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }
}
