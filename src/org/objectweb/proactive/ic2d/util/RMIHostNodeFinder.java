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

import org.objectweb.proactive.core.node.rmi.RemoteNode;
import org.objectweb.proactive.core.node.rmi.RemoteNodeAdapter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * This class talks to ProActive nodes
 */
public class RMIHostNodeFinder implements HostNodeFinder {

  private static final int DEFAULT_RMI_PORT = 1099;
  private IC2DMessageLogger logger;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public RMIHostNodeFinder(IC2DMessageLogger logger) {
    this.logger = logger;
  }
  
  public RMIHostNodeFinder() {
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  //
  // -- implements HostNodeFinder -----------------------------------------------
  //

  public Node[] findNodes(String host) throws java.io.IOException {
    // Try to determine the hostname
    log("RMIHostNodeFinder findNodes for " + host);
    String hostname = host;
    int port = DEFAULT_RMI_PORT;
    int pos = host.lastIndexOf(":");
    if (pos != -1) {
      // if the hostname is host:port
      try {
        port = Integer.parseInt(host.substring(1 + pos));
      } catch (NumberFormatException e) {
        port = DEFAULT_RMI_PORT;
      }
      hostname = host.substring(0, pos);
    }
    log("Trying " + hostname + ":" + port);

    // Hook the registry
    return findNodes(LocateRegistry.getRegistry(hostname, port));
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private Node[] findNodes(Registry registry) throws java.io.IOException {
    // enumarate through the rmi binding on the registry
    log("Listing bindings for " + registry);
    String[] list = registry.list();
    if (list.length == 0) {
      return new Node[0];
    }
    java.util.ArrayList nodes = new java.util.ArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      Object obj;
      try {
        obj = registry.lookup(list[i]);
      } catch (java.rmi.NotBoundException e) {
        //ignore that item;
        log("  registry.lookup of "+list[i]+" throwed ex=" + e);
        continue;
      }
      // If we've found a node..
      if (obj instanceof org.objectweb.proactive.core.node.rmi.RemoteNode) {
        log("  -> Found remote node " + list[i]);
        //Node realNode = NodeFactory.getNode(fullName);
        try {
          nodes.add(new RemoteNodeAdapter((RemoteNode)obj));
        } catch (NodeException e) {
          log("Error while trying to create a RemoteNodeAdapter for "+list[i]);
        }
      }
    }
    Node[] nodeArray = new Node[nodes.size()];
    if (nodes.size() > 0) {
      nodeArray = (Node[]) nodes.toArray(nodeArray);
    }
    return nodeArray;
  }
  
  
  private void log(String s) {
    if (logger != null) {
      logger.log(s);
    } else {
      System.out.println(s);
    }
  }
  
  private void log(String s, Exception e) {
    if (logger != null) {
      logger.log(s, e);
    } else {
      System.out.println(s);
      e.printStackTrace();
    }
  }
}
