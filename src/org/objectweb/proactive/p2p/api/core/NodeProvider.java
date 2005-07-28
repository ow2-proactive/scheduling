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
package org.objectweb.proactive.p2p.api.core;

import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;

import java.io.Serializable;
import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
public class NodeProvider implements Serializable, NodeCreationEventListener {
	private Vector availableNodes = new Vector();
	
    /**
     * The empty and no args constructor for activing the object.
     */
    public NodeProvider() {
        // the empty constructor
    }

    public NodeProvider(String descPath) {
        // TODO
    }

    /**
     * @see org.objectweb.proactive.core.event.NodeCreationEventListener#nodeCreated(org.objectweb.proactive.core.event.NodeCreationEvent)
     */
    public void nodeCreated(NodeCreationEvent event) {
    	Node node = event.getNode();
        // TODO Auto-generated method stub
    }
}
