/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body.proxy;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.exceptions.FutureCreationException;
import org.objectweb.proactive.core.body.exceptions.SendRequestCommunicationException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.MethodCallInfo;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public abstract class AbstractBodyProxy extends AbstractProxy implements BodyProxy, java.io.Serializable {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    private static Logger syncCallLogger = ProActiveLogger.getLogger(Loggers.SYNC_CALL);

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected Integer cachedHashCode = null;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AbstractBodyProxy() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements BodyProxy -----------------------------------------------
    //
    public UniqueID getBodyID() {
        return getBody().getID();
    }

    //
    // -- implements Proxy -----------------------------------------------
    //

    /**
     * Performs operations on the Call object created by the stub, thus changing the semantics of
     * message-passing to asynchronous message-passing with future objects
     * 
     * 
     * The semantics of message-passing implemented by this proxy class may be definied as follows :
     * <UL>
     * <LI>Asynchronous message-passing
     * <LI>Creation of future objects where possible (which leads to wait-by-necessity).
     * <LI>Synchronous, blocking calls where futures are not available.
     * <LI>The Call <code>methodCall</code> is passed to the skeleton for execution.
     * </UL>
     */
    public Object reify(MethodCall methodCall) throws Throwable {
        Object cachedMethodResult = checkOptimizedMethod(methodCall);
        if (cachedMethodResult != null) {
            return cachedMethodResult;
        }

        return invokeOnBody(methodCall);
    }

    /*
     * HACK: toString() can be implicitly called by log4j, which may result in a deadlock if we call
     * log4j inside log4j, so for now, we disable the message for toString().
     */
    private static boolean isToString(MethodCall methodCall) {
        return (methodCall.getNumberOfParameter() == 0) && "toString".equals(methodCall.getName());
    }

    private static boolean isHashCode(MethodCall methodCall) {
        return (methodCall.getNumberOfParameter() == 0) && "hashCode".equals(methodCall.getName());
    }

    private static Set<String> loggedSyncCalls = Collections.synchronizedSet(new HashSet<String>());

    private Object invokeOnBody(MethodCall methodCall) throws Exception, RenegotiateSessionException,
            Throwable {
        // Now gives the MethodCall object to the body
        try {
            MethodCallInfo mci = methodCall.getMethodCallInfo();

            if (mci.getType() == MethodCallInfo.CallType.OneWay) {
                reifyAsOneWay(methodCall);
                return null;
            }

            if (mci.getType() == MethodCallInfo.CallType.Asynchronous) {
                return reifyAsAsynchronous(methodCall);
            }

            if (!isToString(methodCall) && !isHashCode(methodCall) &&
                syncCallLogger.isEnabledFor(Level.DEBUG)) {
                String msg = "[DEBUG: synchronous call] All calls to the method below are synchronous " +
                    "(not an error, but may lead to performance issues or deadlocks):" +
                    System.getProperty("line.separator") + methodCall.getReifiedMethod() +
                    System.getProperty("line.separator") + "They are synchronous for the following reason: " +
                    mci.getMessage();

                if (loggedSyncCalls.add(msg)) {
                    syncCallLogger.debug(msg);
                }
            }

            return reifyAsSynchronous(methodCall);
        } catch (MethodCallExecutionFailedException e) {
            throw new ProActiveRuntimeException(e.getMessage(), e.getTargetException());
        }
    }

    // optimization may be a local execution or a caching mechanism
    // returns null if not applicable
    private Object checkOptimizedMethod(MethodCall methodCall) throws Exception, RenegotiateSessionException,
            Throwable {
        if (methodCall.getName().equals("equals") && (methodCall.getNumberOfParameter() == 1)) {
            Object arg = methodCall.getParameter(0);
            if (MOP.isReifiedObject(arg)) {
                Proxy proxy = ((StubObject) arg).getProxy();
                if (proxy instanceof AbstractBodyProxy) {
                    return Boolean.valueOf(getBodyID().equals(((AbstractBodyProxy) proxy).getBodyID()));
                }
            }

            return new Boolean(false);
        }

        if (methodCall.getName().equals("hashCode") && (methodCall.getNumberOfParameter() == 0)) {
            if (cachedHashCode == null) {
                return cachedHashCode = (Integer) invokeOnBody(methodCall);
            } else {
                return cachedHashCode;
            }
        }

        return null;
    }

    /**
     * 
     */
    protected void reifyAsOneWay(MethodCall methodCall) throws Exception, RenegotiateSessionException {
        sendRequest(methodCall, null);
    }

    /*
     * Dummy Future used to reply to a one-way method call with exceptions Declared as public to
     * accomodate the MOP
     */
    public static class VoidFuture {
        public VoidFuture() {
        }
    }

    protected Object reifyAsAsynchronous(MethodCall methodCall) throws Exception, RenegotiateSessionException {
        StubObject futureobject = null;

        // Creates a stub + FutureProxy for representing the result
        try {
            Class<?> returnType = null;
            Type t = methodCall.getReifiedMethod().getGenericReturnType();
            if (t instanceof TypeVariable) {
                returnType = methodCall.getGenericTypesMapping().get(t);
            } else {
                returnType = methodCall.getReifiedMethod().getReturnType();
            }

            if (returnType.equals(java.lang.Void.TYPE)) {
                /* A future for a void call is used to put the potential exception inside */
                futureobject = (StubObject) MOP.newInstance(VoidFuture.class, null,
                        Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
            } else {
                futureobject = (StubObject) MOP.newInstance(returnType, null,
                        Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME, null);
            }
        } catch (MOPException e) {
            throw new FutureCreationException(
                "Exception occured in reifyAsAsynchronous while creating future for methodcall = " +
                    methodCall.getName(), e);
        } catch (ClassNotFoundException e) {
            throw new FutureCreationException(
                "Exception occured in reifyAsAsynchronous while creating future for methodcall = " +
                    methodCall.getName(), e);
        }

        // Set the id of the body creator in the created future
        FutureProxy fp = (FutureProxy) (futureobject.getProxy());
        fp.setCreatorID(this.getBodyID());
        fp.setUpdater(this.getBody());
        fp.setOriginatingProxy(this);

        try {
            sendRequest(methodCall, fp);
        } catch (java.io.IOException e) {
            throw new SendRequestCommunicationException(
                "Exception occured in reifyAsAsynchronous while sending request for methodcall = " +
                    methodCall.getName(), e);
        }

        // And return the future object
        return futureobject;
    }

    protected Object reifyAsSynchronous(MethodCall methodCall) throws Throwable, Exception,
            RenegotiateSessionException {
        // Setting methodCall.res to null means that we do not use the future mechanism
        FutureProxy fp = FutureProxy.getFutureProxy();
        fp.setCreatorID(this.getBodyID());
        fp.setUpdater(this.getBody());

        try {
            sendRequest(methodCall, fp);
        } catch (java.io.IOException e) {
            throw new SendRequestCommunicationException(
                "Exception occured in reifyAsSynchronous while sending request for methodcall = " +
                    methodCall.getName(), e);
        }

        // Returns the result or throws the exception
        if (fp.getRaisedException() != null) {
            throw fp.getRaisedException();
        } else {
            return fp.getResult();
        }
    }

    protected abstract void sendRequest(MethodCall methodCall, Future future) throws java.io.IOException,
            RenegotiateSessionException, CommunicationForbiddenException;

    protected abstract void sendRequest(MethodCall methodCall, Future future, Body sourceBody)
            throws java.io.IOException, RenegotiateSessionException, CommunicationForbiddenException;
}
