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
package org.objectweb.proactive.core.rmi;

public class RegistryHelper {

  protected final static int DEFAULT_REGISTRY_PORT = 1099;

 /**
  * settings of the registry
  */
  protected int registryPortNumber = DEFAULT_REGISTRY_PORT;
  protected boolean shouldCreateRegistry = true;
  protected boolean registryChecked;

  //
  // -- Constructors -----------------------------------------------
  //
  
  public RegistryHelper() {
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  public int getRegistryPortNumber() {
    return registryPortNumber;
  }

  public void setRegistryPortNumber(int v) {
    registryPortNumber = v;
    registryChecked = false;
  }
  
  public boolean shouldCreateRegistry() {
    return shouldCreateRegistry;
  }

  public void setShouldCreateRegistry(boolean v) {
    shouldCreateRegistry = v;
  }
  

  public synchronized void initializeRegistry() throws java.rmi.RemoteException {
    if (! shouldCreateRegistry) return; // don't bother
    if (registryChecked) return; // already done for this VM
    getOrCreateRegistry(registryPortNumber);
    registryChecked = true;
  }


  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  // 
  
  private static java.rmi.registry.Registry createRegistry(int port) throws java.rmi.RemoteException {
    return java.rmi.registry.LocateRegistry.createRegistry(port);
  }


  private static java.rmi.registry.Registry detectRegistry(int port) {
    java.rmi.registry.Registry registry = null;
    try {
      // whether an effective registry exists or not we should get a reference
      registry = java.rmi.registry.LocateRegistry.getRegistry(port);
      if (registry == null) return null;
      // doing a lookup should produce ConnectException if registry doesn't exist
      // and no exception or NotBoundException if the registry does exist.
      java.rmi.Remote r = registry.lookup("blah!");
      System.out.println("Detected an existing RMI Registry on port "+port);
      return registry;
    } catch (java.rmi.NotBoundException e) {
      System.out.println("Detected an existing RMI Registry on port "+port);
      return registry;
    } catch (java.rmi.RemoteException e) {
      return null;
    }
  }
  
  
  private static java.rmi.registry.Registry getOrCreateRegistry(int port) throws java.rmi.RemoteException {
    java.rmi.registry.Registry registry = detectRegistry(port);
    if (registry != null) return registry;
    // no registry created
    try {
      registry = createRegistry(port);
      System.out.println("Created a new registry on port "+port);
      return registry;
    } catch (java.rmi.RemoteException e) {
      // problem to bind the registry : may be somebody created one in the meantime
      // try to find the rmi registry one more time
      registry = detectRegistry(port);
      if (registry != null) return registry;
      System.out.println("Cannot detect an existing RMI Registry on port "+port+" nor create one e="+e);
      throw e;
    }
  }  


}
