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

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;


/**
 * Interface for IM events monitoring.
 * Interface and methods to implements for a object that want
 * to receive (monitor) Infrastructure manager's (IM) events.
 *
 * IM Events are defined in {@link IMEvent}.
 *
 * @see org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring
 *
 * @author ProActive team.
 *
 */
@PublicAPI
public interface IMEventListener {

    /** IM is shutting down */
    public void imShutDownEvent();

    /** IM has been stopped */
    public void imShuttingDownEvent();

    /** IM has started */
    public void imStartedEvent();

    /** IM has been killed */
    public void imKilledEvent();

    /** new node source available in IM.
     * @param ns node source event containing new {@link NodeSource} properties.
     */
    public void nodeSourceAddedEvent(IMNodeSourceEvent ns);

    /** node removed from IM.
     * @param ns node source event containing removed {@link NodeSource} properties.
     */
    public void nodeSourceRemovedEvent(IMNodeSourceEvent ns);

    /** new node available in IM.
     * @param n node event containing new {@link IMNode} properties.
     */
    public void nodeAddedEvent(IMNodeEvent n);

    /**
     * Node has ended a task.
     * becomes from busy to free state.
     * @param n node event containing {@link IMNode} properties.
     */
    public void nodeFreeEvent(IMNodeEvent n);

    /**
     * Node begins to perform a task.
     * becomes from free to busy state.
     * @param n node event containing {@link IMNode} properties.
     */
    public void nodeBusyEvent(IMNodeEvent n);

    /**
     * Node is busy and must be released at the end of the task.
     * becomes from busy to 'to be released' state.
     * @param n node event containing {@link IMNode} properties.
     */
    public void nodeToReleaseEvent(IMNodeEvent n);

    /**
     * Node does not answer anymore to its monitor, the node is said 'down'.
     * becomes from free, busy, 'to be released' to down state.
     * @param n node event containing {@link IMNode} properties.
     */
    public void nodeDownEvent(IMNodeEvent n);

    /**
     * A Node is removed from the IM.
     * @param n node event containing the removed {@link IMNode} properties.
     */
    public void nodeRemovedEvent(IMNodeEvent n);
}
