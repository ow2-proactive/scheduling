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
package org.objectweb.proactive;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.message.MessageEventProducer;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * <P>
 * An object implementing this interface is an implementation of the non fonctionnal part 
 * of an ActiveObject. This representation is local to the ActiveObject. By contrast there
 * is a remote representation of Body that can be accessed by distant object.
 * </P><P>
 * The body of an ActiveObject provides needed services such as a the ability to sent and
 * receive request and reply.
 * </P><P>
 * The interface also defines how the 'live' method of an active object sees its Body.
 * Subclasses of Body implementing additional service routines should extend this
 * interface to provide access to these new routines from the 'live' method.
 * </P><P>
 * A body has 2 associated states :
 * <ul>
 * <li>alive : the body is alive as long as it is processing request and reply</li>
 * <li>active : the body is active as long as it has an associated thread running
 *              to serve the requests by calling methods on the active object.</li>
 * </ul>
 * </P><P>
 * Note that a thread can be alive but not active, such as a forwarder that just
 * forward request to another peer.
 * </P>
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 */
public interface Body extends UniversalBody, MessageEventProducer {

  /** 
   * Sets the request factory for this body 
   * @param the request factory associated to this body
   */ 
  public void setRequestFactory(RequestFactory requestFactory);


  /** 
   * Returns the request queue associated to this body 
   * @return the request queue associated to this body
   */ 
  public BlockingRequestQueue getRequestQueue();


  /** 
   * Returns the reified object that body is for
   * The reified object is the object that has been turned active.
   * @return the reified object that body is for
   */ 
  public Object getReifiedObject();
  
  
  /** 
   * Returns the name of this body that can be used for displaying information
   * @return the name of this body
   */ 
  public String getName();


  /**
   * Returns the url of the node this body is associated to
   * The url of the node can change if the active object migrates
   * @return the url of the node this body is associated to
   */
  public String getNodeURL();


  /**
   * Tries to find a local version of the body of id uniqueID. If a local version
   * is found it is returned. If not, tries to find the body of id uniqueID in the 
   * known body of this body. If a body is found it is returned, else null is returned.
   * @param uniqueID the id of the body to lookup
   * @return the last known version of the body of id uniqueID or null if not known
   */
  public UniversalBody checkNewLocation(UniqueID uniqueID);


  /**
   * Sends the request <code>request</code> with the future <code>future</code> to the local body
   * <code>body</code>.
   * @param methodCall the methodCall to send
   * @param future the future associated to the request
   * @param destinationBody the body the request is sent to
   * @param factory the RequestFactory to used to create the request to send or null to delegate 
   * the creation of the request to this body.
   * @exception java.io.IOException if the request cannot be sent to the destination body
   */
  public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody, RequestFactory factory) throws java.io.IOException;


  /**
   * blocks all incoming communications. After this call, the body cannot
   * receive any request or reply.
   */
  public void blockCommunication();


  /**
   * Signals the body to accept all incoming communications. This call undo 
   * a previous call to blockCommunication.
   */
  public void acceptCommunication();


  /**
   * Invoke the default fifo policy to pick up the requests from the request queue.
   * This does not return until the body terminate, as the active thread enters in
   * an infinite for processing the request in the fifo order.
   */
  public void fifoPolicy();


  /**
   * Returns whether the body is alive or not.
   * The body is alive as long as it is processing request and reply
   * @return whether the body is alive or not.
   */
  public boolean isAlive();
  

  /**
   * Returns whether the body is active or not.
   * The body is active as long as it has an associated thread running
   * to serve the requests by calling methods on the active object.
   * @return whether the body is active or not.
   */
  public boolean isActive();


  /**
   * Terminate the body. After this call the body is no more alive and no more active
   * although the active thread is not interrupted. The body is unuseable after this call.
   */
  public void terminate();

  
  /**
   * Serves the request <code>request</code> by the invoking the targeted method on the
   * reified object. Some specific type of request may involve special processing that 
   * does not trigger a method on the reified object.
   * @param request the request to serve
   */
  public void serve(Request request);


}
