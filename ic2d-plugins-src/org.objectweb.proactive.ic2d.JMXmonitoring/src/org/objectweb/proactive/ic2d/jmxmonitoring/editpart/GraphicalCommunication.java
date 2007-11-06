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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;


public class GraphicalCommunication {
    private final static int RIGHT_MASK = 0xFFFF; // 00000000000000001111111111111111
    private final static int LEFT_MASK = 0xFFFF << 16; // 11111111111111110000000000000000
    private AOFigure source;
    private AOFigure destination;
    private IFigure panel;
    private Color color;
    private final int hashcode;

    /**
     *
     *
     * @param target the target of the connection
     * @param panel the connection is added to this panel
     */
    public GraphicalCommunication(AOFigure source, AOFigure destination,
        IFigure panel, Color color) {
        this.source = source;
        this.destination = destination;
        this.panel = panel;
        this.color = color;
        // To achieve unicity for a directed communication   
        // we take the right 16 bits of the source hashcode and 16 bits of dest hashcode
        // this is used to avoid a + b = b + a
        this.hashcode = (this.source.hashCode() & RIGHT_MASK) +
            (this.destination.hashCode() & LEFT_MASK) + this.panel.hashCode();
    }

    public Color getColor() {
        return color;
    }

    public AOFigure getDestination() {
        return destination;
    }

    public IFigure getPanel() {
        return panel;
    }

    public AOFigure getSource() {
        return source;
    }

    /**
     * Draw the communication
     */
    public void draw() {
        source.addConnection(destination, panel, color);
    }

    public int hashcode() {
        return this.hashcode;
    }

    @Override
    public boolean equals(Object o) {
        GraphicalCommunication communication = (GraphicalCommunication) o;
        return ((this.source == communication.getSource()) &&
        (this.destination == communication.getDestination()) &&
        (this.panel == communication.getPanel()) &&
        (this.color == communication.getColor()));
    }
}
