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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


public class RequestImpl extends MessageImpl implements Request, java.io.Serializable {

  protected MethodCall methodCall;
  
  /**
   * Indicates if the method has been sent through a forwarder
   */
  protected int sendCounter;


  /** transient because we deal with the serialization of this variable 
      in a custom manner. see writeObject method*/
  protected transient UniversalBody sender;
  

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID) {
    super(sender.getID(), nextSequenceID, isOneWay, methodCall.getName());
    this.methodCall = methodCall;
    this.sender = sender;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  //
  // -- Implements Request -----------------------------------------------
  //
  
  public void send(UniversalBody destinationBody) throws java.io.IOException {
    //System.out.println("RequestSender: sendRequest  " + methodName + " to destination");
    sendCounter++;
    sendRequest(destinationBody);
  }

  public UniversalBody getSender() {
    return sender;
  }
 
  public Reply serve(Body targetBody) throws ServeException {
    Object result = serveInternal(targetBody);
    if (isOneWay || sender == null) return null;
    return createReply(targetBody, result);
  }


  public boolean hasBeenForwarded() {
    return sendCounter > 1;
  }
  
  
  public Object getParameter(int index) {
    return methodCall.getParameter(index);
  }
  
  public void notifyReception(UniversalBody bodyReceiver) throws java.io.IOException {
    if (! hasBeenForwarded()) return;
   // System.out.println("the request has been forwarded " + forwardCounter + " times");
    //we know c.res is a remoteBody since the call has been forwarded
    //if it is null, this is a one way call
    if (sender != null) {
      sender.updateLocation(bodyReceiver.getID(), bodyReceiver.getRemoteAdapter());
    }
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  protected Object serveInternal(Body targetBody) throws ServeException {
    try {
      return methodCall.execute(targetBody.getReifiedObject());
    } catch (MethodCallExecutionFailedException e) {
      e.printStackTrace();
      throw new ServeException("serve method " + methodCall.getReifiedMethod().toString() + " failed", e);
    } catch (java.lang.reflect.InvocationTargetException e) {
      Throwable t = e.getTargetException();
      // t.printStackTrace();
      if (isOneWay) {
        throw new ServeException("serve method " + methodCall.getReifiedMethod().toString() + " failed", t);
      } else {
        return t;
      }
    }
  }
  
  protected Reply createReply(Body targetBody, Object result) {
    return new ReplyImpl(targetBody.getID(), sequenceNumber, methodName, result);
  }

  protected void sendRequest(UniversalBody destinationBody) throws java.io.IOException {
    destinationBody.receiveRequest(this);
  }
  
  //
  // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
  //
  
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    out.defaultWriteObject();
    if (! isOneWay)
      out.writeObject(sender.getRemoteAdapter());
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    if (! isOneWay)
      sender = (UniversalBody) in.readObject(); // it is actually a UniversalBody
  }
  
}
