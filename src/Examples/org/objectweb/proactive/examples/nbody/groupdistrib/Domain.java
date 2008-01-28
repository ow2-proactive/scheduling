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
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Deployer;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Force;
import org.objectweb.proactive.examples.nbody.common.Planet;


public class Domain implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    private class Carrier implements Serializable {
        Planet planet;
        int iter;

        Carrier(Planet plan, int iter) {
            planet = plan;
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
    private Deployer deployer;

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
    public Domain(Integer i, Planet planet, Deployer deployer) {
        identification = i.intValue();
        prematureValues = new Vector();
        info = planet;
        this.deployer = deployer;
        hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
    }

    /**
     * Initialize all necessary variables, namely the variables concerning neighborhood, the display, and forces.
     * @param domainGroup the Group of all Domains within universe
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param maxIter The number of iterations to compute before stoppping
     */
    public void init(Domain domainGroup, Displayer dp, int maxIter, Deployer deployer) {
        this.deployer = deployer;
        display = dp;
        this.maxIter = maxIter;
        neighbours = domainGroup;
        Group g = PAGroup.getGroup(neighbours);
        g.remove(PAActiveObject.getStubOnThis()); // no need to send information to self
        nbvalues = g.size(); // number of expected values to receive.
        reset();
    }

    /**
     * Move the Planet contained, applying the force computed.
     */
    public void moveBody() {
        info.moveWithForce(currentForce);
        sendValueToNeighbours();
    }

    /**
     * Called by a distant Domain, this method adds the inf contribution to the force applied on the local Planet
     * @param inf the distant Planet which adds its contribution.
     * @param receivedIter the distant iteration, to make sure we're synchronized
     */
    public void setValue(Planet inf, int receivedIter) {
        if (iter == receivedIter) {
            currentForce.add(info, inf);
            nbReceived++;
            if (nbReceived == nbvalues) {
                moveBody();
            }
        } else {
            prematureValues.add(new Carrier(inf, receivedIter));
        }
    }

    /**
     * Triggers the emission of the local Planet to all the other Domains.
     */
    public void sendValueToNeighbours() {
        reset();
        iter++;
        if (iter < maxIter) {
            neighbours.setValue(info, iter);
            if (display == null) { // if no display, only the first Domain outputs message to say recompute is going on
                if (identification == 0 && iter % 50 == 0) {
                    logger.info("Compute movement." + iter);
                }
            } else {
                display.drawBody(info.x, info.y, info.z, info.vx, info.vy, info.vz, (int) info.mass,
                        (int) info.diameter, identification, hostName);
            }
            treatPremature();
        } else if (identification == 0) { // only need one quit signal man!
            deployer.abortOnError(new Exception());
        }
    }

    /**
     * Resends the premature information, which is probably up-to-date now
     */
    private void treatPremature() {
        int size = prematureValues.size();
        for (int i = 0; i < size; i++) {
            Carrier c = (Carrier) prematureValues.remove(0);
            setValue(c.planet, c.iter); // works even if c.iter > iter
        }
    }

    /**
     * Empties iteration-specific variables.
     *
     */
    private void reset() {
        nbReceived = 0;
        currentForce = new Force();
    }

    /**
     * Method called when the object is redeployed on a new Node (Fault recovery, or migration).
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
    }
}
