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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCM_NODEALLOC_LOGGER;
public class VirtualNodeImpl implements VirtualNodeInternal {

    /** The, unique, name of this Virtual Node */
    private String id;

    /** The number of Node awaited
     *
     * If 0 the Virtual Node will try to get as many node as possible
     */
    private long requiredCapacity;
    Set<NodeProviderContract> nodeProvidersContracts;
    Set<Node> nodes;

    /** All File Transfer Block associated to this VN */
    private List<FileTransferBlock> fts;

    public VirtualNodeImpl() {
        fts = new ArrayList<FileTransferBlock>();
        nodeProvidersContracts = new HashSet<NodeProviderContract>();
        nodes = Collections.synchronizedSet(new HashSet<Node>());
    }

    public long getRequiredCapacity() {
        return requiredCapacity;
    }

    public void setRequiredCapacity(long requiredCapacity) {
        this.requiredCapacity = requiredCapacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addProvider(GCMDeploymentDescriptor provider, long capacity) {
        NodeProviderContract contract = new NodeProviderContract(provider,
                capacity);
        nodeProvidersContracts.add(contract);
    }

    public void addFileTransfertBlock(FileTransferBlock ftb) {
        fts.add(ftb);
    }

    public String getName() {
        return id;
    }

    public void check() throws IllegalStateException {
        if (nodeProvidersContracts.isEmpty()) {
            throw new IllegalStateException("providers is empty in " + this);
        }
    }

    public void checkDirectMode() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    public DeploymentTree getDeploymentTree() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    private void addNode(Node node) {
        GCM_NODEALLOC_LOGGER.debug("Node " +
            node.getNodeInformation().getURL() + " attached to " + getName());
        nodes.add(node);
    }

    public boolean hasContractWith(GCMDeploymentDescriptor nodeProvider) {
        return null != findNodeProviderContract(nodeProvider);
    }

    public boolean hasUnsatisfiedContract() {
        for (NodeProviderContract nodeProviderContract : nodeProvidersContracts) {
            if (nodeProviderContract.needNode()) {
                return true;
            }
        }
        return false;
    }

    public boolean doesNodeProviderNeed(Node node,
        GCMDeploymentDescriptor nodeProvider) {
        NodeProviderContract contract = findNodeProviderContract(nodeProvider);

        if ((contract != null) && contract.doYouNeed(node, nodeProvider) &&
                (needNode() || isGreedy())) {
            addNode(node);
            return true;
        }

        return false;
    }

    public boolean doYouNeed(Node node, GCMDeploymentDescriptor nodeProvider) {
        if (!needNode()) {
            return false;
        }

        NodeProviderContract contract = findNodeProviderContract(nodeProvider);
        if ((contract != null)) {
            if (contract.isGreedy()) {
                addNode(node);
                return true;
            } else {
                GCM_NODEALLOC_LOGGER.warn("Bug !", new Exception());
            }
        }
        return false;
    }

    public boolean doYouWant(Node node, GCMDeploymentDescriptor nodeProvider) {
        if (!isGreedy()) {
            return false;
        }

        NodeProviderContract contract = findNodeProviderContract(nodeProvider);
        if ((contract != null) && contract.isGreedy()) {
            addNode(node);
            return true;
        }

        return false;
    }

    public NodeProviderContract findNodeProviderContract(
        GCMDeploymentDescriptor nodeProvider) {
        for (NodeProviderContract nodeProviderContract : nodeProvidersContracts) {
            if (nodeProvider == nodeProviderContract.getNodeProvider()) {
                return nodeProviderContract;
            }
        }

        return null;
    }

    public boolean isGreedy() {
        return requiredCapacity == MAX_CAPACITY;
    }

    public boolean needNode() {
        return !isGreedy() && (nodes.size() < requiredCapacity);
    }

    public boolean wantNode() {
        return needNode() || isGreedy();
    }

    private boolean isProvider(GCMDeploymentDescriptor nodeProvider) {
        return findNodeProviderContract(nodeProvider) != null;
    }

    class NodeProviderContract {
        GCMDeploymentDescriptor nodeProvider;
        long capacity;
        long nodes;

        NodeProviderContract(GCMDeploymentDescriptor nodeProvider, long capacity) {
            this.nodeProvider = nodeProvider;
            this.capacity = capacity;
            this.nodes = 0;
        }

        public boolean doYouNeed(Node node, GCMDeploymentDescriptor nodeProvider) {
            if (this.nodeProvider != nodeProvider) {
                return false;
            }

            if (isGreedy()) {
                return false;
            }

            if (nodes >= capacity) {
                return false;
            }

            nodes++;
            return true;
        }

        public boolean doYouWant(Node node, GCMDeploymentDescriptor nodeProvider) {
            if (this.nodeProvider != nodeProvider) {
                return false;
            }

            if (!isGreedy()) {
                return false;
            }

            nodes++;
            return true;
        }

        public boolean isGreedy() {
            return capacity == MAX_CAPACITY;
        }

        public boolean needNode() {
            return !isGreedy() && (this.nodes < this.capacity);
        }

        public GCMDeploymentDescriptor getNodeProvider() {
            return nodeProvider;
        }

        public void addNode(Node node) {
            nodes++;
        }
    }
}
