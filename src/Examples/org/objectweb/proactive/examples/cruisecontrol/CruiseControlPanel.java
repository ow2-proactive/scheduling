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
package org.objectweb.proactive.examples.cruisecontrol;

import javax.swing.JButton;
import javax.swing.JPanel;


/** Screen of the controller,
 * displays the desired speed.
 * 4 buttons allow to turn on or off the controller
 * and to change the desired speed
 */
public class CruiseControlPanel extends JPanel {
    // Fields

    /** constants : represents the controller on */
    final static int ACTIVE = 1;
    final static int INACTIVE = 0;

    /** Turns off the controller */
    JButton CruiseOff;

    /** Turns on the controller */
    JButton CruiseOn;

    /** Increases the desired speed */
    JButton Inc1;

    /** Decreases the desired speed */
    JButton Dec1;

    /** the desired speed of the active controller */
    int desiredSpeed = 0;

    /** java.awt.Font used to display some components */
    java.awt.Font big = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18);

    /** java.awt.Font used to display some components */
    java.awt.Font small = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14);

    /** state of the car necessery for the painting of the car */
    int state = INACTIVE;

    /** Constructor which initializes the labels, the buttons, and places each components of the car */
    public CruiseControlPanel(final CruiseControlApplet parent,
        final Interface activeObject) {
        setLayout(null);
        //setBorder(new LineBorder(Color.black));
        CruiseOff = new JButton("Control Off");
        CruiseOn = new JButton("Control On");
        Inc1 = new JButton("++");
        Dec1 = new JButton("--");

        CruiseOn.setBounds(20, 310, 130, 30);
        CruiseOff.setBounds(160, 310, 130, 30);
        Inc1.setBounds(90, 345, 60, 30);
        Dec1.setBounds(160, 345, 60, 30);

        add(CruiseOn);
        add(CruiseOff);
        add(Inc1);
        add(Dec1);

        CruiseOn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    parent.controlOn();
                }
            });
        CruiseOff.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    parent.controlOff();
                }
            });
        Inc1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.accelerateCruise();
                }
            });
        Dec1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.decelerateCruise();
                }
            });
    }

    // Methods

    /** Displays the desired speed  and of the state of the controller */
    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);

        g.setColor(java.awt.Color.black);
        g.setFont(big);
        g.drawString("Control", 100, 40);
        g.setColor(java.awt.Color.black);

        g.setFont(small);
        g.drawString("Desired Speed ", 100, 120);
        g.drawRect(110, 140, 70, 20);
        // display the desired speed
        g.drawString(String.valueOf(desiredSpeed), 120, 155);

        // display the state off the controller
        if (state == INACTIVE) {
            g.setColor(java.awt.Color.black);
            g.drawString("Controller Off", 80, 230);
            g.setColor(java.awt.Color.red);
            g.fillOval(180, 210, 30, 30);
        } else {
            g.setColor(java.awt.Color.black);
            g.drawString("Controller On", 80, 230);
            g.setColor(java.awt.Color.green);
            g.fillOval(180, 210, 30, 30);
        }
    }

    ///////////////////////////////////////////////////////////////

    /** Activates the controller */
    public void controlOn() {
        if (this.state == INACTIVE) {
            this.state = ACTIVE;
            repaint();
        }
    }

    /** Deactivates the controller */
    public void controlOff() {
        if (this.state == ACTIVE) {
            this.state = INACTIVE;
            repaint();
        }
    }

    /** set the desired speed to the new desired speed */
    public void setDesiredSpeed(double m_speed) {
        desiredSpeed = (int) m_speed;
    }
}
