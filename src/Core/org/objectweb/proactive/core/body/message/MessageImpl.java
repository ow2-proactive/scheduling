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
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;


/**
 * <p>
 * Implements a simple message encapsulating a method call between two
 * active objects.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class MessageImpl implements Message, java.io.Serializable {

    /** The name of the method called */
    protected String methodName;

    /** The UniqueID of the body sending the message */
    protected UniqueID sourceID;

    /** The unique sequence number for the message */
    protected long sequenceNumber;

    /** the time the message has been issued or deserialized */
    protected transient long timeStamp;
    protected boolean isOneWay;

    // FAULT TOLERANCE

    /** all values piggybacked for fault tolerance stuff */
    protected MessageInfo messageInfos;

    /** true if this message can be ignored */
    protected boolean ignoreIt;

    /** ftmanager linked to this message */
    protected transient FTManager ftm;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new Message based on the given information.
     * @param sourceID the id of the sender of this message
     * @param sequenceNumber the unique sequence number of this message
     * @param isOneWay <code>true</code> if oneWay
     * @param methodName the method name of the method call
     */
    public MessageImpl(UniqueID sourceID, long sequenceNumber, boolean isOneWay, String methodName) {
        this.sourceID = sourceID;
        this.sequenceNumber = sequenceNumber;
        this.timeStamp = System.currentTimeMillis();
        this.isOneWay = isOneWay;
        this.methodName = methodName;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("method=").append(methodName).append(", ");
        sb.append("sender=").append(sourceID).append(", ");
        sb.append("sequenceNumber=").append(sequenceNumber).append("\n");
        return sb.toString();
    }

    //
    // -- implements Message -----------------------------------------------
    //
    public UniqueID getSourceBodyID() {
        return this.sourceID;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public boolean isOneWay() {
        return isOneWay;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.timeStamp = System.currentTimeMillis();
    }

    // FAULT-TOLERANCE
    public MessageInfo getMessageInfo() {
        return this.messageInfos;
    }

    public void setMessageInfo(MessageInfo mi) {
        this.messageInfos = mi;
    }

    public boolean ignoreIt() {
        return this.ignoreIt;
    }

    public void setIgnoreIt(boolean ignore) {
        this.ignoreIt = ignore;
    }

    public void setFTManager(FTManager ft) {
        this.ftm = ft;
    }

    public FTManager getFTManager() {
        return this.ftm;
    }
}
