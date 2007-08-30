/**
 *
 */
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RuntimeNode {
    protected String id;
    protected String applicationDescriptorPath;
    protected String deploymentDescriptorPath;
    protected List<String> deploymentPath;
    protected Set<VMNodes> nodeMap;
    protected List<RuntimeNode> children;

    public RuntimeNode() {
        nodeMap = new HashSet<VMNodes>();
        children = new ArrayList<RuntimeNode>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setNodeMap(Set<VMNodes> nodeMap) {
        this.nodeMap = nodeMap;
    }

    public List<RuntimeNode> getChildren() {
        return children;
    }

    public void addChildren(RuntimeNode node) {
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
