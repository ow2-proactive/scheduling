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
package org.objectweb.proactive.examples.hello;


import org.objectweb.proactive.ProActive;

public class SimpleAgent implements java.io.Serializable {

  public SimpleAgent() {
  }

  public void moveTo(String t) {
    try {
      System.out.println("Avant la migration ");
      ProActive.migrateTo(t);
      System.out.println("Apres la migration je suis sur "+whereAreYou());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String whereAreYou() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      return "Localhost lookup failed";
    }
  }

  public static void main (String[] args) {
    if (!(args.length>0)) {
      System.out.println("Usage: java org.objectweb.proactive.examples.hello.SimpleAgent hostname/NodeName ");
      System.exit(-1);
    }
 
    try {
      org.objectweb.proactive.core.node.NodeFactory.setFactory(org.objectweb.proactive.core.Constants.RMI_PROTOCOL_IDENTIFIER, "org.objectweb.proactive.core.node.rmi.RemoteNodeFactory");
      org.objectweb.proactive.core.node.NodeFactory.setFactory(org.objectweb.proactive.core.Constants.JINI_PROTOCOL_IDENTIFIER, "org.objectweb.proactive.core.node.jini.JiniNodeFactory");
      
    } catch (Exception exp) {
      System.out.println(exp.getMessage());
      exp.printStackTrace();
    }




    SimpleAgent t = null;
    try {
      // create the SimpleAgent in this JVM
      t = (SimpleAgent) ProActive.newActive("org.objectweb.proactive.examples.hello.SimpleAgent",null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // migrate the SimpleAgent to the location identified by the given node URL
    // we assume here that the node does already exist
    System.out.println("Je migre depuis " + t.whereAreYou());
    t.moveTo(args[0]);
    System.out.println("The Active Object is now on host (j'suis ici) : " + t.whereAreYou());
  }
}
