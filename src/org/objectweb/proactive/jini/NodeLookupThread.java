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

import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

public class NodeLookupThread extends Thread {

  ServiceRegistrar registrar;


  NodeLookupThread(ServiceRegistrar registrar) {
    this.registrar = registrar;
  }
  

  public void run() {
    System.out.println("NodeLookupThread: now running");
    RemoteNodeJiniInterface remoteIn = null;
    try {
      Class[] serveurClasse = new Class[1];
      try {
        serveurClasse[0] = Class.forName("org.objectweb.proactive.jini.RemoteNodeJiniInterface");
      } catch (Exception e) {
        System.out.println("Exception in NodeLookupThread: " + e);
        e.printStackTrace();
      }
      ServiceTemplate template = new ServiceTemplate(null, serveurClasse, null);
      System.out.println("NodeLookupThread: recherche un noeud " + serveurClasse[0]);
      remoteIn = (RemoteNodeJiniInterface)registrar.lookup(template);
      if (remoteIn != null) {
        System.out.println("NodeLookupThread: On vient de trouver un noeud " + remoteIn);
        NodeFinder.addInterface(remoteIn);
      } else {
        System.out.println("NodeLookupThread: No node found");
      }
    } catch (Exception e) {
      System.out.println("Exception: " + e);
      e.printStackTrace();
    }

    // 	try {
    // 	    remoteIn.echo();


    // 	} catch (Exception e) {
    // 	    System.out.println("Exception: " + e);
    // 	    e.printStackTrace();
    // 	}
	
    // 	try {
    // 	    remoteIn.echo();
    // 	} catch (Exception e) {
    // 	    System.out.println("Exception: " + e);
    // 	    e.printStackTrace();
    // 	}
  }
}
