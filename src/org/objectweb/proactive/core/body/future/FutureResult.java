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
