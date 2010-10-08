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

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.node.Node;


/*
 * Class represents the cluster of nodes grouped by the proximity.
 * One node can belong only to one cluster, so it is one-to-many relationship
 * (one cluster - many nodes).
 *
 * The first node used in the constructor defines the cluster id. It in needed in order
 * to optimize hashCode() & equals() methods as the used a lot and their performance is
 * crucial for the HAC algorithm.
 *
 */
public class Cluster {

    private String id = "";
    private int hashCode = 0;
    private LinkedList<Node> nodes = new LinkedList<Node>();

    public Cluster(Node node) {
        nodes.add(node);
        if (node.getNodeInformation() == null) {
            // for test purpose when nodes are imitated
            id = node.toString();
        } else {
            id = node.getNodeInformation().getURL();
        }
        hashCode = id.hashCode();
    }

    public int getSize() {
        return nodes.size();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void add(List<Node> nodes) {
        this.nodes.addAll(nodes);
    }

    public void remove(List<Node> nodes) {
        this.nodes.removeAll(nodes);
    }

    public void removeLast(int number) {
        // removeLast will throw NoSuchElementException if number is bigger than list size
        for (int i = 0; i < number; i++) {
            this.nodes.removeLast();
        }
    }

    public String toString() {
        String res = super.toString() + " [ ";
        for (Node node : nodes) {
            if (node.getNodeInformation() != null) {
                res += node.getNodeInformation().getURL() + " ";
            } else {
                res += node + " ";
            }
        }
        res += "]";

        return res;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Cluster))
            return false;

        return this.id.equals(((Cluster) obj).id);
    }

    public int hashCode() {
        return hashCode;
    }
}
