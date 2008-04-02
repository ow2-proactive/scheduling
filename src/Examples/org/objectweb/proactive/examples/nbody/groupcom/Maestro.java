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

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.examples.nbody.common.Deployer;


/**
 * Synchronization of the computation of the Domains
 */
public class Maestro implements Serializable {
    private Domain domainGroup;
    private int nbFinished = 0;
    private int iter = 0;
    private int maxIter;
    private int size;
    private Deployer deployer;

    /**
     * Required by ProActive Active Objects
     */
    public Maestro() {
    }

    /**
     * Create a new master for the simulation, which pilots all the domains given in parameter.
     * @param domainG the group of Domains which are to be controled by this Maestro.
     * @param max the total number of iterations that should be simulated
     */
    public Maestro(Domain domainG, Integer max, Deployer deployer) {

        maxIter = max.intValue();
        domainGroup = domainG;
        size = PAGroup.getGroup(domainGroup).size();
    }

    /**
     * Called by a Domain when computation is finished.
     * This method counts the answers, and restarts all Domains when all have finished.
     */
    public void notifyFinished() {
        nbFinished++;
        if (nbFinished == size) {
            iter++;
            if (iter == maxIter) {
                deployer.terminateAllAndShutdown(false);
                return;
            }
            nbFinished = 0;
            domainGroup.sendValueToNeighbours();
        }
    }
}
