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


import java.util.Collection;
import java.util.Iterator;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;

/**
 * <p>
 * A <code>ProactiveDescriptor</code> is an internal representation of XML
 * Descriptor. It offers a set of services to access/activate/desactivate
 * <code>VirtualNode</code>.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 *
 */
public class ProActiveDescriptorImpl implements ProActiveDescriptor
{
//
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //

  /** map virtualNode name and objects */
  private java.util.HashMap virtualNodeMapping;

  /** map jvm name and object */
  private java.util.HashMap virtualMachineMapping;
  
  /** map process id and process */
  private java.util.HashMap processMapping;

  /** map process id and process updater for later update of the process */
  private java.util.HashMap pendingProcessMapping;


  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

 /**
  * Contructs a new intance of ProActiveDescriptor
  */
  public ProActiveDescriptorImpl() {
    virtualNodeMapping = new java.util.HashMap();
    virtualMachineMapping = new java.util.HashMap();
    processMapping = new java.util.HashMap();
    pendingProcessMapping = new java.util.HashMap();
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //
    
  public VirtualNode[] getVirtualNodes(){
  	int i = 0;
  	VirtualNode[] virtualNodeArray = new VirtualNode[virtualNodeMapping.size()];
  	Collection collection = virtualNodeMapping.values();
  	for (Iterator iter = collection.iterator(); iter.hasNext();)
		{
			virtualNodeArray[i] = (VirtualNode) iter.next();
			i++;
		}
		return virtualNodeArray;
  }
  
  
  public VirtualNode getVirtualNode(String name) {
    return (VirtualNode) virtualNodeMapping.get(name);
  }
  
  
  public VirtualMachine getVirtualMachine(String name) {
    return (VirtualMachine) virtualMachineMapping.get(name);
  }


  public ExternalProcess getProcess(String name) {
    return (ExternalProcess) processMapping.get(name);
  }


  public VirtualNode createVirtualNode(String vnName) {
    VirtualNode vn = getVirtualNode(vnName);
    if (vn == null) {
      vn = new VirtualNodeImpl();
      vn.setName(vnName);
      virtualNodeMapping.put(vnName, vn);
      System.out.println("created VirtualNode name="+vnName);
    }
    return vn;
  }
  
  
  public VirtualMachine createVirtualMachine(String vmName) {
    VirtualMachine vm = getVirtualMachine(vmName);
    if (vm == null) {
      vm = new VirtualMachineImpl();
      vm.setName(vmName);
      virtualMachineMapping.put(vmName, vm);
      //System.out.println("created VirtualMachine name="+vmName);
    }
    return vm;
  }
  
  
  public ExternalProcess createProcess(String processID, String processClassName) throws ProActiveException {
    ExternalProcess process = getProcess(processID);
    if (process == null) {
      process = createProcess(processClassName);
      addExternalProcess(processID, process);
    }
    return process;
  }
  
  
  public ExternalProcess createProcess(String processClassName) throws ProActiveException {
    try {
      Class processClass = Class.forName(processClassName);
      return (ExternalProcess) processClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new ProActiveException(e);
    } catch (InstantiationException e) {
      throw new ProActiveException(e);
    } catch (IllegalAccessException e) {
      throw new ProActiveException(e);
    }      
  }
  
  
  public void registerProcess(VirtualMachine virtualMachine, String processID) {
    //System.out.println(processID);
    ExternalProcess process = getProcess(processID);
    if (process == null) {
      addPendingProcess(processID, virtualMachine);
      //System.out.println("registered Process name="+processID+" for a virtualMachine="+virtualMachine.getName());
    } else {
      //System.out.println("found existing process="+process+" for a virtualMachine="+virtualMachine.getName());
      virtualMachine.setProcess(process);
    }
  }
  
  
  public void registerProcess(ExternalProcessDecorator compositeProcess, String processID) {
    ExternalProcess process = getProcess(processID);
    if (process == null) {
      addPendingProcess(processID, compositeProcess);
      //System.out.println("registered Process name="+processID+" for a compositeProcess");
    } else {
      compositeProcess.setTargetProcess(process);
      //System.out.println("found existing process for compositeProcess="+compositeProcess);
    }
  }
  
  
  public void activateMappings(){
			VirtualNode[] virtualNodeArray = getVirtualNodes();
			for (int i = 0; i < virtualNodeArray.length; i++)
			{
				virtualNodeArray[i].activate();
			}
  }
  
  
  public void activateMapping(String virtualNodeName){	
  	VirtualNode virtualNode = getVirtualNode(virtualNodeName);
  	virtualNode.activate();
  }
  
  
//  public void desactivateMapping(){
//  	VirtualNode[] virtualNodeArray = getVirtualNodes();
//			for (int i = 0; i < virtualNodeArray.length; i++)
//			{
//				virtualNodeArray[i].desactivate();
//			}
//  }
//  
//  
//  public void desactivateMapping(String virtualNodeName){
//  	VirtualNode virtualNode = getVirtualNode(virtualNodeName);
//  	virtualNode.desactivate();
//  }
  
	/**
	 * Returns the size of virualNodeMapping HashMap
	 * @return int
	 */
	public int getVirtualNodeMappingSize(){
	return virtualNodeMapping.size();
	}
  //
  //  ----- PROTECTED METHODS -----------------------------------------------------------------------------------
  //
    


  //
  //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
  //
    
  private void addExternalProcess(String processID, ExternalProcess process) {
    ProcessUpdater processUpdater = (ProcessUpdater) pendingProcessMapping.remove(processID);
    if (processUpdater != null) {
      //System.out.println("Updating Process name="+processID);
      processUpdater.updateProcess(process);
    }
    processMapping.put(processID, process);
  }
  
  
  private void addPendingProcess(String processID, VirtualMachine virtualMachine) {
    ProcessUpdater updater = new VirtualMachineProcessUpdater(virtualMachine);
    //pendingProcessMapping.put(processID, updater);
    addUpdater(processID,updater);
  }
  
  
  private void addPendingProcess(String processID, ExternalProcessDecorator compositeProcess) {
    ProcessUpdater updater = new CompositeExternalProcessUpdater(compositeProcess);
    //pendingProcessMapping.put(processID, updater);
    addUpdater(processID,updater);
  }
  
  
  private void addUpdater(String processID, ProcessUpdater processUpdater) {
    CompositeProcessUpdater compositeProcessUpdater = (CompositeProcessUpdater) pendingProcessMapping.get(processID);
    if (compositeProcessUpdater == null) {
      compositeProcessUpdater = new CompositeProcessUpdater();
      //pendingProcessMapping.put(processID, processUpdater);
      pendingProcessMapping.put(processID, compositeProcessUpdater);
    }
    compositeProcessUpdater.addProcessUpdater(processUpdater);
  }
  
  
  
  //
  //  ----- INNER CLASSES -----------------------------------------------------------------------------------
  //
  
  private interface ProcessUpdater {
    public void updateProcess(ExternalProcess p);
  }
  
  
  private class CompositeProcessUpdater implements ProcessUpdater {
  
    private java.util.ArrayList updaterList;
    
    public CompositeProcessUpdater() {
      updaterList = new java.util.ArrayList();
    }
    
    public void addProcessUpdater(ProcessUpdater p) {
      updaterList.add(p);
    }
    
    public void updateProcess(ExternalProcess p) {
      java.util.Iterator it = updaterList.iterator();
      while (it.hasNext()) {
        ProcessUpdater processUpdater = (ProcessUpdater) it.next();
        processUpdater.updateProcess(p);
      }
      updaterList.clear();
    }
  }
  
  
  private class CompositeExternalProcessUpdater implements ProcessUpdater {
    private ExternalProcessDecorator compositeExternalProcess;
    public CompositeExternalProcessUpdater(ExternalProcessDecorator compositeExternalProcess) {
      this.compositeExternalProcess = compositeExternalProcess;
    }
    public void updateProcess(ExternalProcess p) {
      //System.out.println("Updating CompositeExternal Process");
      compositeExternalProcess.setTargetProcess(p);
    }
  }
  
  
  private class VirtualMachineProcessUpdater implements ProcessUpdater {
    private VirtualMachine virtualMachine;
    public VirtualMachineProcessUpdater(VirtualMachine virtualMachine) {
      this.virtualMachine = virtualMachine;
    }
    public void updateProcess(ExternalProcess p) {
      //System.out.println("Updating VirtualMachine Process");
      virtualMachine.setProcess(p);
    }
  }

}
