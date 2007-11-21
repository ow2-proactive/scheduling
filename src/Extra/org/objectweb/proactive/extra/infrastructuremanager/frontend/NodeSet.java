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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.proactive.core.node.Node;


/**
 * Representation of a node set.
 * In this first version, the node set contains only the nodes given by the Infrastructure Manager.
 * In a future version, it will give also further informations like why nodes haven't been given,
 * or more specifications on the nodes, like distance between them, etc...
 *
 * @author ProActive Team
 * @version 1.0, Jun 11, 2007
 * @since ProActive 3.2
 */
public class NodeSet extends ArrayList<Node> {

    /**  */
    private static final long serialVersionUID = 4372709972508178428L;

    public NodeSet() {
        super();

        // TODO Auto-generated constructor stub
    }

    public NodeSet(Collection<?extends Node> c) {
        super(c);

        // TODO Auto-generated constructor stub
    }

    public NodeSet(int initialCapacity) {
        super(initialCapacity);

        // TODO Auto-generated constructor stub
    }
}
