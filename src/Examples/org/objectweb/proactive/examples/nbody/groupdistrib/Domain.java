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
package org.objectweb.proactive.examples.nbody.groupdistrib;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Force;
import org.objectweb.proactive.examples.nbody.common.Planet;


public class Domain implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    private class Carrier implements Serializable {
        Planet planet;
        int iter;

        Carrier(Planet plan, int iter) {
            this.planet = plan;
            this.iter = iter;
        }
    }

    private Displayer display; // If we want some graphical interface
    private int identification; // unique domain identifier
    private String hostName = "unknown"; // to display on which host we're running
    private Domain neighbours; // The Group containing all the other Domains
    private Planet info; // the body information
    private Force currentForce; // the sum of the forces already worked out 
    private int nbvalues; // iteration related
    private int nbReceived = 0; // iteration related
    private int iter;
    private int maxIter;
    private Vector prematureValues; // if values arrive too early, put them here.
    private org.objectweb.proactive.examples.nbody.common.Start killsupport;

    /**
     * Required by ProActive Active Objects
     */
    public Domain() {
    }

    /**
     * Constructor
     * @param i the unique identifier
     * @param planet the Planet controlled by this Domain
     */
    public Domain(Integer i, Planet planet,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.identification = i.intValue();
        this.prematureValues = new Vector();
        this.info = planet;
        this.killsupport = killsupport;
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize all necessary variables, namely the variables concerning neighborhood, the display, and forces.
     * @param domainGroup the Group of all Domains within universe
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param maxIter The number of iterations to compute before stoppping
     */
    public void init(Domain domainGroup, Displayer dp, int maxIter,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        this.display = dp;
        this.maxIter = maxIter;
        this.neighbours = domainGroup;
        Group g = ProGroup.getGroup(neighbours);
        g.remove(ProActiveObject.getStubOnThis()); // no need to send information to self
        this.nbvalues = g.size(); // number of expected values to receive.
        reset();
    }

    /**
     * Move the Planet contained, applying the force computed.
     */
    public void moveBody() {
        this.info.moveWithForce(currentForce);
        sendValueToNeighbours();
    }

    /**
     * Called by a distant Domain, this method adds the inf contribution to the force applied on the local Planet
     * @param inf the distant Planet which adds its contribution.
     * @param receivedIter the distant iteration, to make sure we're synchronized
     */
    public void setValue(Planet inf, int receivedIter) {
        if (this.iter == receivedIter) {
            this.currentForce.add(info, inf);
            this.nbReceived++;
            if (this.nbReceived == this.nbvalues) {
                moveBody();
            }
        } else {
            this.prematureValues.add(new Carrier(inf, receivedIter));
        }
    }

    /**
     * Triggers the emission of the local Planet to all the other Domains.
     */
    public void sendValueToNeighbours() {
        reset();
        this.iter++;
        if (this.iter < this.maxIter) {
            neighbours.setValue(this.info, this.iter);
            if (this.display == null) { // if no display, only the first Domain outputs message to say recompute is going on
                if ((this.identification == 0) && ((this.iter % 50) == 0)) {
                    logger.info("Compute movement." + this.iter);
                }
            } else {
                this.display.drawBody(this.info.x, this.info.y, this.info.z,
                    this.info.vx, this.info.vy, this.info.vz,
                    (int) this.info.mass, (int) this.info.diameter,
                    this.identification, this.hostName);
            }
            treatPremature();
        } else if (this.identification == 0) { // only need one quit signal man!
            this.killsupport.quit();
        }
    }

    /**
     * Resends the premature information, which is probably up-to-date now
     */
    private void treatPremature() {
        int size = this.prematureValues.size();
        for (int i = 0; i < size; i++) {
            Carrier c = (Carrier) this.prematureValues.remove(0);
            setValue(c.planet, c.iter); // works even if c.iter > iter
        }
    }

    /**
     * Empties iteration-specific variables.
     *
     */
    private void reset() {
        this.nbReceived = 0;
        this.currentForce = new Force();
    }

    /**
     * Method called when the object is redeployed on a new Node (Fault recovery, or migration).
     */
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "unknown";
            e.printStackTrace();
        }
    }
}
