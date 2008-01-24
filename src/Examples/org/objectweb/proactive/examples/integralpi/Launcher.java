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
package org.objectweb.proactive.examples.integralpi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


/**
 * This simple program approximates pi by computing<br>
 * pi = integral from 0 to 1 of 4/(1+x*x)dx<br>
 * which is approximated by<br>
 * sum from k=1 to N of 4 / ((1 + (k-1/2)**2 ).
 * The only input data required is N.<br>
 * <br>
 * This example is not intended to be the fastest.<br>
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class Launcher {
    private static GCMApplicationDescriptor pad;

    /** The main method, not used by TimIt */
    public static void main(String[] args) {
        try {
            // The number of workers
            int np = Integer.valueOf(args[1]).intValue();

            Object[] param = new Object[] {};
            Object[][] params = new Object[np][];
            for (int i = 0; i < np; i++) {
                params[i] = param;
            }

            Set<Node> nodes = provideNodes(args[0]);
            Node[] nodesArray = nodes.toArray(new Node[0]);
            Worker workers = (Worker) PASPMD.newSPMDGroup(Worker.class.getName(), params, nodesArray);

            String input = "";

            //default number of iterations
            long numOfIterations = 1;
            double result;
            double error;
            DoubleWrapper wrappedResult;

            while (numOfIterations > 0) {
                // Prompt the user
                System.out.print("\nEnter the number of iterations (0 to exit) : ");

                try {
                    // Read a line of text from the user.
                    input = stdin.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                try {
                    numOfIterations = Long.parseLong(input);
                } catch (NumberFormatException numberException) {
                    System.err.println(numberException.getMessage());
                    System.out.println("No valid number entered using 1 iteration...");
                }

                if (numOfIterations <= 0) {
                    break;
                }

                // Send the number of iterations to the first worker
                Worker firstWorker = (Worker) PAGroup.getGroup(workers).get(0);
                wrappedResult = firstWorker.start(numOfIterations);
                result = wrappedResult.doubleValue();
                error = result - Math.PI;
                System.out.println("\nCalculated PI is " + result + " error is " + error);
            }

            finish();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    private static Set<Node> provideNodes(String descriptorUrl) {
        try {
            // Common stuff about ProActive deployement
            pad = API.getGCMApplicationDescriptor(new File(descriptorUrl));

            pad.startDeployment();
            Map<String, ? extends GCMVirtualNode> virtualNodes = pad.getVirtualNodes();
            Iterator<? extends GCMVirtualNode> iterator = virtualNodes.values().iterator();
            GCMVirtualNode vnode = iterator.next();
            vnode.waitReady();
            Set<Node> nodes = vnode.getCurrentNodes();

            System.out.println(nodes.size() + " nodes found");

            return nodes;
        } catch (NodeException ex) {
            ex.printStackTrace();
        } catch (ProActiveException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void finish() {
        pad.kill();
        PALifeCycle.exitSuccess();
    }
}
