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
package org.objectweb.proactive.core.body.message;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.event.AbstractEventProducer;
import org.objectweb.proactive.core.event.MessageEvent;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.event.ProActiveEvent;
import org.objectweb.proactive.core.event.ProActiveListener;


public class MessageEventProducerImpl extends AbstractEventProducer
    implements MessageEventProducer, java.io.Serializable {
    public MessageEventProducerImpl() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void notifyListeners(Message message, int type, UniqueID bodyID,
        int requestQueueLength) {
        if (hasListeners()) {
            notifyAllListeners(new MessageEvent(message, type, bodyID,
                    requestQueueLength));
        }
    }

    public void notifyListeners(Message message, int type, UniqueID bodyID) {
        if (hasListeners()) {
            notifyAllListeners(new MessageEvent(message, type, bodyID, -1));
        }
    }

    //
    // -- implements MessageEventProducer -----------------------------------------------
    //
    public void addMessageEventListener(MessageEventListener listener) {
        addListener(listener);
    }

    public void removeMessageEventListener(MessageEventListener listener) {
        removeListener(listener);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected void notifyOneListener(ProActiveListener listener,
        ProActiveEvent event) {
        MessageEvent messageEvent = (MessageEvent) event;
        MessageEventListener messageEventListener = (MessageEventListener) listener;
        switch (messageEvent.getType()) {
        case MessageEvent.REQUEST_SENT:
            // WARNING: we don't generate an event in the following case:
            //   - The listener is an active object  AND
            //   - The destination of the request is the listener
            // This is done to avoid recursive generation of events
            if (listener instanceof org.objectweb.proactive.core.mop.StubObject) {
                org.objectweb.proactive.core.mop.StubObject so = (org.objectweb.proactive.core.mop.StubObject) listener;
                UniqueID id = ((org.objectweb.proactive.core.body.proxy.BodyProxy) so.getProxy()).getBodyID();
                if (id.equals(messageEvent.getDestinationBodyID())) {
                    break;
                }
            }
            messageEventListener.requestSent(messageEvent);
            break;
        case MessageEvent.REQUEST_RECEIVED:
            messageEventListener.requestReceived(messageEvent);
            break;
        case MessageEvent.REPLY_SENT:
            messageEventListener.replySent(messageEvent);
            break;
        case MessageEvent.REPLY_RECEIVED:
            messageEventListener.replyReceived(messageEvent);
            break;
        case MessageEvent.VOID_REQUEST_SERVED:
            messageEventListener.voidRequestServed(messageEvent);
            break;
        case MessageEvent.SERVING_STARTED:
            messageEventListener.servingStarted(messageEvent);
            break;
        }
    }
} // end inner class MessageEventProducer
