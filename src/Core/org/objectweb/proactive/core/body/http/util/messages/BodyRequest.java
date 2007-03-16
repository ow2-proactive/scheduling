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
package org.objectweb.proactive.core.body.http.util.messages;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.http.util.HttpUtils;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 *  This class is used to encapsulate  a request into an HTTP message
 * @author jbrocoll
 * @see java.io.Serializable
 */
public class BodyRequest extends ReflectRequest implements Serializable {
    private static HashMap hMapMethods;

    static {
        hMapMethods = getHashMapReflect(Body.class);
    }

    private String methodName;
    private ArrayList parameters = new ArrayList();
    private UniqueID oaid;
    private Body body = null;

    /**
     * Construct a request to send to the Active object identified by the UniqueID
     * @param methodName The method name contained in the request
     * @param parameters The parameters associated with the method
     * @param oaid The unique ID of targeted active object
     */
    public BodyRequest(String methodName, ArrayList parameters, UniqueID oaid,
        String url) {
        super(url);
        this.methodName = methodName;
        this.parameters = parameters;
        this.oaid = oaid;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public Object getReturnedObject() throws Exception {
        if (this.returnedObject instanceof Exception) {
            //            System.out.println("J'ai choppe une exception ( " + ClassServer.getUrl() + ")");
            throw (Exception) this.returnedObject;
        }
        return this.returnedObject;
    }

    /**
     *  This method process the request. Generally it is executed when the request is sent and unmarshalled.
     * @return a RuntimeReply containing the result of the method call
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Override
    public Object processMessage() throws Exception {
        Object result = null;

        if (body == null) {
            this.body = HttpUtils.getBody(this.oaid);
        }

        //System.out.println("invocation de la methode");
        Method m = getProActiveRuntimeMethod(methodName, parameters,
                hMapMethods.get(methodName));

        try {
            result = m.invoke(body, parameters.toArray());
        } catch (IllegalArgumentException e) {
            //               e.printStackTrace();
            throw e;
        } catch (IllegalAccessException e) {
            //                e.printStackTrace();
            throw e;
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block 
            //                e.printStackTrace();
            throw new SecurityNotAvailableException(e.getCause());
        } catch (Exception e) {
            //            System.out.println("----------------------- Exception : " +
            //                e.getClass());
            throw e;
        }

        return result;
    }
}
