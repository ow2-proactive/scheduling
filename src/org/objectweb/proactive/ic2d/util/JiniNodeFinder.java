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

//import org.objectweb.proactive.core.node.rmi.RemoteNode;
//import org.objectweb.proactive.core.node.rmi.RemoteNodeAdapter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
//import org.objectweb.proactive.core.node.jini.JiniNode;
/**
 * This class talks to ProActive nodes
 */
public class JiniNodeFinder implements AllNodeFinder {


   
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

 
  public JiniNodeFinder() {
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  //
  // -- implements NodeFinder -----------------------------------------------
  //

  public Node[] findNodes()  {    
    return findNodes(null);
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  public Node[] findNodes(String host)  {
    // enumarate through the rmi binding on the registry
    JiniNodeListener nodelist= new JiniNodeListener(host);
 // stay around long enough to receive replies
    try {
      Thread.sleep(10000L);
    } catch(java.lang.InterruptedException e) {
      // do nothing
    }
    java.util.ArrayList nodes = nodelist.getNodes();
    //System.out.println("JiniNodeFinder: on  recupere "+nodes.size()+" noeuds");
    Node[] nodeArray = new Node[nodes.size()];
    if (nodes.size() > 0) {
      //System.out.println("JiniNodeFinder: copie du tableau");
      for (int i=0; i<nodes.size();i++){
	Node node = (Node)  nodes.get(i);
	nodeArray[i] = node;
      }
    }
    return nodeArray;
  }
  
  
}


