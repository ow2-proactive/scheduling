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
package org.ow2.proactive.resourcemanager.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicyFactory;


/**
 * An interface Front-End for the {@link RMAdminImpl} active object.
 * this Resource Manager object is designed to receive and perform
 * administrator commands :<BR>
 * -initiate creation and removal of {@link org.ow2.proactive.resourcemanager.nodesource.frontend#NodeSource} active objects.<BR>
 * -add nodes to a static node source ({@link org.ow2.proactive.resourcemanager.nodesource.deprecated.GCMNodeSource}), by
 * a ProActive descriptor.<BR>
 * -remove nodes from the RM.<BR>
 * -shutdown the RM.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public interface RMAdmin extends RMUser, Serializable {

    /**
     * Set the ping frequency to the default node source
     * @param frequency the frequency to set to the node source in ms.
     */
    public void setDefaultNodeSourcePingFrequency(int frequency);

    /**
     * Set the ping frequency to a node source
     * @param frequency the frequency to set to the node source in ms.
     * @param sourceName name of the node source to set the frequency
     * @throws RMException if the node source doesn't exist
     */
    public void setNodeSourcePingFrequency(int frequency, String sourceName) throws RMException;

    /**
     * Set the ping frequency to all nodes sources.
     * @param frequency the frequency to set to the node sources in ms.
     */
    public void setAllNodeSourcesPingFrequency(int frequency);

    /**
     * Return the Ping frequency of a node source
     * @param sourceName name of the node source
     * @return the ping frequency
     * @throws RMException if the node source doesn't exist
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) throws RMException;

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
    public void createNodesource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters)
            throws RMException;

    /**
     * Adds nodes to the specified node source.
     *
     * @param sourceName a name of the node source
     * @param parameters information necessary to deploy nodes. Specific to each infrastructure.
     * @throws RMException if any errors occurred
     */
    public void addNodes(String sourceName, Object... parameters) throws RMException;

    /**
     * Add an already deployed node to the default static nodes source of the RM
     * @param nodeUrl URL of the node to add.
     * @throws RMException if a exception occurs during the node registration.
     */
    public void addNode(String nodeUrl) throws RMException;

    /**
     * Add nodes to a StaticNodeSource represented by sourceName.
     * SourceName must exist and must be a static source
     * @param nodeUrl URL of the node to add.
     * @param sourceName name of the static node source that will handle the node
     * @throws RMException if a exception occurs during the node registration,
     * notably if the node source doesn't exist
     */
    public void addNode(String nodeUrl, String sourceName) throws RMException;

    /**
     * Removes a node from the RM.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     */
    public void removeNode(String nodeUrl, boolean preempt);

    /**
     * Removes a node from the RM.
     *
     * @param nodeUrl URL of the node to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @param forever if true remove the from a dynamic node source forever. Otherwise node source
     * is able to add this node to the RM again once it is needed. See {@link NodeSourcePolicy}.
     */
    public void removeNode(String nodeUrl, boolean preempt, boolean forever);

    /**
     * Remove a node source from the RM.
     * All nodes handled by the node source are removed.
     *
     * @param sourceName name (id) of the source to remove.
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @throws RMException if the node source doesn't exists
     */
    public void removeSource(String sourceName, boolean preempt) throws RMException;

    /**
     * Kills Resource Manager
     * @param preempt if true remove the node immediately without waiting while it will be freed.
     * @exception ProActiveException
     *
     */
    public BooleanWrapper shutdown(boolean preempt) throws ProActiveException;

    /**
     * Gets a list of nodes handled by Resource Manager
     * @return a list of RMNodeEvent objects representing the nodes 
     */
    public List<RMNodeEvent> getNodesList();

    /**
     * Get list of nodes sources on Resource Manager
     * @return a list of RMNodeSourceEvent objects representing the nodes sources
     */
    public List<RMNodeSourceEvent> getNodeSourcesList();

    /**
     * Disconnects from resource manager.
     */
    public void disconnect();

    /**
     * Gets a list of supported node source infrastructures
     * @return a list of supported node source infrastructures
     */
    public ArrayList<String> getSupportedNodeSourceInfrastructures();

    /**
     * Gets a list of supported node source policies
     * @return a list of supported node source policies
     */
    public ArrayList<String> getSupportedNodeSourcePolicies();
}
