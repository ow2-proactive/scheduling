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
package org.ow2.proactive.resourcemanager.core;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource;


/**
 * This Interface defines for {@link NodeSource} objects, accessible methods to {@link RMCore}.<BR><BR>
 *
 * Methods defined here provide a way to a {@link NodeSource} object to :<BR>
 * -register itself to the RMCore when its active Object creation is ended.<BR>
 * -add nodes to RMCore, nodes added become ready to perform tasks.<BR>
 * -remove nodes to the RMCore, nodes are no longer available to perform jobs.<BR>
 * -inform RMCore that a node has been detected down. <BR><BR>
 *
 * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 *
 */
public interface RMCoreSourceInterface {

    /** Add a new node to the node Manager.
     *  The new node is available for tasks execution.
     * @param node {@link Node} object to add.
     * @param VNodeName Virtual node name of the node.
     * @param PADName ProActive descriptor name of the node.
     * @param nodeSource Stub of the {@link NodeSource} object that handle the node.
     */
    public void addingNodeNodeSourceRequest(Node node, String VNodeName, String PADName, NodeSource nodeSource);

    /**
     * Add a NodeSource to the core with its Id.
     * @param source Stub of the {@link NodeSource} object to add.
     * @param sourceId name of the {@link NodeSource} object to add.
     */
    public void nodeSourceRegister(NodeSource source, String sourceId);

    /**
     * Removes a NodeSource to the core.
     * Nodes source confirms by this call its removal.
     * Node source has previously removed its nodes.
     * RMcore delete the nodeSource from its source list.
     * @param sourceId name of the {@link NodeSource} to remove.
     * @param evt Remove source event to throw to RMMonitoring
     */
    public void nodeSourceUnregister(String sourceId, RMNodeSourceEvent evt);

    /**
     * Removes a node from the Core.
     * RMCore confirm after to the NodeSource the removing.
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void nodeRemovalNodeSourceRequest(String nodeUrl, boolean preempt);

    /**
     * Informs the nodeManager that a node is down.
     * @param nodeUrl URL of the down node.
     */
    public void setDownNode(String nodeUrl);
}
