package org.objectweb.proactive.core.exceptions.manager;

import java.util.Iterator;
import java.util.LinkedList;


public class ExceptionMaskStack {

    /* This state is used to know if the fix in the finally block is needed */
    private static final int STATE_AFTER_PUSH = 0;
    private static final int STATE_AFTER_POP = 1;
    private int state;

    /* List of ExceptionMaskLevel, starts with the top */
    private LinkedList stack;

    /* The combination of all masks */
    private ExceptionMaskLevel currentExceptionMask;

    /* The potential pending exception */
    private Throwable currentException;

    private ExceptionMaskStack() {
        state = STATE_AFTER_POP;
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
        currentExceptionMask.addExceptions(level);
        state = STATE_AFTER_PUSH;
    }

    void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("The stack has nothing to pop");
        }

        stack.removeFirst();
        updateExceptionMask();
        state = STATE_AFTER_POP;
    }

    void fixupPop() {
        if (state == STATE_AFTER_PUSH) {
            pop();
        }
    }

    /* Recompute the full mask */
    private void updateExceptionMask() {
        currentExceptionMask = new ExceptionMaskLevel();
        Iterator iter = stack.iterator();
        while (iter.hasNext()) {
            ExceptionMaskLevel level = (ExceptionMaskLevel) iter.next();
            currentExceptionMask.addExceptions(level);
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
        return (ExceptionMaskLevel) stack.getFirst();
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
            if (level.catchRuntimeException() || level.isCaught(c)) {
                return level;
            }
        }

        throw new IllegalStateException("No exception level found");
    }

    void waitForIntersection(Class[] exceptions) {
        if (currentExceptionMask.isCaught(exceptions)) {
            Iterator iter = stack.iterator();
            while (iter.hasNext()) {
                ExceptionMaskLevel level = (ExceptionMaskLevel) iter.next();
                if (level.isCaught(exceptions)) {
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

    boolean isCaught(Class[] c) {
        return currentExceptionMask.isCaught(c);
    }

    boolean isCaught(Class c) {
        return currentExceptionMask.isCaught(c);
    }

    boolean isRuntimeExceptionHandled() {
        return currentExceptionMask.catchRuntimeException();
    }
}
