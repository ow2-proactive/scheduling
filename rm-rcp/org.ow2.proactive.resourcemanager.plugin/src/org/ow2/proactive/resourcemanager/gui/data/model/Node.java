/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.data.model;

import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


/**
 * @author The ProActive Team
 */
public class Node extends TreeLeafElement {
    private NodeState state = null;
    private String stateChangeTime;

    public Node(String name, NodeState state, String stateChangeTime) {
        super(name, TreeElementType.NODE);
        this.state = state;
        this.stateChangeTime = stateChangeTime;
    }

    /**
     * To get the state
     *
     * @return the state
     */
    public NodeState getState() {
        return state;
    }

    /**
     * To set the state
     *
     * @param state the state to set
     */
    public void setState(RMNodeEvent event) {
        this.state = event.getNodeState();
        this.stateChangeTime = event.getStateChangeTime();
    }

    public String getStateChangeTime() {
        return stateChangeTime;
    }
}
