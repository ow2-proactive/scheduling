/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
import org.objectweb.proactive.core.exceptions.manager.NFEManager;
import org.objectweb.proactive.core.exceptions.proxy.FutureCreationException;
import org.objectweb.proactive.core.exceptions.proxy.ProxyNonFunctionalException;
import org.objectweb.proactive.core.exceptions.proxy.SendRequestCommunicationException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


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
        if (methodCall.getName().equals("equals") &&
                (methodCall.getNumberOfParameter() == 1)) {
            Object arg = methodCall.getParameter(0);
            if (MOP.isReifiedObject(arg)) {
                Proxy proxy = ((StubObject) arg).getProxy();
                if (proxy instanceof AbstractBodyProxy) {
                    return new Boolean(bodyID.equals(
                            ((AbstractBodyProxy) proxy).bodyID));
                }
            }
            return new Boolean(false);
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
            ProxyNonFunctionalException nfe = new SendRequestCommunicationException(
                    "Exception occured in reifyAsOneWay while sending request for methodcall = " +
                    methodCall.getName(), e);

            NFEManager.fireNFE(nfe, this);
        }
    }

    /*
     * Dummy Future used to reply to a one-way method call with exceptions
     * Declared as public to accomodate the MOP
     */
    public static class VoidFuture {
        public VoidFuture() {
        }
    }

    protected Object reifyAsAsynchronous(MethodCall methodCall)
        throws Exception, RenegotiateSessionException {
        StubObject futureobject = null;

        // Creates a stub + FutureProxy for representing the result
        try {
            Class returnType = methodCall.getReifiedMethod().getReturnType();

            if (returnType.equals(java.lang.Void.TYPE)) {

                /* A future for a void call is used to put the potential exception inside */
                futureobject = (StubObject) MOP.newInstance(VoidFuture.class,
                        null, Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
            } else {
                futureobject = (StubObject) MOP.newInstance(returnType, null,
                        Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
            }
        } catch (MOPException e) {
            // Create a non functional exception encapsulating the network exception
            ProxyNonFunctionalException nfe = new FutureCreationException(
                    "Exception occured in reifyAsAsynchronous while creating future for methodcall = " +
                    methodCall.getName(), e);

            NFEManager.fireNFE(nfe, this);
        } catch (ClassNotFoundException e) {
            // Create a non functional exception encapsulating the network exception
            ProxyNonFunctionalException nfe = new FutureCreationException(
                    "Exception occured in reifyAsAsynchronous while creating future for methodcall = " +
                    methodCall.getName(), e);

            NFEManager.fireNFE(nfe, this);
        }

        // Set the id of the body creator in the created future
        FutureProxy fp = (FutureProxy) (futureobject.getProxy());
        fp.setCreatorID(bodyID);
        fp.setOriginatingProxy(this);

        try {
            sendRequest(methodCall, fp);
        } catch (java.io.IOException e) {
            // old stuff
            // throw new MethodCallExecutionFailedException("Exception occured in reifyAsAsynchronous while sending request for methodcall ="+methodCall.getName(), e);
            // Create a non functional exception encapsulating the network exception
            ProxyNonFunctionalException nfe = new SendRequestCommunicationException(
                    "Exception occured in reifyAsAsynchronous while sending request for methodcall = " +
                    methodCall.getName(), e);

            NFEManager.fireNFE(nfe, this);
        }

        // And return the future object
        return futureobject;
    }

    protected Object reifyAsSynchronous(MethodCall methodCall)
        throws Throwable, Exception, RenegotiateSessionException {
        // Setting methodCall.res to null means that we do not use the future mechanism
        FutureProxy fp = FutureProxy.getFutureProxy();
        fp.setCreatorID(bodyID);
        fp.setOriginatingProxy(this);

        try {
            sendRequest(methodCall, fp);
        } catch (java.io.IOException e) {
            // old stuff 
            // throw new MethodCallExecutionFailedException("Exception occured in reifyAsSynchronous while sending request for methodcall ="+methodCall.getName(), e);
            // Create a non functional exception encapsulating the network exception
            ProxyNonFunctionalException nfe = new SendRequestCommunicationException(
                    "Exception occured in reifyAsSynchronous while sending request for methodcall = " +
                    methodCall.getName(), e);

            NFEManager.fireNFE(nfe, this);
        }

        // Returns the result (exception returned is a functional one -> NFE is not needed)
        if (fp.getRaisedException() != null) {
            throw fp.getRaisedException();
        } else {
            return fp.getResult();
        }
    }

    protected abstract void sendRequest(MethodCall methodCall, Future future)
        throws java.io.IOException, RenegotiateSessionException;

    protected abstract void sendRequest(MethodCall methodCall, Future future,
        Body sourceBody)
        throws java.io.IOException, RenegotiateSessionException;
}
