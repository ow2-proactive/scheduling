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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.message.Message;


/**
 * <p>
 * A <code>MessageEvent</code> occurs when a <code>Message</code> get sent or received or
 * when the treatment of a request begins or ends.
 * </p>
 *
 * @see org.objectweb.proactive.core.body.message.Message
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class MessageEvent extends ProActiveEvent implements Message, java.io.Serializable {

    /** constant indicating the encapsulated message has been sent */
    public static final int REQUEST_SENT = 10;

    /** constant indicating the encapsulated message has been received */
    public static final int REQUEST_RECEIVED = 20;

    /** constant indicating the encapsulated message has been sent */
    public static final int REPLY_SENT = 30;

    /** constant indicating the encapsulated message has been received */
    public static final int REPLY_RECEIVED = 40;

    /** constant indicating the encapsulated request without reply has been served */
    public static final int VOID_REQUEST_SERVED = 50;

    /** constant indicating that the serving of the encapsulated request has started */
    public static final int SERVING_STARTED = 60;
    protected UniqueID destinationID;

    /** Length of the request queue of the body that sends this event or -1
     * if this message did not affect the request queue length. */
    protected int requestQueueLength;

    /**
     * Creates a new <code>MessageEvent</code> based on the message
     * <code>message</code> and on the given action
     * @param message the message on which this event is based.
     * @param messageActionType the type of the action occuring with
     * this message either REQUEST_SENT/RECEIVED, REPLY_SENT/RECEIVED,
     * VOID_REQUEST_SERVED or SERVING_STARTED.
     * @param requestQueueLength the length of the request queue of the
     * body that sends this event or -1 if this event did not affect the queue length.
     */
    public MessageEvent(Message message, int messageActionType, UniqueID destinationID, int requestQueueLength) {
        super(message, messageActionType);
        this.destinationID = destinationID;
        this.requestQueueLength = requestQueueLength;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Returns the id of the body receiver of the encapsulated message.
     * For a VOID_REQUEST_SERVED message, this is the destination body of the
     * encapsulated request, i.e. the body that sends this event!
     * @return the id of the body receiver of the encapsulated message
     */
    public UniqueID getDestinationBodyID() {
        return destinationID;
    }

    /**
     * Returns the length of the request queue of the sending body or -1.
     * @return the length of the request queue of the sending body or -1.
     */
    public int getRequestQueueLength() {
        return requestQueueLength;
    }

    /**
     * Returns a string representation of this event
     * @return a string representation of this event
     */
    @Override
    public String toString() {
        return "methodName=" + getMethodName() + " sourceID=" + getSourceBodyID() + " destinationID=" +
            getDestinationBodyID() + " sequenceNumber=" + getSequenceNumber();
    }

    public boolean wasSent() {
        return (type == REQUEST_SENT) || (type == REPLY_SENT);
    }

    //
    // -- implements Message -----------------------------------------------
    //
    public UniqueID getSourceBodyID() {
        return getMessage().getSourceBodyID();
    }

    public String getMethodName() {
        return getMessage().getMethodName();
    }

    public long getSequenceNumber() {
        return getMessage().getSequenceNumber();
    }

    public boolean isOneWay() {
        return getMessage().isOneWay();
    }

    public MessageInfo getMessageInfo() {
        return getMessage().getMessageInfo();
    }

    public void setMessageInfo(MessageInfo mi) {
        getMessage().setMessageInfo(mi);
    }

    public void setIgnoreIt(boolean ignore) {
        getMessage().setIgnoreIt(ignore);
    }

    public boolean ignoreIt() {
        return getMessage().ignoreIt();
    }

    public void setFTManager(FTManager ft) {
        getMessage().setFTManager(ft);
    }

    public FTManager getFTManager() {
        return getMessage().getFTManager();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private final Message getMessage() {
        return (Message) getSource();
    }
}
