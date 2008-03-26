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
package org.objectweb.proactive.core.remoteobject.http.util.messages;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;


/**
 *  This class is used to encapsulate  a request into an HTTP message
 * @author The ProActive Team
 * @see java.io.Serializable
 */
public class HttpRemoteObjectRequest extends ReflectRequest implements Serializable {
    private static HashMap hMapMethods;

    static {
        hMapMethods = getHashMapReflect(RemoteObject.class);
    }

    private String methodName;
    private List<Object> parameters = new ArrayList<Object>();
    private UniqueID oaid;
    private InternalRemoteRemoteObject remoteObject = null;

    /**
     * Construct a request to send to the remote object identified by the url
     * @param methodName The method name contained in the request
     * @param parameters The parameters associated with the method
     * @param oaid The unique ID of targeted active object
     */
    public HttpRemoteObjectRequest(String methodName, List<Object> parameters, String url) {
        super(url);
        this.methodName = methodName;
        this.parameters = parameters;
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

        if (remoteObject == null) {
            this.remoteObject = HTTPRegistry.getInstance().lookup(url);
        }

        //System.out.println("invocation de la methode");
        Method m = getMethod(methodName, parameters, hMapMethods.get(methodName));

        try {
            result = m.invoke(remoteObject, parameters.toArray());
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
