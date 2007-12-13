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

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/** This very limited class is needed as an Active Object class, containing the GUI.
 * The swing GUI is attached as a field of the Active Object, so it can be recreated
 * from scratch. */
public class Displayer implements Serializable {
    private transient NBodyFrame nbf;
    private boolean displayft;
    private int nbBodies;
    private boolean ddd = false;

    public Displayer() {
    }

    public Displayer(Integer nbBodies, Boolean displayft,
            org.objectweb.proactive.examples.nbody.common.Start killsupport, BooleanWrapper enable3D) {
        this.nbBodies = nbBodies.intValue();
        this.displayft = displayft.booleanValue();
        if (!enable3D.booleanValue()) {
            this.nbf = new NBody2DFrame("ProActive N-Body", this.nbBodies, this.displayft, killsupport);
        } else {
            this.ddd = true;
            // For compiling without Java 3D installed
            try {
                this.nbf = (NBodyFrame) Class.forName(
                        "org.objectweb.proactive.examples.nbody.common.NBody3DFrame").getConstructor(
                        new Class[] { String.class, Integer.class, Boolean.class, Start.class }).newInstance(
                        new Object[] { "ProActive N-Body", new Integer(this.nbBodies),
                                new Boolean(this.displayft), killsupport });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void drawBody(double x, double y, double z, double vx, double vy, double vz, int weight, int d,
            int id, String name) {
        if (!this.ddd) {
            this.nbf.drawBody(x, y, z, vx, vy, vz, weight, d, id, name);
        } else {
            // Doesn't work wothout / 1000, Igor (or Alex I don't know) doesn't know why ;)
            this.nbf.drawBody(((double) x) / 1000, ((double) y) / 1000, ((double) z) / 1000, vx, vy, vz,
                    weight, d, id, name);
        }
    }
}
