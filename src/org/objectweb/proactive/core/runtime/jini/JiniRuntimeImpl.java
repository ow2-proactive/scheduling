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
package org.objectweb.proactive.core.runtime.jini;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.discovery.DiscoveryEvent;
import net.jini.lease.LeaseRenewalEvent;
import net.jini.lookup.entry.Name;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;

/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 * 	 @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */


public class JiniRuntimeImpl
  extends java.rmi.server.UnicastRemoteObject
  implements JiniRuntime, java.io.Serializable, net.jini.discovery.DiscoveryListener, net.jini.lease.LeaseListener {


	protected ProActiveRuntimeImpl proActiveRuntime;
	protected String proActiveRuntimeURL;
	//ServiceRegistar table used afterwards to register node service
	//Vector is used because the size is unknown and this class is synchronized
	protected java.util.Vector registrarsTable;
	
	//table used to handle node's registration when discovery event is received after the node's creation
	protected java.util.Hashtable jiniNodeMap;
	
	private boolean isRuntimeRegistered = false;
  // this object is not serializable
  protected transient net.jini.lease.LeaseRenewalManager leaseManager = new net.jini.lease.LeaseRenewalManager();

  //
  // -- Constructors -----------------------------------------------
  //

  public JiniRuntimeImpl() throws java.rmi.RemoteException {
  	this.proActiveRuntime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
  	this.proActiveRuntimeURL = buildRuntimeURL();
  	this.jiniNodeMap = new java.util.Hashtable();
  	this.registrarsTable = new java.util.Vector();
  	net.jini.discovery.LookupDiscovery discover = null;
    try {
      discover = new net.jini.discovery.LookupDiscovery(net.jini.discovery.LookupDiscovery.ALL_GROUPS);
      // stay around long enough to receice replies
      //Thread.currentThread().sleep(10000L);
    } catch (Exception e) {
      System.err.println(e.toString());
    }

    discover.addDiscoveryListener(this);
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  //
  // -- Implements JiniRuntime -----------------------------------------------
  //
  
  public String createLocalNode(String nodeName,boolean replacePreviousBinding) throws java.rmi.RemoteException 
	{
		//counter used to check that the node has been registered at 
		//least once as jini service
		int counter = 0;
		//wait until the discovered method is called
		//otherwise registars could be null, because it is 
		//another thread that fulfill this table
		while(!isRuntimeRegistered){}
		String nodeURL = null;
		//first we build a well-formed url
		//System.out.println("table size "+registrarsTable.size());
		try{
		nodeURL = buildNodeURL(nodeName);
		//then take the name of the node
		String name = UrlBuilder.getNameFromUrl(nodeURL);
		//System.out.println("name is : "+ name);
		//System.out.println("url is : "+ nodeURL);
		
		//create the node with the name 
		proActiveRuntime.createLocalNode(name,replacePreviousBinding);
		}catch (java.net.UnknownHostException e){
			throw new java.rmi.RemoteException("Host unknown in "+nodeURL,e);
		}
		ServiceID serviceID = newServiceID();
		//register it as a jini service with the url
		for (int n = 0; n < registrarsTable.size(); n++) {
    ServiceRegistrar registrar = (ServiceRegistrar)registrarsTable.get(n);
    ServiceRegistration reg = null;
    try {
      // construction du service
      //ServiceID serviceID = new ServiceID((new Long(nodeURL)).longValue(),(new Long("jini")).longValue());
      //ServiceID serviceID = new ServiceID(new DataInputStream(new InputStream()));
      //ServiceItem item = new ServiceItem(null, this, new Entry[] { new Name(nodeURL)});
			ServiceItem item = new ServiceItem(serviceID, this, new Entry[] { new Name(nodeURL)});
      reg = registrar.register(item, Lease.FOREVER);
      counter++;
    } catch (Exception e) {
      System.out.println("register exception " + e.toString());
      continue;
    }
    // if counter=0 no node are registered as jini Service
    if(counter == 0) throw new java.rmi.RemoteException("register exception ");
    System.out.println(" service JiniNode Registered " + nodeURL);
    //System.out.println("Registrar "+registrar.getLocator().getHost());
      // on lance le lease manager pour que l'objet puisse se reenregistrer
    leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
		}
		//register the node in the hashtable, hence if another lookup service is
		//discoverer after the node creation, the runtime will be able to register this node in the new 
		//lookup service
		jiniNodeMap.put(nodeURL,serviceID);
		return nodeURL;
		
	}


	public void DeleteAllNodes()
	{
		proActiveRuntime.DeleteAllNodes();
	}

	
	public void killNode(String nodeName)
	{
		proActiveRuntime.killNode(nodeName);
	}

	
//	public void createLocalVM(JVMProcess jvmProcess)
//		throws IOException
//	{
//	proActiveRuntime.createLocalVM(jvmProcess);
//	}

	
	public void createVM(UniversalProcess remoteProcess)
		throws IOException
	{
	 proActiveRuntime.createVM(remoteProcess);
	}

	
//	public Node[] getLocalNodes()
//	{
//		return proActiveRuntime.getLocalNodes(); 
//	}

	
	public String[] getLocalNodeNames()
	{
		return proActiveRuntime.getLocalNodeNames();
	}

	
//	public String getLocalNode(String nodeName)
//	{
//		return proActiveRuntime.getLocalNode(nodeName);
//	}
//
//	
//	public String getNode(String nodeName)
//	{
//		return proActiveRuntime.getNode(nodeName);
//	}
	
	
//	public String getDefaultNodeName(){
//		return proActiveRuntime.getDefaultNodeName();
//	}

	
	public VMInformation getVMInformation()
	{
		return proActiveRuntime.getVMInformation();
	}

	
	public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName, String creatorID, String creationProtocol)
	{
		proActiveRuntime.register(proActiveRuntimeDist,proActiveRuntimeName,creatorID,creationProtocol);
	}

	
	public ProActiveRuntime[] getProActiveRuntimes()
	{
		return proActiveRuntime.getProActiveRuntimes();
	}
	
	public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName){
		return proActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
	}
		


	
	public void killRT()
	{
		proActiveRuntime.killRT();
	}
	
	public String getURL(){
  		return proActiveRuntimeURL;
  	}

	public ArrayList getActiveObjects(String nodeName){
		return proActiveRuntime.getActiveObjects(nodeName);
	}
	
	
	public ArrayList getActiveObject(String nodeName, String objectName){
		return proActiveRuntime.getActiveObject(nodeName,objectName);
	}
	
	
	
	public UniversalBody createBody(String nodeName,ConstructorCall bodyConstructorCall,boolean isNodeLocal)
		throws
			ConstructorCallExecutionFailedException,
			InvocationTargetException
	{
		return proActiveRuntime.createBody(nodeName,bodyConstructorCall,isNodeLocal);
	}

	
	public UniversalBody receiveBody(String nodeName, Body body) 
	{ 
		return proActiveRuntime.receiveBody(nodeName,body);
	}



  //
  // -- Implements  DiscoveryListener,LeaseListener-----------------------------------------------
  //

  public void discovered(DiscoveryEvent evt) {
    ServiceRegistrar[] registrars = evt.getRegistrars();
    //System.out.println("NB registrar "+registrars.length);
    // on cherche un registrar pour pouvoir s'enregistrer
    for (int n = 0; n < registrars.length; n++) {
      ServiceRegistrar registrar = registrars[n];
      //System.out.println("Name registrar :"+registrars[n].getServiceID().toString());
      ServiceRegistration reg = null;
      try {
        // construction du service
        //ServiceItem item = new ServiceItem(null, this, new Entry[] { new Name(proActiveRuntimeURL)});
        ServiceItem item = new ServiceItem(newServiceID(), this, new Entry[] { new Name(proActiveRuntimeURL)});
        reg = registrar.register(item, Lease.FOREVER);
        
      } catch (Exception e) {
      	//e.printStackTrace();
        System.out.println("register exception " + e.toString());
        continue;
      }
      //System.out.println(" service JiniRuntime Registered");
      //add the registrar in the table for future use(node registration)
       registrarsTable.add(registrar);
       registerJiniNodes(registrar);
      // on lance le lease manager pour que l'objet puisse se reenregistrer
      leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
      isRuntimeRegistered = true;
    }
    //we put this line in order to avoid deadlock in createLocalNode
    //Hence if a problem occurs when registering the runtime, we can still try to register
    //it in the createLocalNode method and an exception will be thrown in this method 
    isRuntimeRegistered = true;
  }




  public void discarded(DiscoveryEvent evt) {}

  public void notify(LeaseRenewalEvent evt) {
    System.out.println("Lease expired " + evt.toString());
   evt.getException().printStackTrace();
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  protected ServiceID newServiceID() {
  		
  /** random number generator for UUID generation */
    SecureRandom secRand = new SecureRandom();
  /** 128-bit buffer for use with secRand */
    byte[] secRandBuf16 = new byte[16];
  /** 64-bit buffer for use with secRand */
    byte[] secRandBuf8 = new byte[8];

	secRand.nextBytes(secRandBuf16);
	secRandBuf16[6] &= 0x0f;
	secRandBuf16[6] |= 0x40; /* version 4 */
	secRandBuf16[8] &= 0x3f;
	secRandBuf16[8] |= 0x80; /* IETF variant */
	secRandBuf16[10] |= 0x80; /* multicast bit */
	long mostSig = 0;
	for (int i = 0; i < 8; i++) {
	    mostSig = (mostSig << 8) | (secRandBuf16[i] & 0xff);
	}
	long leastSig = 0;
	for (int i = 8; i < 16; i++) {
	    leastSig = (leastSig << 8) | (secRandBuf16[i] & 0xff);
	}
	return new ServiceID(mostSig, leastSig);

 }



  
  
  //
  // ---PRIVATE METHODS--------------------------------------
  //
  
  /**
 * Method registerJiniNodes.
 * @param registrar
 */
  
  //This method is very useful when the JiniRuntime receives event about a new Lookup service
  //that was discovered.In such case, the runtime registers all nodes previously created
  //as Jini service with the registrar given as parameter
	private void registerJiniNodes(ServiceRegistrar registrar)
	{
		ServiceRegistration reg = null;
		if(!jiniNodeMap.isEmpty()){
			synchronized(jiniNodeMap){
				for(java.util.Enumeration e = jiniNodeMap.keys();e.hasMoreElements();){
					String nodeURL = (String)e.nextElement();
					ServiceItem item = new ServiceItem((ServiceID)(jiniNodeMap.get(nodeURL)),this, new Entry[] { new Name(nodeURL)});
					try{
					reg = registrar.register(item, Lease.FOREVER);
					}catch (Exception ex) {
      			System.out.println("register exception " + ex.toString());
      			continue;
    			}
    			System.out.println(" service JiniNode Registered " + nodeURL);
//    			try{
//    			System.out.println("Registrar "+registrar.getLocator().getHost());
//    			}catch(Exception et){
//    				et.printStackTrace();
//    			}
      		// on lance le lease manager pour que l'objet puisse se reenregistrer
    			leaseManager.renewUntil(reg.getLease(), Lease.FOREVER, this);
				}
			}
		}
	}
  
  private String buildRuntimeURL() {
  	String host = getVMInformation().getInetAddress().getHostName();
  	String name = getVMInformation().getName();
  	return UrlBuilder.buildUrl(host,name);
  }
  
  
  private String buildNodeURL(String url) throws java.net.UnknownHostException{
  	int i = url.indexOf('/');
  	if (i == -1){
  		//it is an url given by a descriptor
  		String host = getVMInformation().getInetAddress().getHostName();
  		return UrlBuilder.buildUrl(host,url);
  	}else{
  		return UrlBuilder.checkUrl(url);
  	}
  }
  
}