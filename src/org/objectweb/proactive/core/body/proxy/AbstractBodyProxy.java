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
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.communication.SendRequestCommunicationException;
import org.objectweb.proactive.core.exceptions.creation.FutureCreationException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.security.RenegotiateSessionException;


public abstract class AbstractBodyProxy extends AbstractProxy
    implements BodyProxy, java.io.Serializable {
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
    public Object reify(MethodCall methodCall) throws Throwable {
        if (methodCall.getName().equals("equals")) {
            //there is only one argument to this method
            Object arg = methodCall.getParameter(0);
            if (MOP.isReifiedObject(arg)) {
                AbstractBodyProxy bodyProxy = (AbstractBodyProxy) ((StubObject) arg).getProxy();
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
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                // check now which exception can be safely thrown
                Class[] declaredExceptions = methodCall.getReifiedMethod()
                                                       .getExceptionTypes();
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
    protected void reifyAsOneWay(MethodCall methodCall)
        throws Exception, RenegotiateSessionException {
        try {
            sendRequest(methodCall, null);
        } catch (java.io.IOException e) {
            // old stuff
            // throw new MethodCallExecutionFailedException("Exception occured in reifyAsOneWay while sending request for methodcall ="+methodCall.getName(), e);
            // Create a non functional exception encapsulating the network exception
            NonFunctionalException nfe = new SendRequestCommunicationException(
                    "Exception occured in reifyAsOneWay while sending request for methodcall =" +
                    methodCall.getName(), e);

            // Retrieve the right handler for the given exception
            Handler handler = ProActive.searchExceptionHandler(nfe, this);
            handler.handle(nfe,
                new MethodCallExecutionFailedException(
                    "Exception occured in reifyAsOneWay while sending request for methodcall =" +
                    methodCall.getName(), e));
        }
    }

    protected Object reifyAsAsynchronous(MethodCall methodCall)
        throws Exception, RenegotiateSessionException {
        StubObject futureobject = null;

        // Creates a stub + FutureProxy for representing the result
        try {
            futureobject = (StubObject) MOP.newInstance(methodCall.getReifiedMethod()
                                                                  .getReturnType(),
                    null, Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
        } catch (MOPException e) {
            // Create a non functional exception encapsulating the network exception
            NonFunctionalException nfe = new FutureCreationException(
                    "Exception occured in reifyAsAsynchronous while creating future for methodcall =" +
                    methodCall.getName(), e);

            // Retrieve the right handler for the given exception
            Handler handler = ProActive.searchExceptionHandler(nfe, this);
            handler.handle(nfe,
                new MethodCallExecutionFailedException(
                    "Exception occured in reifyAsAsynchronous while creating future for methodcall =" +
                    methodCall.getName(), e));
        } catch (ClassNotFoundException e) {
            // Create a non functional exception encapsulating the network exception
            NonFunctionalException nfe = new FutureCreationException(
                    "Exception occured in reifyAsAsynchronous while creating future for methodcall =" +
                    methodCall.getName(), e);

            // Retrieve the right handler for the given exception
            Handler handler = ProActive.searchExceptionHandler(nfe, this);
            handler.handle(nfe,
                new MethodCallExecutionFailedException(
                    "Exception occured in reifyAsAsynchronous while creating future for methodcall =" +
                    methodCall.getName(), e));
        }

        // Set the id of the body creator in the created future
        FutureProxy fp = (FutureProxy) (futureobject.getProxy());
        fp.setCreatorID(bodyID);

        // Send the request
        try {
            sendRequest(methodCall, (Future) futureobject.getProxy());
        } catch (java.io.IOException e) {
            // old stuff
            // throw new MethodCallExecutionFailedException("Exception occured in reifyAsAsynchronous while sending request for methodcall ="+methodCall.getName(), e);
            // Create a non functional exception encapsulating the network exception
            NonFunctionalException nfe = new SendRequestCommunicationException(
                    "Exception occured in reifyAsAsynchronous while sending request for methodcall =" +
                    methodCall.getName(), e);

            // Retrieve the right handler for the given exception
            Handler handler = ProActive.searchExceptionHandler(nfe, this);
            handler.handle(nfe,
                new MethodCallExecutionFailedException(
                    "Exception occured in reifyAsAsynchronous while sending request for methodcall =" +
                    methodCall.getName(), e));
        }

        // And return the future object
        return futureobject;
    }

    protected Object reifyAsSynchronous(MethodCall methodCall)
        throws Throwable, Exception, RenegotiateSessionException {
        // Setting methodCall.res to null means that we do not use the future mechanism
        Future f = FutureProxy.getFutureProxy();
        f.setCreatorID(bodyID);

        // Set it as the 'thing' to send results to methodCall.res = f;
        // Send the request
        try {
            sendRequest(methodCall, f);
        } catch (java.io.IOException e) {
            // old stuff 
            // throw new MethodCallExecutionFailedException("Exception occured in reifyAsSynchronous while sending request for methodcall ="+methodCall.getName(), e);
            // Create a non functional exception encapsulating the network exception
            NonFunctionalException nfe = new SendRequestCommunicationException(
                    "Exception occured in reifyAsSynchronous while sending request for methodcall =" +
                    methodCall.getName(), e);

            // Retrieve the right handler for the given exception
            Handler handler = ProActive.searchExceptionHandler(nfe, this);
            handler.handle(nfe,
                new MethodCallExecutionFailedException(
                    "Exception occured in reifyAsSynchronous while sending request for methodcall =" +
                    methodCall.getName(), e));
        }

        // Returns the result
        // note : it seems that a functional exception is returned -> NFE are not necessary
        if (f.getRaisedException() != null) {
            throw f.getRaisedException();
        } else {
            return f.getResult();
        }
    }

    protected abstract void sendRequest(MethodCall methodCall, Future future)
        throws java.io.IOException, RenegotiateSessionException;

    protected abstract void sendRequest(MethodCall methodCall, Future future,
        Body sourceBody)
        throws java.io.IOException, RenegotiateSessionException;
}
