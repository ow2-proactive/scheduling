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
package org.objectweb.proactive.core.exceptions.manager;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class ExceptionMaskStack {

    /* List of ExceptionMaskLevel, starts with the top */
    private LinkedList stack;

    /* The combination of all masks */
    private ExceptionMaskLevel currentExceptionMask;

    /* The potential pending exception */
    private Throwable currentException;

    /* Exceptions caught in the current or previous level */
    private Collection caughtExceptions;

    private ExceptionMaskStack() {
        stack = new LinkedList();
        currentExceptionMask = new ExceptionMaskLevel();
    }

    /*
     * As there is one call stack per thread, there is also one exception
     * stack per thread. So we use the threadlocal stuff which may be an
     * optimized way of doing this thread local thing
     */
    private static ThreadLocal threadLocalMask = new ThreadLocal() {
            protected synchronized Object initialValue() {
                return new ExceptionMaskStack();
            }
        };

    /* The mask for the current thread */
    static ExceptionMaskStack get() {
        return (ExceptionMaskStack) threadLocalMask.get();
    }

    void push(Class[] exceptions) {
        ExceptionMaskLevel level = new ExceptionMaskLevel(this, exceptions);
        stack.add(0, level);
        currentExceptionMask.addExceptionTypes(level);
        caughtExceptions = level.getCaughtExceptions();
    }

    void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("The stack has nothing to pop");
        }

        caughtExceptions = ((ExceptionMaskLevel) stack.removeFirst()).getCaughtExceptions();
        updateExceptionMask();
    }

    /* Recompute the full mask */
    private void updateExceptionMask() {
        currentExceptionMask = new ExceptionMaskLevel();
        Iterator iter = stack.iterator();
        while (iter.hasNext()) {
            ExceptionMaskLevel level = (ExceptionMaskLevel) iter.next();
            currentExceptionMask.addExceptionTypes(level);
        }
    }

    void throwArrivedException() {
        if (currentException != null) {
            Throwable exc = currentException;
            currentException = null; // No more pending
            ExceptionThrower.throwException(exc);
        }
    }

    private ExceptionMaskLevel getTopLevel() {
        try {
            return (ExceptionMaskLevel) stack.getFirst();
        } catch (NoSuchElementException nsee) {
            throw new IllegalStateException("Exception stack is empty");
        }
    }

    void waitForPotentialException(boolean allLevels) {
        if (allLevels) {
            Iterator iter = stack.iterator();
            while (iter.hasNext()) {
                ExceptionMaskLevel level = (ExceptionMaskLevel) iter.next();
                level.waitForPotentialException();
            }
        } else {
            getTopLevel().waitForPotentialException();
        }
    }

    /* Optimization: we don't always insert at the top level */
    ExceptionMaskLevel findBestLevel(Class[] c) {
        Iterator iter = stack.iterator();
        while (iter.hasNext()) {
            ExceptionMaskLevel level = (ExceptionMaskLevel) iter.next();
            if (level.catchRuntimeException() ||
                    level.areExceptionTypesCaught(c)) {
                return level;
            }
        }

        throw new IllegalStateException("No exception level found");
    }

    void waitForIntersection(Class[] exceptions) {
        if (currentExceptionMask.areExceptionTypesCaught(exceptions)) {
            Iterator iter = stack.iterator();
            while (iter.hasNext()) {
                ExceptionMaskLevel level = (ExceptionMaskLevel) iter.next();
                if (level.areExceptionTypesCaught(exceptions)) {
                    level.waitForPotentialException();
                }
            }
        }
    }

    /* Currently we only keep the first exception, but we could be smarter */
    void setException(Throwable e) {
        if (currentException == null) {
            currentException = e;
        }
    }

    boolean areExceptionTypesCaught(Class[] c) {
        return currentExceptionMask.areExceptionTypesCaught(c);
    }

    boolean isExceptionTypeCaught(Class c) {
        return currentExceptionMask.isExceptionTypeCaught(c);
    }

    boolean isRuntimeExceptionHandled() {
        return currentExceptionMask.catchRuntimeException();
    }

    Collection getCaughtExceptions() {
        return getTopLevel().getCaughtExceptions();
    }
}
