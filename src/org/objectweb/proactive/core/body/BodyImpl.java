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

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.migration.AbstractMigratableBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;


/**
 * This class is the default implementation of the Body interface.
 * An implementation of the Body interface, which lets the reified object
 * explicitely manage the queue of pending requests through its live() routine.
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see org.objectweb.proactive.Body
 * @see AbstractBody
 * @see AbstractMigratableBody
 *
 */
public class BodyImpl extends AbstractMigratableBody implements Runnable, java.io.Serializable {
  
  //
  // -- STATIC MEMBERS -----------------------------------------------
  //
 

  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //
  
  //
  // -- PRIVATE MEMBERS -----------------------------------------------
  //


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * Doesn't build anything, just for having one no-arg constructor
   */
  public BodyImpl() {
  }


  /**
   * Build the body object, then fires its service thread
   */
  public BodyImpl(ConstructorCall c, String nodeURL) throws java.lang.reflect.InvocationTargetException, ConstructorCallExecutionFailedException {
    // Creates the reified object
    super(c.execute(), nodeURL);
    startBody();
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //



  //
  // -- implements Runnable -----------------------------------------------
  //
  
  /**
   * The method executed by the active thread that will eventually launch the live 
   * method of the active object of the default live method of this body.
   */
  public void run() {
    activityStarted();
    // find out the live method to run
    // try to find live(Body body)
    java.lang.reflect.Method liveMethod = locateLiveRoutine(Constants.DEFAULT_BODY_INTERFACE);
    // then try to find a method live(<specific type of body)  
    if (liveMethod == null) liveMethod = locateLiveRoutine(this.getClass());
    try {  
      if (liveMethod == null) {
        // no live method found : default to fifoPolicy
        fifoPolicy();
      } else {
        // run the custom live method
        launchLive(liveMethod);
      }
      // the body terminate its activity
      if (isAlive()) {
        // serve remaining requests if non dead
        while (!(requestQueue.isEmpty())) {
          serve(requestQueue.removeOldest());
        }
      }
    } catch (Exception e) {
      System.out.println("Exception occured in live method of body "+toString()+". Now terminating the body");
      e.printStackTrace();
      terminate();
    } finally {
      if (isActive()) activityStopped();
    }
  }

    
  
  //
  // -- implements Body -----------------------------------------------
  //

  /**
   * Invoke the default fifo policy to pick up the requests from the request queue.
   * This does not return until the body terminate, as the active thread enters in
   * an infinite loop for processing requests in the fifo order.
   */
  public void fifoPolicy() {
    while (isActive()) {
      serve(requestQueue.blockingRemoveOldest());
    }
  }
  
  
  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  /**
   * Creates the component in charge of storing incoming requests.
   * @return the component in charge of storing incoming requests.
   */
  protected BlockingRequestQueue createRequestQueue() {
    return new org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl(bodyID);
  }
  
  
  /**
   * Creates the component in charge of receiving incoming requests.
   * @return the component in charge of receiving incoming requests.
   */
  protected RequestReceiver createRequestReceiver() {
    return new org.objectweb.proactive.core.body.request.RequestReceiverImpl();
  }
  
  
  /**
   * Creates the component in charge of receiving incoming replies.
   * @return the component in charge of receiving incoming replies.
   */
  protected ReplyReceiver createReplyReceiver() {
    return new org.objectweb.proactive.core.body.reply.ReplyReceiverImpl();
  }
  
  
  /**
   * Creates the component in charge of migration.
   * @return the component in charge of migration.
   */
  protected MigrationManager createMigrationManager() {
    return MetaObjectFactory.createMigrationManager();
  }
  
  
  /**
   * Launches the proper live method on the reified object if one is defined.
   * This method is called automagically by the constructor,
   * and should <b>only</b> be called by subclasses.
   * @param liveMethod the live method to launch on the reified object.
   */
  protected void launchLive(java.lang.reflect.Method liveMethod) {
    Object[] o = new Object[] { this };
    try {
      //  System.out.println("Invoking live routine in the reified Object");
      liveMethod.invoke(this.reifiedObject, o);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw new ProActiveRuntimeException("Exception in the live method "+liveMethod+" invoked", e);
    } catch (NullPointerException e) {
      throw new ProActiveRuntimeException("liveMethod "+liveMethod+" is null ?", e);
    } catch (IllegalArgumentException e) {
      throw new ProActiveRuntimeException("Wrong parameter to the live method "+liveMethod, e);
    } catch (IllegalAccessException e) {
      throw new ProActiveRuntimeException("live method "+liveMethod+" is not accessible", e);
    }
  }
  
  
  /**
   * Locates the live method on the reified object. The live method
   * searched is the one taking an object of class <code>aClass</code>
   * in parameter
   * @param aClass the class of the argument of the live method to look for.
   * @return the live method on the reified object taking an object of class 
   * <code>aClass</code> in paramter or null if such a method cannot be found.
   */
  protected java.lang.reflect.Method locateLiveRoutine(Class aClass) {
    try {
      Class[] liveargstype = new Class[] { aClass };
      return reifiedObject.getClass().getMethod("live", liveargstype);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }


  protected void finalize() throws Throwable {
    //System.err.println(">>>>>>>>>> Finalizing Body");
  }

  
  /**
   * Creates the active thread and start it using this runnable body.
   */
  protected void startBody() {
    Thread t = new Thread(this, shortClassName(getName())+" on "+getNodeURL());
    t.start();
  }
 

  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  private static String shortClassName(String fqn) {
    int n = fqn.lastIndexOf('.');
    if (n == -1 || n == fqn.length()-1) return fqn;
    return fqn.substring(n+1);
  }
  
  
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    out.defaultWriteObject();
  }


  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    startBody();
  }

}
