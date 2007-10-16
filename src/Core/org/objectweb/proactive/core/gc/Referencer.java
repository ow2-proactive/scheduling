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

public class Referencer {

    /**
     * The last activity we gave it in a GC response
     */
    private Activity givenActivity;

    /**
     * Whether the referenced agreed on the last activity it gave us.
     * Note that this last activity can be different than this.givenActivity
     * in which case, it will obviously disagree with the consensus.
     */
    private boolean consensus;

    /**
     * When did we receive the latest GC message from this referencer?
     */
    private long lastMessageTimestamp;

    /**
     * Did we notify this referencer that we were in a dead cycle?
     */
    private boolean notifiedCycle;

    Referencer() {
        this.notifiedCycle = false;
    }

    long getLastMessageTimestamp() {
        return this.lastMessageTimestamp;
    }

    void setLastGCMessage(GCSimpleMessage mesg) {
        this.consensus = mesg.getConsensus();
        this.lastMessageTimestamp = System.currentTimeMillis();
    }

    void setGivenActivity(Activity activity) {
        if (!activity.equals(this.givenActivity)) {
            this.givenActivity = activity;
            this.consensus = false;
        }
    }

    boolean getConsensus(Activity activity) {
        if (activity.equals(this.givenActivity)) {
            return this.consensus;
        }

        return false;
    }

    boolean isNotifiedCycle() {
        return this.notifiedCycle;
    }

    void setNotifiedCycle() {
        this.notifiedCycle = true;
    }
}
