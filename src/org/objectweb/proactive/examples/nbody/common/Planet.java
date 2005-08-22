/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.nbody.common;

import java.io.Serializable;

import org.objectweb.proactive.examples.nbody.common.Rectangle;


/**
 * The implementation of a physical body
 */
public class Planet implements Serializable {
    final double dt = 0.002; // the time step. The smaller the more precise the movement
    public double mass;
    public double x; // position and velocity
    public double y; // position and velocity
    public double vx; // position and velocity
    public double vy; // position and velocity
    public double diameter; // diameter of the body, used by the Displayer

    /**
     * Required by ProActive, because this Object will be send as a parameter of a method on
     * a distant Active Object.
     */
    public Planet() {
    }
    ;

    /**
     * Builds one Planet within the given frame.
     * @param limits the bounds which contain the Planet
     */
    public Planet(Rectangle limits) {
        this.x = limits.x + (Math.random() * limits.width);
        this.y = limits.y + (Math.random() * limits.height);
        this.mass = 1000 + (Math.random() * 100000);
        //vx = 2000*(Math.random () -0.5 );  
        //vy = 2000*(Math.random () -0.5 );
        this.vx = 0;
        this.vy = 0;
        this.diameter = (this.mass / 2000) + 3;
        ;
    }

    /**
     *         Move the given Planet with the Force given as parameter.
     *  @param force the force that causes the movement of the Planet
     */
    public void moveWithForce(Force force) {
        // Using f(t+dt) ~= f(t) + dt * f'(t)
        this.x += (this.dt * this.vx);
        this.y += (this.dt * this.vy);
        // sum F  = mass * acc;
        // v' = a = sum F / mass:
        this.vx += (this.dt * force.x); // removed /mass because * p1.mass removed as well  
        this.vy += (this.dt * force.y);
    }
}
