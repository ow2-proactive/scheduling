/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.Cluster;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.HAC;
import org.ow2.proactive.topology.descriptor.DistanceFunction;


/**
 *
 * Class represents hosts topology handled by resource manager.
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class TopologyImpl implements Topology, Cloneable {

    private static final long serialVersionUID = 62L;

    /**
     * Host -> Hosts -> Distance
     * Store here only half of matrix - each host have distances to hosts added before.
     * Assume that they are symmetrical.
     */
    private HashMap<InetAddress, HashMap<InetAddress, Long>> distances = new HashMap<InetAddress, HashMap<InetAddress, Long>>();
    /**
     * This map is needed to store the dependency between host name and address.
     * All listeners of the RM receive only host name in events, so for them it's simpler to
     * operate with strings.
     */
    private HashMap<String, InetAddress> hosts = new HashMap<String, InetAddress>();

    /**
     * {@inheritDoc}
     */
    public Long getDistance(Node node, Node node2) {
        InetAddress host = node.getVMInformation().getInetAddress();
        InetAddress host2 = node2.getVMInformation().getInetAddress();
        return getDistance(host, host2);
    }

    /**
     * {@inheritDoc}
     */
    public Long getDistance(InetAddress host, InetAddress host2) {
        if (host.equals(host2)) {
            return new Long(0);
        } else if (distances.get(host) != null && distances.get(host).get(host2) != null) {
            return distances.get(host).get(host2);
        } else if (distances.get(host2) != null && distances.get(host2).get(host) != null) {
            return distances.get(host2).get(host);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Long getDistance(String hostName, String hostName2) {
        InetAddress host = hosts.get(hostName);
        InetAddress host2 = hosts.get(hostName2);

        if (host != null && host2 != null) {
            return getDistance(host, host2);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean onSameHost(Node node, Node node2) {
        return getDistance(node, node2) == 0;
    }

    /**
     * {@inheritDoc}
     */
    public Set<InetAddress> getHosts() {
        return distances.keySet();
    }

    /**
     * Adds host information to the topology.
     * Pass both name and address of host avoiding to do a conversion inside.
     *
     * @param hostName the name of the host
     * @param hostAddress the address of the host
     * @param hostTopology distances to other hosts
     */
    public void addHostTopology(String hostName, InetAddress hostAddress,
            HashMap<InetAddress, Long> hostTopology) {
        distances.put(hostAddress, hostTopology);
        hosts.put(hostName, hostAddress);
    }

    /**
     * Removes all information about host from the topology.
     * As it stores internally names and addresses we do not try to convert one into
     * another (as it may lead to network call) but rather pass them is parameters.
     *
     * @param hostName name of host to be removed
     * @param hostAddress host address to be removed
     */
    public void removeHostTopology(String hostName, InetAddress hostAddress) {
        distances.remove(hostAddress);
        hosts.remove(hostName);
        // removing links to "host"
        for (InetAddress h : distances.keySet()) {
            if (distances.get(h).containsKey(hostAddress)) {
                distances.get(h).remove(hostAddress);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public HashMap<InetAddress, Long> getHostTopology(InetAddress hostAddress) {
        return distances.get(hostAddress);
    }

    /**
     * {@inheritDoc}
     */
    public boolean knownHost(InetAddress hostAddress) {
        return distances.containsKey(hostAddress);
    }

    /**
     * Clones the topology object, which is uses for synchronization purposes.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<Cluster<String>> clusterize(int numberOfClusters, DistanceFunction distanceFunction) {
        HAC hac = new HAC(this, new LinkedList<Node>(), distanceFunction, Long.MAX_VALUE);
        return hac.clusterize(numberOfClusters, hosts.keySet());
    }
}
