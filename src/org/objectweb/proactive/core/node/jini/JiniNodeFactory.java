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

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.entry.Name;
import org.objectweb.proactive.core.jini.ServiceLocatorHelper;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

public class JiniNodeFactory extends NodeFactory {

  protected final static int MAX_RETRY = 5;
  private  final static long WAITFOR = 100000L;

  protected java.util.Random random;
  protected static ServiceLocatorHelper serviceLocatorHelper = new ServiceLocatorHelper();


  static {
    System.out.println ("JiniNodeFactory created with "+JiniNodeFactory.class.getClassLoader().getClass().getName());
  }

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public JiniNodeFactory() throws java.io.IOException {
    // Obligatoire d'avoir le security manager fixe
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new java.rmi.RMISecurityManager());
    }
    random = new java.util.Random(System.currentTimeMillis());
    //System.out.println("------------------------ JiniNodeFactory ------------------------");
    serviceLocatorHelper.initializeServiceLocator();

    System.out.println ("ClassLoader is "+this.getClass().getClassLoader().getClass().getName());
  }



  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected Node createNodeImpl(String s, boolean replacePreviousBinding) throws NodeException {
    return createNodeAdapter(s, replacePreviousBinding);
  }


  protected Node createDefaultNodeImpl(String baseName) throws NodeException {
    int i = 0;
    while (true) {
      try {
        return createNodeAdapter(baseName + Integer.toString(random.nextInt()), false);
      } catch (NodeException e) {
        i++;
        if (i >= MAX_RETRY) throw e;
      }
    }
  }



  protected Node getNodeImpl(String s) throws NodeException {
    System.out.println("> JiniNodeFactory.getNodeImpl("+s+")");

    ServiceDiscoveryManager clientMgr = null;
    JiniNode jiniNode  = null;
    try {
      // recherche multicast
      LookupDiscoveryManager mgr = new LookupDiscoveryManager(LookupDiscovery.ALL_GROUPS,
                    null,null);

      clientMgr = new ServiceDiscoveryManager(mgr,
                new LeaseRenewalManager());
    } catch (Exception e) {
      throw new NodeException("Remote",e);
    }
    Class[] classes = new Class[] {JiniNode.class};
    Entry[] entries;
    if (s == null){
      entries = null;
    } else {
      entries=  new Entry[] { new Name(s)};
    }
    // construction de la template pour la recherche
    // on peut ne pas mettre le nom de l'objet
    // ensuite on recherche une certaine classe d'objet

    ServiceTemplate template = new ServiceTemplate(null,classes,entries);

    ServiceItem item = null;
    try {
      item = clientMgr.lookup(template, null, WAITFOR);
    } catch (Exception e) {
      throw new NodeException("Remote",e);
    }

    if (item == null) {
      System.out.println("no service found");
      return null;
    } else {
      jiniNode = (JiniNode) item.service;
      if (jiniNode == null) {
  System.out.println("item null");
  return null;
      }
      return createNodeAdapter(jiniNode);
    }
  }
  protected JiniNodeAdapter createNodeAdapter(JiniNode jiniNode) throws NodeException {
    return new JiniNodeAdapter(jiniNode);
  }


  protected JiniNodeAdapter createNodeAdapter(String jiniNodeName, boolean replacePreviousBinding) throws NodeException {
    return new JiniNodeAdapter(jiniNodeName, replacePreviousBinding);
  }


  public static void setMulticastLocator(boolean multicastLocator) {
    serviceLocatorHelper.setMulticastLocator(multicastLocator);
  }

  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

}




