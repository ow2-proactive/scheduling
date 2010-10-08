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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;


/**
 *
 * Class represents the hosts topology handled by resource manager.
 *
 */
public class TopologyImpl implements Topology, Cloneable {

    // Host -> Host -> Distance
    private HashMap<InetAddress, HashMap<InetAddress, Long>> distances = new HashMap<InetAddress, HashMap<InetAddress, Long>>();

    public Long getDistance(Node node, Node node2) {
        InetAddress host = node.getVMInformation().getInetAddress();
        InetAddress host2 = node2.getVMInformation().getInetAddress();
        return getDistance(host, host2);
    }

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

    public boolean onSameHost(Node node, Node node2) {
        return getDistance(node, node2) == 0;
    }

    public Set<InetAddress> getHosts() {
        return distances.keySet();
    }

    public void addHostTopology(InetAddress host, HashMap<InetAddress, Long> hostTopology) {
        distances.put(host, hostTopology);
    }

    public void removeHostTopology(InetAddress host) {
        distances.remove(host);
        // removing links to "host"
        for (InetAddress h : distances.keySet()) {
            if (distances.get(h).containsKey(host)) {
                distances.get(h).remove(host);
            }
        }
    }

    public HashMap<InetAddress, Long> getHostTopology(InetAddress host) {
        return distances.get(host);
    }

    public boolean knownHost(InetAddress host) {
        return distances.containsKey(host);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }
}
