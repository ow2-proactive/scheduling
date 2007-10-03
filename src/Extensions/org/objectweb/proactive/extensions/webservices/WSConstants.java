/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.webservices;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.soap.util.xml.QName;
import org.objectweb.proactive.extensions.webservices.soap.ProActiveProvider;


/**
 * @author vlegrand
 * Utility constants for deploying active objects and components as Web Services
 */
public class WSConstants {
    public static final String PROACTIVE_PROVIDER = ProActiveProvider.class.getName();
    public static final String PROACTIVE_STUB = "Stub";
    public static final String WSDL_FILE = "Wsdl";
    public static final String WEBAPP_NAME = "proactive";
    public static final String ROUTER = "/" + WEBAPP_NAME +
        "/servlet/rpcrouter";
    public static final String DOCUMENTATION = "ProActive Active Object";
    public static final String COMPONENT_INTERFACE = "Compoment Interface";
    public static final String URL_PUBLICATION = "URL";
    public static final Vector<String> disallowedMethods = new Vector<String>();

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
        disallowedMethods.addElement("isFcInternalItf");
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
    protected static Vector<Class<?>> supportedTypes = new Vector<Class<?>>();

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
        supportedTypes.addElement(Array.newInstance(Byte.TYPE, 0).getClass());
        supportedTypes.addElement(Array.newInstance(Boolean.TYPE, 0).getClass());
        supportedTypes.addElement(Array.newInstance(Double.TYPE, 0).getClass());
        supportedTypes.addElement(Array.newInstance(Long.TYPE, 0).getClass());
        supportedTypes.addElement(Array.newInstance(Float.TYPE, 0).getClass());
        supportedTypes.addElement(Array.newInstance(Integer.TYPE, 0).getClass());
        supportedTypes.addElement(Array.newInstance(Object.class, 0).getClass());
        supportedTypes.addElement(Vector.class);
        supportedTypes.addElement(Hashtable.class);
        supportedTypes.addElement(Map.class);
        supportedTypes.addElement(Enumeration.class);
    }
}
