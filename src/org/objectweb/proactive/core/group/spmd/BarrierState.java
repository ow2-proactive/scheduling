/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.group.spmd;

import java.io.Serializable;


/**
 * This class describes the state of a barrier.
 * 
 * @author Laurent Baduel
 */
public class BarrierState implements Serializable {

    /** The number of calls awaited to finish the barrier */
    private int awaitedCalls = 0;

    /** The number of calls already received */
    private int receivedCalls = 0;

    /** The local call is arrived ? */
    private boolean localyCalled = false;

    /**
     * Constructor
     */
    public BarrierState() {
    }

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

    /**
     * Set the localy call state to <code>true</code>
     */
    public void tagLocalyCalled() {
        this.localyCalled = true;
    }

    /**
     * Return <code>true</code> if the local barrier call was performed
     * @return <code>true</code> if the local barrier call was performed, else return <code>false</code>
     */
    public boolean isLocalyCalled() {
        return this.localyCalled;
    }
}
