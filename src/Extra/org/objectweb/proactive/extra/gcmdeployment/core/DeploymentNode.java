/**
 *
 */
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.util.ProActiveCounter;


public class DeploymentNode {
    protected long id;
    protected String applicationDescriptorPath;
    protected String deploymentDescriptorPath;
    protected List<String> deploymentPath;
    protected Set<VMNodes> nodeMap;
    protected List<DeploymentNode> children;

    public DeploymentNode() {
        nodeMap = new HashSet<VMNodes>();
        children = new ArrayList<DeploymentNode>();
        id = ProActiveCounter.getUniqID();
    }

    public long getId() {
        return id;
    }

    public List<String> getDeploymentPath() {
        return deploymentPath;
    }

    public void setDeploymentPath(List<String> deploymentPath) {
        this.deploymentPath = deploymentPath;
    }

    public Set<VMNodes> getNodeMap() {
        return nodeMap;
    }

    public void addVMNodes(VMNodes vmNode) {
        nodeMap.add(vmNode);
    }

    public List<DeploymentNode> getChildren() {
        return children;
    }

    public void addChildren(DeploymentNode node) {
        children.add(node);
    }

    public String getApplicationDescriptorPath() {
        return applicationDescriptorPath;
    }

    public void setApplicationDescriptorPath(String applicationDescriptorPath) {
        this.applicationDescriptorPath = applicationDescriptorPath;
    }

    public String getDeploymentDescriptorPath() {
        return deploymentDescriptorPath;
    }

    public void setDeploymentDescriptorPath(String deploymentDescriptorPath) {
        this.deploymentDescriptorPath = deploymentDescriptorPath;
    }
}
