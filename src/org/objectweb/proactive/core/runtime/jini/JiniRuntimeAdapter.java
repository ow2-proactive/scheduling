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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;

/**
 *   An adapter for a JiniRuntime to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another remote objects library.
 * 	 @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */

public class JiniRuntimeAdapter implements ProActiveRuntime, java.io.Serializable {

  protected JiniRuntime jiniRuntime;
  protected VMInformation vmInformation;
  protected String proActiveRuntimeURL;

  //
  // -- Constructors -----------------------------------------------
  //

  protected JiniRuntimeAdapter() throws ProActiveException{
  	try{
  	this.jiniRuntime = createJiniRuntime();
  	this.vmInformation = jiniRuntime.getVMInformation();
  	this.proActiveRuntimeURL = jiniRuntime.getURL();
  	}catch(java.rmi.RemoteException e) {
      throw new ProActiveException("Cannot create the jiniRuntime or get the VMInformation from the JiniRuntime",e);
    }
  }



  public JiniRuntimeAdapter(JiniRuntime r) throws ProActiveException {
    this.jiniRuntime = r;
    try {
      this.vmInformation = jiniRuntime.getVMInformation();
      this.proActiveRuntimeURL = jiniRuntime.getURL();
    } catch (java.rmi.RemoteException e) {
      throw new ProActiveException("Cannot get the NodeInformation of the node", e);
    }
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public boolean equals(Object o) {
    if (! (o instanceof JiniRuntimeAdapter)) return false;
    JiniRuntimeAdapter runtime = (JiniRuntimeAdapter)o;
    return jiniRuntime.equals(runtime.jiniRuntime);
  }

  public int hashCode() {
    return jiniRuntime.hashCode();
  }



  //
  // -- Implements ProActiveRuntime -----------------------------------------------
  //
  
  public String createLocalNode(String nodeName,boolean replacePreviousBinding) throws NodeException
	{
		try{
		return jiniRuntime.createLocalNode(nodeName,replacePreviousBinding);
		}catch(java.rmi.RemoteException e){
		throw new NodeException (e);
		}
	}

	
	public void killAllNodes() throws ProActiveException
	{
		try{
		jiniRuntime.killAllNodes();
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}

	
	public void killNode(String nodeName) throws ProActiveException
	{
		try{
		jiniRuntime.killNode(nodeName);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}

	
//	public void createLocalVM(JVMProcess jvmProcess)
//		throws IOException,ProActiveException
//	{
//		try{
//		jiniRuntime.createLocalVM(jvmProcess);
//		}catch(java.rmi.RemoteException re){
//			throw new ProActiveException(re);
//		}
//	}

	
	public void createVM(UniversalProcess remoteProcess)
		throws IOException,ProActiveException
	{
		try{
		jiniRuntime.createVM(remoteProcess);
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
		return jiniRuntime.getLocalNodeNames();
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
			// behavior to be defined
		}
	}

	
//	public String getLocalNode(String nodeName) throws ProActiveException
//	{
//		try{
//		return jiniRuntime.getLocalNode(nodeName);
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
//		return jiniRuntime.getNode(nodeName);
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
		jiniRuntime.register(proActiveRuntimeDist,proActiveRuntimeName,creatorID,creationProtocol);
		}catch(java.rmi.RemoteException re){
			re.printStackTrace();
			// behavior to be defined
		}
	}

	
	public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException
	{
		try{
		return jiniRuntime.getProActiveRuntimes();
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
			// behavior to be defined
		}
	}
	
	public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException
	{
		try{
		return jiniRuntime.getProActiveRuntime(proActiveRuntimeName);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
			// behavior to be defined
		}
	}

	
	public void killRT() throws Exception
	{
		try{
		jiniRuntime.killRT();
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
//			re.printStackTrace();
//			// behavior to be defined
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
			return jiniRuntime.getActiveObjects(nodeName);
		}
		catch (java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}
	
	
	public ArrayList getActiveObjects(String nodeName, String objectName) throws ProActiveException{
		try{
			return jiniRuntime.getActiveObjects(nodeName,objectName);
		}catch (java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}


	public VirtualNode getVirtualNode(String virtualNodeName)throws ProActiveException{
		try{
		return jiniRuntime.getVirtualNode(virtualNodeName);
		}catch (java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}
	
	
	public void registerVirtualNode(String virtualNodeName,boolean replacePreviousBinding)throws ProActiveException {
		try{
		jiniRuntime.registerVirtualNode(virtualNodeName,replacePreviousBinding);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}
	
	
	public void unregisterVirtualNode(String virtualNodeName) throws ProActiveException{
		try{
		jiniRuntime.unregisterVirtualNode(virtualNodeName);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}
	
	
	public void unregisterAllVirtualNodes() throws ProActiveException {
		try{
		jiniRuntime.unregisterAllVirtualNodes();
		}catch(java.rmi.RemoteException re){
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
		return jiniRuntime.createBody(nodeName,bodyConstructorCall, isNodeLocal);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}

	
	public UniversalBody receiveBody(String nodeName, Body body)
		throws ProActiveException
	{
		try{
		return jiniRuntime.receiveBody(nodeName,body);
		}catch(java.rmi.RemoteException re){
			throw new ProActiveException(re);
		}
	}


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected JiniRuntime createJiniRuntime() throws java.rmi.RemoteException {
    return new JiniRuntimeImpl();
  }




}
