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
package org.objectweb.proactive.core.body;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.ThreadStore;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class gives a common implementation of the Body interface. It provides all
 * the non specific behavior allowing sub-class to write the detail implementation.
 * </p><p>
 * Each body is identify by an unique identifier.
 * </p><p>
 * All active bodies that get created in one JVM register themselves into a table that allows
 * to tack them done. The registering and deregistering is done by the AbstractBody and
 * the table is managed here as well using some static methods.
 * </p><p>
 * In order to let somebody customize the body of an active object without subclassing it,
 * AbstractBody delegates lot of tasks to satellite objects that implements a given
 * interface. Abstract protected methods instantiate those objects allowing subclasses
 * to create them as they want (using customizable factories or instance).
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see Body
 * @see UniqueID
 *
 */
public abstract class AbstractBody extends AbstractUniversalBody implements Body, java.io.Serializable {

  //
  // -- STATIC MEMBERS -----------------------------------------------
  //

  private static final String TERMINATED_BODY_EXCEPTION_MESSAGE = "The body has been Terminated";


  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //

  protected ThreadStore threadStore;
  
  // the current implementation of the local view of this body
  protected LocalBodyStrategy localBodyStrategy;

  //
  // -- PRIVATE MEMBERS -----------------------------------------------
  //

  /** whether the body has an activity done with a active thread */
  private transient boolean isActive;

  /** whether the body has been killed. A killed body has no more activity although
   stopping the activity thread is not immediate */
  private transient boolean isDead;


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * Creates a new AbstractBody.
   * Used for serialization.
   */
  public AbstractBody() {}

  /**
   * Creates a new AbstractBody for an active object attached to a given node.
   * @param reifiedObject the active object that body is for
   * @param nodeURL the URL of the node that body is attached to
   * @param factory the factory able to construct new factories for each type of meta objects 
   *                needed by this body
   */
  public AbstractBody(Object reifiedObject, String nodeURL, MetaObjectFactory factory) {
    super(nodeURL, factory.newRemoteBodyFactory());
    this.threadStore = factory.newThreadStoreFactory().newThreadStore();
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  /**
   * Returns a string representation of this object.
   * @return a string representation of this object
   */
  public String toString() {
    return "Body for "+localBodyStrategy.getName()+" node="+nodeURL+" id=" + bodyID;
  }


  //
  // -- implements UniversalBody -----------------------------------------------
  //
  
  public void receiveRequest(Request request) throws java.io.IOException {
    //if (reifiedObject != null)
    //  System.out.println("  --> "+reifiedObject.getClass().getName()+".receiveRequest m="+request.getMethodName());
    if (isDead) throw new java.io.IOException(TERMINATED_BODY_EXCEPTION_MESSAGE);
    this.threadStore.enter();
    internalReceiveRequest(request);
    this.threadStore.exit();
  }

  public void receiveReply(Reply reply) throws java.io.IOException {
    //if (reifiedObject != null)
    //  System.out.println("  --> "+reifiedObject.getClass().getName()+".receiveReply m="+reply.getMethodName());
    if (isDead) throw new java.io.IOException(TERMINATED_BODY_EXCEPTION_MESSAGE);
    this.threadStore.enter();
    internalReceiveReply(reply);
    this.threadStore.exit();
  }
  
  

  //
  // -- implements Body -----------------------------------------------
  //
  
  public void terminate() {
    if (isDead) return;
    isDead = true;
    activityStopped();
    // unblock is thread was block
    acceptCommunication();
  }
  

  public void blockCommunication() {
    threadStore.close();
  }

  public void acceptCommunication() {
    threadStore.open();
  }

  public boolean isAlive() {
    return !isDead;
  }

  public boolean isActive() {
    return isActive;
  }


  public UniversalBody checkNewLocation(UniqueID bodyID) {
    //we look in the location table of the current JVM
    Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);
    if (body != null) {
      // we update our table to say that this body is local
      location.putBody(bodyID, body);
      return body;
    } else {
      //it was not found in this vm let's try the location table
      return location.getBody(bodyID);
    }
  }

  //
  // -- implements LocalBody -----------------------------------------------
  //

  public FuturePool getFuturePool() {
    return localBodyStrategy.getFuturePool();
  }

  public BlockingRequestQueue getRequestQueue() {
    return localBodyStrategy.getRequestQueue();
  }

  public Object getReifiedObject() {
    return localBodyStrategy.getReifiedObject();
  }

  public String getName() {
    return localBodyStrategy.getName();
  }

  public void serve(Request request) {
    localBodyStrategy.serve(request);
  }

  public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody) throws java.io.IOException {
    localBodyStrategy.sendRequest(methodCall, future, destinationBody);
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  /**
   * Receives a request for later processing. The call to this method is non blocking
   * unless the body cannot temporary receive the request.
   * @param request the request to process
   * @exception java.io.IOException if the request cannot be accepted
   */
  protected abstract void internalReceiveRequest(Request request) throws java.io.IOException;

  /**
   * Receives a reply in response to a former request.
   * @param reply the reply received
   * @exception java.io.IOException if the reply cannot be accepted
   */
  protected abstract void internalReceiveReply(Reply reply) throws java.io.IOException;


  protected void setLocalBodyImpl(LocalBodyStrategy localBody) {
    localBodyStrategy = localBody;
  }
  
  
  /**
   * Signals that the activity of this body, managed by the active thread has just stopped.
   */
  protected void activityStopped() {
    if (! isActive) return;
    isActive = false;
    //We are no longer an active body
    LocalBodyStore.getInstance().unregisterBody(this);
  }

  /**
   * Signals that the activity of this body, managed by the active thread has just started.
   */
  protected void activityStarted() {
    if (isActive) return;
    isActive = true;
    // we associated this body to the thread running it
    LocalBodyStore.getInstance().setCurrentThreadBody(this);
    // we register in this JVM
    LocalBodyStore.getInstance().registerBody(this);
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //



  //
  // -- inner classes -----------------------------------------------
  //
}