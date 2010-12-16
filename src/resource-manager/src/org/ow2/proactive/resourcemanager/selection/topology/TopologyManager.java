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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDisabledException;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyImpl;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.HAC;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.Pinger;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.topology.descriptor.ArbitraryTopologyDescriptor;
import org.ow2.proactive.topology.descriptor.BestProximityDescriptor;
import org.ow2.proactive.topology.descriptor.DifferentHostsExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.MultipleHostsExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.SingleHostDescriptor;
import org.ow2.proactive.topology.descriptor.SingleHostExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;

import edu.emory.mathcs.backport.java.util.Collections;


/**
 * Class is responsible for collecting the topology information, keeping it up to date and taking it into
 * account for nodes selection.
 * 
 */
public class TopologyManager {

    // logger
    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.TOPOLOGY);

    // hosts distances
    private TopologyImpl topology = new TopologyImpl();
    // this hash map allows to quickly find nodes on a single host (much faster than from the topology).
    private HashMap<InetAddress, List<Node>> nodesOnHost = new HashMap<InetAddress, List<Node>>();
    // list of handlers corresponded to topology descriptors
    private final HashMap<Class<? extends TopologyDescriptor>, TopologyHandler> handlers = new HashMap<Class<? extends TopologyDescriptor>, TopologyHandler>();

    // class using for pinging
    private Class<? extends Pinger> pingerClass;

    /**
     * Constructs new instance of the topology descriptor.
     * @throws ClassNotFoundException when pinger class specified 
     * in the RM configuration file is not found
     */
    @SuppressWarnings(value = "unchecked")
    public TopologyManager() throws ClassNotFoundException {
        handlers.put(ArbitraryTopologyDescriptor.class, new ArbitraryTopologyHandler());
        handlers.put(BestProximityDescriptor.class, new BestProximityHandler());
        handlers.put(ThresholdProximityDescriptor.class, new TresholdProximityHandler());
        handlers.put(SingleHostDescriptor.class, new SingleHostHandler());
        handlers.put(SingleHostExclusiveDescriptor.class, new SingleHostExclusiveHandler());
        handlers.put(MultipleHostsExclusiveDescriptor.class, new MultipleHostsExclusiveHandler());
        handlers.put(DifferentHostsExclusiveDescriptor.class, new DifferentHostsExclusiveHandler());

        pingerClass = (Class<? extends Pinger>) Class.forName(PAResourceManagerProperties.RM_TOPOLOGY_PINGER
                .getValueAsString());
    }

    /**
     * Returns the handler of corresponding descriptor. HAndles contains the logic of nodes
     * selection in respect of the topology information.
     */
    public TopologyHandler getHandler(TopologyDescriptor topologyDescriptor) {
        if (topologyDescriptor.isTopologyBased() &&
            !PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
            throw new TopologyDisabledException("Topology is disabled");
        }
        TopologyHandler handler = handlers.get(topologyDescriptor.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("Unknown descriptor type " + topologyDescriptor.getClass());
        }
        handler.setDescriptor(topologyDescriptor);
        return handler;
    }

    /**
     * Updates the topology for new node. Executes the pinger on new node when this node belongs
     * to unknow host.
     */
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
                nodesOnHost.get(host).add(node);
                return;
            }
        }

        // unknown host => start pinging process
        NodeSet toPing = new NodeSet();
        synchronized (topology) {
            // adding one node from each host
            for (InetAddress h : nodesOnHost.keySet()) {
                // always have at least one node on each host
                toPing.add(nodesOnHost.get(h).iterator().next());
            }
        }

        HashMap<InetAddress, Long> hostsTopology = pingNode(node, toPing);
        synchronized (topology) {
            topology.addHostTopology(node.getVMInformation().getHostName(), host, hostsTopology);
            List<Node> nodesList = new LinkedList<Node>();
            nodesList.add(node);
            nodesOnHost.put(node.getVMInformation().getInetAddress(), nodesList);
        }
    }

    /**
     * Node is removed or down. Method removes corresponding topology information.
     */
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
                    topology.removeHostTopology(node.getVMInformation().getHostName(), host);
                    nodesOnHost.remove(host);
                }
            }
        }
    }

    /**
     * Launches the pinging process from new host. It will ping all other hosts
     * according to the pinger logic.
     */
    private HashMap<InetAddress, Long> pingNode(Node node, NodeSet nodes) {

        try {
            logger.debug("Launching ping process on node " + node.getNodeInformation().getURL());
            long timeStamp = System.currentTimeMillis();

            Pinger pinger = PAActiveObject.newActive(pingerClass, null, node);
            HashMap<InetAddress, Long> result = pinger.ping(nodes);
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

    /**
     * Returns the topology representation. As the Topology is not a thread-safe class
     * and all synchronization happens on TopologyManager level, the topology is cloned.
     */
    public Topology getTopology() {
        if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
            throw new TopologyException("Topology is disabled");
        }

        synchronized (topology) {
            return (Topology) ((TopologyImpl) topology).clone();
        }
    }

    // Handlers implementations

    /**
     * Handler for arbitrary topology descriptor, which just select a sublist
     * from given list.
     */
    private class ArbitraryTopologyHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {

            if (number < matchedNodes.size()) {
                return new NodeSet(matchedNodes.subList(0, number));
            }
            return new NodeSet(matchedNodes);
        }
    }

    /**
     * Handler finds the set of the closest nodes by running HAC algorithm.
     */
    private class BestProximityHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            synchronized (topology) {
                BestProximityDescriptor descriptor = (BestProximityDescriptor) topologyDescriptor;
                // HAC is very efficient algorithm but it does not guarantee the complete solution
                logger.info("Running clustering algorithm in order to find closest nodes");
                HAC hac = new HAC(topology, null, descriptor.getDistanceFunction(), Long.MAX_VALUE);
                return new NodeSet(hac.select(number, matchedNodes));
            }
        }
    }

    /**
     * Handler finds the set of the closest nodes by running HAC
     * algorithm with a given threshold.
     *
     * Note: initially clique search algorithm has been used
     * but the performance of searching the clique in graph
     * is not acceptable for real-time requests.
     *
     */
    private class TresholdProximityHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            synchronized (topology) {
                ThresholdProximityDescriptor descriptor = (ThresholdProximityDescriptor) topologyDescriptor;
                logger.info("Running clustering algorithm in order to find closest nodes");
                HAC hac = new HAC(topology, null, descriptor.getDistanceFunction(), descriptor.getThreshold());
                return new NodeSet(hac.select(number, matchedNodes));
            }
        }
    }

    /**
     * The handler finds nodes on the same hosts.
     */
    private class SingleHostHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            if (number == 0) {
                return new NodeSet();
            }
            if (number > matchedNodes.size()) {
                // cannot select more than matchedNodes.size()
                number = matchedNodes.size();
            }

            NodeSet result = new NodeSet();
            for (InetAddress host : nodesOnHost.keySet()) {
                if (nodesOnHost.get(host).size() >= number) {
                    // found the host with required capacity
                    // checking that all nodes are free
                    for (Node nodeOnHost : nodesOnHost.get(host)) {
                        if (matchedNodes.contains(nodeOnHost)) {
                            result.add(nodeOnHost);
                            if (result.size() == number) {
                                // found enough nodes on the same host
                                return result;
                            }
                        } else {
                            continue;
                        }
                    }
                    result.clear();
                }
            }
            return select(number - 1, matchedNodes);
        }
    }

    /**
     *  The selection handler for "single host exclusive" requests.
     *
     *  For "single host exclusive" if user requests k nodes
     *  - the machine with exact capacity will be selected if exists
     *  - the machine with bigger capacity will be selected if exists.
     *    The capacity of the selected machine will be the closest to k.
     *  - the machine with smaller capacity than k will be selected.
     *    In this case the capacity of selected host will be the biggest among all other.
     */
    private class SingleHostExclusiveHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            if (number == 0) {
                return new NodeSet();
            }

            List<InetAddress> sortedByNodesNumber = new LinkedList<InetAddress>(nodesOnHost.keySet());

            Collections.sort(sortedByNodesNumber, new Comparator<InetAddress>() {
                public int compare(InetAddress host, InetAddress host2) {
                    return nodesOnHost.get(host).size() - nodesOnHost.get(host2).size();
                }
            });

            return selectRecursively(number, sortedByNodesNumber, matchedNodes);
        }

        private NodeSet selectRecursively(int number, List<InetAddress> hostsSortedByNodesNumber,
                List<Node> matchedNodes) {

            if (number == 0) {
                return new NodeSet();
            }
            if (number > matchedNodes.size()) {
                // cannot select more than matchedNodes.size()
                number = matchedNodes.size();
            }

            List<InetAddress> busyHosts = new LinkedList<InetAddress>();
            for (InetAddress host : hostsSortedByNodesNumber) {
                if (nodesOnHost.get(host).size() >= number) {
                    // found the host with required capacity
                    // checking that all nodes are free
                    boolean busyNode = false;
                    for (Node nodeOnHost : nodesOnHost.get(host)) {
                        if (!matchedNodes.contains(nodeOnHost)) {
                            busyNode = true;
                            busyHosts.add(host);
                            break;
                        }
                    }
                    // all nodes are free on host
                    if (!busyNode) {
                        // found enough nodes on the same host
                        if (nodesOnHost.get(host).size() > number) {
                            // some extra nodes will be provided
                            List<Node> nodes = nodesOnHost.get(host);
                            NodeSet result = new NodeSet(nodes.subList(0, number));
                            result.setExtraNodes(new LinkedList<Node>(nodes.subList(number, nodes.size())));
                            return result;
                        } else {
                            // all nodes required for computation
                            return new NodeSet(nodesOnHost.get(host));
                        }
                    }
                }
            }

            hostsSortedByNodesNumber.removeAll(busyHosts);
            return selectRecursively(number - 1, hostsSortedByNodesNumber, matchedNodes);
        }
    }

    /**
     *
     * For "multiple host exclusive" request (k nodes)
     * - if one machine exists with the capacity k it will be selected
     * - if several machines give exact number of nodes they will be selected
     *   (in case of several possibilities number of machines will be minimized)
     * - if it not possible to find exact number of nodes but it's possible to
     *   find more than they will be selected. The number of waisted resources
     *   & number of machines will be minimized
     * - otherwise less nodes will be provided but as the closest as possible to k
     *
     */
    private class MultipleHostsExclusiveHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            if (number == 0) {
                return new NodeSet();
            }

            // try to find the optimal set of hosts which give us the required set
            //
            // "optimal" means the minimization of hosts while the number of nodes has to
            // be bigger but as close as possible to the requested one
            //
            // in order to do it forming the map
            // [number -> [list_1 given this nodes number]...[list_k given this nodes number]]
            HashMap<Integer, List<List<InetAddress>>> hostsMap = new HashMap<Integer, List<List<InetAddress>>>();
            for (InetAddress host : nodesOnHost.keySet()) {
                boolean busyNode = false;
                for (Node nodeOnHost : nodesOnHost.get(host)) {
                    if (!matchedNodes.contains(nodeOnHost)) {
                        busyNode = true;
                        break;
                    }
                }
                if (!busyNode) {
                    int nodesNumber = nodesOnHost.get(host).size();
                    if (nodesNumber == number) {
                        // found exactly required number of nodes on one host
                        return new NodeSet(nodesOnHost.get(host));
                    }

                    for (Integer i : new LinkedList<Integer>(hostsMap.keySet())) {
                        if (i > number) {
                            // do not updates records above the "number"
                            continue;
                        }

                        int n = i + nodesNumber;
                        if (!hostsMap.containsKey(n)) {
                            hostsMap.put(n, new LinkedList<List<InetAddress>>());
                        }
                        for (List<InetAddress> hosts : hostsMap.get(i)) {
                            List<InetAddress> list = new LinkedList<InetAddress>(hosts);
                            list.add(host);
                            hostsMap.get(n).add(list);
                        }

                    }

                    if (!hostsMap.containsKey(nodesNumber)) {
                        hostsMap.put(nodesNumber, new LinkedList<List<InetAddress>>());
                        hostsMap.get(nodesNumber).add(Collections.singletonList(host));
                    }
                }
            }

            if (hostsMap.size() == 0) {
                return new NodeSet();
            }

            // looking for the index we are going to use in the map
            // it should be either the smallest index >= number or if there is no such index
            // the closest bigger one
            int index = -1;
            for (Integer i : hostsMap.keySet()) {
                if (i >= number) {
                    if (index < number || i < index) {
                        index = i;
                    }
                } else if (i < number) {
                    if (i > index) {
                        index = i;
                    }
                }
            }

            // selecting the list with the smallest size
            List<InetAddress> minSizeList = null;
            for (List<InetAddress> hosts : hostsMap.get(index)) {
                if (minSizeList == null || minSizeList != null && minSizeList.size() > hosts.size()) {
                    minSizeList = hosts;
                }
            }
            NodeSet hostsNodes = new NodeSet();
            for (InetAddress host : minSizeList) {
                hostsNodes.addAll(nodesOnHost.get(host));
            }

            if (hostsNodes.size() <= number) {
                // no extra nodes
                return hostsNodes;
            } else {
                // more nodes found than needed, put them into the extra nodes list
                NodeSet result = new NodeSet(hostsNodes.subList(0, number));
                result.setExtraNodes(new LinkedList<Node>(hostsNodes.subList(number, hostsNodes.size())));
                return result;
            }
        }
    }

    /**
     * 
     * If k nodes are requested
     * - trying to find hosts with 1 node
     * - if there are no more such hosts add hosts with two nodes and so on.
     * 
     */
    private class DifferentHostsExclusiveHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            if (number == 0) {
                return new NodeSet();
            }

            // create the map of free hosts: nodes_number -> list of hosts
            HashMap<Integer, List<InetAddress>> hostsMap = new HashMap<Integer, List<InetAddress>>();
            for (InetAddress host : nodesOnHost.keySet()) {
                boolean busyNode = false;
                for (Node nodeOnHost : nodesOnHost.get(host)) {
                    if (!matchedNodes.contains(nodeOnHost)) {
                        busyNode = true;
                        break;
                    }
                }
                if (!busyNode) {
                    int nodesNumber = nodesOnHost.get(host).size();
                    if (!hostsMap.containsKey(nodesNumber)) {
                        hostsMap.put(nodesNumber, new LinkedList<InetAddress>());
                    }
                    hostsMap.get(nodesNumber).add(host);
                }
            }

            // if empty => no entirely free hosts
            if (hostsMap.size() == 0) {
                return new NodeSet();
            }

            // sort by nodes number and accumulate the result
            List<Integer> sortedCapacities = new LinkedList<Integer>(hostsMap.keySet());
            Collections.sort(sortedCapacities);

            NodeSet result = new NodeSet();
            for (Integer i : sortedCapacities) {
                for (InetAddress host : hostsMap.get(i)) {
                    List<Node> hostNodes = nodesOnHost.get(host);
                    result.add(hostNodes.get(0));
                    if (hostNodes.size() > 1) {
                        result.setExtraNodes(new LinkedList<Node>(hostNodes.subList(1, hostNodes.size())));
                    }
                    if (--number <= 0) {
                        // found required node set
                        return result;
                    }
                }
            }
            // best effort: return less than needed
            return result;
        }
    }
}
