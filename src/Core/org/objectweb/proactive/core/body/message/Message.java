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
 * A class implementing this interface is an object encapsulating a reified method call
 * either the sending of the call with the arguments or the reply of the call with
 * the result.
 * </p><p>
 * A <code>Message</code> clearly identifies a sender and a receiver of the message. Each message
 * is associated with a unique sequence number.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface Message {

    /**
     * Returns the id of the body source of this message
     * @return the id of the body source of this message
     */
    public UniqueID getSourceBodyID();

    /**
     * Returns the method name of the method call packed in this message
     * @return the method name of the method call packed in this message
     */
    public String getMethodName();

    /**
     * Returns a unique sequence number of this message
     * @return a unique sequence number of this message
     */
    public long getSequenceNumber();

    /**
     * Returns true if the message will not generate a response message
     * @return true if the message will not generate a response message
     */
    public boolean isOneWay();

    /**
     * Returns the time this message was created or deserialized
     * @return the time this message was created or deserialized
     */
    public long getTimeStamp();

    /**
     * Returns fault-tolerance infos piggybacked on this message
     * @return a MessageInfo object that contains fault-tolerance infos OR null
     * if the attached message has been sent by a non fault-tolerant object
     */
    public MessageInfo getMessageInfo();

    /**
     * Set fault-tolerance infos piggybacked on this message
     * @param mi a MessageInfo object that contains fault-tolerance infos
     */
    public void setMessageInfo(MessageInfo mi);

    /**
     * Return true if this message must be ignored by the receiver
     * @return true if this message must be ignored by the receiver
     */
    public boolean ignoreIt();

    /**
     * Set or unset the ignore tag.
     * @param ignore true if this request must be ignored, false otherwise.
     */
    public void setIgnoreIt(boolean ignore);

    /**
     * Set the FTManager that have to treat this message
     * @param ft the FTManager that have to treat this message
     */
    public void setFTManager(FTManager ft);

    /**
     * Return the FTManager that have to treat this message
     * @return the FTManager that have to treat this message
     */
    public FTManager getFTManager();
}
