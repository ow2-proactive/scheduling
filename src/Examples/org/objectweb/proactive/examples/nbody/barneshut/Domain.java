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
package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Displayer;


public class Domain implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** a unique number to differentiate this Domain from the others */
    private int identification;

    /** to display on which host we're running */
    private String hostName = "unknown";

    /** used for synchronization between domains */
    private Maestro maestro;

    /** optional, to have a nice output with java3D */
    private Displayer display;

    /** the planet associed to the domain */
    private Planet rock;

    /** OctTree for this domain */
    private OctTree octTree;

    /**
     * Empty constructor, required by ProActive
     */
    public Domain() {
    }

    /**
     * Creates a container for a Planet, within a region of space.
     * @param i The unique identifier of this Domain
     * @param planet The Planet controlled by this Domain
     * @param oct The OctTree corresponding of this Domain
     */
    public Domain(Integer i, Planet planet, OctTree oct) {
        identification = i.intValue();
        rock = planet;
        octTree = oct;
        hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
    }

    /**
     * Sets some execution-time related variables.
     * @param dp The Displayer used to show on screen the movement of the objects.
     * @param master Maestro used to synchronize the computations.
     */
    public void init(Displayer dp, Maestro master) {
        display = dp; // even if Displayer is null
        maestro = master;
        maestro.notifyFinished(identification, rock); // say we're ready to start .
    }

    /**
     * Calculate the force exerted on this body and move it.
     */
    public void moveBody() {
        Force force = octTree.computeForce(rock);
        rock.moveWithForce(force);
    }

    /**
     * Move the body, Draw the planet on the displayer and inform the Maestro
     */
    public void moveAndDraw() {
        maestro.notifyFinished(identification, rock);

        moveBody();

        if (display == null) { // if no display, only the first Domain outputs message to say recompute is going on
            if (identification == 0) {
                logger.info("Compute movement.");
            }
        } else {
            display.drawBody(rock.x, rock.y, rock.z, rock.vx, rock.vy, rock.vz, (int) rock.mass,
                    (int) rock.diameter, identification, hostName);
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
