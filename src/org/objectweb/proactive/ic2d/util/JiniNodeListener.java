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
package org.objectweb.proactive.ic2d.util;

import java.rmi.RemoteException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.node.jini.JiniNode;
import org.objectweb.proactive.core.node.jini.JiniNodeAdapter;

import java.rmi.RMISecurityManager;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryEvent;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lease.Lease;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lease.LeaseListener;
import net.jini.lease.LeaseRenewalEvent;
import net.jini.core.entry.Entry;
import net.jini.lookup.entry.Name;



public class JiniNodeListener  implements DiscoveryListener {

  protected java.util.ArrayList nodes = new java.util.ArrayList();
  private String host;
  private IC2DMessageLogger logger;
  
  public JiniNodeListener() {
    this(null);
    
    
  }
  
  public JiniNodeListener(String _host) {    
    host = _host;
    System.setSecurityManager(new RMISecurityManager());
    this.logger = logger;
    LookupDiscovery discover = null;
    
    try{
      discover = new LookupDiscovery(LookupDiscovery.ALL_GROUPS);
	} catch (Exception e){
	  System.err.println(" JiniNodeFinder exception");
	  e.printStackTrace();
	}
    
    discover.addDiscoveryListener(this);
    
  }
    
    
    public void discovered(DiscoveryEvent evt) {

	ServiceRegistrar[] registrars = evt.getRegistrars();
	Class[] classes = new Class[] {JiniNode.class};
	JiniNode node = null;
	ServiceMatches matches = null;
	ServiceTemplate template= new ServiceTemplate(null, classes, null);
	NodeInformation info = null;

	for (int n=0; n<registrars.length; n++){
	  //System.out.println("JiniNodeListener:  Service found");
	    ServiceRegistrar registrar = registrars[n];
	    try{
	      //System.out.println("JiniNodeListener:  lookup registrar");
	      matches = registrar.lookup(template,Integer.MAX_VALUE);
	      if (matches.totalMatches >0 ){
		//System.out.println("JiniNodeListener: JiniNode trouvee");
		for (int i=0;i< matches.items.length; i++) {
		  try{
		    if (matches.items[i].service == null) {
		      //System.out.println("Service : NULL !!!");	
		    } else {
		      node = (JiniNode) matches.items[i].service;
		      //System.out.println("JiniNodeListener: node "+node);
		      info = node.getNodeInformation();
		      if (info != null){
			//System.out.println("JiniNodeListener: Node name "+info.getName());
			//System.out.println("JiniNodeListener: Inet Address "+info.getInetAddress());
			try {
			  //System.out.println("JiniNodeListener: on gere le host");
			  if (host != null){
			    //System.out.println("host non null: "+host+"  "+info.getInetAddress().getHostName());
			    if (info.getInetAddress().getHostName().equals(host)){
			      //System.out.println("JiniNodeListener: ajout du noeud pour le host "+host);
			      nodes.add(new JiniNodeAdapter(node));
			    }
			  } else {
			    //System.out.println("host null: ");
			    //System.out.println("JiniNodeListener: ajout du noeud");
			    nodes.add(new JiniNodeAdapter(node));
			  }
			} catch (org.objectweb.proactive.core.node.NodeException e){
			  e.printStackTrace();
			}
		      }
		    }
		  } catch (java.rmi.ConnectException e) {
		    System.err.println("JiniNodeListener ConnectException ");
		    continue;
		    //e.printStackTrace();
		  } 
		}
	      } else{
		//System.out.println("JiniNodeListener: Pas de JiniNode");
	      }
	    } catch (java.rmi.RemoteException e) {
	      //System.err.println("JiniNodeListener RemoteException ");
	      continue;
	      //e.printStackTrace();
	    }
	} 
	
	//System.out.println("JiniNodeListener: Fin recherche multicast");
    }
  
  
  

  public void discarded(DiscoveryEvent evt){
  }
    

  public java.util.ArrayList getNodes(){
    return nodes;
  }


  public static void main(String args[]) {
    JiniNodeListener jnf = new JiniNodeListener(null);
    // stay around long enough to receive replies
    try {
      Thread.currentThread().sleep(100000L);
    } catch(java.lang.InterruptedException e) {
      // do nothing
    }
    
  }
    
    
}












