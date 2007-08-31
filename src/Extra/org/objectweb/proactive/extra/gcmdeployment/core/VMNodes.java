/**
 *
 */
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.VMInformation;


public class VMNodes {
    protected VMInformation vmInfo;
    protected List<Node> nodes;

    public VMNodes(VMInformation vmInfo) {
        super();
        this.vmInfo = vmInfo;
        this.nodes = new ArrayList<Node>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }
}
