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
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeCreationEventProducerImpl extends AbstractEventProducer
    implements NodeCreationEventProducer {
    
    public NodeCreationEventProducerImpl(){
        super(false,false);
    }
    //
    //-------------------inherited methods from AbstractEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.AbstractEventProducer#notifyOneListener(ProActiveListener, ProActiveEvent)
     */
    protected void notifyOneListener(ProActiveListener proActiveListener,
        ProActiveEvent event) {
        NodeCreationEvent creationEvent = (NodeCreationEvent) event;
        NodeCreationEventListener creationEventListener = (NodeCreationEventListener) proActiveListener;

        //notify the listener that a creation occurs
        creationEventListener.nodeCreated(creationEvent);
    }

    //
    //-------------------PROTECTED METHODS------------------
    //
    protected void notifyListeners(VirtualNode vn, int type, Node node, int nodeCreated) {
        if (hasListeners()) {
            notifyAllListeners(new NodeCreationEvent(vn, type, node, nodeCreated));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("no listener");
            }
        }
    }

    //
    //-------------------implements NodeCreationEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.NodeCreationEventProducer#addNodeCreationEventListener(org.objectweb.proactive.core.event.NodeCreationEventListener)
     */
    public void addNodeCreationEventListener(NodeCreationEventListener listener) {
        addListener(listener);
    }

    /**
     * @see org.objectweb.proactive.core.event.NodeCreationEventProducer#removeNodeCreationEventListener(org.objectweb.proactive.core.event.NodeCreationEventListener)
     */
    public void removeNodeCreationEventListener(
        NodeCreationEventListener listener) {
    }
}
