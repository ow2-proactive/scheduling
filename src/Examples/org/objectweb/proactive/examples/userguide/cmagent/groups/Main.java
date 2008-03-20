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
package org.objectweb.proactive.examples.userguide.cmagent.groups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.migration.CMAgentMigrator;
import org.objectweb.proactive.examples.userguide.cmagent.simple.State;
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
            Vector<CMAgentMigrator> agents = new Vector<CMAgentMigrator>();
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
            VirtualNode vn = deploy(args[0]);
            String currentState = new String();
            //create a new empty  group
            CMAgentMigrator monitorsGroup = (CMAgentMigrator) PAGroup.newGroup(CMAgentMigrator.class
                    .getName());
            //create a collection of active objects
            for (Node node : vn.getNodes()) {
                CMAgentMigrator ao = (CMAgentMigrator) PAActiveObject.newActive(CMAgentMigrator.class
                        .getName(), new Object[] {}, node);
                agents.add(ao);
            }

            Group gA = PAGroup.getGroup(monitorsGroup);
            //ask for adding or removing nodes

            //get statistics

            //  int input = 0;
            int k = 1;
            int choice;
            while (k != 0) {
                //display the meniu 
                k = 1;
                System.out.println("Toggle monitored nodes (*) or display statistics: ");
                for (CMAgentMigrator agent : agents) {
                    if (gA.contains(agent))
                        System.out.println(" " + k + ".* " + PAActiveObject.getActiveObjectNodeUrl(agent));
                    else
                        System.out.println(" " + k + ".  " + PAActiveObject.getActiveObjectNodeUrl(agent));
                    k++;
                }
                System.out.println("-1.  Display statistics for monitored nodes");
                System.out.println(" 0.  Exit");

                //select a node
                do {
                    System.out.print("Choose a node to add or remove  :> ");
                    try {
                        // Read am option from keyboard
                        choice = Integer.parseInt(inputBuffer.readLine().trim());
                    } catch (NumberFormatException noExcep) {
                        choice = -1;
                    }
                } while (!(choice >= 1 && choice < k || choice == 0 || choice == -1));
                if (choice == 0)
                    break;
                if (choice == -1) {

                    State resultsGroup = monitorsGroup.getCurrentState();
                    while (PAGroup.size(resultsGroup) > 0) {
                        State statistic = (State) PAGroup.waitAndGetOneThenRemoveIt(resultsGroup);
                        System.out.println(statistic.toString());
                    }
                } else {
                    if (gA.contains(agents.elementAt(choice - 1)))
                        gA.remove(agents.elementAt(choice - 1));
                    else
                        gA.add(agents.elementAt(choice - 1));
                }

                //                    ao.getLastRequestServeTime().longValue() + "ms \n");
            }

            //stopping all the objects and JVMS
            //            PAActiveObject.terminateActiveObject(ao, false);
            vn.killAll(true);
            PALifeCycle.exitSuccess();
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
//@snippet-end cma_main_migrator