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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;

/**
 * <p>
 * A <code>Node</code> offers a set of services needed by ProActive to work with
 * remote JVM. Each JVM that is aimed to hold active objects should contains at least
 * one instance of the node class. That instance, when created, will register itself
 * to some registry where it is possible to perform a lookup (such as the RMI registry).
 * </p><p>
 * When ProActive needs to interact with a remote JVM, it will lookup for one node associated
 * with that JVM (using typically the RMI Registry) and use the remote interface of the node
 * to perform the interaction.
 * </p><p>
 * Aside the getter giving information about the node, the 2 services offered are :
 * <ul>
 * <li>the creation of active object through the method <code>createBody</code></li>
 * <li>the migration of active object through the method <code>receiveBody</code></li>
 * </ul>
 * </p><p>
 * We expect several concrete implementations of the Node to be wrtten such as a RMI node, a JINI node ...
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface Node {

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
   * @param bodyConstructorCall the Constructor call allowing to create the body
   * @return a stub on the newly created body.
   * @exception NodeException if a problem occurs due the remote nature of the node
   * @exception ConstructorCallExecutionFailedException if the constructor call cannot be executed
   * @exception java.lang.reflect.InvocationTargetException if the java constructor execution failed
   */
  public UniversalBody createBody(ConstructorCall bodyConstructorCall) throws NodeException,
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
   * @param <code>body</code> the body of the active object migrating to this node.
   * @return a RMI stub on the migrated body.
   * @exception NodeException if a problem occurs due the remote nature of the node
   */
  public UniversalBody receiveBody(Body body) throws NodeException;


  /**
   * Returns the node information as one object. This method allows to
   * retrieve all node information in one call to optimize performance.
   * @return the node information as one object
   */
  public NodeInformation getNodeInformation();

}
