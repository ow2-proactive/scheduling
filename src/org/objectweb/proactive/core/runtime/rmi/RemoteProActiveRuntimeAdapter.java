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
package org.objectweb.proactive.core.runtime.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;

/**
 *   An adapter for a RemoteProActiveRuntime to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another remote objects library.
 * 	 @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */

public class RemoteProActiveRuntimeAdapter
	implements ProActiveRuntime, Serializable
{
	
  
 
  //-----------Protected Members--------------------------------
  
	protected RemoteProActiveRuntime remoteProActiveRuntime;
	protected VMInformation vmInformation;
	protected String proActiveRuntimeURL;
	
	
  //
  // -- Constructors -----------------------------------------------
  //
  
  
  
  public RemoteProActiveRuntimeAdapter() throws ProActiveException{
 	try{
 		
		this.remoteProActiveRuntime = createRemoteProActiveRuntime();
		//System.out.println(remoteProActiveRuntime.getClass().getName());
 		this.vmInformation = remoteProActiveRuntime.getVMInformation();
 		this.proActiveRuntimeURL=remoteProActiveRuntime.getURL();
 	}catch(java.rmi.RemoteException e){
 		throw new ProActiveException("Cannot create the remoteProactiveRuntime or get the VMInformation from the RemoteProActiveRuntime",e);	
 	}
 	catch(java.rmi.AlreadyBoundException e){
 		throw new ProActiveException("Cannot bind remoteProactiveRuntime to"+proActiveRuntimeURL,e);
 	}
  }


  public RemoteProActiveRuntimeAdapter(RemoteProActiveRuntime remoteProActiveRuntime) throws ProActiveException{
  	this.remoteProActiveRuntime = remoteProActiveRuntime; 
  	try{
  		this.vmInformation = remoteProActiveRuntime.getVMInformation();
  		this.proActiveRuntimeURL = remoteProActiveRuntime.getURL();
  		
  	}catch(java.rmi.RemoteException re){
  		throw new ProActiveException(re);
  	}
  }
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public boolean equals(Object o) {
    if (! (o instanceof RemoteProActiveRuntimeAdapter)) return false;
    RemoteProActiveRuntimeAdapter runtime = (RemoteProActiveRuntimeAdapter)o;
    return remoteProActiveRuntime.equals(runtime.remoteProActiveRuntime);
  }

  public int hashCode() {
    return remoteProActiveRuntime.hashCode();
  }
  
  //
  // -- Implements ProActiveRuntime -----------------------------------------------
  //
	
	public String createLocalNode(String nodeName,boolean replacePreviousBinding) throws NodeException
	{
		try{
		return remoteProActiveRuntime.createLocalNode(nodeName,replacePreviousBinding);
		}catch(java.rmi.RemoteException e){
		throw new NodeException (e);
		}
	}

	
	public void DeleteAllNodes()
	{
		try{
		remoteProActiveRuntime.DeleteAllNodes();
		}catch(java.rmi.RemoteException re){
			re.printStackTrace();
			// behavior to be defined
		}
	}

	
	public void killNode(String nodeName)
	{
		try{
		remoteProActiveRuntime.killNode(nodeName);
		}catch(java.rmi.RemoteException re){
			re.printStackTrace();
			// behavior to be defined
		}
	}

	
//	public void createLocalVM(JVMProcess jvmProcess)
//		throws IOException,ProActiveException
//	{
//		try{
//		remoteProActiveRuntime.createLocalVM(jvmProcess);
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//		}
//	}

	
	public void createVM(UniversalProcess remoteProcess)
		throws IOException,ProActiveException
	{
		try{
		remoteProActiveRuntime.createVM(remoteProcess);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}

	
//	public Node[] getLocalNodes() throws ProActiveException
//	{
//		try{
//		return remoteProActiveRuntime.getLocalNodes();
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//			// behavior to be defined
//		}
//	}

	
	public String[] getLocalNodeNames() throws ProActiveException
	{
		try{
		return remoteProActiveRuntime.getLocalNodeNames();
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
			// behavior to be defined
		}
	}

	
//	public String getLocalNode(String nodeName) throws ProActiveException
//	{
//		try{
//		return remoteProActiveRuntime.getLocalNode(nodeName);
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//			// behavior to be defined
//		}
//	}
//
//	
//	public String getNode(String nodeName) throws ProActiveException
//	{
//		try{
//		return remoteProActiveRuntime.getNode(nodeName);
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//			// behavior to be defined
//		}
//	}
	
//	public String getDefaultNodeName() throws ProActiveException{
//		try{
//		return remoteProActiveRuntime.getDefaultNodeName();
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//			// behavior to be defined
//		}
//	}

	
	public VMInformation getVMInformation()
	{
		return vmInformation;
	}

	
	public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName, String creatorID, String creationProtocol)
	{
		try{
			//System.out.println("register in adapter"+remoteProActiveRuntime.getURL());
		remoteProActiveRuntime.register(proActiveRuntimeDist,proActiveRuntimeName,creatorID,creationProtocol);
		}catch(java.rmi.RemoteException re){
			re.printStackTrace();
			// behavior to be defined
		}
	}

	
	public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException
	{
		try{
		return remoteProActiveRuntime.getProActiveRuntimes();
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
			// behavior to be defined
		}
	}
	
	public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException
	{
		try{
		return remoteProActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
			// behavior to be defined
		}
	}

	
	public void killRT() 
	{
		try{
		remoteProActiveRuntime.killRT();
		}catch(java.rmi.RemoteException re){
			re.printStackTrace();
			// behavior to be defined
		}
	}
	
	public String getURL()throws ProActiveException{
		return proActiveRuntimeURL;
//		try{
//		return remoteProActiveRuntime.getURL();
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//			// behavior to be defined
//		}
	}
	
	public ArrayList getActiveObjects(String nodeName) throws ProActiveException{
		try{
			return remoteProActiveRuntime.getActiveObjects(nodeName);
		}
		catch (java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}
	
	
	public ArrayList getActiveObject(String nodeName, String objectName) throws ProActiveException{
		try{
			return remoteProActiveRuntime.getActiveObject(nodeName,objectName);
		}catch (java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}

	
	public UniversalBody createBody(String nodeName,ConstructorCall bodyConstructorCall,boolean isNodeLocal)
		throws
			ProActiveException,
			ConstructorCallExecutionFailedException,
			InvocationTargetException
	{
		try{
		return remoteProActiveRuntime.createBody(nodeName,bodyConstructorCall, isNodeLocal);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}

	
	public UniversalBody receiveBody(String nodeName, Body body)
		throws ProActiveException
	{
		try{
		return remoteProActiveRuntime.receiveBody(nodeName,body);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}
	
	
  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
	
	
	protected RemoteProActiveRuntime createRemoteProActiveRuntime()throws java.rmi.RemoteException,java.rmi.AlreadyBoundException
	{
		return new RemoteProActiveRuntimeImpl();
	}
	
  
   
}
