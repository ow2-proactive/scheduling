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
package org.objectweb.proactive.extensions.scilab.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;


public class WelcomeDialog extends JDialog {

    /**
         *
         */
    private JPanel panelLogo = null;
    private Image logo;

    public WelcomeDialog() {
        super();

        logo = Toolkit.getDefaultToolkit()
                      .getImage(getClass().getResource("img/logo.jpg"));
        panelLogo = new JPanel() {

                    /**
                         *
                         */
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        g.drawImage(logo, 0, 0, this);
                    }
                };

        this.setSize(510, 300);
        this.setResizable(false);
        this.setTitle("Welcome");
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(false);
        this.setUndecorated(true);
        this.getContentPane().add(this.panelLogo);
        this.center();
    }

    public void center() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension f = this.getSize();
        int x = (d.width - f.width) / 2;
        int y = (d.height - f.height) / 2;
        this.setBounds(x, y, f.width, f.height);
    }
}
