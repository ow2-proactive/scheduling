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

import javax.swing.SwingConstants;


/** This class displays the speed, the acceleration of the car
 * and holds the buttons for the interaction between the driver and the car
 */
public class CarPanel extends javax.swing.JPanel {
    // Fields
    static final int INACTIVE = 0;
    static final int ACTIVE = 1;

    /** the current speed of the car */
    double speed;

    /** the distance to display */
    double distance = 0;

    /** the state of the car necessery for painting */
    int state = INACTIVE;

    /** The label accelerate the car */
    javax.swing.JLabel Accelerate;

    /** Acceleration bar */
    javax.swing.JProgressBar accBar;

    /** Road incline */
    javax.swing.JProgressBar roadBar;

    /** Label necessery to display the speed of the car */
    javax.swing.JLabel speedLabel;

    /** Label necessery to display the distance covering by the car */
    javax.swing.JLabel distanceLabel;

    /** Constructors : displays the buttons, the labels, the distance  and the speedometer */
    public CarPanel(final CruiseControlApplet parent,
        final Interface activeObject) {
        setLayout(null);
        setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));

        ///////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////
        // Display
        speedLabel = new javax.swing.JLabel("0");
        distanceLabel = new javax.swing.JLabel("0");
        javax.swing.JLabel speedDisplay = new javax.swing.JLabel("Speed");
        javax.swing.JLabel distanceDisplay = new javax.swing.JLabel("Dist.");

        //Buttons and Labels
        javax.swing.JButton EngineOn = new javax.swing.JButton("Engine On");
        javax.swing.JButton EngineOff = new javax.swing.JButton("Engine Off");
        Accelerate = new javax.swing.JLabel("Accelerate");
        javax.swing.JLabel Up = new javax.swing.JLabel("Up");
        javax.swing.JLabel Down = new javax.swing.JLabel("Down");
        javax.swing.JButton Brake = new javax.swing.JButton("Brake");
        javax.swing.JButton Inc = new javax.swing.JButton("+");
        javax.swing.JButton Dec = new javax.swing.JButton("-");
        javax.swing.JButton UpAlpha = new javax.swing.JButton("Up");
        javax.swing.JButton DownAlpha = new javax.swing.JButton("Down");

        accBar = new javax.swing.JProgressBar(SwingConstants.VERTICAL, 0, 50);
        accBar.setValue(0);
        accBar.setStringPainted(true);

        roadBar = new javax.swing.JProgressBar(SwingConstants.VERTICAL, -60, 60);
        roadBar.setValue(0);
        roadBar.setStringPainted(true);

        EngineOn.setBounds(10, 310, 120, 30);
        EngineOff.setBounds(140, 310, 130, 30);

        Accelerate.setBounds(390, 310, 120, 30);
        Up.setBounds(450, 10, 30, 30);
        Down.setBounds(450, 120, 50, 30);
        UpAlpha.setBounds(370, 20, 70, 30);
        DownAlpha.setBounds(350, 110, 90, 30);

        Brake.setBounds(280, 310, 80, 30);
        Inc.setBounds(365, 345, 60, 30);
        Dec.setBounds(430, 345, 60, 30);
        accBar.setBounds(420, 200, 40, 80);
        roadBar.setBounds(450, 40, 40, 80);

        speedDisplay.setBounds(200, 230, 50, 30);
        distanceDisplay.setBounds(200, 260, 50, 30);
        speedLabel.setBounds(270, 230, 50, 30);
        distanceLabel.setBounds(270, 260, 50, 30);

        // add the components
        add(EngineOn);
        add(EngineOff);

        add(Accelerate);
        add(Up);
        add(Down);

        add(UpAlpha);
        add(DownAlpha);

        add(Brake);
        add(Inc);
        add(Dec);
        add(accBar);
        add(roadBar);

        add(speedDisplay);
        add(distanceDisplay);
        add(speedLabel);
        add(distanceLabel);

        EngineOn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.engineOn();
                }
            });
        EngineOff.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.engineOff();
                }
            });
        Brake.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.brake();
                    brake();
                    parent.controlPaneOff();
                }
            });
        Inc.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.accelerate();
                }
            });
        Dec.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.decelerate();
                }
            });
        UpAlpha.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.incAlpha();
                }
            });
        DownAlpha.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    activeObject.decAlpha();
                }
            });
    }

    ///////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////

    /** Turns the car to the acive state */
    public void engineOn() {
        if (this.state == INACTIVE) {
            this.state = ACTIVE;
            repaint();
        }
    }

    /** Turns the car to the off state */
    public void engineOff() {
        setSpeed(0);
        setAcceleration(0);
        this.state = INACTIVE;
        repaint();
    }

    /** Not used in this release */
    public void controlOn() {
        if (this.state == ACTIVE) {
        }
    }

    /** Not used in this release */
    public void controlOff() {
    }

    /** Not used in this release */
    public void accelerate() {
    }

    /** Sets the current acceleration to null */
    public void brake() {
        setAcceleration(0);
    }

    ////////////////////////////////////////////////////////////////

    /** Displays the car, the speedometer */
    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (state == INACTIVE) {
            g.setColor(java.awt.Color.black);
            g.drawString("Engine Off", 40, 245);
            g.setColor(java.awt.Color.red);
            g.fillOval(110, 230, 20, 20);
        } else {
            g.setColor(java.awt.Color.black);
            g.drawString("Engine On", 40, 245);
            g.setColor(java.awt.Color.green);
            g.fillOval(110, 230, 20, 20);
        }

        g.setColor(java.awt.Color.black);
        g.drawOval(150, 40, 170, 170);

        for (int i = 0; i <= 160; i += 10) {
            drawMark(g, 150 + 85, 40 + 85, 85, i);
        }
        drawSpeed(g, 150 + 85, 40 + 85, 85);
    }

    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////

    /** called by paintComponent to display the speedometer */
    private void drawSpeed(java.awt.Graphics g, int x, int y, int len) {
        g.setColor(java.awt.Color.black);
        double fangle = ((40 + ((speed * 7) / 4)) * Math.PI) / 180;

        int mx = x - (int) ((len - 30) * Math.sin(fangle));
        int my = y + (int) ((len - 30) * Math.cos(fangle));

        g.drawLine(mx, my, x, y);
    }

    /** called by paintComponent to display the speedometer */
    private void drawMark(java.awt.Graphics g, int x, int y, int len, int n) {
        double flen = len;
        double fangle = ((40 + (n * 1.75)) * Math.PI) / 180;
        int mx = x - (int) (flen * Math.sin(fangle));
        int my = y + (int) (flen * Math.cos(fangle));
        int mx2 = x - (int) ((flen - 5) * Math.sin(fangle));
        int my2 = y + (int) ((flen - 5) * Math.cos(fangle));

        g.drawLine(mx, my, mx2, my2);

        flen = flen + 12;
        mx = x - 7 - (int) (flen * Math.sin(fangle));
        my = y + 7 + (int) (flen * Math.cos(fangle));
        g.drawString(String.valueOf(n), mx, my);
    }

    /** change the current speed to the new one, and displays it */
    public void setSpeed(double m_speed) {
        this.speed = m_speed;
        this.speedLabel.setText(Integer.toString((int) speed));
        repaint(150, 40, 170, 170);
    }

    /** change the current distance to the new one, and displays it */
    public void setDistance(double m_distance) {
        this.distance = m_distance;
        this.distanceLabel.setText(Integer.toString((int) distance));
    }

    /** Displays the current acceleration in percentage */
    public void setAcceleration(double m_acc) {
        accBar.setValue((int) m_acc);
    }

    /** Displays the current road incline */
    public void setAlpha(double m_alpha) {
        roadBar.setValue((int) (m_alpha * 100));
    }
}
