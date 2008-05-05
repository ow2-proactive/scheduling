/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.benchmarks.NAS;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * A new NAS kernel must extend this class to be executable
 * 
 */
public abstract class Kernel implements Serializable {

    public abstract void runKernel(ProActiveDescriptor pad) throws ProActiveException;

    public abstract void killKernel();

    public static void printStarted(String kernel, char className, long[] size, int nbIteration, int nbProcess) {
        System.out.print("\n\n NAS Parallel Benchmarks ProActive -- " + kernel + " Benchmark\n\n");
        System.out.println(" Class: " + className);
        System.out.print(" Size:  " + size[0]);
        for (int i = 1; i < size.length; i++) {
            System.out.print(" x " + size[i]);
        }
        System.out.println();

        System.out.println(" Iterations:   " + nbIteration);
        System.out.println(" Number of processes:     " + nbProcess);
    }

    public static void printEnd(NASProblemClass clss, double totalTime, double mops,
            boolean passed_verification) {
        String verif;
        String javaVersion = System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name") +
            " " + System.getProperty("java.vm.version") + " - Version " + System.getProperty("java.version");
        String proactiveVersion = "ProActive " + PAVersion.getProActiveVersion();

        verif = passed_verification ? "SUCCESSFUL" : "UNSUCCESSFUL";

        System.out.println("\n\n " + clss.KERNEL_NAME + " Benchmark Completed");
        System.out.println(" Class            =  " + clss.PROBLEM_CLASS_NAME);
        System.out.println(" Size             =  " + clss.SIZE);
        System.out.println(" Iterations       =  " + clss.ITERATIONS);
        System.out.println(" Time in seconds  =  " + totalTime);
        System.out.println(" Total processes  =  " + clss.NUM_PROCS);
        System.out.println(" Mop/s total      =  " + mops);
        System.out.println(" Mop/s/process    =  " + (mops / clss.NUM_PROCS));
        System.out.println(" Operation type   =  " + clss.OPERATION_TYPE);
        System.out.println(" Verification     =  " + verif);
        System.out.println(" NPB Version      =  " + clss.VERSION);
        System.out.println(" Java RE          =  " + javaVersion);
        System.out.println(" Middleware       =  " + proactiveVersion);
    }

    public Node[] vnodeMapping(VirtualNode[] vnodeArray, int nbProcs) {
        return vnodeMapping(vnodeArray, nbProcs, 1);
    }

    public Node[] vnodeMapping(VirtualNode[] vnodeArray, int nbProcs, int subGroupSize) {
        Node[] tempNodeArray;
        Node[] nodes;
        ArrayList<Node> nodeList;
        int wantedVirtualNodeSize = nbProcs / vnodeArray.length;
        String printBuffer = "";
        printBuffer += "-- Mapping sub group of workers in a ROUND ROBIN fashion. [ wantedVirtualNodeSize = " +
            wantedVirtualNodeSize + ", NUMPROCS = " + nbProcs + " ]\n";
        int localIndex = 0;
        int localIndexGsize;
        int tempValue = 0;

        try {
            if (vnodeArray.length > 1) {
                nodeList = new ArrayList<Node>();

                // Get the total number of nodes
                for (int i = 0; i < vnodeArray.length; ++i) {
                    tempValue += vnodeArray[i].getNodes().length;
                }

                // If total number of nodes is smaller than the problem class one
                if (tempValue < nbProcs) {
                    printBuffer += "-- Warning !! There is not enough nodes. Probably there will be several workers mapped to a single node !\n";
                }

                // while ( roundRobinIndex <= nbProcs ) {
                while (nodeList.size() < nbProcs) {
                    localIndexGsize = localIndex + subGroupSize;
                    for (int i = 0; i < vnodeArray.length; i++) {
                        if (nodeList.size() < nbProcs) { // check if there is enough harvested
                            // nodes
                            // Get the array of node from current virtualNode
                            tempNodeArray = vnodeArray[i].getNodes();
                            // If there is enough nodes in this virtualNode
                            if (tempNodeArray.length >= wantedVirtualNodeSize) {
                                printBuffer += "-- Mapping " + subGroupSize + " workers to the virtualNode " +
                                    vnodeArray[i].getName() + " [ localIndex = " + localIndex + " ]\n";
                                // Adding nodes to the final nodeList
                                for (int k = localIndex; k < localIndexGsize && k < tempNodeArray.length; k++) {
                                    printBuffer += "           ----> adding node " +
                                        tempNodeArray[k].getNodeInformation().getName() + "\n";
                                    nodeList.add(tempNodeArray[k]);
                                }
                            } else {
                                printBuffer += "-- Warning !! There is not enough nodes on the virtualNode " +
                                    vnodeArray[i].getName() + " : " + " there are currently only " +
                                    tempNodeArray.length + " nodes " + " there should be at minimum " +
                                    wantedVirtualNodeSize + " nodes.\n" +
                                    "-- BE AWARE : HAZARDOUS MAPPING !!! \n";
                                for (int k = 0; k < tempNodeArray.length; k++) {
                                    nodeList.add(tempNodeArray[k]);
                                }
                            }
                        } else {
                            printBuffer += "-- There is enough recolted nodes : " + nodeList.size() + "\n";
                            break;
                        }
                    }
                    localIndex += subGroupSize;
                    // roundRobinIndex += ( subGroupSize == 1 ? subGroupSize : wantedVirtualNodeSize
                    // );
                }

                nodes = (Node[]) nodeList.toArray(new Node[0]);
            } else {
                nodes = vnodeArray[0].getNodes();
            }
            printBuffer += "" + nodes.length + " node" + (nodes.length == 1 ? "" : "s") + " found\n";
            System.out.println(printBuffer);

            return nodes;
        } catch (NodeException e) {
            e.printStackTrace();
        }
        return null;
    }
}