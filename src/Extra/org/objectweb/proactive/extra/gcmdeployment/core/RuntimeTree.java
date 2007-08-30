/**
 *
 */
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extra.gcmdeployment.core.RuntimeNode;


public class RuntimeTree {
    protected RuntimeNode rootNode;
    protected Map<String, RuntimeNode> nodeMap;

    public RuntimeTree() {
        rootNode = null;
        nodeMap = new HashMap<String, RuntimeNode>();
    }

    public void addNode(RuntimeNode node, RuntimeNode parent) {
        parent.addChildren(node);
        nodeMap.put(node.getId(), node);
    }

    public RuntimeNode getNode(String id) {
        return nodeMap.get(id);
    }

    public RuntimeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(RuntimeNode rootNode) {
        this.rootNode = rootNode;
    }
}
