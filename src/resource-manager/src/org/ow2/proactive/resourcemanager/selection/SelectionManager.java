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

import java.util.Collection;
import java.util.List;

import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * An interface of selection manager which is responsible for
 * nodes selection from a pool of free nodes for further scripts execution. 
 *
 */
public interface SelectionManager {
    /**
     * Find appropriate candidates nodes for script execution, taking into 
     * account "free" and "exclusion" nodes lists.  
     * 
     * @param selectionScriptList - set of scripts to execute
     * @param freeNodes - free nodes list provided by resource manager
     * @param exclusionNodes - exclusion nodes list
     * @return candidates node list for script execution
     */
    public Collection<RMNode> findAppropriateNodes(List<SelectionScript> selectionScriptList,
            final Collection<RMNode> freeNodes, NodeSet exclusionNodes);

    /**
     * Predicts script execution result. Allows to avoid duplicate script execution 
     * on the same node. 
     * 
     * @param script - script to execute
     * @param rmnode - target node
     * @return true if script will pass on the node 
     */
    public boolean scriptWillPassOnTheNode(SelectionScript script, RMNode rmnode);

    /**
     * Processes script result and updates knowledge base of 
     * selection manager at the same time.
     *
     * @param script - executed script
     * @param scriptResult - obtained script result
     * @param rmnode - node on which script has been executed
     * @return whether node is selected
     */
    public boolean processScriptResult(SelectionScript script, ScriptResult<Boolean> scriptResult,
            RMNode rmnode);
}
