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
package org.objectweb.proactive.ic2d.util;


import org.objectweb.proactive.ic2d.data.*;
import org.objectweb.proactive.core.node.Node;


public class CreateJiniNodeTask implements Runnable {
  private WorldObject worldObject;
  private String host;
  
  public CreateJiniNodeTask() {
  }
    
  public CreateJiniNodeTask(WorldObject worldObject) {
    this.worldObject = worldObject;
    this.host = null;
  }
    
  public CreateJiniNodeTask(WorldObject worldObject,String host) {
    this.worldObject = worldObject;
    this.host = host;
  }
  
  public void run() {
    System.out.println("CreateJininodeTask: lancement de la recherche");
    Node[] nodes;
    JiniNodeFinder finder = new JiniNodeFinder();
    nodes = finder.findNodes(host);
    System.out.println("fin de la recherche NB de noeud: "+nodes.length);
    for (int i=0 ; i<nodes.length; i++){
      Node node = nodes[i];
      String nodeName = node.getNodeInformation().getName();
      String hostname = node.getNodeInformation().getInetAddress().getHostName();
      HostObject hostObject = worldObject.getHostObject(hostname);
      //System.out.println("hostObject: "+hostObject.getHostName());
      if (hostObject == null) {
				System.out.println("CreateJiniNodeTask: creation d'un nouvel host");
				hostObject = new HostObject(worldObject, hostname);
				worldObject.addHostsObject(hostObject);
				worldObject.putChild(hostname, hostObject);
      }
      VMObject vmObject = hostObject.findVMObjectHavingExistingNode(nodeName);
      if (vmObject == null) {
	// new NodeObject
				System.out.println("CreateJiniNodeTask: creation d'une nouvelle VM ");
				hostObject.addVMObject(node);
      } else {
				System.out.println("CreateJiniNodeTask: creation d'un node dans une VM ");
				vmObject.addNodeObject(node);
				vmObject.sendEventsForAllActiveObjects();
      }
    
    }


  }
  
}
  
