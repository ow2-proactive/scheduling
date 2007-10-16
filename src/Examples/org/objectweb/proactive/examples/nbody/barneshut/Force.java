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
package org.objectweb.proactive.examples.nbody.barneshut;


/**
 * Class implementing physical gravitation force between bodies.
 * In this Barnes-Hut package, building a force may yield an exception
 * if the info has a too big diameter.
 */
import java.io.Serializable;


public class Force implements Serializable {

    /** Vector of the force */
    public double x = 0;

    /** Vector of the force */
    public double y = 0;

    /** Vector of the force */
    public double z = 0;

    /** Gravitational constant */
    public final double G = 9.81;

    //    public Force() {
    //    }

    /**
     * Creation of a new Force
     * @param ux parameter x
     * @param uy parameter y
     * @param uz parameter z
     */
    public Force(double ux, double uy, double uz) {
        this.x = ux;
        this.y = uy;
        this.z = uz;
    }

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
