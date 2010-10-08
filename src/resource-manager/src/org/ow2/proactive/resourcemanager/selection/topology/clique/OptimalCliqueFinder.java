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

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.selection.topology.clustering.DistanceFunction;


/**
 * Class removes all edges bigger than threshold value from the given graph,
 * then finds the clique in the graph.
 *
 * For details see
 * http://en.wikipedia.org/wiki/Clique_problem
 *
 */
public class OptimalCliqueFinder extends CliqueFinder {

    private List<Node> optimalClique = null;
    private long optimalCliqueWeight = Long.MAX_VALUE;
    private DistanceFunction distanceFunction;

    public OptimalCliqueFinder(List<Node> pivot, long threshold, DistanceFunction distanceFunction) {
        super(pivot, threshold);
        this.distanceFunction = distanceFunction;
    }

    public List<Node> getClique(int n, List<Node> graphNodes) {
        optimalClique = null;
        optimalCliqueWeight = Long.MAX_VALUE;
        return super.getClique(n, graphNodes);
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
            if (result != null && result.size() == n && result != optimalClique) {
                // found a clique
                long weight = getCliqueWeight(result);
                //logger.debug("Clique weight: " + weight);
                if (weight < optimalCliqueWeight) {
                    if (logger.isDebugEnabled()) {
                        String urls = "";
                        for (Node node : result) {
                            urls += node.getNodeInformation().getURL() + " ";
                        }
                        logger.debug("New optimal clique found: weight " + weight + ", nodes: " + urls);
                    }
                    optimalCliqueWeight = weight;
                    optimalClique = new LinkedList<Node>(result);
                }
            }
            current.remove(nodes[i]);
        }
        return optimalClique;
    }

    private long getCliqueWeight(List<Node> clique) {
        long weight = -1;
        Node[] array = clique.toArray(new Node[] {});
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (weight == -1) {
                    weight = getDistance(array[i], array[j]);
                    continue;
                }
                weight = distanceFunction.distance(weight, getDistance(array[i], array[j]));
            }
        }
        return weight;
    }
}
