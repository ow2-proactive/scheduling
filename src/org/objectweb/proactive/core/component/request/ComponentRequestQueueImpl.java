/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.component.request;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;


/**
 * Extension of the standard ProActive request queue.
 * It enables the control of the life cycle of components.
 * The algorithm is the following :
 * 
 * loop
 * 	if componentLifeCycle.isStarted() 
 * 		get next request
 * 		// all requests are served
 * 	else 	if componentLifeCycle.isStopped() 
 * 		get next component controller request
 *			// only component request are served
 *		;
 * 	
 * 	if gotten request is a component life cycle request
 * 		if startFc --> set started = true
 * 		if stopFc --> set started = false
 * 	
 * 
 * @author Matthieu Morel
 *
 */
public class ComponentRequestQueueImpl extends BlockingRequestQueueImpl implements ComponentRequestQueue {
    protected static Logger logger = Logger.getLogger(ComponentRequestQueueImpl.class.getName());
    private boolean started = false;
    private RequestFilter requestFilterOnComponentControllerClasses;

    /**
     * Constructor for ComponentRequestQueueImpl.
     * @param ownerID
     */
    public ComponentRequestQueueImpl(UniqueID ownerID) {
        super(ownerID);
        requestFilterOnComponentControllerClasses = new RequestFilterOnComponentControllerClasses();
    }

    public synchronized void start() {
        started = true;
    }

    public synchronized void stop() {
        started = false;
    }

    public synchronized boolean isStarted() {
        return started;
    }

    public synchronized Request blockingRemoveOldest() {
        Request req;

        if (isStarted()) {
            req = super.blockingRemoveOldest();
        } else {
//            if (logger.isDebugEnabled()) {
//                logger.debug("ComponentBody is currently stopped");
//            }
//            // if the component is stopped, we serve only the component controller requests
            req = super.blockingRemoveOldest(requestFilterOnComponentControllerClasses);
        }

        // if we get a life cycle controller action, we should directly process it here
        if (req instanceof ComponentRequest) {
            if (req.getMethodCall().getReifiedMethod().getDeclaringClass().equals(LifeCycleController.class)) {
                if (req.getMethodName().equals("startFc")) {
                    start();
                } else {
                    if (req.getMethodName().equals("stopFc")) {
                        stop();
                    }
                }
            }
        }
        return req;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // private filtering class
    private class RequestFilterOnComponentControllerClasses implements RequestFilter, java.io.Serializable {
        public RequestFilterOnComponentControllerClasses() {
        }

        public boolean acceptRequest(Request request) {
            if (request instanceof ComponentRequest) {
                // request is accepted only if originating from a component controller class
				return ((ComponentRequest) request).isControllerRequest();
            } else {
                // standard requests cannot be component controller requests
                return false;
            }
        }
    }
}