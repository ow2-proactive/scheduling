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
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.Executor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentResources;
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
import org.xml.sax.SAXException;


public class GCMApplicationDescriptorImpl implements GCMApplicationDescriptor {

    /** The descriptor file */
    private File gadFile = null;

    /** A parser dedicated to this GCM Application descriptor */
    private GCMApplicationParser gadParser = null;

    /** All the Virtual Nodes defined in this application */
    private Map<String, VirtualNodeInternal> virtualNodes = null;
    private DeploymentTree deploymentTree;
    private Map<String, GCMDeploymentDescriptor> selectedDeploymentDesc;
    private ArrayList<String> currentDeploymentPath;

    public GCMApplicationDescriptorImpl(String filename)
        throws IllegalArgumentException, SAXException, IOException,
            XPathExpressionException {
        this(new File(filename));
    }

    public GCMApplicationDescriptorImpl(File file)
        throws IllegalArgumentException, SAXException, IOException,
            XPathExpressionException {
        currentDeploymentPath = new ArrayList<String>();

        gadFile = Helpers.checkDescriptorFileExist(file);
        try {
            // FIXME glaurent Handle XML errors ! When invalid content is encountered an exception not always thrown
            gadParser = new GCMApplicationParserImpl(gadFile);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        // 1. Load all GCM Deployment Descriptor
        Map<String, GCMDeploymentDescriptor> gdds;
        gdds = gadParser.getResourceProviders();

        // 2. Get Virtual Node and Command Builder
        virtualNodes = gadParser.getVirtualNodes();

        CommandBuilder commandBuilder = gadParser.getCommandBuilder();

        // 3. Select the GCM Deployment Descriptors to be used
        selectedDeploymentDesc = selectGCMD(virtualNodes, gdds);

        // 4. Build the runtime tree
        buildDeploymentTree();

        //		JMXNotificationManager.getInstance().subscribe(FactoryName.createRuntimeObjectName(.getURL()), this);

        // 5. Start the deployment
        for (GCMDeploymentDescriptor gdd : selectedDeploymentDesc.values()) {
            gdd.start(commandBuilder);
        }

        /**
         * If this GCMA describes a distributed application. The Runtime has
         * been started and will populate Virtual Nodes etc. We let the user
         * code, interact with its Middleware.
         *
         * if a "script" is described. The command has been started on each
         * machine/VM/core and we can safely return
         */
    }

    protected void buildDeploymentTree() {
        deploymentTree = new DeploymentTree();

        // make root node from local JVM
        DeploymentNode rootNode = new DeploymentNode();

        rootNode.setDeploymentDescriptorPath(""); // no deployment descriptor here

        try {
            rootNode.setApplicationDescriptorPath(gadFile.getCanonicalPath());
        } catch (IOException e) {
            rootNode.setApplicationDescriptorPath("");
        }

        currentDeploymentPath.clear();

        ProActiveRuntimeImpl proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        VMNodeList vmNodeList = new VMNodeList(proActiveRuntime.getVMInformation());
        currentDeploymentPath.add(proActiveRuntime.getVMInformation().getName());

        //                    vmNodes.addNode(<something>); - TODO cmathieu
        rootNode.addVMNodes(vmNodeList);
        rootNode.setDeploymentPath(getCurrentdDeploymentPath());

        deploymentTree.setRootNode(rootNode);

        // Build leaf nodes
        for (GCMDeploymentDescriptor gdd : selectedDeploymentDesc.values()) {
            GCMDeploymentDescriptorImpl gddi = (GCMDeploymentDescriptorImpl) gdd;
            GCMDeploymentResources resources = gddi.getResources();

            HostInfo hostInfo = resources.getHostInfo();
            if (hostInfo != null) {
                buildHostInfoTreeNode(rootNode, hostInfo);
            }

            for (Group group : resources.getGroups()) {
                buildGroupTreeNode(rootNode, group);
            }

            for (Bridge bridge : resources.getBridges()) {
                buildBridgeTree(rootNode, bridge);
            }
        }
    }

    /**
     * return a copy of the current deployment path
     * @return
     */
    private List<String> getCurrentdDeploymentPath() {
        return new ArrayList<String>(currentDeploymentPath);
    }

    private void buildHostInfoTreeNode(DeploymentNode rootNode,
        HostInfo hostInfo) {
        DeploymentNode deploymentNode = new DeploymentNode();
        deploymentNode.setDeploymentDescriptorPath(rootNode.getDeploymentDescriptorPath());
        pushDeploymentPath(hostInfo.getId());
        hostInfo.setDeploymentId(deploymentNode.getId());
        deploymentTree.addNode(deploymentNode, rootNode);
    }

    private void buildGroupTreeNode(DeploymentNode rootNode, Group group) {
        DeploymentNode deploymentNode = new DeploymentNode();
        deploymentNode.setDeploymentDescriptorPath(rootNode.getDeploymentDescriptorPath());
        HostInfo hostInfo = group.getHostInfo();
        pushDeploymentPath(hostInfo.getId());
        hostInfo.setDeploymentId(deploymentNode.getId());
        deploymentTree.addNode(deploymentNode, rootNode);
        popDeploymentPath();
    }

    private void buildBridgeTree(DeploymentNode baseNode, Bridge bridge) {
        DeploymentNode deploymentNode = new DeploymentNode();
        deploymentNode.setDeploymentDescriptorPath(baseNode.getDeploymentDescriptorPath());

        pushDeploymentPath(bridge.getId());

        // first look for a host info...
        //
        if (bridge.getHostInfo() != null) {
            HostInfo hostInfo = bridge.getHostInfo();
            pushDeploymentPath(hostInfo.getId());
            hostInfo.setDeploymentId(deploymentNode.getId());
            deploymentTree.addNode(deploymentNode, baseNode);
            popDeploymentPath();
        }

        // then groups...
        //
        if (bridge.getGroups() != null) {
            for (Group group : bridge.getGroups()) {
                buildGroupTreeNode(deploymentNode, group);
            }
        }

        // then bridges (and recurse)
        //
        if (bridge.getBridges() != null) {
            for (Bridge subBridge : bridge.getBridges()) {
                buildBridgeTree(deploymentNode, subBridge);
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

    /**
     * Select the GCM Deployment descriptor to be used
     *
     * A Virtual Node is a consumer, and a GCM Deployment Descriptor a producer.
     * We try to fulfill the consumers needs with as few as possible producer.
     *
     * @param vns
     *            Virtual Nodes asking for some resources
     * @param gdds
     *            GCM Deployment Descriptor providing some resources
     * @return A
     */
    static private Map<String, GCMDeploymentDescriptor> selectGCMD(
        Map<String, VirtualNodeInternal> vns,
        Map<String, GCMDeploymentDescriptor> gdds) {
        // TODO: Implement this method
        return gdds;
    }

    private long getRequiredCapacity() {
        int cap = 0;
        for (VirtualNodeInternal vn : virtualNodes.values()) {
            cap += vn.getRequiredCapacity();
        }

        return cap;
    }

    public VirtualNode getVirtualNode(String vnName)
        throws IllegalArgumentException {
        VirtualNode ret = virtualNodes.get(vnName);
        if (ret == null) {
            throw new IllegalArgumentException("Virtual Node " + vnName +
                " does not exist");
        }
        return ret;
    }

    public Map<String, ?extends VirtualNode> getVirtualNodes() {
        return virtualNodes;
    }

    public void kill() {
        // TODO Auto-generated method stub
    }

    @SuppressWarnings("unused")
    static public class TestGCMApplicationDescriptorImpl {
    }

    public boolean allProcessExited() {
        // TODO Auto-generated method stub
        return false;
    }

    public void awaitTermination() {
        try {
            Executor.getExecutor().awaitTermination();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
