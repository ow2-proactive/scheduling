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

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Random;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This factory creates server socket with randomly choosen port number
 * it tries 5 different ports before reporting a failure
 */
public class RandomPortSocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.RMI);
    static protected final int MAX = 5;
    static protected Random random = new Random();
    protected int basePort = 35000;
    protected int range = 5000;

    public RandomPortSocketFactory() {
        logger.debug("RandomPortSocketFactory constructor()");
    }

    public RandomPortSocketFactory(int basePort, int range) {
        logger.debug("RandomPortSocketFactory constructor(2) basePort = " + basePort + " range " + range);
        this.basePort = basePort;
        this.range = range;
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        int tries = 0;
        logger.debug("RandomPortSocketFactory: createServerSocket " + port + " requested");
        while (true) {
            try {
                int offset = random.nextInt(range);
                ServerSocket socket = new ServerSocket(basePort + offset);
                logger.debug("RandomPortSocketFactory: success for port " + (basePort + offset));
                return socket;
            } catch (IOException e) {
                tries++;
                if (tries > MAX) {
                    throw new IOException();
                }
            }
        }
    }

    public Socket createSocket(String host, int port) throws IOException {
        logger.debug("RandomPortServerSocketFactory: createSocket to host " + host + " on port " + port);
        return new Socket(host, port);
    }
}
