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
package org.objectweb.proactive.core.body.http;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.runtime.http.RuntimeReply;
import org.objectweb.proactive.ext.webservices.utils.HTTPRemoteException;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;
import org.objectweb.proactive.ext.webservices.utils.ReflectRequest;


/**
 *  This class is used to encapsulate  a request into an HTTP message
 * @author jbrocoll
 * @see java.io.Serializable
 * @see org.objectweb.proactive.ext.webservices.utils.ReflectRequest
 */
public class BodyRequest extends ReflectRequest implements Serializable
{
    private static Logger logger = Logger.getLogger("XML_HTTP");
    private String methodName;
    private ArrayList parameters = new ArrayList();
    private UniqueID oaid;
    private Body body = null;

    private static HashMap hMapMethods;
    
    static {
     	hMapMethods = getHashMapReflect(Body.class);
    	
    }
    
    /**
     * Construct a request to send to the Active object identified by the UniqueID
     * @param methodName The method name contained in the request 
     * @param parameters The parameters associated with the method
     * @param oaid The unique ID of targeted active object 
     */
    public BodyRequest(String methodName, ArrayList parameters, UniqueID oaid) {
        this.methodName = methodName;
        this.parameters = parameters;
        this.oaid = oaid;
        this.body = ProActiveXMLUtils.getBody(this.oaid);
    }
    
    /**
     *  This method process the request. Generally it is executed when the request is sent and unmarshalled.
     * @return a RuntimeReply containing the result of the method call
     * @throws Exception
     */
    public RuntimeReply process() throws Exception {
       
        		Object result = null;
 
        		
        		if(body == null )
        		     this.body = ProActiveXMLUtils.getBody(this.oaid);
        		
        		Method m = getProActiveRuntimeMethod(methodName,parameters, hMapMethods.get(methodName));
    			try {
					result = m.invoke(body, parameters.toArray());
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
}
