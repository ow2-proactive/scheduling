/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.rmi;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ClassServer implements Runnable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    public static final int DEFAULT_SERVER_BASE_PORT = 2010;
    protected static int DEFAULT_SERVER_PORT_INCREMENT = 2;
    protected static int MAX_RETRY = 500;
    protected static int port;
    private boolean active = true;

    static {
        String newport;

        if (PAProperties.PA_XMLHTTP_PORT.getValue() != null) {
            newport = PAProperties.PA_XMLHTTP_PORT.getValue();
        } else {
            newport = new Integer(DEFAULT_SERVER_BASE_PORT).toString();
            PAProperties.PA_XMLHTTP_PORT.setValue(newport);
        }
    }

    protected String hostname;
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
        if (port_ == 0) {
            port = boundServerSocket(Integer.parseInt(PAProperties.PA_XMLHTTP_PORT.getValue()), MAX_RETRY);
            //            Thread.dumpStack();
        } else {
            port = port_;
            server = new java.net.ServerSocket(port);
        }

        PAProperties.PA_XMLHTTP_PORT.setValue(port + "");

        hostname = ProActiveInet.getInstance().getInetAddress().getHostName();
        //        hostname = URIBuilder.ipv6withoutscope(UrlBuilder.getNetworkInterfaces());

        //        System.out.println("URL du classServer : " + hostname + ":" + port);
        newListener();
        //        if (logger.isInfoEnabled()) {
        //            logger.info("communication protocol = " +System.getProperty("proactive.communication.protocol")+", http server port = " + port);
        //        }
    }

    /**
     * Constructs a ClassServer that listens on <b>port</b> and
     * obtains a class's bytecodes using the method <b>getBytes</b>.
     * @param port_ the port number
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
     * @param paths the classpath where the server locates classes
     */
    protected ClassServer(String paths) throws java.io.IOException {
        this(0, paths);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static boolean isPortAlreadyBound(int port) {
        java.net.Socket socket = null;

        try {
            socket = new java.net.Socket(ProActiveInet.getInstance().getInetAddress(), port);

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
            logger.info("To use this ClassFileServer set the property java.rmi.server.codebase to http://" +
                hostname + ":" + port + "/");
        }

        if (this.paths == null) {
            logger.info(" --> This ClassFileServer is listening on port " + port);
        } else {
            logger.info(" --> This ClassFileServer is reading resources from the following paths");

            //for (int i = 0; i < codebases.length; i++) {
            logger.info(paths);

            //codebases[i].getAbsolutePath());
        }
    }

    public static int getServerSocketPort() {
        if (PAProperties.PA_HTTP_SERVLET.isTrue()) {
            return ClassServerServlet.getPort();
        }
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public static String getUrl() {
        if (PAProperties.PA_HTTP_SERVLET.isTrue()) {
            return ClassServerServlet.getURI().toString();
        } else {
            return URIBuilder.buildURI(
                    URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()), "",
                    Constants.XMLHTTP_PROTOCOL_IDENTIFIER, port).toString();
        }
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
        while (active) {
            try {
                socket = server.accept();

                HTTPRequestHandler service = (new HTTPRequestHandler(socket, paths));
                service.start();
            } catch (java.io.IOException e) {
                System.out.println("Class Server died: " + e.getMessage());
                e.printStackTrace();

                return;
            }
        }
    }

    private void newListener() {
        Thread t = new Thread(this, "ClassServer-" + hostname + ":" + port);

        //        MyShutdownHook shutdownHook = new MyShutdownHook();
        //        Runtime.getRuntime().addShutdownHook(shutdownHook);
        t.setDaemon(true);
        t.start();
    }

    private int boundServerSocket(int basePortNumber, int numberOfTry) throws java.io.IOException {
        for (int i = 0; i < numberOfTry; i++) {
            try {
                server = new java.net.ServerSocket(basePortNumber);
                return basePortNumber;
            } catch (java.io.IOException e) {
                basePortNumber += ProActiveRandom.nextInt(DEFAULT_SERVER_PORT_INCREMENT);
            }
        }

        throw new java.io.IOException("ClassServer cannot create a ServerSocket after " + numberOfTry +
            " attempts !!!");
    }
}
