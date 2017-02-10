/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
