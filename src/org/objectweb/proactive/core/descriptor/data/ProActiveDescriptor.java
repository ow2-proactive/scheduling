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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.ext.security.PolicyServer;

/**
 * <p>
 * A <code>ProactiveDescriptor</code> is an internal representation of XML
 * Descriptor. It offers a set of services to access/activate/desactivate
 * <code>VirtualNode</code>.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see VirtualNode
 * @see VirtualMachine
 */
public interface ProActiveDescriptor extends java.io.Serializable{

  
  /**
   * Returns all VirtualNodes described in the XML Descriptor
   * @return VirtualNode[] all the VirtualNodes described in the XML Descriptor
   */
  public VirtualNode[] getVirtualNodes();
  
  /**
   * Returns the specified VirtualNode
   * @param name name of the VirtualNode
   * @return VirtualNode VirtualNode of the given name
   */  
  public VirtualNode getVirtualNode(String name);
  
  
  /**
   * Returns the VitualMachine of the given name
   * @param name
   * @return VirtualMachine
   */
  public VirtualMachine getVirtualMachine(String name);

	
	/**
   * Returns the Process of the given name
   * @param name
   * @return ExternalProcess
   */
  public ExternalProcess getProcess(String name);

	
	/**
   * Creates a VirtualNode with the given name
   * If the VirtualNode with the given name has previously been created, this method returns it.
   * @param vnName
   * @param lookup if true, at creation time the VirtualNode will be a VirtualNodeLookup.
   * If false the created VirtualNode is a VirtualNodeImpl. Once the VirtualNode created this field 
   * has no more influence when calling this method
   * @return VirtualNode
   */
  public VirtualNode createVirtualNode(String vnName, boolean lookup);
  
  
  /**
   * Creates a VirtualMachine of the given name
   * @param vmName
   * @return VirtualMachine
   */
  public VirtualMachine createVirtualMachine(String vmName);
  
  
  /**
   * Creates an ExternalProcess of the given className with the specified ProcessID
   * @param processID
   * @param processClassName
   * @throws ProActiveException if a problem occurs during process creation
   */
  public ExternalProcess createProcess(String processID, String processClassName) throws ProActiveException;
  
  
  /**
   * Returns a new instance of ExternalProcess from processClassName
   * @param processClassName
   * @throws ProActiveException if a problem occurs during process creation
   */
  public ExternalProcess createProcess(String processClassName) throws ProActiveException;
  
  
  /**
   * Maps the process given by the specified processID with the specified virtualMachine.
   * @param virtualMachine
   * @param processID
   */
  public void registerProcess(VirtualMachine virtualMachine, String processID);
  
  
  /**
   * Registers the specified composite process with the specified processID.
   * @param compositeProcess
   * @param processID
   */
  public void registerProcess(ExternalProcessDecorator compositeProcess, String processID);
  
  
  /**
   * Activates all VirtualNodes defined in the XML Descriptor.
   */
  public void activateMappings();
  
  
  /**
   * Activates the specified VirtualNode defined in the XML Descriptor
   * @param virtualNodeName name of the VirtulNode to be activated
   */
  public void activateMapping(String virtualNodeName);
  
  
	/**
	 * Kills all Nodes and JVMs(local or remote) created when activating the descriptor
	 * @param softly if false, all jvms created when activating the descriptor are killed abruptely
	 * if true a jvm that originates the creation of  a rmi registry waits until registry is empty before
	 * dying. To be more precise a thread is created to ask periodically the registry if objects are still
	 * registered.
	 * @throws ProActiveException if a problem occurs when terminating all jvms
	 */
  public void killall(boolean softly) throws ProActiveException;
  
  
  
//  /**
//   * Kills all Nodes mapped to VirtualNodes in the XML Descriptor
//   * This method kills also the jvm on which 
//   */
//  public void desactivateMapping();
//  
//  
//  /**
//   * Kills all Nodes mapped to the specified VirutalNode in the XML Descriptor
//   * @param vitualNodeName name of the virtualNode to be desactivated
//   */
//  public void desactivateMapping(String virtualNodeName);
  
	/**
	 * Returns the size of virualNodeMapping HashMap
	 * @return int
	 */
	public int getVirtualNodeMappingSize();
	
	// SECURITY
	/**
	 * Intialize application security policy
	 * @param file
	 */
	public void createPolicyServer(String file);
	
	public PolicyServer getPolicyServer();
	public String getSecurityFilePath();

}
