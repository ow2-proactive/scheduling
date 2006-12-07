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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.event.MessageEvent;


public class SpyMessageEvent extends SpyEvent implements java.io.Serializable {

    /** The name of the method called */
    protected String methodName;

    /** The UniqueID of the body sending the call */
    protected UniqueID sourceID;

    /** The UniqueID of the body receiving the call */
    protected UniqueID destinationID;

    /** The unique sequence number for the call */
    protected long sequenceNumber;

    /** The request queue length of the body that sent the event or -1 */
    protected int requestQueueLength;

    public SpyMessageEvent(int eventType, MessageEvent message) {
        super(eventType,
            (message.wasSent()) ? message.getSourceBodyID()
                                : message.getDestinationBodyID());
        this.methodName = message.getMethodName();
        this.sourceID = message.getSourceBodyID();
        this.destinationID = message.getDestinationBodyID();
        this.sequenceNumber = message.getSequenceNumber();
        this.requestQueueLength = message.getRequestQueueLength();
    }

    public String toString() {
        return super.toString() + " methodName=" + methodName +
        " requestQueue=" + requestQueueLength;
    }

    /**
     * Return the id of the sender of the request
     */
    public UniqueID getSourceBodyID() {
        return sourceID;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public UniqueID getDestinationBodyID() {
        return destinationID;
    }

    public int getRequestQueueLength() {
        return requestQueueLength;
    }

    /** Returns whether the body that created the event sent the message included in the event
     *  or received it. */
    public boolean wasSent() {
        return (type == REPLY_SENT_MESSAGE_TYPE) ||
        (type == REQUEST_SENT_MESSAGE_TYPE);
    }

    /** Returns true if the event was created when a request was sent or received. */
    public boolean isRequestMessage() {
        return (type == REQUEST_SENT_MESSAGE_TYPE) ||
        (type == REQUEST_RECEIVED_MESSAGE_TYPE);
    }

    /** Returns true if the event was created when a reply was sent or received or a void request was finished. */
    public boolean isReplyMessage() {
        return (type == REPLY_SENT_MESSAGE_TYPE) ||
        (type == REPLY_RECEIVED_MESSAGE_TYPE) ||
        (type == VOID_REQUEST_SERVED_TYPE);
    }

    /** Returns whether matching event belongs to the same Qsend Qrecv Rsend Rrecv set */
    public boolean matches(SpyMessageEvent matchingEvent) {
        if (matchingEvent.sequenceNumber != sequenceNumber) {
            return false;
        }
        if (!matchingEvent.methodName.equals(methodName)) {
            return false;
        }
        if ((matchingEvent.sourceID.equals(sourceID) &&
                matchingEvent.destinationID.equals(destinationID)) ||
                (matchingEvent.sourceID.equals(destinationID) &&
                matchingEvent.destinationID.equals(sourceID))) {
            return true;
        }
        return false;
    }
}
