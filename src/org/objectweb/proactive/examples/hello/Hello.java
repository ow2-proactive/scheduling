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

/**
 * Our hello server
 */
public class Hello implements org.objectweb.proactive.RunActive, java.io.Serializable {

  private String name;
  private String nodeURL;
  private String hi = "Hello world";
  private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


  public Hello() {
  }


  public Hello(String name) {
    this.name = name;
  }


  public String sayHello() {
    return hi + " at " + dateFormat.format(new java.util.Date()) + "\n from \n" + nodeURL;
  }

  
  public void runActivity(org.objectweb.proactive.Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    nodeURL = body.getNodeURL();
    service.fifoServing();
  }

  
  public static void main(String[] args) {
    // Registers it with an URL
    try {
      // Creates an active instance of class HelloServer on the local node
      Hello hello = (Hello)org.objectweb.proactive.ProActive.newActive(Hello.class.getName(), new Object[]{"remote"});
      java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
      org.objectweb.proactive.ProActive.register(hello, "//" + localhost.getHostName() + "/Hello");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
}
