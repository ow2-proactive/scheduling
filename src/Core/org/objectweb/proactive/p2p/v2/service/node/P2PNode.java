/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.v2.service.node;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;


/**
 * Used to return an available node or not from the node manager.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Jan 13, 2005
 */
public class P2PNode implements Serializable {
    private Node node = null;
    private P2PNodeManager nodeManager = null;

    /**
     * Construct a new <code>P2PNode</code> with specified <code>node</code> or
     * <code>null</code>.
     *
     * @param node a ProActive node or <code>null</code>.
     * @see org.objectweb.proactive.core.node.Node
     */
    public P2PNode(Node node, P2PNodeManager nodeManager) {
        this.node = node;
        this.nodeManager = nodeManager;
    }

    /**
     * @return a free node or <code>null</code> if no nodes are available.
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * @return the associed node manager.
     */
    public P2PNodeManager getNodeManager() {
        return this.nodeManager;
    }
}
