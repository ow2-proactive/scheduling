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

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.entry.Name;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jini.ServiceLocatorHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;


public class JiniRuntimeFactory extends RuntimeFactory {

  protected static int MAX_RETRY = 3;
  private  final static long WAITFOR = 100000L;

  protected java.util.Random random;
  protected static ServiceLocatorHelper serviceLocatorHelper = new ServiceLocatorHelper();
	private static ProActiveRuntime defaultJiniRuntime = null;

	private int count;

  static {
    System.out.println ("JiniRuntimeFactory created with "+JiniRuntimeFactory.class.getClassLoader().getClass().getName());
  }

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public JiniRuntimeFactory() throws java.io.IOException {
    // Obligatoire d'avoir le security manager fixe
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new java.rmi.RMISecurityManager());
    }
    random = new java.util.Random(System.currentTimeMillis());
    //serviceLocatorHelper.initializeServiceLocator();
  }



  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  protected synchronized ProActiveRuntime getProtocolSpecificRuntimeImpl() throws ProActiveException {
     //return createRuntimeAdapter(s,false);
     if (defaultJiniRuntime == null)
     {
     	serviceLocatorHelper.initializeServiceLocator();
     	defaultJiniRuntime = createRuntimeAdapter();
     }
 
     return defaultJiniRuntime;
  }
  
  
  protected ProActiveRuntime getRemoteRuntimeImpl(String s) throws ProActiveException {
  	System.out.println("> JiniRuntimeFactory.getJiniRuntimeImpl("+s+")");
  	String host = null;
  	Entry[] entries;
  	JiniRuntime jiniRuntime  = null;
  	LookupLocator lookup = null;
  	ServiceRegistrar registrar = null;
  	try{
  	host = UrlBuilder.getHostNameFromUrl(s);
  	System.out.println("Try to find the service lookup on host: "+host);
  	}catch(java.net.UnknownHostException e){
  		System.out.println("Unable to locate host");
  		e.printStackTrace();
  	}
  	//ServiceDiscoveryManager clientMgr = null;
    
    if(host != null){
    	// recherche unicast
    	try {
        	lookup = new LookupLocator("jini://" + host);
        	System.out.println("Service lookup found ");
        	registrar = lookup.getRegistrar();
   	 	}catch (java.net.MalformedURLException e) {
        	throw new ProActiveException("Lookup failed: " + e.getMessage() );
    	}catch (java.io.IOException e) {
        	System.err.println("Registrar search failed: " + e.getMessage() );
        	if (MAX_RETRY-- > 0) {
        		System.out.println("failed to contact the service lookup, retrying ...");
        		getRemoteRuntimeImpl(s);
        	}else {
          		throw new ProActiveException("Cannot contact the lookup service for node "+s);
       	 	}
    	}catch (java.lang.ClassNotFoundException e) {
    		throw new ProActiveException("Registrar search failed: " + e.toString());
      	}
    	Class[] classes = new Class[] {JiniRuntime.class};
    	
      	entries=  new Entry[] { new Name(s)};
   		
    	ServiceTemplate template = new ServiceTemplate(null,classes,entries);
    	try{
    		jiniRuntime = (JiniRuntime)registrar.lookup(template);
    		if(jiniRuntime == null) System.out.println("No service found for url: "+s);
    		return createRuntimeAdapter(jiniRuntime);
    	}catch(java.rmi.RemoteException e){
    		throw new ProActiveException(e);
    		//e.printStackTrace();
    	}
    }else{
    	throw new ProActiveException("node url should not be null");
    }
//    try {
//      // recherche multicast
//      LookupDiscoveryManager mgr = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS,
//                    null,null);
//
//      clientMgr = new ServiceDiscoveryManager(mgr,new LeaseRenewalManager());
//    } catch (Exception e) {
//      throw new ProActiveException("Remote",e);
//    }
//    Class[] classes = new Class[] {JiniRuntime.class};
    
    // construction de la template pour la recherche
    // on peut ne pas mettre le nom de l'objet
    // ensuite on recherche une certaine classe d'objet
    
    
//    ServiceItem item = null;
//  	try {
//      item = clientMgr.lookup(template, null, WAITFOR);
//    } catch (Exception e) {
//      throw new ProActiveException("Remote",e);
//    }
//
//    if (item == null) {
//      System.out.println("no service found");
//      return null;
//    } else {
//      jiniRuntime = (JiniRuntime) item.service;
//      if (jiniRuntime == null) {
//  System.out.println("item null");
//  return null;
//      }
//      return createRuntimeAdapter(jiniRuntime);
//    }
  }
  
  
  protected JiniRuntimeAdapter createRuntimeAdapter(JiniRuntime jiniRuntime) throws ProActiveException {
    return new JiniRuntimeAdapter(jiniRuntime);
  }
  
  protected JiniRuntimeAdapter createRuntimeAdapter() throws ProActiveException {
    return new JiniRuntimeAdapter();
  }
  
  
  public static void setMulticastLocator(boolean multicastLocator) {
    serviceLocatorHelper.setMulticastLocator(multicastLocator);
  }


}




