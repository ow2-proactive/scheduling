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
package org.ow2.proactive.resourcemanager.frontend;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


/**
 * This class represents the interface of the resource manager.
 * <p>
 * The resource manager is used to aggregate resources across the network which are represented by ProActive nodes.
 * Its main features are
 * <ul>
 * <li> deployment, acquisition and release of ProActive nodes to/from an underlying infrastructure</li>
 * <li> providing nodes for computations, based on clients criteria (@see {@link SelectionScript})</li>
 * <li> maintaining and monitoring its list of resources and managing their states (free, busy, down...)</li>
 * </ul>
 * <p>
 * This interface provides means to create/remove node sources in the resource manager, add remove nodes to node sources,
 * track the state of the resource manager and get nodes for computations.
 * <p>
 * All the methods of this interface are asynchronous.
 */
@PublicAPI
public interface ResourceManager {

    /**
     * The node source is the set of nodes acquired from specific infrastructure and characterized
     * by particular acquisition policy.
     * <p>
     * This method creates a new node source with specified name, infrastructure manager and acquisition policy.
     * Parameters required to create infrastructure manager and policy can be obtained from
     * corresponding {@link PluginDescriptor}.
     *
     * @param nodeSourceName the name of the node source
     * @param infrastructureType type of the underlying infrastructure
     * @param infrastructureParameters parameters for infrastructure creation
     * @param policyType name of the policy type. It passed as a string due to plug-able approach
     * @param policyParameters parameters for policy creation
     * @return true if a new node source was created successfully, runtime exception otherwise
     */
    BooleanWrapper createNodeSource(String nodeSourceName, String infrastructureType, Object[] infrastructureParameters,
            String policyType, Object[] policyParameters);

    /**
     * Remove a node source from the RM.
     * All nodes handled by the node source are removed.
     *
     * @param sourceName name of the source to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @return true if the node source was removed successfully, runtime exception otherwise
     */
    BooleanWrapper removeNodeSource(String sourceName, boolean preempt);

    /**
     * Returns the list of existing node source infrastructures 
     *
     * @return the list of existing node source infrastructures 
     */
    List<RMNodeSourceEvent> getExistingNodeSourcesList();

    /**
     * Returns the list of supported node source infrastructures descriptors.
     *
     * @return the list of supported node source infrastructures descriptors
     */
    Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures();

    /**
     * Returns the list of supported node source policies descriptors.
     *
     * @return the list of supported node source policies descriptors
     */
    Collection<PluginDescriptor> getSupportedNodeSourcePolicies();

    /**
     * Each node source scan its nodes periodically to check their states.
     * This method changes the period of nodes scanning.
     *
     * @param frequency the frequency to set to the node source in ms.
     * @param sourceName name of the node source to set the frequency
     * @return true if ping frequency is successfully changed, runtime exception otherwise
     */
    BooleanWrapper setNodeSourcePingFrequency(int frequency, String sourceName);

    /**
     * Returns the ping frequency of a node source.
     *
     * @param sourceName name of the node source
     * @return the ping frequency
     */
    IntWrapper getNodeSourcePingFrequency(String sourceName);

    /**
     * Adds an existing node to the default node source of the resource manager.
     *
     * @param nodeUrl URL of the node to add.
     * @return true if new node is added successfully, runtime exception otherwise
     */
    BooleanWrapper addNode(String nodeUrl);

    /**
     * Adds an existing node to the particular node source.
     *
     * @param nodeUrl URL of the node to add.
     * @param sourceName name of the static node source that will handle the node
     * @return true if new node is added successfully, runtime exception otherwise
     */
    BooleanWrapper addNode(String nodeUrl, String sourceName);

    /**
     * Removes a node from the resource manager.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @return true if the node is removed successfully, false or exception otherwise
     */
    BooleanWrapper removeNode(String nodeUrl, boolean preempt);

    /**
     * Locks the set of nodes and makes them not available for others.
     * The node state "locked" means that node cannot be used for computations by anyone.
     *
     * Could be called only by node administrator, which is one of the following: rm admin,
     * node source admin or node provider.
     *
     * Nodes can be locked whatever their state is.
     *
     * @param urls is a set of nodes
     * @return {@code true} if all the nodes become locked, {@code false} otherwise.
     *
     */
    BooleanWrapper lockNodes(Set<String> urls);

