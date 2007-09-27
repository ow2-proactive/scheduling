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
package org.objectweb.proactive.api;

import java.util.Collection;

import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.exceptions.manager.ExceptionHandler;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.exceptions.manager.NFEManager;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


public class ProException {

    /*** <Exceptions> See ExceptionHandler.java for the documentation ***/
    /**
     * This has to be called just before a try block for a single exception.
     *
     * @param c the caught exception type in the catch block
     */
    public static void tryWithCatch(Class c) {
        tryWithCatch(new Class[] { c });
    }

    /**
     * This has to be called just before a try block for many exceptions.
     *
     * @param c the caught exception types in the catch block
     */
    public static void tryWithCatch(Class[] c) {
        ExceptionHandler.tryWithCatch(c);
    }

    /**
     * This has to be called at the end of the try block.
     */
    public static void endTryWithCatch() {
        ExceptionHandler.endTryWithCatch();
    }

    /**
     * This has to be called at the beginning of the finally block, so
     * it requires one.
     */
    public static void removeTryWithCatch() {
        ExceptionHandler.removeTryWithCatch();
    }

    /**
     * This can be used to query a potential returned exception, and
     * throw it if it exists.
     */
    public static void throwArrivedException() {
        ExceptionHandler.throwArrivedException();
    }

    /**
     * Get the exceptions that have been caught in the current
     * ProActive.tryWithCatch()/ProActive.removeTryWithCatch()
     * block. This waits for every call in this block to return.
     *
     * @return a collection of these exceptions
     */
    public static Collection getAllExceptions() {
        return ExceptionHandler.getAllExceptions();
    }

    /**
     * This is used to wait for the return of every call, so that we know
     * the execution can continue safely with no pending exception.
     */
    public static void waitForPotentialException() {
        ExceptionHandler.waitForPotentialException();
    }

    /**
     * Find out if the object contains an exception that should be thrown
     * @param future the future object that is examinated
     * @return true iff an exception should be thrown when accessing the object
     */
    public static boolean isException(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return false;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future it's not an exception
            if (!(theProxy instanceof Future)) {
                return false;
            } else {
                return ((Future) theProxy).getRaisedException() != null;
            }
        }
    }

    /**
     * Add a listener for NFE reaching the local JVM
     *
     * @param listener The listener to add
     */
    public static void addNFEListenerOnJVM(NFEListener listener) {
        NFEManager.addNFEListener(listener);
    }

    /**
     * Add a listener for NFE reaching a given active object
     *
     * @param ao The active object receiving the NFE
     * @param listener The listener to add
     */
    public static void addNFEListenerOnAO(Object ao, NFEListener listener) {

        /* Security hazard: arbitrary code execution by the ao... */
        UniversalBody body = AbstractBody.getRemoteBody(ao);
        body.addNFEListener(listener);
    }

    /**
     * Add a listener for NFE reaching the client side of a given active object
     *
     * @param ao The active object receiving the NFE
     * @param listener The listener to add
     */
    public static void addNFEListenerOnProxy(Object ao, NFEListener listener) {
        try {
            ((AbstractProxy) ao).addNFEListener(listener);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException(
                "The object must be a proxy to an active object");
        }
    }

    /**
     * Add a listener for NFE regarding a group.
     *
     * @param group The group receiving the NFE
     * @param listener The listener to add
     */
    public static void addNFEListenerOnGroup(Object group, NFEListener listener) {
        ProGroup.getGroupProxy(group).addNFEListener(listener);
    }

    /**
     * Remove a listener for NFE reaching the local JVM
     *
     * @param listener The listener to remove
     */
    public static void removeNFEListenerOnJVM(NFEListener listener) {
        NFEManager.removeNFEListener(listener);
    }

    /**
     * Remove a listener for NFE reaching a given active object
     *
     * @param ao The active object receiving the NFE
     * @param listener The listener to remove
     */
    public static void removeNFEListenerOnAO(Object ao, NFEListener listener) {
        UniversalBody body = AbstractBody.getRemoteBody(ao);
        body.removeNFEListener(listener);
    }

    /**
     * Remove a listener for NFE reaching the client side of a given active object
     *
     * @param ao The active object receiving the NFE
     * @param listener The listener to remove
     */
    public static void removeNFEListenerOnProxy(Object ao, NFEListener listener) {
        try {
            ((AbstractProxy) ao).removeNFEListener(listener);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException(
                "The object must be a proxy to an active object");
        }
    }

    /**
     * Remove a listener for NFE regarding a group.
     *
     * @param group The group receiving the NFE
     * @param listener The listener to remove
     */
    public static void removeNFEListenerOnGroup(Object group,
        NFEListener listener) {
        ProGroup.getGroupProxy(group).removeNFEListener(listener);
    }
}
