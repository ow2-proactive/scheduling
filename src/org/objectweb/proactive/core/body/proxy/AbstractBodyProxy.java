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
package org.objectweb.proactive.core.body.proxy;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallImpl;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractBodyProxy extends AbstractProxy implements BodyProxy, java.io.Serializable  {

  //
  // -- STATIC MEMBERS -----------------------------------------------
  //


  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //

  protected UniqueID bodyID;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public AbstractBodyProxy() {
  }

  AbstractBodyProxy(UniqueID bodyID) {
    this.bodyID = bodyID;
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  //
  // -- implements BodyProxy -----------------------------------------------
  //

  public UniqueID getBodyID() {
    return bodyID;
  }
  
  //
  // -- implements Proxy -----------------------------------------------
  //

  /**
   * Performs operations on the Call object created by the stub, thus changing
   * the semantics of message-passing to asynchronous message-passing with
   * future objects
   *
   *
   * The semantics of message-passing implemented by this proxy class
   * may be definied as follows :<UL>
   * <LI>Asynchronous message-passing
   * <LI>Creation of future objects where possible (which leads to
   * wait-by-necessity).
   * <LI>Synchronous, blocking calls where futures are not available.
   * <LI>The Call <code>methodCall</code> is passed to the skeleton for execution.
   * </UL>
   */
  public Object reify(MethodCall methodCall) throws InvocationTargetException, IllegalAccessException {
    if (methodCall.getName().equals("equals")) {
      //there is only one argument to this method
      Object arg = methodCall.getParameter(0);
      if (MOP.isReifiedObject(arg)) {
        AbstractBodyProxy bodyProxy = (AbstractBodyProxy) ((StubObject)arg).getProxy();
        return new Boolean(bodyID.equals(bodyProxy.bodyID));
      } else {
        return new Boolean(false);
      }
    }

    // Now gives the MethodCall object to the body
    if (isOneWayCall(methodCall)) {
      reifyAsOneWay(methodCall);
      return null;
    }
    if (isAsynchronousCall(methodCall)) {
      return reifyAsAsynchronous(methodCall);
    }
    return reifyAsSynchronous(methodCall);
  }
  
 
  /**
   *
   */
  protected void reifyAsOneWay(MethodCall methodCall) throws InvocationTargetException {
    try {
      sendRequest(methodCall, null);
    } catch (java.io.IOException e) {
      throw new InvocationTargetException(e);
    }
  }


  protected Object reifyAsAsynchronous(MethodCall methodCall) throws InvocationTargetException {
    StubObject futureobject;
    // Creates a stub + FutureProxy for representing the result
    try {
      futureobject = (StubObject)MOP.newInstance(methodCall.getReifiedMethod().getReturnType().getName(), null, Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
    } catch (Exception e) {
      throw new InvocationTargetException(e);
    }

    // Send the request
    try {
      sendRequest(methodCall, (Future)futureobject.getProxy());
    } catch (java.io.IOException e) {
      throw new InvocationTargetException(e);
    }
    // And return the future object
    return futureobject;
  }


  protected Object reifyAsSynchronous(MethodCall methodCall) throws InvocationTargetException {
    // Setting methodCall.res to null means that we do not use the future mechanism
    Future f = FutureProxy.getFutureProxy();
    // Set it as the 'thing' to send results to methodCall.res = f;
    // Send the request
    try {
      sendRequest(methodCall, f);
    } catch (java.io.IOException e) {
      throw new InvocationTargetException(e);
    }
    // Returns the result
    if (f.getRaisedException() != null) {
      throw (f.getRaisedException());
    } else {
      return f.getResult();
    }
  }
  

  protected abstract void sendRequest(MethodCall methodCall, Future future) throws java.io.IOException;
  

  //
  // -- PROTECTED STATIC UTILITY METHODS -----------------------------------------------
  //
 
  protected static ConstructorCall findBodyConstructorCall(Class bodyClass, String nodeURL, ConstructorCall reifiedObjectConstructorCall) throws ProActiveException {
    // Determines the constructor of the body object: it is the constructor that
    // has only one argument, this argument being of type ConstructorCall
    try {
      Class[] cstrargs = new Class[] { ConstructorCall.class, String.class };
      Constructor cstr = bodyClass.getConstructor(cstrargs);
      Object[] effargs = new Object[] { reifiedObjectConstructorCall, nodeURL };
      // A word of explanation: here we have two nested ConstructorCall objects:
      // 'bodyConstructorCall' is the reification of the construction of the body,
      // which contains another ConstructorCall object that represents the reification
      // of the construction of the reified object itself.
      return new ConstructorCallImpl(cstr, effargs);
    } catch (NoSuchMethodException e) {
      throw new ProActiveException("Class " + bodyClass.getName() + " has no constructor matching ", e);
    }
  } 
  
}
