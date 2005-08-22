package org.objectweb.proactive.core.exceptions.proxy;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public abstract class ProxyNonFunctionalException extends NonFunctionalException {
    public ProxyNonFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
