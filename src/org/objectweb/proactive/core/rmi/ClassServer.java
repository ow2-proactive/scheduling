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

import org.apache.log4j.Logger;

import java.net.UnknownHostException;


public class ClassServer implements Runnable {
    protected static Logger logger = Logger.getLogger(ClassServer.class.getName());
    public static int DEFAULT_SERVER_BASE_PORT = 2001;
    protected static int DEFAULT_SERVER_PORT_INCREMENT = 20;
    protected static int MAX_RETRY = 50;
    private static java.util.Random random = new java.util.Random();
    protected static int port;
    protected String hostname;

    static {
        String newport = System.getProperty("proactive.http.port");

        if (newport != null) {
        	DEFAULT_SERVER_BASE_PORT = Integer.valueOf(newport).intValue();
        }
        else
        	DEFAULT_SERVER_BASE_PORT = 2222;
        
    }

    private java.net.ServerSocket server = null;
    protected String paths;

    /**
     * Constructs a ClassServer that listens on a random port. The port number
     * used is the first one found free starting from a default base port.
     * obtains a class's bytecodes using the method <b>getBytes</b>.
     * @exception java.io.IOException if the ClassServer could not listen on any port.
     */
    protected ClassServer() throws java.io.IOException {
        this(0, null);
    }

    
    protected ClassServer(int port_) throws java.io.IOException {
        if (port == 0) {
            port = boundServerSockect(DEFAULT_SERVER_BASE_PORT, MAX_RETRY);
        } else {
            port = port_;
            server = new java.net.ServerSocket(port);
        }
        hostname = java.net.InetAddress.getLocalHost().getHostAddress();
        newListener();
        
        if (logger.isInfoEnabled()) {
            logger.info("communication protocol = " +System.getProperty("proactive.communication.protocol")+", http server port = " + port);
        }

    }
    
    /**
     * Constructs a ClassServer that listens on <b>port</b> and
     * obtains a class's bytecodes using the method <b>getBytes</b>.
     * @param port the port number
     * @exception java.io.IOException if the ClassServer could not listen
     *            on <b>port</b>.
     */
    protected ClassServer(int port_, String paths) throws java.io.IOException {        
    	this(port_);
    	this.paths = paths;
        printMessage();
    }

    /**
     * Constructs a ClassFileServer.
     * @param classpath the classpath where the server locates classes
     */
    public ClassServer(String paths) throws java.io.IOException {
        this(0, paths);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static boolean isPortAlreadyBound(int port) {
        java.net.Socket socket = null;

        try {
            socket = new java.net.Socket(java.net.InetAddress.getLocalHost(),
                    port);

            // if we can connect to the port it means the server already exists
            return true;
        } catch (java.io.IOException e) {
            return false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (java.io.IOException e) {
            }
        }
    }

    private void printMessage() {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "To use this ClassFileServer set the property java.rmi.server.codebase to http://" +
                hostname + ":" + port + "/");
        }

        if (this.paths == null) {
            logger.info(
                " --> This ClassFileServer is reading resources from classpath");
        } else {
            logger.info(
                " --> This ClassFileServer is reading resources from the following paths");

            //for (int i = 0; i < codebases.length; i++) {
            logger.info(paths);

            //codebases[i].getAbsolutePath());
        }
    }

    public static int getServerSocketPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public static String getUrl() {
        try {
            return "http://" +
            java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /**
     * The "listen" thread that accepts a connection to the
     * server, parses the header to obtain the class file name
     * and sends back the bytecodes for the class (or error
     * if the class is not found or the response was malformed).
     */
    public void run() {
        java.net.Socket socket = null;   

        // accept a connection
        while (true) {
            try {
                socket = server.accept();

                ProActiveService service = (new ProActiveService(socket, paths));
                service.start();
            } catch (java.io.IOException e) {
                System.out.println("Class Server died: " + e.getMessage());
                e.printStackTrace();

                return;
            } finally {
            }
        }
    }

    private void newListener() {
        (new Thread(this, "ClassServer-" + hostname + ":" + port)).start();
    }

    private int boundServerSockect(int basePortNumber, int numberOfTry)
        throws java.io.IOException {
        for (int i = 0; i < numberOfTry; i++) {
            try {
                server = new java.net.ServerSocket(basePortNumber);

                return basePortNumber;
            } catch (java.io.IOException e) {
                basePortNumber += random.nextInt(DEFAULT_SERVER_PORT_INCREMENT);
            }
        }

        throw new java.io.IOException(
            "ClassServer cannot create a ServerSocket after " + numberOfTry +
            " attempts !!!");
    }
}
