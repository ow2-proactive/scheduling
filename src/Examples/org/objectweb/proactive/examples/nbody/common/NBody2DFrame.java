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
package org.objectweb.proactive.examples.nbody.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class NBody2DFrame extends JFrame implements Serializable, ActionListener, MouseListener,
        WindowListener, NBodyFrame {
    // functional
    private String[] bodyname;
    private int[][] bodies; //[index]-> [x,y,w,d,vx,vy]
    private ArrayList names;
    private int nbBodies;
    private CircularPostionList[] historics;
    private boolean showTrace = false;
    private int histoSize = MAX_HISTO_SIZE;
    private double zoomValue = 1;
    private int xCenter = SIZE / 2; // the center of the display
    private int yCenter = SIZE / 2;

    // gui
    private JButton kill;
    private JComboBox listVMs = new JComboBox();
    private JComboBox protocol;
    private JCheckBox queueCheckBox;
    private JButton zoomIn;
    private JButton zoomOut;
    private Start killsupport;

    public NBody2DFrame(String title, int nb, boolean displayft, Start killsupport) {
        super(title);
        this.killsupport = killsupport;
        this.nbBodies = nb;
        setSize(SIZE + 11, SIZE + 90);
        setLocation(500, 50);
        bodies = new int[nb][6];
        bodyname = new String[nb];
        historics = new CircularPostionList[nb];
        for (int i = 0; i < nb; i++) {
            historics[i] = new CircularPostionList(MAX_HISTO_SIZE);
        }
        names = new ArrayList(nb);
        for (int i = 0; i < nb; i++) {
            names.add(i, " ");
            bodyname[i] = "";
        }

        ClassLoader cl = this.getClass().getClassLoader();
        java.net.URL u = cl.getResource("org/objectweb/proactive/examples/nbody/common/fondnbody.jpg");
        final Image backGround = getToolkit().getImage(u);
        this.addWindowListener(this);

        // the GUI panel (where the buttons are), first the atomic components, then assembling them
        this.queueCheckBox = new JCheckBox("Show trace", false);
        this.queueCheckBox.addActionListener(this);

        this.zoomIn = new JButton("Zoom in");
        this.zoomIn.addActionListener(this);

        this.zoomOut = new JButton("Zoom out");
        this.zoomOut.addActionListener(this);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(this.zoomIn);
        buttonsPanel.add(this.zoomOut);
        buttonsPanel.add(this.queueCheckBox);
        buttonsPanel.setBorder(BorderFactory.createTitledBorder("Draw control"));

        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        if (displayft) {
            JPanel killingPanel = new JPanel(new GridLayout(1, 4));
            this.protocol = new JComboBox(new Object[] { "rsh", "ssh" });
            this.listVMs.addActionListener(this);
            JLabel cmd = new JLabel(" killall java  ");
            this.kill = new JButton("Execute");
            this.kill.addActionListener(this);
            killingPanel.add(protocol);
            killingPanel.add(listVMs);
            killingPanel.add(cmd);
            killingPanel.add(kill);
            killingPanel.setBorder(BorderFactory.createTitledBorder("Execution control"));

            controlPanel.add(killingPanel);
        }
        controlPanel.add(buttonsPanel);

        // Animation panel, where the planets evolve
        JPanel anim = new PlanetDisplayPanel(backGround);
        anim.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        anim.addMouseListener(this);

        // Assembling it all
        JPanel main = new JPanel(new BorderLayout());
        main.add(anim, BorderLayout.CENTER);
        main.add(controlPanel, BorderLayout.SOUTH);
        setContentPane(main);
        setVisible(true);
    }

    public void drawBody(double x, double y, double z, double vx, double vy, double vz, int weight, int d,
            int id, String name) {
        this.bodies[id][0] = (int) x;
        this.bodies[id][1] = (int) y;
        this.bodies[id][2] = weight;
        this.bodies[id][3] = d;
        this.bodies[id][4] = (int) vx;
        this.bodies[id][5] = (int) vy;
        bodyname[id] = name;
        if (!names.contains(name)) {
            this.names.remove(id);
            this.names.add(id, name);
            this.listVMs.addItem(name);
        }
        repaint();
    }

    private void changeZoom(double d) {
        this.zoomValue *= d;
        center(SIZE / 2, SIZE / 2);
    }

    private void center(int xxx, int yyy) {
        int xRef = 0;
        int yRef = 0;
        for (int i = 0; i < this.nbBodies; i++) {
            xRef += getZoomedCoord(this.bodies[i][0]);
            yRef += getZoomedCoord(this.bodies[i][1]);
        }
        this.xCenter = xxx - (xRef / nbBodies);
        this.yCenter = yyy - (yRef / nbBodies);
        this.clearTrace();
    }

    private int getZoomedCoord(int x) {
        return (int) (x * zoomValue);
    }

    private void clearTrace() {
        historics = new CircularPostionList[this.nbBodies];
        for (int i = 0; i < this.nbBodies; i++) {
            historics[i] = new CircularPostionList(MAX_HISTO_SIZE);
        }
    }

    private class PlanetDisplayPanel extends JPanel {
        private final Image bkground;
        private int iter = 0;
        private Color[] colors = { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.DARK_GRAY,
                Color.MAGENTA, Color.ORANGE, Color.PINK, Color.BLACK };

        private PlanetDisplayPanel(Image bkground) {
            super();
            this.bkground = bkground;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            iter++;
            g.drawImage(bkground, 0, 0, this);
            // draw historics
            if (showTrace) {
                for (int i = 0; i < nbBodies; i++) {
                    for (int j = 0; j < histoSize; j++) {
                        int diameter = (bodies[i][3] > 10) ? (bodies[i][3]) : (6);
                        g.setColor(getColor(i));
                        g.fillOval(historics[i].getX(j) + (diameter / 2), historics[i].getY(j) +
                            (diameter / 2), 6, 6);
                        g.setColor(Color.DARK_GRAY);
                        g.drawOval(historics[i].getX(j) + (diameter / 2), historics[i].getY(j) +
                            (diameter / 2), 6, 6);
                    }
                }
            }

            g.setFont(g.getFont().deriveFont(Font.ITALIC, 12));
            for (int i = 0; i < nbBodies; i++) {
                g.setColor(getColor(i));
                int diameter = bodies[i][3];
                int zoomedX = getZoomedCoord(bodies[i][0]) + xCenter;
                int zoomedY = getZoomedCoord(bodies[i][1]) + yCenter;
                g.fillOval(zoomedX, zoomedY, diameter, diameter);
                g.setColor(Color.WHITE);
                g.drawOval(zoomedX, zoomedY, diameter, diameter);
                g.drawString(bodyname[i], zoomedX + diameter, zoomedY);

                //update histo
                if ((iter % 8) == 0) {
                    historics[i].addValues(zoomedX, zoomedY);
                }
            }
        }

        private Color getColor(int sel) {
            return colors[sel % colors.length];
        }
    }

    private class CircularPostionList {
        private int[][] list;
        private int currentIndex;
        private int size;

        public CircularPostionList(int size) {
            this.size = size;
            this.list = new int[size][2];
            this.currentIndex = 0;
        }

        public void addValues(int x, int y) {
            this.list[currentIndex][0] = x;
            this.list[currentIndex][1] = y;
            this.currentIndex++;
            if (this.currentIndex == size) {
                this.currentIndex = 0;
            }
        }

        public int getX(int position) {
            return this.list[position][0];
        }

        public int getY(int position) {
            return this.list[position][1];
        }

        public void setX(int x, int position) {
            this.list[position][0] = x;
        }

        public void setY(int y, int position) {
            this.list[position][1] = y;
        }

        public int getSize() {
            return this.size;
        }

        public int getCurrentIndex() {
            return this.currentIndex;
        }
    }

    /// EVENT HANDLING
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.zoomIn) {
            changeZoom(1.5);
        } else if (e.getSource() == this.zoomOut) {
            changeZoom(0.66);
        } else if (e.getSource() == this.queueCheckBox) {
            this.showTrace = !showTrace;
        } else if (e.getSource() == this.kill) {
            try {
                Runtime.getRuntime().exec(
                        "" + this.protocol.getSelectedItem() + " " + this.listVMs.getSelectedItem() +
                            " killall -KILL java");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        // else logger.info("Event not caught : " + e);
    }

    // WindowListener methods 
    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.killsupport.quit();
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
        windowClosing(e);
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        center(e.getX(), e.getY());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}
