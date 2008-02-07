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
package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentResources;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.hostinfo.HostInfo;

import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.core.Topology;
import org.objectweb.proactive.extra.gcmdeployment.core.TopologyImpl;
import org.objectweb.proactive.extra.gcmdeployment.core.TopologyRootImpl;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNodeInternal;


public class GCMApplicationDescriptorImpl implements GCMApplicationDescriptorInternal {

    /** An unique identifier for this deployment*/
    private long deploymentId;

    /** descriptor file */
    private File descriptor = null;

    /** GCM Application parser (statefull) */
    private GCMApplicationParser parser = null;

    /** All Node Providers referenced by the Application descriptor */
    private Map<String, NodeProvider> nodeProviders = null;

    /** Defined Virtual Nodes */
    private Map<String, GCMVirtualNodeInternal> virtualNodes = null;

    /** The Deployment Tree*/
    private TopologyRootImpl deploymentTree;

    /** A mapping to associate deployment IDs to Node Provider */
    private Map<Long, NodeProvider> topologyIdToNodeProviderMapping;

    /** The Command builder to use to start the deployment */
    private CommandBuilder commandBuilder;

    /** The node allocator in charge of Node dispatching */
    private NodeMapper nodeAllocator;
    private ArrayList<String> currentDeploymentPath;
    private Set<Node> nodes;
    private Object deploymentMutex = new Object();
    private boolean isStarted;

    private VariableContract vContract;

    public GCMApplicationDescriptorImpl(String filename) throws ProActiveException {
        this(new File(filename), null);
    }

    public GCMApplicationDescriptorImpl(String filename, VariableContract vContract)
            throws ProActiveException {
        this(new File(filename), vContract);
    }

    public GCMApplicationDescriptorImpl(File file) throws ProActiveException {
        this(file, null);
    }

    public GCMApplicationDescriptorImpl(File file, VariableContract vContract) throws ProActiveException {
        try {
            deploymentId = ProActiveRandom.nextPosLong();

            currentDeploymentPath = new ArrayList<String>();
            topologyIdToNodeProviderMapping = new HashMap<Long, NodeProvider>();
            nodes = new HashSet<Node>();
            isStarted = false;

            if (vContract == null) {
                vContract = new VariableContract();
            }
            this.vContract = vContract;

            descriptor = Helpers.checkDescriptorFileExist(file);
            // vContract will be modified by the Parser to include variable defined in the descriptor
            parser = new GCMApplicationParserImpl(descriptor, this.vContract);
            nodeProviders = parser.getNodeProviders();
            virtualNodes = parser.getVirtualNodes();
            commandBuilder = parser.getCommandBuilder();
            nodeAllocator = new NodeMapper(this, virtualNodes.values());
        } catch (Exception e) {
            GCMA_LOGGER.warn("GCM Application Descriptor cannot be created", e);
            throw new ProActiveException(e);
        }
    }

    /*
     * ----------------------------- GCMApplicationDescriptor interface
     */
    public void startDeployment() {
        synchronized (deploymentMutex) {
            if (isStarted) {
                GCMA_LOGGER.warn("A GCM Application descriptor cannot be started twice", new Exception());
            }

            isStarted = true;

            deploymentTree = buildDeploymentTree();
            for (GCMVirtualNodeInternal virtualNode : virtualNodes.values()) {
                virtualNode.setDeploymentTree(deploymentTree);
            }

            for (NodeProvider nodeProvider : nodeProviders.values()) {
                nodeProvider.start(commandBuilder, this);
            }
        }
    }

    public boolean isStarted() {
        synchronized (deploymentMutex) {
            return isStarted;
        }
    }

    public GCMVirtualNode getVirtualNode(String vnName) {
        return virtualNodes.get(vnName);
    }

    public Map<String, ? extends GCMVirtualNode> getVirtualNodes() {
        return virtualNodes;
    }

    public void kill() {
        synchronized (deploymentMutex) {
            Set<ProActiveRuntime> cache = new HashSet<ProActiveRuntime>();

            for (Node node : nodes) {
                try {
                    ProActiveRuntime part = node.getProActiveRuntime();
                    if (!cache.contains(part)) {
                        cache.add(part);
                        part.killRT(false);
                    }
                } catch (Exception e) {
                    // Miam Miam Miam
                }
            }
        }
    }

    public Set<Node> getCurrentNodes() {
        synchronized (nodes) {
            return new HashSet<Node>(nodes);
        }
    }

