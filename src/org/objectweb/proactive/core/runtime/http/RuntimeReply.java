/*
 * Created on May 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.runtime.http;

/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RuntimeReply implements java.io.Serializable {
    
	private Object returnedObject;

	public RuntimeReply () { 
		new RuntimeReply (null);
	}
	
	public RuntimeReply (Object o) { 
		this.returnedObject = o;
	}

	public Object getReturnedObject () {
		return this.returnedObject;
	}

}
