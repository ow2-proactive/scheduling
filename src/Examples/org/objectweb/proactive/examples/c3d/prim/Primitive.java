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
 * All palpable objects in space should implement this class.
 * As it is now, it only is a container for the surface.
 */
public abstract class Primitive implements java.io.Serializable {
    private Surface surf = new Surface();

    /**
     * The normal Vector to the Primitve, considered at the given point.
     * The result must be normalized!
     * @param pnt the coordinate on the primitive of which to give a normal.
     * @return the normal vector, ie the one orthogonal to the Primitive
     */
    public abstract Vec normal(Vec pnt);

    /**
     * Given a Ray, find the intersection between the Primitive and the Ray.
     * @param ray the Ray which should intersect the Primitive
     * @return null if no intersection
     */
    public abstract Isect intersect(Ray ray);

    /**
     * Rotate the object along the given three angles.
     * Watch out, performs rotateX, rotateY, then rotateZ [not commutative]
     * @param vec the three angles of rotation, in radians
     */
    public abstract void rotate(Vec vec);

    /**
     * @param surf The surface to apply to this primitive.
     */
    public void setSurface(Surface surf) {
        this.surf = surf;
    }

    /**
     * @return Returns the surface which is mapped onto this Primitive.
     */
    public Surface getSurface() {
        return surf;
    }
}
