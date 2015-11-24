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
package org.ow2.proactive.resourcemanager.selection.topology;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyDisabledException;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyImpl;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.HAC;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.Pinger;
import org.ow2.proactive.topology.descriptor.*;
import org.ow2.proactive.utils.NodeSet;

import java.net.InetAddress;
import java.util.*;


/**
 * Class is responsible for collecting the topology information, keeping it up to date and taking it into
 * account for nodes selection.
 * 
 */
public class TopologyManager {

    // logger
    private final static Logger logger = Logger.getLogger(TopologyManager.class);
    // list of handlers corresponded to topology descriptors
    private final HashMap<Class<? extends TopologyDescriptor>, TopologyHandler> handlers = new HashMap<>();
    // hosts distances
    private final TopologyImpl topology = new TopologyImpl();
    // this hash map allows to quickly find nodes on a single host (much faster than from the topology).
    private HashMap<InetAddress, Set<Node>> nodesOnHost = new HashMap<>();
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
        if (topologyDescriptor instanceof BestProximityDescriptor || topologyDescriptor instanceof ThresholdProximityDescriptor) {
            if (!PAResourceManagerProperties.RM_TOPOLOGY_DISTANCE_ENABLED.getValueAsBoolean()) {
                throw new TopologyDisabledException("Topology distance is disabled, cannot use distance-based descriptors");
            }
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
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Node " + node.getNodeInformation().getURL() + " to topology");
        }

        InetAddress host = node.getVMInformation().getInetAddress();
        synchronized (topology) {
            if (topology.knownHost(host)) {
                // host topology is already known
                if (logger.isDebugEnabled()) {
                    logger.debug("The topology information has been already added for node " +
                            node.getNodeInformation().getURL());
                }
                nodesOnHost.get(host).add(node);
                return;
            }
        }

