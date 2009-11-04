/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Initial developer(s): ActiveEon Team - www.activeeon.com
 *
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.net.Socket;
import java.util.Formatter;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.VirtualInfrastructure;


/**
 * This class is in charge of starting RMNodes and passing nodes' uri to
 * the listening socket on the local machine whose port number is the first
 * argument on the command line.
 *
 * @author ProActive team
 */
public final class VirtualInfrastructureNodeStarter {

    /** for local inter process communication */
    public static final int message_length = 1000;

    /** PALogger */
    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMNODE);

    /**
     * Creates a new instance of this class and calls registersInRm method. The
     * arguments must be as follows:
     * arg[1] = vmname, arg[2] = processID, arg[0] = server socket listenning port
     * @param args
     *            The arguments needed to join the Resource Manager
     * @throws Throwable
     */
    public static void main(final String args[]) throws Throwable {
        final String vmname = args[1];
        final int processID = Integer.parseInt(args[2]);
        final int serverSocketPort = Integer.parseInt(args[0]);
        Socket clientSocket = new Socket("127.0.0.1", serverSocketPort);
        Formatter output = new Formatter(clientSocket.getOutputStream());
        int currentRMIPort = PAProperties.PA_RMI_PORT.getValueAsInt();
        PAProperties.PA_RMI_PORT.setValue(currentRMIPort + processID - 1);//processID starts from 1
        startLocalNode(vmname, processID, output);
    }

    /**
     * Registers a in ResourceManager at given URL in parameter and handles all
     * errors/exceptions. Tries to joins the Resource Manager with a specified
     * timeout then logs as admin with the provided username and password and
     * adds the created node to the Resource Manager
     * @throws Throwable
     */
    private static void startLocalNode(String vmName, int processID, Formatter output) throws Throwable {
        try {
            Node n = NodeFactory.createLocalNode(vmName + "_node_" + processID, false, null, null, null);
            if (n == null) {
                throw new RuntimeException("The node returned by the NodeFactory is null");
            }
            n.setProperty(VirtualInfrastructure.Prop.HOLDING_VIRTUAL_MACHINE.getValue(), vmName);
            output.format("%1$" + message_length + "s", n.getNodeInformation().getURL());
            logger.info("RMNode started with url " + n.getNodeInformation().getURL());
            output.flush();
        } catch (Throwable t) {
            logger.error("Unable to start RMNode", t);
            System.exit(1);
        } finally {
            output.format("%1$" + message_length + "s", "EOF");
            logger.info("IPC ended.");
            output.flush();
            output.close();
        }
    }
}