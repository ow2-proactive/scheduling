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

import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;

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
 * <li>the creation of another VM through the method <code>createLocalVM</code></li>
 * <li>the creation of remote VM through the method <code>createRemoteVM</code></li>
 * </ul>
 * </p><p>
 * We expect several concrete implementations of the Node to be written such as a RMI ProActiveRuntime, a JINI ProActiveRuntime ...
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.91
 *
 */
public interface ProActiveRuntime {

  /**
   * Creates a new Node in the same VM as this ProActiveRuntime
   * @param nodeName the name of the node to create localy
   * @return the node newly created in the target VM
   * @exception NodeException if the new node cannot be created
   */
  public Node createLocalNode(String nodeName) throws NodeException;
  

  /**
   * Creates a new Node in a new VM on the same host as this ProActiveRuntime.
   * @param nodeName the name of the node to spawn localy
   * @return the node newly created in the target VM
   * @exception java.io.IOException if the new VM cannot be created
   */
  public ProActiveRuntime createLocalVM(JVMProcess jvmProcess) throws java.io.IOException;
  

  /**
   * Creates a new Node in a new VM on the specified host.
   * @param address the address of the host where to create the VM
   * @return the newly created ProActiveRutime on the given host
   * @exception java.io.IOException if the new VM cannot be created
   */
  public ProActiveRuntime createRemoteVM(UniversalProcess remoteProcess) throws java.io.IOException;


  /**
   * Returns all nodes known by this ProActiveRuntime on this VM
   * @return all nodes known by this ProActiveRuntime on this VM
   */
  public Node[] getLocalNodes();
  

  /**
   * Returns all the name of all nodes known by this ProActiveRuntime on this VM
   * @return all the name of all nodes known by this ProActiveRuntime on this VM
   */
  public String[] getLocalNodeNames();
  

  /**
   * Returns the node of specified name known by this ProActiveRuntime on this VM
   * @return the node of specified name known by this ProActiveRuntime on this VM
   * or null if such a node cannot be found.
   */
  public Node getLocalNode(String nodeName);
  

  /**
   * Returns the node of specified url
   * @return the node of specified name known by this ProActiveRuntime on this VM
   * or null if such a node cannot be found.
   */
  public Node getNode(String nodeURL);
  

  /**
   * Returns the node information as one object. This method allows to 
   * retrieve all node information in one call to optimize performance.
   * @return the node information as one object
   */
  public VMInformation getVMInformation();


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
   * @param <code>nodeName</code> the name of the node the newly created active object will be associated to
   * @param <code>bodyConstructorCall</code> the Constructor call allowing to create the body
   * @return a stub on the newly created body.
   * @exception NodeException if a problem occurs due the remote nature of the node
   * @exception ConstructorCallExecutionFailedException if the constructor call cannot be executed
   * @exception java.lang.reflect.InvocationTargetException if the java constructor execution failed
   */
  public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall) throws NodeException,
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
   * @param <code>nodeName</code> the name of the node the newly created active object will be associated to
   * @param <code>body</code> the body of the active object migrating to this node.
   * @return a RMI stub on the migrated body.
   * @exception NodeException if a problem occurs due the remote nature of the node
   */
  public UniversalBody receiveBody(String nodeName, Body body) throws NodeException;
}
