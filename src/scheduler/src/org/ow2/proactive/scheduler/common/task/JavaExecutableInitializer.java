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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.task;

import java.util.Map;

import org.ow2.proactive.utils.NodeSet;


/**
 * JavaExecutableInitializer is the class used to store context of java executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JavaExecutableInitializer implements ExecutableInitializer {

    /** Demanded nodes */
    protected NodeSet nodes;

    /** Arguments of the java task */
    protected Map<String, String> arguments;

    /**
     * Get the nodes list
     *
     * @return the nodes list
     */
    public NodeSet getNodes() {
        return nodes;
    }

    /**
     * Set the nodes list value to the given nodes value
     *
     * @param nodes the nodes to set
     */
    public void setNodes(NodeSet nodes) {
        this.nodes = nodes;
    }

    /**
     * Get the arguments of the executable
     *
     * @return the arguments of the executable
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * Set the arguments value to the given arguments value
     *
     * @param arguments the arguments to set
     */
    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

}
