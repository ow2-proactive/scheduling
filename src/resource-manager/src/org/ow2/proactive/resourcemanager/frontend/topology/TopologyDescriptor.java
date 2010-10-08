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

import java.io.Serializable;
import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class TopologyDescriptor implements Serializable {

    // default descriptors without start point
    public static final TopologyDescriptor ARBITRARY = new ArbitraryTopologyDescriptor();
    public static final TopologyDescriptor BEST_PROXIMITY = new BestProximityDescriptor(
        BestProximityDescriptor.AVG);
    public static final TopologyDescriptor SINGLE_HOST = new SingleHostDescriptor();
    public static final TopologyDescriptor SINGLE_HOST_EXCLUSIVE = new SingleHostExclusiveDescriptor();
    public static final TopologyDescriptor MULTIPLE_HOSTS_EXCLUSIVE = new MultipleHostsExclusiveDescriptor();

    private boolean greedy = true;

    protected TopologyDescriptor(boolean greedy) {
        this.greedy = greedy;
    }

    public boolean isGreedy() {
        return greedy;
    }
}
