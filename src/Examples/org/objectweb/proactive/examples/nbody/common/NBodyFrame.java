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

public interface NBodyFrame {

    /**
     * size of the screen
     */
    public final static int SIZE = 500;

    /**
     * Maximum of ancient position of body (aka Ghost) saved.
     * Reduces this number to increase performance
     * Example of Complexities :
     * <li> Creation of java3d scene tree is at <B>O(MAX_HISTO_SIZE*nbBodies)</B><li>
     * <li> Drawing of trace is at <B>O(MAX_HISTO_SIZE*nbBodies)</B>
    */
    public final static int MAX_HISTO_SIZE = 100;

    /**
     * Method Invoked by remote bodies
     * @param x new x of the body
     * @param y new y of the body
     * @param z new z of the body
     * @param vx new vx of the body
     * @param vy new vy of the body
     * @param vz new vz of the body
     * @param mass mass of the body (INCOHERENT !)
     * @param diameter diameter of the body (DOUBLON D INFO, et INCOHERENT)
     * @param identification id of the body who call the method
     * @param hostName where the body is hosted
     */
    public abstract void drawBody(double x, double y, double z, double vx,
        double vy, double vz, int mass, int diameter, int identification,
        String hostName);
}
