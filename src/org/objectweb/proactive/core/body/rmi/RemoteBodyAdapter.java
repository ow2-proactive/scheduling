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
package org.objectweb.proactive.core.body.rmi;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.reply.Reply;

import java.rmi.RemoteException;

/**
 *   An adapter for a RemoteBody to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another remote objects library.
 */
public class RemoteBodyAdapter implements UniversalBody, java.io.Serializable {

  /**
   * The encapsulated RemoteBody
   */
  protected RemoteBody proxiedRemoteBody;
  
  /**
   * Cache the ID of the Body locally for speed
   */
  protected UniqueID bodyID;
  

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public RemoteBodyAdapter() {
  }


  public RemoteBodyAdapter(RemoteBody remoteBody) throws ProActiveException {
    this.proxiedRemoteBody = remoteBody;
    try {
      this.bodyID = remoteBody.getID();
    } catch (java.rmi.RemoteException e) {
      throw new ProActiveException(e);
    }
  }
  
  
  public RemoteBodyAdapter(UniversalBody body) throws ProActiveException {
    try {
      this.proxiedRemoteBody = new RemoteBodyImpl(body);
    } catch (java.rmi.RemoteException e) {
      throw new ProActiveException(e);
    }
    this.bodyID = body.getID();
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  /**
   * Registers an active object into a RMI registry. In fact it is the
   * remote version of the body of the active object that is registered into the 
   * RMI Registry under the given URL. 
   * @param obj the active object to register.
   * @param url the url under which the remote body is registered.
   * @exception java.io.IOException if the remote body cannot be registered
   */
  public static void register(RemoteBodyAdapter bodyAdapter, String url) throws java.io.IOException {
    java.rmi.Naming.rebind(url, bodyAdapter.proxiedRemoteBody);
  }
  

 /**
  * Unregisters an active object previously registered into a RMI registry.
  * @param url the url under which the active object is registered.
  * @exception java.io.IOException if the remote object cannot be removed from the registry
  */
  public static void unregister(String url) throws java.io.IOException {
    try {
      java.rmi.Naming.unbind(url);
    } catch (java.rmi.NotBoundException e) {
      throw new java.io.IOException("No object is bound to the given url : " + url);
    }
  }
  
  
  /**
   * Looks-up an active object previously registered in a RMI registry. In fact it is the
   * remote version of the body of an active object that can be registered into the 
   * RMI Registry under a given URL.
   * @param url the url the remote Body is registered to
   * @return a UniversalBody
   * @exception java.io.IOException if the remote body cannot be found under the given url
   *      or if the object found is not of type RemoteBody
   */
  public static UniversalBody lookup(String url) throws java.io.IOException {
    Object o = null;
    // Try if URL is the address of a RemoteBody
    try { 
      o = java.rmi.Naming.lookup(url);
    } catch (java.rmi.NotBoundException e) {
      throw new java.io.IOException("The url "+url+" is not bound to any known object");
    }
    if (o instanceof RemoteBody) {
      try {
        return new RemoteBodyAdapter((RemoteBody)o);
      } catch (ProActiveException e) {
        throw new java.io.IOException("Cannot build a Remote Adapter"+ e.toString());
      }
    } else {
      throw new java.io.IOException("The given url does exist but doesn't point to a remote body  url="+url+" class found is "+o.getClass().getName());
    }
  }


  public boolean equals(Object o) {
    if (! (o instanceof RemoteBodyAdapter)) return false;
    RemoteBodyAdapter rba = (RemoteBodyAdapter)o;
    return proxiedRemoteBody.equals(rba.proxiedRemoteBody);
  }

  
  public int hashCode() {
    return proxiedRemoteBody.hashCode();
  }


  //
  // -- implements UniversalBody -----------------------------------------------
  //

  public void receiveRequest(Request r) throws java.io.IOException {
    proxiedRemoteBody.receiveRequest(r);
  }


  public void receiveReply(Reply r) throws java.io.IOException {
    proxiedRemoteBody.receiveReply(r);
  }


  public String getNodeURL() {
    try {
      return proxiedRemoteBody.getNodeURL();
    } catch (java.rmi.RemoteException e) {
      return "cannot contact the body to get the nodeURL";
    }
  }


  public UniqueID getID() {
    return bodyID;
  }


  public void updateLocation(UniqueID id, UniversalBody remoteBody) throws java.io.IOException {
    proxiedRemoteBody.updateLocation(id, remoteBody);
  }


  public UniversalBody getRemoteAdapter() {
    return this;
  }
  

  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
}
