/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.nbody.common;

import java.io.Serializable;


public class Displayer implements Serializable {
    private transient NBodyFrame nbf;
    private boolean displayft;
    private int nbBodies;

    public Displayer() {
    }

    public Displayer(Integer nbBodies, Boolean displayft,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.nbBodies = nbBodies.intValue();
        this.displayft = displayft.booleanValue();
        this.nbf = new NBodyFrame("ProActive N-Body", this.nbBodies,
                this.displayft, killsupport);
        this.nbf.setVisible(true);
    }

    public void drawBody(int x, int y, int vx, int vy, int weight, int d,
        int id, String name) {
        this.nbf.drawBody(x, y, vx, vy, weight, d, id, name);
    }
}
