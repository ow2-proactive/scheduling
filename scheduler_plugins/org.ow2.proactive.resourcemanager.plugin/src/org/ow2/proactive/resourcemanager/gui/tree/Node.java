package org.ow2.proactive.resourcemanager.gui.tree;

import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * @author The ProActive Team
 */
public class Node extends TreeLeafElement {
    private NodeState state = null;

    public Node(String name, NodeState state) {
        super(name, TreeElementType.NODE);
        this.state = state;
    }

    /**
     * To get the state
     *
     * @return the state
     */
    public NodeState getState() {
        return state;
    }

    /**
     * To set the state
     *
     * @param state the state to set
     */
    public void setState(NodeState state) {
        this.state = state;
    }
}
