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
import org.objectweb.proactive.core.rmi.RandomPortSocketFactory;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;

import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.DiscoveryEvent;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lease.Lease;
import net.jini.core.entry.Entry;
import net.jini.lookup.entry.Name;
import net.jini.discovery.DiscoveryListener;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lease.LeaseRenewalEvent;
import net.jini.lease.LeaseListener;

public class JiniNodeImpl extends java.rmi.server.UnicastRemoteObject
  implements JiniNode,
	     java.io.Serializable,
	     DiscoveryListener,
	     LeaseListener
{
  
  private static String[] LOCAL_URLS = { "///", "//localhost/", "//127.0.0.1/" };
  protected NodeInformation nodeInformation;
  // objet pas serializable donc on le met transient
  transient LeaseRenewalManager leaseManager = new LeaseRenewalManager();


  //
  // -- Constructors -----------------------------------------------
  //
  
  public JiniNodeImpl() throws java.rmi.RemoteException {
  }
  
  
  public JiniNodeImpl(String url) throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
    this(url, false);
  }


  /**
   * Creates a Node object, then bounds it in registry
   */
  public JiniNodeImpl(String url, boolean replacePreviousBinding) throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
    url = checkURL(url);
    try {
      nodeInformation = new NodeInformationImpl(url);
    } catch (java.net.UnknownHostException e) {
      throw new RemoteException("Host unknown in "+url, e);
    }
    
    LookupDiscovery discover = null;
    try{
      discover = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
    }
    catch (Exception e){
      System.err.println(e.toString());
    }

    discover.addDiscoveryListener(this);
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  

  
  
  //
  // -- Implements JiniNode -----------------------------------------------
  //

  public UniversalBody createBody(ConstructorCall c) throws ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException  {
    System.out.println ("JiniNodeImpl.createBody "+nodeInformation.getURL()+" -> new "+c.getTargetClassName());
    Body localBody = (Body)c.execute();
    System.out.println ("JiniNodeImpl.localBody created localBody="+localBody);
    return localBody.getRemoteAdapter();
  }


  public UniversalBody receiveBody(Body b) {
    System.out.println ("JiniNodeImpl.receiveBody "+nodeInformation.getURL());
    long startTime = System.currentTimeMillis();
    System.out.println(" --------- JiniNodeImpl: receiveBody() name set ");
    UniversalBody boa = b.getRemoteAdapter(); 
    long endTime = System.currentTimeMillis();
      System.out.println(" --------- JiniNodeImpl: receiveBody() finished after " + (endTime-startTime));
    return boa;
  }


  public UniqueID[] getActiveObjectIDs() {
    BodyMap knownBodies = AbstractBody.getLocalBodies();
    UniqueID[] uniqueIDs = new UniqueID[knownBodies.size()];
    int i = 0;
    java.util.Iterator bodiesIterator = knownBodies.bodiesIterator();
    while (bodiesIterator.hasNext()) {
      Body activeObjectBody = (Body) bodiesIterator.next();
      if (activeObjectBody.isActive()) {
        uniqueIDs[i] = activeObjectBody.getID();
        i++;
      }
    }
    if (i < knownBodies.size()) {
      UniqueID[] newUniqueIDs = new UniqueID[i];
      if (i > 0) {
        System.arraycopy(uniqueIDs, 0, newUniqueIDs, 0, i);
      }
      return newUniqueIDs;
    } else {
      return uniqueIDs;
    }
  }


  public NodeInformation getNodeInformation() {
    return nodeInformation;
  }
  //
  // -- Implements  DiscoveryListener,LeaseListener-----------------------------------------------
  //

 public void discovered( DiscoveryEvent evt) {
    ServiceRegistrar[] registrars =  evt.getRegistrars();
    // on cherche un registrar pour pouvoir s'enregistrer
    for (int n = 0; n<registrars.length; n++){
      ServiceRegistrar registrar = registrars[n];
      ServiceRegistration reg = null;
      try {
	// construction du service
	ServiceItem item = new ServiceItem(null,
					   this,
					   new Entry[] { new Name(nodeInformation.getURL())});
	reg = registrar.register(item, Lease.FOREVER);
      } catch (Exception e) {
	System.out.println("register exception "+e.toString());
	continue;
      }
      System.out.println(" service JiniNode Registered");
      // on lance le lease manager pour que l'objet puisse se reenregistrer
      leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
    }
 }

  public void discarded(DiscoveryEvent evt){
  }
  
  
  public void notify(LeaseRenewalEvent evt){
    System.out.println("Lease expired "+evt.toString());
  }

  
  //
  // -- PROTECTED METHODS -----------------------------------------------
  // 
  
  protected String checkURL(String url) throws java.rmi.RemoteException {
    String noProtocolURL = removeProtocol(url);
    if (noProtocolURL.charAt(noProtocolURL.length()-1) == '/')
      noProtocolURL = noProtocolURL.substring(0,noProtocolURL.length()-1);
    if (! noProtocolURL.startsWith("//"))
      throw new java.rmi.RemoteException("Cannot create the JiniNode, malformed node URL = "+url);
    return noProtocolURL;
  }


  //
  // -- PRIVATE METHODS - STATIC -----------------------------------------------
  //

  private static String removeProtocol(String url) {
    String tmp = url;
    //if it starts with jini we remove it
    if (url.startsWith("jini:")) {
      tmp = url.substring(5);
    }
    if (!tmp.startsWith("//")) {
      tmp = "//" + tmp;
    }
    return tmp;
  }


  //
  // -- INNER CLASSES  -----------------------------------------------
  //
  
  protected static class NodeInformationImpl implements NodeInformation, java.io.Serializable {
  
    private String nodeName;
    private String nodeURL;
      private int portNumber;
    //private String codebase;
    private java.net.InetAddress hostInetAddress;
    private java.rmi.dgc.VMID hostVMID;
    
    public NodeInformationImpl(String nodeURL) throws java.net.UnknownHostException {
      readHostAndNodeName(nodeURL);
      if (portNumber>0) {
	  this.nodeURL = "//"+hostInetAddress.getHostName()+":"+portNumber+"/"+nodeName;
      } else {
	  this.nodeURL = "//"+hostInetAddress.getHostName()+"/"+nodeName;
      } 
      
      
    
      this.hostVMID = UniqueID.getCurrentVMID();
      //this.codebase = System.getProperty("java.rmi.server.codebase");
    }
    
    
    //
    // -- PUBLIC METHODS  -----------------------------------------------
    //


    //
    // -- implements NodeInformation  -----------------------------------------------
    //

    public java.rmi.dgc.VMID getVMID() {
      return hostVMID;
    }


    public String getName() {
      return nodeName;
    }
    
    public String getProtocol() {
      return "jini";
    }

    public String getURL() {
	return nodeURL;
    }


    public java.net.InetAddress getInetAddress() {
      return hostInetAddress;
    }


    //
    // -- PRIVATE METHODS  -----------------------------------------------
    //

    private void readHostAndNodeName(String url) throws java.net.UnknownHostException {
      for (int i=0; i<LOCAL_URLS.length; i++) {
        if (url.toLowerCase().startsWith(LOCAL_URLS[i])) {
          // local url
          hostInetAddress = java.net.InetAddress.getLocalHost();
          nodeName = url.substring(LOCAL_URLS[i].length());
          return;
        }
      }
      // non local url
      int n = url.indexOf('/', 2); // looking for the end of the host
      if (n < 3) throw new java.net.UnknownHostException("Cannot determine the name of the host in this url="+url);
      String hostname =  removePortNumber(url.substring(2,n));
      hostInetAddress = java.net.InetAddress.getByName(hostname);
      nodeName =  url.substring(n+1);
    }
      // end inner class NodeInformationImpl

      private String removePortNumber(String url) {
	  int index = url.indexOf(":");
	  if (index > -1) {
	      this.portNumber=Integer.parseInt(url.substring(index+1,url.length()));
	      return url.substring(0,index);
	  }
	  return url;
      }
  }
}

