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
package org.objectweb.proactive.jini;

import org.objectweb.proactive.core.node.rmi.RemoteNodeImpl;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;

import java.rmi.RemoteException;

public class NodeJini extends RemoteNodeImpl {

  /**
   * Creates a Node using Jini
   */
  public NodeJini(String url) throws RemoteException {
    super();
    url = checkURL(url);
    try {
      nodeInformation = new NodeInformationImpl(url);
    } catch (java.net.UnknownHostException e) {
      throw new RemoteException("Host unknown in "+url, e);
    }
    LookupLocator lookup = null;
    ServiceRegistrar registrar = null;
    ServiceRegistration registration = null;
    try {
      lookup = new LookupLocator("jini://" + url);
      registrar = lookup.getRegistrar();
    } catch (Exception e) {
      throw new RemoteException("Cannot creator the lookup locator or lookup the registrar",e);
    }
    try {
      ServiceItem item = new ServiceItem(null, this, null);
      registration = registrar.register(item, 100);
      System.out.println("Just registered with a lease " + registration.getLease().getExpiration());
    } catch (Exception e) {
      throw new RemoteException("Cannot register",e);
    }
  }


  public static void main(String[] args) {
    String url;
    NodeJini myNode;
    // Try to guess localhost
    // First, let's find local host name
    if (args.length > 0)
      url = args[0];
    else url = "";
    System.out.println("The URL is " + url);
    try {
      //we use a join manager to renew leases
      JoinManager joinMn = new JoinManager(new NodeJini(url), null, (ServiceID) null, null, new LeaseRenewalManager());
      myNode = new NodeJini(url);
    } catch (Exception e) {
      System.err.println("Cannot instantiate Node - Fatal Error" + e.getMessage());
      e.printStackTrace();
    }
  }


}

