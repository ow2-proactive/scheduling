/*
 * Created on 27 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.event;


/**
 * <p>
 * A class implementating this interface is listener of <code>NodeCreationEvent</code>.
 * </p>
 *
 * @version 1.0,  2004/07/06
 * @since   ProActive 2.0.1
 *
 */
public interface NodeCreationEventListener extends ProActiveListener {

    /**
     * Signals that a node creation occured on the virtualNode encapsulated in the event
     * @param event that details the creation of the node
     */
    public void nodeCreated(NodeCreationEvent event);
}
