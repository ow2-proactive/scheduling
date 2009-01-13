/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.selection;

import java.util.Comparator;
import java.util.HashMap;

import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * Comparator for {@link RMNode} objects :<BR>
 * compare two nodes by their chances to verify a script.
 * This comparator is used to sort a nodes collection according to results
 * of a {@link SelectionScript}.
 *
 */
public class NodeProbabilityComparator implements Comparator<RMNode> {

    HashMap<RMNode, Probability> nodes;

    public NodeProbabilityComparator(HashMap<RMNode, Probability> nodes) {
        this.nodes = nodes;
    }

    public int compare(RMNode n1, RMNode n2) {
        // probabilities are always greater than zero, so diff approach can be used
        // for numbers which can be negative it won't work
        double diff = nodes.get(n2).value() - nodes.get(n1).value();
        if (diff < 0)
            return -1;
        else if (diff > 0)
            return 1;

        return 0;
    }

}
