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

import org.objectweb.proactive.examples.c3d.geom.Vec;


/**
 * Represents a punctual 3D viewpoint
 */
public class View implements java.io.Serializable {
    public Vec from;
    public Vec at;
    public Vec up;
    public double dist;
    public double angle;
    public double aspect;

    /** Default location */
    public View() {
        this.from = new Vec(0, 0, -30);
        this.at = new Vec(0, 0, -15);
        this.up = new Vec(0, 1, 0);
        this.angle = (35.0 * 3.14159265) / 180.0;
        this.aspect = 1.0;
        this.dist = 1.0;
    }
}
