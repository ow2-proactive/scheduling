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
package org.ow2.proactive.resourcemanager.rmnode;

import java.util.Comparator;

import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;


/**
 * Comparator for {@link RMNode} objects :<BR>
 * compare two nodes by their chances to verify a script.
 * This comparator is used to sort a nodes collection according to results
 * of a {@link SelectionScript}.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class RMNodeComparator implements Comparator<RMNode> {
    private SelectionScript script;

    /**
     * Create a the comparator object
     * @param script comparison criteria.
     */
    public RMNodeComparator(SelectionScript script) {
        this.script = script;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * @param o1 an RMNode object
     * @param o2 an RMNode object
     * @return an integer, greater than 0 if o2 is more able to verify again the selection
     * script, or an integer lesser than 0 if o1 is more able to verify again the selection
     * than o1.
     */
    public int compare(RMNode o1, RMNode o2) {
        int status1 = RMNode.NEVER_TESTED;

        if (o1.getScriptStatus().containsKey(script)) {
            status1 = o1.getScriptStatus().get(script);
        }

        int status2 = RMNode.NEVER_TESTED;

        if (o2.getScriptStatus().containsKey(script)) {
            status2 = o2.getScriptStatus().get(script);
        }

        return status2 - status1;
    }
}
