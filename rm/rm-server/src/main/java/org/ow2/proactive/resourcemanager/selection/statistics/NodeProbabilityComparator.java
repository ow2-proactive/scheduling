/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.selection.statistics;

import java.util.Comparator;
import java.util.HashMap;

import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * Comparator for {@link RMNode} objects :<BR>
 * compare two nodes by their chances to verify a script.
 * This comparator is used to sort a nodes collection according to results
 * of a {@link org.ow2.proactive.scripting.SelectionScript}.
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
