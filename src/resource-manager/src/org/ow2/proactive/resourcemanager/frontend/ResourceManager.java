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
package org.ow2.proactive.resourcemanager.frontend;

import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * This class represents the interface of the resource manager.
 * <p>
 * The resource manager is used to aggregate resources across the network which are represented by ProActive nodes.
 * Its main features are
 * <ul>
 * <li> deployment, acquisition and release of ProActive nodes to/from an underlying infrastructure</li>
 * </li> providing nodes for computations, based on clients criteria (@see {@link SelectionScript})</li>
 * </li> maintaining and monitoring its list of resources and managing their states (free, busy, down...)</li>
 * </ul>
 *
 * <p>
 * This interface provides means to create/remove node sources in the resource manager, add remove nodes to node sources,
 * track the state of the resource manager and get nodes for computations.
 *
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
     * @param infrastructureType type of the underlying infrastructure {@link InfrastructureType}
     * @param infrastructureParameters parameters for infrastructure creation
     * @param policyType name of the policy type. It passed as a string due to pluggable approach {@link NodeSourcePolicyFactory}
     * @param policyParameters parameters for policy creation
     * @return true if a new node source was created successfully, runtime exception otherwise
     */
    public BooleanWrapper createNodeSource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters);

    /**
     * Remove a node source from the RM.
     * All nodes handled by the node source are removed.
     *
     * @param sourceName name of the source to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @return true if the node source was removed successfully, runtime exception otherwise
     */
    public BooleanWrapper removeNodeSource(String sourceName, boolean preempt);

    /**
     * Returns the list of supported node source infrastructures descriptors.
     *
     * @return the list of supported node source infrastructures descriptors
     */
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures();

    /**
     * Returns the list of supported node source policies descriptors.
     *
     * @return the list of supported node source policies descriptors
     */
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies();

    /**
     * Each node source scan its nodes periodically to check their states.
     * This method changes the period of nodes scanning.
     *
     * @param frequency the frequency to set to the node source in ms.
     * @param sourceName name of the node source to set the frequency
     * @return true if ping frequency is successfully changed, runtime exception otherwise
     */
    public BooleanWrapper setNodeSourcePingFrequency(int frequency, String sourceName);

    /**
     * Returns the ping frequency of a node source.
     *
     * @param sourceName name of the node source
     * @return the ping frequency
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName);

    /**
     * Adds an existing node to the default node source of the resource manager.
     *
     * @param nodeUrl URL of the node to add.
     * @return true if new node is added successfully, runtime exception otherwise
     */
    public BooleanWrapper addNode(String nodeUrl);

    /**
     * Adds an existing node to the particular node source.
     *
     * @param nodeUrl URL of the node to add.
     * @param sourceName name of the static node source that will handle the node
     * @return true if new node is added successfully, runtime exception otherwise
     */
    public BooleanWrapper addNode(String nodeUrl, String sourceName);

    /**
     * Removes a node from the resource manager.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @return true if the node is removed successfully, false or exception otherwise
     */
    public BooleanWrapper removeNode(String nodeUrl, boolean preempt);

    /**
     * Returns true if the node nodeUrl is registered (i.e. known by the RM) and not down.
     *
     * @param nodeUrl the tested node.
     * @return true if the node nodeUrl is registered and not down.
     */
    public BooleanWrapper nodeIsAvailable(String nodeUrl);

    /**
     * Returns true if the resource manager is operational.
     *
     * @return true if the resource manager is operational, false otherwise
     */
    public BooleanWrapper isActive();

    /**
     * Returns the resource manager summary state.
     * To retrieve detailed state use {@link RMMonitoring}.getState() method.
     *
     * @return the resource manager summary state.
     */
    public RMState getState();

    /**
     * Returns the monitoring interface to manager listeners of the resource manager.
     *
     * @return the resource manager monitoring interface
     */
    public RMMonitoring getMonitoring();

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
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript);

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
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript, NodeSet exclusion);

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
     * @param selectionScriptList criteria to be verified by the returned nodes
     * @param exclusion a list of node which should not be in the result set
     * @return a list of nodes
     */
    public NodeSet getAtMostNodes(int number, List<SelectionScript> selectionScriptsList, NodeSet exclusion);

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
     * @see {@link TopologyDescriptor}
     * @param selectionScriptList criteria to be verified by the returned nodes
     * @param exclusion a list of node which should not be in the result set
     * @return a list of nodes
     */
    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor,
            List<SelectionScript> selectionScriptsList, NodeSet exclusion);

    /**
     * Releases the node after computations. The specified node is marked as free and become
     * available to other users.
     *
     * @param the set of nodes to be released
     * @return true if the node has been released successfully, runtime exception otherwise.
     * {@link SecurityException} may be thrown if the user does not have right to release the node or it tries to release
     * a foreign node.
     */
    public BooleanWrapper releaseNode(Node node);

    /**
     * Releases nodes after computations. The specified node is marked as free and become
     * available to other users.
     *
     * @param the set of nodes to be released
     * @return true if nodes have been released successfully, runtime exception otherwise.
     * {@link SecurityException} may be thrown if the user does not have right to release one of nodes or it tries to release
     * a foreign node.
     */
    public BooleanWrapper releaseNodes(NodeSet nodes);

    /**
     * Disconnects from resource manager and releases all the nodes taken by user for computations.
     *
     * @return true if successfully disconnected, runtime exception otherwise
     */
    public BooleanWrapper disconnect();

    /**
     * Initiate the shutdowns the resource manager. During the shutdown resource manager
     * removed all the nodes and kills them if necessary.
     * <p>
     * {@link RMEvent}(SHUTDOWN) will be send when the shutdown is finished.
     *
     * @return true if the shutdown process is successfully triggered, runtime exception otherwise
     */
    public BooleanWrapper shutdown(boolean preempt);

    /**
     * Returns the topology information of nodes.
     * @return nodes topology
     */
    public Topology getTopology();
}
