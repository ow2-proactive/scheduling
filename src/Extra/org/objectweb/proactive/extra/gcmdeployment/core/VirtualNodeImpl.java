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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.NodeProvider;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCM_NODEALLOC_LOGGER;
public class VirtualNodeImpl implements VirtualNodeInternal {

    /** unique name (declared by GCMA) */
    private String id;

    /** capacity (declared by GCMA) */
    private long capacity;
    /** All Node Provider Contracts (declared by GCMA) */
    Set<NodeProviderContract> nodeProvidersContracts;
    /** All the Nodes attached to this VN */
    Set<Node> nodes;
    Set<Node> previousNodes;
    Set<Subscriber> nodeAttachmentSubscribers;
    Set<Subscriber> isReadySubscribers;
    TopologyRootImpl deploymentTree;

    public VirtualNodeImpl() {
        nodeProvidersContracts = new HashSet<NodeProviderContract>();
        nodes = Collections.synchronizedSet(new HashSet<Node>());

        nodeAttachmentSubscribers = Collections.synchronizedSet(new HashSet<Subscriber>());
        isReadySubscribers = Collections.synchronizedSet(new HashSet<Subscriber>());
    }

    /*
     * ------------------- VirtualNode interface
     */
    public String getName() {
        return new String(id);
    }

    public boolean isGreedy() {
        return capacity == MAX_CAPACITY;
    }

    public boolean isReady() {
        return (!hasUnsatisfiedContract()) && (!needNode());
    }

    public long getNbRequiredNodes() {
        long acc = 0;
        for (NodeProviderContract contract : nodeProvidersContracts) {
            if (!contract.isGreedy()) {
                acc += contract.capacity;
            }
        }

        return Math.max(Math.max(capacity, acc), 0);
    }

    public long getNbCurrentNodes() {
        return nodes.size();
    }

    public Set<Node> getCurrentNodes() {
        return new HashSet<Node>(nodes);
    }

    public Set<Node> getNewNodes() {
        Set<Node> ret = new HashSet<Node>(nodes);
        if (previousNodes == null) {
            previousNodes = new HashSet<Node>(nodes);
        } else {
            ret.removeAll(previousNodes);
        }

        return ret;
    }

    public boolean subscribeNodeAttachment(Object client, String methodeName) {
        if ((client == null) || (methodeName == null)) {
            return false;
        }

        Class<?> cl = client.getClass();
        try {
            cl.getMethod(methodeName, Node.class, VirtualNode.class);
            nodeAttachmentSubscribers.add(new Subscriber(client, methodeName));
        } catch (NoSuchMethodException e) {
            GCM_NODEALLOC_LOGGER.warn("Method " + methodeName +
                "(Node, VirtualNode) cannot be found on " + cl.getSimpleName());
            return false;
        }

        return true;
    }

    public void unsubscribeNodeAttachment(Object client, String methodeName) {
        nodeAttachmentSubscribers.remove(new Subscriber(client, methodeName));
    }

    public boolean subscribeIsReady(Object client, String methodeName) {
        if (isGreedy() || (client == null) || (methodeName == null)) {
            return false;
        }

        Class<?> cl = client.getClass();
        try {
            cl.getMethod(methodeName, VirtualNode.class);
            isReadySubscribers.add(new Subscriber(client, methodeName));
        } catch (NoSuchMethodException e) {
            GCM_NODEALLOC_LOGGER.warn("Method " + methodeName +
                "(VirtualNode) cannot be found on " + cl.getSimpleName());
            return false;
        }

        return true;
    }

    public void unsubscribeIsReady(Object client, String methodeName) {
        isReadySubscribers.remove(new Subscriber(client, methodeName));
    }

    public Topology getCurrentTopology() {
        return TopologyImpl.createTopology(deploymentTree, nodes);
    }

    public void updateTopology(Topology topology) {
        TopologyImpl.updateTopology(topology, nodes);
    }

    /* -------------------
     * VirtualNodeInternal interface
     */
    public void addNodeProviderContract(NodeProvider provider, long capacity) {
        if (findNodeProviderContract(provider) != null) {
            throw new IllegalStateException("A contract with the provider " +
                provider.getId() + " already exist for " + id);
        }

        nodeProvidersContracts.add(new NodeProviderContract(provider, capacity));
    }

