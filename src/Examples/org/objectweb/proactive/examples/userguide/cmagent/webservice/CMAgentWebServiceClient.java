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
package org.objectweb.proactive.examples.userguide.cmagent.webservice;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;

import org.objectweb.proactive.extensions.webservices.WSConstants;


/**
 * @author vlegrand
 * An example to call the hello world web service
 */
public class CMAgentWebServiceClient {
    public static void main(String[] args) {
        String address;
        if (args.length == 0) {
            address = "http://localhost:8080";
        } else {
            address = args[0];
        }
        if (!address.startsWith("http://")) {
            address = "http://" + address;
        }

        address += WSConstants.ROUTER;
        String namespaceURI = "cmAgentService";
        String serviceName = "cmAgentService";
        String portName = "getLastRequestServeTime";

        ServiceFactory factory;
        try {
            factory = ServiceFactory.newInstance();

            Service service = factory.createService(new QName(serviceName));

            Call call = service.createCall(new QName(portName));

            call.setTargetEndpointAddress(address);

            call.setOperationName(new QName(namespaceURI, portName));

            Object[] inParams = new Object[0];
            String name = ((String) call.invoke(inParams));
            System.out.println(name);
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
