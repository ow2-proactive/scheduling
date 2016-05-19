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
package org.ow2.proactive.utils;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 * The set of nodes given by the resource manager for computations.
 * The set may contain another collection of nodes (extra nodes) that
 * basically are not intended to be used for computations but occupied
 * by used according to the request.
 *
 * For example when 1 node is requested on a single host exclusively
 * and there is no host available with a single node on it. In this case
 * the resource manager may find another a host with bigger capacity and
 * put all all nodes with were not requested explicitly into the extra nodes collection.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public class NodeSet extends ArrayList<Node> {

    /**
     * extra nodes
     */
    private Collection<Node> extraNodes;

    /**
     * constructor.
     */
    public NodeSet() {
        super();
    }

    /**
     * Creates a node set containing given nodes.
     * @param nodes collection to put into the NodeSet
     */
    public NodeSet(Collection<Node> nodes) {
        super(nodes);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     * @param initialCapacity the initial capacity of the list
     */
    public NodeSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs the nodes set from another node set.
     * @param another
     */
    public NodeSet(NodeSet another) {
        super(another);
        // coping the extra nodes
        if (another.getExtraNodes() != null) {
            this.extraNodes = new LinkedList<>(another.getExtraNodes());
        }
    }

    /**
     * Returns the collection of extra nodes associated to this node set.
     */
    public Collection<Node> getExtraNodes() {
        return extraNodes;
    }

    /**
     * Sets new extra nodes list.
     * @param extraNodes
     */
    public void setExtraNodes(Collection<Node> extraNodes) {
        this.extraNodes = extraNodes;
    }

    /**
     * Return the total number of nodes (standard + extra) included in this node set
     *
     * @return size
     */
    public int getTotalNumberOfNodes() {
        return getAllNodesUrls().size();
    }

    /**
     * Returns a set containing all nodes urls (standard + extra) included in this node set
     *
     * @return set of urls
     */
    public Set<String> getAllNodesUrls() {
        HashSet<String> nodesUrls = new HashSet<>(size() + (extraNodes != null ? extraNodes.size() : 0));
        addNodesToUrlsSet(nodesUrls, this);
        if (extraNodes != null) {
            addNodesToUrlsSet(nodesUrls, extraNodes);
        }
        return nodesUrls;
    }

    private void addNodesToUrlsSet(HashSet<String> nodesUrls, Collection<Node> nodes) {
        for (Node node : nodes) {
            nodesUrls.add(node.getNodeInformation().getURL());
        }
    }
}
