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
package org.objectweb.proactive.core.runtime.http;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.ext.webservices.utils.HTTPRemoteException;
import org.objectweb.proactive.ext.webservices.utils.ReflectRequest;

/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RuntimeRequest extends ReflectRequest implements Serializable {
	private static Logger logger = Logger.getLogger("XML_HTTP");
    private String methodName;
    private ArrayList parameters = new ArrayList();
    private ArrayList paramsTypes;

    private static HashMap hMapMethods;
    private static ProActiveRuntimeImpl runtime;
    
    static {
   
    	// init the hashmap, that contains all the methods of  ProActiveRuntimeImpl 
        // in 'Object' (value) and the name of funtions in key 
        // (Warning two functions can t have the same name (for now)) 
        runtime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
     	hMapMethods = getHashMapReflect(runtime.getClass());
        
    }
    
    public RuntimeRequest(String newmethodName) {
        this.methodName = newmethodName;   	
    }
    
   
    public RuntimeRequest(String newmethodName, ArrayList newparameters) {
        this(newmethodName);
        this.parameters = newparameters;
    }

    public RuntimeRequest(String newmethodName, ArrayList newparameters,
        ArrayList newparamsTypes) {
        this(newmethodName,newparameters);
        this.paramsTypes = newparamsTypes;
    }

    public RuntimeReply process() throws Exception {
       
        		Object[] params = parameters.toArray();
        		Object result = null;
            
        			Method m = getProActiveRuntimeMethod(methodName,parameters, hMapMethods.get(methodName));
        			try {
						result = m.invoke(runtime, parameters.toArray());
					} catch (IllegalArgumentException e) {
						throw new HTTPRemoteException("Error during reflexion", e);
					} catch (IllegalAccessException e) {
						throw new HTTPRemoteException("Error during reflexion", e);
					} catch (InvocationTargetException e) {
						throw (Exception) e.getCause();	
						//throw new HTTPRemoteException("Error during reflexion", e);
					}
            return new RuntimeReply(result);
    }

    public String getMethodName() {
        return this.methodName;
    }

    public ArrayList getParameters() {
        return this.parameters;
    }
       
}
