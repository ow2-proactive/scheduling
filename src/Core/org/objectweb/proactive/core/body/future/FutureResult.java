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
package org.objectweb.proactive.core.body.future;

import java.io.Serializable;

import org.objectweb.proactive.core.exceptions.ExceptionHandler;


class ThisIsNotAnException extends Exception {
    public ThisIsNotAnException() {
        super("This is the call in the proxy");
    }
}


/**
 * This class is a placeholder for the result of a method call,
 * it can be an Object or a thrown Exception.
 */
public class FutureResult implements Serializable {

    /** The object to be returned */
    private Object result;

    /** The exception to throw */
    private Throwable exception;

    public FutureResult(Object result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

    public Object getResult() {
        if (exception != null) {
            ExceptionHandler.throwException(exception);
        }

        return result;
    }

    @Override
    public String toString() {
        String str = "[";
        if (exception != null) {
            str += ("ex:" + exception.getClass().getName());
        } else if (result != null) {
            str += result.getClass().getName();
        } else {
            str += "null";
        }

        return str + "]";
    }

    public void augmentException(StackTraceElement[] stackTrace) {
        Throwable cause = exception;
        if ((cause != null) && (stackTrace != null)) {
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            Exception origCause = new ThisIsNotAnException();
            origCause.setStackTrace(stackTrace);
            cause.initCause(origCause);
        }
    }
}
