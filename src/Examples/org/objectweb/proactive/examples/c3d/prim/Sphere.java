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
package org.objectweb.proactive.examples.c3d.prim;

import org.objectweb.proactive.examples.c3d.geom.Ray;
import org.objectweb.proactive.examples.c3d.geom.Vec;


/**
 * 3D representation of a Sphere, in space.
 */
public class Sphere extends Primitive implements java.io.Serializable {
    private Vec c;
    private double r;
    private double r2;
    private Vec tmp; // temporary vecs used to minimize the memory load
    private static double mindiff = 1e-6;

    public Sphere(Vec center, double radius) {
        c = center;
        r = radius;
        r2 = r * r;
        tmp = new Vec();
    }

    /**
     * Modified intersection method - creates _many_ less Vecs
     * @author Doyon Florian
     * @author Wilfried Klauser
     */
    @Override
    public Isect intersect(Ray ray) {
        Isect ip;
        tmp.sub2(c, ray.P);
        double dot = Vec.dot(tmp, ray.D);
        double disc = (dot * dot) - Vec.dot(tmp, tmp) + r2;
        if (disc < 0.0) {
            return null;
        }
        disc = Math.sqrt(disc);
        double t = ((dot - disc) < mindiff) ? (dot + disc) : (dot - disc);
        if (t < mindiff) {
            return null;
        }
        ip = new Isect();
        ip.t = t;
        ip.enter = (Vec.dot(tmp, tmp) > (r2 + mindiff));
        ip.prim = this;
        return ip;
    }

    /**
     * Normal (outwards) vector at point P of the sphere.
     */
    @Override
    public Vec normal(Vec p) {
        Vec normal = Vec.sub(p, c);
        normal.normalize();
        return normal;
    }

    @Override
    public String toString() {
        return "Sphere {" + c.toString() + ", radius " + r + "}";
    }

    public Vec getCenter() {
        return c;
    }

    public void setCenter(Vec c) {
        this.c = c;
    }

    /**
     * Rotates the Sphere.
     * @see org.objectweb.proactive.examples.c3d.prim.Primitive#rotate(org.objectweb.proactive.examples.c3d.geom.Vec)
     */
    @Override
    public void rotate(Vec vec) {
        double phi;
        double l;

        // the X axis rotation
        if (vec.x != 0) {
            phi = Math.atan2(c.z, c.y);
            l = Math.sqrt((c.y * c.y) + (c.z * c.z));
            c.y = l * Math.cos(phi + vec.x);
            c.z = l * Math.sin(phi + vec.x);
        }

        // the Y axis rotation
        if (vec.y != 0) {
            phi = Math.atan2(c.z, c.x);
            l = Math.sqrt((c.x * c.x) + (c.z * c.z));
            c.x = l * Math.cos(phi + vec.y);
            c.z = l * Math.sin(phi + vec.y);
        }

        // the Z axis rotation
        if (vec.z != 0) {
            phi = Math.atan2(c.x, c.y);
            l = Math.sqrt((c.y * c.y) + (c.x * c.x));
            c.y = l * Math.cos(phi + vec.z);
            c.x = l * Math.sin(phi + vec.z);
        }
    }
}
