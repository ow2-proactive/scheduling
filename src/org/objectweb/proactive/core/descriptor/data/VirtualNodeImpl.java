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


import java.io.Serializable;
import java.util.Iterator;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.UrlBuilder;
/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class VirtualNodeImpl implements VirtualNode,Serializable
{
//
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //
  /** Reference on the local runtime*/
  private ProActiveRuntimeImpl proActiveRuntimeImpl;
  /** the name of this VirtualNode */
  private String name;

  /** true if this VirtualNode is cyclic */
  private boolean cyclic;

  /** true if this VirtualNode should be created localy in case the creation of the target jvm fails */
  private boolean localBackup;

  /** the list of virtual machines associated with this VirtualNode */
  private java.util.ArrayList virtualMachines;
  
  /** index of the last associated jvm used */
  private int lastVirtualMachineIndex;
  /** the list of nodes linked to this VirtualNode that have been created*/
  private java.util.ArrayList createdNodes;
  /** index of the last node used */
  private int lastNodeIndex;

  /** Number of Nodes mapped to this VirtualNode in the XML Descriptor */
  private int nodeCount;
  
  /** Number of Nodes mapped to this VitualNode in the XML Descriptor that are actually created */
  private int nodeCountCreated;
  /** true if the node has been created*/
  private boolean nodeCreated = false;
  
	protected static final int MAX_RETRY = 70;
  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

 /**
  * Contructs a new intance of VirtualNode
  */
  VirtualNodeImpl() {
    virtualMachines = new java.util.ArrayList(5);
    createdNodes = new java.util.ArrayList();
    proActiveRuntimeImpl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
   // System.out.println("vn "+this.name+" registered on "+proActiveRuntimeImpl.getVMInformation().getVMID().toString());
    proActiveRuntimeImpl.addRuntimeRegistrationEventListener(this);
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //
  
  public void setCyclic(boolean b) {
    cyclic = b;
  }
  
  public boolean getCyclic() {
    return cyclic;
  }


  public void setLocalBackup(boolean b) {
    localBackup = b;
  }
  
  public boolean getLocalBackup() {
    return localBackup;
  }


  public void setName(String s) {
    this.name = s;
  }
  
  public String getName() {
    return name;
  }
  
  
  public void addVirtualMachine(VirtualMachine virtualMachine) {
    virtualMachines.add(virtualMachine);
    //System.out.println("mapped VirtualNode="+name+" with VirtualMachine="+virtualMachine.getName());
  }

  public VirtualMachine getVirtualMachine() {
    if (virtualMachines.isEmpty()) return null;
    VirtualMachine vm = (VirtualMachine) virtualMachines.get(lastVirtualMachineIndex);
    return vm;
  }
  
  
  /**
   * Activates all the Nodes mapped to this VirtualNode in the XML Descriptor
   */
  public void activate(){
  	
    //VirtualMachine vm = getVirtualMachine();
    //JVMProcessImpl process =(JVMProcessImpl)vm.getProcess();
    for (int i = 0; i < virtualMachines.size(); i++)
		{
			ExternalProcess process = getProcess();
			setParameters(process);
			try{
    		if (process.getHostname() == null){
    		// it is a local process:JVMProcess
    			proActiveRuntimeImpl.createLocalVM((JVMProcess)process);
    		}else{
    		// it is a remote process
    			proActiveRuntimeImpl.createRemoteVM(process);
    		}
    		increaseIndex();
    	}catch(java.io.IOException e){
    		e.printStackTrace();
    		System.out.println("cannot activate virtualNode "+this.name+" with the process "+process.getCommand());
    	}
		}
    
  }
  
  
  /**
   * Desactivate all the Nodes mapped to This VirtualNode in the XML Descriptor
   */
  public void desactivate(){
  }
  
  
  /**
   * Returns the number of Nodes mapped to this VirtualNode in the XML Descriptor
   * @return int
   */
  public int getNodeCount(){
    return nodeCount;
  }
  
  
  /**
   * Returns the number of Nodes already created among the Nodes mapped to this VirtualNode in the XML Descriptor
   * @return int
   */
  public int CreatedNodeCount(){
    return nodeCountCreated;
  }
  
  
  /**
   * Returns the first Node available among Nodes mapped to this VirtualNode in the XML Descriptor 
   * Another call to this method will return the following available node if any.
   * @return Node
   */
  public Node getNode() throws NodeException{
  	//try first to get the Node from the createdNodes array to be continued
  	Node node;
  	waitForNodeCreation();
  	if (!createdNodes.isEmpty()) {
    node = (Node) createdNodes.get(lastNodeIndex);
    return node;
  	}else {
  		throw new NodeException("Cannot get the node "+this.name);
  	}
//  	// if we cannot find the node then create a name and get the Node from the RMIregistry 
//    String nodeName = buildNodeName();
//    node = getNode(nodeName);
//    if (node != null){
//    	return node;
//    	}
//    	else{
//    	throw new NodeException("Cannot get the node of the name"+nodeName);
//    	}
			
  }
  
  /**
   * Returns the Node mapped to this VirtualNode with the specified index(in the XML Descriptor
   * @param index
   * @return Node
   */
  public Node getNode(int index){
    return (Node)createdNodes.get(index);
  }
  
  
  public String[] getNodesURL() throws NodeException{
  	String [] nodeNames ;
  	try{
  	WaitForAllNodesCreation();
  	}catch(NodeException e){
  		System.out.println(e.getMessage());
  	}
  	if (!createdNodes.isEmpty()){
  		synchronized(createdNodes){
  			nodeNames = new String[createdNodes.size()];
  			for (int i = 0; i < createdNodes.size(); i++)
				{
					nodeNames[i] = ((Node)createdNodes.get(i)).getNodeInformation().getURL();
				}
  		}
  	}else{
  		throw new NodeException("Cannot return nodes, no nodes hava been created");
  	}
  	return nodeNames;
  }
  
  public Node[] getNodes()throws NodeException{
  	Node [] nodeTab;
  	try{
  	WaitForAllNodesCreation();
  	}catch(NodeException e){
  		System.out.println(e.getMessage());
  	}
  	if (!createdNodes.isEmpty()){
  		synchronized(createdNodes){
  			nodeTab = new Node[createdNodes.size()];
  			for (int i = 0; i < createdNodes.size(); i++)
				{
					nodeTab[i] = ((Node)createdNodes.get(i));
				}
  		}
  	}else{
  		throw new NodeException("Cannot return nodes, no nodes hava been created");
  	}
  	return nodeTab;
  }
  
//  /**
//   * Returns the Node mapped to this VirtualNode in the XML Descriptor with the specified name
//   * @param name
//   * @return Node
//   */
//  public Node getNode(String name) throws NodeException{
//    Node node = null;
//    try{
//    node = NodeFactory.getNode(name);
//    }catch (Exception ne){
//    ne.printStackTrace();
//    throw new NodeException("Cannot get the node of the name"+name);
//    } 
//    createdNodes.add(node);
//    return node;
//		return null;
 // }
  
  
  //
  //-------------------IMPLEMENTS RuntimeRegistrationEventListener------------
  //
  
  public synchronized void runtimeRegistered(RuntimeRegistrationEvent event) {
  	//System.out.println("receive event");
  	String nodeName;
  	String [] nodeNames = null;
  	//Check if it this virtualNode that originates the process
  	if(event.getCreatorID().equals(this.name)){
  		//System.out.println("runtime "+event.getCreatorID()+" registered on virtualnode "+this.name);
  		String protocol = event.getProtocol();
  		//gets the registered runtime
  		ProActiveRuntime proActiveRuntimeRegistered = proActiveRuntimeImpl.getProActiveRuntime(event.getRegisteredRuntimeName());
  		try{
  		//get the node on the registered runtime
  		nodeNames = proActiveRuntimeRegistered.getLocalNodeNames();
  		}catch(ProActiveException e){
  			e.printStackTrace();
  		}
  		// get the host of nodes
  		String nodeHost = proActiveRuntimeRegistered.getVMInformation().getInetAddress().getHostName();
  		for (int i = 0; i < nodeNames.length; i++){
  			//System.out.println(buildURL(nodeHost,nodeNames[i]));
				nodeName = nodeNames[i];
				String url = buildURL(nodeHost,nodeName,protocol);
				createdNodes.add(new NodeImpl(proActiveRuntimeRegistered,url,protocol));
				System.out.println("**** Mapping VirtualNode "+this.name+" with Node: "+url+" done");
				nodeCountCreated++;
			}
			//System.out.println("nodecount "+nodeCountCreated);
			nodeCreated = true;
			
  	}
  }
  
  
  //
  //-------------------PRIVATE METHODS--------------------------------------
  //
  
  /**
   * Waits until at least one Node mapped to this VirtualNode in the XML Descriptor is created
   */
  private void waitForNodeCreation() throws NodeException{
  	int count = 0;
  	while(!nodeCreated){
  		if(count<MAX_RETRY){
  			//System.out.println("Error when getting the node, retrying ("+count+")");
  			count ++;
  			try {
         	Thread.sleep(1000);
      	} catch (InterruptedException e2) {
      		e2.printStackTrace();
      	}
  		}else {
  			throw new NodeException("After many retries, not even one node can be found");
  		}
  	}
  	return;
  }
  
  
  /**
   * Waits until all Nodes mapped to this VirtualNode in the XML Descriptor are created
   */
  private void WaitForAllNodesCreation() throws NodeException{
  	int count = 0;
  	while(nodeCountCreated != nodeCount){
  		if(count<MAX_RETRY){
  			//System.out.println("Error when getting the node, retrying ("+count+")");
  			count ++;
  			try {
         	Thread.sleep(1000);
      	} catch (InterruptedException e2) {}
  		}else{
  			throw new NodeException("After many retries, only "+nodeCountCreated+" nodes are created on "+nodeCount+" expected");
  		}
  	}
  	return;
  }
  
	/**
 	* Returns the process mapped to the virtual machine mapped to this virtual node
 	* @return ExternalProcess
 	*/
  private ExternalProcess getProcess(){
  ExternalProcess copyProcess;
	VirtualMachine vm = getVirtualMachine();
	ExternalProcess process = vm.getProcess();
	// we need to do a deep copy of the process otherwise,
	//modifications will be applied on one object that might 
	// be referenced by other virtualNodes .i.e check started
	copyProcess = makeDeepCopy(process);
	vm.setProcess(copyProcess);
	return copyProcess;
  }
  
	/**
	 * Sets parameters to the JVMProcess linked to the ExternalProcess
	 * @param process
	 */
  private void setParameters(ExternalProcess process){
  	ExternalProcess processImpl = process;
  	ExternalProcessDecorator processImplDecorator;
  	JVMProcess jvmProcess;
  	while(ExternalProcessDecorator.class.isInstance(processImpl)){
  		processImplDecorator = (ExternalProcessDecorator)processImpl;
  		processImpl = processImplDecorator.getTargetProcess();
  	}
  	//When the virtualNode will be activated, it has to launch the process
  	//with such parameter.See StartRuntime
  	jvmProcess = (JVMProcess)processImpl;
  	//if the target class is StartRuntime, then give parameters otherwise keep parameters
  	if(jvmProcess.getClassname().equals("org.objectweb.proactive.core.runtime.StartRuntime")){
  		VirtualMachine vm = getVirtualMachine();
  		String vnName = this.name;
  		String acquisitionMethod = vm.getAcquisitionMethod();
  		String nodeNumber = vm.getNodeNumber();
  		//we increment the index of nodecount
  		increaseNodeCount((new Integer(nodeNumber)).intValue());
  		//System.out.println("Aquisition method :"+acquisitionMethod);
  		//System.out.println(vnName);
  		String localruntimeURL = proActiveRuntimeImpl.getURL();
  		//System.out.println(localruntimeURL);
  	
  	
  		jvmProcess.setParameters(vnName+" "+localruntimeURL+" "+acquisitionMethod+":"+" "+nodeNumber);
  	}
  }
  
	/**
	 * Returns a deepcopy of the process
	 * @param process the process to copy
	 * @return ExternalProcess, the copy version of the process
	 */
  private ExternalProcess makeDeepCopy(ExternalProcess process){
  	ExternalProcess result = null;
  	try{
  	java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
    oos.writeObject(process);
    oos.flush();
    oos.close();
    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
    java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
    result = (ExternalProcess)ois.readObject();
    ois.close();
  	}catch(Exception e){
  		e.printStackTrace();
  	}
    return result; 
  }
  
  
  private String buildURL(String host, String name,String protocol){
  	if(protocol.equals("jini:")) return UrlBuilder.buildUrl(host,name,protocol);
  	return UrlBuilder.buildUrl(host,name);
  } 
  
  private void increaseIndex(){
  	if (cyclic) {
      		lastVirtualMachineIndex = (lastVirtualMachineIndex + 1) % virtualMachines.size();
    		}
  }
  
  private void increaseNodeCount(int n){
  	nodeCount = nodeCount + n;
  }

  

}
