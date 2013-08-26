/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.rmnode;

import java.security.Permission;
import java.util.HashMap;

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
     * @return the {@link ScriptResult} corresponding to the script execution.
     */
    public <T> ScriptResult<T> executeScript(Script<T> script);

    /**
     * Get a map of all selection scripts already tested on this node,
     * and the responses given.
     * @return the map of Script and status
     */
    public HashMap<SelectionScript, Integer> getScriptStatus();

    /**
     * Cleaning method : remove all active objects on this node.
     */
    public void clean() throws NodeException;

    /**
     * @return a string describing the RMNode (status, vnode, host, pad, ...)
     */
    public String toString();

    // ---------------------------------------------------------------------//
    // GET
    //----------------------------------------------------------------------//

    /**
     * @return the name of the node
     */
    public String getNodeName();

    /** Gives the node object related. 
     * @return a node Object.
     * @throws NodeException if the node is down.
     */
    public Node getNode() throws NodeException;

    /**
     * @return the name of the virtual node
     */
    public String getVNodeName();

    /**
     * This method call nodeInformation.getHostName();
     *
     * @return the name of the host machine
     */
    public String getHostName();

    /**
     * This method call nodeInformation.getDescriptorVMName();
     *
     * @return the name of the virtual machine
     */
    public String getDescriptorVMName();

    /**
     * @return the {@link NodeSource} name of the source that handle the node
     */
    public String getNodeSourceName();

    /**
     * @return the URL of the node.
     */
    public String getNodeURL();

    /** Get the node source object that handle the node
     * @return the stub of the node source active object
     */
    public NodeSource getNodeSource();

    /**
     * Returns the node state
     * @return the node state
     */
    public NodeState getState();

    /**
     * Returns the time of the last node state change.
     *  
     * @return the time of the last node state change
     */
    public long getStateChangeTime();

    /**
     * Gets the provider of the node (who created and deployed it)
     * @return the node provider
     */
    public Client getProvider();

    /**
     * Gets the owner of the node (who currently running computations on it)
     * @return the node owner
     */
    public Client getOwner();

    /**
     * Gets the permission which defines who can user this node.
     * For instance if at the moment of node source creation "node users" is set to "PROVIDER" it means
     * that only node providers can use it for computations.
     *
     * @return node usage permission
     */
    public Permission getUserPermission();

    /**
     * Gets the permission which defines who can remove this node.
     */
    public Permission getAdminPermission();

    /**
     * Returns the id of the add event {@link RMNodeEvent}.
     * @return the id of the add event
     */
    public RMNodeEvent getAddEvent();

    /**
     * Returns the id of the last event {@link RMNodeEvent}.
     * @return the id of the last event
     */
    public RMNodeEvent getLastEvent();

    // ---------------------------------------------------------------------//
    // IS
    //----------------------------------------------------------------------//

    /**
     * @return true if the node is free, false otherwise.
     */
    public boolean isFree();

    /**
     * @return true if the node is down, false otherwise.
     */
    public boolean isDown();

    /**
     * @return true if the node has to be released, false otherwise.
     */
    public boolean isToRemove();

    /**
     * @return true if the node is busy, false otherwise.
     */
    public boolean isBusy();

    /**
     * @return true if the node is configuring, false otherwise.
     */
    public boolean isConfiguring();

    /**
     * @return true if the node is locked, false otherwise.
     */
    public boolean isLocked();

    // ---------------------------------------------------------------------//
    // SET
    //----------------------------------------------------------------------//

    /**
     * change the node's status to free
     * @throws NodeException
     */
    public void setFree() throws NodeException;

    /**
     * change the node's status to busy.
     * @param owner
     * @throws NodeException
     */
    public void setBusy(Client owner) throws NodeException;

    /**
     *  * change the node's status to 'to release'.
     * @throws NodeException if the node is down
     */
    public void setToRemove() throws NodeException;

    /**
     * change the node's status to down.
     */
    public void setDown();

    /**
     * change the node's status to configuring
     */
    public void setConfiguring(Client owner);

    /**
     * Changes the node status to locked
     * @param owner
     */
    public void lock(Client owner);

    /**
     * Change the {@link NodeSource} from where the node is.
     * @param nodeSource
     */
    public void setNodeSource(NodeSource nodeSource);

    public void setAddEvent(final RMNodeEvent addEvent);

    public void setLastEvent(final RMNodeEvent lastEvent);

    /**
     * Sets the jmx url of the node.
     * 
     * @param jmx protocol to be used for accessing the node
     * @param address url of the jmx server
     * 
     */
    public void setJMXUrl(JMXTransportProtocol protocol, String address);

    /**
     * Gets the JMX url of the node.
     * 
     * @param jmx protocol to be used for accessing the node
     * 
     * @return a jmx url 
     */
    public String getJMXUrl(JMXTransportProtocol protocol);

    /**
     * @return true if node is protected with token 
     */
    public boolean isProtectedByToken();

    RMNodeEvent createNodeEvent(RMEventType eventType, NodeState previousNodeState, String initiator);

    RMNodeEvent createNodeEvent();
}
