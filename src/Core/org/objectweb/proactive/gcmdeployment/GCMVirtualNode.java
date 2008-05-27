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
package org.objectweb.proactive.gcmdeployment;

import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;


/**
 * A Virtual Node is an abstraction for deploying parallel and distributed applications.
 * 
 * Virtual Nodes are declared inside a GCM Application Descriptor. Each nodes resulting of a GCM
 * Deployment is mapped to a Virtual Node according to rules defined by the application deployer.
 * 
 * GCMVirtualNode are exported as Remote Objects (RPC). It means they are remotely accessible and
 * never Serialized. To achieve good performances, heavy GCMApplication manipulations should occur
 * on the deployer ProActive Runtime since it does not involve remote calls.
 * 
 */
@PublicAPI
public interface GCMVirtualNode {

    /**
     * A magic number to indicate that a Virtual Node or a Node Provider Contract is Greedy
     */
    static final public long MAX_CAPACITY = -2;

    /**
     * Returns the name of this Virtual Node
     * 
     * The name of a Virtual Node is declared inside the GCM Application Descriptor. It is an unique
     * identifier for a given GCM Application
     * 
     * @return the name of this Virtual Node
     */
    public String getName();

    /**
     * Is this Virtual Node greedy ?
     * 
     * A Virtual Node is Greedy if no capacity is defined for the Virtual Node. It means that the
     * Virtual Node will try to get as many {@link Node}s as possible.
     * 
     * @return true if the Virtual Node is Greedy, false otherwise
     */
    public boolean isGreedy();

    /**
     * Returns true if the Virtual Node is Ready
     * 
     * A Virtual Node is Ready if all Node Provider Contracts and the Virtual Node capacity are
     * satisfied.
     *
     * Nodes can still be attached to the Virtual Node after it becomes Ready. It happens if the
     * Virtual Node is Greedy and at least one Node Provider Contract is Greedy too. In the
     * following example vn will becomes ready as soon as 2 nodes from np2 are mapped but nodes from
     * np1 can still be mapped. <code>
     *  &lt;virtualNode id="vn"&gt;
     *      &lt;nodeProvider refid="np1"/&gt;
     *      &lt;nodeProvider refid="np2" capacity="2"/&gt;
     * </code>
     * 
     * @return true if the Virtual Node is Ready, false otherwise
     */
    public boolean isReady();

    /**
     * Waits until the Virtual Node is ready
     *
     * This method can hang forever if application requirements cannot be fulfilled by the
     * deployment.
     */
    public void waitReady();

    /**
     * Waits until the Virtual Node is ready or timeout is reached
     *
     * @param timeout
     *            A timeout in milliseconds
     * @throws TimeoutException
     *             If the timeout is reached
     */
    public void waitReady(int timeout) throws ProActiveTimeoutException;

    /**
     * Returns the number of Nodes needed to become ready
     * 
     * This number is computed as follows:
     * <code>max(GCMVirtualNode.capacity, sum(NodeProviderContracts.capacity))</code>
     * 
     * @return the number of Nodes to be Ready
     */
    public long getNbRequiredNodes();

    /**
     * Returns the number of Nodes currently mapped into the Virtual Node
     * 
     * @return the number of Nodes mapped into the Virtual Node.
     */
    public long getNbCurrentNodes();

    /**
     * Returns all the Nodes mapped into the Virtual Node
     * 
     * A snapshot is returned. The returned Set will not be updated to reflect new Node arrivals.
     * This method a to be invoked again to get a larger set.
     * 
     * @return The set of all Nodes mapped into the Virtual Node
     */
    public List<Node> getCurrentNodes();

    /**
     * Returns all the Nodes that have been attached to the Virtual Node since last call to
     * <code>getNewNodes()</code>
     * 
     * New nodes are flushed after each call. Multithread applications must be careful when calling
     * this method.
     *
     * @return The set of all freshly mapped Nodes
     */
    public List<Node> getNewNodes();

    /**
     * Subscribes to Node attachment notifications
     * 
     * When a client subscribe to Node attachment notification, the method passed as parameter is
     * invoked each time a Node is attached to the GCMVirtualNode.
     * 
     * The method must have the following signature:
     * <code>void method(Node, String virtualNodeName)</code>
     * 
     * Client can be an Active Object or a POJO. POJO can only be used if the client is located in
     * the deployer ProActive Runtime. Otherwise subscription will fail and false is be returned.
     * 
     * @param client
     *            the object to be notified
     * @param methodName
     *            the method name to be called. The method must have this signature:
     *            <code>void method(Node, String virtualNodeName)</code>
     * @param withHistory
     *            If true already attached Node will generate notification too.
     * @return true is returned if a method named methodName with the right signature exists, false
     *         otherwise
     */
    public void subscribeNodeAttachment(Object client, String methodName, boolean withHistory)
            throws ProActiveException;

    /**
     * Terminate Node Attachment notifications
     * 
     * @param client
     *            the object to be notified
     * @param methodName
     *            the method name to be called
     */
    public void unsubscribeNodeAttachment(Object client, String methodName) throws ProActiveException;

    /**
     * Subscribes to isReady notification
     * 
     * When a client subscribe to isReady notification, the method passed as parameter is invoked
     * when the Virtual Node becomes Ready.
     * 
     * The method must have the following signature:
     * <code>void method(String virtualNodeName)</code>
     * 
     * This notification is not available on Greedy Virtual Node
     * 
     * Client can be an Active Object or a POJO. POJO can only be used if the client is located in
     * the deployer ProActive Runtime. Otherwise subscription will fail and false is be returned.
     *
     * @param client
     *            the object to be notified
     * @param methodName
     *            the method name to be called. The method must have this signature:
     *            <code>method(GCMVirtualNode)</code>
     * @return true is returned if a method named methodName with the right signature exists and the
     *         Virtual Node is not Greedy, false otherwise
     */
    public void subscribeIsReady(Object client, String methodName) throws ProActiveException;

    /**
     * Terminate isReady notification
     * 
     * @param client
     *            the object to be notified
     * @param methodName
     *            the method name to be called
     */
    public void unsubscribeIsReady(Object client, String methodName) throws ProActiveException;

    /**
     * Returns the topology of all the Nodes currently available in the Virtual Node
     * 
     * Once returned a topology will not be updated. <code>getCurrentTopology</code> can be called
     * to get a new topology or <code>updateTopology</code> to update the current one.
     *
     * @return the current topology of all the nodes inside the application
     */
    public Topology getCurrentTopology();

    /**
     * Updates the Topology passed in parameter
     * 
     * Nodes present in this Virtual Node but not in the Topology are added to it.
     * 
     * @param topology
     *            the topology to be updated
     */
    public void updateTopology(Topology topology);

    /**
     * Returns a node from this GCMVirtualNode
     * 
     * This method will block until a node is available (can block forever if the deployment process
     * is over)
     * 
     * @return A node from this virtual node
     */
    public Node getANode();

    /**
     * Returns a node from this GCMVirtualNode
     * 
     * This method will block until a node is available or the timeout is reached.
     * 
     * @param timeout
     *            in milliseconds
     * @return A node from this virtual node or null if the timeout is reached
     */
    public Node getANode(int timeout);

    public UniqueID getUniqueID();
}
