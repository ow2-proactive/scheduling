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
package org.ow2.proactive.resourcemanager.frontend;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;


/**
 * Interface for RM events monitoring.
 * Interface and methods to implements for a object that want
 * to receive (monitor) Resource manager's (RM) events.
 *
 * RM Events are defined in {@link RMEventType}.
 *
 * @see org.ow2.proactive.resourcemanager.frontend.RMMonitoring
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public interface RMEventListener {

    /** RM is shutting down
     * @param event object representing the event.
     */
    public void rmShutDownEvent(RMEvent event);

    /** RM has been stopped
     * @param event object representing the event.
     */
    public void rmShuttingDownEvent(RMEvent event);

    /** RM has started
     * @param event object representing the event.
     */
    public void rmStartedEvent(RMEvent event);

    /** new node source available in RM.
     * @param event node source event containing new {@link NodeSource} properties.
     */
    public void nodeSourceAddedEvent(RMNodeSourceEvent event);

    /** node removed from RM.
     * @param event node source event containing removed {@link NodeSource} properties.
     */
    public void nodeSourceRemovedEvent(RMNodeSourceEvent event);

    /** new node available in RM.
     * @param event node event containing new {@link RMNode} properties.
     */
    public void nodeAddedEvent(RMNodeEvent event);

    /**
     * Node has ended a task.
     * becomes from busy to free state.
     * @param event node event containing {@link RMNode} properties.
     */
    public void nodeFreeEvent(RMNodeEvent event);

    /**
     * Node begins to perform a task.
     * becomes from free to busy state.
     * @param event node event containing {@link RMNode} properties.
     */
    public void nodeBusyEvent(RMNodeEvent event);

    /**
     * Node is busy and must be released at the end of the task.
     * becomes from busy to 'to be released' state.
     * @param event node event containing {@link RMNode} properties.
     */
    public void nodeToReleaseEvent(RMNodeEvent event);

    /**
     * Node does not answer anymore to its monitor, the node is said 'down'.
     * becomes from free, busy, 'to be released' to down state.
     * @param event node event containing {@link RMNode} properties.
     */
    public void nodeDownEvent(RMNodeEvent event);

    /**
     * A Node is removed from the RM.
     * @param event node event containing the removed {@link RMNode} properties.
     */
    public void nodeRemovedEvent(RMNodeEvent event);
}
