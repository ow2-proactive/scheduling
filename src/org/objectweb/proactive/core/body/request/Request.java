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
package org.objectweb.proactive.core.body.request;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.message.Message;
import org.objectweb.proactive.core.body.reply.Reply;

/**
 * <p>
 * A class implementing this interface is an object encapsulating a reified method call.
 * Any method call on an active object ends up as a Request sent to its associated body.
 * The request must implements this Request interface.
 * </p><p>
 * In addition to the standard messaging facilities (sender, receiver) it adds the concepts
 * of method call and forwarding, which is, the ability for a request to pass on from 
 * one body to another in case of migration.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface Request extends Message {

  /**
   * Returns true if the request has been forwarded
   * @return true if the request has been forwarded
   */
  public boolean hasBeenForwarded();


  /**
   * Returns the parameter number <code>index</code> from the method call
   * embedded in the request
   * @param index the position of the parameter to return.
   * @return the object passed in parameter of the method call that matches
   * the given position or null if no match.
   */
  public Object getParameter(int index);
  
  
  /**
   * Returns the sender of this request
   * @return the sender of this request
   */
  public UniversalBody getSender();
  
      
  /**
   * Sends this request to the body destination
   * @param destinationBody the body destination of this request
   * @exception java.io.IOException if the request fails to be sent
   */
  public void send(UniversalBody destinationBody) throws java.io.IOException;


  /**
   * Serves this request by executing the embedded method call using the given 
   * <code>targetBody</code>. Once the eventual result obtained from the method call
   * a the reply is returned (based on that result).
   * @param targetBody the body destination of the call
   * @return the reply built using the result or null if the request is one way
   * @exception ServeException if the method call fails to be served
   */
  public Reply serve(Body targetBody) throws ServeException;


  /**
   * Notifies the request that it has been received by the destination.
   * When this request gets fowarded, this method must not be called as a
   * fowarder is not the genuine destination of the request.
   * @param bodyReceiver the body destination that received the request
   * @exception java.io.IOException if the request failed to perform a possible 
   * operation upon that notification
   */
  public void notifyReception(UniversalBody bodyReceiver) throws java.io.IOException;
  
}
