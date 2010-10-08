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
package org.ow2.proactive.resourcemanager.selection.topology.clique;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.frontend.topology.TopologyException;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * Class removes all edges bigger than threshold value from the given graph,
 * then finds the clique in the graph.
 *
 * For details see
 * http://en.wikipedia.org/wiki/Clique_problem
 *
 */
public class CliqueFinder {

    protected final static Logger logger = ProActiveLogger.getLogger(RMLoggers.TOPOLOGY);
    protected List<Node> pivot;
    protected long threshold;

    public CliqueFinder(List<Node> pivot, long threshold) {
        this.pivot = pivot;
        this.threshold = threshold;

        if (this.pivot == null) {
            this.pivot = new LinkedList<Node>();
        }

        // checking if nodes are interconnected with a given threshold
        if (this.pivot.size() > 1) {
            List<Node> clique = getClique(pivot.size(), pivot);
            if (clique.size() < pivot.size()) {
                throw new TopologyException("Pivot nodes are not interconnected within threshold " +
                    threshold);
            }
        }
    }

    /**
     *
     * @param n
     * @param graphNodes
     * @param optimal - clique with a minimum weight
     * @return
     */
    public List<Node> getClique(int n, List<Node> graphNodes) {

        logger.debug("Finding a clique of size " + n + " in graph with " + graphNodes.size() + " nodes");

        if (pivot.size() > 0) {
            graphNodes = new LinkedList<Node>(graphNodes);
            for (Node piv : pivot) {
                if (!graphNodes.contains(piv))
                    graphNodes.add(piv);
            }
        }

        // copy the graph for specified nodes, filtering out
        // the distances greater threshold
        HashMap<Node, HashMap<Node, Long>> graph = new HashMap<Node, HashMap<Node, Long>>();
        for (Node node : graphNodes) {
            for (Node anotherNode : graphNodes) {
                if (node.equals(anotherNode)) {
                    continue;
                }

                Long distance = getDistance(node, anotherNode);
                if (distance != null && distance >= 0 && distance <= threshold) {
                    if (graph.get(node) == null) {
                        graph.put(node, new HashMap<Node, Long>());
                    }
                    if (graph.get(anotherNode) == null) {
                        graph.put(anotherNode, new HashMap<Node, Long>());
                    }
                    graph.get(node).put(anotherNode, distance);
                    graph.get(anotherNode).put(node, distance);
                }
            }
        }

        // now in the graph we have only distances less or equal to the threshold
        // looking up for a clique of "n" size

        // first remove all the nodes that have less then "n - 1" connections
        // as they cannot form "n" size click
        boolean nodeRemoved = true;
        while (nodeRemoved) {
            nodeRemoved = false;
            for (Node node : new LinkedList<Node>(graph.keySet())) {
                if (graph.get(node).size() < n - 1) {
                    for (Node connectedNode : graph.get(node).keySet()) {
                        graph.get(connectedNode).remove(node);
                    }
                    graph.remove(node);
                    nodeRemoved = true;
                }
            }
        }

        logger.debug("Number of nodes in the graph after removal is " + graph.size());

        List<Node> clique = null;
        if (graph.size() > 0) {
            List<Node> current = pivot.size() == 0 ? new LinkedList<Node>() : new LinkedList<Node>(pivot);
            LinkedList<Node> suitableNodes = new LinkedList<Node>(graph.keySet());
            // pivot has to be in the beginning of the list
            for (Node piv : pivot) {
                suitableNodes.remove(piv);
                suitableNodes.addFirst(piv);
            }
            clique = findCliqueRecursive(n, current.size(), current, suitableNodes.toArray(new Node[] {}));
        }

        if (clique == null) {
            if (n > 1) {
                logger.debug("Cannot find the clique of size " + n + ". Will try for " + (n - 1));
                return getClique(n - 1, graphNodes);
            } else {
                logger.debug("Cannot find the clique of size " + n + ". Nothing found");
                return new LinkedList<Node>();
            }
        } else {
            logger.debug("Clique of size " + n + " found");
            if (logger.isDebugEnabled()) {
                for (Node cliqueNode : clique) {
                    logger.debug("Node " + cliqueNode.getNodeInformation().getURL());
                }
            }

            // removing pivot from the result
            for (Node piv : pivot) {
                clique.remove(piv);
            }
            return clique;
        }
    }

    protected Long getDistance(Node node, Node node2) {
        return RMCore.topologyManager.getDistance(node, node2);
    }

    protected List<Node> findCliqueRecursive(int n, int index, List<Node> current, Node[] nodes) {

        if (current.size() == n) {
            // found a clique
            return current;
        }

        if (nodes.length - index < n - current.size()) {
            // cannot form a cliques as there are not enough nodes left
            return null;
        }

        for (int i = index; i < nodes.length; i++) {
            if (!isConnected(current, nodes[i])) {
                continue;
            }
            current.add(nodes[i]);
            //logger.debug("Current: " + current);
            List<Node> result = findCliqueRecursive(n, i + 1, current, nodes);
            if (result != null && result.size() == n) {
                // found a clique
                if (logger.isDebugEnabled()) {
                    String urls = "";
                    for (Node node : result) {
                        urls += node.getNodeInformation().getURL() + " ";
                    }
                    logger.debug("Clique found: " + urls);
                }

                //logger.debug("Clique weight: " + weight);
                return result;
            }
            current.remove(nodes[i]);
        }
        return null;
    }

    protected boolean isConnected(List<Node> nodes, Node node) {
        for (Node n : nodes) {
            Long distance = getDistance(n, node);
            if (distance == null || (distance != null && distance < 0) ||
                (distance != null && distance > threshold)) {
                return false;
            }
        }
        return true;
    }
}
