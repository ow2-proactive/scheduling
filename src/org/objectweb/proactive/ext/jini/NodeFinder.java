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
package org.objectweb.proactive.ext.jini;

import net.jini.core.lookup.ServiceRegistrar;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;

public class NodeFinder implements DiscoveryListener {

  public static java.util.Vector remoteNodeList = new java.util.Vector();


  static void addInterface(RemoteNodeJiniInterface i) {
    System.out.println("NodeFinder: addInterface() Now adding node");
    synchronized (remoteNodeList) {
      remoteNodeList.addElement(i);
    }
    System.out.println("NodeFinder: addInterface() Node added");

  }


  static java.util.Vector getInterfaces() {
    return (remoteNodeList);
  }

  //    static public void main(String argv[])
  //     {
  // 	new NodeFinder();
  // 	try {
  // 	    Thread.currentThread().sleep(5000);
  // 	} catch (Exception e) {}

  //     }


  public NodeFinder() {
    System.out.println("Construction node finder");
    LookupDiscovery discover = null;
    try {
      discover = new LookupDiscovery(null); //start a multicast call
    } catch (Exception e) {
      e.printStackTrace();
    }
    discover.addDiscoveryListener(this);

  }


  public void discovered(DiscoveryEvent evt) {
    ServiceRegistrar[] registrars = evt.getRegistrars();
    for (int n = 0; n < registrars.length; n++) {
      ServiceRegistrar registrar = registrars[n];
      System.out.println("On a trouve un DiscoveryEvent " + evt); 
      //a LookupService has been discovered, now we do what we have to do
      new NodeLookupThread(registrar).start();
    }
  }


  public void discarded(DiscoveryEvent evt) {
  }
  
  
  
  //
  // -- INNER CLASSES -----------------------------------------------
  //
  
  
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
        net.jini.core.lookup.ServiceTemplate template = new net.jini.core.lookup.ServiceTemplate(null, serveurClasse, null);
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

    }
  } // end inner class NodeLookupThread
  
}
