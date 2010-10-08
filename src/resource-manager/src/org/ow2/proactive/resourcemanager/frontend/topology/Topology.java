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
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;


/**
 *
 * Interface represents the hosts topology handled by resource manager.
 *
 */
@PublicAPI
public interface Topology extends Serializable {

    public Long getDistance(Node node, Node node2);

    public Long getDistance(InetAddress hostAddress, InetAddress hostAddress2);

    public Long getDistance(String hostName, String hostName2);

    public boolean onSameHost(Node node, Node node2);

    public boolean knownHost(InetAddress hostAddress);

    public Set<InetAddress> getHosts();

    public void addHostTopology(String hostName, InetAddress hostAddress,
            HashMap<InetAddress, Long> hostTopology);

    public void removeHostTopology(String hostName, InetAddress hostAddress);

    public HashMap<InetAddress, Long> getHostTopology(InetAddress hostAddress);
}
