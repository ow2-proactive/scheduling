/*
 * Created on Mar 21, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.ext.webservices;

import org.apache.soap.util.xml.QName;

import java.math.BigDecimal;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;


/**
 * @author vlegrand
 * Utility constants for deploying active objects and components as Web Services
 */
public class WSConstants {
    public static final String PROACTIVE_PROVIDER = "org.objectweb.proactive.ext.webservices.soap.ProActiveProvider";
    public static final String PROACTIVE_STUB = "Stub";
    public static final String WSDL_FILE = "Wsdl";
    public static final String ROUTER = "/soap/servlet/rpcrouter";
    public static final String DOCUMENTATION = "ProActive Active Object";
    public static final String COMPONENT_INTERFACE = "Compoment Interface";
    public static final String URL_PUBLICATION = "URL";
    public static final Vector disallowedMethods = new Vector();

    static {
        disallowedMethods.addElement("equals");
        disallowedMethods.addElement("toString");
        disallowedMethods.addElement("runActivity");
        disallowedMethods.addElement("setProxy");
        disallowedMethods.addElement("getProxy");
        disallowedMethods.addElement("setFcItfName");
        disallowedMethods.addElement("isFcInternalItf");
        disallowedMethods.addElement("setFcOwner");
        disallowedMethods.addElement("setFcIsInternal");
        disallowedMethods.addElement("getFcItfName");
        disallowedMethods.addElement("getFcItfType");
        disallowedMethods.addElement("getFcItfOwner");
        disallowedMethods.addElement("IsFcInternalItf");
        disallowedMethods.addElement("setFcItfImpl");
        disallowedMethods.addElement("getFcItfImpl");
        disallowedMethods.addElement("setFcType");
        disallowedMethods.addElement("getFcItfImpl");
        disallowedMethods.addElement("getFcItfImpl");
        disallowedMethods.addElement("wait");
        disallowedMethods.addElement("notify");
        disallowedMethods.addElement("notifyAll");
        disallowedMethods.addElement("getClass");
        disallowedMethods.addElement("hashCode");
    }

    /* A vector containing all supported types by Apache Soap */
    protected static Vector supportedTypes = new Vector();

    static {
        supportedTypes.addElement(String.class);
        supportedTypes.addElement(Boolean.class);
        supportedTypes.addElement(Boolean.TYPE);
        supportedTypes.addElement(Double.class);
        supportedTypes.addElement(Double.TYPE);
        supportedTypes.addElement(Long.class);
        supportedTypes.addElement(Long.TYPE);
        supportedTypes.addElement(Float.class);
        supportedTypes.addElement(Float.TYPE);
        supportedTypes.addElement(Integer.class);
        supportedTypes.addElement(Integer.TYPE);
        supportedTypes.addElement(Short.class);
        supportedTypes.addElement(Byte.class);
        supportedTypes.addElement(Byte.TYPE);
        supportedTypes.addElement(BigDecimal.class);
        supportedTypes.addElement(GregorianCalendar.class);
        supportedTypes.addElement(Date.class);
        supportedTypes.addElement(QName.class);
    }
}
