package org.objectweb.proactive.core.runtime.rmi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;

/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 * 	 @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */

public class RemoteProActiveRuntimeImpl
	extends UnicastRemoteObject
	implements RemoteProActiveRuntime
{


	protected transient ProActiveRuntimeImpl proActiveRuntime;
	protected String proActiveRuntimeURL;
	
	
	 
  //	
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  
	public RemoteProActiveRuntimeImpl() throws java.rmi.RemoteException,java.rmi.AlreadyBoundException{
		//System.out.println("toto");
		this.proActiveRuntime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
		//this.urlBuilder = new UrlBuilder();
		try{
		this.proActiveRuntimeURL = buildRuntimeURL();
		java.rmi.Naming.bind(proActiveRuntimeURL,this);
		//System.out.println ("ProActiveRuntime successfully bound in registry at "+proActiveRuntimeURL);
		}catch(java.net.MalformedURLException e){
			throw new java.rmi.RemoteException("Cannot bind in registry at "+proActiveRuntimeURL, e);
		}
	}
	
	
		
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
	

	public String createLocalNode(String nodeName,boolean replacePreviousBinding) throws java.rmi.RemoteException, NodeException 
	{
		String nodeURL = null;
		//Node node;
		try
		{
			//first we build a well-formed url
			nodeURL = buildNodeURL(nodeName);
			//then take the name of the node
			String name = UrlBuilder.getNameFromUrl(nodeURL);
			//System.out.println("name is : "+ name);
			//System.out.println("url is : "+ nodeURL);
			//register it with the url
			if (replacePreviousBinding) java.rmi.Naming.rebind(nodeURL,this);
			else java.rmi.Naming.bind(nodeURL,this);
			//create the node with the name 
			proActiveRuntime.createLocalNode(name,replacePreviousBinding);
			
			System.out.println ("Node "+ nodeURL+" successfully bound in registry at "+nodeURL);
		}catch (java.rmi.AlreadyBoundException e){
			throw new java.rmi.RemoteException("Node "+nodeURL+" already bound in registry",e);
		}catch (java.net.MalformedURLException e){
			throw new java.rmi.RemoteException("cannot bind in registry at "+nodeURL,e);
		}catch (java.net.UnknownHostException e){
			throw new java.rmi.RemoteException("Host unknown in "+nodeURL,e);
		}
		
		return nodeURL;
		
	}


	public void DeleteAllNodes()
	{
		proActiveRuntime.DeleteAllNodes();
	}

	
	public void killNode(String nodeName)
	{
		proActiveRuntime.killNode(nodeName);
	}

	
//	public void createLocalVM(JVMProcess jvmProcess)
//		throws IOException
//	{
//	proActiveRuntime.createLocalVM(jvmProcess);
//	}

	
	public void createVM(UniversalProcess remoteProcess)
		throws IOException
	{
	 proActiveRuntime.createVM(remoteProcess);
	}

	
//	public Node[] getLocalNodes()
//	{
//		return proActiveRuntime.getLocalNodes(); 
//	}

	
	public String[] getLocalNodeNames()
	{
		return proActiveRuntime.getLocalNodeNames();
	}

	
//	public String getLocalNode(String nodeName)
//	{
//		return proActiveRuntime.getLocalNode(nodeName);
//	}
//
//	
//	public String getNode(String nodeName)
//	{
//		return proActiveRuntime.getNode(nodeName);
//	}
	
	
//	public String getDefaultNodeName(){
//		return proActiveRuntime.getDefaultNodeName();
//	}

	
	public VMInformation getVMInformation()
	{
		return proActiveRuntime.getVMInformation();
	}

	
	public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName, String creatorID, String creationProtocol)
	{
		proActiveRuntime.register(proActiveRuntimeDist,proActiveRuntimeName,creatorID,creationProtocol);
	}

	
	public ProActiveRuntime[] getProActiveRuntimes()
	{
		return proActiveRuntime.getProActiveRuntimes();
	}
	
	public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName){
		return proActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
	}
		


	
	public void killRT()
	{
		proActiveRuntime.killRT();
	}
	
	public String getURL(){
  		return proActiveRuntimeURL;
  	}

	public ArrayList getActiveObjects(String nodeName){
		return proActiveRuntime.getActiveObjects(nodeName);
	}
	
	
	public ArrayList getActiveObjects(String nodeName, String objectName){
		return proActiveRuntime.getActiveObjects(nodeName,objectName);
	}
	
	public VirtualNode getVirtualNode(String virtualNodeName){
		return proActiveRuntime.getVirtualNode(virtualNodeName); 
	}
	
	
	public void registerVirtualNode(String virtualNodeName,boolean replacePreviousBinding)throws java.rmi.RemoteException {
		String virtualNodeURL = null;
		
		try
		{ 
			//first we build a well-formed url
			virtualNodeURL = buildNodeURL(virtualNodeName);
			//then take the name of the virtualnode
			//String name = UrlBuilder.getNameFromUrl(virtualNodeURL);
			//register it with the url
			if (replacePreviousBinding) java.rmi.Naming.rebind(virtualNodeURL,this);
			else java.rmi.Naming.bind(virtualNodeURL,this);
			System.out.println ("VirtualNode "+ virtualNodeURL+" successfully bound in registry at "+virtualNodeURL);
		}catch (java.rmi.AlreadyBoundException e){
			throw new java.rmi.RemoteException("VirtualNode "+virtualNodeURL+" already bound in registry",e);
		}catch (java.net.MalformedURLException e){
			throw new java.rmi.RemoteException("cannot bind in registry at "+virtualNodeURL,e);
		}catch (java.net.UnknownHostException e){
			throw new java.rmi.RemoteException("Host unknown in "+virtualNodeURL,e);
		}
		
		
	}
	
	public void unregisterVirtualNode(String virtualnodeName)throws java.rmi.RemoteException{
		String virtualNodeURL = null;
		proActiveRuntime.unregisterVirtualNode(UrlBuilder.removeVnSuffix(virtualnodeName));
		try{
			//first we build a well-formed url
			virtualNodeURL = buildNodeURL(virtualnodeName);
			java.rmi.Naming.unbind(virtualNodeURL);
		}catch(java.net.MalformedURLException e){
			throw new java.rmi.RemoteException("cannot unbind in registry at "+virtualNodeURL,e);
		}catch(java.rmi.NotBoundException e){
			throw new java.rmi.RemoteException(virtualNodeURL+ "is not bound in the registry",e);
		}catch (java.net.UnknownHostException e){
			throw new java.rmi.RemoteException("Host unknown in "+virtualNodeURL,e);
		}
	}
	
	
	public UniversalBody createBody(String nodeName,ConstructorCall bodyConstructorCall,boolean isNodeLocal)
		throws
			ConstructorCallExecutionFailedException,
			InvocationTargetException
	{
		return proActiveRuntime.createBody(nodeName,bodyConstructorCall,isNodeLocal);
	}

	
	public UniversalBody receiveBody(String nodeName, Body body) 
	{ 
		return proActiveRuntime.receiveBody(nodeName,body);
	}
	


  	
  	
  	
  //
  // ---PRIVATE METHODS--------------------------------------
  //
  
  private String buildRuntimeURL() {
  	int port = RemoteRuntimeFactory.getRegistryHelper().getRegistryPortNumber();
  	String host = getVMInformation().getInetAddress().getHostName();
  	String name = getVMInformation().getName();
  	return UrlBuilder.buildUrl(host,name,port);
  }
  
  
  private String buildNodeURL(String url) throws java.net.UnknownHostException{
  	int i = url.indexOf('/');
  	if (i == -1){
  		//it is an url given by a descriptor
  		String host = getVMInformation().getInetAddress().getHostName();
  		return UrlBuilder.buildUrl(host,url);
  	}else{
  		return UrlBuilder.checkUrl(url);
  	}
  }
	
	
   
  
}
