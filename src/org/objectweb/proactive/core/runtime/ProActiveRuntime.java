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
package org.objectweb.proactive.core.runtime;

import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;

/**
 * <p>
 * A <code>ProActiveRuntime</code> offers a set of services needed by ProActive to work with 
 * remote JVM. Each JVM that is aimed to hold active objects must contain
 * one and only one instance of the <code>ProActiveRuntime</code> class. That instance, when 
 * created, will register itself to some registry where it is possible to perform a lookup 
 * (such as the RMI registry).
 * </p><p>
 * When ProActive needs to interact with a remote JVM, it will lookup for the <code>ProActiveRuntime</code> associated 
 * with that JVM (using typically the RMI Registry) and use the remote interface of the <code>ProActiveRuntime</code>
 * to perform the interaction.
 * </p><p>
 * Aside the getter giving information about the VM, the 3 services offered are :
 * <ul>
 * <li>the creation of local node through the method <code>createLocalNode</code></li>
 * <li>the creation of another VM(local or remote) through the method <code>createVM</code></li>
 * </ul>
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/08/29
 * @since   ProActive 0.91
 *
 */
public interface ProActiveRuntime {

  /**
   * Creates a new Node in the same VM as this ProActiveRuntime
   * @param nodeName the name of the node to create localy
   * @param replacePreviousBinding
   * @return the url of the newly created node in the target VM
   * @exception NodeException if the new node cannot be created
   */
  public String createLocalNode(String nodeName,boolean replacePreviousBinding) throws NodeException;
  
  
  /**
   * <i><font size="-1" color="#FF0000">**Under development** </font></i>
   * Kills all Nodes in this ProActiveRuntime
   */
  public void DeleteAllNodes();
  
  
  /**
   * <i><font size="-1" color="#FF0000">**Under development** </font></i>
   * Kills the Node of the given name and all Active Objects deployed on it.
   * @param nodeName the name of the node to kill
   */
  public void killNode(String nodeName);
  
  
//  /**
//   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
//   * Creates a new ProActiveRuntime associated with a new VM on the same host as this ProActiveRuntime.
//   * @param jvmProcess the process to spawn localy
//   * @exception java.io.IOException if the new VM cannot be created
//   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
//   */
//  public void createLocalVM(JVMProcess jvmProcess) throws java.io.IOException,ProActiveException;
//  
  
  /**
   * Creates a new ProActiveRuntime associated with a new VM on the host defined in the given process.
   * @param remoteProcess the process that will originate the creation of the runtime
   * @exception java.io.IOException if the new VM cannot be created
   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
   */
  public void createVM(UniversalProcess remoteProcess) throws java.io.IOException,ProActiveException;


//  /**
//   * Returns all nodes known by this ProActiveRuntime on this VM
//   * @return all nodes known by this ProActiveRuntime on this VM
//   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
//   */
// // public Node[] getLocalNodes() throws ProActiveException;
  

