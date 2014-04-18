/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.logforwarder.util;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.common.util.logforwarder.LoggingEventProcessor;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class SimpleLoggerServer implements Runnable {
    public static final Logger logger = Logger.getLogger(SimpleLoggerServer.class);
    private final LoggingEventProcessor eventProcessor;

    // socket port
    private int port;
    private boolean terminate = false;

    // to close sockets
    private Vector<ConnectionHandler> connections;

    private ServerSocket serverSocket;

    /**
     * Create a logger server on a given port.
     *
     * @param port         the binding port of the created server.
     * @param eventProcessor
     *
     * @throws IOException
     */
    public SimpleLoggerServer(int port, LoggingEventProcessor eventProcessor) throws IOException {
        this.connections = new Vector<ConnectionHandler>();
        this.serverSocket = new ServerSocket(port);
        this.port = this.serverSocket.getLocalPort();
        this.eventProcessor = eventProcessor;
    }

    /**
     * Create a logger server on any free port. This port can be
     * retreived using getPort().
     *
     * @throws IOException
     */
    public SimpleLoggerServer(LoggingEventProcessor eventProcessor) throws IOException {
        this(0, eventProcessor);
    }

    /**
     * Create and start a new logger server on any free port. This port can be
     * retreived using getPort().
     *
     * @return the created server.
     * @throws IOException
     */
    public static SimpleLoggerServer createLoggerServer(LoggingEventProcessor eventProcessor) throws IOException {
        SimpleLoggerServer simpleLoggerServer = new SimpleLoggerServer(eventProcessor);
        Thread simpleLoggerServerThread = new Thread(simpleLoggerServer);
        simpleLoggerServerThread.start();

        return simpleLoggerServer;
    }

    /**
     * Return the binding port of the created server.
     *
     * @return the binding port of the created server.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Stop this logger server.
     */
    public synchronized void stop() {
        for (ConnectionHandler c : this.connections) {
            c.stop();
        }

        this.terminate = true;
    }

    public void run() {
        while (!terminate) {
            try {
                Socket s = this.serverSocket.accept();
                ConnectionHandler ch = new ConnectionHandler(s);
                this.connections.add(ch);
                new Thread(ch).start();
            } catch (IOException e1) {
                logger.error("", e1);
            }
        }

        // close connection
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private void removeConnection(ConnectionHandler c) {
        this.connections.remove(c);
    }

    /**
     * Thread for handling incoming blocking connection.
     *
     * @author The ProActive Team
     * @since 2.2
     */
    private class ConnectionHandler implements Runnable {
        private ObjectInputStream inputStream;
        private boolean terminate;

        public ConnectionHandler(Socket input) {
            try {
                this.inputStream = new ObjectInputStream(new BufferedInputStream(input.getInputStream()));
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        public void run() {
            LoggingEvent currentEvent;

            try {
                while (!terminate) {
                    // read an event from the wire
                    currentEvent = (LoggingEvent) inputStream.readObject();
                    // if the server has been terminated while waiting for event to read
                    if (terminate) {
                        break;
                    }
                    eventProcessor.processEvent(currentEvent);
                }

            } catch (EOFException e) {
                // normal case ...
            } catch (IOException e) {
                logger.error("", e);
            } catch (ClassNotFoundException e) {
                logger.error("", e);
            } finally {
                // close stream
                try {
                    this.inputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
                // remove connexion from server
                SimpleLoggerServer.this.removeConnection(this);
            }
        }

        public synchronized void stop() {
            this.terminate = true;
        }
    }
}
