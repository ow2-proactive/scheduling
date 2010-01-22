/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;


/**
 * Representation of a node set.
 * In this first version, the node set contains only the nodes given by the Resource Manager.
 * In a future version, it will give also further informations like why nodes haven't been given,
 * or more specifications on the nodes, like distance between them, etc...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public class NodeSet extends ArrayList<Node> {

    /**
     * constructor.
     */
    public NodeSet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Create a node containing a nodes collection.
     * @param c collection to put ion the NodeSet
     */
    public NodeSet(Collection<? extends Node> c) {
        super(c);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     * @param   initialCapacity   the initial capacity of the list
     */
    public NodeSet(int initialCapacity) {
        super(initialCapacity);
        // TODO Auto-generated constructor stub
    }
}
