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
package org.objectweb.proactive.core.runtime.http.messages;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.body.http.util.messages.ReflectRequest;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RuntimeRequest extends ReflectRequest implements Serializable {
    private String methodName;
    private ArrayList<Object> parameters = new ArrayList<Object>();
    private static HashMap hMapMethods;
    private static ProActiveRuntimeImpl runtime;

    static {
        // init the hashmap, that contains all the methods of  ProActiveRuntimeImpl 
        // in 'Object' (value) and the name of funtions in key 
        // (Warning two functions can t have the same name (for now)) 
        runtime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
        hMapMethods = getHashMapReflect(runtime.getClass());
    }

    public RuntimeRequest(String newmethodName, String url) {
        super(url);
        this.methodName = newmethodName;
    }

    public Object getReturnedObject() throws Exception {
        if (this.returnedObject instanceof Exception) {
            throw (Exception) this.returnedObject;
        }
        return this.returnedObject;
    }

    public RuntimeRequest(String newmethodName,
        ArrayList<Object> newparameters, String url) {
        this(newmethodName, url);
        this.parameters = newparameters;
    }

    public RuntimeRequest(String newmethodName,
        ArrayList<Object> newparameters, ArrayList newparamsTypes, String url) {
        this(newmethodName, newparameters, url);
    }

    @Override
    public Object processMessage() throws Exception {
        Object[] params = parameters.toArray();
        Object result = null;

        Method m = getProActiveRuntimeMethod(methodName, parameters,
                hMapMethods.get(methodName));
        try {
            result = m.invoke(runtime, parameters.toArray());
        } catch (IllegalArgumentException e) {
            //throw new HTTPRemoteException("Error during reflexion", e);
            //e.printStackTrace();
            //esult =e;
            throw e;
        } catch (IllegalAccessException e) {
            //throw new HTTPRemoteException("Error during reflexion", e);
            //.e.printStackTrace();
            //result = e;
            throw e;
        } catch (InvocationTargetException e) {
            //throw (Exception) e.getCause();	
            //throw new HTTPRemoteException("Error during reflexion", e);
            //					    e.printStackTrace();
            //					    result = e;
            throw (Exception) e.getCause();
        } catch (Exception e) {
            //					    result = e;
            throw e;
        }
        return result;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public ArrayList getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        return "[ " + methodName + " ( " + parameters + " )" + " ]";
    }
}
