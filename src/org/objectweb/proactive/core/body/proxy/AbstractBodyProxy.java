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
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.StubObject;


public abstract class AbstractBodyProxy extends AbstractProxy
    implements BodyProxy,
               java.io.Serializable {
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
    public Object reify(MethodCall methodCall)
                 throws Throwable {
        if (methodCall.getName().equals("equals")) {
            //there is only one argument to this method
            Object arg = methodCall.getParameter(0);
            if (MOP.isReifiedObject(arg)) {
                AbstractBodyProxy bodyProxy = (AbstractBodyProxy)((StubObject)arg).getProxy();
                return new Boolean(bodyID.equals(bodyProxy.bodyID));
            } else {
                return new Boolean(false);
            }
        }
        // Now gives the MethodCall object to the body
        try {
            if (isOneWayCall(methodCall)) {
                reifyAsOneWay(methodCall);
                return null;
            }
            if (isAsynchronousCall(methodCall)) {
                return reifyAsAsynchronous(methodCall);
            }
            return reifyAsSynchronous(methodCall);
        } catch (MethodCallExecutionFailedException e) {
            throw new ProActiveRuntimeException(e.getMessage(), 
                                                e.getTargetException());
        }
         catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else if (t instanceof Error) {
                throw (Error)t;
            } else {
                // check now which exception can be safely thrown
                Class[] declaredExceptions = methodCall.getReifiedMethod().getExceptionTypes();
                for (int i = 0; i < declaredExceptions.length; i++) {
                    Class exceptionClass = declaredExceptions[i];
                    if (exceptionClass.isAssignableFrom(t.getClass())) {
                        throw t;
                    }
                }
                // Here we should extend the behavior to accept exception Handler
                throw new ProActiveRuntimeException(t);
            }
        }
    }

    /**
   *
   */

  protected void reifyAsOneWay(MethodCall methodCall) throws MethodCallExecutionFailedException {
    try {
      sendRequest(methodCall, null);
    } catch (java.io.IOException e) {
      throw new MethodCallExecutionFailedException("Exception occured in reifyAsOneWay while sending request for m="+methodCall.getName(), e);
    }
  }


  protected Object reifyAsAsynchronous(MethodCall methodCall) throws MethodCallExecutionFailedException {
    StubObject futureobject;
    // Creates a stub + FutureProxy for representing the result
    try {
      futureobject = (StubObject)MOP.newInstance(methodCall.getReifiedMethod().getReturnType().getName(), null, Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
    } catch (MOPException e) {
      throw new MethodCallExecutionFailedException("Exception occured in reifyAsAsynchronous while creating future for m="+methodCall.getName(), e);
    } catch (ClassNotFoundException e) {
      throw new MethodCallExecutionFailedException("Exception occured in reifyAsAsynchronous while creating future for m="+methodCall.getName(), e);
    }
   
    // Set the id of the body creator in the created future
    FutureProxy fp = (FutureProxy)(futureobject.getProxy());
    fp.setCreatorID(bodyID);
   
    // Send the request
    try {
      sendRequest(methodCall, (Future)futureobject.getProxy());
    } catch (java.io.IOException e) {
      throw new MethodCallExecutionFailedException("Exception occured in reifyAsAsynchronous while sending request for m="+methodCall.getName(), e);
    }
    // And return the future object
    return futureobject;
  }


  protected Object reifyAsSynchronous(MethodCall methodCall) throws Throwable, MethodCallExecutionFailedException {
    // Setting methodCall.res to null means that we do not use the future mechanism
    Future f = FutureProxy.getFutureProxy();
    f.setCreatorID(bodyID);
    // Set it as the 'thing' to send results to methodCall.res = f;
    // Send the request
    try {
      sendRequest(methodCall, f);
    } catch (java.io.IOException e) {
      throw new MethodCallExecutionFailedException("Exception occured in reifyAsSynchronous while sending request for m="+methodCall.getName(), e);
    }
    // Returns the result
    if (f.getRaisedException() != null) {
      throw f.getRaisedException();
    } else {
      return f.getResult();
    }
  }



    protected abstract void sendRequest(MethodCall methodCall, Future future) throws java.io.IOException;
	
	protected abstract void sendRequest(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException;

}