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

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Vector;

import javax.wsdl.WSDLException;

import javax.xml.parsers.ParserConfigurationException;


/**
 * @author vlegrand
 */
public class WSDLGenerator {

    /**
     * Generate the WSDL document associate with an  active object exposed as a web service
     * @param o The object that we want to obtain WSDL
     * @param serviceName The name of the service (urn)
     * @param urlRouter   The url where the service can be accessed
     * @param documentation The Documentation of the service
     * @param methods The public methods allowed for this service
     * @return a String containing the Wsdl document.
     */
    public static String getWSDL(Object o, String serviceName,
        String urlRouter, String documentation, String[] methods) {
        StringWriter sw = new StringWriter();
        String namespace = serviceName;
        try {
            Emitter emitter = new Emitter();

            Vector disallowedMethods = new Vector();

            disallowedMethods.add("equals");
            disallowedMethods.add("toString");
            disallowedMethods.add("runActivity");
            disallowedMethods.add("setProxy");
            disallowedMethods.add("getProxy");

            emitter.setDisallowedMethods(disallowedMethods);

            Vector allowedMethods = new Vector(methods.length);
            for (int i = 0; i < methods.length; i++) {
                allowedMethods.addElement(methods[i]);
            }

            emitter.setAllowedMethods(allowedMethods);
            emitter.setLocationUrl(urlRouter);
            //  emitter.setTargetService(serviceName);
            emitter.setIntfNamespace(namespace);
            emitter.setImplNamespace(namespace);
            HashMap namespaceMap = new HashMap();

            //namespaceMap.put(o.getClass().getPackage().getName(), serviceName);
            //emitter.setNamespaceMap(namespaceMap);
            emitter.setCls(o.getClass());
            emitter.setServiceElementName(serviceName);
            String wsdl = emitter.emitToString(Emitter.MODE_ALL);

            //Definition def = (com.ibm.wsdl.DefinitionImpl) emitter.getIntfWSDL();
            //           def.setTargetNamespace(namespace);
            //           def.setDocumentBaseURI(urlRouter);
            //       
            //           
            //            Service service = def.createService();
            //            emitter.setIntfNamespace(namespace);
            //            service.setQName(new QName(serviceName));
            //            def.addService(service);
            //            
            //            Iterator bindings = def.getBindings().values().iterator();
            //            while (bindings.hasNext()) {
            //                Binding binding = (Binding) bindings.next();
            //                List listOp = binding.getBindingOperations();
            //                for (int i=0; i<  listOp.size (); i ++) {
            //                	BindingOperation op  = (BindingOperation) listOp.get(i);
            //                  //  op.            	
            //                }
            //                
            //                SOAPAddress addr = new SOAPAddressImpl();
            //                addr.setLocationURI(urlRouter);
            //
            //                //Creation d'un port
            //                Port port = def.createPort();
            //                port.setName(serviceName);
            //                port.setBinding(binding);
            //                port.addExtensibilityElement(addr);
            //                service.addPort(port);
            //            }
            //            javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter()
            //                                          .writeWSDL(def, sw);
            //String result = sw.toString();
            //        System.out.println("wsdl =  " + wsdl);
            return wsdl;
        } catch (WSDLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
