package org.objectweb.proactive.core.group;

import java.lang.reflect.*;
import org.objectweb.proactive.core.mop.*;


/**
 * This class provides a simple proxy to add 'standard' Java object into a ProActive Group
 * 
 * @author Laurent Baduel - INRIA
 * @see org.objectweb.proactive.core.mop.Proxy
 */

public class ProxyForJavaObject implements org.objectweb.proactive.core.mop.Proxy
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
}
