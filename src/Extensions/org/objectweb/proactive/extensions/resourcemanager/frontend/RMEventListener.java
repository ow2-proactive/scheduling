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
package org.objectweb.proactive.extensions.resourcemanager.frontend;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMEventType;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.resourcemanager.rmnode.RMNode;


/**
 * Interface for RM events monitoring.
 * Interface and methods to implements for a object that want
 * to receive (monitor) Resource manager's (RM) events.
 *
 * RM Events are defined in {@link RMEventType}.
 *
 * @see org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring
 *
 * @author ProActive team.
 *
 */
@PublicAPI
public interface RMEventListener {

    /** RM is shutting down */
    public void imShutDownEvent(RMEvent evt);

    /** RM has been stopped */
    public void imShuttingDownEvent(RMEvent evt);

    /** RM has started */
    public void imStartedEvent(RMEvent evt);

    /** RM has been killed */
    public void imKilledEvent(RMEvent evt);

    /** new node source available in RM.
     * @param ns node source event containing new {@link NodeSource} properties.
     */
    public void nodeSourceAddedEvent(RMNodeSourceEvent ns);

    /** node removed from RM.
     * @param ns node source event containing removed {@link NodeSource} properties.
     */
    public void nodeSourceRemovedEvent(RMNodeSourceEvent ns);

    /** new node available in RM.
     * @param n node event containing new {@link RMNode} properties.
     */
    public void nodeAddedEvent(RMNodeEvent n);

    /**
     * Node has ended a task.
     * becomes from busy to free state.
     * @param n node event containing {@link RMNode} properties.
     */
    public void nodeFreeEvent(RMNodeEvent n);

    /**
     * Node begins to perform a task.
     * becomes from free to busy state.
     * @param n node event containing {@link RMNode} properties.
     */
    public void nodeBusyEvent(RMNodeEvent n);

    /**
     * Node is busy and must be released at the end of the task.
     * becomes from busy to 'to be released' state.
     * @param n node event containing {@link RMNode} properties.
     */
    public void nodeToReleaseEvent(RMNodeEvent n);

    /**
     * Node does not answer anymore to its monitor, the node is said 'down'.
     * becomes from free, busy, 'to be released' to down state.
     * @param n node event containing {@link RMNode} properties.
     */
    public void nodeDownEvent(RMNodeEvent n);

    /**
     * A Node is removed from the RM.
     * @param n node event containing the removed {@link RMNode} properties.
     */
    public void nodeRemovedEvent(RMNodeEvent n);
}
