/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
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
 * - define a way to add information about further node acquisition implementing {@link InfrastructureManager#configure(Object...)}
 * - define a way to acquire a single node from underlying infrastructure in {@link InfrastructureManager#acquireNode()}
 * - define a way to acquire all available nodes from the infrastructure in the method {@link InfrastructureManager#acquireAllNodes()}
 * - register available nodes in the resource manager using {@link InfrastructureManager#registerAcquiredNode(Node)}, so they till be taken into account.
 * - add the name of new class to the resource manager configuration file (config/rm/nodesource/infrastructures).
 *
 */
public abstract class InfrastructureManager implements Serializable {

    /** manager's node source */
    protected NodeSource nodeSource;

    /**
     * Adds information required to deploy nodes in the future.
     * Do not initiate a real nodes deployment/acquisition as it's up to the
     * policy.
     *
     * @return true if configuration is successful, RuntimeException otherwise
     */
    public abstract BooleanWrapper configure(Object... parameters);

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
     * Removes the node from the resource manager.
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    public abstract void removeNode(Node node) throws RMException;

    /**
     * Sets an infrastructure node source
     * @param nodeSource policy node source
     */
    public final void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }

    /**
     * Notifies an infrastructure manager (IM) that node which belongs to it is registering in the core.
     * So the IM could maintain its internal structure up to date.
     * Throwing an exception inside this method prevents node to be registered in RMCore
     *
     * @param node an available node to register
     * @throws RMException if any problems occurred
     */
    public abstract void registerAcquiredNode(Node node) throws RMException;

    /**
     * Notify this infrastructure it is going to be shut down along with
     * its nodesource. All necessary cleanup should be done here.
     */
    public abstract void shutDown();
}
