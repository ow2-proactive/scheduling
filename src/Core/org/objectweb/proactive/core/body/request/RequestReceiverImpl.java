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
package org.objectweb.proactive.core.body.request;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.exceptions.InactiveBodyException;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RequestReceiverImpl implements RequestReceiver, java.io.Serializable {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.REQUESTS);

    private static List<Class<?>[]> ANY_PARAMETERS = null;

    static {
        ANY_PARAMETERS = new ArrayList<Class<?>[]>(1);
        ANY_PARAMETERS.add(new Class[] { AnyParametersClass.class });
    }

    /**
     * Class that represents any parameters for a method
     *
     */
    private static class AnyParametersClass implements Serializable {
    }

    //private java.util.Vector immediateServices;
    // refactored : keys are method names, and values are arrays of parameters types
    // map of immediate services (method names +lists of method parameters)
    private java.util.Map<String, List<Class<?>[]>> immediateServices;
    private AtomicInteger inImmediateService;

    public RequestReceiverImpl() {
        immediateServices = new Hashtable<String, List<Class<?>[]>>(4);
        immediateServices.put("toString", ANY_PARAMETERS);
        immediateServices.put("hashCode", ANY_PARAMETERS);
        immediateServices.put("_terminateAOImmediately", ANY_PARAMETERS);
        immediateServices.put("_ImmediateMethodCallDummy", ANY_PARAMETERS);
        this.inImmediateService = new AtomicInteger(0);
    }

    public int receiveRequest(Request request, Body bodyReceiver) {
        try {
            if (immediateExecution(request)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("immediately serving " + request.getMethodName());
                }
                this.inImmediateService.incrementAndGet();
                try {
                    bodyReceiver.serve(request);
                } finally {
                    this.inImmediateService.decrementAndGet();
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("end of service for " + request.getMethodName());
                }

                //Dummy value for immediate services...
                return FTManager.IMMEDIATE_SERVICE;
            } else {
                request.notifyReception(bodyReceiver);
                RequestQueue queue = null;
                try {
                    queue = bodyReceiver.getRequestQueue();
                } catch (InactiveBodyException e) {
                    throw new InactiveBodyException("Cannot add request \"" + request.getMethodName() +
                        "\" because this body is inactive", e);
                }
                return queue.add(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean immediateExecution(Request request) {
        if ((request == null) || (request.getMethodCall() == null) ||
            (request.getMethodCall().getReifiedMethod() == null)) {
            return false;
        } else {
            String methodName = request.getMethodName();
            if (immediateServices.containsKey(methodName)) {
                if (ANY_PARAMETERS.equals(immediateServices.get(methodName))) {
                    // method was registered using method name only
                    return true;
                } else {
                    @SuppressWarnings("unchecked")
                    Iterator<Class<?>[]> it = ((List) immediateServices.get(methodName)).iterator();
                    while (it.hasNext()) {
                        Class<?>[] next = (Class<?>[]) it.next();
                        if (Arrays.equals(next, request.getMethodCall().getReifiedMethod()
                                .getParameterTypes())) {
                            return true;
                        }
                    }

                    // not found
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public void setImmediateService(String methodName) {
        this.immediateServices.put(methodName, ANY_PARAMETERS);
    }

    public void removeImmediateService(String methodName) {
        this.immediateServices.remove(methodName);
    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) {
        if (immediateServices.containsKey(methodName)) {
            if (!ANY_PARAMETERS.equals(immediateServices.get(methodName))) {

                List<Class<?>[]> list = (List<Class<?>[]>) immediateServices.get(methodName);
                List<Class<?>[]> elementsToRemove = new ArrayList<Class<?>[]>(list.size());
                Iterator<Class<?>[]> it = list.iterator();
                while (it.hasNext()) {
                    Class<?>[] element = it.next();
                    if (Arrays.equals(element, parametersTypes)) {
                        // cannot modify a list while iterating over it => keep reference of 
                        // the elements to remove
                        elementsToRemove.add(element);
                    }
                }
                it = elementsToRemove.iterator();
                while (it.hasNext()) {
                    list.remove(it.next());
                }
            } else {
                immediateServices.remove(methodName);
            }
        } else {
            // methodName not registered
        }
    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes) {
        if (immediateServices.containsKey(methodName)) {
            if (ANY_PARAMETERS.equals(immediateServices.get(methodName))) {
                // there is already a filter on all methods with that name, whatever the parameters
                return;
            } else {
                ((List<Class<?>[]>) immediateServices.get(methodName)).add(parametersTypes);
            }
        } else {
            List<Class<?>[]> list = new ArrayList<Class<?>[]>();
            list.add(parametersTypes);
            immediateServices.put(methodName, list);
        }
    }

    public boolean isInImmediateService() throws IOException {
        return this.inImmediateService.intValue() > 0;
    }

}
