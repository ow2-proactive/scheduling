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
package org.objectweb.proactive.core.node.rmi;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.rmi.RegistryHelper;

public class RemoteNodeFactory extends NodeFactory {

  protected final static int MAX_RETRY = 5;

  protected java.util.Random random;
  protected static RegistryHelper registryHelper = new RegistryHelper();
  protected static ClassServerHelper classServerHelper = new ClassServerHelper();

  static {
    System.out.println ("RemoteNodeFactory created with "+RemoteNodeFactory.class.getClassLoader().getClass().getName());
  }

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public RemoteNodeFactory() throws java.io.IOException {
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new java.rmi.RMISecurityManager());
    }
    random = new java.util.Random(System.currentTimeMillis());
    System.out.println("------------------------ RemoteNodeFactory ------------------------");
    registryHelper.initializeRegistry();
    System.out.println ("ClassLoader is "+this.getClass().getClassLoader().getClass().getName());
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public static void setClassServerClasspath(String v) {
    classServerHelper.setClasspath(v);
  }

  public static void setShouldCreateClassServer(boolean v) {
    classServerHelper.setShouldCreateClassServer(v);
  }

  public static void setRegistryPortNumber(int v) {
    registryHelper.setRegistryPortNumber(v);
  }

  public static void setShouldCreateRegistry(boolean v) {
    registryHelper.setShouldCreateRegistry(v);
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected Node _createNode(String s, boolean replacePreviousBinding) throws NodeException {
    return createNodeAdapter(s, replacePreviousBinding);
  }


  protected Node _createDefaultNode(String baseName) throws NodeException {
    int i = 0;
    while (true) {
      try {
        return createNodeAdapter(baseName + Integer.toString(random.nextInt()), false);
      } catch (NodeException e) {
        i++;
        if (i >= MAX_RETRY) throw e;
      }
    }
  }


  protected Node _getNode(String s) throws NodeException {
    if (s == null) return null;
    try {
      RemoteNode remoteNode = (RemoteNode)java.rmi.Naming.lookup(s);
      return createNodeAdapter(remoteNode);
    } catch (java.rmi.RemoteException e) {
      throw new NodeException("Remote",e);
    } catch (java.rmi.NotBoundException e) {
      throw new NodeException("NotBound",e);
    } catch (java.net.MalformedURLException e) {
      throw new NodeException("Malformed URL:"+s,e);
    }
  }

  protected RemoteNodeAdapter createNodeAdapter(RemoteNode remoteNode) throws NodeException {
    return new RemoteNodeAdapter(remoteNode);
  }


  protected RemoteNodeAdapter createNodeAdapter(String remoteNodeName, boolean replacePreviousBinding) throws NodeException {
    return new RemoteNodeAdapter(remoteNodeName, replacePreviousBinding);
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

}
