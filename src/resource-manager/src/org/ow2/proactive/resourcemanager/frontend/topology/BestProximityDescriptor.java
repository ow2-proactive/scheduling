/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend.topology;

import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;


@PublicAPI
public class BestProximityDescriptor extends TopologyDescriptor {

    // pivot nodes
    protected List<Node> pivot = null;
    protected DistanceFunction function;

    public final static DistanceFunction AVG = new DistanceFunction() {
        public long distance(long d1, long d2) {
            // not connected
            if (d1 < 0 || d2 < 0)
                return -1;

            return (d1 + d2) / 2;
        }
    };

    public final static DistanceFunction MAX = new DistanceFunction() {
        public long distance(long d1, long d2) {
            // not connected
            if (d1 < 0 || d2 < 0)
                return -1;

            return Math.max(d1, d2);
        }
    };

    public final static DistanceFunction MIN = new DistanceFunction() {
        public long distance(long d1, long d2) {
            return Math.min(d1, d2);
        }
    };

    public BestProximityDescriptor() {
        super(true);
    }

    public BestProximityDescriptor(DistanceFunction function) {
        super(true);
        this.function = function;
    }

    public BestProximityDescriptor(DistanceFunction function, List<Node> pivot) {
        this(function);
        this.pivot = pivot;
    }

    public List<Node> getPivot() {
        return pivot;
    }

    public DistanceFunction getDistanceFunction() {
        return function == null ? AVG : function;
    }
}
