/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.ext.webservices.wsdl;

import org.apache.axis.wsdl.fromJava.Emitter;

import org.objectweb.proactive.ext.webservices.WSConstants;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;

import java.util.Vector;

import javax.wsdl.WSDLException;

import javax.xml.parsers.ParserConfigurationException;


/**
 * @author vlegrand
 */
public class WSDLGenerator extends WSConstants {
    /**
     * Generate the WSDL document associate with an  active object exposed as a web service
     * @param c The class object that we want to obtain WSDL
     * @param serviceName The name of the service (urn)
     * @param urlRouter   The url where the service can be accessed
     * @param documentation The Documentation of the service
     * @param methods The public methods allowed for this service
     * @return a String containing the Wsdl document.
     */
    public static String getWSDL(Class c, String serviceName,
        String urlRouter, String documentation, String[] methods) {
        StringWriter sw = new StringWriter();
        String namespace = serviceName;

        try {
            Emitter emitter = new Emitter();

            emitter.setDisallowedMethods(disallowedMethods);
            
            if (methods != null) {
                Vector allowedMethods = new Vector(methods.length);
                for (int i = 0; i < methods.length; i++) {
                    allowedMethods.addElement(methods[i]);
                }
                emitter.setAllowedMethods(allowedMethods);
            }

            emitter.setLocationUrl(urlRouter);
            emitter.setIntfNamespace(namespace);
            emitter.setImplNamespace(namespace);
            emitter.setCls(c);
            emitter.setServiceElementName(serviceName);
           
            
            String wsdl = emitter.emitToString(Emitter.MODE_ALL);

            return wsdl;
        } catch (WSDLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
