package org.objectweb.proactive.core.exceptions.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public class ExceptionMaskLevel {

    /* Exception types in the catch blocks */
    private Collection caughtExceptions;

    /* Pending futures */
    private int nbFutures;

    /* The stack this level belongs to */
    private ExceptionMaskStack parent;

    /* Do we catch a subtype of RuntimeException */
    private boolean catchRuntimeException;

    /* Do we catch a Non Functional Exception */
    /* TODO: private boolean catchNFE; */
    ExceptionMaskLevel(ExceptionMaskStack parent, Class[] exceptions) {
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

        caughtExceptions = Arrays.asList(exceptions);
        nbFutures = 0;
        this.parent = parent;
    }

    /* Empty constructor for ExceptionHandler */
    ExceptionMaskLevel() {
        caughtExceptions = new LinkedList();
    }

    boolean isCaught(Class c) {
        Iterator iter = caughtExceptions.iterator();
        while (iter.hasNext()) {
            Class cc = (Class) iter.next();
            if (cc.isAssignableFrom(c) || c.isAssignableFrom(cc)) {
                return true;
            }
        }

        return false;
    }

    /* We do an OR */
    boolean isCaught(Class[] exceptions) {
        if (caughtExceptions.isEmpty()) {
            return false;
        }

        for (int i = 0; i < exceptions.length; i++) {
            if (isCaught(exceptions[i])) {
                return true;
            }
        }

        return false;
    }

    void addExceptions(ExceptionMaskLevel level) {
        Iterator iter = level.caughtExceptions.iterator();
        while (iter.hasNext()) {
            Class c = (Class) iter.next();
            if (!isCaught(c)) {
                caughtExceptions.add(c);
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
        FutureResult res = f.getFutureResult();

        NonFunctionalException nfe = res.getNFE();
        if ((nfe != null) && parent.isCaught(nfe.getClass())) {
            parent.setException(nfe);
        }

        Throwable exception = f.getFutureResult().getExceptionToRaise();
        if (exception != null) {
            parent.setException(exception);
        }

        notifyAll();
    }
}
