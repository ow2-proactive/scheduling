/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.message.Message;

/**
 * <p>
 * A <code>MessageEvent</code> occurs when a <code>Message</code> get sent or received.
 * </p>
 * 
 * @see org.objectweb.proactive.core.body.message.Message
 * @author  ProActive Team
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

  protected UniqueID destinationID;
  
  /**
   * Creates a new <code>MessageEvent</code> based on the message 
   * <code>message</code> and on the given action
   * @param <code>message</code> the message on which this event is based.
   * @param <code>messageActionType</code> the type of the action occuring with
   * this message either REQUEST_SENT/RECEIVED or REPLY_SENT/RECEIVED.
   */
  public MessageEvent(Message message, int messageActionType, UniqueID destinationID) {
    super(message, messageActionType);
    this.destinationID = destinationID;
  }
  

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  /**
   * Returns the id of the body receiver of the encapsulated message
   * @return the id of the body receiver of the encapsulated message
   */
  public UniqueID getDestinationBodyID() {
    return destinationID;
  }
  
  
  /**
   * Returns a string representation of this event
   * @return a string representation of this event
   */
  public String toString() {
    return "methodName="+getMethodName()+" sourceID="+getSourceBodyID()+" destinationID="+getDestinationBodyID()+" sequenceNumber="+getSequenceNumber();
  }
  
  public boolean wasSent() {
    return type == REQUEST_SENT || type == REPLY_SENT;
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


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private final Message getMessage() {
    return (Message) getSource();
  }
}