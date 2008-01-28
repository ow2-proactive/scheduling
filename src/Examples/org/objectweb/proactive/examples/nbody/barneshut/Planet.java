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

import java.io.Serializable;

import org.objectweb.proactive.examples.nbody.common.Cube;


/**
 * The implementation of a physical body
 */
public class Planet implements Serializable {

    /** Time step, the smaller the more precise the movement */
    private final double dt = 0.002;

    /** Mass of the Planet */
    public double mass;

    /** Position of the Planet x */
    public double x;

    /** Position of the Planet y */
    public double y;

    /** Position of the Planet z */
    public double z;

    /** Velocity of the Planet x */
    public double vx;

    /** Velocity of the Planet y */
    public double vy;

    /** Velocity of the Planet z */
    public double vz;

    /** Diameter of the body, used by the Displayer */
    public double diameter;

    /**
     * Required by ProActive, because this Object will be send as a parameter of a method on
     * a distant Active Object.
     */
    public Planet() {
    }

    /**
     * Builds one Planet within the given frame.
     * @param limits the bounds which contain the Planet
     */
    public Planet(Cube limits) {
        // Positions random in the cube
        x = limits.x + Math.random() * limits.width;
        y = limits.y + Math.random() * limits.height;
        z = limits.z + Math.random() * limits.depth;
        mass = 1000 + Math.random() * 100000;
        // Velocity null at the beginning
        vx = 0;
        vy = 0;
        vz = 0;
        // Diameter calculated in function of the planet's mass
        // for more coherence
        diameter = mass / 2000 + 3;
    }

    /**
     *  Move the given Planet with the Force given as parameter.
     *  @param force the force that causes the movement of the Planet
     */
    public void moveWithForce(Force force) {
        // Using f(t+dt) ~= f(t) + dt * f'(t)
        x += dt * vx;
        y += dt * vy;
        z += dt * vz;
        // Using the formulates :
        // sum F  = mass * acc
        // v' = a = sum F / mass
        // We have removed / mass in the three next lines because * pl.mass 
        //removed as well in the OctTree's class (fonction computeForce)
        vx += dt * force.x;
        vy += dt * force.y;
        vz += dt * force.z;
    }
}
