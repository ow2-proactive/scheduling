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
package org.objectweb.proactive.examples.boundedbuffer;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * <p>
 * A classical bounded buffer composed of cells. The buffer is shared by a
 * producer and a consumer. The producer can only write in an empty cell, and the
 * consumer can only read a full cell.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class AppletBuffer extends org.objectweb.proactive.examples.StandardFrame {

    /**
     * The graphic panes
     */
    public CellPanel[] cells;
    private int max;
    private ActiveDisplay display;
    private javax.swing.JButton bProd;
    private javax.swing.JButton bCons;

    public AppletBuffer(String name, int width, int height) {
        super(name);
        max = 7;
        init(width, height);
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        new AppletBuffer("Bounded Buffer", 500, 300);
    }

    @Override
    public void start() {
        cells[0].setIn(true);
        cells[0].setOut(true);
        try {
            display = (ActiveDisplay) org.objectweb.proactive.api.ProActiveObject.turnActive(new ActiveDisplay(
                        max, this));
            receiveMessage("ActiveDisplay created : " +
                display.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        display.start();
    }

    public void kill() {
        display.done();
    }

    public void setCell(int pos, String str) {
        //displayMessage("set["+pos+"]="+str);
        cells[pos].setCaption(str);
        repaint();
    }

    public void setOut(int pos, boolean val) {
        //displayMessage("out["+pos+"]="+val);
        cells[pos].setOut(val);
        repaint();
    }

    public void setIn(int pos, boolean val) {
        //displayMessage("in["+pos+"]="+val);
        cells[pos].setIn(val);
        repaint();
    }

    public void consumerStartRunning() {
        receiveMessage("consumer now start running");
        bCons.setText("Stop");
    }

    public void consumerStopRunning() {
        receiveMessage("consumer now stop running");
        bCons.setText("Start");
    }

    public void producerStartRunning() {
        receiveMessage("producer now start running");
        bProd.setText("Stop");
    }

    public void producerStopRunning() {
        receiveMessage("producer now stop running");
        bProd.setText("Start");
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected javax.swing.JPanel createRootPanel() {
        javax.swing.JPanel rootPanel = new javax.swing.JPanel();
        rootPanel.setBackground(java.awt.Color.white);
        rootPanel.setLayout(new java.awt.GridLayout(3, 1));

        // Producer
        javax.swing.JPanel panel = new javax.swing.JPanel();
        javax.swing.JLabel label = new javax.swing.JLabel("Producer");
        label.setForeground(java.awt.Color.red);
        panel.add(label);
        bProd = new javax.swing.JButton("Start");
        bProd.setBackground(java.awt.Color.lightGray);
        bProd.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    display.toggleProd();
                }
            });
        panel.add(bProd);

        rootPanel.add(panel);

        // Cells panel
        javax.swing.JPanel cellsPanel = new javax.swing.JPanel();
        cells = new CellPanel[max];
        for (int i = 0; i < max; i++) {
            cells[i] = new CellPanel();
            cellsPanel.add(cells[i]);
        }
        rootPanel.add(cellsPanel);

        // Consumer panel
        panel = new javax.swing.JPanel();
        label = new javax.swing.JLabel("Consumer");
        label.setForeground(java.awt.Color.green);
        panel.add(label);
        bCons = new javax.swing.JButton("Start");
        bCons.setBackground(java.awt.Color.lightGray);
        bCons.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    display.toggleCons();
                }
            });
        panel.add(bCons);
        rootPanel.add(panel);
        return rootPanel;
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class CellPanel extends javax.swing.JPanel {
        private javax.swing.JLabel caption;
        private boolean in;
        private boolean out;
        private boolean empty;

        public CellPanel() {
            super();
            in = out = false;
            empty = true;
            caption = new javax.swing.JLabel("-empty-");
            add(caption);
            java.awt.Dimension d = new java.awt.Dimension(80, 25);
            this.setSize(d);
            this.setPreferredSize(d);
            this.setMinimumSize(d);
            this.setMaximumSize(d);
        }

        public void setIn(boolean in) {
            this.in = in;
            repaint();
        }

        public void setOut(boolean out) {
            this.out = out;
            repaint();
        }

        public void setCaption(String str) {
            if (str == null) {
                str = "-empty-";
                empty = true;
            } else {
                empty = false;
            }
            caption.setText(str);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (empty) {
                setBackground(java.awt.Color.gray);
            } else {
                setBackground(java.awt.Color.blue);
            }
            if (in) {
                java.awt.Color old = g.getColor();
                g.setColor(java.awt.Color.red);
                g.fillRect(1, 1, 5, 5);
                g.setColor(old);
            }
            if (out) {
                java.awt.Color old = g.getColor();
                g.setColor(java.awt.Color.green);
                g.fillRect(1, 10, 5, 5);
                g.setColor(old);
            }
        }
    }
}
