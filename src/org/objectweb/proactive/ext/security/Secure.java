/*
 * Created on 8 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.proactive.ext.security;

/**
 * @author acontes
 * Objects implementing this interface have access to ProActive security methods. 
 * Doing this, the object can monitor, check access and, by the way, block some
 * requests
 */
public interface Secure {
	
	public SecurityContext receiveRequest (SecurityContext sc);
	
	public SecurityContext execute (SecurityContext sc);
	
	
}
