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
package org.ow2.proactive.resourcemanager.selection.topology.clustering;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.frontend.topology.DistanceFunction;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 *
 * Implementation of "Hierarchical Agglomerative Clustering"
 * For details see
 * http://en.wikipedia.org/wiki/Cluster_analysis#Agglomerative_hierarchical_clustering
 *
 */
public class HAC {

    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.TOPOLOGY);
    private final List<Node> pivot;
    private DistanceFunction distanceFunction;
    private long threshold;

    public HAC(List<Node> pivot, DistanceFunction distanceFunction, long threshold) {

        this.pivot = pivot == null ? new LinkedList<Node>() : pivot;
        this.distanceFunction = distanceFunction;
        this.threshold = threshold;

        // check that all distances for pivot nodes are presented
        for (Node node : this.pivot) {
            for (Node anotherNode : this.pivot) {
                if (node.equals(anotherNode))
                    continue;
                if (getDistance(node, anotherNode) != null) {
                    continue;
                } else {
                    throw new TopologyException("No distances found between pivot nodes " +
                        node.getNodeInformation().getURL() + " and " +
                        anotherNode.getNodeInformation().getURL());
                }
            }
        }
    }

    /**
     * Selects a set of closest nodes from specified list.
     * The proximity between nodes is defined by distance function.
     *
     * @param number desired nodes number
     * @param from list of "free" nodes
     * @return list of nodes to be provided to the client
     */
    public List<Node> select(int number, List<Node> from) {
        // initializing cluster distances map
        // cluster is a group of nodes, initially each cluster consist of one node
        logger.debug("Initializing clusters map");
        HashMap<Cluster, HashMap<Cluster, Long>> clusterDistances = initClusterDistances(from);

        // no topology information for provided nodes
        if (from.size() > 0 && clusterDistances.size() == 0) {
            throw new TopologyException("Topology information is not available");
        }

        Cluster target = null;
        if (pivot.size() > 0) {
            // fixed orientation clustering
            Iterator<Node> it = pivot.iterator();
            target = new Cluster(it.next());
            // merging pivot nodes into one cluster and recalculating distances
            logger.debug("Merging pivot nodes into one cluster");
            while (it.hasNext()) {
                // merging clusters and recalculating distances between others
                target = recalculateDistances(new Cluster[] { target, new Cluster(it.next()) },
                        clusterDistances);
            }

            // clustering centralized to the pivot
            logger.debug("Begin centralized hierarchical agglomerative clustering");
            while (clusterDistances.size() > 1 && target.getSize() < (number + pivot.size())) {
                Cluster closest = findClosestClustersTo(target, clusterDistances);

                if (closest == null) {
                    // no clusters found => cannot merge anything => stop where we are
                    break;
                }
                // merging clusters and recalculating distances between others
                target = recalculateDistances(new Cluster[] { target, closest }, clusterDistances);
            }

            // removing pivot nodes from the result
            target.remove(pivot);
        } else {
            logger.debug("Begin hierarchical agglomerative clustering");
            target = clusterDistances.keySet().iterator().next();
            // floating clustering
            while (clusterDistances.size() > 1) {
                // finding two clusters to merge according
                Cluster[] clustersToMerge = findClosestClusters(clusterDistances);
                if (clustersToMerge == null) {
                    // there is no clusters close to each other
                    // stop the process
                    break;
                }
                // merging clusters and recalculating distances between others
                target = recalculateDistances(clustersToMerge, clusterDistances);

                if (target.getSize() == number) {
                    // found all the nodes we need
                    break;
                } else if (target.getSize() > number) {
                    // found more nodes that we need,
                    // target cluster contains all nodes from another cluster

                    logger.debug("Number of node in the cluster exceeded required node number " +
                        target.getSize() + " vs " + number);

                    Cluster anotherCluster = clustersToMerge[0] == target ? clustersToMerge[1]
                            : clustersToMerge[0];
                    target.removeLast(anotherCluster.getSize());
                    final Cluster finalTarget = target;

                    Comparator<Node> nodeDistanceComparator = new Comparator<Node>() {
                        public int compare(Node n1, Node n2) {
                            long res = getDistance(n1, finalTarget) - getDistance(n2, finalTarget);
                            if (res < 0) {
                                return -1;
                            } else if (res > 0) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    };
                    // sorting nodes in the smaller cluster according to their distances to target
                    Collections.sort(anotherCluster.getNodes(), nodeDistanceComparator);

                    int neededNodesNumber = number - target.getSize();
                    target.add(anotherCluster.getNodes().subList(0, neededNodesNumber));
                    break;
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + target.getSize() + " nodes out of " + number + ": " + target);
        }
        return target.getNodes();
    }

    private long getDistance(Node from, Cluster to) {
        long globalDistance = 0;
        for (Node n : to.getNodes()) {
            long distance = getDistance(from, n);
            globalDistance = distanceFunction.distance(globalDistance, distance);
        }
        return globalDistance;
    }

    protected Long getDistance(Node node, Node node2) {
        return RMCore.topologyManager.getDistance(node, node2);
    }

    private HashMap<Cluster, HashMap<Cluster, Long>> initClusterDistances(List<Node> from) {
        if (pivot.size() > 0) {
            from = new LinkedList<Node>(from);
            for (Node piv : pivot) {
                if (!from.contains(piv))
                    from.add(piv);
            }
        }

        HashMap<Cluster, HashMap<Cluster, Long>> clusterDistances = new HashMap<Cluster, HashMap<Cluster, Long>>();

        for (Node node : from) {
            Cluster newCluster = new Cluster(node);
            HashMap<Cluster, Long> dist = new HashMap<Cluster, Long>();
            for (Cluster cluster : clusterDistances.keySet()) {
                dist.put(cluster, getDistance(node, cluster.getNodes().get(0)));
            }
            clusterDistances.put(newCluster, dist);
        }
        return clusterDistances;
    }

    private Cluster[] findClosestClusters(HashMap<Cluster, HashMap<Cluster, Long>> curDistances) {

        Cluster[] res = null;
        long proximity = threshold;

        for (Cluster a : curDistances.keySet()) {
            for (Cluster b : curDistances.get(a).keySet()) {
                if (a.equals(b)) {
                    continue;
                }

                Long distance = curDistances.get(a).get(b);
                if (distance >= 0 && distance <= proximity) {
                    res = new Cluster[] { a, b };
                    proximity = curDistances.get(a).get(b);
                }
            }
        }

        return res;
    }

    private Cluster findClosestClustersTo(Cluster cluster,
            HashMap<Cluster, HashMap<Cluster, Long>> curDistances) {

        Cluster[] res = null;
        long proximity = threshold;

        for (Cluster a : curDistances.keySet()) {
            for (Cluster b : curDistances.get(a).keySet()) {
                if (a.equals(b) || (!a.equals(cluster) && !b.equals(cluster))) {
                    continue;
                }

                Long distance = curDistances.get(a).get(b);
                if (distance >= 0 && distance <= proximity) {
                    res = new Cluster[] { a, b };
                    proximity = distance;
                }
            }
        }

        // no closest found
        if (res == null)
            return null;

        //return the one that is not 'cluster'
        return (res[0].equals(cluster)) ? res[1] : res[0];

    }

    private Cluster recalculateDistances(Cluster[] clusters2Merge,
            HashMap<Cluster, HashMap<Cluster, Long>> curDistances) {

        final Cluster biggerCluster = clusters2Merge[0].getSize() > clusters2Merge[1].getSize() ? clusters2Merge[0]
                : clusters2Merge[1];
        final Cluster smallerCluster = clusters2Merge[0].getSize() > clusters2Merge[1].getSize() ? clusters2Merge[1]
                : clusters2Merge[0];

        if (logger.isDebugEnabled()) {
            logger.debug("Recalculating distances");
            logger.debug("Clusters to merge:\n" + clusters2Merge[0] + "\n" + clusters2Merge[1]);
        }

        for (Cluster cluster : curDistances.keySet()) {
            if (cluster.equals(smallerCluster) || cluster.equals(biggerCluster)) {
                continue;
            }

            //logger.debug(curDistances);
            long d0, d1;
            if (curDistances.get(cluster) != null && curDistances.get(cluster).containsKey(biggerCluster)) {
                d0 = curDistances.get(cluster).get(biggerCluster);
            } else {
                d0 = curDistances.get(biggerCluster).get(cluster);
            }
            if (curDistances.get(cluster) != null && curDistances.get(cluster).containsKey(smallerCluster)) {
                d1 = curDistances.get(cluster).get(smallerCluster);
            } else {
                d1 = curDistances.get(smallerCluster).get(cluster);
            }

            long newDistance = distanceFunction.distance(d0, d1);
            curDistances.get(biggerCluster).put(cluster, newDistance);

            if (curDistances.get(cluster) != null) {
                curDistances.get(cluster).remove(smallerCluster);
                curDistances.get(cluster).remove(biggerCluster);
            }
        }

        biggerCluster.add(smallerCluster.getNodes());
        // we may actually add nodes to another instance of the cluster
        // so override the key value in the hash
        curDistances.put(biggerCluster, curDistances.remove(biggerCluster));

        curDistances.remove(smallerCluster);
        curDistances.get(biggerCluster).remove(smallerCluster);

        return biggerCluster;
    }
}