  /**
   * Returns the name of all nodes known by this ProActiveRuntime on this VM
   * @return the name of all nodes known by this ProActiveRuntime on this VM
   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
   */
  public String[] getLocalNodeNames() throws ProActiveException;
  

//  /**
//   * Returns the url of the node with specified name known by this ProActiveRuntime on this VM
//   * @return the url of the node with specified name known by this ProActiveRuntime on this VM
//   * or null if such a node cannot be found.
//   * @param nodeName the name of the node.
//   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
//   */
//  public String getLocalNode(String nodeName) throws ProActiveException;
//  
//
//  /**
//   * Returns the url of the node with specified name or null if such a node cannot be found.
//   * @return the url of the node identified by the given name known by this ProActiveRuntime on this VM
//   * or null if such a node cannot be found.
//   * @param nodeName the name of the node
//   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
//   */
//  public String getNode(String nodeName) throws ProActiveException;
  
//  /**
//   * Returns the default Node name associated with this ProActiveRuntime on this VM or null
//   * if this node does not yet exist.
//   * @return the default Node associated with this ProActiveRuntime on this VM.
//   */
//  public String getDefaultNodeName() throws ProActiveException;
  /**
   * Returns the JVM information as one object. This method allows to 
   * retrieve all JVM information in one call to optimize performance.
   * @return the JVM information as one object
   */
  public VMInformation getVMInformation();
  
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Allows this ProactiveRuntime on this VM to register another ProActiveRuntime
   * @param proActiveRuntimeDist the remote ProactiveRuntime to register
   * @param proActiveRuntimeName the name of the remote ProActiveRuntime 
   * @param creatorID the name of the creator of the remote ProActiveRuntime
   * @param creationProtocol the protocol used to register the remote ProActiveRuntime when created
   */
  public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName, String creatorID, String creationProtocol);
  
  
  /**
   * Returns all the ProActiveRuntime registered on this ProActiveRuntime on this VM
   * @return all the ProActiveRuntime registered on this ProActiveRuntime on this VM
   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
   */
  public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException;
  
  /**
   * Returns the ProActiveRuntime of specified name
   * @param proActiveRuntimeName the name of the ProActiveruntime to return
   * @return the ProActiveRuntime of specified name
   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
   */
  public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException;
  
  /**
   * Kills this ProActiveRuntime and this VM
   */
  public void killRT() throws ProActiveException;
  
  /**
   * Returns the url of this ProActiveRuntime on the local or remote VM
   */
  public String getURL() throws ProActiveException;
  
  
	/**
	 * Returns all Active Objects deployed on the node with the given name on 
	 * this ProActiveRuntime
	 * @param nodeName the name of the node
	 * @return Object[] Active Objects deployed on this ProactiveRuntime
	 */
  public ArrayList getActiveObjects(String nodeName) throws ProActiveException;
  
  
  /**
	 * Returns all Active Objects with the specified class name, deployed on the node with the given name on 
	 * this ProActiveRuntime
	 * @param nodeName the name of the node
	 * @param classname class of the Active Objects to look for
	 * @return Object[] Active Objects of the specified class name deployed on this ProactiveRuntime
	 */
  public ArrayList getActiveObjects(String nodeName, String className) throws ProActiveException;
  
  
	/**
	 * Returns the VirtualNode with the given name
	 * @param VirtualNodeName the name of the VirtualNode to be acquired
	 * @return VirtualNode the virtualnode of the given name or null if such virtualNode
	 * does not exist. 
	 */
  public VirtualNode getVirtualNode(String virtualNodeName)throws ProActiveException;
  
  
	/**
	 * Registers the virtualNode of the given name in a registry such RMIRegistry or Jini Service Lookup
	 * @param virtualNodeName
	 */
  public void registerVirtualNode(String virtualNodeName,boolean replacePreviousBinding)throws ProActiveException ;
  
  
	/**
	 * Unregisters the VirtualNode of the given name from the local runtime.
	 * @param virtualNodeName the virtualNode to unregister.
	 * @throws ProActiveException if a problem occurs when trying to unregister the virtualNode
	 */
  public void unregisterVirtualNode(String virtualNodeName) throws ProActiveException;
  
  
  
  /**
   * <p>
   * This method is the basis for creating remote active objects. 
   * It receives a <code>ConstructorCall</code> that is the constructor call of the body
   * of the active object to create. Inside the parameters of this constructor call is 
   * the constructor call of the reified object. Upon execution of the constructor call of the 
   * body, the body holding a reference on the reified object will get created and returned.
   * </p><p>
   * The method returns a reference on the RMI stub of the newly created body.
   * </p>
   * @param nodeName the name of the node the newly created active object will be associated to
   * @param bodyConstructorCall the Constructor call allowing to create the body
   * @param isNodeLocal boolean. True if proxy and body are on the same vm, false otherwise
   * @return a stub on the newly created body.
   * @exception ProActiveException if a problem occurs due to the remote nature of this ProactiveRuntime
   * @exception ConstructorCallExecutionFailedException if the constructor call cannot be executed
   * @exception java.lang.reflect.InvocationTargetException if the java constructor execution failed
   */
  public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall,boolean isNodeLocal) throws ProActiveException,
              ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException;


  /**
   * <p>
   * This method is the basis for migrating active objects. 
   * It receives a <code>Body</code> that embbeds the reified object and its graph of
   * passive objects. Once transfered remotely using serialization, the body should restart
   * itself and perform all updates needed to be functionning.
   * </p><p>
   * The method returns a reference on the RMI stub of the migrated body.
   * </p>
   * @param nodeName the name of the node the newly created active object will be associated to
   * @param body the body of the active object migrating to this node.
   * @return a RMI stub on the migrated body.
   * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
   */
  public UniversalBody receiveBody(String nodeName, Body body) throws ProActiveException;
}
