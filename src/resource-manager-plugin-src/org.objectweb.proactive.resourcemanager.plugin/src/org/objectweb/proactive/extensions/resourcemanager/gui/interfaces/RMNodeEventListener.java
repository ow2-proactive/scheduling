package org.objectweb.proactive.extensions.resourcemanager.gui.interfaces;

import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extensions.resourcemanager.common.event.RMNodeSourceEvent;


/**
 * @author The ProActive Team
 */
public interface RMNodeEventListener {
    public void nodeAddedEvent(RMNodeEvent nodeEvent);

    public void nodeRemovedEvent(RMNodeEvent nodeEvent);

    public void nodeBusyEvent(RMNodeEvent nodeEvent);

    public void nodeDownEvent(RMNodeEvent nodeEvent);

    public void nodeFreeEvent(RMNodeEvent nodeEvent);

    public void nodeToReleaseEvent(RMNodeEvent nodeEvent);

    public void nodeSourceAddedEvent(RMNodeSourceEvent nodeSourceEvent);

    public void nodeSourceRemovedEvent(RMNodeSourceEvent nodeSourceEvent);
}
