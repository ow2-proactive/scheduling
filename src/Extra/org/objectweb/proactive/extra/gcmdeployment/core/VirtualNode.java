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
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;


/**
 * A Virtual Node is an abstraction for deploying parallel and distributed applications.
 *
 * Virtual Nodes are declared inside a GCM Application Descriptor. Each nodes resulting of a
 * GCM Deployment is attached to a Virtual Node.
 *
 *
 */
@PublicAPI
public interface VirtualNode {

    /**
     * A magic number to indicate that a Virtual Node or a Node Provider is Greedy
     */
    static final public long MAX_CAPACITY = -2;

    /**
     * Returns the name of this Virtual Node
     *
     * The name of a Virtual Node is declared inside the GCM Application Descriptor. Each
     * Virtual Node as an unique name.
     *
     * @return the name of this Virtual Node
     */
    public String getName();

    /**
     * Returns true if the Virtual Node is Greedy
     *
     * A Virtual Node is Greedy if no capacity is defined for the Virtual Node.
     *
     * @return true if the Virtual Node is Greedy, false otherwise
     */
    public boolean isGreedy();

    /**
     * Returns true if the Virtual Node is Ready
     *
     * A Virtual Node is Ready if all Node Provider Contracts and Virtual Node capacity
     * are satisfied. Nodes can still be attached to the Virtual Node after it becomes Ready.
     * It happens if the Virtual Node is Greedy and at least one Node Provider Contract is Greedy too.
     *
     * @return true if the Virtual Node is Ready, false otherwise
     */
    public boolean isReady();

    /**
     * Returns the number of Nodes needed to become Ready
     *
     * This number is computed as following:
     *         <code>max(VirtualNode.capacity, sum(NodeProviderContracts.capacity))</code>
     *
     * @return the number of Nodes to be Ready
     */
    public long getNbRequiredNodes();

    /**
     * Returns the number of Nodes currently attached to the Virtual Node
     *
     * @return the number of Nodes attached to the Virtual Node.
     */
    public long getNbCurrentNodes();

    /**
     * Returns all the Nodes attached to the Virtual Node
     *
     * A snapshot is performed. The returned Set will not be updated to reflect
     * new Node arrivals. This method a to be invoked again to get a larger set.
     *
     * @return The set of all Nodes attached to the Virtual Node
     */
    public Set<Node> getCurrentNodes();

    /**
     * Returns all the Nodes that have been attached to the Virtual Node since last <code>getNewNodes()</code> call
     *
     * @return The set of all freshly attached Nodes
     */
    public Set<Node> getNewNodes();

    /**
     * Subscribes to Node attachment notifications
     *
     * When a client subscribe to Node attachment notification, the method passed
     * as parameter is invoked each time a Node is attached to the VirtualNode.
     *
     * The method must have the following signature:
     *                 <code>void method(Node, VirtualNode)</code>
     *
     * @param client the object to be notified
     * @param methodName the method name to be called. The method must have this signature:
     * <code>void method(Node, VirtualNode)</code>
     * @return true is returned if a method named methodName with the right
     * signature exists, false otherwise
     */
    public boolean subscribeNodeAttachment(Object client, String methodName);

    /**
     * Unsubscribes to Node Attachment notifications
     *
     * @param client the object to be notified
     * @param methodName the method name to be called
     */
    public void unsubscribeNodeAttachment(Object client, String methodName);

    /**
     * Subscribe to isReady notification
     *
     * When a client subscribe to isReady notification, the method passed
     * as parameter is invoked when the Virtual Node becomes Ready.
     *
     * The method must have the following signature:
     *                 <code>void method(VirtualNode)</code>
     *
     * This notification is not available on Greedy Virtual Node
     *
     * @param client the object to be notified
     * @param methodName the method name to be called. The method must have this signature:
     * <code>method(VirtualNode)</code>
     * @return true is returned if a method named methodName with the right
     * signature exists and the Virtual Node is not Greedy, false otherwise
     */
    public boolean subscribeIsReady(Object client, String methodName);

    /**
     * Unsubscribes to isReady notifications
     *
     * @param client the object to be notified
     * @param methodName the method name to be called
     */
    public void unsubscribeIsReady(Object client, String methodName);

    /**
     *
     * @return
     */
    public DeploymentTree getCurrentTopology();
}
