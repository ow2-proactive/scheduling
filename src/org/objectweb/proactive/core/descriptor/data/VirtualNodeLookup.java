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
package org.objectweb.proactive.core.descriptor.data;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.UrlBuilder;

/**
 * A <code>VirtualNode</code> represents a conceptual entity. After activation
 * a <code>VirtualNode</code> represents one or several nodes.
 * 
 * This class represents a remote VirtualNode resulting from a lookup in some registry such as RMIRegistry 
 * or JINI Lookup  service.
 * Objects of this class will be created when in an XML descriptor a VirtualNode is declared 
 * in the virtualNodesAcquisition tag and defined with
 * <pre>
 * lookup virtualNode="Dispatcher" host="hostname" protocol="rmi or jini"
 * </pre>
 * @author  ProActive Team
 * @version 1.0,  2003/04/01
 * @since   ProActive 1.0.2
 */

public class VirtualNodeLookup extends RuntimeDeploymentProperties implements VirtualNode
{
	private VirtualNode virtualNode;
	private ProActiveRuntime remoteProActiveRuntime;
	private String name;
	private String urlForLookup;
	private String lookupProtocol;
	private String message = "########## Calling this method on a VirtualNodeLookup has no sense, since such VirtualNode object references a remote VirtualNode ##########";
	protected String runtimeHostForLookup = "LOOKUP_HOST";
	
	public VirtualNodeLookup(String name){
		this.name = name;	
		ProActiveRuntimeImpl proActiveRuntimeImpl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
		proActiveRuntimeImpl.registerLocalVirtualNode(this,this.name);
	}
	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#setProperty(String)
	 */
	public void setProperty(String property)
	{
		System.out.println(message);
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getProperty()
	 */
	public String getProperty()
	{
		return virtualNode.getProperty();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#setName(String)
	 */
	public void setName(String s)
	{
		System.out.println(message);
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getName()
	 */
	public String getName()
	{
		return virtualNode.getName();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#addVirtualMachine(VirtualMachine)
	 */
	public void addVirtualMachine(VirtualMachine virtualMachine)
	{
		System.out.println(message);
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getVirtualMachine()
	 */
	public VirtualMachine getVirtualMachine()
	{
		return virtualNode.getVirtualMachine();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#activate()
	 */
	public void activate()
	{
		if (isWaitingForProperties()) return;
		try{
//		this.remoteProActiveRuntime = RuntimeFactory.getRuntime(urlForLookup,lookupProtocol);
//		this.virtualNode = remoteProActiveRuntime.getVirtualNode(this.name);
			this.virtualNode = ProActive.lookupVirtualNode(urlForLookup,lookupProtocol);
		}catch(ProActiveException e){
			e.printStackTrace();
		}
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNodeCount()
	 */
	public int getNodeCount()
	{
		return virtualNode.getNodeCount();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#createdNodeCount()
	 */
	public int createdNodeCount()
	{
		return virtualNode.createdNodeCount();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNode()
	 */
	public Node getNode() throws NodeException
	{
		try{
		checkActivation();
		}catch(ProActiveException pae){
			throw new NodeException(pae);
		}
		return virtualNode.getNode();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNode(int)
	 */
	public Node getNode(int index) throws NodeException
	{
		try{
		checkActivation();
		}catch(ProActiveException pae){
			throw new NodeException(pae);
		}
		return virtualNode.getNode(index);
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNodesURL()
	 */
	public String[] getNodesURL() throws NodeException
	{
		try{
		checkActivation();
		}catch(ProActiveException pae){
			throw new NodeException(pae);
		}
		return virtualNode.getNodesURL();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNodes()
	 */
	public Node[] getNodes() throws NodeException
	{
		try{
		checkActivation();
		}catch(ProActiveException pae){
			throw new NodeException(pae);
		}
		return virtualNode.getNodes();
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNode(String)
	 */
	public Node getNode(String url) throws NodeException
	{
		try{
		checkActivation();
		}catch(ProActiveException pae){
			throw new NodeException(pae);
		}
		return virtualNode.getNode(url);
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#createNodeOnCurrentJvm(String)
	 */
	public void createNodeOnCurrentJvm(String protocol)
	{
		System.out.println(message);
	}
	
	public Object getUniqueAO() throws ProActiveException {
		checkActivation();
		return virtualNode.getUniqueAO();
	}
	
	/**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#setRuntimeInformations(String,String)
	 * At the moment the only property that can be set at runtime is LOOKUP_HOST.
	 */
	public void setRuntimeInformations(String information, String value) throws ProActiveException{
		try{
			checkProperty(information);
		}catch(ProActiveException e){
			throw new ProActiveException("only "+runtimeHostForLookup+" property can be set at runtime",e);
		}
		performTask(information,value);
	}
	
//	//
//	//-----------------------implements DeploymentPropertiesEventListener ----------
//	//
//	
//	public void  lookForProperty(DeploymentPropertiesEvent event){
//		
//	}
	
	
	public void setLookupInformations(String url, String protocol){
		this.urlForLookup = url;
		
		if(urlForLookup.indexOf("*")>-1) runtimeProperties.add(runtimeHostForLookup);
		
		this.lookupProtocol = protocol;
	}
	
	private boolean isWaitingForProperties(){
		return (runtimeProperties.size()>=1);
	}
	
	private void performTask(String information,String value){
		if(information.equals(runtimeHostForLookup)){
		urlForLookup = UrlBuilder.buildUrl(value,this.name,this.lookupProtocol);
		runtimeProperties.remove(runtimeHostForLookup);
		activate();
		}
	}
	
	private void checkActivation() throws ProActiveException{
		if(isWaitingForProperties()){
			String exceptionMessage = "This VirtualNode has not yet been activated since, it is waiting for runtime properties ";
			for(int i = 0; i<runtimeProperties.size(); i++){
				exceptionMessage = exceptionMessage.concat((String)runtimeProperties.get(i)+" ");
			}
			throw new ProActiveException(exceptionMessage);
		}
	}
}
