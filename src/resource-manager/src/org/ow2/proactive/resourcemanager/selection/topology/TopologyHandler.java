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
package org.ow2.proactive.resourcemanager.selection.topology;

import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;


/**
 * The base class for handlers which are associated to
 * the particular topology descriptor and implement the
 * semantic of the request.
 */
public abstract class TopologyHandler {

    protected TopologyDescriptor topologyDescriptor;

    /**
     * Selects nodes according to the topology descriptor.
     *
     * @param number of needed nodes
     * @param matchedNodes list of "free" nodes satisfied to the selection scripts
     * @return list of nodes located according to the specified topology
     */
    public abstract NodeSet select(int number, List<Node> matchedNodes);

    /**
     * Sets the descriptor.
     * @param topologyDescriptor a new descriptor to be set
     */
    public void setDescriptor(TopologyDescriptor topologyDescriptor) {
        this.topologyDescriptor = topologyDescriptor;
    }
}
