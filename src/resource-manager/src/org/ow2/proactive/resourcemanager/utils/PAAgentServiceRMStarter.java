/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


public class PAAgentServiceRMStarter {

    /**
     * This class is responsible for implementing actions that are started in
     * ProActiveAgent: registration in ProActive Resource Manager 
     * 
     * The created process from this class should be monitored by ProActiveAgent
     * component and restarted automatically on any failures
     */

    private static final long RM_FREQ = 6000;
    private static final String PAAGENT_NODE_NAME = "PA-AGENT_NODE";

    // command dispatch

    public static void main(String args[]) {
        if (args.length < 1) {
            printUsage();
            return;
        }
        String rmHost = args[0];
        registerInRM(rmHost);
    }

    // registers itself in ResourceManager at given URL in parameter

    private static void registerInRM(String rmHost) {
        // create local node
        Node n = null;
        try {
            n = NodeFactory.createNode("//localhost/" + PAAGENT_NODE_NAME);
        } catch (ProActiveException e) {
            return;
        } catch (AlreadyBoundException e) {
            return;
        }

        if (n == null)
            return;

        String url = rmHost + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMADMIN;
        boolean success = false;

        while (!success) {
            try {
                RMAdmin admin = RMConnection.connectAsAdmin(url);
                admin.addNode(n.getNodeInformation().getURL());
                success = true;
            } catch (RMException e) {
                e.printStackTrace();
                waitInterval(RM_FREQ);
            }
        }

    }

    // waits specified amount of time

    private static void waitInterval(long rmFreq) {
        long endTime = System.currentTimeMillis() + rmFreq;
        long curTime = System.currentTimeMillis();
        while (curTime < endTime) {
            try {
                Thread.sleep(endTime - curTime);
            } catch (InterruptedException e) {
            }
            curTime = System.currentTimeMillis();
        }
    }

    // prints help

    private static void printUsage() {
        System.out.println("PAAgentServiceRMStarter help. Available parameters are: ");
        System.out.println("[rm-url]");

    }
}
