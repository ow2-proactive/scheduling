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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


/**
 * This class is responsible for implementing actions that are started in
 * ProActiveAgent: registration in ProActive Resource Manager 
 * 
 * The created process from this class should be monitored by ProActiveAgent
 * component and restarted automatically on any failures.
 * 
 * @author ProActive team
 *
 */
public class PAAgentServiceRMStarter {

    //the starter will try to connect to the RM NB_OF_CONNECT_RETRIES times before killing itself
    //that means that it will try to connect during NB_OF_CONNECT_RETRIES x RM_FREQ milliseconds
    private static final int NB_OF_CONNECT_RETRIES = 10;
    private static final long RM_FREQ = 6000;
    private static final long PING_FREQ = 30000;
    private static final String PAAGENT_NODE_NAME = "PA-AGENT_NODE";
    private static Executor tpe = Executors.newFixedThreadPool(1);

    // command dispatch

    /**
     * main function
     * @param args
     */
    public static void main(String args[]) {
        if (args.length < 1) {
            printUsage();
            return;
        }
        String rmHost = args[0];
        PAAgentServiceRMStarter starter = new PAAgentServiceRMStarter();
        starter.registerInRM(rmHost);
    }

    // registers itself in ResourceManager at given URL in parameter

    private void registerInRM(String rmHost) {
        // create local node
        Node n = null;
        try {
            n = NodeFactory.createNode("//localhost/" + PAAGENT_NODE_NAME);
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            return;
        }

        if (n == null) {
            System.out.println("Could not create the node.");
            return;
        }
        String url = rmHost + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMADMIN;
        int nbOfRetries = 1;
        boolean success = false;
        RMAdmin admin = null;
        while ((!success) && (nbOfRetries <= NB_OF_CONNECT_RETRIES)) {
            try {
                admin = RMConnection.connectAsAdmin(url);
                admin.addNode(n.getNodeInformation().getURL());
                success = true;
            } catch (RMException e) {
                System.out.println("The attemt number " + nbOfRetries +
                    " to connect to the Resource Manager failed. " + e.getMessage());
                nbOfRetries++;
                waitInterval(RM_FREQ);
            }
        }
        if (success) {
            System.out.println("Connected to the Resource Manager at " + url + "\n");
        } else {
            //it means we already tried to connect NB_OF_CONNECT_RETRIES times
            System.out.println("The Resource Manager at " + url + " is not reachable (after " +
                NB_OF_CONNECT_RETRIES + " attempts). The application will exit.");
            System.exit(1);
        }
        //     boolean connected = true;   
        //     //ping the rm to see if we are still connected
        //    //if not connected just exit
        //        while (connected)
        //        {
        //        	if (!PAActiveObject.pingActiveObject(admin))
        //        	{
        //        		connected=false;
        //        	}
        //        	waitInterval(PING_FREQ);
        //        }//while connected
        //        
        //      //if we are here it means we lost the connection. just exit.. 
        //        System.out.println("The connection to the Resource Manager has been lost. The application will exit. ");
        //        System.exit(1);

        RMPinger rp = new RMPinger(admin);
        tpe.execute(rp);

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

    class RMPinger implements Runnable {

        private RMAdmin admin;

        public RMPinger(RMAdmin admin) {
            this.admin = admin;
        }

        public void run() {

            boolean connected = true;
            //ping the rm to see if we are still connected
            //if not connected just exit
            while (connected) {
                if (!PAActiveObject.pingActiveObject(admin)) {
                    connected = false;
                }
                waitInterval(PING_FREQ);
            }//while connected

            //if we are here it means we lost the connection. just exit.. 
            System.out
                    .println("The connection to the Resource Manager has been lost. The application will exit. ");
            System.exit(1);

        }

    }

}
