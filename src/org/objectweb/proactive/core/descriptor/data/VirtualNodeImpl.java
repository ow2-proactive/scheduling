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
import java.util.Hashtable;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.lsf.LSFBSubProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

/**
 * A <code>VirtualNode</code> is a conceptual entity that represents one or several nodes. After activation
 * a <code>VirtualNode</code> represents one or several nodes.
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptor
 * @see VirtualMachine
 */

public class VirtualNodeImpl extends RuntimeDeploymentProperties implements VirtualNode,Serializable,RuntimeRegistrationEventListener

{
//

	
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //
  
  
  /** Reference on the local runtime*/
  protected transient ProActiveRuntimeImpl proActiveRuntimeImpl;
  
  /** the name of this VirtualNode */
  private String name;

  /** the property of this virtualNode, property field can take five value: null,unique, unique_singleAO, multiple, multiple_cyclic */
  private String property;

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
  
  /** the list of VitualNodes Id that this VirualNode is waiting for in order to create Nodes on a JVM 
   * already assigned in the XML descriptor */
  private Hashtable awaitedVirtualNodes;
  
  private String registrationProtocol;
  
  private boolean registration = false;
  
  private boolean waitForTimeout = false;
  
	protected int MAX_RETRY = 70;
	
	private Object uniqueActiveObject = null;
	
  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //
 /**
  * Contructs a new intance of VirtualNode
  */
  VirtualNodeImpl(){
  }

