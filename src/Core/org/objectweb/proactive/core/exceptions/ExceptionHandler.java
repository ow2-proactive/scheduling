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
package org.objectweb.proactive.core.exceptions;

import java.lang.reflect.Method;
import java.util.Collection;

import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExceptionContext;


public class ExceptionHandler {

    /* Called by the user */
    public static void tryWithCatch(Class[] exceptions) {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForIntersection(exceptions);
            stack.push(exceptions);
        }
    }

    public static void throwArrivedException() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.throwArrivedException();
        }
    }

    public static void waitForPotentialException() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForPotentialException(true);
        }
    }

    public static void endTryWithCatch() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForPotentialException(false);
        }
    }

    public static void removeTryWithCatch() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.pop();
        }
    }

    public static Collection getAllExceptions() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            return stack.getAllExceptions();
        }
    }

    /* Called by ProActive on the client side */
    public static void addRequest(MethodCall methodCall, FutureProxy future) {
        MethodCallExceptionContext context = methodCall.getExceptionContext();
        if (context.isExceptionAsynchronously() ||
                context.isRuntimeExceptionHandled()) {
            ExceptionMaskStack stack = ExceptionMaskStack.get();
            synchronized (stack) {
                Method m = methodCall.getReifiedMethod();
                ExceptionMaskLevel level = stack.findBestLevel(m.getExceptionTypes());
                level.addFuture(future);
            }
        }
    }

    public static void addResult(FutureProxy future) {
        ExceptionMaskLevel level = future.getExceptionLevel();
        if (level != null) {
            level.removeFuture(future);
        }
    }

    public static MethodCallExceptionContext getContextForCall(Method m) {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            boolean runtime = stack.isRuntimeExceptionHandled();
            boolean async = stack.areExceptionTypesCaught(m.getExceptionTypes());
            MethodCallExceptionContext res = new MethodCallExceptionContext(runtime,
                    async);

            //            System.out.println(m + " => " + res);
            return res;
        }
    }

    public static void throwException(Throwable exception) {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        if (exception instanceof Error) {
            throw (Error) exception;
        }

        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            if (!stack.isExceptionTypeCaught(exception.getClass())) {
                RuntimeException re = new IllegalStateException(
                        "Invalid Future Usage");
                re.initCause(exception);
                throw re;
            }

            ExceptionThrower.throwException(exception);
        }
    }
}
