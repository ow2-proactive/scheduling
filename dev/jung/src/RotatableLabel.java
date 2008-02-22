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


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;


public class RotatableLabel extends JLabel {
    protected double angle;

    public RotatableLabel() {
        super();
    }

    public RotatableLabel(String s, double angle) {
        super(s);
        this.angle = angle;
        setPreferredSize(new Dimension(100, 100));
        setMinimumSize(new Dimension(100, 100));
    }

    public void paintComponent(Graphics g) {
        System.out.println(this.getText() + " " + angle);
        Graphics2D g2d = (Graphics2D) g;
        //           g2d.translate(this.getWidth(), this.getHeight());
        g2d.translate(10, 10);
        // g2d.rotate( 90 - angle*360);
        g2d.rotate((3 * Math.PI) / 2, 0, 0);
        g2d.drawRect(this.getX(), this.getY(), this.getHeight(), this.getWidth());
        g2d.drawString(this.getText(), 0, 0);
    }
}
