/*
 * Created on Aug 4, 2004
 *
 */
package org.objectweb.proactive.ext.webservices.utils;

/**
 * @author sbeucler
 *
 * @see java.rmi.UnexpectedException
 */
public class HTTPUnexpectedException extends HTTPRemoteException {
	public HTTPUnexpectedException(String s) {
		super(s);
	}

	public HTTPUnexpectedException(String s, Throwable ex) {
		super(s, ex);
	}

}
