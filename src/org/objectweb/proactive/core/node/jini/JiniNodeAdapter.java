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
package org.objectweb.proactive.core.node.jini;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.jini.JiniBodyAdapter;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;

public class JiniNodeAdapter implements Node, java.io.Serializable {

  protected JiniNode jiniNode;
  protected NodeInformation nodeInformation;

  //
  // -- Constructors -----------------------------------------------
  //
  
  protected JiniNodeAdapter() {
  }
  
  
  public JiniNodeAdapter(String s) throws NodeException {
    this(s, false);
  }
  
  
  public JiniNodeAdapter(String s, boolean replacePreviousBinding) throws NodeException {
    try {
      this.jiniNode = createJiniNode(s, replacePreviousBinding);
      this.nodeInformation = jiniNode.getNodeInformation();
    } catch (java.rmi.RemoteException e) {
      throw new NodeException("Cannot get the NodeInformation of the node", e);
    } catch (java.rmi.AlreadyBoundException e) {
      throw new NodeException("Cannot bound the node to "+s,e);
    }
  }


  public JiniNodeAdapter(JiniNode r) throws NodeException {
    this.jiniNode = r;
    try {
      this.nodeInformation = jiniNode.getNodeInformation();
    } catch (java.rmi.RemoteException e) {
      throw new NodeException("Cannot get the NodeInformation of the node", e);
    }
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  public boolean equals(Object o) {
    if (! (o instanceof JiniNodeAdapter)) return false;
    JiniNodeAdapter node = (JiniNodeAdapter)o;
    return jiniNode.equals(node.jiniNode);
    //return nodeInformation.getInetAddress().equals(node.getNodeInformation().getInetAddress()) 
    //    && nodeInformation.getName().equals(node.getNodeInformation().getName());
  }
  
  public int hashCode() {
    return jiniNode.hashCode();
  }



  //
  // -- Implements Node -----------------------------------------------
  //

  public UniversalBody createBody(ConstructorCall bodyConstructorCall) throws NodeException,
                  ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException {
    try {
      return jiniNode.createBody(bodyConstructorCall);
    } catch (java.rmi.RemoteException e) {
      throw new NodeException(e);
    }
  }


  public UniversalBody receiveBody(Body body) throws NodeException {
    try {
      return jiniNode.receiveBody(body);
    } catch (java.rmi.RemoteException e) {
      throw new NodeException(e);
    }
  }
  
  public UniqueID[] getActiveObjectIDs() throws NodeException {
    try {
      return jiniNode.getActiveObjectIDs();
    } catch (java.rmi.RemoteException e) {
      throw new NodeException(e);
    }
  }


  public NodeInformation getNodeInformation() {
    return nodeInformation;
  }



  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  protected JiniNode createJiniNode(String jiniNodeName, boolean replacePreviousBinding) throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
    return new JiniNodeImpl(jiniNodeName, replacePreviousBinding);
  }
}
