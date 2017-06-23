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
package org.ow2.proactive.resourcemanager.selection.topology;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import org.ow2.proactive.topology.descriptor.ArbitraryTopologyDescriptor;
import org.ow2.proactive.topology.descriptor.BestProximityDescriptor;
import org.ow2.proactive.topology.descriptor.DifferentHostsExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.MultipleHostsExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.SingleHostDescriptor;
import org.ow2.proactive.topology.descriptor.SingleHostExclusiveDescriptor;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;

import com.google.common.annotations.VisibleForTesting;


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

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // this hash map allows to quickly find nodes on a single host (much faster than from the topology).
    private HashMap<InetAddress, Set<Node>> nodesOnHost = new HashMap<>();

    // class using for pinging
    private Class<? extends Pinger> pingerClass;

    /**
     * Constructs new instance of the topology descriptor.
     * @throws ClassNotFoundException when the pinger class specified
     * in the RM configuration file is not found
     */
    @SuppressWarnings(value = "unchecked")
    public TopologyManager() throws ClassNotFoundException {
        this((Class<? extends Pinger>) Class.forName(PAResourceManagerProperties.RM_TOPOLOGY_PINGER.getValueAsString()));
    }

    @VisibleForTesting
    public TopologyManager(Class<? extends Pinger> pingerClass) {
        this.pingerClass = pingerClass;
        handlers.put(ArbitraryTopologyDescriptor.class, new ArbitraryTopologyHandler());
        handlers.put(BestProximityDescriptor.class, new BestProximityHandler());
        handlers.put(ThresholdProximityDescriptor.class, new TresholdProximityHandler());
        handlers.put(SingleHostDescriptor.class, new SingleHostHandler());
        handlers.put(SingleHostExclusiveDescriptor.class, new SingleHostExclusiveHandler());
        handlers.put(MultipleHostsExclusiveDescriptor.class, new MultipleHostsExclusiveHandler());
        handlers.put(DifferentHostsExclusiveDescriptor.class, new DifferentHostsExclusiveHandler());
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
        if (topologyDescriptor instanceof BestProximityDescriptor ||
            topologyDescriptor instanceof ThresholdProximityDescriptor) {
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
    public void addNode(Node node) {
        try {
            rwLock.writeLock().lock();

            if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
                // do not do anything if topology disabled
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Adding Node " + node.getNodeInformation().getURL() + " to topology");
            }

            InetAddress host = node.getVMInformation().getInetAddress();

            if (topology.knownHost(host)) {
                // host topology is already known
                if (logger.isDebugEnabled()) {
                    logger.debug("The topology information has been already added for node " +
                                 node.getNodeInformation().getURL());
                }
                nodesOnHost.get(host).add(node);
                return;
            }

            // unknown host => start pinging process
            NodeSet toPing = new NodeSet();
            HashMap<InetAddress, Long> hostsTopology = new HashMap<>();

            // adding one node from each host
            for (InetAddress h : nodesOnHost.keySet()) {
                // always have at least one node on each host
                if (nodesOnHost.get(h) != null && !nodesOnHost.get(h).isEmpty()) {
                    toPing.add(nodesOnHost.get(h).iterator().next());
                    hostsTopology.put(h, Long.MAX_VALUE);
                }
            }

            if (PAResourceManagerProperties.RM_TOPOLOGY_DISTANCE_ENABLED.getValueAsBoolean()) {
                hostsTopology = pingNode(node, toPing);
            }

            topology.addHostTopology(node.getVMInformation().getHostName(), host, hostsTopology);
            Set<Node> nodesList = new LinkedHashSet<>();
            nodesList.add(node);
            nodesOnHost.put(node.getVMInformation().getInetAddress(), nodesList);
        } finally {
            rwLock.writeLock().unlock();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Node " + node.getNodeInformation().getURL() + " added.");
        }

    }

    /**
     * Node is removed or down. Method removes corresponding topology information.
     */
    public void removeNode(Node node) {
        try {
            rwLock.writeLock().lock();
            if (!PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.getValueAsBoolean()) {
                // do not do anything if topology disabled
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Removing Node " + node.getNodeInformation().getURL() + " from topology");
            }

            InetAddress host = node.getVMInformation().getInetAddress();
            if (!topology.knownHost(host)) {
                logger.warn("Topology info does not exist for node " + node.getNodeInformation().getURL());
            } else {
                Set<Node> nodes = nodesOnHost.get(host);
                nodes.remove(node);
                if (nodes.isEmpty()) {
                    // no more nodes on the host
                    topology.removeHostTopology(node.getVMInformation().getHostName(), host);
                    nodesOnHost.remove(host);
                }
            }
        } finally {
            rwLock.writeLock().unlock();
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
            logger.debug(result.size() + " hosts were pinged from " + node.getNodeInformation().getURL() + " in " +
                         (System.currentTimeMillis() - timeStamp) + " ms");

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
        try {
            rwLock.readLock().lock();
            if (nodesOnHost.get(addr) != null) {
                return new LinkedHashSet<>(nodesOnHost.get(addr));
            } else {
                return null;
            }
        } finally {
            rwLock.readLock().unlock();
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

        try {
            rwLock.readLock().lock();
            return (Topology) ((TopologyImpl) topology).clone();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private NodeSet getNodeSetWithExtraNodes(Set<Node> nodes, int numberOfNodesToExtract) {
        Set<Node> main = subListLHS(nodes, 0, numberOfNodesToExtract);
        Set<Node> extra = subListLHS(nodes, numberOfNodesToExtract, nodes.size());
        NodeSet result = new NodeSet(main);
        result.setExtraNodes(extra);
        return result;
    }

    private Set<Node> subListLHS(Set<Node> nodes, int begin, int end) {
        Set<Node> result = new LinkedHashSet<>();
        if (begin > end) {
            throw new IllegalArgumentException("First index must be smaller.");
        }
        int i = 0;
        for (Node n : nodes) {
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
            try {
                rwLock.readLock().lock();
                BestProximityDescriptor descriptor = (BestProximityDescriptor) topologyDescriptor;
                // HAC is very efficient algorithm but it does not guarantee the complete solution
                logger.info("Running clustering algorithm in order to find closest nodes");
                HAC hac = new HAC(topology, null, descriptor.getDistanceFunction(), Long.MAX_VALUE);
                return new NodeSet(hac.select(number, matchedNodes));
            } finally {
                rwLock.readLock().unlock();
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
            try {
                rwLock.readLock().lock();
                ThresholdProximityDescriptor descriptor = (ThresholdProximityDescriptor) topologyDescriptor;
                logger.info("Running clustering algorithm in order to find closest nodes");
                HAC hac = new HAC(topology, null, descriptor.getDistanceFunction(), descriptor.getThreshold());
                return new NodeSet(hac.select(number, matchedNodes));
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    /**
     * The handler finds nodes on the same hosts.
     */
    private class SingleHostHandler extends TopologyHandler {
        @Override
        public NodeSet select(int number, List<Node> matchedNodes) {
            try {
                rwLock.readLock().lock();
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
            } finally {
                rwLock.readLock().unlock();
            }
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
            try {
                rwLock.readLock().lock();
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

            } finally {
                rwLock.readLock().unlock();
            }
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
                Set<Node> nodes = nodesOnHost.get(host);
                int nbNodes;
                if (nodes != null && (nbNodes = nodes.size()) >= number) {
                    // found the host with required capacity
                    // checking that all nodes are free
                    boolean busyNode = false;
                    for (Node nodeOnHost : nodes) {
                        if (!matchedNodes.contains(nodeOnHost)) {
                            busyNode = true;
                            busyHosts.add(host);
                            break;
                        }
                    }
                    // all nodes are free on host
                    if (!busyNode) {
                        // found enough nodes on the same host
                        if (nbNodes > number) {
                            // some extra nodes will be provided
                            return getNodeSetWithExtraNodes(nodes, number);
                        } else {
                            // all nodes required for computation
                            return new NodeSet(nodes);
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
            try {
                rwLock.readLock().lock();
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
            } finally {
                rwLock.readLock().unlock();
            }
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

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((address == null) ? 0 : address.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (!(obj instanceof Host))
                    return false;
                Host other = (Host) obj;
                if (address == null) {
                    if (other.address != null)
                        return false;
                } else if (!address.equals(other.address))
                    return false;
                return true;
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
            try {
                rwLock.readLock().lock();
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
                        int nbNodes = hostNodes.size();
                        if (nbNodes > 0) {
                            result.add(hostNodes.iterator().next());
                            if (nbNodes > 1) {
                                List<Node> newExtraNodes = new LinkedList<>(subListLHS(hostNodes, 1, nbNodes));
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
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
}
