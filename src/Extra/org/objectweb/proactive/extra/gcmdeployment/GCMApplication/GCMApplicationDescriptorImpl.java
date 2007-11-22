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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentResources;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentNode;
import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentTree;
import org.objectweb.proactive.extra.gcmdeployment.core.VMNodeList;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.process.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public class GCMApplicationDescriptorImpl
    implements GCMApplicationDescriptorInternal {

    /** descriptor file */
    private File descriptor = null;

    /** GCM Application parser (statefull) */
    private GCMApplicationParser parser = null;

    /** All Node Providers referenced by the Application descriptor */
    private Map<String, NodeProvider> nodeProviders = null;

    /** Defined Virtual Nodes */
    private Map<String, VirtualNodeInternal> virtualNodes = null;

    /** The Deployment Tree*/
    private DeploymentTree deploymentTree;

    /** A mapping to associate deployment IDs to Node Provider */
    private Map<Long, NodeProvider> deploymentIdToNodeProviderMapping;

    /** The Command builder to use to start the deployment */
    private CommandBuilder commandBuilder;

    /** The node allocator in charge of Node dispatching */
    private NodeAllocator nodeAllocator;
    private ArrayList<String> currentDeploymentPath;
    private Set<Node> nodes;
    private boolean isStarted;

    public GCMApplicationDescriptorImpl(String filename)
        throws ProActiveException {
        this(new File(filename));
    }

    public GCMApplicationDescriptorImpl(File file) throws ProActiveException {
        try {
            currentDeploymentPath = new ArrayList<String>();
            deploymentIdToNodeProviderMapping = new HashMap<Long, NodeProvider>();
            nodes = Collections.synchronizedSet(new HashSet<Node>());
            isStarted = false;

            descriptor = Helpers.checkDescriptorFileExist(file);
            parser = new GCMApplicationParserImpl(descriptor);
            nodeProviders = parser.getNodeProviders();
            virtualNodes = parser.getVirtualNodes();
            commandBuilder = parser.getCommandBuilder();
            nodeAllocator = new NodeAllocator(this, virtualNodes.values());
        } catch (Exception e) {
            GCMA_LOGGER.warn("GCM Application Descriptor cannot be created", e);
            throw new ProActiveException(e);
        }
    }

    /* -----------------------------
     *  GCMApplicationDescriptor interface
     */
    public void startDeployment() {
        if (isStarted) {
            GCMA_LOGGER.warn("A GCM Application descriptor cannot be started twice",
                new Exception());
        }

        isStarted = true;
        buildDeploymentTree();

        for (NodeProvider gdd : nodeProviders.values()) {
            gdd.start(commandBuilder);
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public VirtualNode getVirtualNode(String vnName) {
        return virtualNodes.get(vnName);
    }

    public Map<String, ?extends VirtualNode> getVirtualNodes() {
        return virtualNodes;
    }

    public void kill() {
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

    public Set<Node> getCurrentNodes() {
        return new HashSet<Node>(nodes);
    }

    public DeploymentTree getCurrentTopology() {
        // TODO cmathieu
        return null;
    }

    public Set<Node> getCurrentUnusedNodes() {
        return nodeAllocator.getUnusedNode();
    }

    /* -----------------------------
     *  GCMApplicationDescriptorInternal interface
     */
    public NodeProvider getNodeProviderFromDeploymentId(Long deploymentNodeId) {
        return deploymentIdToNodeProviderMapping.get(deploymentNodeId);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    /* -----------------------------
     *  Internal Methods
     */
    protected void buildDeploymentTree() {
        deploymentTree = new DeploymentTree();

        // make root node from local JVM
        DeploymentNode rootNode = new DeploymentNode();

        rootNode.setDeploymentDescriptorPath(""); // no deployment descriptor
                                                  // here

        try {
            rootNode.setApplicationDescriptorPath(descriptor.getCanonicalPath());
        } catch (IOException e) {
            rootNode.setApplicationDescriptorPath("");
        }

        currentDeploymentPath.clear();

        ProActiveRuntimeImpl proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        VMNodeList vmNodeList = new VMNodeList(proActiveRuntime.getVMInformation());
        currentDeploymentPath.add(proActiveRuntime.getVMInformation().getName());

        // vmNodes.addNode(<something>); - TODO cmathieu
        rootNode.addVMNodes(vmNodeList);
        rootNode.setDeploymentPath(getCurrentdDeploymentPath());

        deploymentTree.setRootNode(rootNode);

        // Build leaf nodes
        for (NodeProvider nodeProvider : nodeProviders.values()) {
            for (GCMDeploymentDescriptor gdd : nodeProvider.getDescriptors()) {
                GCMDeploymentDescriptorImpl gddi = (GCMDeploymentDescriptorImpl) gdd;
                GCMDeploymentResources resources = gddi.getResources();

                HostInfo hostInfo = resources.getHostInfo();
                if (hostInfo != null) {
                    buildHostInfoTreeNode(rootNode, hostInfo, nodeProvider);
                }

                for (Group group : resources.getGroups()) {
                    buildGroupTreeNode(rootNode, group, nodeProvider);
                }

                for (Bridge bridge : resources.getBridges()) {
                    buildBridgeTree(rootNode, bridge, nodeProvider);
                }
            }
        }
    }

    /**
     * return a copy of the current deployment path
     *
     * @return
     */
    private List<String> getCurrentdDeploymentPath() {
        return new ArrayList<String>(currentDeploymentPath);
    }

    private void buildHostInfoTreeNode(DeploymentNode rootNode,
        HostInfo hostInfo, NodeProvider nodeProvider) {
        DeploymentNode deploymentNode = new DeploymentNode();
        deploymentNode.setDeploymentDescriptorPath(rootNode.getDeploymentDescriptorPath());
        pushDeploymentPath(hostInfo.getId());
        hostInfo.setDeploymentId(deploymentNode.getId());
        deploymentIdToNodeProviderMapping.put(deploymentNode.getId(),
            nodeProvider);
        deploymentTree.addNode(deploymentNode, rootNode);
    }

    private void buildGroupTreeNode(DeploymentNode rootNode, Group group,
        NodeProvider nodeProvider) {
        DeploymentNode deploymentNode = new DeploymentNode();
        deploymentNode.setDeploymentDescriptorPath(rootNode.getDeploymentDescriptorPath());
        HostInfo hostInfo = group.getHostInfo();
        pushDeploymentPath(hostInfo.getId());
        hostInfo.setDeploymentId(deploymentNode.getId());
        deploymentIdToNodeProviderMapping.put(deploymentNode.getId(),
            nodeProvider);
        deploymentTree.addNode(deploymentNode, rootNode);
        popDeploymentPath();
    }

    private void buildBridgeTree(DeploymentNode baseNode, Bridge bridge,
        NodeProvider nodeProvider) {
        DeploymentNode deploymentNode = new DeploymentNode();
        deploymentNode.setDeploymentDescriptorPath(baseNode.getDeploymentDescriptorPath());

        pushDeploymentPath(bridge.getId());

        // first look for a host info...
        //
        if (bridge.getHostInfo() != null) {
            HostInfo hostInfo = bridge.getHostInfo();
            pushDeploymentPath(hostInfo.getId());
            hostInfo.setDeploymentId(deploymentNode.getId());
            deploymentIdToNodeProviderMapping.put(deploymentNode.getId(),
                nodeProvider);
            deploymentTree.addNode(deploymentNode, baseNode);
            popDeploymentPath();
        }

        // then groups...
        //
        if (bridge.getGroups() != null) {
            for (Group group : bridge.getGroups()) {
                buildGroupTreeNode(deploymentNode, group, nodeProvider);
            }
        }

        // then bridges (and recurse)
        //
        if (bridge.getBridges() != null) {
            for (Bridge subBridge : bridge.getBridges()) {
                buildBridgeTree(deploymentNode, subBridge, nodeProvider);
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