 /**
  * Contructs a new intance of VirtualNode
  */
  VirtualNodeImpl(String name) {
  	this.name = name;
    virtualMachines = new java.util.ArrayList(5);
    createdNodes = new java.util.ArrayList();
    awaitedVirtualNodes = new Hashtable();
    proActiveRuntimeImpl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
		if (logger.isDebugEnabled()) {
   	 logger.debug("vn "+this.name+" registered on "+proActiveRuntimeImpl.getVMInformation().getVMID().toString());
		}
    proActiveRuntimeImpl.addRuntimeRegistrationEventListener(this);
    proActiveRuntimeImpl.registerLocalVirtualNode(this,this.name);
    
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //
  
  public void setProperty(String value) {
    this.property = value;
  }
  
  public String getProperty() {
    return property;
  }
  
  public void setTimeout(String timeout){
  	MAX_RETRY = new Integer (timeout).intValue();
  	waitForTimeout = true;
  }
  

  public void setName(String s) {
    this.name = s;
  }
  
  public String getName() {
    return name;
  }
  
  
  public void addVirtualMachine(VirtualMachine virtualMachine) {
    virtualMachines.add(virtualMachine);
    if(!((virtualMachine.getCreatorId()).equals(this.name))){
    	// add in the hashtable the vm's creator id, and the number of nodes that should be created
				awaitedVirtualNodes.put(virtualMachine.getCreatorId(),virtualMachine);
				//we need to do it here otherwise event could occurs, whereas vm 's creator id is not in the hash map
				//just synchro pb, this workaround solves the pb
    }
		if (logger.isDebugEnabled()) {
    	logger.debug("mapped VirtualNode="+name+" with VirtualMachine="+virtualMachine.getName());
		}
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
  	
    for (int i = 0; i < virtualMachines.size(); i++)
		{
			VirtualMachine vm = getVirtualMachine();
			boolean vmAlreadyAssigned = !((vm.getCreatorId()).equals(this.name));
			ExternalProcess process = getProcess(vm,vmAlreadyAssigned);
			
			// Test if that is this virtual Node that originates the creation of the vm
			// else the vm was already created by another virtualNode, in that case, nothing is
			// done at this point, nodes creation will occur when the runtime associated with the jvm
			// will register.
				if(!vmAlreadyAssigned){
					setParameters(process,vm);
					// It is this virtual Node that originates the creation of the vm
					try{
    				proActiveRuntimeImpl.createVM(process);	
    			}catch(java.io.IOException e){
    				e.printStackTrace();
    				logger.error("cannot activate virtualNode "+this.name+" with the process "+process.getCommand());
    			}
				}
//			}else{
//				// add in the hashtable the vm's creator id, and the number of nodes that should be created
//				awaitedVirtualNodes.put(vm.getCreatorId(),vm.getNodeNumber());
//			}
			increaseIndex();	
		}
		if (registration){
			register();
		}
    
  }
  
  
//  /**
//   * Desactivate all the Nodes mapped to This VirtualNode in the XML Descriptor
//   */
//  public void desactivate(){
//  }
  
  
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
  public int createdNodeCount(){
    return nodeCountCreated;
  }
  
  
  /**
   * Returns the first Node created among Nodes mapped to this VirtualNode in the XML Descriptor 
   * Another call to this method will return the following created node if any.
   * @return Node
   */
  public Node getNode() throws NodeException{
  	//try first to get the Node from the createdNodes array to be continued
  	Node node;
  	waitForNodeCreation();
  	if (!createdNodes.isEmpty()) {
    node = (Node) createdNodes.get(lastNodeIndex);
    increaseNodeIndex();
    return node;
  	}else {
  		throw new NodeException("Cannot get the node "+this.name);
  	}		
  }
  
  
  public Node getNode(int index) throws NodeException{
  	Node node = (Node)createdNodes.get(index);
  	if (node == null) throw new NodeException("Cannot return the first node, no nodes hava been created");
    return node;
  }
  
  
  public String[] getNodesURL() throws NodeException{
  	String [] nodeNames ;
  	try{
  	waitForAllNodesCreation();
  	}catch(NodeException e){

  		logger.error(e.getMessage());

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
  	waitForAllNodesCreation();
  	}catch(NodeException e){

  		logger.error(e.getMessage());

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
  

  public Node getNode(String url) throws NodeException{
    Node node = null;
    try{
    waitForAllNodesCreation();
    }catch(NodeException e){

  		logger.error(e.getMessage());

  	}
    if (!createdNodes.isEmpty()){
    	synchronized(createdNodes){
    		for (int i = 0; i < createdNodes.size(); i++)
				{
					if (((Node)createdNodes.get(i)).getNodeInformation().getURL().equals(url)){
						 node = (Node)createdNodes.get(i);
						 break;
						 }
				}
				return node;
    	}
    }else{
  		throw new NodeException("Cannot return nodes, no nodes hava been created");
  	}
  }
  
  
  public void createNodeOnCurrentJvm(String protocol){
  	try{
    // this method should be called when in the xml document the tag currenJVM is encountered. It means that one node must be created
    // on the jvm that originates the creation of this virtualNode(the current jvm) and mapped on this virtualNode
    // we must increase the node count
  	increaseNodeCount(1);
  	String nodeName = this.name+Integer.toString(new java.util.Random(System.currentTimeMillis()).nextInt());
  	String nodeHost = java.net.InetAddress.getLocalHost().getHostName();
  	String url = buildURL(nodeHost,nodeName,protocol);
  	// get the Runtime for the given protocol
  	ProActiveRuntime defaultRuntime = RuntimeFactory.getProtocolSpecificRuntime(checkProtocol(protocol));
  	//create the node
  	defaultRuntime.createLocalNode(url,false);
  	//add this node to this virtualNode
//  	createdNodes.add(new NodeImpl(defaultRuntime,url,checkProtocol(protocol)));

//		nodeCreated = true;
//  	nodeCountCreated ++ ;
			performOperations(defaultRuntime,url,protocol);
  	}catch(Exception e)
  	{
			e.printStackTrace();
		}
  }
  
  public Object getUniqueAO() throws ProActiveException{
  	

  	if(!property.equals("unique_singleAO")) logger.warn("!!!!!!!!!!WARNING. This VirtualNode is not defined with unique_single_AO property in the XML descriptor. Calling getUniqueAO() on this VirtualNode can lead to unexpected behaviour");

  	if(uniqueActiveObject == null){
  		try{
  		Node node = getNode();

  		if(node.getActiveObjects().length > 1) logger.warn("!!!!!!!!!!WARNING. More than one active object is created on this VirtualNode.");

  		uniqueActiveObject = node.getActiveObjects()[0];
 		 
  		}catch(Exception e){
  			e.printStackTrace();
  		}
  		
  	}
  	if(uniqueActiveObject == null) throw new ProActiveException("No active object are registered on this VirtualNode");
  	
  	return uniqueActiveObject;
  	
  }
  //
  //-------------------IMPLEMENTS RuntimeRegistrationEventListener------------
  //
  
  public synchronized void runtimeRegistered(RuntimeRegistrationEvent event) {

  	String nodeName;
  	String [] nodeNames = null;
  	ProActiveRuntime proActiveRuntimeRegistered;
  	String nodeHost;
  	String protocol;
  	String url;
  	//Check if it this virtualNode that originates the process
  	if(event.getCreatorID().equals(this.name)){
			if (logger.isDebugEnabled()) {
  			logger.debug("runtime "+event.getCreatorID()+" registered on virtualnode "+this.name);
			}
  	  protocol = event.getProtocol();
  		//gets the registered runtime
  	  proActiveRuntimeRegistered = proActiveRuntimeImpl.getProActiveRuntime(event.getRegisteredRuntimeName());
  		try{
  			//get the node on the registered runtime
  			nodeNames = proActiveRuntimeRegistered.getLocalNodeNames();
  		}catch(ProActiveException e){
  			e.printStackTrace();
  		}
  		// get the host of nodes
  		nodeHost = proActiveRuntimeRegistered.getVMInformation().getInetAddress().getHostName();
  		for (int i = 0; i < nodeNames.length; i++){
			
				nodeName = nodeNames[i];
			  url = buildURL(nodeHost,nodeName,protocol);
				performOperations(proActiveRuntimeRegistered,url,protocol);
			}
			
  	}
  	//Check if the virtualNode that originates the process is among awaited VirtualNodes
  	if(awaitedVirtualNodes.containsKey(event.getCreatorID())){
  		//gets the registered runtime
  		proActiveRuntimeRegistered = proActiveRuntimeImpl.getProActiveRuntime(event.getRegisteredRuntimeName());
  		// get the host for the node to be created
  		nodeHost = proActiveRuntimeRegistered.getVMInformation().getInetAddress().getHostName();
  		protocol = event.getProtocol();
  		// it is the only way to get accurate value of nodeNumber
  		VirtualMachine vm = (VirtualMachine)awaitedVirtualNodes.get(event.getCreatorID());
  		int nodeNumber = (new Integer((String)vm.getNodeNumber())).intValue();
  		for (int i = 1; i <= nodeNumber; i++)
				{
					try{
						nodeName = this.name+Integer.toString(new java.util.Random(System.currentTimeMillis()).nextInt());
						url = buildURL(nodeHost,nodeName,protocol);
						// nodes are created from the registered runtime, since this virtualNode is
						// waiting for runtime registration to perform co-allocation in the jvm.
					  proActiveRuntimeRegistered.createLocalNode(url,false);						
							performOperations(proActiveRuntimeRegistered,url,protocol);
					}catch(ProActiveException e){
						e.printStackTrace();
					}
			}
  	} 
  }
  
  /**
	 * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#setRuntimeInformations(String,String)
	 * At the moment no property can be set at runtime on a VirtualNodeImpl.
	 */
	public void setRuntimeInformations(String information, String value) throws ProActiveException{
		try{
			checkProperty(information);
		}catch(ProActiveException e){
			throw new ProActiveException("No property can be set at runtime on this VirtualNode",e);
		}
		
	}
	
  public void setRegistrationProtocol(String protocol){
  	setRegistrationValue(true);
  	this.registrationProtocol = protocol;
  }
  
  
  
  public String getRegistrationProtocol(){
  	return this.registrationProtocol;
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
  private void waitForAllNodesCreation() throws NodeException{
  	int count = 0;
  	
  	if (waitForTimeout){
  		while(count<MAX_RETRY){
  			count ++;
  			try {
         	Thread.sleep(1000);
      	} catch (InterruptedException e2) {
      		e2.printStackTrace();
      	}
  		}
  		
  	}else{
  		while(nodeCountCreated != nodeCount){
  			if(count<MAX_RETRY){

  				count ++;
  				try {
         		Thread.sleep(1000);
      		} catch (InterruptedException e2) {
      			e2.printStackTrace();
      		}
  			}else{
  				throw new NodeException("After many retries, only "+nodeCountCreated+" nodes are created on "+nodeCount+" expected");
  			}
  		}
  	}
  	
  	return;
  }
  
  
	/**
 	* Returns the process mapped to the given virtual machine mapped to this virtual node
 	* @param VirtualMachine
 	* @return ExternalProcess
 	*/
  private ExternalProcess getProcess(VirtualMachine vm, boolean vmAlreadyAssigned){
  ExternalProcess copyProcess;
	//VirtualMachine vm = getVirtualMachine();
	ExternalProcess process = vm.getProcess();
	// we need to do a deep copy of the process otherwise,
	//modifications will be applied on one object that might 
	// be referenced by other virtualNodes .i.e check started
	if(!vmAlreadyAssigned){
		copyProcess = makeDeepCopy(process);
		vm.setProcess(copyProcess);
		return copyProcess;
	}else{
		//increment the node count by nodeNumber
		increaseNodeCount(new Integer(vm.getNodeNumber()).intValue());
		return process;
	}
 }
  
  
	/**
	 * Sets parameters to the JVMProcess linked to the ExternalProcess
	 * @param process
	 */
  private void setParameters(ExternalProcess process, VirtualMachine vm){
  	ExternalProcess processImpl = process;
  	ExternalProcessDecorator processImplDecorator;
  	JVMProcess jvmProcess;
  	LSFBSubProcess bsub = null;
  	
  	int nodeNumber = new Integer(vm.getNodeNumber()).intValue();
		if (logger.isDebugEnabled()) {
  		logger.debug("nodeNumber "+nodeNumber);
		}
  	
  	while(ExternalProcessDecorator.class.isInstance(processImpl)){
  		if(processImpl instanceof LSFBSubProcess){
  			//if the process is bsub we have to increase the node count by the number of processors
  			bsub = (LSFBSubProcess)processImpl;
  			increaseNodeCount((new Integer(bsub.getProcessorNumber()).intValue())*nodeNumber);
  		}
  		processImplDecorator = (ExternalProcessDecorator)processImpl;
  		processImpl = processImplDecorator.getTargetProcess();
  	}
  	//When the virtualNode will be activated, it has to launch the process
  	//with such parameter.See StartRuntime
  	jvmProcess = (JVMProcess)processImpl;
  	//if the target class is StartRuntime, then give parameters otherwise keep parameters
  	if(jvmProcess.getClassname().equals("org.objectweb.proactive.core.runtime.StartRuntime")){
  		
  		//we increment the index of nodecount
  		if(bsub == null){
  			//if bsub is null we can increase the nodeCount
  			increaseNodeCount(nodeNumber);
  		}
  		//if(!vmAlreadyAssigned){
  			String vnName = this.name;
  			String acquisitionMethod = vm.getAcquisitionMethod();
				if (logger.isDebugEnabled()) {
	  			logger.debug("Aquisition method :"+acquisitionMethod);
	  			logger.debug(vnName);
				}
  			String localruntimeURL = proActiveRuntimeImpl.getURL();
				if (logger.isDebugEnabled()) {
  				logger.debug(localruntimeURL);
				}
  			jvmProcess.setParameters(vnName+" "+localruntimeURL+" "+acquisitionMethod+":"+" "+nodeNumber);
  		
  	}
  }
  
	/**
	 * Returns a deepcopy of the process
	 * @param process the process to copy
	 * @return ExternalProcess, the copy version of the process
	 */
  private ExternalProcess makeDeepCopy(ExternalProcess process){
  	//deepCopyTag = true;
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
  	//deepCopyTag = false;
    return result; 
  }
  
  
  private String buildURL(String host, String name,String protocol){
  	protocol = checkProtocol(protocol);
  	if(protocol.equals("jini:")) return UrlBuilder.buildUrl(host,name,protocol);
  	return UrlBuilder.buildUrl(host,name);
  } 
  
  private void increaseIndex(){ 
  	if (virtualMachines.size() > 1) {
      		lastVirtualMachineIndex = (lastVirtualMachineIndex + 1) % virtualMachines.size();
    		}
  }
  
  private void increaseNodeCount(int n){
  	nodeCount = nodeCount + n;
		if (logger.isDebugEnabled()) {
  		logger.debug("NodeCount: "+nodeCount);
		}
  }
  
  private void increaseNodeIndex(){
  	if (createdNodes.size() > 1){	
  			lastNodeIndex = (lastNodeIndex + 1) % createdNodes.size();
  	}
  }
  
  	
  private String checkProtocol(String protocol){
  	
  	if(protocol.indexOf(":") == -1) return protocol.concat(":");
  	return protocol;
  }
  
  
  private void performOperations(ProActiveRuntime part, String url, String protocol){
  	createdNodes.add(new NodeImpl(part,url,checkProtocol(protocol)));
  	logger.info("**** Mapping VirtualNode "+this.name+" with Node: "+url+" done");	
		nodeCreated = true;
		nodeCountCreated++;
  }
  
  private void register() {
  	
  	try{
  	waitForAllNodesCreation();
//  	ProActiveRuntime part = RuntimeFactory.getProtocolSpecificRuntime(registrationProtocol);
//  	part.registerVirtualnode(this.name,false);
		ProActive.registerVirtualNode(this,registrationProtocol,false);
  	}catch(NodeException e){

  		logger.error(e.getMessage());

  	}
  	catch(ProActiveException e){
  		e.printStackTrace();
  	}
  	
  }
  
  private void setRegistrationValue(boolean value){
  	this.registration = value;
  }
  
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
  	
  	try{
  	waitForAllNodesCreation();
  	}catch(NodeException e){
  		e.printStackTrace();
  	}
		
  	out.defaultWriteObject();
  
  }

	
	
  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
  }
 
  

  

}
