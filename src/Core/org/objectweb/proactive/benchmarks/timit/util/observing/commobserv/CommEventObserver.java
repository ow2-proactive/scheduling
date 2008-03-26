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

import org.objectweb.proactive.benchmarks.timit.util.observing.EventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObservable;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;


/**
 * This class implements the StatDataObserver interface. Part of the specialized
 * Observer/Observable pattern.
 *
 * @author The ProActive Team
 */
public class CommEventObserver implements EventObserver {

    /**
     *
     */

    /** This will be the concrete StatData */
    private CommEventData commStatData;
    private String name;

    /**
     * Creates a new instance of CommEventObserver
     *
     * @param name
     *            The name of the event tag in the result xml file
     * @param groupSize
     *            The size of the group used to create the array of marked
     *            communications
     * @param subjectRank
     *            The rank of the subject in the group
     */
    public CommEventObserver(String name, int groupSize, int subjectRank) {
        this.commStatData = new CommEventData(name, groupSize, subjectRank);
        this.name = name;
    }

    /**
     * Updates the current observer ( the observable will call this method ).
     *
     * @param s
     *            The observable caller
     * @param arg
     *            Argument that contains the information
     */
    public void update(EventObservable s, Object arg) {
        if (arg instanceof CommEvent && (((CommEvent) arg).getObserver() == this)) {
            this.commStatData.mark(((CommEvent) arg).getDestRank(), ((CommEvent) arg).getValue()); // increment the value each
            // time we speak to sombody
        }
    }

    /**
     * Returns the event data associated with this observer.
     *
     * @return The CommEventData associated with this observer
     */
    public EventData getEventData() {
        return this.commStatData;
    }

    /**
     * Return the name of this event observer
     */
    public String getName() {
        return this.name;
    }
}
