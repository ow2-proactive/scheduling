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
import java.util.ArrayList;
import java.util.List;


/**
 * Synchronization of the others Maestro
 */
public class BigMaestro implements Serializable {

    /** Counts the number of Maestro that have respond */
    private int nbFinished = 0;

    /** Number of iteration at a time */
    private int iter = 0;

    /** Number of iteration maximum */
    private int maxIter;

    /** List of all the Planets */
    private List lPlanets;

    /** KillSupport */
    private org.objectweb.proactive.examples.nbody.common.Start killsupport;

    /** References on all the Active Maestro */
    private Maestro[] maestroArray;

    /**
     * Required by ProActive
     */
    public BigMaestro() {
    }

    /**
     * Create a new master for the simulation, which pilots all the Maestro given in parameter.
     * @param maestroArray the group of Maestro which are to be controled by this BigMaestro.
     * @param max the total number of iterations that should be simulated
     * @param killsupport KillSupport
     */
    public BigMaestro(Maestro[] maestroArray, Integer max,
        org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        this.maxIter = max.intValue();
        this.maestroArray = maestroArray;
        // All the Maestro have a list of 8 Planets
        this.lPlanets = new ArrayList(maestroArray.length * 8);
        for (int i = 0; i < (maestroArray.length * 8); i++)
            lPlanets.add(null);
    }

    /**
     * Called by a Maestro when all of this Domain have finished computation.
     * This method counts the calls, and responds all Maestro for continuing if needed.
     * @param id the identification of the Maestro that have finished
     * @param lPla the list of Planets that contains the given Maestro
     */
    public void notifyFinished(int id, List lPla) {
        this.nbFinished++; // one another have finished

        // update of the new planets's positions
        for (int i = 0; i < lPla.size(); i++)
            lPlanets.set((id * 8) + i, lPla.get(i));

        // next iteration
        if (this.nbFinished == maestroArray.length) {
            this.nbFinished = 0;
            this.iter++;
            if (this.iter == this.maxIter) {
                this.killsupport.quit();
            }

            // Restart all the Maestro
            for (int i = 0; i < maestroArray.length; i++)
                maestroArray[i].finished();
        }
    }
}
