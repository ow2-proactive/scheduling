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

import java.io.Serializable;


/**
 * Class implementing physical gravitation force between bodies.
 */
public class Force implements Serializable {
    double x = 0;
    double y = 0;
    double z = 0;
    final double G = 9.81;
    final double RMIN = 1;

    public Force() {
    }

    /**
     * From 2 interacting bodies 1 & 2, adds the force resulting from their interaction.
     * The force is the force that applies on 1, caused by 2
     * @param p1 the information of the boody on which the force is applied.
     * @param p2 the information of the body which caused the generation of a force.
     */
    public void add(Planet p1, Planet p2) {
        if (p2 != null) { // indeed, P2 null means no force must be added 
            double a = p2.x - p1.x;
            double b = p2.y - p1.y;
            double c = p2.z - p1.z;
            double length = Math.sqrt((a * a) + (b * b) + (c * c));
            if (length < (p1.diameter + p2.diameter)) {
                length = p1.diameter + p2.diameter;
            }
            double cube = length * length; // *length; 
            double coeff = (this.G * p2.mass) / cube; // * p1.mass removed, because division removed as well

            // Watch out : no minus sign : we want to have force of 2 on 1!
            this.x += (coeff * a);
            this.y += (coeff * b);
            this.z += (coeff * c);
        }
    }

    // FIXME : the code below is not used, and should be in order to make things more efficient.

    /**
     * Adds up the force of the parameter force to this.
     * @param f the force to be added to this
     */
    public void add(Force f) {
        this.x += f.x;
        this.y += f.y;
        this.z += f.z;
    }

    @Override
    public String toString() {
        return "<" + (int) this.x + " " + (int) this.y + " " + (int) this.z +
        ">";
    }
}
