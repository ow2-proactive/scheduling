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
package org.objectweb.proactive.core.body.proxy;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.NodeException;

import java.lang.reflect.InvocationTargetException;

public class UniversalBodyProxy extends AbstractBodyProxy implements java.io.Serializable {

  // note that we do not want to serialize this member but rather handle
  // the serialization by ourselve
  protected transient UniversalBody universalBody;
  
  protected transient boolean isLocal;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
 
  /**
   * Empty, no args constructor
   */
  public UniversalBodyProxy() {
  }
  
  
  /**
   * Instantiates an object of class BodyProxy, creates a body object
   * (referenced either via the instance variable <code>localBody</code>
   * or <code>remoteBody</code>) and passes the ConstructorCall
   * object <code>c</code> to the body, which will then handle
   * the creation of the reified object (That's it !).
   * parameter contains either :
   *    &lt;bodyClass, Node>
   * or 
   *    &lt;universal body>
   */
  public UniversalBodyProxy(ConstructorCall constructorCall, Object[] parameters) throws ProActiveException {
    Object p0 = parameters[0];
    // Determines whether the body is local or remote
    if (p0 instanceof UniversalBody) {
      // This is simple connection to an existant local body
      this.universalBody = (UniversalBody) p0;
      this.bodyID = universalBody.getID();
      isLocal = AbstractBody.getLocalActiveBody(bodyID) != null;
      //System.out.println("UniversalBodyProxy created from UniversalBody bodyID="+bodyID+" isLocal="+isLocal);
    } else  {
      // instantiate the body locally or remotely
      Class bodyClass = (Class) p0;
      Node node = (Node) parameters[1];
      if (NodeFactory.isNodeLocal(node)) {
        // the node is local
        this.universalBody = createLocalBody(constructorCall, bodyClass, node);
        isLocal = true;
      } else {
        this.universalBody = createRemoteBody(constructorCall, bodyClass, node);
        isLocal = false;
      }
      this.bodyID = universalBody.getID();
      //System.out.println("UniversalBodyProxy created from constructorCall bodyID="+bodyID+" isLocal="+isLocal);
    }
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  public boolean equals(Object o) {
    if (! (o instanceof UniversalBodyProxy)) return false;
    UniversalBodyProxy proxy = (UniversalBodyProxy) o;
    return universalBody.equals(proxy.universalBody);
  }
  
  public int hashCode() {
    return universalBody.hashCode();
  }


  //
  // -- implements BodyProxy interface -----------------------------------------------
  //

  public UniversalBody getBody() {
    return universalBody;
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  protected UniversalBody createLocalBody(ConstructorCall reifiedObjectConstructorCall, Class bodyClass, Node node) throws ProActiveException {
    try {
      ConstructorCall bodyConstructorCall = findBodyConstructorCall(bodyClass, node.getNodeInformation().getURL(), reifiedObjectConstructorCall);
      reifiedObjectConstructorCall.makeDeepCopyOfArguments();
      return (UniversalBody) bodyConstructorCall.execute();
      //System.out.println("LocalBodyProxy created using " + body + " from ConstructorCall");
    } catch (ConstructorCallExecutionFailedException e) {
      throw new ProActiveException(e);
    } catch (InvocationTargetException e) {
      throw new ProActiveException(e.getTargetException());
    } catch (java.io.IOException e) {
      throw new ProActiveException("Error in the copy of the arguments of the constructor", e);
    }
  }
  
  protected UniversalBody createRemoteBody(ConstructorCall reifiedObjectConstructorCall, Class bodyClass, Node node) throws ProActiveException {
    try {
      ConstructorCall bodyConstructorCall = findBodyConstructorCall(bodyClass, node.getNodeInformation().getURL(), reifiedObjectConstructorCall);
      //System.out.println("UniversalBodyProxy.createRemoteBody bodyClass="+bodyClass+"  node="+node);
      return node.createBody(bodyConstructorCall);
      //System.out.println("RemoteBodyProxy created bodyID=" + bodyID + " from ConstructorCall");
    } catch (ConstructorCallExecutionFailedException e) {
      throw new ProActiveException(e);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw new ProActiveException(e);
    } catch (NodeException e) {
      throw new ProActiveException(e);
    }
  }
  
  protected void sendRequest(MethodCall methodCall, Future future) throws java.io.IOException {
    // Determines the body that is at the root of the subsystem from which the
    // call was sent.
    // It is always true that the body that issued the request (and not the body
    // that is the target of the call) and this BodyProxy are in the same
    // address space because being a local representative for something remote
    // is what the proxy is all about. This is why we know that the table that
    // can be accessed by using a static methode has this information.
    Body sourceBody = AbstractBody.getThreadAssociatedBody();
    // Now we check whether the reference to the remoteBody has changed i.e the body has migrated
    // Maybe we could use some optimisation here
    UniversalBody newBody = sourceBody.checkNewLocation(universalBody.getID());
    if (newBody != null) {
      universalBody = newBody;
      isLocal = AbstractBody.getLocalActiveBody(bodyID) != null;
    }
    if (isLocal) {
      // Replaces the effective arguments with a deep copy
      // Only do this if the body is local
      // For remote bodies, this is automatically handled by the RMI stub
      methodCall.makeDeepCopyOfArguments();
    }
    sendRequestInternal(methodCall ,future, sourceBody);
  }

  protected void sendRequestInternal(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException {
    sourceBody.sendRequest(methodCall, future, universalBody, null);
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
  //
  // -- SERIALIZATION -----------------------------------------------
  //
  
  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    out.writeObject(universalBody.getRemoteAdapter());
  }


  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    Body localBody = AbstractBody.getLocalActiveBody(bodyID);
    if (localBody != null)  {
      // the body is local
      universalBody = localBody;
      isLocal = true;
    } else {
      // the body is not local
      universalBody = (UniversalBody) in.readObject();
      isLocal = false;
    }
  }
}


  