    public Topology getCurrentTopology() {
        // To not block other threads too long we make a snapshot of the node set
        Set<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new HashSet<Node>(nodes);
        }
        return TopologyImpl.createTopology(deploymentTree, nodesCopied);
    }

    public Set<FakeNode> getCurrentUnusedNodes() {
        return nodeAllocator.getUnusedNode();
    }

    public void updateTopology(Topology topology) {
        // To not block other threads too long we make a snapshot of the node set
        Set<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new HashSet<Node>(nodes);
        }
        TopologyImpl.updateTopology(topology, nodesCopied);
    }

    public VariableContract getVariableContract() {
        return this.vContract;
    }

    /*
     * ----------------------------- GCMApplicationDescriptorInternal interface
     */
    public long getDeploymentId() {
        return deploymentId;
    }

    public NodeProvider getNodeProviderFromTopologyId(Long topologyId) {
        return topologyIdToNodeProviderMapping.get(topologyId);
    }

    public void addNode(Node node) {
        synchronized (nodes) {
            nodes.add(node);
        }
    }

    /*
     * ----------------------------- Internal Methods
     */
    protected TopologyRootImpl buildDeploymentTree() {
        // make root node from local JVM
        TopologyRootImpl rootNode = new TopologyRootImpl();

        ProActiveRuntimeImpl proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        currentDeploymentPath.clear();
        pushDeploymentPath(proActiveRuntime.getVMInformation().getName());

        rootNode.setDeploymentDescriptorPath("none"); // no deployment descriptor here

        try {
            rootNode.setApplicationDescriptorPath(descriptor.getCanonicalPath());
        } catch (IOException e) {
            rootNode.setApplicationDescriptorPath("");
        }
        rootNode.setDeploymentPath(getCurrentdDeploymentPath());
        popDeploymentPath();

        // Build leaf nodes
        for (NodeProvider nodeProvider : nodeProviders.values()) {
            for (GCMDeploymentDescriptor gdd : nodeProvider.getDescriptors()) {
                GCMDeploymentDescriptorImpl gddi = (GCMDeploymentDescriptorImpl) gdd;
                GCMDeploymentResources resources = gddi.getResources();

                HostInfo hostInfo = resources.getHostInfo();
                if (hostInfo != null) {
                    buildHostInfoTreeNode(rootNode, rootNode, hostInfo, nodeProvider, gdd);
                }

                for (Group group : resources.getGroups()) {
                    buildGroupTreeNode(rootNode, rootNode, group, nodeProvider, gdd);
                }

                for (Bridge bridge : resources.getBridges()) {
                    buildBridgeTree(rootNode, rootNode, bridge, nodeProvider, gdd);
                }
            }
        }

        return rootNode;
    }

    /**
     * return a copy of the current deployment path
     *
     * @return
     */
    private List<String> getCurrentdDeploymentPath() {
        return new ArrayList<String>(currentDeploymentPath);
    }

    private TopologyImpl buildHostInfoTreeNode(TopologyRootImpl rootNode, TopologyImpl parentNode,
            HostInfo hostInfo, NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd) {
        pushDeploymentPath(hostInfo.getId());
        TopologyImpl node = new TopologyImpl();
        node.setDeploymentDescriptorPath(gcmd.getDescriptorFilePath());
        node.setApplicationDescriptorPath(rootNode.getApplicationDescriptorPath());
        node.setDeploymentPath(getCurrentdDeploymentPath());
        node.setNodeProvider(nodeProvider.getId());
        hostInfo.setTopologyId(node.getId());
        topologyIdToNodeProviderMapping.put(node.getId(), nodeProvider);
        rootNode.addNode(node, parentNode);
        popDeploymentPath(); // ???
        return node;
    }

    private void buildGroupTreeNode(TopologyRootImpl rootNode, TopologyImpl parentNode, Group group,
            NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd) {
        pushDeploymentPath(group.getId());
        buildHostInfoTreeNode(rootNode, parentNode, group.getHostInfo(), nodeProvider, gcmd);
        popDeploymentPath();
    }

    private void buildBridgeTree(TopologyRootImpl rootNode, TopologyImpl parentNode, Bridge bridge,
            NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd) {
        pushDeploymentPath(bridge.getId());

        TopologyImpl node = parentNode;

        // first look for a host info...
        //
        if (bridge.getHostInfo() != null) {
            HostInfo hostInfo = bridge.getHostInfo();
            node = buildHostInfoTreeNode(rootNode, parentNode, hostInfo, nodeProvider, gcmd);
        }

        // then groups...
        //
        if (bridge.getGroups() != null) {
            for (Group group : bridge.getGroups()) {
                buildGroupTreeNode(rootNode, node, group, nodeProvider, gcmd);
            }
        }

        // then bridges (and recurse)
        if (bridge.getBridges() != null) {
            for (Bridge subBridge : bridge.getBridges()) {
                buildBridgeTree(rootNode, node, subBridge, nodeProvider, gcmd);
            }
        }

        popDeploymentPath();
    }

    private boolean pushDeploymentPath(String pathElement) {
        return currentDeploymentPath.add(pathElement);
    }

    private void popDeploymentPath() {
        currentDeploymentPath.remove(currentDeploymentPath.size() - 1);
    }
}
