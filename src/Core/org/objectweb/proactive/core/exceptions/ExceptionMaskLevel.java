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
package org.objectweb.proactive.core.exceptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.future.MethodCallResult;


public class ExceptionMaskLevel {

    /* Exception types in the catch blocks */
    private Collection caughtExceptionTypes;

    /* Actual caught exceptions in this level */
    private Collection caughtExceptions;

    /* Pending futures */
    private int nbFutures;

    /* The stack this level belongs to */
    private ExceptionMaskStack parent;

    /* Do we catch a subtype of RuntimeException */
    private boolean catchRuntimeException;

    /* Do we catch a Non Functional Exception */
    ExceptionMaskLevel(ExceptionMaskStack parent, Class<?>[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            if (!Throwable.class.isAssignableFrom(exceptions[i])) {
                throw new IllegalArgumentException(
                    "Only exceptions can be catched");
            }

            catchRuntimeException = catchRuntimeException ||
                RuntimeException.class.isAssignableFrom(exceptions[i]) ||
                exceptions[i].isAssignableFrom(RuntimeException.class);
        }

        if (exceptions.length < 1) {
            throw new IllegalArgumentException(
                "At least one exception must be catched");
        }

        caughtExceptionTypes = Arrays.asList(exceptions);
        caughtExceptions = new LinkedList();
        nbFutures = 0;
        this.parent = parent;
    }

    /* Empty constructor for ExceptionHandler */
    ExceptionMaskLevel() {
        caughtExceptionTypes = new LinkedList();
    }

    boolean isExceptionTypeCaught(Class<?> c) {
        Iterator iter = caughtExceptionTypes.iterator();
        while (iter.hasNext()) {
            Class<?> cc = (Class<?>) iter.next();
            if (cc.isAssignableFrom(c) || c.isAssignableFrom(cc)) {
                return true;
            }
        }

        return false;
    }

    /* We do an OR */
    boolean areExceptionTypesCaught(Class<?>[] exceptions) {
        if (caughtExceptionTypes.isEmpty()) {
            return false;
        }

        for (int i = 0; i < exceptions.length; i++) {
            if (isExceptionTypeCaught(exceptions[i])) {
                return true;
            }
        }

        return false;
    }

    void addExceptionTypes(ExceptionMaskLevel level) {
        Iterator iter = level.caughtExceptionTypes.iterator();
        while (iter.hasNext()) {
            Class<?> c = (Class<?>) iter.next();
            if (!isExceptionTypeCaught(c)) {
                caughtExceptionTypes.add(c);
            }
        }

        catchRuntimeException = catchRuntimeException ||
            level.catchRuntimeException();
    }

    synchronized void waitForPotentialException() {
        parent.throwArrivedException();
        while (nbFutures != 0) {
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                break;
            }
            parent.throwArrivedException();
        }
    }

    boolean catchRuntimeException() {
        return catchRuntimeException;
    }

    /* A call is launched */
    synchronized void addFuture(FutureProxy f) {
        if (f != null) {
            f.setExceptionLevel(this);
            nbFutures++;
        }
    }

    /* A future has returned */
    synchronized void removeFuture(FutureProxy f) {
        nbFutures--;
        MethodCallResult res = f.getFutureResult();

        if (res != null) {
            Throwable exception = f.getFutureResult().getException();
            if (exception != null) {
                synchronized (caughtExceptions) {
                    caughtExceptions.add(exception);
                }
            }
        }

        notifyAll();
    }

    Collection getCaughtExceptions() {
        return caughtExceptions;
    }

    synchronized Collection getAllExceptions() {
        while (nbFutures != 0) {
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                break;
            }
        }
        return caughtExceptions;
    }
}
