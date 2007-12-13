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
 * An infinite Plane, represented by the cartesian equation  ax+by+cz =d .
 * The base vector represents the normal vector, and d the distance to the origin.
 */
public class Plane extends Primitive {
    private Vec base;
    private float d;
    private static double mindiff = 1e-6;

    public Plane(Vec abc, float d) {
        this.base = abc;
        this.d = d;
    }

    /**
     * The normal vector at the point pnt on the plane.
     *@see org.objectweb.proactive.examples.c3d.prim.Primitive#normal(org.objectweb.proactive.examples.c3d.geom.Vec)
     */
    @Override
    public Vec normal(Vec pnt) {
        Vec normal = new Vec(base.x, base.y, base.z);
        normal.normalize();
        return normal;
    }

    /**
     * @see org.objectweb.proactive.examples.c3d.prim.Primitive#intersect(org.objectweb.proactive.examples.c3d.geom.Ray)
     */
    @Override
    public Isect intersect(Ray ray) {
        double div = (ray.D.x * base.x) + (ray.D.y * base.y) + (ray.D.z * base.z);
        if (div == 0) {
            return null;
        }
        double t = (d - (base.x * ray.P.x) - (base.y * ray.P.y) - (base.z * ray.P.z)) / div;
        if (t > mindiff) {
            Isect ip = new Isect();
            ip.t = t;
            ip.enter = true; // I don't know what value to give to 'enter'.
            ip.prim = this;
            return ip;
        }
        return null;
    }

    @Override
    public String toString() {
        return base + " d=" + d;
    }

    /**
     * Rotate the Plane.
     * @see org.objectweb.proactive.examples.c3d.prim.Primitive#rotate(org.objectweb.proactive.examples.c3d.geom.Vec)
     */
    @Override
    public void rotate(Vec vec) {
        double phi;
        double l;

        // the X axis rotation
        if (vec.x != 0) {
            phi = Math.atan2(base.z, base.y);
            l = Math.sqrt((base.y * base.y) + (base.z * base.z));
            base.y = l * Math.cos(phi + vec.x);
            base.z = l * Math.sin(phi + vec.x);
        }

        // the Y axis rotation
        if (vec.y != 0) {
            phi = Math.atan2(base.z, base.x);
            l = Math.sqrt((base.x * base.x) + (base.z * base.z));
            base.x = l * Math.cos(phi + vec.y);
            base.z = l * Math.sin(phi + vec.y);
        }

        // the Z axis rotation
        if (vec.z != 0) {
            phi = Math.atan2(base.x, base.y);
            l = Math.sqrt((base.y * base.y) + (base.x * base.x));
            base.y = l * Math.cos(phi + vec.z);
            base.x = l * Math.sin(phi + vec.z);
        }
    }
}
