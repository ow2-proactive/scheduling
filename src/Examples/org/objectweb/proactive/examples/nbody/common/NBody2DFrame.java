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
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
    private int[][] bodies; // [index]-> [x,y,w,d,vx,vy]
    private ArrayList names;
    private int nbBodies;
    private CircularPostionList[] historics;
    private boolean showTrace = false;
    private int histoSize = MAX_HISTO_SIZE;
    private double zoomValue = 1;
    private int xCenter = SIZE / 2; // the center of the display
    private int yCenter = SIZE / 2;
    private Deployer deployer;

    // gui
    private JButton kill;
    private JComboBox listVMs = new JComboBox();
    private JComboBox protocol;
    private JCheckBox queueCheckBox;
    private JButton zoomIn;
    private JButton zoomOut;

    public NBody2DFrame(String title, int nb, boolean displayft, Deployer deployer) {
        super(title);
        nbBodies = nb;
        this.deployer = deployer;
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
        addWindowListener(this);

        // the GUI panel (where the buttons are), first the atomic components, then assembling them
        queueCheckBox = new JCheckBox("Show trace", false);
        queueCheckBox.addActionListener(this);

        zoomIn = new JButton("Zoom in");
        zoomIn.addActionListener(this);

        zoomOut = new JButton("Zoom out");
        zoomOut.addActionListener(this);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(zoomIn);
        buttonsPanel.add(zoomOut);
        buttonsPanel.add(queueCheckBox);
        buttonsPanel.setBorder(BorderFactory.createTitledBorder("Draw control"));

        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        if (displayft) {
            JPanel killingPanel = new JPanel(new GridLayout(1, 4));
            protocol = new JComboBox(new Object[] { "rsh", "ssh" });
            listVMs.addActionListener(this);
            JLabel cmd = new JLabel(" killall java  ");
            kill = new JButton("Execute");
            kill.addActionListener(this);
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
        bodies[id][0] = (int) x;
        bodies[id][1] = (int) y;
        bodies[id][2] = weight;
        bodies[id][3] = d;
        bodies[id][4] = (int) vx;
        bodies[id][5] = (int) vy;
        bodyname[id] = name;
        if (!names.contains(name)) {
            names.remove(id);
            names.add(id, name);
            listVMs.addItem(name);
        }
        repaint();
    }

    private void changeZoom(double d) {
        zoomValue *= d;
        center(SIZE / 2, SIZE / 2);
    }

    private void center(int xxx, int yyy) {
        int xRef = 0;
        int yRef = 0;
        for (int i = 0; i < nbBodies; i++) {
            xRef += getZoomedCoord(bodies[i][0]);
            yRef += getZoomedCoord(bodies[i][1]);
        }
        xCenter = xxx - xRef / nbBodies;
        yCenter = yyy - yRef / nbBodies;
        clearTrace();
    }

    private int getZoomedCoord(int x) {
        return (int) (x * zoomValue);
    }

    private void clearTrace() {
        historics = new CircularPostionList[nbBodies];
        for (int i = 0; i < nbBodies; i++) {
            historics[i] = new CircularPostionList(MAX_HISTO_SIZE);
        }
    }

    private class PlanetDisplayPanel extends JPanel {
        private final Image bkground;
        private int iter = 0;
        private BufferedImage[] stars;
        private Color[] colors = { Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW, Color.CYAN, Color.WHITE };

        // private Color[] colors = { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
        // Color.DARK_GRAY,
        // Color.MAGENTA, Color.ORANGE, Color.PINK, Color.BLACK };

        private PlanetDisplayPanel(Image bkground) {
            super();
            this.bkground = bkground;

            // Image planets
            try {
                ClassLoader cl = this.getClass().getClassLoader();
                stars = new BufferedImage[6];
                for (int i = 0; i < stars.length; i++) {
                    stars[i] = ImageIO.read(cl
                            .getResource("org/objectweb/proactive/examples/nbody/common/gflare" + (i + 1) +
                                ".png"));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            iter++;

            Insets inset = this.getInsets();
            Rectangle clip = g.getClipBounds();
            Rectangle clippingRegion = new Rectangle(this.getWidth() - (inset.left + inset.right), this
                    .getHeight() -
                (inset.top + inset.bottom));

            int xRepeat = 0;
            int yRepeat = 0;

            ImageIcon image = new ImageIcon(bkground);

            int height = getSize().height - inset.bottom;
            int width = getSize().width - inset.right;

            yRepeat = (int) Math.ceil(clippingRegion.getHeight() / image.getIconHeight());
            xRepeat = (int) Math.ceil(clippingRegion.getWidth() / image.getIconWidth());

            if (clip.y + clip.height > height)
                clip.height = height - clip.y;

            if (clip.x + clip.width > width)
                clip.width = width - clip.x;

            for (int i = 0; i <= yRepeat; i++) {
                for (int j = 0; j <= xRepeat; j++) {
                    image.paintIcon(this, g, j * image.getIconWidth() + inset.left, i *
                        image.getIconHeight() + inset.top);
                }
            }

            g.drawImage(image.getImage(), 0, 0, this);

            // draw historics
            if (showTrace) {
                for (int i = 0; i < nbBodies; i++) {
                    for (int j = 0; j < histoSize; j++) {
                        int diameter = bodies[i][3] > 10 ? bodies[i][3] : 6;
                        g.setColor(getColor(i));
                        g.fillOval(historics[i].getX(j) + diameter / 2, historics[i].getY(j) + diameter / 2,
                                6, 6);
                        g.setColor(Color.DARK_GRAY);
                        g.drawOval(historics[i].getX(j) + diameter / 2, historics[i].getY(j) + diameter / 2,
                                6, 6);
                    }
                }
            }

            Graphics2D g2d = (Graphics2D) g;
            AffineTransform originalAT = g2d.getTransform();
            g.setFont(g.getFont().deriveFont(Font.ITALIC, 12));
            for (int i = 0; i < nbBodies; i++) {
                // g.setColor(getColor(i));
                int diameter = bodies[i][3];
                int zoomedX = getZoomedCoord(bodies[i][0]) + xCenter;
                int zoomedY = getZoomedCoord(bodies[i][1]) + yCenter;
                // g.fillOval(zoomedX, zoomedY, diameter, diameter);
                // g.setColor(Color.WHITE);
                // g.drawOval(zoomedX, zoomedY, diameter, diameter);
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(bodyname[i], zoomedX + diameter, zoomedY);
                AffineTransform newAT = (AffineTransform) (originalAT.clone());
                newAT.rotate(Math.toRadians((i % 2 == 0 ? -1 : 1) * (iter % 360)), zoomedX + 2 * diameter,
                        zoomedY + 2 * diameter);
                g2d.setTransform(newAT);
                g.drawImage(stars[i % stars.length], zoomedX, zoomedY, 4 * diameter, 4 * diameter, null);
                g2d.setTransform(originalAT);

                // update histo
                if (iter % 8 == 0) {
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
            list = new int[size][2];
            currentIndex = 0;
        }

        public void addValues(int x, int y) {
            list[currentIndex][0] = x;
            list[currentIndex][1] = y;
            currentIndex++;
            if (currentIndex == size) {
                currentIndex = 0;
            }
        }

        public int getX(int position) {
            return list[position][0];
        }

        public int getY(int position) {
            return list[position][1];
        }

        public void setX(int x, int position) {
            list[position][0] = x;
        }

        public void setY(int y, int position) {
            list[position][1] = y;
        }

        public int getSize() {
            return size;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }
    }

    // / EVENT HANDLING
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == zoomIn) {
            changeZoom(1.5);
        } else if (e.getSource() == zoomOut) {
            changeZoom(0.66);
        } else if (e.getSource() == queueCheckBox) {
            showTrace = !showTrace;
        } else if (e.getSource() == kill) {
            try {
                Runtime.getRuntime().exec(
                        "" + protocol.getSelectedItem() + " " + listVMs.getSelectedItem() +
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
        deployer.terminateAllAndShutdown(false);
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
