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
package org.objectweb.proactive.examples.c3d.geom;

/**
 * This class reflects the 3d vectors used in 3d computations
 */
public class Vec implements java.io.Serializable {

    /** The x coordinate */
    public double x;

    /** The y coordinate */
    public double y;

    /** The z coordinate */
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

    /** Copy constructor */
    public Vec(Vec a) {
        x = a.x;
        y = a.y;
        z = a.z;
    }

    /** Default (0,0,0) constructor */
    public Vec() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    /** Add a vector to the current vector, so that this += a
     * @param a The vector to be added */
    public final void add(Vec a) {
        x += a.x;
        y += a.y;
        z += a.z;
    }

    /** adds: Returns a new vector such as new = sA + B */
    public static Vec adds(double s, Vec a, Vec b) {
        return new Vec((s * a.x) + b.x, (s * a.y) + b.y, (s * a.z) + b.z);
    }

    /** Adds vector such as: this+=sB
     * @param: s The multiplier
     * @param: b The vector to be added */
    public final void adds(double s, Vec b) {
        x += (s * b.x);
        y += (s * b.y);
        z += (s * b.z);
    }

    /** Substraction of two vectors. @return vec such as vec = a -b */
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

    /** @return vec such as vec = ax * bx , ay * by , az * bz */
    public static Vec mult(Vec a, Vec b) {
        return new Vec(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    /** Cross product a ^ b*/
    public static Vec cross(Vec a, Vec b) {
        return new Vec((a.y * b.z) - (a.z * b.y), (a.z * b.x) - (a.x * b.z), (a.x * b.y) - (a.y * b.x));
    }

    /** Dot product  a.b */
    public static double dot(Vec a, Vec b) {
        return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
    }

    /** @return vec such as vec = aA + bB */
    public static Vec comb(double a, Vec A, double b, Vec B) {
        return new Vec((a * A.x) + (b * B.x), (a * A.y) + (b * B.y), (a * A.z) + (b * B.z));
    }

    /** Set this so that this = aA + bB */
    public final void comb2(double a, Vec A, double b, Vec B) {
        x = (a * A.x) + (b * B.x);
        y = (a * A.y) + (b * B.y);
        z = (a * A.z) + (b * B.z);
    }

    /** Multiply coordinates by t */
    public final void scale(double t) {
        x *= t;
        y *= t;
        z *= t;
    }

    /** changes vector this to -this */
    public final void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    /** The normal vector, ie the Vector in the same line, with norm 1. */
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

    /** Genreates a random Vec
     * @param length size of the side of the bounding cube */
    public static Vec random(float length) {
        double x = (Math.random() - 0.5) * length;
        double y = (Math.random() - 0.5) * length;
        double z = (Math.random() - 0.5) * length;
        return new Vec(x, y, z);
    }

    /** A vector in the quadrant of the vector, with coordinates eithr 0, 1 or -1 */
    public Vec direction() {
        double xx = ((this.x != 0) ? (this.x / Math.abs(this.x)) : 0);
        double yy = ((this.y != 0) ? (this.y / Math.abs(this.y)) : 0);
        double zz = ((this.z != 0) ? (this.z / Math.abs(this.z)) : 0);
        return new Vec(xx, yy, zz);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vec) {
            Vec v = (Vec) o;
            return ((v.x - this.x) == 0) & ((v.y - this.y) == 0) & ((v.z - this.z) == 0);
        }
        return false;
    }
}
