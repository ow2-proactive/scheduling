package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.HashMap;
import java.util.Map;


public class TopologyRootImpl extends TopologyImpl {
    protected Map<Long, TopologyImpl> nodeMap;

    public TopologyRootImpl() {
        nodeMap = new HashMap<Long, TopologyImpl>();
    }

    public boolean isRoot() {
        return nodeMap != null;
    }

    public void addNode(TopologyImpl node, TopologyImpl parent) {
        parent.addChildren(node);
        nodeMap.put(node.getId(), node);
    }

    public TopologyImpl getNode(Long topologyId) {
        return nodeMap.get(topologyId);
    }
}
