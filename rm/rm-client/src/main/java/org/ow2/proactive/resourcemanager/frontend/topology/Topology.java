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
package org.ow2.proactive.resourcemanager.frontend.topology;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.Cluster;
import org.ow2.proactive.topology.descriptor.DistanceFunction;


/**
 *
 * Interface represents hosts topology handled by resource manager.
 * Users may receive the topology information using {@code ResourceManager.getTopology()} method.
 */
@PublicAPI
public interface Topology extends TopologyInfo {

    /**
     * Returns the distance between 2 nodes.
     *
     * @return the distance between 2 nodes
     */
    Long getDistance(Node node, Node node2);

    /**
     * Checks if 2 nodes are on the sane host.
     * @return true if 2 nodes are on the same hosts, false otherwise
     */
    boolean onSameHost(Node node, Node node2);

}
