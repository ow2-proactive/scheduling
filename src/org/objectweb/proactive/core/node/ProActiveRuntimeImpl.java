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
package org.objectweb.proactive.core.node;

import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;

/**
 * <p>
 * Implementation of  ProActiveRuntime
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.91
 *
 */
public class ProActiveRuntimeImpl implements ProActiveRuntime {

  //
  // -- STATIC MEMBERS -----------------------------------------------------------
  //

  //the Unique instance of ProActiveRuntime
  private static ProActiveRuntime proactiveRuntime = new ProActiveRuntimeImpl();


  //
  // -- PRIVATE MEMBERS -----------------------------------------------------------
  //

  private VMInformation vmInformation;
  
  // map nodes and their names
  private java.util.HashMap nodeMap;
  

  //
  // -- CONSTRUCTORS -----------------------------------------------------------
  //
  
  // singleton
  private ProActiveRuntimeImpl() {
    try {
      vmInformation = new VMInformationImpl();
    } catch (java.net.UnknownHostException e) {
      System.out.println();
      System.out.println(" !!! Cannot do a reverse lookup on that host");
      System.out.println();
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------------------
  //
  
  public static ProActiveRuntime getProActiveRuntime() {
    return proactiveRuntime;
  }
  

  //
  // -- Implements ProActiveRuntime  -----------------------------------------------
  //

  public Node createLocalNode(String nodeName) throws NodeException {
    return null;
  }
  

  public ProActiveRuntime createLocalVM(JVMProcess jvmProcess) throws java.io.IOException {
    return null;
  }
  

  public ProActiveRuntime createRemoteVM(UniversalProcess remoteProcess) throws java.io.IOException {
    return null;
  }


  public Node[] getLocalNodes() {
    return null;
  }
  

  public String[] getLocalNodeNames() {
    return null;
  }
  

  public Node getLocalNode(String nodeName) {
    return null;
  }
  

  public Node getNode(String nodeURL) {
    return null;
  }
  

  public VMInformation getVMInformation() {
    return vmInformation;
  }


  public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall) throws NodeException,
              ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException {              
    return null;
  }


  public UniversalBody receiveBody(String nodeName, Body body) throws NodeException {
    return null;
  }



  //
  // -- PRIVATE METHODS  -----------------------------------------------
  //



  //
  // -- INNER CLASSES  -----------------------------------------------
  //

  protected static class VMInformationImpl implements VMInformation, java.io.Serializable {

    private java.net.InetAddress hostInetAddress;
    //the Unique ID of the JVM
    private java.rmi.dgc.VMID uniqueVMID;
    
    private String name;

    public VMInformationImpl() throws java.net.UnknownHostException {
      this.uniqueVMID = UniqueID.getCurrentVMID();
      hostInetAddress = java.net.InetAddress.getLocalHost();
      String hostname = hostInetAddress.getHostName();
      this.name = "PA_RT"+Integer.toString(new java.util.Random(System.currentTimeMillis()).nextInt())+"_"+hostname;
    }
        
    //
    // -- PUBLIC METHODS  -----------------------------------------------
    //

    //
    // -- implements VMInformation  -----------------------------------------------
    //

    public java.rmi.dgc.VMID getVMID() {
      return uniqueVMID;
    }

    public String getName() {
      return name;
    }

    public java.net.InetAddress getInetAddress() {
      return hostInetAddress;
    }
  
  }


}
