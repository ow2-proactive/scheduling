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
package org.objectweb.proactive.core.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Random;

/**
 * This factory creates server socket with randomly choosen port number
 * it tries 5 different ports before reporting a failure
 */
public class RandomPortSocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable {

  static protected final int MAX = 5;
  static protected Random random = new Random();
  //static private RMISocketFactory factory = RMISocketFactory.getDefaultSocketFactory();

  protected int basePort = 35000;
  protected int range = 5000;

  public RandomPortSocketFactory() {
    //	System.out.println("RandomPortSocketFactory constructor()");
  }


  public RandomPortSocketFactory(int basePort, int range) {
    //	System.out.println("RandomPortSocketFactory constructor(2) basePort = " + basePort + " range " + range);
    this.basePort = basePort;
    this.range = range;
  }


  public ServerSocket createServerSocket(int port) throws IOException {
    int tries = 0;
    //	System.out.println("RandomPortSocketFactory: createServerSocket " + port + " requested" );
    while (true) {
      try {
        int offset = random.nextInt(range);
        //	    System.out.println("RandomPortSocketFactory: createServerSocket with defaultRMI trying to use port " + (basePort+offset));
        ServerSocket socket = new ServerSocket(basePort + offset);
        //System.out.println("RandomPortSocketFactory: success for port " + (basePort + offset));
        //	    System.out.println("RandomPortSocketFactory: socket says port " +  socket.getLocalPort());
        return socket;
      } catch (IOException e) {
        tries++;
        if (tries > MAX)
	    //throw new IOException("RandomPortSocketFactory: failure to create a socket after " + tries + " attempts !!!");
	    throw new IOException();
      }
    }
  }


  public Socket createSocket(String host, int port) throws IOException {
    //	System.out.println("RandomPortServerSocketFactory: createSocket to host " + host + " on port "  + port);
    //try {
    return new Socket(host, port);
    //} catch (Exception e) {
    //  e.printStackTrace();
    //}
    //return null;
  }
}
