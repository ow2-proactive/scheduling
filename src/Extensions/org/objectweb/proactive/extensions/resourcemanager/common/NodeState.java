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
package org.objectweb.proactive.extensions.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Enumeration of all states of a RMNode :<BR>
 * -free : node is ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR>
 *
 * @see RMNode
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public enum NodeState {
    /**
     * a node that can be provided to a RM user, and able to perform a task
     */
    FREE(0),
    /**
     * a node provided to a RM user.
     */
    BUSY(1),
    /**
     * a node that has been detected down.
     */
    DOWN(2),
    /**
     * a node busy which must be removed from Resource manager, when the RM user
     * will give back the node.
     * 
     */
    TO_BE_RELEASED(3);
    private int state;

    /**
     * Constructor
     * @param nb state to specify.
     */
    NodeState(int nb) {
        state = nb;
    }

    /**
     * Gives a string representation of the state.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return String.valueOf(state);
    }
}
