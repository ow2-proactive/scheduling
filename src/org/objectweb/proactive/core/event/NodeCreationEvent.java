/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


/**
 * <p>
 * Event sent when a Node is created for a given VirtualNode.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2004/07/06
 * @since   ProActive 2.0.1
 *
 */
public class NodeCreationEvent extends ProActiveEvent {
    public static final int NODE_CREATED = 10;
    protected Node node;
    protected VirtualNode vn;
    protected int nodeCreated;

    /**
     * Creates a new <code>NodeCreationEvent</code>
     * @param vn the virtualnode on which the creation occurs
     * @param messageType the type of the event
     * @param node the newly created node
     * @param nodeCreated the number of nodes already created
     */
    public NodeCreationEvent(VirtualNode vn, int messageType, Node node,
        int nodeCreated) {
        super(vn, messageType);
        this.node = node;
        this.vn = vn;
        this.nodeCreated = nodeCreated;
    }

    /**
     * @return Returns the node.
     */
    public Node getNode() {
        return node;
    }

    /**
     * @return Returns the vn.
     */
    public VirtualNode getVirtualNode() {
        return vn;
    }

    /**
     * @return Returns the number of nodes already created.
     */
    public int getNodeCreated() {
        return nodeCreated;
    }
}