    public boolean doesNodeProviderNeed(Node node, NodeProvider nodeProvider) {
        NodeProviderContract contract = findNodeProviderContract(nodeProvider);

        if ((contract != null) && contract.doYouNeed(node, nodeProvider) &&
                (needNode() || isGreedy())) {
            addNode(node);
            return true;
        }

        return false;
    }

    public boolean doYouNeed(Node node, NodeProvider nodeProvider) {
        if (!needNode()) {
            return false;
        }

        NodeProviderContract contract = findNodeProviderContract(nodeProvider);
        if ((contract != null) && contract.isGreedy()) {
            contract.addNode(node);
            addNode(node);
            return true;
        }

        return true;
    }

    public boolean doYouWant(Node node, NodeProvider nodeProvider) {
        if (!isGreedy()) {
            return false;
        }

        NodeProviderContract contract = findNodeProviderContract(nodeProvider);
        if ((contract != null) && contract.isGreedy()) {
            contract.addNode(node);
            addNode(node);
            return true;
        }

        return false;
    }

    public boolean hasContractWith(NodeProvider nodeProvider) {
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

    public void setNbRequiredNodes(long capacity) {
        this.capacity = capacity;
    }

    public void setName(String id) {
        this.id = id;
    }

    public void setDeploymentTree(TopologyRootImpl deploymentTree) {
        this.deploymentTree = deploymentTree;
    }

    /* -------------------
     * Private Helpers
     */
    private void addNode(Node node) {
        GCM_NODEALLOC_LOGGER.debug("Node " +
            node.getNodeInformation().getURL() + " attached to " + getName());
        nodes.add(node);

        for (Subscriber subscriber : nodeAttachmentSubscribers) {
            Class<?> cl = subscriber.getClient().getClass();
            try {
                Method m = cl.getMethod(subscriber.getMethod(), Node.class,
                        VirtualNode.class);
                m.invoke(subscriber.getClient(), node, this);
            } catch (Exception e) {
                GCM_NODEALLOC_LOGGER.warn(e);
            }
        }

        if (isReady()) {
            for (Subscriber subscriber : isReadySubscribers) {
                Class<?> cl = subscriber.getClient().getClass();
                try {
                    Method m = cl.getMethod(subscriber.getMethod(),
                            VirtualNode.class);
                    m.invoke(subscriber.getClient(), this);
                    isReadySubscribers.remove(subscriber);
                } catch (Exception e) {
                    GCM_NODEALLOC_LOGGER.warn(e);
                }
            }
        }
    }

    private NodeProviderContract findNodeProviderContract(
        NodeProvider nodeProvider) {
        for (NodeProviderContract nodeProviderContract : nodeProvidersContracts) {
            if (nodeProvider == nodeProviderContract.getNodeProvider()) {
                return nodeProviderContract;
            }
        }

        return null;
    }

    private boolean needNode() {
        return !isGreedy() && (nodes.size() < capacity);
    }

    private boolean wantNode() {
        return needNode() || isGreedy();
    }

    class NodeProviderContract {
        NodeProvider nodeProvider;
        long capacity;
        long nodes;

        NodeProviderContract(NodeProvider nodeProvider, long capacity) {
            this.nodeProvider = nodeProvider;
            this.capacity = capacity;
            this.nodes = 0;
        }

        public boolean doYouNeed(Node node, NodeProvider nodeProvider) {
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

        public boolean isGreedy() {
            return capacity == MAX_CAPACITY;
        }

        public boolean needNode() {
            return !isGreedy() && (this.nodes < this.capacity);
        }

        public NodeProvider getNodeProvider() {
            return nodeProvider;
        }

        public void addNode(Node node) {
            nodes++;
        }
    }

    private class Subscriber {
        private Object client;
        private String method;

        public Subscriber(Object client, String method) {
            this.client = client;
            this.method = method;
        }

        public Object getClient() {
            return client;
        }

        public void setClient(Object client) {
            this.client = client;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) +
                ((client == null) ? 0 : client.hashCode());
            result = (prime * result) +
                ((method == null) ? 0 : method.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Subscriber other = (Subscriber) obj;
            if (client == null) {
                if (other.client != null) {
                    return false;
                }
            } else if (!client.equals(other.client)) {
                return false;
            }
            if (method == null) {
                if (other.method != null) {
                    return false;
                }
            } else if (!method.equals(other.method)) {
                return false;
            }
            return true;
        }
    }
}
