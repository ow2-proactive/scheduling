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
package org.objectweb.proactive.examples.nbody.groupcom;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Force;
import org.objectweb.proactive.examples.nbody.common.Planet;


/**
 * Domains encapsulate one Planet, do their calculations, communicates with a Group, and synchronized by a master.
 */
public class Domain implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private int identification; // unique domain identifier
    private Domain neighbours; // The Group containing all the other Domains
    private String hostName = "unknown"; // to display on which host we're running
    private Maestro maestro; // the master for synchronization
    private Displayer display; // If we want some graphical interface
    Planet info; // the body information
    private Planet[] values; // list of all the bodies sent by the other domains
    private int nbvalues; // iteration related variables, counting the "pings"
    private int nbReceived = 0; // iteration related variables, counting the "pings"
    private org.objectweb.proactive.examples.nbody.common.Start killsupport;

    /**
     * Required by ProActive Active Objects
     */
    public Domain() {
    }

    /**
     * Constructor
     * @param i the unique identifier
     * @param planet the Planet which is inside this Domain
     */
    public Domain(Integer i, Planet planet,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        identification = i.intValue();
        info = planet;
        try {
            this.hostName = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets some execution-time related variables.
     * @param domainGroup all the other Domains.
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param master Maestro used to synchronize the computations.
     */
    public void init(Domain domainGroup, Displayer dp, Maestro master) {
        this.neighbours = domainGroup;
        Group g = ProGroup.getGroup(neighbours);
        g.remove(ProActiveObject.getStubOnThis()); // no need to send information to self
        this.nbvalues = g.size(); // number of expected values to receive.
        this.values = new Planet[nbvalues + 1]; // leave empty slot for self
        this.display = dp;
        this.maestro = master;
    }

    /**
     * Reset iteration-related variables
     *
     */
    public void clearValues() {
        this.nbReceived = 0;
    }

    /**
     * Move the Planet contained, applying the force computed.
     */
    public void moveBody() {
        // logger.info("Domain " + identification + " starting mvt computation");
        Force force = new Force();
        for (int i = 0; i < values.length; i++) {
            force.add(info, values[i]); // adds the interaction of the distant body 
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
            this.killsupport.abort(new RuntimeException("Domain " +
                    identification + " received too many answers"));
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
        this.neighbours.setValue(info, identification);
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
