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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.webservices.wsdl;

import java.io.IOException;
import java.util.Vector;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.wsdl.fromJava.Emitter;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.xml.sax.SAXException;


/**
 * @author vlegrand
 */
public class WSDLGenerator extends WSConstants {

    /**
     * Generates the WSDL document associate with an  active object exposed as a web service
     * @param c The class object that we want to obtain WSDL
     * @param serviceName The name of the service (urn)
     * @param urlRouter   The url where the service can be accessed
     * @param documentation The Documentation of the service
     * @param methods The public methods allowed for this service
     * @return a String containing the Wsdl document.
     */
    public static String getWSDL(Class<?> c, String serviceName,
        String urlRouter, String documentation, String[] methods) {
        String namespace = serviceName;

        try {
            Emitter emitter = new Emitter();

            emitter.setDisallowedMethods(disallowedMethods);

            if (methods != null) {
                Vector<String> allowedMethods = new Vector<String>(methods.length);
                for (int i = 0; i < methods.length; i++) {
                    allowedMethods.addElement(methods[i]);
                }
                emitter.setAllowedMethods(allowedMethods);
            }

            //            TypeMappingRegistryImpl tmr = new TypeMappin gRegistryImpl();
            //            emitter.setTypeMappingRegistry(tmr);
            emitter.setLocationUrl(urlRouter);
            emitter.setIntfNamespace(namespace);
            emitter.setImplNamespace(namespace);
            emitter.setCls(c);
            emitter.setServiceElementName(serviceName);

            String wsdl = emitter.emitToString(Emitter.MODE_ALL);
            System.out.println(wsdl);
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
