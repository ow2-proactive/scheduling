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


public class Point3D implements Serializable {

    /** x-coordinate of the point */
    public double x;

    /** y-coordinate of the point */
    public double y;

    /** z-coordinate of the point */
    public double z;

    /**
     * Required by ProActive
     */
    public Point3D() {
    }

    /**
     * Creation of a new Point3D
     * @param a x-coordinate of the point
     * @param b y-coordinate of the point
     * @param c z-coordinate of the point
     */
    public Point3D(double a, double b, double c) {
        x = a;
        y = b;
        z = c;
    }

    /** For displaying a Point3D */
    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
