package org.objectweb.proactive.core.group;

import java.lang.reflect.*;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.mop.*;


/**
 * This class provides a simple proxy to add 'standard' Java object into a ProActive Group
 * 
 * @author Laurent Baduel - INRIA
 * @see org.objectweb.proactive.core.mop.Proxy
 */

public class ProxyForJavaObject extends AbstractProxy implements org.objectweb.proactive.core.mop.Proxy, java.io.Serializable
{ 
    protected Object target;
	
	public ProxyForJavaObject () {};

    public ProxyForJavaObject (ConstructorCall constructorCall, Object[] parameters)
    {
	try
	    {
		this.target = constructorCall.execute();
	    }
	catch (Exception e)
	    {
		e.printStackTrace();
		this.target = null;
	    }
    }
    
    
	protected void setTarget (Object o) {
		target = o;
	}
/*
    public Object reify(MethodCall c) throws InvocationTargetException, IllegalAccessException
    {
        try
            {
                Object o = c.execute (target);
                return o;
            }
        catch (MethodCallExecutionFailedException e)
            {
                e.printStackTrace();
                return null;
            }
    }
*/
    public Object reify(MethodCall mc) throws InvocationTargetException, IllegalAccessException {
	try
	    {
		Object result = null;
        if (!AbstractProxy.isOneWayCall(mc)) {
	        Object[] paramProxy = new Object[0];
    	    result = MOP.newInstance(mc.getReifiedMethod().getReturnType().getName(), 
        	                                      null, 
            	                                  Constants.DEFAULT_FUTURE_PROXY_CLASS_NAME,
                	                              paramProxy);
	        ((org.objectweb.proactive.core.body.future.FutureProxy)((StubObject)result).getProxy()).setResult(mc.execute(target)); }
		else {result = mc.execute(target); }
		return result;
	    }		
	catch (MethodCallExecutionFailedException e) { e.printStackTrace(); return null; }
	catch (Exception e) { e.printStackTrace(); return null; }
    }
}
