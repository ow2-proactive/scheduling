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

import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.migration.AbstractMigratableBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
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
  
  private transient InitActive initActive; // used only once when active object is started first time
  private RunActive runActive;
  private EndActive endActive;


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
  public BodyImpl(ConstructorCall c, String nodeURL, Active activity, MetaObjectFactory factory) throws java.lang.reflect.InvocationTargetException, ConstructorCallExecutionFailedException {
    // Creates the reified object
    super(c.execute(), nodeURL, factory);
    
    // InitActive
    if (activity != null && activity instanceof InitActive) {
      initActive = (InitActive) activity;
    } else if (reifiedObject instanceof InitActive) {
      initActive = (InitActive) reifiedObject;
    }
    
    // RunActive
    if (activity != null && activity instanceof RunActive) {
      runActive = (RunActive) activity;
    } else if (reifiedObject instanceof RunActive) {
      runActive = (RunActive) reifiedObject;
    } else {
      runActive = new RunActive() {
          public void runActivity(Body body) {
            fifoPolicy();
          }
        };
    }
    
    // EndActive
    if (activity != null && activity instanceof EndActive) {
      endActive = (EndActive) activity;
    } else if (reifiedObject instanceof EndActive) {
      endActive = (EndActive) reifiedObject;
    } else {
      endActive = null;
    }

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
    // execute the initialization if needed. Only once
    if (initActive != null) {
      initActive.initActivity(this);
      initActive = null; // we won't do it again
    }
    // run the activity of the body
    try {
      runActive.runActivity(this);
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
      // execute the end of activity
      if (endActive != null) endActive.endActivity(this);
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
