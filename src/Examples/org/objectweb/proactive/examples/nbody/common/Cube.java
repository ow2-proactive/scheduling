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


public class Cube implements Serializable {

    /** x-coordinate of the cube */
    public double x;

    /** y-coordinate of the cube */
    public double y;

    /** z-coordinate of the cube */
    public double z;

    /** width of the cube */
    public double width;

    /** height of the cube */
    public double height;

    /** depth of the cube */
    public double depth;

    /**
     * Required by ProActive
     */
    public Cube() {
    }

    /**
     * Constructor of a new cube with the given arguments
     * @param x x-coordinate of the cube
     * @param y y-coordinate of the cube
     * @param z z-coordinate of the cube
     * @param width width of the cube
     * @param height height of the cube
     * @param depth depth of the cube
     */
    public Cube(double x, double y, double z, double width, double height, double depth) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * Constructor of a new cube spanning between the three given points
     * @param p Point in the bottom left corner in front
     * @param q Point in the top right corner in front
     * @param r Point in the bottom left corner in back
     */
    public Cube(Point3D p, Point3D q, Point3D r) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
        this.width = q.x - p.x;
        this.height = q.y - p.y;
        this.depth = r.z - p.z;
    }

    /** For displaying a Point3D */
    @Override
    public String toString() {
        return "x=" + x + " y=" + y + " y=" + y + " width=" + width + " height=" + height + " depth=" + depth;
    }
}
