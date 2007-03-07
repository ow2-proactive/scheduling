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
package org.objectweb.proactive.ic2d.spy;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.event.BodyEvent;
import org.objectweb.proactive.core.event.BodyEventListener;
import org.objectweb.proactive.core.event.FutureEvent;
import org.objectweb.proactive.core.event.FutureEventListener;
import org.objectweb.proactive.core.event.MessageEvent;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.event.RequestQueueEvent;
import org.objectweb.proactive.core.event.RequestQueueEventListener;


/**
 * Helper class for the Spy that is listener of all objects.
 */
public class SpyEventManager {
    protected static final int MASTER_SPY_CHECK_INTERVAL = 100000; // 100 seconds
    protected MessageEventListener messageEventListener;
    protected RequestQueueEventListener requestQueueEventListener;
    protected BodyEventListener bodyEventListener;
    protected FutureEventListener futureEventListener;

    /**
     * Log of all the RequestSent messages received
     * to perform a search when receiving a ReplyReceive
     */
    protected java.util.LinkedList<MessageEvent> requestSentEventsList;
    protected java.util.LinkedList<MessageEvent> replyReceivedEventsList;

    /**
     * Vector of pending messages
     */
    protected java.util.List<SpyEvent> pendingSpyEvents;
    protected UniqueID masterSpyID;

