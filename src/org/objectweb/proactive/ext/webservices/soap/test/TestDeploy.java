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
package org.objectweb.proactive.ext.webservices.soap.test;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.hello2.HelloServer;
import org.objectweb.proactive.ext.webservices.soap.ProActiveDeployer;


/**
 * @author vlegrand
 */
public class TestDeploy {
    public static void main(String[] args) {
        HelloServer  h;
		String host  = "noadcoco:8080";
		
		if (args.length != 0)
			host=args[0];
			
        try {
            h = (HelloServer) ProActive.newActive(HelloServer.class.getName(), null);
//			Class2WSDL wsdl = Class2WSDL.getInstance();
//				javax.wsdl.Definition def = wsdl.getWsdlDefinition(h,"helloPA");
//				

	String [] methods = {"sayHello"};
//	
//			Vector methodsV = new Vector ();
//			methodsV.add("sayHello");
			
//			String wsdlString = WSDLGenerator.getWSDL(h,"helloPA","http://amda:8080/soap/rpcrouter",
//						"un simple hello world",methodsV);
          
		  //Class2WSDL wsdl = Class2WSDL.getInstance();
		  //Definition def = wsdl.getWSDL(h);  
		 // String wsdlString = Class2WSDL.toString(def);
          // System.out.println("WSDL = " + wsdl );
           	 String urn = "helloPA";
            String url = "http://" + host ;
            System.out.println("Je deploie a l'url :" + url);
            ProActiveDeployer.deploy(urn, url, h,"",methods);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (org.objectweb.proactive.ActiveObjectCreationException e) {
           e.printStackTrace();
        }
    }
}
