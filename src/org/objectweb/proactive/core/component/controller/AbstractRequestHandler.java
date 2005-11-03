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
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


/**
 * This class is able to handle requests, and checks if the current request can be
 * handled by the current handler in the chain of handlers (here, it is actually
 * a list of controllers). If this is the case, then the request is executed by
 * this handler. Otherwise, the request is transferred to the next handler in
 * the chain.
 *
 * @author Matthieu Morel
 */
public abstract class AbstractRequestHandler implements RequestHandler,
    Serializable {
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

    public Object handleRequest(ComponentRequest request)
        throws MethodCallExecutionFailedException, InvocationTargetException {
        if (request.getMethodCall().getComponentInterfaceName().equals(getFcItfName()) && request.getTargetClass().isAssignableFrom(this.getClass())) {
            return request.getMethodCall().execute(this);
        }
        if (nextHandler() != null) {
            return nextHandler().handleRequest(request);
        }
        throw new MethodCallExecutionFailedException(
            "cannot find a suitable handler for this request");
    }
    
    abstract public String getFcItfName();
}