        // lock the current node while pinging
        synchronized (node.getNodeInformation().getURL().intern()) {
            // unknown host => start pinging process
            NodeSet toPing = new NodeSet();
            HashMap<InetAddress, Long> hostsTopology = new HashMap<>();

            synchronized (topology) {
                // adding one node from each host
                for (InetAddress h : nodesOnHost.keySet()) {
                    // always have at least one node on each host
                    if (nodesOnHost.get(h) != null && !nodesOnHost.get(h).isEmpty()) {
                        toPing.add(nodesOnHost.get(h).iterator().next());
                        hostsTopology.put(h, Long.MAX_VALUE);
                    }
                }
            }


            if (PAResourceManagerProperties.RM_TOPOLOGY_DISTANCE_ENABLED.getValueAsBoolean()) {
                hostsTopology = pingNode(node, toPing);
            }

            synchronized (topology) {
                topology.addHostTopology(node.getVMInformation().getHostName(), host, hostsTopology);
                Set<Node> nodesList = new LinkedHashSet<>();
                nodesList.add(node);
                nodesOnHost.put(node.getVMInformation().getInetAddress(), nodesList);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Node " + node.getNodeInformation().getURL() + " added.");
            }
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
        if (logger.isDebugEnabled()) {
            logger.debug("Removing Node " + node.getNodeInformation().getURL() + " from topology");
        }

        // if the node we're trying to remove is in process of pinging => wait
        synchronized (node.getNodeInformation().getURL().intern()) {
            synchronized (topology) {
                InetAddress host = node.getVMInformation().getInetAddress();
                if (!topology.knownHost(host)) {
                    logger
                            .warn("Topology info does not exist for node " +
                                node.getNodeInformation().getURL());
                } else {
                    nodesOnHost.get(host).remove(node);
                    if (nodesOnHost.get(host).isEmpty()) {
                        // no more nodes on the host
                        topology.removeHostTopology(node.getVMInformation().getHostName(), host);
                        nodesOnHost.remove(host);
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Node " + node.getNodeInformation().getURL() + " removed.");
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

            try {
                PAActiveObject.terminateActiveObject(pinger, true);
            } catch (RuntimeException e) {
                logger.error("Cannot kill the pinger active object", e);
            }

            return result;
        } catch (ActiveObjectCreationException e) {
            logger.warn(e.getMessage(), e);
        } catch (NodeException e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    public Set<Node> getNodesOnHost(InetAddress addr) {
        synchronized (topology) {
            if (nodesOnHost.get(addr) != null) {
                return new LinkedHashSet<>(nodesOnHost.get(addr));
            } else {
                return null;
            }
        }
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

    private NodeSet getNodeSetWithExtraNodes(Set<Node> nodes, int number) {
        Set<Node> main = new LinkedHashSet<>();
        Set<Node> extra = new LinkedHashSet<>();
        int i = 0;
        for (Node n : nodes) {
            if (i < number) {
                main.add(n);
            } else {
                extra.add(n);
            }
            i++;
        }
        NodeSet result = new NodeSet(main);
        result.setExtraNodes(extra);
        return result;
    }

    private Set<Node> subListLHS(Set<Node> lhs, int begin, int end) {
        Set<Node> result = new LinkedHashSet<>();
        if (begin > end) {
            throw new IllegalArgumentException("First index must be smaller.");
        }
        int i = 0;
        for (Node n : lhs) {
            if ((i >= begin) && (i < end)) {
                result.add(n);
            }
            i++;
        }
        return result;
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
            if (number <= 0 || matchedNodes.size() == 0) {
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
            if (number <= 0 || matchedNodes.size() == 0) {
                return new NodeSet();
            }

            List<InetAddress> sortedByNodesNumber = new LinkedList<>(nodesOnHost.keySet());

            Collections.sort(sortedByNodesNumber, new Comparator<InetAddress>() {
                public int compare(InetAddress host, InetAddress host2) {
                    return nodesOnHost.get(host).size() - nodesOnHost.get(host2).size();
                }
            });

            return selectRecursively(number, sortedByNodesNumber, matchedNodes);
        }

        private NodeSet selectRecursively(int number, List<InetAddress> hostsSortedByNodesNumber,
                List<Node> matchedNodes) {

            if (number <= 0 || matchedNodes.size() == 0) {
                return new NodeSet();
            }
            if (number > matchedNodes.size()) {
                // cannot select more than matchedNodes.size()
                number = matchedNodes.size();
            }

            List<InetAddress> busyHosts = new LinkedList<>();
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
                            Set<Node> nodes = nodesOnHost.get(host);
                            return getNodeSetWithExtraNodes(nodes, number);
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
            if (number <= 0 || matchedNodes.size() == 0) {
                return new NodeSet();
            }

            // first we need to understand which hosts have busy nodes and filter them out
            // building a map from matched nodes: host -> "number of matched nodes"
            HashMap<InetAddress, Integer> matchedHosts = new HashMap<>();
            for (Node matchedNode : matchedNodes) {
                InetAddress host = matchedNode.getVMInformation().getInetAddress();
                if (matchedHosts.containsKey(host)) {
                    matchedHosts.put(host, matchedHosts.get(host) + 1);
                } else {
                    matchedHosts.put(host, 1);
                }
            }

            // freeHosts contains hosts sorted by nodes number and allows
            // to quickly find a host with given number of nodes (or closest if it is not in the tree)
            TreeSet<Host> freeHosts = new TreeSet<>();
            // if a host in matchedHosts map has the same number of nodes
            // as in nodesOnHost map it means there no busy nodes on this host
            for (InetAddress matchedHost : matchedHosts.keySet()) {
                if (!nodesOnHost.containsKey(matchedHost)) {
                    // should not be here
                    throw new TopologyException("Inconsitent topology state");
                }
                if (nodesOnHost.get(matchedHost).size() == matchedHosts.get(matchedHost)) {
                    // host has no busy nodes
                    freeHosts.add(new Host(matchedHost, matchedHosts.get(matchedHost)));
                }
            }

            // selecting nodes recursively taking on each step the host closest to the required node number
            return selectRecursively(number, freeHosts);
        }

        private NodeSet selectRecursively(int number, TreeSet<Host> freeHosts) {
            if (number <= 0 || freeHosts.size() == 0) {
                return new NodeSet();
            }

            // freeHosts is sorted based on nodes number
            // get the host with nodes number closest to the "number" (but smaller if possible)
            // complexity is log(n)
            InetAddress closestHost = removeClosest(number, freeHosts);

            Set<Node> nodes = nodesOnHost.get(closestHost);
            if (nodes.size() > number) {
                return getNodeSetWithExtraNodes(nodes, number);
            } else {
                NodeSet curNodes = new NodeSet(nodes);
                NodeSet result = selectRecursively(number - nodes.size(), freeHosts);
                result.addAll(curNodes);
                return result;
            }
        }

        private InetAddress removeClosest(int target, TreeSet<Host> freeHosts) {
            // search for element with target+1 nodes as the result is strictly less
            SortedSet<Host> headSet = freeHosts.headSet(new Host(null, target + 1));
            Host host = null;
            if (headSet.size() == 0) {
                // take the largest element from the tree
                host = freeHosts.last();
            } else {
                host = headSet.last();
            }
            freeHosts.remove(host);
            return host.address;
        }

        private class Host implements Comparable<Host> {
            private InetAddress address;
            private int nodesNumber;

            public Host(InetAddress address, int nodesNumber) {
                this.address = address;
                this.nodesNumber = nodesNumber;
            }

            public boolean equals(Object obj) {
                if (obj instanceof Host) {
                    Host host = (Host) obj;
                    if (address == null && host.address == null) {
                        return true;
                    } else if (address != null && host.address != null) {
                        return address.equals(host.address);
                    }
                }
                return false;
            }

            public int compareTo(Host host) {
                boolean equal = equals(host);
                if (equal) {
                    return 0;
                } else {
                    // must not return 0 in order to comply with equals()
                    int nodesDiff = nodesNumber - host.nodesNumber;
                    if (nodesDiff == 0) {
                        // the same node number, use addresses to define what is bigger
                        String thisAdd = address == null ? "" : address.toString();
                        String hostAdd = host.address == null ? "" : host.address.toString();
                        return thisAdd.compareTo(hostAdd);
                    }
                    return nodesDiff;
                }
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
            if (number <= 0 || matchedNodes.size() == 0) {
                return new NodeSet();
            }

            // create the map of free hosts: nodes_number -> list of hosts
            HashMap<Integer, List<InetAddress>> hostsMap = new HashMap<>();
            for (InetAddress host : nodesOnHost.keySet()) {
                boolean busyNode = false;
                for (Node nodeOnHost : nodesOnHost.get(host)) {
                    // TODO: this is n^2 complexity. Change it as in MultipleHostsExclusiveHandler
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
            List<Integer> sortedCapacities = new LinkedList<>(hostsMap.keySet());
            Collections.sort(sortedCapacities);

            NodeSet result = new NodeSet();
            for (Integer i : sortedCapacities) {
                for (InetAddress host : hostsMap.get(i)) {
                    Set<Node> hostNodes = nodesOnHost.get(host);
                    int sz = hostNodes.size();
                    if (sz > 0) {
                        result.add(hostNodes.iterator().next());
                        if (sz > 1) {
                            List<Node> newExtraNodes = new LinkedList<>(subListLHS(hostNodes,
                                    1, sz));
                            if (result.getExtraNodes() == null) {
                                result.setExtraNodes(new LinkedList<Node>());
                            }
                            result.getExtraNodes().addAll(newExtraNodes);
                        }
                        if (--number <= 0) {
                            // found required node set
                            return result;
                        }
                    }
                }
            }
            // best effort: return less than needed
            return result;
        }
    }
}
