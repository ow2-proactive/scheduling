package org.objectweb.proactive.core.exceptions.proxy;

import org.objectweb.proactive.core.body.request.ServeException;


public class ServiceFailedCallerNFE extends ProxyNonFunctionalException {
    public ServiceFailedCallerNFE(String message, ServeException e) {
        super(message, e);
    }
}
