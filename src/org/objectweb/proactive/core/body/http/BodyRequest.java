/*
 * Created on May 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
 * @author jerome
 *
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
    
    public BodyRequest(String newmethodName, ArrayList newparameters, UniqueID newoaid) {
        this.methodName = newmethodName;
        this.parameters = newparameters;
        this.oaid = newoaid;
        this.body = ProActiveXMLUtils.getBody(this.oaid);
    }

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
