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
package org.objectweb.proactive.examples.readers;

import java.awt.Color;
import java.awt.Dimension;


public class ReadCanvas extends javax.swing.JPanel {
    private int[] readers;
    private int[] writers;

    public ReadCanvas() {
        readers = new int[3];
        writers = new int[3];
        this.setPreferredSize(new Dimension(300, 300));
    }

    public void setRead(int id, boolean state) {
        readers[id] = (state) ? 1 : 0; // Casting bool into int..
        repaint();
    }

    public void setWrite(int id, boolean state) {
        writers[id] = (state) ? 1 : 0; // Casting bool into int..
        repaint();
    }

    public void setWait(int id, boolean isReader) {
        if (isReader) {
            readers[id] = -1;
        } else {
            writers[id] = -1;
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        // Text layout
        g.setColor(Color.black);
        g.drawString("Readers", 10, 10);
        g.drawString("Writers", 10, 140);

        for (int current = 0; current < 3; current++) {
            // Reader's image
            if (readers[current] == 1) {
                g.setColor(Color.red);
            } else if (readers[current] == -1) {
                g.setColor(Color.orange);
            } else {
                g.setColor(Color.green);
            }
            g.fillRect(10 + (current * 50), 30, 40, 60);

            // Writer's image
            if (writers[current] == 1) {
                g.setColor(Color.red);
            } else if (writers[current] == -1) {
                g.setColor(Color.orange);
            } else {
                g.setColor(Color.green);
            }
            g.fillRect(10 + (current * 50), 170, 40, 60);
        }

        // Legende
        g.setColor(Color.green);
        g.fillRect(160, 120, 10, 10); // vert 
        g.setColor(Color.orange);
        g.fillRect(160, 140, 10, 10); // orange
        g.setColor(Color.red);
        g.fillRect(160, 160, 10, 10); // rouge

        g.setColor(Color.black);
        g.drawString(" : Available", 170, 128);
        g.drawString(" : Waiting for Read or Write lock", 170, 148);
        g.drawString(" : Reading or Writing", 170, 168);
    }
}
