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
package org.objectweb.proactive.core.body.jini;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.reply.Reply;

import java.rmi.RemoteException;

/**
 *   An adapter for a JiniBody to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another jini objects library.
 */
public class JiniBodyAdapter implements UniversalBody, java.io.Serializable {

  /**
   * The encapsulated JiniBody
   */
  protected JiniBody proxiedJiniBody;
  
  /**
   * Cache the ID of the Body locally for speed
   */
  protected UniqueID bodyID;
  

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public JiniBodyAdapter() {
  }


  public JiniBodyAdapter(JiniBody jiniBody) throws ProActiveException {
    this.proxiedJiniBody = jiniBody;
    try {
      this.bodyID = jiniBody.getID();
    } catch (java.rmi.RemoteException e) {
      throw new ProActiveException(e);
    }
  }
  
  
  public JiniBodyAdapter(Body body) throws ProActiveException {
    try {
      this.proxiedJiniBody = new JiniBodyImpl(body);
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
   * jini version of the body of the active object that is registered into the 
   * RMI Registry under the given URL. 
   * @param obj the active object to register.
   * @param url the url under which the jini body is registered.
   * @exception java.io.IOException if the jini body cannot be registered
   */
  public static void register(JiniBodyAdapter bodyAdapter, String url) throws java.io.IOException {
    java.rmi.Naming.rebind(url, bodyAdapter.proxiedJiniBody);
  }
  

 /**
  * Unregisters an active object previously registered into a RMI registry.
  * @param url the url under which the active object is registered.
  * @exception java.io.IOException if the jini object cannot be removed from the registry
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
   * jini version of the body of an active object that can be registered into the 
   * RMI Registry under a given URL.
   * @param url the url the jini Body is registered to
   * @return a UniversalBody
   * @exception java.io.IOException if the jini body cannot be found under the given url
   *      or if the object found is not of type JiniBody
   */
  public static UniversalBody lookup(String url) throws java.io.IOException {
    Object o = null;
    // Try if URL is the address of a JiniBody
    try { 
      o = java.rmi.Naming.lookup(url);
    } catch (java.rmi.NotBoundException e) {
      throw new java.io.IOException("The url "+url+" is not bound to any known object");
    }
    if (o instanceof JiniBody) {
      try {
        return new JiniBodyAdapter((JiniBody)o);
      } catch (ProActiveException e) {
        throw new java.io.IOException("Cannot build a Jini Adapter"+ e.toString());
      }
    } else {
      throw new java.io.IOException("The given url does exist but doesn't point to a jini body  url="+url+" class found is "+o.getClass().getName());
    }
  }


  public boolean equals(Object o) {
    if (! (o instanceof JiniBodyAdapter)) return false;
    JiniBodyAdapter rba = (JiniBodyAdapter)o;
    return proxiedJiniBody.equals(rba.proxiedJiniBody);
  }

  
  public int hashCode() {
    return proxiedJiniBody.hashCode();
  }


  //
  // -- implements UniversalBody -----------------------------------------------
  //

  public void receiveRequest(Request r) throws java.io.IOException {
    proxiedJiniBody.receiveRequest(r);
  }


  public void receiveReply(Reply r) throws java.io.IOException {
    proxiedJiniBody.receiveReply(r);
  }


  public String getNodeURL() {
    try {
      return proxiedJiniBody.getNodeURL();
    } catch (java.rmi.RemoteException e) {
      return "cannot contact the body to get the nodeURL";
    }
  }


  public UniqueID getID() {
    return bodyID;
  }


  public void updateLocation(UniqueID id, UniversalBody jiniBody) throws java.io.IOException {
    proxiedJiniBody.updateLocation(id, jiniBody);
  }


  public UniversalBody getRemoteAdapter() {
    return this;
  }
  
  
  public void enableAC() throws java.io.IOException {
  	proxiedJiniBody.enableAC();
  }
  
  public void disableAC() throws java.io.IOException {
  	proxiedJiniBody.disableAC();
  }

  public void setImmediateService (String methodName) throws java.io.IOException {
  	proxiedJiniBody.setImmediateService(methodName);
  }

  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
}