    /**
     * Unlock nodes. The specified nodes become available to other users for computations.
     * Real eligibility still depends on the Node state.
     *
     * Could be called only by node administrator, which is one of the following: rm admin,
     * node source admin or node provider.
     *
     * @param urls is a set of nodes to be unlocked.
     *
     * @return {@code true} if all the nodes are unlocked with success, {@code false} otherwise.
     */
    BooleanWrapper unlockNodes(Set<String> urls);

    /**
     * Returns true if the node nodeUrl is registered (i.e. known by the RM) and not down.
     *
     * @param nodeUrl of node to ping.
     * @return true if the node nodeUrl is registered and not down.
     */
    BooleanWrapper nodeIsAvailable(String nodeUrl);

    /**
     * This method is called periodically by ProActive Nodes to inform the
     * Resource Manager of a possible reconnection. The method is also used by
     * ProActive Nodes to know if they are still known by the Resource Manager.
     * For instance a Node which has been removed by a user from the
     * Resource Manager is no longer known.
     *
     * @param nodeUrls the URLs of the workers associated to the node that publishes the update.
     *
     * @return The set of worker node URLs that are unknown to the Resource Manager
     * (i.e. have been removed by a user).
     */
    Set<String> setNodesAvailable(Set<String> nodeUrls);

    /**
     * Returns true if the resource manager is operational and a client is connected.
     *
     * Throws SecurityException if client is not connected.
     * @return true if the resource manager is operational, false otherwise
     */
    BooleanWrapper isActive();

    /**
     * Returns the resource manager summary state.
     * To retrieve detailed state use {@link RMMonitoring}.getState() method.
     *
     * @return the resource manager summary state.
     */
    RMState getState();

    /**
     * Returns the monitoring interface to manager listeners of the resource manager.
     *
     * @return the resource manager monitoring interface
     */
    RMMonitoring getMonitoring();

    /**
     * Returns a list of all alive Nodes Urls. Alive means neither down nor currently deploying.
     * @return list of node urls
     */
    Set<String> listAliveNodeUrls();

    /**
     * Returns a list of all alive Nodes Urls associated with the given node sources.
     * @param nodeSourceNames set of node sources containing the nodes.
     * @return list of node urls
     */
    Set<String> listAliveNodeUrls(Set<String> nodeSourceNames);

    /**
     * Finds "number" nodes for computations according to the selection script.
     * All nodes which are returned to the client as marked internally as busy and cannot
     * be used by others until the client frees them.
     * <p>
     * If the resource manager does not have enough nodes it returns as much as it
     * has, but only those which correspond to the selection criteria.
     *
     * @param number the number of nodes
     * @param selectionScript criterion to be verified by the returned nodes
     * @return a list of nodes
     */
    @Deprecated
    NodeSet getAtMostNodes(int number, SelectionScript selectionScript);

    /**
     * Finds "number" nodes for computations according to the selection script.
     * All nodes which are returned to the client as marked internally as busy and cannot
     * be used by others until the client frees them.
     * <p>
     * If the resource manager does not have enough nodes it returns as much as it
     * has, but only those which correspond the to selection criteria.
     *
     * @param number the number of nodes
     * @param selectionScript criterion to be verified by the returned nodes
     * @param exclusion a list of node which should not be in the result set
     * @return a list of nodes
     */
    @Deprecated
    NodeSet getAtMostNodes(int number, SelectionScript selectionScript, NodeSet exclusion);

    /**
     * Finds "number" nodes for computations according to the selection scripts
     * (node must be complaint to all scripts).
     * All nodes which are returned to the client as marked internally as busy and cannot
     * be used by others until the client frees them.
     * <p>
     * If the resource manager does not have enough nodes it returns as much as it
     * has, but only those which correspond the to selection criteria.
     *
     * @param number the number of nodes
     * @param selectionScriptsList criteria to be verified by the returned nodes
     * @param exclusion a list of node which should not be in the result set
     * @return a list of nodes
     */
    @Deprecated
    NodeSet getAtMostNodes(int number, List<SelectionScript> selectionScriptsList, NodeSet exclusion);

