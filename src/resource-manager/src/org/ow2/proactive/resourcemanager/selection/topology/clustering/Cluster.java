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


public class Cluster {

    private List<Node> nodes = new LinkedList<Node>();

    public Cluster() {
    }

    public Cluster(Cluster[] clusters) {
        for (Cluster c : clusters) {
            nodes.addAll(c.getNodes());
        }
    }

    public Cluster(Node node) {
        nodes.add(node);
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
        return nodes.equals(((Cluster) obj).getNodes());
    }

    public int hashCode() {
        return nodes.hashCode();
    }
}
