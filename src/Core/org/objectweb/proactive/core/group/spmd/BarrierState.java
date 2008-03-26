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
package org.objectweb.proactive.core.group.spmd;

import java.io.Serializable;


/**
 * This class describes the state of a barrier.
 *
 * @author The ProActive Team
 */
public class BarrierState implements Serializable {

    /** The number of calls awaited to finish the barrier */
    private int awaitedCalls = 0;

    /** The number of calls already received */
    private int receivedCalls = 0;

    /**
     * Returns the number of awaited calls to finish the barrier
     * @return the number of awaited calls to finish the barrier
     */
    public int getAwaitedCalls() {
        return this.awaitedCalls;
    }

    /**
     * Returns the number of received calls to finish the barrier
     * @return the number of received calls to finish the barrier
     */
    public int getReceivedCalls() {
        return this.receivedCalls;
    }

    /**
     * Sets the number of calls need to finish the barrier
     * @param nbCalls the number of calls need to finish the barrier
     */
    public void setAwaitedCalls(int nbCalls) {
        this.awaitedCalls = nbCalls;
    }

    /**
     * Increments the number of received calls to finish the barrier
     */
    public void incrementReceivedCalls() {
        this.receivedCalls++;
    }
}
