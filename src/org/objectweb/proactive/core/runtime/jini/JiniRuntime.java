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
package org.objectweb.proactive.core.runtime.jini;

import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 * 	 @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */

public interface JiniRuntime extends java.rmi.Remote
{
  
  public String createLocalNode(String nodeName,boolean replacePreviousBinding) throws java.rmi.RemoteException, NodeException;
  
  
  
  public void DeleteAllNodes() throws java.rmi.RemoteException;   
  
  
  
  public void killNode(String nodeName) throws java.rmi.RemoteException;
  
  
  
  //public void createLocalVM(JVMProcess jvmProcess) throws java.rmi.RemoteException,java.io.IOException;
  
  
  
  public void createVM(UniversalProcess remoteProcess) throws java.rmi.RemoteException,java.io.IOException;


 
  //public Node[] getLocalNodes() throws java.rmi.RemoteException;
  

 
  public String[] getLocalNodeNames() throws java.rmi.RemoteException;
  

  
  //public String getLocalNode(String nodeName) throws java.rmi.RemoteException;
  

  
  //public String getNode(String nodeName) throws java.rmi.RemoteException;
  

  
  public VMInformation getVMInformation() throws java.rmi.RemoteException;
  
  
  
  public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName, String creatorID, String creationProtocol) throws java.rmi.RemoteException;
  
  
  
  public ProActiveRuntime[] getProActiveRuntimes() throws java.rmi.RemoteException;
  
  
  
  public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)throws java.rmi.RemoteException;
  
  
 
  public void killRT() throws java.rmi.RemoteException;
  
  
  
 	public String getURL() throws java.rmi.RemoteException;
 	
 	
 	public ArrayList getActiveObjects(String nodeName) throws java.rmi.RemoteException;
 	
 	
 	public ArrayList getActiveObjects(String nodeName, String objectName) throws java.rmi.RemoteException;
 	
  
  public VirtualNode getVirtualNode(String virtualNodeName)throws java.rmi.RemoteException;
  
  
  public void registerVirtualNode(String virtualNodeName,boolean replacePreviousBinding)throws java.rmi.RemoteException ;
  
  
  public void unregisterVirtualNode(String virtualNodeName)throws java.rmi.RemoteException;
  
  
  public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall,boolean isNodeLocal) throws java.rmi.RemoteException,
              ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException;


 
  public UniversalBody receiveBody(String nodeName, Body body) throws java.rmi.RemoteException;
}
