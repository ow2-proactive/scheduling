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
package org.objectweb.proactive.examples.doctor;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class Legend extends Dialog {
    public class LegendPanel extends Panel {
        DisplayPanel display;

        public LegendPanel(DisplayPanel _display) {
            display = _display;
        }

        @Override
        public void update(Graphics g) {
            FontMetrics fm = g.getFontMetrics();
            int h = fm.getAscent();

            g.setColor(display.wellOn);
            g.fillOval(30, 30 - h, h, h);
            g.setColor(display.patColor);
            g.drawString("Healthy patient", 40 + h, 30);

            g.setColor(display.sickOn);
            g.fillOval(30, (40 + h) - h, h, h);
            g.setColor(display.patColor);
            g.drawString("Sick patient", 40 + h, 40 + h);

            g.setColor(display.cureOn);
            g.fillOval(30, (50 + (2 * h)) - h, h, h);
            g.setColor(display.patColor);
            g.drawString("Patient with doctor", 40 + h, 50 + (2 * h));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 90);
        }

        @Override
        public void paint(Graphics g) {
            update(g);
        }
    }

    public Legend(Frame dw, DisplayPanel display) {
        super(dw, "Legend", false);

        Point parLoc = dw.getLocation();
        setLocation(parLoc.x + (dw.getSize().width), parLoc.y);

        LegendPanel pan = new LegendPanel(display);
        add(pan);

        pack();

        this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
    }
}
