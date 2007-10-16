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
package org.objectweb.proactive.examples.webservices.c3dWS.prim;

import org.objectweb.proactive.examples.webservices.c3dWS.geom.Ray;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;


public class Sphere extends Primitive implements java.io.Serializable {
    Vec c;
    double r;
    double r2;
    Vec v; // temporary vecs used to minimize the memory load
    Vec b; // temporary vecs used to minimize the memory load

    public Sphere(Vec center, double radius) {
        c = center;
        r = radius;
        r2 = r * r;
        v = new Vec();
        b = new Vec();
    }

    /**
     * Modified intersection method - creates _much_ less Vecs
     * @author Doyon Florian
     * @author Wilfried Klauser
     */
    @Override
    public Isect intersect(Ray ry) {
        double b;
        double disc;
        double t;
        Isect ip;
        v.sub2(c, ry.P);
        b = Vec.dot(v, ry.D);
        disc = (b * b) - Vec.dot(v, v) + r2;

        if (disc < 0.0) {
            return null;
        }

        disc = Math.sqrt(disc);
        t = ((b - disc) < 1e-6) ? (b + disc) : (b - disc);

        if (t < 1e-6) {
            return null;
        }

        ip = new Isect();
        ip.t = t;
        ip.enter = (Vec.dot(v, v) > (r2 + 1e-6)) ? 1 : 0;
        ip.prim = this;
        ip.surf = surf;

        return ip;
    }

    @Override
    public Vec normal(Vec p) {
        Vec r;
        r = Vec.sub(p, c);
        r.normalize();

        return r;
    }

    @Override
    public String toString() {
        return "Sphere {" + c.toString() + "," + r + "}";
    }

    @Override
    public Vec getCenter() {
        return c;
    }

    @Override
    public void setCenter(Vec c) {
        this.c = c;
    }
}
