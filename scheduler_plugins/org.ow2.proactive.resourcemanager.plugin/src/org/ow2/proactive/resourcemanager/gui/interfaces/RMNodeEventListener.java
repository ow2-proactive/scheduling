package org.ow2.proactive.resourcemanager.gui.interfaces;

import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;


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
