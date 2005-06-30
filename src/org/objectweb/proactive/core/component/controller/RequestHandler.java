package org.objectweb.proactive.core.component.controller;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;

/**
 * A RequestHandler is able to control the flow of a component request in a
 * chain of handlers. If the request can be handled by the current handler, then
 * it is executed here, otherwise it is passed to the next handler in the chain
 * of handlers.
 * 
 * @author Matthieu Morel
 */
public interface RequestHandler {
    
    public Object handleRequest(ComponentRequest request) throws MethodCallExecutionFailedException, InvocationTargetException;
    
    public RequestHandler nextHandler();
    
    public void setNextHandler(RequestHandler handler);

}
