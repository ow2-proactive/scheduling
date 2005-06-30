package org.objectweb.proactive.core.component.controller;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;

/**
 * This class is able to handle requests, check if the current request can be
 * handled by the current handler in the chain of handlers (here, it is actually
 * a list of controllers). If this is the case, then the request is executed by
 * this handler. Otherwise, the request is transferred to the next handler in
 * the chain.
 * 
 * @author Matthieu Morel
 */
public abstract class AbstractRequestHandler implements RequestHandler {

    private RequestHandler nextHandler = null;
    /*
     * @see org.objectweb.proactive.core.component.controller.RequestHandler#nextHandler()
     */
    public RequestHandler nextHandler() {
        return nextHandler;
    }
    /*
     * @see org.objectweb.proactive.core.component.controller.RequestHandler#setNextHandler(org.objectweb.proactive.core.component.controller.RequestHandler)
     */
    public void setNextHandler(RequestHandler handler) {
        nextHandler = handler;
    }

    public Object handleRequest(ComponentRequest request) throws MethodCallExecutionFailedException, InvocationTargetException {
        if(request.getTargetClass().isAssignableFrom(this.getClass())) {
            return request.getMethodCall().execute(this);
        }
        if (nextHandler()!=null) {
            return nextHandler().handleRequest(request);
        }
        throw new MethodCallExecutionFailedException("cannot find a suitable handler for this request");
    }
}
