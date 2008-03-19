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
//@snippet-start cma_main_migrator
package org.objectweb.proactive.examples.userguide.cmagent.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ActiveObjectCreationException;


public class Main {
    private static VirtualNode deploy(String descriptor) {
        ProActiveDescriptor pad;
        VirtualNode vn;
        try {
            //create object representation of the deployment file
            pad = PADeployment.getProactiveDescriptor(descriptor);
            //active all Virtual Nodes
            pad.activateMappings();
            //get the first Node available in the first Virtual Node 
            //specified in the descriptor file
            vn = pad.getVirtualNodes()[0];
            return vn;
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ProActiveException proExcep) {
            System.err.println(proExcep.getMessage());
        }
        return null;
    }

    public static void main(String args[]) {
        try {
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
            VirtualNode vn = deploy(args[0]);
            String currentState = new String();
            //@snippet-start cma_migrator_ao
            //create the active oject
            CMAgentMigrator ao = (CMAgentMigrator) PAActiveObject.newActive(CMAgentMigrator.class.getName(),
                    new Object[] {}, vn.getNode());
            //@snippet-end cma_migrator_ao

            //  int input = 0;
            int k = 1;
            int choice;
            while (k != 0) {

                //display the menu with the available nodes 
                k = 1;
                for (Node node : vn.getNodes()) {
                    System.out.println(k + ".  Statistics for node :" + node.getNodeInformation().getURL());
                    k++;
                }
                System.out.println("0.  Exit");

                //select a node
                do {
                    System.out.print("Choose a node :> ");
                    try {
                        // Read am option from keyboard
                        choice = Integer.parseInt(inputBuffer.readLine().trim());
                    } catch (NumberFormatException noExcep) {
                        choice = -1;
                    }
                } while (!(choice >= 1 && choice < k || choice == 0));
                if (choice == 0)
                    break;

                //display information for the selected node 
                ao.migrateTo(vn.getNodes()[choice - 1]); //migrate
                currentState = ao.getCurrentState().toString(); //get the state
                System.out.println("\n" + currentState);
            }

            //stopping all the objects and JVMS
            PAActiveObject.terminateActiveObject(ao, false);
            vn.killAll(true);
            PALifeCycle.exitSuccess();
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//@snippet-end cma_main_migrator