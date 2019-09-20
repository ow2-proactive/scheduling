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

import org.ow2.proactive.resourcemanager.frontend.topology.clustering.Cluster;
import org.ow2.proactive.topology.descriptor.DistanceFunction;


public interface TopologyInfo extends Serializable {

    /**
     * Returns the distance between 2 hosts identified by their inet addresses.
     *
     * @return the distance between 2 nodes
     */
    Long getDistance(InetAddress hostAddress, InetAddress hostAddress2);

    /**
     * Returns the distance between 2 hosts identified by their domain names.
     *
     * @return the distance between 2 nodes
     */
    Long getDistance(String hostName, String hostName2);

    /**
     * Checks if the information about host is presented in the topology records.
     * @param hostAddress the address of the host
     * @return true if the host is known, false otherwise
     */
    boolean knownHost(InetAddress hostAddress);

    /**
     * Gets the set of hosts handled by resource manager.
     *
     * @return the set of hosts handled by resource manager
     */
    Set<InetAddress> getHosts();

    /**
     * Gets the distances associated to the host.
     * Note that it does not return information about all hosts handled by the resource manager
     * but rather the information about hosts added before the one specified.
     *
     * @param hostAddress - inet address of the host
     * @return distances map to nodes added before this one
     */
    HashMap<InetAddress, Long> getHostTopology(InetAddress hostAddress);

    /**
     * Clustirizes hosts into clusters based on their proximity
     *
     * @param numberOfClusters the number of clusters to produce
     * @param distanceFunction the function for distances recalculation
     * @return the list of host clusters
     */
    List<Cluster<String>> clusterize(int numberOfClusters, DistanceFunction distanceFunction);

}