    /**
     * Every so often this Manager check if its master spy is still running ok
     */
    protected long lastTimeMasterSpyCheck;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public SpyEventManager(UniqueID masterSpyID) {
        this.masterSpyID = masterSpyID;
        messageEventListener = new MyMessageEventListener();
        requestQueueEventListener = new MyRequestQueueEventListener();
        bodyEventListener = new MyBodyEventListener();
        futureEventListener = new MyFutureEventListener();

        // Initialises the list of pending messages
        pendingSpyEvents = new java.util.ArrayList<SpyEvent>();
        // Initialize the list of RequestSent
        requestSentEventsList = new java.util.LinkedList<MessageEvent>();
        // Initialize the list of ReplyReceived
        replyReceivedEventsList = new java.util.LinkedList<MessageEvent>();

        lastTimeMasterSpyCheck = System.currentTimeMillis();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public SpyEvent[] collectPendingSpyEvents() {
        synchronized (pendingSpyEvents) {
            SpyEvent[] events = new SpyEvent[pendingSpyEvents.size()];
            if (pendingSpyEvents.size() > 0) {
                pendingSpyEvents.toArray(events);
                pendingSpyEvents.clear();
            }
            return events;
        }
    }

    //
    // -- ADD / REMOVE LISTENERS -----------------------------------------------
    //
    public void addMessageEventListener(Body body) {
        body.addMessageEventListener(messageEventListener);
    }

    public void removeMessageEventListener(Body body) {
        body.removeMessageEventListener(messageEventListener);
    }

    //
    // -- FRIENDLY METHODS -----------------------------------------------
    //
    void addBodyEventListener() {
        LocalBodyStore.getInstance().addBodyEventListener(bodyEventListener);
    }

    void removeBodyEventListener() {
        LocalBodyStore.getInstance().removeBodyEventListener(bodyEventListener);
    }

    void addFutureEventListener() {
        FutureProxy.getFutureEventProducer()
                   .addFutureEventListener(futureEventListener);
    }

    void removeFutureEventListener() {
        FutureProxy.getFutureEventProducer()
                   .addFutureEventListener(futureEventListener);
    }

    SpyEvent[] createSpyEventForExistingBodies(Body spyBody) {
        BodyMap knownBodies = LocalBodyStore.getInstance().getLocalBodies();

        // remove our body
        knownBodies.removeBody(masterSpyID);
        SpyEvent[] spyEvents = new SpyEvent[knownBodies.size()]; // messages to send bufferized
        int i = 0;
        java.util.Iterator bodiesIterator = knownBodies.bodiesIterator();
        while (bodiesIterator.hasNext()) {
            Body activeObjectBody = (Body) bodiesIterator.next();
            if (activeObjectBody.isActive()) {
                addListenersToNewBody(activeObjectBody);
                spyEvents[i] = new BodyCreationSpyEvent(activeObjectBody.getID(),
                        activeObjectBody.getNodeURL(),
                        activeObjectBody.getName(), activeObjectBody.isActive());
                i++;
            }
        }
        if (i < knownBodies.size()) {
            SpyEvent[] newSpyEvents = new SpyEvent[i];
            if (i > 0) {
                System.arraycopy(spyEvents, 0, newSpyEvents, 0, i);
            }
            return newSpyEvents;
        } else {
            return spyEvents;
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void addListenersToNewBody(Body body) {
        if (body.getRequestQueue() != null) {
            body.getRequestQueue()
                .addRequestQueueEventListener(requestQueueEventListener);
        }

        // we don't add it by default but wait until the order is sent   
        //addMessageEventListener(body);  
    }

    private void removeListenersFromDeadBody(Body body) {
        if (body.getRequestQueue() != null) {
            body.getRequestQueue()
                .removeRequestQueueEventListener(requestQueueEventListener);
        }
        removeMessageEventListener(body);
    }

    // call when a request is sent
    private SpyMessageEvent checkReplyReceivedEvent(
        MessageEvent requestSentEvent) {
        long sequence = requestSentEvent.getSequenceNumber();
        UniqueID requestSenderID = requestSentEvent.getSourceBodyID();
        UniqueID requestReceiverID = requestSentEvent.getDestinationBodyID();
        synchronized (replyReceivedEventsList) {
            java.util.ListIterator<MessageEvent> l = replyReceivedEventsList.listIterator();
            while (l.hasNext()) {
                MessageEvent replyReceivedEvent = l.next();
                if (sequence == replyReceivedEvent.getSequenceNumber()) {
                }
                if ((sequence == replyReceivedEvent.getSequenceNumber()) &&
                        (requestSenderID.equals(
                            replyReceivedEvent.getDestinationBodyID())) &&
                        (requestReceiverID.equals(
                            replyReceivedEvent.getSourceBodyID()))) {
                    l.remove();
                    return new SpyMessageEvent(SpyMessageEvent.REPLY_RECEIVED_MESSAGE_TYPE,
                        replyReceivedEvent);
                }
            }
            requestSentEventsList.add(requestSentEvent);
            return null;
        }
    }

    // call when a reply is received
    private boolean checkRequestSentEvent(MessageEvent replyReceivedEvent) {
        long sequence = replyReceivedEvent.getSequenceNumber();
        UniqueID replySenderID = replyReceivedEvent.getSourceBodyID();
        UniqueID replyReceiverID = replyReceivedEvent.getDestinationBodyID();
        synchronized (replyReceivedEventsList) {
            java.util.ListIterator<MessageEvent> l = requestSentEventsList.listIterator();
            while (l.hasNext()) {
                MessageEvent requestSentEvent = l.next();
                if (sequence == requestSentEvent.getSequenceNumber()) {
                }
                if ((sequence == requestSentEvent.getSequenceNumber()) &&
                        (replySenderID.equals(
                            requestSentEvent.getDestinationBodyID())) &&
                        (replyReceiverID.equals(
                            requestSentEvent.getSourceBodyID()))) {
                    l.remove();
                    return true;
                }
            }
            replyReceivedEventsList.add(replyReceivedEvent);
            return false;
        }
    }

    private void addEvent(SpyEvent event) {
        //System.out.println("add event evt="+event+"   size="+pendingSpyEvents.size());
        synchronized (pendingSpyEvents) {
            pendingSpyEvents.add(event);
        }
        checkMasterSpy();
    }

    private void checkMasterSpy() {
        if ((lastTimeMasterSpyCheck + MASTER_SPY_CHECK_INTERVAL) < System.currentTimeMillis()) {
            return;
        }
        lastTimeMasterSpyCheck = System.currentTimeMillis();
        if (LocalBodyStore.getInstance().getLocalBody(masterSpyID) != null) {
            return;
        }

        // the master Spy is gone : we have no more reason to exist.
        // we deregister from everybody
        removeBodyEventListener();
        synchronized (pendingSpyEvents) {
            java.util.ListIterator<SpyEvent> l = pendingSpyEvents.listIterator();
            while (l.hasNext()) {
                SpyEvent event = l.next();
                UniqueID bodyID = event.getBodyID();
                Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);
                if (body != null) {
                    removeListenersFromDeadBody(body);
                }
            }
            pendingSpyEvents.clear();
        }
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * BodyEventListener
     */
    private class MyBodyEventListener implements BodyEventListener {
        //
        // -- implements BodyEventListener -----------------------------------------------
        //
        public void bodyCreated(BodyEvent event) {
            Body body = checkBody(event.getBody());
            if (body == null) {
                return;
            }

            //System.out.println("bodyCreated name="+body.getNodeURL());
            addEvent(new BodyCreationSpyEvent(body.getID(), body.getNodeURL(),
                    body.getName(), body.isActive()));
            addListenersToNewBody(body);
        }

        public void bodyDestroyed(BodyEvent event) {
            Body body = checkBody(event.getBody());
            if (body == null) {
                return;
            }

            //System.out.println("bodyDestroyed name="+body.getNodeURL());
            addEvent(new BodySpyEvent(body.getID(), false, false));
            removeListenersFromDeadBody(body);
        }

        public void bodyChanged(BodyEvent event) {
            Body body = checkBody(event.getBody());
            if (body == null) {
                return;
            }

            //System.out.println("bodyChanged name="+body.getNodeURL()+" isActive="+body.isActive());
            addEvent(new BodySpyEvent(body.getID(), body.isActive(),
                    body.isAlive()));
            removeListenersFromDeadBody(body);
        }

        private Body checkBody(UniversalBody uBody) {
            if (!(uBody instanceof Body)) {
                return null;
            }
            Body body = (Body) uBody;
            UniqueID bodyID = body.getID();
            if (masterSpyID.equals(bodyID)) {
                return null;
            }
            return body;
        }
    } // end inner class MyBodyEventListener

    /**
     * MyRequestQueueEventListener
     */
    private class MyRequestQueueEventListener
        implements RequestQueueEventListener {
        //
        // -- implements RequestQueueEventListener -----------------------------------------------
        //
        public void requestQueueModified(RequestQueueEvent event) {
            if (event.getType() == RequestQueueEvent.WAIT_FOR_REQUEST) {
                addEvent(new SpyEvent(SpyEvent.OBJECT_WAIT_FOR_REQUEST_TYPE,
                        event.getOwnerID()));
            }
        }
    } // end inner class MyRequestQueueEventListener

    /**
     * MyFutureEventListener
     */
    private class MyFutureEventListener implements FutureEventListener {
        //
        // -- implements FutureEventListener -----------------------------------------------
        //
        public void waitingForFuture(FutureEvent event) {
            addEvent(new SpyFutureEvent(
                    SpyEvent.OBJECT_WAIT_BY_NECESSITY_TYPE, event));
        }

        public void receivedFutureResult(FutureEvent event) {
            addEvent(new SpyFutureEvent(
                    SpyEvent.OBJECT_RECEIVED_FUTURE_RESULT_TYPE, event));
        }
    } // end inner class MyRequestQueueEventListener

    /**
     * RequestReceiverListener
     */
    private class MyMessageEventListener implements MessageEventListener {
        //
        // -- implements MessageEventListener -----------------------------------------------
        //
        public void requestSent(MessageEvent event) {
            addEvent(new SpyMessageEvent(SpyEvent.REQUEST_SENT_MESSAGE_TYPE,
                    event));
            //Synchro purpose
            if (!event.isOneWay()) {
                // check is the reply event has already been received for this request
                SpyMessageEvent replyEvent = checkReplyReceivedEvent(event);
                if (replyEvent != null) {
                    addEvent(replyEvent);
                }
            }
        }

        public void requestReceived(MessageEvent event) {
            addEvent(new SpyMessageEvent(
                    SpyEvent.REQUEST_RECEIVED_MESSAGE_TYPE, event));
        }

        public void replySent(MessageEvent event) {
            addEvent(new SpyMessageEvent(SpyEvent.REPLY_SENT_MESSAGE_TYPE, event));
        }

        public void replyReceived(MessageEvent event) {
            if (checkRequestSentEvent(event)) {
                addEvent(new SpyMessageEvent(
                        SpyEvent.REPLY_RECEIVED_MESSAGE_TYPE, event));
            }
        }

        public void voidRequestServed(MessageEvent event) {
            addEvent(new SpyMessageEvent(SpyEvent.VOID_REQUEST_SERVED_TYPE,
                    event));
        }

        public void servingStarted(MessageEvent event) {
            addEvent(new SpyMessageEvent(SpyEvent.SERVING_STARTED_TYPE, event));
        }
    } // end inner class MyMessageEventListener
}
