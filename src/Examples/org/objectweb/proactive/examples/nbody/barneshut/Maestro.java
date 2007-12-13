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
 * Synchronization of the computation of the Domain
 */
public class Maestro implements Serializable {

    /** Counts the number of Domain that have respond */
    private int nbFinished = 0;

    /** a unique number to differentiate this Maestro from the others */
    private int identification;

    /** List of the Planets associed at this Maestro */
    private List lPlanets;

    /** References on all the Active Domain associed at this Maestro */
    private Domain[] domainArray;

    /** Reference on the BigMaestro that synchronize all the Maestro */
    private BigMaestro bigMaestro;

    /**
     * Required by ProActive
     */
    public Maestro() {
    }

    /**
     * Create a new master for the simulation, which pilots 8 domains maximum.
     * @param aDomainArray all the Domain of the simulation.
     */
    public Maestro(Integer id, Domain[] aDomainArray,
            org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.identification = id.intValue();

        // We put only the Domain that correspond with this Maestro
        if (aDomainArray.length < (8 * (this.identification + 1))) {
            this.domainArray = new Domain[aDomainArray.length - (8 * this.identification)];
        } else {
            this.domainArray = new Domain[8];
        }
        for (int i = 0; i < 8; i++) {
            if (((this.identification * 8) + i) < aDomainArray.length) {
                this.domainArray[i] = aDomainArray[(this.identification * 8) + i];
            }
        }

        // Initialisation of the list of planets
        this.lPlanets = new ArrayList(domainArray.length);
        for (int i = 0; i < domainArray.length; i++)
            lPlanets.add(null);
    }

    /** Initialisation of the reference to the Bigmaestro */
    public void init(BigMaestro big) {
        this.bigMaestro = big;
    }

    /**
     * Called by a Domain when computation is finished.
     * This method counts the calls, and communicates to the BigMaestro only when all have finished
     * @param id identification of the Domain
     * @param pl planet of the Domain, for the update
     */
    public void notifyFinished(int id, Planet pl) {
        this.nbFinished++; // one another have finished

        // update of the new planet's positions
        lPlanets.set(id % 8, pl);

        if (this.nbFinished == domainArray.length) {
            // notify the BigMaestro that the 8 Domain have finished
            bigMaestro.notifyFinished(this.identification, lPlanets);
        }
    }

    /** Lauch by the igMaestro when the Domain can pass to the next iteration
     *  Recontact the domain
     */
    public void finished() {
        this.nbFinished = 0;
        for (int i = 0; i < domainArray.length; i++)
            domainArray[i].moveAndDraw();
    }
}
