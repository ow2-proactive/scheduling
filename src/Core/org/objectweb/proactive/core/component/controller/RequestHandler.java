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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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
 * @author The ProActive Team
 */
public interface RequestHandler {

    /**
     * If the current controller is suitable, the request is executed. If not executed,
     * it is passed to the next handler.
     * @param request the request to process
     * @return the result of the request
     * @throws MethodCallExecutionFailedException if the execution of a request failed (ProActive level)
     * @throws InvocationTargetException if the execution of a request failed (java reflection level)
     */
    public Object handleRequest(ComponentRequest request) throws MethodCallExecutionFailedException,
            InvocationTargetException;

    /**
     *
     * @return the next handler in the chain, or null
     */
    public RequestHandler nextHandler();

    /**
     * Sets the next handler in the chain of handlers.
     * @param handler the next handler in the chain of handlers.
     */
    public void setNextHandler(RequestHandler handler);
}
