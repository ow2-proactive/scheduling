/*
 * Created on 27 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


/**
 * <p>
 * Event sent when a Node is created on a VirtualNode.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2004/07/06
 * @since   ProActive 2.0.1
 *
 */
public class NodeCreationEvent extends ProActiveEvent {
    public static final int NODE_CREATED = 10;
    protected Node node;
    protected VirtualNode vn;
    protected int nodeCreated;

    /**
     * Creates a new <code>NodeCreationEvent</code>
     * @param vn the virtualnode on which the creation occurs
     * @param messageType the type of the event
     * @param node the newly created node
     * @param nodeCreated the number of nodes already created
     */
    public NodeCreationEvent(VirtualNode vn, int messageType, Node node,
        int nodeCreated) {
        super(vn, messageType);
        this.node = node;
        this.vn = vn;
        this.nodeCreated = nodeCreated;
    }

    /**
     * @return Returns the node.
     */
    public Node getNode() {
        return node;
    }

    /**
     * @return Returns the vn.
     */
    public VirtualNode getVirtualNode() {
        return vn;
    }

    /**
     * @return Returns the number of nodes already created.
     */
    public int getNodeCreated() {
        return nodeCreated;
    }
}
