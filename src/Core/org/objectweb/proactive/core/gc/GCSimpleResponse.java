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


public class GCSimpleResponse implements Serializable {
    protected final Activity consensusActivity;
    private final boolean hasParent;

    GCSimpleResponse(Activity consensusActivity, boolean hasParent) {
        this.consensusActivity = consensusActivity;
        this.hasParent = hasParent;
    }

    @Override
    public String toString() {
        return "GCREP[" + this.consensusActivity + "]";
    }

    Activity getConsensusActivity() {
        return this.consensusActivity;
    }

    boolean isTerminationResponse() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GCSimpleResponse) {
            return this.consensusActivity.equals(((GCSimpleResponse) o).consensusActivity);
        }
        return false;
    }

    boolean hasParent() {
        return this.hasParent;
    }
}
