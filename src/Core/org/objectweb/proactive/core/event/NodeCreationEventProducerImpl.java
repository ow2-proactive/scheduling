/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.node.Node;


public class NodeCreationEventProducerImpl extends AbstractEventProducer
    implements NodeCreationEventProducer {
    public NodeCreationEventProducerImpl() {
        super(false, false);
    }

    //
    //-------------------inherited methods from AbstractEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.AbstractEventProducer#notifyOneListener(ProActiveListener, ProActiveEvent)
     */
    @Override
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
    protected void notifyListeners(VirtualNodeInternal vn, int type, Node node,
        int nodeCreated) {
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
