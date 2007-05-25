/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.monitoring.spy;

import java.io.IOException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

public class SpyListenerImpl implements SpyListener{
    protected SpyEventListener spyEventListener;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public SpyListenerImpl() {
    }

    public SpyListenerImpl(SpyEventListener spyEventListener) {
        this.spyEventListener = spyEventListener;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    
    /**
     * Terminate the body of the "Spylistener". After this call the body is no more alive and no more active
     * although the active thread is not interrupted. The body is unuseable after this call.
     */
    public void terminate() {
        Body body = ProActive.getBodyOnThis();
        if (body != null) {
            body.terminate();
        }
    }

	public void enableMonitoring(boolean enable){
		spyEventListener.enableMonitoring(enable);
	}
    
    //
    // -- implements SpyListener -----------------------------------------------
    //

    /**
     * Receives a message from a spy
     */
    public void observationPerformed(SpyEvent spyEvent) {
        fireSpyEventInternal(spyEvent);
        spyEventListener.allEventsProcessed();
    }

    /**
     * Receives multiples messages in a buffer
     */
    public void observationsPerformed(SpyEvent[] spyEvents) {
        if (spyEvents.length > 0) {
            for (int i = 0; i < spyEvents.length; i++) {
                fireSpyEventInternal(spyEvents[i]);
            }
        }
        if (spyEvents.length > 0) {
            spyEventListener.allEventsProcessed();
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void fireSpyEventInternal(SpyEvent spyEvent) {
        switch (spyEvent.getType()) {
        case SpyEvent.BODY_CREATION_EVENT_TYPE:
            BodyCreationSpyEvent event = (BodyCreationSpyEvent) spyEvent;
            spyEventListener.activeObjectAdded(spyEvent.getBodyID(),
            		event.getJobID(),event.getNodeURL(),event.getClassName(),event.isActive());
            break;
        case SpyEvent.BODY_EVENT_TYPE:
            BodySpyEvent bse = (BodySpyEvent) spyEvent;            
            spyEventListener.activeObjectChanged(spyEvent.getBodyID(),
                bse.isActive(), bse.isAlive());
            break;
        case SpyEvent.OBJECT_WAIT_BY_NECESSITY_TYPE:
            spyEventListener.objectWaitingByNecessity(spyEvent.getBodyID(),
                spyEvent);
            break;
        case SpyEvent.OBJECT_RECEIVED_FUTURE_RESULT_TYPE:
            spyEventListener.objectReceivedFutureResult(spyEvent.getBodyID(),
                spyEvent);
            break;
        case SpyEvent.OBJECT_WAIT_FOR_REQUEST_TYPE:
            spyEventListener.objectWaitingForRequest(spyEvent.getBodyID(),
                spyEvent);
            break;
        case SpyEvent.REQUEST_SENT_MESSAGE_TYPE:
            spyEventListener.requestMessageSent(spyEvent.getBodyID(), spyEvent);
            break;
        case SpyEvent.REPLY_SENT_MESSAGE_TYPE:
            spyEventListener.replyMessageSent(spyEvent.getBodyID(), spyEvent);
            break;
        case SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE:
            spyEventListener.requestMessageReceived(spyEvent.getBodyID(),
                spyEvent);
            break;
        case SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE:
            spyEventListener.replyMessageReceived(spyEvent.getBodyID(), spyEvent);
            break;
        case SpyEvent.VOID_REQUEST_SERVED_TYPE:
            spyEventListener.voidRequestServed(spyEvent.getBodyID(), spyEvent);
            break;
        case SpyEvent.SERVING_STARTED_TYPE:
            spyEventListener.servingStarted(spyEvent.getBodyID(), spyEvent);
            break;
        }
    }
}
