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
package org.objectweb.proactive.core.gc;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * An Activity is an instance of the Lamport clock. Instances of this class
 * are immutable.
 */
public class Activity implements Serializable {

    /**
     * Who increased the counter?
     */
    private final UniqueID bodyID;

    /**
     * The increasing counter
     */
    private final long activityCounter;

    Activity(UniqueID bodyID, long activityCounter) {
        this.bodyID = bodyID;
        this.activityCounter = activityCounter;
    }

    /**
     * Compare the counter, then the ID
     */
    boolean strictlyMoreRecentThan(Activity a) {
        if (this.activityCounter != a.activityCounter) {
            return this.activityCounter > a.activityCounter;
        }

        return this.bodyID.getCanonString().compareTo(a.bodyID.getCanonString()) > 0;
    }

    @Override
    public String toString() {
        return bodyID.shortString() + ":" + activityCounter;
    }

    /**
     * Same counter and ID
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Activity) {
            Activity a = (Activity) o;
            return (this.activityCounter == a.activityCounter) &&
            this.bodyID.equals(a.bodyID);
        }
        return false;
    }

    UniqueID getBodyID() {
        return this.bodyID;
    }

    long getCounter() {
        return this.activityCounter;
    }
}
