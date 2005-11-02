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
package org.objectweb.proactive.core.body.future;

import java.io.Serializable;

import org.objectweb.proactive.core.exceptions.manager.ExceptionHandler;
import org.objectweb.proactive.core.exceptions.proxy.ProxyNonFunctionalException;


/**
 * This class is a placeholder for the result of a method call,
 * it can be an Object or a thrown Exception.
 */
public class FutureResult implements Serializable {

    /** The object to be returned */
    private Object result;

    /** The exception to throw */
    private Throwable exception;

    /** It may contain a NFE */
    private ProxyNonFunctionalException nfe;

    public FutureResult(Object result, Throwable exception,
        ProxyNonFunctionalException nfe) {
        this.result = result;
        this.exception = exception;
        this.nfe = nfe;
    }

    public Throwable getExceptionToRaise() {
        return exception;
    }

    public ProxyNonFunctionalException getNFE() {
        return nfe;
    }

    public Object getResult() {
        if (exception != null) {
            ExceptionHandler.throwException(exception);
        }

        return result;
    }

    public String toString() {
        String str = "[";
        if (nfe != null) {
            str += ("nfe:" + nfe.getClass().getName());
        } else if (exception != null) {
            str += ("ex:" + exception.getClass().getName());
        } else if (result != null) {
            str += result.getClass().getName();
        } else {
            str += "null";
        }

        return str + "]";
    }
}
