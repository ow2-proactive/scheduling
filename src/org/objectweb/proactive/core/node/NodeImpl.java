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

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


public class NodeImpl implements Node, Serializable
{

	protected NodeInformation nodeInformation;
	protected ProActiveRuntime proActiveRuntime;
	//
	// ----------Constructors--------------------
	//
	public NodeImpl(){}
	
	public NodeImpl(ProActiveRuntime proActiveRuntime, String nodeURL, String protocol){
		this.proActiveRuntime = proActiveRuntime;
		this.nodeInformation = new NodeInformationImpl(nodeURL,protocol);
	}
	
	//
	//--------------------------Implements Node-----------------------------
	
	/**
	 * @see org.objectweb.proactive.core.node.Node#getNodeInformation()
	 */
	public NodeInformation getNodeInformation()
	{
		return nodeInformation;
	}
	
	/**
	 * @see org.objectweb.proactive.core.node.Node#getProActiveRuntime
	 */
	public ProActiveRuntime getProActiveRuntime(){
		return proActiveRuntime;
	}

	/**
	 * @see org.objectweb.proactive.core.node.Node#getActiveObjects()
	 */
	public Object[] getActiveObjects() throws NodeException,ActiveObjectCreationException
	{	
		ArrayList bodyArray;
		try{
	  bodyArray = this.proActiveRuntime.getActiveObjects(this.nodeInformation.getName());
		}catch(ProActiveException e){
			throw new NodeException("Cannot get Active Objects registered on this node: "+this.nodeInformation.getURL(),e);
		}
		if(bodyArray.size() == 0){
			throw new NodeException("no ActiveObjects are registered for this node: "+ this.nodeInformation.getURL());
		}else{
			Object[] stubOnAO = new Object[bodyArray.size()];
			for (int i = 0; i < bodyArray.size(); i++)
			{
				UniversalBody body = (UniversalBody)((ArrayList)bodyArray.get(i)).get(0);
				String className = (String)((ArrayList)bodyArray.get(i)).get(1);
				try{
				stubOnAO[i] = createStubObject(className,body);
				}catch(MOPException e){
					throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy",e);
				}
			}
			return stubOnAO;
		}
		
	}


	/**
	 * @see org.objectweb.proactive.core.node.Node#getActiveObjects(String)
	 */
	public Object[] getActiveObjects(String className) throws NodeException,ActiveObjectCreationException
	{
		ArrayList bodyArray;
		try{
			bodyArray = this.proActiveRuntime.getActiveObjects(this.nodeInformation.getName(),className);
		}catch(ProActiveException e){
			throw new NodeException("Cannot get Active Objects of type "+className+" registered on this node: "+this.nodeInformation.getURL(),e);
		}
		if(bodyArray.size() == 0){
			throw new NodeException("no ActiveObjects of type "+className+" are registered for this node: "+ this.nodeInformation.getURL());
		}else{
			Object[] stubOnAO = new Object[bodyArray.size()];
			for (int i = 0; i < bodyArray.size(); i++)
			{
				UniversalBody body = (UniversalBody)bodyArray.get(i);
				try{
				stubOnAO[i] = createStubObject(className,body);
				}catch(MOPException e){
					throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy",e);
				}
			}
			return stubOnAO;
		}
		
	}


	private void readObject(ObjectInputStream in)throws java.io.IOException, ClassNotFoundException, ProActiveException{
		in.defaultReadObject();
		if(NodeFactory.isNodeLocal(this)){
			this.proActiveRuntime = RuntimeFactory.getProtocolSpecificRuntime(nodeInformation.getProtocol());	
		}

	}
	
	// -------------------------------------------------------------------------------------------
  // 
  // STUB CREATION
  // 
  // -------------------------------------------------------------------------------------------
  
  
  private static Object createStubObject(String className, UniversalBody body) throws MOPException {
    return createStubObject(className, null, new Object[] { body });
  }
  
  
  private static Object createStubObject(String className, Object[] constructorParameters, Object[] proxyParameters)
    throws MOPException {
    try {
      return MOP.newInstance(
        className,
        constructorParameters,
        Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
        proxyParameters);
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Class can't be found e=" + e);
    }
  }
  
  
	
	//
	//------------------------INNER CLASS---------------------------------------
	//
	
	protected class NodeInformationImpl implements NodeInformation{
		private String nodeName;
    private String nodeURL;
    private String protocol;
    private java.net.InetAddress hostInetAddress;
    private java.rmi.dgc.VMID hostVMID;
    
    public NodeInformationImpl(String url,String protocol){
    	this.nodeURL = url;
    	this.hostVMID = proActiveRuntime.getVMInformation().getVMID();
    	this.hostInetAddress = proActiveRuntime.getVMInformation().getInetAddress();
    	this.protocol = protocol;
    	this.nodeName = extractNameFromUrl(url);
    }
	  
	  /**
	   * @see org.objectweb.proactive.core.runtime.VMInformation#getVMID
	   */
	  public java.rmi.dgc.VMID getVMID() {
      return hostVMID;
    }
	
	
		/**
	   * @see org.objectweb.proactive.core.node.NodeInformation#getName()
	   */
    public String getName() {
      return nodeName;
    }


		/**
	   * @see org.objectweb.proactive.core.node.NodeInformation#getProtocol()
	   */
    public String getProtocol() {
      return protocol;
    }


		/**
	   * @see org.objectweb.proactive.core.node.NodeInformation#getURL()
	   */
    public String getURL() {
      return nodeURL;
    }


		/**
	   * @see org.objectweb.proactive.core.runtime.VMInformation#getInetAddress()
	   */
    public java.net.InetAddress getInetAddress() {
      return hostInetAddress;
    }
    
		/**
		 * Returns the name specified in the url
		 * @param url. The url of the node
		 * @return String. The name of the node
		 */
    private String extractNameFromUrl(String url){
    	int n = url.lastIndexOf("/");
    	String name = url.substring(n+1);
    	return name;
    } 
    
	}
}
