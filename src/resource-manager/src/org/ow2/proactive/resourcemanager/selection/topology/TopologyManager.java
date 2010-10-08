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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.ArbitraryTopologyDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.BestProximityDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.MultipleHostsExclusiveDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.NodePinger;
import org.ow2.proactive.resourcemanager.frontend.topology.SingleHostDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.SingleHostExclusiveDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.ThresholdProximityDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.selection.topology.clique.CliqueFinder;
import org.ow2.proactive.resourcemanager.selection.topology.clique.OptimalCliqueFinder;
import org.ow2.proactive.resourcemanager.selection.topology.clustering.HAC;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


public class TopologyManager {

    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.TOPOLOGY);

    // hosts topology
    private Topology topology = new Topology();
    private HashMap<InetAddress, Set<Node>> nodesOnHost = new HashMap<InetAddress, Set<Node>>();

    private final HashMap<Class<? extends TopologyDescriptor>, TopologyHandler> handlers = new HashMap<Class<? extends TopologyDescriptor>, TopologyHandler>();

    public TopologyManager() {
        handlers.put(ArbitraryTopologyDescriptor.class, new ArbitraryTopologyHandler());
        handlers.put(BestProximityDescriptor.class, new BestProximityHandler());
        handlers.put(ThresholdProximityDescriptor.class, new TresholdProximityHandler());
        handlers.put(SingleHostDescriptor.class, new TresholdProximityHandler());
        handlers.put(SingleHostExclusiveDescriptor.class, new SingleHostExclusiveHandler());
        handlers.put(MultipleHostsExclusiveDescriptor.class, new MultipleHostsExclusiveHandler());
    }

    public TopologyHandler getHandler(TopologyDescriptor topologyDescriptor) {
        if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
            throw new TopologyException("Topology is disabled");
        }

        TopologyHandler handler = handlers.get(topologyDescriptor.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("Unknown descriptor type " + topologyDescriptor.getClass());
        }
        handler.setDescriptor(topologyDescriptor);
        return handler;
    }

    public synchronized void addNode(Node node) {
        if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
            // do not do anything if topology disabled
            return;
        }

        InetAddress host = node.getVMInformation().getInetAddress();
        synchronized (topology) {
            if (topology.knownHost(host)) {
                // host topology is already known
                logger.debug("The topology information has been already added for node " +
                    node.getNodeInformation().getURL());
                nodesOnHost.get(node.getVMInformation().getInetAddress()).add(node);
                return;
            }
        }

        // unknown host => start pinging process
        List<InetAddress> toPing = null;
        synchronized (topology) {
            toPing = new LinkedList<InetAddress>(topology.getHosts());
        }
        HashMap<InetAddress, Long> hostTopology = pingNode(node, toPing);
        synchronized (topology) {
            topology.addHostTopology(host, hostTopology);
            Set<Node> nodesSet = new HashSet<Node>();
            nodesSet.add(node);
            nodesOnHost.put(node.getVMInformation().getInetAddress(), nodesSet);
        }
    }

    public void removeNode(Node node) {
        if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
            // do not do anything if topology disabled
            return;
        }

        synchronized (topology) {
            InetAddress host = node.getVMInformation().getInetAddress();
            if (!topology.knownHost(host)) {
                logger.warn("Topology info does not exist for node " + node.getNodeInformation().getURL());
            } else {

                nodesOnHost.get(host).remove(node);
                if (nodesOnHost.get(host).size() == 0) {
                    // no more nodes on the host
                    topology.removeHostTopology(host);
                }
            }
        }
    }

    private HashMap<InetAddress, Long> pingNode(Node node, List<InetAddress> hosts) {

        try {
            logger.debug("Launching ping process on node " + node.getNodeInformation().getURL());
            long timeStamp = System.currentTimeMillis();
            NodePinger pinger = PAActiveObject.newActive(NodePinger.class, null, node);
            HashMap<InetAddress, Long> result = pinger.ping(hosts);
            PAFuture.waitFor(result);
            logger.debug(result.size() + " hosts were pinged from " + node.getNodeInformation().getURL() +
                " in " + (System.currentTimeMillis() - timeStamp) + " ms");

            if (logger.isDebugEnabled()) {
                logger.debug("Distances are:");
                for (InetAddress host : result.keySet()) {
                    logger.debug(result.get(host) + " to " + host);
                }
            }

            return result;
        } catch (ActiveObjectCreationException e) {
            logger.warn(e.getMessage(), e);
        } catch (NodeException e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    public Topology getTopology() {
        synchronized (topology) {
            return (Topology) topology.clone();
        }
    }

    public Long getDistance(Node node, Node node2) {
        synchronized (topology) {
            Long distance = topology.getDistance(node, node2);
            if (distance == null) {
                logger.warn("No distance between " + node.getNodeInformation().getURL() + " and " +
                    node2.getNodeInformation().getURL());
            }
            return distance;
        }
    }

    // Handler implementations
    private class ArbitraryTopologyHandler extends TopologyHandler {
        @Override
        public List<Node> select(int number, List<Node> matchedNodes) {

            if (number < matchedNodes.size()) {
                return matchedNodes.subList(0, number);
            }
            return matchedNodes;
        }
    }

    private class BestProximityHandler extends TopologyHandler {
        @Override
        public List<Node> select(int number, List<Node> matchedNodes) {
            synchronized (topology) {
                BestProximityDescriptor descriptor = (BestProximityDescriptor) topologyDescriptor;
                // HAC is very efficient algorithm but it does not guarantee the complete solution
                logger.info("Running clustering algorithm in order to find closest nodes");
                HAC hac = new HAC(descriptor.getPivot(), descriptor.getDistanceFunction());
                List<Node> hacResult = hac.select(number, matchedNodes);
                if (hacResult.size() < number && hacResult.size() < matchedNodes.size()) {
                    logger
                            .info("Switching to clique search algorithm as clustering did not produce a required node set");
                    OptimalCliqueFinder cliqueFinder = new OptimalCliqueFinder(descriptor.getPivot(),
                        Long.MAX_VALUE, descriptor.getDistanceFunction());
                    return cliqueFinder.getClique(number, matchedNodes);
                } else {
                    return hacResult;
                }
            }
        }
    }

    private class TresholdProximityHandler extends TopologyHandler {
        @Override
        public List<Node> select(int number, List<Node> matchedNodes) {
            synchronized (topology) {
                ThresholdProximityDescriptor descriptor = (ThresholdProximityDescriptor) topologyDescriptor;
                logger
                        .info("Running clique search algorithm in order to find nodes with threshold proximity " +
                            descriptor.getThreshold());
                CliqueFinder cliqueFinder = new CliqueFinder(descriptor.getPivot(), descriptor.getThreshold());
                return cliqueFinder.getClique(number, matchedNodes);
            }
        }
    }

    private class SingleHostExclusiveHandler extends TopologyHandler {
        @Override
        public List<Node> select(int number, List<Node> matchedNodes) {
            return null;
        }
    }

    private class MultipleHostsExclusiveHandler extends TopologyHandler {
        @Override
        public List<Node> select(int number, List<Node> matchedNodes) {
            return null;
        }
    }
}
