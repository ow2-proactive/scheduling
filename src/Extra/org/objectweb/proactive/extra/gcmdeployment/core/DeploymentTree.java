/**
 *
 */
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extra.gcmdeployment.core.DeploymentNode;


public class DeploymentTree {
    protected DeploymentNode rootNode;
    protected Map<Long, DeploymentNode> nodeMap;

    public DeploymentTree() {
        rootNode = null;
        nodeMap = new HashMap<Long, DeploymentNode>();
    }

    public void addNode(DeploymentNode node, DeploymentNode parent) {
        parent.addChildren(node);
        nodeMap.put(node.getId(), node);
    }

    public void registerNode(DeploymentNode node) {
        // TODO
    }

    public DeploymentNode getNode(String id) {
        return nodeMap.get(id);
    }

    public DeploymentNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(DeploymentNode rootNode) {
        this.rootNode = rootNode;
    }
}
