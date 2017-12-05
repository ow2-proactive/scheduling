/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.rmnode;

import java.io.Serializable;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * An RMNode is a ProActive node able to execute schedulers tasks.
 * So an RMNode is a representation of a ProActive node object with its associated {@link NodeSource},
 * and its state in the Resource Manager :<BR>
 * -free : node is ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be released : node is busy and have to be removed at the end of the its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR><BR>
 *
 * Resource Manager can select nodes that verify criteria. this selection is implemented with
 * {@link SelectionScript} objects. Each node memorize results of executed scripts, in order to
 * answer faster to a selection already asked.
 *
 *
 * @see NodeSource
 * @see SelectionScript
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public interface RMNode extends Comparable<RMNode> {

    /**
     * Execute a {@link Script} on this {@link RMNode}
     * @param <T> The parameterized result type of the script 
     * @param script a selection script to execute.
     * @param bindings bindings used to execute the selection scripts
     * @return the {@link ScriptResult} corresponding to the script execution.
     */
    <T> ScriptResult<T> executeScript(Script<T> script, Map<String, Serializable> bindings);

    /**
     * Get a map of all selection scripts already tested on this node,
     * and the responses given.
     * @return the map of Script and status
     */
    HashMap<SelectionScript, Integer> getScriptStatus();

    /**
     * Cleaning method : remove all active objects on this node.
     */
    void clean() throws NodeException;

    /**
     * @return a string describing the RMNode (status, vnode, host, pad, ...)
     */
    String getNodeInfo();

    // ---------------------------------------------------------------------//
    // GET
    //----------------------------------------------------------------------//

    /**
     * Returns the name of the node.
     *
     * @return the name of the node.
     */
    String getNodeName();

    /** Gives the node object related. 
     * @return a node Object.
     */
    Node getNode();

    /**
     * @return the name of the virtual node
     */
    String getVNodeName();

    /**
     * This method call nodeInformation.getHostName();
     *
     * @return the name of the host machine
     */
    String getHostName();

    /**
     * This method call nodeInformation.getDescriptorVMName();
     *
     * @return the name of the virtual machine
     */
    String getDescriptorVMName();

    /**
     * Returns the NodeSource name of the node.
     *
     * @return the NodeSource name of the node.
     */
    String getNodeSourceName();

    /**
     * Returns the unique ID of the node.
     *
     * @return the unique ID of the node represented by its URL.
     */
    String getNodeURL();

    /**
     * Returns the Stub of the {@link NodeSource} that handles the node.
     *
     * @return the stub of the {@link NodeSource} that handles the node.
     */
    NodeSource getNodeSource();

    /**
     * Returns the node state
     * @return the node state
     */
    NodeState getState();

    /**
     * Returns the time of the last node state change.
     *  
     * @return the time of the last node state change
     */
    long getStateChangeTime();

    /**
     * Gets the provider of the node (who created and deployed it)
     * @return the node provider
     */
    Client getProvider();

    /**
     * Gets the owner of the node (who currently running computations on it)
     * @return the node owner
     */
    Client getOwner();

    /**
     * Gets the permission which defines who can user this node.
     * For instance if at the moment of node source creation "node users" is set to "PROVIDER" it means
     * that only node providers can use it for computations.
     *
     * @return node usage permission
     */
    Permission getUserPermission();

    /**
     * Gets the permission which defines who can remove this node.
     */
    Permission getAdminPermission();

    /**
     * Returns the id of the add event {@link RMNodeEvent}.
     * @return the id of the add event
     */
    RMNodeEvent getAddEvent();

    /**
     * Returns the id of the last event {@link RMNodeEvent}.
     * @return the id of the last event
     */
    RMNodeEvent getLastEvent();

    // ---------------------------------------------------------------------//
    // IS
    //----------------------------------------------------------------------//

    /**
     * Returns a boolean that says if the node is deploying or not.
     *
     * @return {@code true} if the node is an instance of {@link RMDeployingNode},
     * {@code false} if it is an instance of {@link RMNodeImpl}.
     */
    boolean isDeploying();

    /**
     * @return true if the node is free, false otherwise.
     */
    boolean isFree();

    /**
     * @return true if the node is down, false otherwise.
     */
    boolean isDown();

    /**
     * @return true if the node has to be released, false otherwise.
     */
    boolean isToRemove();

    /**
     * @return true if the node is busy, false otherwise.
     */
    boolean isBusy();

    /**
     * @return true if the node is configuring, false otherwise.
     */
    boolean isConfiguring();

    /**
     * Returns a boolean saying whether the node is locked or not.
     *
     * @return {@code true} if the node is locked, {@code false} otherwise.
     */
    boolean isLocked();

    /**
     * Returns the timestamp at which the node has been locked.
     * <p>
     * The value returned by this method is meaningful when the node is locked only.
     *
     * @see #isLocked()
     *
     * @return the timestamp at which the node has been locked or {@code -1} if the node is not locked.
     */
    long getLockTime();

    /**
     * Returns the client who has locked the node.
     *
     * @return the client who has locked the node, or {@code null} if the node is not locked.
     */
    Client getLockedBy();

    // ---------------------------------------------------------------------//
    // SET
    //----------------------------------------------------------------------//

    /**
     * change the node's status to free
     */
    void setFree();

    /**
     * change the node's status to busy.
     * @param owner
     */
    void setBusy(Client owner);

    /**
     *  * change the node's status to 'to release'.
     */
    void setToRemove();

    /**
     * change the node's status to down.
     */
    void setDown();

    /**
     * change the node's status to configuring
     */
    void setConfiguring(Client owner);

    /**
     * Set the node locked.
     *
     * @param client the client that locks the node.
     */
    void lock(Client client);

    /**
     * Unlock the node.
     *
     * @param client the client that unlocks the node.
     */
    void unlock(Client client);

    /**
     * Sets the Stub of the NodeSource to the specified value.
     *
     * @param nodeSource the new NodeSource Stub to use.
     */
    void setNodeSource(NodeSource nodeSource);

    void setAddEvent(final RMNodeEvent addEvent);

    void setLastEvent(final RMNodeEvent lastEvent);

    /**
     * Sets the jmx url of the node.
     * 
     * @param protocol protocol to be used for accessing the node
     * @param address url of the jmx server
     * 
     */
    void setJMXUrl(JMXTransportProtocol protocol, String address);

    /**
     * Gets the JMX url of the node.
     * 
     * @param protocol protocol to be used for accessing the node
     * 
     * @return a jmx url 
     */
    String getJMXUrl(JMXTransportProtocol protocol);

    /**
     * Gets the JMX urls of the node.
     *
     * @return an array of jmx urls
     */
    String[] getJmxUrls();

    /**
     * @return true if node is protected with token 
     */
    boolean isProtectedByToken();

    RMNodeEvent createNodeEvent(RMEventType eventType, NodeState previousNodeState, String initiator);

    RMNodeEvent createNodeEvent();

}
