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
package org.objectweb.proactive.benchmarks.timit.util.observing.commobserv;

import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;


/**
 * A communication event represented by the triplet (observer, destination rank,
 * value).<br>
 * Must be interpreted like the
 * <li>observer</li>
 * registers the communicated
 * <li>value</li>
 * to the
 * <li>destination rank</li>.
 *
 * @see org.objectweb.proactive.benchmarks.timit.util.observing.Event
 * @author The ProActive Team
 */
public class CommEvent extends Event {

    /** The destination rank */
    private int destRank;

    /**
     * Creates an instance of CommEvent.
     *
     * @param observer
     *            The observer that registers this event
     * @param destRank
     *            The destination rank
     * @param value
     *            The communicated value
     */
    public CommEvent(EventObserver observer, int destRank, double value) {
        super(observer, value);
        this.destRank = destRank;
    }

    /**
     * Returns the destination rank.
     *
     * @return The destination rank
     */
    public int getDestRank() {
        return this.destRank;
    }

    /**
     * Sets the destination rank.
     *
     * @param destRank
     *            The destination rank
     */
    public void setDestRank(int destRank) {
        this.destRank = destRank;
    }
}
