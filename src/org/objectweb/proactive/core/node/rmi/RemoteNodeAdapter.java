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
package org.objectweb.proactive.core.node.rmi;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;

public class RemoteNodeAdapter implements Node, java.io.Serializable {

  protected RemoteNode remoteNode;
  protected NodeInformation nodeInformation;

  //
  // -- Constructors -----------------------------------------------
  //

  protected RemoteNodeAdapter() {
  }


  public RemoteNodeAdapter(String s) throws NodeException {
    this(s, false);
  }


  public RemoteNodeAdapter(String s, boolean replacePreviousBinding) throws NodeException {
    try {
      this.remoteNode = createRemoteNode(s, replacePreviousBinding);
      this.nodeInformation = remoteNode.getNodeInformation();
    } catch (java.rmi.RemoteException e) {
      throw new NodeException("Cannot get the NodeInformation of the node", e);
    } catch (java.rmi.AlreadyBoundException e) {
      throw new NodeException("Cannot bound the node to "+s,e);
    }
  }


  public RemoteNodeAdapter(RemoteNode r) throws NodeException {
    this.remoteNode = r;
    try {
      this.nodeInformation = remoteNode.getNodeInformation();
    } catch (java.rmi.RemoteException e) {
      throw new NodeException("Cannot get the NodeInformation of the node", e);
    }
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public boolean equals(Object o) {
    if (! (o instanceof RemoteNodeAdapter)) return false;
    RemoteNodeAdapter node = (RemoteNodeAdapter)o;
    return remoteNode.equals(node.remoteNode);
  }

  public int hashCode() {
    return remoteNode.hashCode();
  }



  //
  // -- Implements Node -----------------------------------------------
  //

  public UniversalBody createBody(ConstructorCall bodyConstructorCall) throws NodeException,
                  ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException {
    try {
      return remoteNode.createBody(bodyConstructorCall);
    } catch (java.rmi.RemoteException e) {
      throw new NodeException(e);
    }
  }


  public UniversalBody receiveBody(Body body) throws NodeException {
    try {
      return remoteNode.receiveBody(body);
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

  protected RemoteNode createRemoteNode(String remoteNodeName, boolean replacePreviousBinding) throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
    return new RemoteNodeImpl(remoteNodeName, replacePreviousBinding);
  }
}
