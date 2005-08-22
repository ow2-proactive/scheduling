/*
 * Created on 4 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.proactive.ext.security.exceptions;

import java.io.Serializable;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CertificateException extends Exception implements Serializable {

    /**
     *
     */
    public CertificateException() {
        super();
    }

    /**
     * @param arg0
     */
    public CertificateException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public CertificateException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public CertificateException(Throwable arg0) {
        super(arg0);
    }
}
