/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;


/**
 *
 * Represents underlying infrastructure and defines ways to acquire / release nodes.<br>
 *
 * Acquisition requests are supposed to be asynchronous. When new new node is available it
 * should register itself into the resource manager by calling {@link InfrastructureManager#registerAcquiredNode(Node)}<br>
 *
 * To define a new infrastructure manager
 * - define a way to add information about further node acquisition implementing {@link InfrastructureManager#addNodesAcquisitionInfo(Object...)}
 * - define a way to acquire a single node from underlying infrastructure in {@link InfrastructureManager#acquireNode()}
 * - define a way to acquire all available nodes from the infrastructure in the method {@link InfrastructureManager#acquireAllNodes()}
 * - register available nodes in the resource manager using {@link InfrastructureManager#registerAcquiredNode(Node)}, so they till be taken into account.
 * - add the name of new class to the resource manager configuration file (config/nodesource/infrastructures).
 *
 */
public abstract class InfrastructureManager implements Serializable {

    /** manager's node source */
    protected NodeSource nodeSource;

    /**
     * Adds information required to deploy nodes in the future.
     * Do not initiate a real nodes deployment/acquisition as it's up to the
     * policy.
     */
    public abstract void addNodesAcquisitionInfo(Object... parameters) throws RMException;

    /**
     * Asynchronous node acquisition request.
     * Proactive node should be registered by calling {@link InfrastructureManager#registerAcquiredNode(Node)}
     */
    public abstract void acquireNode();

    /**
     * Asynchronous request of all nodes acquisition.
     * Proactive nodes should be registered by calling {@link InfrastructureManager#registerAcquiredNode(Node)}
     */
    public abstract void acquireAllNodes();

    /**
     * Releases the node.
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    // TODO forever should be set true by default
    public abstract void removeNode(Node node, boolean forever) throws RMException;

    /**
     * Sets an infrastructure node source
     * @param nodeSource policy node source
     */
    public final void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }

    /**
     * Registers available node in resource manager.
     *
     * @param node an available node to register
     * @throws RMException if any problems occurred
     */
    protected void registerAcquiredNode(Node node) throws RMException {
        if (nodeSource != null) {
            nodeSource.getRMCore().addNode(node.getNodeInformation().getURL(), nodeSource.getName());
        } else {
            throw new RMException("Node source is not set for this infrastructure.");
        }
    }
}
