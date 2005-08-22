package org.objectweb.proactive.core.exceptions.body;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStrategy;
import org.objectweb.proactive.core.body.request.ServeException;


public class ServiceFailedCalleeNFE extends BodyNonFunctionalException {
    public ServiceFailedCalleeNFE(String message, ServeException e,
        LocalBodyStrategy body, Body bodyOnThis) {
        super(message, e);
    }
}
