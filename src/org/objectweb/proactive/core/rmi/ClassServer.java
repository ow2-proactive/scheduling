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

/**
 * ClassServer is an abstract class that provides the
 * basic functionality of a mini-webserver, specialized
 * to load class files only. A ClassServer must be extended
 * and the concrete subclass should define the <b>getBytes</b>
 * method which is responsible for retrieving the bytecodes
 * for a class.<p>
 *
 * The ClassServer creates a thread that listens on a socket
 * and accepts  HTTP GET requests. The HTTP response contains the
 * bytecodes for the class that requested in the GET header. <p>
 *
 * For loading remote classes, an RMI application can use a concrete
 * subclass of this server in place of an HTTP server. <p>
 *
 * @see ClassFileServer
 */
public abstract class ClassServer implements Runnable {

  protected static int DEFAULT_SERVER_BASE_PORT = 2001;
  protected static int DEFAULT_SERVER_PORT_INCREMENT = 20;
  protected static int MAX_RETRY = 50;

  private static java.util.Random random = new java.util.Random();

  private java.net.ServerSocket server = null;
  protected int port;
  protected String hostname;


static {
	System.out.println("ClassServer loaded by " + ClassServer.class.getClassLoader());
	System.out.println("Context Class loader " + Thread.currentThread().getContextClassLoader());
	
}

  /**
   * Constructs a ClassServer that listens on a random port. The port number
   * used is the first one found free starting from a default base port.
   * obtains a class's bytecodes using the method <b>getBytes</b>.
   * @exception java.io.IOException if the ClassServer could not listen on any port.
   */
  protected ClassServer() throws java.io.IOException {
    this(0);
  }


  /**
   * Constructs a ClassServer that listens on <b>port</b> and
   * obtains a class's bytecodes using the method <b>getBytes</b>.
   * @param port the port number
   * @exception java.io.IOException if the ClassServer could not listen
   *            on <b>port</b>.
   */
  protected ClassServer(int port) throws java.io.IOException {
    if (port == 0){
      this.port = boundServerSockect(DEFAULT_SERVER_BASE_PORT, MAX_RETRY);
	  System.out.println("Port is " + this.port);
    } else {
      this.port = port;
      server = new java.net.ServerSocket(port);
    }
    hostname = java.net.InetAddress.getLocalHost().getHostAddress();
    newListener();
  }


  public int getServerSocketPort() {
  	//System.out.println("XXXXXX " + this.port);
    return port;
  }

  public String getHostname() {
    return hostname;
  }


  /**
   * The "listen" thread that accepts a connection to the
   * server, parses the header to obtain the class file name
   * and sends back the bytecodes for the class (or error
   * if the class is not found or the response was malformed).
   */
  public void run() {
    java.net.Socket socket;

    // accept a connection
    try {
      socket = server.accept();
    } catch (java.io.IOException e) {
      System.out.println("Class Server died: " + e.getMessage());
      e.printStackTrace();
      return;
    }

    // create a new thread to accept the next connection
    newListener();
    try {
      java.io.DataOutputStream out = new java.io.DataOutputStream(socket.getOutputStream());
      RequestInfo info = null;
      try {
        // get path to class file from header
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
        info = getInfo(in);
        // retrieve bytecodes
        byte[] bytecodes = getBytes(info.path);
        // send bytecodes in response (assumes HTTP/1.0 or later)
        try {
          out.writeBytes("HTTP/1.0 200 OK\r\n");
          out.writeBytes("Content-Length: " + bytecodes.length + "\r\n");
          out.writeBytes("Content-Type: application/java\r\n\r\n");
          out.write(bytecodes);
          out.flush();
          System.out.println("ClassServer sent class " + info.path+" successfully");
        } catch (java.io.IOException ie) {
          return;
        }
      } catch (Exception e) {
        // write out error response
        e.printStackTrace();
        System.out.println("!!! ClassServer failed to load class " + info.path);
        out.writeBytes("HTTP/1.0 400 " + e.getMessage() + "\r\n");
        out.writeBytes("Content-Type: text/html\r\n\r\n");
        out.flush();
      }
    } catch (java.io.IOException ex) {
      // eat exception (could log error to log file, but write out to stdout for now).
      //System.out.println("error writing response: " + ex.getMessage());
      //ex.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (java.io.IOException e) {}
    }
  }



  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  /**
   * Returns an array of bytes containing the bytecodes for
   * the class represented by the argument <b>path</b>.
   * The <b>path</b> is a dot separated class name with
   * the ".class" extension removed.
   *
   * @return the bytecodes for the class
   * @exception ClassNotFoundException if the class corresponding
   * to <b>path</b> could not be loaded.
   * @exception java.io.IOException if error occurs reading the class
   */
  protected abstract byte[] getBytes(String path) throws java.io.IOException, ClassNotFoundException;



  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  /**
   * Create a new thread to listen.
   */
  private void newListener() {
    (new Thread(this, "ClassServer-"+hostname+":"+port)).start();
  }


  /**
   * Returns the path to the class file obtained from
   * parsing the HTML header.
   */
  private static RequestInfo getInfo(java.io.BufferedReader in) throws java.io.IOException {
    RequestInfo info = new RequestInfo();
    String line = null;
    do {
      line = in.readLine();
      if (line.startsWith("GET /")) {
        info.path = getPath(line);
      } else if (line.startsWith("Host:")) {
        info.host = getHost(line);
      } else {
        // eat line
      }
    } while ((line.length() != 0) && (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
    if (info.path != null) {
      return info;
    } else {
      throw new java.io.IOException("Malformed Header");
    }
  }


  /**
   * Returns the path to the class file obtained from
   * parsing the HTML header.
   * @param line the GET item starting by "GET /"
   */
  private static String getPath(String line) {
    // extract class from GET line
    line = line.substring(5, line.length()-1).trim();
    int index = line.indexOf(".class ");
    if (index != -1) {
      return line.substring(0, index).replace('/', '.');
    } else {
      return null;
    }
  }


  /**
   * Returns the path to the class file obtained from
   * parsing the HTML header.
   * @param line the GET item starting by "Host:"
   */
  private static String getHost(String line) {
    // extract class from Host line
    return line.substring(5, line.length()-1).trim();
  }


  private int boundServerSockect(int basePortNumber, int numberOfTry) throws java.io.IOException {
    for (int i=0; i<numberOfTry; i++) {
      try {
        server = new java.net.ServerSocket(basePortNumber);
        return basePortNumber;
      } catch (java.io.IOException e) {
        basePortNumber += random.nextInt(DEFAULT_SERVER_PORT_INCREMENT);
      }
    }
    throw new java.io.IOException("ClassServer cannot create a ServerSocket after " + numberOfTry + " attempts !!!");
  }


  private static class RequestInfo {
    String path;
    String host;
  }
}

