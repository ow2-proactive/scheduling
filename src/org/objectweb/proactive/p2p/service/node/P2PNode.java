/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.service.node;

import org.objectweb.proactive.core.node.Node;

import java.io.Serializable;


/**
 * Used to return an available node or not from the node manager.
 *
 * @author Alexandre di Costanzo
 *
 * Created on Jan 13, 2005
 */
public class P2PNode implements Serializable {
    private Node node = null;

    /**
     * Construct a new <code>P2PNode</code> with specified <code>node</code> or
     * <code>null</code>.
     *
     * @param node a ProActive node or <code>null</code>.
     * @see org.objectweb.proactive.core.node.Node
     */
    public P2PNode(Node node) {
        this.node = node;
    }

    /**
     * @return a free node or <code>null</code> if no nodes are available.
     */
    public Node getNode() {
        return this.node;
    }
}
