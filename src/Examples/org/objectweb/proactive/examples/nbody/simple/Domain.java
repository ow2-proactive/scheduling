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
package org.objectweb.proactive.examples.nbody.simple;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Force;
import org.objectweb.proactive.examples.nbody.common.Planet;


public class Domain implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private int identification; // a unique number to differentiate this Domain from the others
    private Domain[] neighbours; // the list of all the Domains
    private String hostName = "unknown"; // to display on which host we're running
    private Maestro maestro; // used for synchronization
    private Displayer display; // optional, to have a nice output 
    Planet info; // the information of the body considered
    private Planet[] values; // list of all the bodies within all the other domains
    private int nbvalues; // have we received all values awaited ?
    private int nbReceived = 0; // have we received all values awaited ?

    /**
     * Empty constructor, required by ProActive
     */
    public Domain() {
    }

    /**
     * Creates a container for a Planet, within a region of space.
     * @param i The unique identifier of this Domain
     * @param planet The Planet controlled by this Domain
     */
    public Domain(Integer i, Planet planet) {
        this.identification = i.intValue();
        this.info = planet;
        try {
            this.hostName = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets some execution-time related variables.
     * @param domainArray all the other Domains.
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param master Maestro used to synchronize the computations.
     */
    public void init(Domain[] domainArray, Displayer dp, Maestro master) {
        this.display = dp; // even if Displayer is null
        this.maestro = master;
        this.neighbours = domainArray;
        this.values = new Planet[domainArray.length];
        this.values[this.identification] = null; // null will mean don't compute for this value
        this.nbvalues = domainArray.length - 1; // will never receive value from self!
        maestro.notifyFinished(); // say we're ready to start .
    }

    /**
     *         Reset all iteration related variables
     */
    public void clearValues() {
        this.nbReceived = 0;
    }

    /**
     * Work out the movement of the Planet,
     */
    public void moveBody() {
        Force force = new Force();
        for (int i = 0; i < this.values.length; i++) {
            force.add(this.info, this.values[i]); // adds the interaction of the distant body 
        }
        this.info.moveWithForce(force);
        clearValues();
    }

    /**
     * Called by a distant Domain, this method adds the inf contribution to the force applied on the local Planet
     * @param inf the distant Planet which adds its contribution.
     * @param id the identifier of this distant body.
     */
    public void setValue(Planet inf, int id) {
        this.values[id] = inf;
        this.nbReceived++;
        if (this.nbReceived > this.nbvalues) { // This is a bad sign!
            System.err.println("Domain " + identification +
                " received too many answers");
        }
        if (this.nbReceived == this.nbvalues) {
            this.maestro.notifyFinished();
            moveBody();
        }
    }

    /**
     * Triggers the emission of the local Planet to all the other Domains.
     */
    public void sendValueToNeighbours() {
        for (int i = 0; i < this.neighbours.length; i++)
            if (i != this.identification) { // don't notify self!
                this.neighbours[i].setValue(this.info, this.identification);
            }
        if (this.display == null) { // if no display, only the first Domain outputs message to say recompute is going on
            if (this.identification == 0) {
                logger.info("Compute movement.");
            }
        } else {
            this.display.drawBody(this.info.x, this.info.y, this.info.z,
                this.info.vx, this.info.vy, this.info.vz, (int) this.info.mass,
                (int) this.info.diameter, this.identification, this.hostName);
        }
    }

    /**
     * Method called when the object is redeployed on a new Node (Fault recovery, or migration).
     */
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            this.hostName = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) {
            hostName = "unknown";
            e.printStackTrace();
        }
    }
}
