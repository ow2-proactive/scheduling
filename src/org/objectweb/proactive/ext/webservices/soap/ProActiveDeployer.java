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
package org.objectweb.proactive.ext.webservices.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.apache.soap.SOAPException;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.ServiceManagerClient;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;
import org.objectweb.proactive.ext.webservices.wsdl.WSDLGenerator;


/**
 * @author vlegrand
 * This class is responsible to deploy an active object as a web service.
 * It serialize the stub/proxy into a string and send it to the rcprouter Servlet in order to register it on the tomcat server.
 * */

public class ProActiveDeployer {
    private static final String PROACTIVE_PROVIDER = "org.objectweb.proactive.ext.webservices.soap.ProActiveProvider";
    private static final String PROACTIVE_STUB = "Stub";
    private static final String WSDL_FILE = "Wsdl";
    private static final String ROUTER="/soap/servlet/rpcrouter";
    private static final String DOCUMENTATION = "ProActive Active Object";
    
    private static Hashtable deployedObjects = new Hashtable();

    /**
     *  Deploy an active object as a web service
     * @param urn The name of the web service
     * @param url The runtime URL where to contact the active object
     * @param o The active Object
     * @param methods The methods of the active object you  want to be accessible
     */
    
    public static void deploy(String urn, String url, Object o,  String [] methods) {

    	/* first we need to generate a WSDL description of the object we want to deploy */
    	String wsdl = WSDLGenerator.getWSDL(o, urn, url + ROUTER, DOCUMENTATION,methods );
    	
    	
        /*For deploying an active object we need a ServiceManagerClient that will contact the Serlvet */
        ServiceManagerClient serviceManagerClient = null;

        try {
            serviceManagerClient = new ServiceManagerClient(new URL(url + ROUTER));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /* The informations about services will be put in a deployment descriptor */
        DeploymentDescriptor dd = new DeploymentDescriptor();
        dd.setID(urn);
        dd.setProviderType(DeploymentDescriptor.PROVIDER_USER_DEFINED);
    
        dd.setServiceClass(PROACTIVE_PROVIDER);
        
        dd.setIsStatic(false);
        dd.setMethods(methods);
        dd.setProviderClass(o.getClass().getName());

        /* Here we put the serialized stub into a dd property */
        Hashtable props = new Hashtable();
        props.put(PROACTIVE_STUB,
            ProActiveXMLUtils.serializeObject(o));
		props.put(WSDL_FILE, wsdl);
        dd.setProps(props);

        try {
            serviceManagerClient.deploy(dd);
        } catch (SOAPException e1) {
            e1.printStackTrace();
        }
   
    }
    
 /**
  *  Undeploy a service on a web server
  * @param urn The name (urn) of the service	
  * @param url The web server url
  */
    public static void undeploy (String urn , String url) {
    	 ServiceManagerClient serviceManagerClient = null;

         try {
             serviceManagerClient = new ServiceManagerClient(new URL(url + ROUTER));
             serviceManagerClient.undeploy(urn);
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }catch (SOAPException e1) {
            e1.printStackTrace();
        }
    }
    
  
}
