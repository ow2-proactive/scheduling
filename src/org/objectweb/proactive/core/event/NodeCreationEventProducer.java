/*
 * Created on 27 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.event;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface NodeCreationEventProducer {

    /**
     * Adds a listener of NodeCreationEvent. The listener will receive event when
     * a node is created on a VirtualNode.
     * @param listener the listener to add
     */
    public void addNodeCreationEventListener(NodeCreationEventListener listener);

    /**
     * Removes the NodeCreationEventListener.
     * @param listener the listener to remove
     */
    public void removeNodeCreationEventListener(
        NodeCreationEventListener listener);
}
