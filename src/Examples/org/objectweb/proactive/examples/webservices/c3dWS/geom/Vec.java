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
package org.objectweb.proactive.examples.webservices.c3dWS.geom;


/**
 * This class reflects the 3d vectors used in 3d computations
 */
public class Vec implements java.io.Serializable {

    /**
     * The x coordinate
     */
    public double x;

    /**
     * The y coordinate
     */
    public double y;

    /**
     * The z coordinate
     */
    public double z;

    /**
     * Constructor
     * @param a the x coordinate
     * @param b the y coordinate
     * @param c the z coordinate
     */
    public Vec(double a, double b, double c) {
        x = a;
        y = b;
        z = c;
    }

    /**
     * Copy constructor
     */
    public Vec(Vec a) {
        x = a.x;
        y = a.y;
        z = a.z;
    }

    /**
     * Default (0,0,0) constructor
     */
    public Vec() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    /**
     * Add a vector to the current vector
     * @param: a The vector to be added
     */
    public final void add(Vec a) {
        x += a.x;
        y += a.y;
        z += a.z;
    }

    /**
     * adds: Returns a new vector such as
     * new = sA + B
     */
    public static Vec adds(double s, Vec a, Vec b) {
        return new Vec((s * a.x) + b.x, (s * a.y) + b.y, (s * a.z) + b.z);
    }

    /**
     * Adds vector such as:
     * this+=sB
     * @param: s The multiplier
     * @param: b The vector to be added
     */
    public final void adds(double s, Vec b) {
        x += (s * b.x);
        y += (s * b.y);
        z += (s * b.z);
    }

    /**
     * Substracs two vectors
     */
    public static Vec sub(Vec a, Vec b) {
        return new Vec(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    /**
     * Substracts two vects and places the results in the current vector
     * Used for speedup with local variables -there were too much Vec to be gc'ed
     * Consumes about 10 units, whether sub consumes nearly 999 units!!
     * cf thinking in java p. 831,832
     */
    public final void sub2(Vec a, Vec b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        this.z = a.z - b.z;
    }

    public static Vec mult(Vec a, Vec b) {
        return new Vec(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static Vec cross(Vec a, Vec b) {
        return new Vec((a.y * b.z) - (a.z * b.y), (a.z * b.x) - (a.x * b.z),
            (a.x * b.y) - (a.y * b.x));
    }

    public static double dot(Vec a, Vec b) {
        return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
    }

    public static Vec comb(double a, Vec A, double b, Vec B) {
        return new Vec((a * A.x) + (b * B.x), (a * A.y) + (b * B.y),
            (a * A.z) + (b * B.z));
    }

    public final void comb2(double a, Vec A, double b, Vec B) {
        x = (a * A.x) + (b * B.x);
        y = (a * A.y) + (b * B.y);
        z = (a * A.z) + (b * B.z);
    }

    public final void scale(double t) {
        x *= t;
        y *= t;
        z *= t;
    }

    public final void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    public final double normalize() {
        double len;
        len = Math.sqrt((x * x) + (y * y) + (z * z));

        if (len > 0.0) {
            x /= len;
            y /= len;
            z /= len;
        }

        return len;
    }

    @Override
    public final String toString() {
        return "<" + x + "," + y + "," + z + ">";
    }
}
