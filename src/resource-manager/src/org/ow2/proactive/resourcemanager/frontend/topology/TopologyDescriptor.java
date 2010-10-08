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
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;


/**
 *
 * Class represents the descriptor of the desired nodes topology
 * which could be used with {@link ResourceManager.getAtMostNodes} request.
 *
 * User may create his own instance id defined descriptors and parameterize it or
 * use one of predefined constant instances when it is sufficient.
 */
@PublicAPI
public class TopologyDescriptor implements Serializable {

    /** no constraint on node location */
    public static final TopologyDescriptor ARBITRARY = new ArbitraryTopologyDescriptor();
    /** the set of closest nodes */
    public static final TopologyDescriptor BEST_PROXIMITY = new BestProximityDescriptor(
        BestProximityDescriptor.AVG);
    /** the set of nodes on a single host */
    public static final TopologyDescriptor SINGLE_HOST = new SingleHostDescriptor();
    /**
     * the set of nodes of a single host exclusively
     * (the host is reserved for the user)
     */
    public static final TopologyDescriptor SINGLE_HOST_EXCLUSIVE = new SingleHostExclusiveDescriptor();
    /**
     * the set of nodes of several hosts exclusively
     * (hosts are reserved for the user)
     */
    public static final TopologyDescriptor MULTIPLE_HOSTS_EXCLUSIVE = new MultipleHostsExclusiveDescriptor();

    private boolean greedy = true;

    /**
     * Creates the descriptor
     * @param greedy defines if selection scripts have to be executed on all
     * available nodes before the topology information will be processed.
     */
    protected TopologyDescriptor(boolean greedy) {
        this.greedy = greedy;
    }

    /**
     * Defines if selection scripts have to be executed on all
     * available nodes before the topology information will be processed.
     *
     * @return true in case of greedy behavior false otherwise
     */
    public boolean isGreedy() {
        return greedy;
    }
}
