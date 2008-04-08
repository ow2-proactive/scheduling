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
package org.objectweb.proactive.extensions.gcmdeployment.core;

import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesProperties;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl.NodeProviderContract;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * The internal part of the Virtual Node API.
 *
 * Methods of this API are not for ProActive users.
 *
 */
public interface GCMVirtualNodeInternal extends GCMVirtualNode {

    /**
     * Creates a new Node Provider Contract with the specified capacity
     *
     * @param provider the Node Provider
     * @param capacity the number of Node to be attached
     * @throws IllegalStateException if a contract with the provider already exists
     */
    public void addNodeProviderContract(NodeProvider provider,
            TechnicalServicesProperties associatedTSProperties, long capacity);

    /**
     * Offers the Node to the Virtual Node to satisfy a Node Provider Contract
     *
     * The Node is attached to the Virtual Node if:
     * <ol>
     *  <li>The VN is not Ready</li>
     *         <li>The VN has an unsatisfied, non greedy, Contract with nodeProvider</li>
     * </ol>
     *
     * If true is returned, the Node is attached to the Virtual Node and cannot be used
     * again (a Node can belong to one and only one Virtual Node).
     *
     * @param fakeNode offered Node
     * @param nodeProvider provider of the Node
     * @return true if the Node can satisfy a Node Provider Contract, false otherwise
     */
    public boolean doesNodeProviderNeed(FakeNode fakeNode, NodeProvider nodeProvider);

    /**
     *  Offers the Node to the Virtual Node to satisfy Virtual Node Capacity
     *
     *  The node is attached to the Virtual Node if:
     *  <ol>
     *          <li>All Virtual Node which have a non Greedy contract with the Node Provider, have satisfied their Node Provider Contract</li>
     *          <li>The Virtual Node is not greedy and its capacity is not satisfied</li>
     *  </ol>
     *
     * @param node offered Node
     * @param nodeProvider provider of the Node
     * @return true if the Node can satisfy Virtual Node capacity, false otherwise
     */
    public boolean doYouNeed(FakeNode fakeNode, NodeProvider nodeProvider);

    /**
     * Offers the Node to the Virtual Node to satisfy greedy Virtual Node
     *
     * The Node is attached to the Virtual Node if:
     * <ol>
     *         <li>The Virtual Node is Greedy</li>
     *         <li>The Node Provider Contract is Greedy</li>
     * </ol>
     * @param node offered Node
     * @param nodeProvider provider of the Node
     * @return true if the Virtual Node is greedy, false otherwise
     */
    public boolean doYouWant(FakeNode fakeNode, NodeProvider nodeProvider);

    /**
     * Indicates if the Virtual Node has a contract with this Node Provider
     *
     * @param nodeProvider a Node Provider
     * @return true if a contract exists between the Virtual Node and the Node Provider, false
     * otherwise
     */
    public boolean hasContractWith(NodeProvider nodeProvider);

    /**
     * Indicates if the Virtual Node has at lest one unsatisfied Node Provider Contract
     *
     * @return true if a least one contract is not satisfied, false otherwise
     */
    public boolean hasUnsatisfiedContract();

    public void setCapacity(long capacity);

    public void setName(String name);

    public void setDeploymentTree(TopologyRootImpl deploymentTree);

    public void addNode(FakeNode fakeNode);

    public void addNode(FakeNode fakeNode, NodeProviderContract contract);

}
