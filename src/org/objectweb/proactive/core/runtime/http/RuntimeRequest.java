/*
 * Created on May 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
