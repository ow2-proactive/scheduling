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

import org.objectweb.proactive.extra.infrastructuremanager.common.NodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.NodeSourceEvent;


/**
 * Interface for IM events monitoring
 * @author gsigety
 *
 */
public interface IMEventListener {
    public void imShutDownEvent();

    public void imShuttingDownEvent();

    public void imStartedEvent();

    public void imKilledEvent();

    public void nodeSourceAddedEvent(NodeSourceEvent ns);

    public void nodeSourceRemovedEvent(NodeSourceEvent ns);

    public void nodeAddedEvent(NodeEvent n);

    public void nodeFreeEvent(NodeEvent n);

    public void nodeBusyEvent(NodeEvent n);

    public void nodeToReleaseEvent(NodeEvent n);

    public void nodeDownEvent(NodeEvent n);

    public void nodeRemovedEvent(NodeEvent n);
}
