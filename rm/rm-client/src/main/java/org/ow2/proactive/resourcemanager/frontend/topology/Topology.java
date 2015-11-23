/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
public interface Topology extends Serializable {

    /**
     * Returns the distance between 2 nodes.
     *
     * @return the distance between 2 nodes
     */
    Long getDistance(Node node, Node node2);

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
     * Checks if 2 nodes are on the sane host.
     * @return true if 2 nodes are on the same hosts, false otherwise
     */
    boolean onSameHost(Node node, Node node2);

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
