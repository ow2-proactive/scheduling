/*
 * Created on Aug 4, 2004
 *
 */
package org.objectweb.proactive.ext.webservices.utils;

import java.io.IOException;

/**
 * @author sbeucler
 *
 * @see java.rmi.RemoteException
 */
public class HTTPRemoteException extends IOException {
	public HTTPRemoteException() {
		super();
	}

	public HTTPRemoteException(String s) {
		super(s);
	}

	public HTTPRemoteException(String s, Throwable ex) {
		super(s);
		initCause(ex);
	}
}
