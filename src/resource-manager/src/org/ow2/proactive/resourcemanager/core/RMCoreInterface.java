/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.ow2.proactive.resourcemanager.core;

import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * Interface of the RMCore Active object for {@link RMAdmin},
 * {@link RMUser}, {@link RMMonitoring} active objects.
 *
 * @see RMCore
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public interface RMCoreInterface {

    /**
     * Gives a String representation of the RMCore's ID.
     * @return String representation of the RMCore's ID.
     */
    public String getId();

    /**
     * Creates a new node source with specified name, infrastructure manages {@link InfrastructureManager}
     * and acquisition policy {@link NodeSourcePolicy}.
     *
     * @param nodeSourceName the name of the node source
     * @param infrastructureType type of the underlying infrastructure {@link InfrastructureType}
     * @param infrastructureParameters parameters for infrastructure creation
     * @param policyType name of the policy type. It passed as a string due to pluggable approach {@link NodeSourcePolicyFactory}
     * @param policyParameters parameters for policy creation
     * @return constructed NodeSource
     * @throws RMException if any problems occurred
     */
    public NodeSource createNodesource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters)
            throws RMException;

    /**
     * Add a deployed node to the default static nodes source of the RM
     * @param nodeUrl URL of the node.
     * @return true if new node is added successfully, false otherwise
     */
    public BooleanWrapper addNode(String nodeUrl);

    /**
     * Add nodes to a StaticNodeSource represented by sourceName.
     * SourceName must exist and must be a static source
     * @param nodeUrl URL of an existing node to add.
     * @param sourceName name of the static node source that perform the deployment.
     * @return true if new node is added successfully, false otherwise
     */
    public BooleanWrapper addNode(String nodeUrl, String sourceName);

    /**
     * Remove a node from the Core and from its node source.
     *
     * If the node is down, node is just removed from the Core, and nothing is asked to its related NodeSource,
     * because the node source has already detected the node down (it is its function), informed the RMCore,
     * and removed the node from its list.<BR>
     * Else the node is removed from the Core and the removing request is forwarded to the corresponding NodeSource of the node.
     * If the node is busy and the removal request is non preemptive, the node is just put in 'to release' state
     * <BR><BR>
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     *
     */
    public void removeNode(String nodeUrl, boolean preempt);

    /**
     * Removes "number" of nodes from specified node source
     *
     * @param number nodes count to be released
     * @param name a name of the node source
     * @param preemptive if true remove the node immediately without waiting while it will be freed
     */
    public void removeNodes(int number, String nodeSourceName, boolean preemptive);

    /**
     * Removes all nodes from the specified node source.
     *
     * @param nodeSourceName a name of the node source
     * @param preemptive if true remove the node immediately without waiting while it will be freed
     */
    public void removeAllNodes(String nodeSourceName, boolean preemptive);

    /**
     * Stops the RMCore.
     * Stops all {@link NodeSource} active objects
     * Stops {@link RMAdmin}, {@link RMUser}, {@link RMMonitoring} active objects.
     * @param preempt if set to true, Resource manager wait its RM User give back all the busy
     * nodes before performing the shutdown 
     *
     */
    public void shutdown(boolean preempt);

    /**
     * Stops and removes a NodeSource active object with their nodes from the Resource Manager
     * @param sourceName name of the NodeSource object to remove
     * @param preempt preemptive if true remove the node immediately without waiting while it will be freed.
     * @throws RMException if the node source cannot be removed, notably if the name of the node source is unknown.
     */
    public void removeNodeSource(String sourceName, boolean preempt) throws RMException;

    /**
     * Get a set of nodes that verify a selection script.
     * This method has three way to handle the request :<BR>
     *  - if there is no script, it returns at most the
     * first nb free nodes asked.<BR>
     * - Otherwise, it will return the nodes on which 
     * the given script has been verified.<BR>
     * 
     * Exclusion list permits to eliminate a list of non wished nodes.
     *
     * @param nb number of node to provide
     * @param selectionScript that nodes must verify
     * @param exclusion a set of node that must not be returned
     * @return an array list of nodes.
     */
    public NodeSet getAtMostNodes(int nb, SelectionScript selectionScript, NodeSet exclusion);

    /**
     * Get a set of nodes that verify a list of selection scripts, with
     * ability, to exclude a set of nodes 
     *  - if scripts list is null or empty, it returns at most the
     * first nb free nodes asked.<BR>
     * - otherwise, Resource manager test selection scripts in the list,
     * and return a set of node that satisfy ALL the selection scripts defined in the list
     * 
     * Exclusion list permits to eliminate a list of non wished nodes. 
     * 
     * @param nb number of node to provide
     * @param selectionScriptList : a list of scripts to be verified.
     * @param exclusion a set of node that must not be returned
     * @return a set of node that must not be returned.
     */
    public NodeSet getAtMostNodes(int nb, List<SelectionScript> selectionScriptList, NodeSet exclusion);

    /**
     * Returns an exactly number of nodes
     * not yet implemented.
     * @param nb exactly number of nodes to provide.
     * @param selectionScript  that nodes must verify.
     * @return an array list of nodes.
     */
    public NodeSet getExactlyNodes(int nb, SelectionScript selectionScript);

    /**
     * Free a node after a work.
     * RMUser active object wants to free a node that has ended a task.
     * If the node is 'to be released', perform the removing mechanism with
     * the {@link NodeSource} object corresponding to the node,
     * otherwise just set the node to free.
     * @param node node that has terminated a task and must be freed.
     */
    public void freeNode(Node node);

    /**
     * Free a set of nodes.
     * @param nodes a set of nodes to set free.
     */
    public void freeNodes(NodeSet nodes);

    /**
     * Return number of free nodes available for scheduling
     * @return number of free nodes
     */
    public IntWrapper getFreeNodesNumber();

    /**
     * Gives total number of alive nodes handled by RM
     * @return total number of alive nodes
     */
    public IntWrapper getTotalAliveNodesNumber();

    /**
     * Gives total number of nodes handled by RM (including dead nodes)
     * @return total number of nodes
     */
    public IntWrapper getTotalNodesNumber();

    /**
     * Set the ping frequency to the default node source
     * @param frequency the frequency to set to the node source in ms.
     * @throws RMException if there is no default node source.
     */
    public void setPingFrequency(int frequency) throws RMException;

    /**
     * Set the ping frequency to a node source
     * @param frequency the frequency to set to the node source in ms.
     * @param sourceName name of the node source to set the frequency
     * @throws RMException if node source name is unknown.
     */
    public void setPingFrequency(int frequency, String sourceName) throws RMException;

    /**
     * Return the Ping frequency of a node source
     * @param sourceName name of the node source
     * @return the ping frequency
     * @throws RMException if the node source doesn't exist
     */
    public IntWrapper getPingFrequency(String sourceName) throws RMException;

    /**
     * Set the ping frequency to all nodes sources.
     * @param frequency the frequency to set to the node source in ms.
     */
    public void setAllPingFrequency(int frequency);

    /**
     * Builds and returns a snapshot of RMCore's current state.
     * Initial state must be understood as a new Monitor point of view.
     * A new monitor start to receive RMCore events, so must be informed of the current
     * state of the Core at the beginning of monitoring.
     * @return RMInitialState containing nodes and nodeSources of the RMCore.
     */
    public RMInitialState getRMInitialState();

    /**
     * Returns the stub of RMAdmin Active object.
     * @return the RMAdmin Active object.
     */
    public RMAdmin getAdmin();

    /**
     * Returns the stub of RMMonitoring Active object.
     * @return the RMMonitoring Active object.
     */
    public RMMonitoring getMonitoring();

    /**
     * Returns the stub of RMUser Active object.
     * @return the RMUser Active object.
     */
    public RMUser getUser();
}
