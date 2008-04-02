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
package org.objectweb.proactive.examples.nbody.groupoospmd;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Deployer;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Force;
import org.objectweb.proactive.examples.nbody.common.Planet;


public class Domain implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** If we want some graphical interface */
    private Displayer display;

    /** Unique domain identifier */
    private int identification;

    /** To display on which host we're running */
    private String hostName = "unknown";

    /** The typed group containing all the other Domains */
    private Domain neighbours;

    /**  The body information */
    private Planet info;

    /** The sum of the forces already worked out */
    private Force currentForce;

    /** ProActive reference on self */
    private Domain asyncRefToSelf;

    /** iteration count related variables */
    private int iter;

    /** iteration count related variables */
    private int maxIter;

    /** reference to descriptor pad, useful when killing all deployment at the end of the simulation */
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
    public Domain(Integer i, Planet planet) {
        identification = i.intValue();
        info = planet;
        hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
    }

    /**
     * Initialize all necessary variables, namely the variables concerning neighborhood, the display, and forces.
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param maxIter The number of iterations to compute before stopping
     */
    public void init(Displayer dp, int maxIter, Deployer deployer) {
        this.deployer = deployer;
        display = dp;
        this.maxIter = maxIter;
        neighbours = (Domain) PASPMD.getSPMDGroup();
        asyncRefToSelf = (Domain) PAActiveObject.getStubOnThis();
        PASPMD.barrier("INIT"); // first barrier, needed to have all objects synchronized before running 
        asyncRefToSelf.sendValueToNeighbours();
        currentForce = new Force(); // initialize the force to 0.
    }

    /**
     * Move the Planet contained, applying the force computed.
     */
    public void moveBody() {
        info.moveWithForce(currentForce);
        currentForce = new Force(); // clean up, for following iteration
    }

    /**
     * Called by a distant Domain, this method adds the inf contribution to the force applied on the local Planet
     * @param inf the distant Planet which adds its contribution.
     * @param id the distant Domain's identification
     */
    public void setValue(Planet inf, int id) {
        if (id != identification) {
            currentForce.add(info, inf); // add this contribution to the force on Planet
        }
    }

    /**
     * Triggers the emission of the local Planet to all the other Domains.
     */
    public void sendValueToNeighbours() {
        neighbours.setValue(info, identification);
        PASPMD.barrier("barrier" + iter);
        iter++;
        asyncRefToSelf.moveBody();
        if (iter < maxIter) {
            asyncRefToSelf.sendValueToNeighbours();
        } else {
            if (identification == 0) { // clean up all the deployment. 
                deployer.abortOnError(new Exception());
            }
        }

        // Display code
        if (display == null) { // if no display, only the first Domain outputs message to say recompute is going on
            if (identification == 0) {
                logger.info("Compute movement. " + iter);
            }
        } else {
            display.drawBody(info.x, info.y, info.z, info.vx, info.vy, info.vz, (int) info.mass,
                    (int) info.diameter, identification, hostName);
        }
    }

    /**
     * Method called when the object is redeployed on a new Node (Fault recovery, or migration).
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
    }
}
