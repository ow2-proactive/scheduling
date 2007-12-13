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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class ExceptionMaskStack {

    /* List of ExceptionMaskLevel, starts with the top */
    private LinkedList<ExceptionMaskLevel> stack;

    /* The combination of all masks */
    private ExceptionMaskLevel currentExceptionMask;

    private ExceptionMaskStack() {
        stack = new LinkedList<ExceptionMaskLevel>();
        currentExceptionMask = new ExceptionMaskLevel();
    }

    /*
     * As there is one call stack per thread, there is also one exception
     * stack per thread. So we use the threadlocal stuff which may be an
     * optimized way of doing this thread local thing
     */
    private static ThreadLocal threadLocalMask = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return new ExceptionMaskStack();
        }
    };

    /* The mask for the current thread */
    static ExceptionMaskStack get() {
        return (ExceptionMaskStack) threadLocalMask.get();
    }

    void push(Class<?>[] exceptions) {
        ExceptionMaskLevel level = new ExceptionMaskLevel(this, exceptions);
        stack.add(0, level);
        currentExceptionMask.addExceptionTypes(level);
    }

    void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("The stack has nothing to pop");
        }

        stack.removeFirst();
        updateExceptionMask();
    }

    /* Recompute the full mask */
    private void updateExceptionMask() {
        currentExceptionMask = new ExceptionMaskLevel();
        Iterator<ExceptionMaskLevel> iter = stack.iterator();
        while (iter.hasNext()) {
            ExceptionMaskLevel level = iter.next();
            currentExceptionMask.addExceptionTypes(level);
        }
    }

    void throwArrivedException() {
        Collection caughtExceptions = getTopLevel().getCaughtExceptions();
        synchronized (caughtExceptions) {
            if (!caughtExceptions.isEmpty()) {
                Throwable exc = (Throwable) caughtExceptions.iterator().next();
                ExceptionThrower.throwException(exc);
            }
        }
    }

    private ExceptionMaskLevel getTopLevel() {
        try {
            return stack.getFirst();
        } catch (NoSuchElementException nsee) {
            throw new IllegalStateException("Exception stack is empty");
        }
    }

    void waitForPotentialException(boolean allLevels) {
        if (allLevels) {
            Iterator<ExceptionMaskLevel> iter = stack.iterator();
            while (iter.hasNext()) {
                ExceptionMaskLevel level = iter.next();
                level.waitForPotentialException();
            }
        } else {
            getTopLevel().waitForPotentialException();
        }
    }

    /* Optimization: we don't always insert at the top level */
    ExceptionMaskLevel findBestLevel(Class<?>[] c) {
        Iterator<ExceptionMaskLevel> iter = stack.iterator();
        while (iter.hasNext()) {
            ExceptionMaskLevel level = iter.next();
            if (level.catchRuntimeException() || level.areExceptionTypesCaught(c)) {
                return level;
            }
        }

        throw new IllegalStateException("No exception level found");
    }

    void waitForIntersection(Class<?>[] exceptions) {
        if (currentExceptionMask.areExceptionTypesCaught(exceptions)) {
            Iterator<ExceptionMaskLevel> iter = stack.iterator();
            while (iter.hasNext()) {
                ExceptionMaskLevel level = iter.next();
                if (level.areExceptionTypesCaught(exceptions)) {
                    level.waitForPotentialException();
                }
            }
        }
    }

    boolean areExceptionTypesCaught(Class<?>[] c) {
        return currentExceptionMask.areExceptionTypesCaught(c);
    }

    boolean isExceptionTypeCaught(Class<?> c) {
        return currentExceptionMask.isExceptionTypeCaught(c);
    }

    boolean isRuntimeExceptionHandled() {
        return currentExceptionMask.catchRuntimeException();
    }

    Collection getAllExceptions() {
        return getTopLevel().getAllExceptions();
    }
}