    /**
     * Finds "number" nodes for computations according to the selection scripts
     * (node must be complaint to all scripts).
     * All nodes which are returned to the client as marked internally as busy and cannot
     * be used by others until the client frees them.
     * <p>
     * If the resource manager does not have enough nodes it returns as much as it
     * has, but only those which correspond the to selection criteria.
     *
     * @param number the number of nodes
     * @param descriptor the topology descriptor of nodes 
     * @see TopologyDescriptor
     * @param selectionScriptsList criteria to be verified by the returned nodes
     * @param exclusion a list of node which should not be in the result set
     * @return a list of nodes
     */
    @Deprecated
    NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion);

    /**
     * Finds "number" nodes for computations according to the selection scripts
     * (node must be complaint to all scripts).
     * All nodes which are returned to the client as marked internally as busy and cannot
     * be used by others until the client frees them.
     * <p>
     * If the resource manager does not have enough nodes the result depends on the bestEffort
     * mode. If set to true, the method returns as many node as it has, 
     * but only those which correspond the to selection criteria. If bestEffort set to false
     * the method returns either 0 or all required nodes.
     * 
     *
     * @param number the number of nodes
     * @param descriptor the topology descriptor of nodes 
     * @see TopologyDescriptor
     * @param selectionScriptsList criteria to be verified by the returned nodes
     * @param exclusion a list of node which should not be in the result set
     * @param bestEffort the mode of node aggregation
     *  
     * @return a list of nodes
     */
    @Deprecated
    NodeSet getNodes(int number, TopologyDescriptor descriptor, List<SelectionScript> selectionScriptsList,
            NodeSet exclusion, boolean bestEffort);

    /**
     * Finds and books nodes for computations.
     * Nodes should satisfy specified criteria. 
     * 
     * @param criteria criteria to select nodes
     * @see Criteria
     * @return a list of nodes according to the criteria
     */
    NodeSet getNodes(Criteria criteria);

    /**
     * Releases the node after computations. The specified node is marked as free and become
     * available to other users.
     *
     * @param node the node to be released
     * @return true if the node has been released successfully, runtime exception otherwise.
     * {@link SecurityException} may be thrown if the user does not have right to release the node or it tries to release
     * a foreign node.
     */
    BooleanWrapper releaseNode(Node node);

    /**
     * Releases nodes after computations. The specified node is marked as free and become
     * available to other users.
     *
     * @param nodes the set of nodes to be released
     * @return true if nodes have been released successfully, runtime exception otherwise.
     * {@link SecurityException} may be thrown if the user does not have right to release one of nodes or it tries to release
     * a foreign node.
     */
    BooleanWrapper releaseNodes(NodeSet nodes);

    /**
     * Disconnects from resource manager and releases all the nodes taken by user for computations.
     *
     * @return true if successfully disconnected, runtime exception otherwise
     */
    BooleanWrapper disconnect();

    /**
     * Initiate the shutdowns the resource manager. During the shutdown resource manager
     * removed all the nodes and kills them if necessary.
     * <p>
     * {@link RMEvent}(SHUTDOWN) will be send when the shutdown is finished.
     *
     * @return true if the shutdown process is successfully triggered, runtime exception otherwise
     */
    BooleanWrapper shutdown(boolean preempt);

    /**
     * Returns the topology information of nodes.
     * @return nodes topology
     */
    Topology getTopology();

    /**
     * Checks if the currently connected user is the node administrator
     * @return true if yes, false otherwise
     */
    BooleanWrapper isNodeAdmin(String nodeUrl);

    /**
     * Checks if the currently connected user can use node for computations
     * @return true if yes, false otherwise
     */
    BooleanWrapper isNodeUser(String nodeUrl);

    /**
     * Executes the script on the specified targets depending on the target type.
     *
     * @param script a selection script to execute.
     * @param targetType must be either NODE_URL, NODESOURCE_NAME or HOSTNAME
     * @param targets are names of particular resources
     *
     * @return the {@link ScriptResult} corresponding to the script execution.
     */
    <T> List<ScriptResult<T>> executeScript(Script<T> script, String targetType, Set<String> targets);

    /**
     * Executes the script on the specified targets depending on the target type.
     *
     * @param script a selection script to execute.
     * @param scriptEngine script engine name
     * @param targetType must be either NODE_URL, NODESOURCE_NAME or HOSTNAME
     * @param targets are names of particular resources
     * @return the {@link ScriptResult} corresponding to the script execution.
     */
    List<ScriptResult<Object>> executeScript(String script, String scriptEngine, String targetType,
            Set<String> targets);
}
