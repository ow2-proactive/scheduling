/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.body.request;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;


public class RequestReceiverImpl implements RequestReceiver,
    java.io.Serializable {
    public static Logger logger = Logger.getLogger(RequestReceiverImpl.class.getName());

    //list of immediate services (method names)
    private java.util.Vector immediateServices;

    public RequestReceiverImpl() {
        this.immediateServices = new java.util.Vector(2);
        this.immediateServices.add("toString");
        this.immediateServices.add("hashCode");
    }

    public void receiveRequest(Request request, Body bodyReceiver)
        throws java.io.IOException {
        if (immediateExecution(request.getMethodName())) {
            if (logger.isDebugEnabled()) {
                logger.debug("immediately serving " + request.getMethodName());
            }
            bodyReceiver.serve(request);
            if (logger.isDebugEnabled()) {
                logger.debug("end of service for " + request.getMethodName());
            }
        } else {
            request.notifyReception(bodyReceiver);
            bodyReceiver.getRequestQueue().add(request);
        }
    }

    private boolean immediateExecution(String methodName) {
        if (logger.isDebugEnabled()) {
            logger.debug("immediateExecution for methode " + methodName +
                " is " + immediateServices.contains(methodName));
        }

        //        if (immediateServices.contains(methodName)) {
        //            return true;
        //        } else {
        //            return false;
        //        }
        return immediateServices.contains(methodName);
    }

    public void setImmediateService(String methodName) {
        this.immediateServices.add(methodName);
    }
}
