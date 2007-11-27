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
package org.objectweb.proactive.extra.infrastructuremanager.core;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;


/**
 * Interface for IMNodeSourceManager
 * methods called by NodeSource objects
 */
public interface IMCoreSourceInt {

    /** add a new node to the node Manager
     *  the new node will be available for job
     */
    public void internalAddNode(Node node, String VNodeName, String PADName,
        NodeSource nodeSource);

    /**
     * adding a NodeSource to the core with its Id
     */
    public void addSource(NodeSource source, String sourceId);

    /**
    * release a node as soon as possible.
    * if the node is busy, waiting the job end
    * a call back is awaited to confirm this node unregistering
    */
    public void internalRemoveNode(String nodeUrl, boolean preempt);

    /**
     * informing the nodeManager that the node is down
     */
    public void setDownNode(String nodeUrl);
}
