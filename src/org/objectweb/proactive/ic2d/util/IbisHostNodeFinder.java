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

import ibis.rmi.NotBoundException;
import ibis.rmi.registry.LocateRegistry;
import ibis.rmi.registry.Registry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntimeAdapter;
import org.objectweb.proactive.core.util.IbisProperties;

/**
 * This class talks to ProActive nodes
 */
public class IbisHostNodeFinder implements HostNodeFinder {

static Logger log4jlogger = Logger.getLogger(IbisHostNodeFinder.class.getName());

static {
	IbisProperties.load();
}

  private static final int DEFAULT_RMI_PORT = 1099;
  private IC2DMessageLogger logger;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public IbisHostNodeFinder(IC2DMessageLogger logger) {
    this.logger = logger;
  }
  
  public IbisHostNodeFinder() {
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
    Registry registry = LocateRegistry.getRegistry(hostname, port);
    return findNodes(registry,host);
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private Node[] findNodes(Registry registry, String host) throws java.io.IOException {
    // enumarate through the rmi binding on the registry
    log("Listing bindings for " + registry);
    String[] list = registry.list();
    if(log4jlogger.isDebugEnabled()){
    log4jlogger.debug("list " + list.length);
    }
    if (list.length == 0) {
      return new Node[0];
    }
    java.util.ArrayList nodes = new java.util.ArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      Object obj;
      //-----------------added lines-------------------------------
      if(list[i].indexOf("PA_RT")== -1 && list[i].indexOf("SpyListenerNode") == -1 && list[i].indexOf("_VN")== -1){
      //-----------------added lines---------------------------- 	
      try {
        obj = registry.lookup(list[i]);
      } catch (NotBoundException e) {
        //ignore that item;
        log("  registry.lookup of "+list[i]+" throwed ex=" + e);
        continue;
      }
      // If we've found a node..
      //if (obj instanceof org.objectweb.proactive.core.node.rmi.RemoteNode) {
      //---------------added lines ------------------------
      if (obj instanceof org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime) {
      //--------------added lines --------------------------
        log("  -> Found remote node " + list[i]);
        //Node realNode = NodeFactory.getNode(fullName);
        try {
          nodes.add(new NodeImpl(new RemoteProActiveRuntimeAdapter((RemoteProActiveRuntime)obj),"//"+host+"/"+list[i],"rmi"));
        } catch (ProActiveException e) {
          log("Error while trying to create a RuntimeAdapter for "+list[i]+", check the version of ProActive or jdk");
        }
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
      log4jlogger.info(s);
    }
  }
  
  private void log(String s, Exception e) {
    if (logger != null) {
      logger.log(s, e);
    } else {
      log4jlogger.info(s);
      e.printStackTrace();
    }
  }
}
