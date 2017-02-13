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
package org.ow2.proactive.resourcemanager.frontend.topology.pinging;

import java.net.InetAddress;
import java.util.HashMap;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.utils.NodeSet;


/**
 * Pings ProActive nodes using Node.getNumberOfActiveObjects().
 */
public class NodesPinger implements Pinger {

    /**
     * Pings remote nodes and returns distances to hosts where these nodes are located.
     *
     * @param nodes to ping
     * @return distances map to hosts where these nodes are located
     */
    public HashMap<InetAddress, Long> ping(NodeSet nodes) {
        HashMap<InetAddress, Long> results = new HashMap<>();
        for (Node node : nodes) {
            try {
                InetAddress current = NodeFactory.getDefaultNode().getVMInformation().getInetAddress();
                InetAddress nodeAddress = node.getVMInformation().getInetAddress();
                if (current.equals(nodeAddress)) {
                    // nodes on the same host
                    results.put(nodeAddress, new Long(0));
                } else {
                    results.put(nodeAddress, pingNode(node));
                }
            } catch (NodeException e) {
            }
        }
        return results;
    }

    /**
     * Method pings the node by calling Node.getNumberOfActiveObjects()
     * several time.
     *
     * @param node to ping
     * @return ping value to this node from the one it is executing on
     */
    private Long pingNode(Node node) {

        final int ATTEMPS = 10;
        long minPing = Long.MAX_VALUE;
        for (int i = 0; i < ATTEMPS; i++) {
            long start = System.nanoTime();
            try {
                node.getNumberOfActiveObjects();
            } catch (Exception e) {
                // cannot reach the node
                return (long) -1;
            }

            // microseconds
            long ping = (System.nanoTime() - start) / 1000;
            if (ping < minPing)
                minPing = ping;
        }

        return minPing;
    }
}
