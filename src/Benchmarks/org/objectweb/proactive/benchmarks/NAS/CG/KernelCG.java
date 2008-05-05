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
package org.objectweb.proactive.benchmarks.NAS.CG;

import java.util.ArrayList;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
import org.objectweb.proactive.benchmarks.NAS.util.NpbMath;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.EventStatistics;
import org.objectweb.proactive.benchmarks.timit.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * NAS PARALLEL BENCHMARKS
 *
 * Kernel CG
 *
 * A conjugate gradient method is used to compute an approximation
 * to the smallest eigenvalue of a large, sparse, symmetric positive
 * definite matrix. This kernel is typical of unstructured grid
 * computations in that it tests irregular long distance communication,
 * employing unstructured matrix vector multiplication.
 */
public class KernelCG extends Kernel {

    public static boolean ROUND_ROBIN_MAPPING_MODE = true;

    private CGProblemClass problemClass;

    /** Initial values calculated locally */
    private int npcols;
    private int nprows;
    private int nzz;

    /** The reference for the typed group of Workers */
    private WorkerCG workers;

    public KernelCG() {
    }

    public KernelCG(NASProblemClass pclass) {
        this.problemClass = (CGProblemClass) pclass;
    }

    public void runKernel(ProActiveDescriptor pad) throws ProActiveException {
        // String buff to have a print order
        String printBuffer = "";
        // Some int values
        int i, k, tempValue = 0;
        // The Array of virtual nodes
        VirtualNode[] vnodeArray;
        // The virtual node
        VirtualNode vnode;
        // The ArrayList of node for the multicluster case
        ArrayList<Node> nodeList;
        // The nodes array
        Node[] nodes = null;
        // Temp node array
        Node[] tempArray = null;
        // The array of parameters for each worker
        Object[] param;
        // The array of parameters for all workers
        Object[][] params;

        try {
            // Check if number of procs is a power of two.
            if (this.problemClass.NUM_PROCS != 1 &&
                ((this.problemClass.NUM_PROCS & (this.problemClass.NUM_PROCS - 1)) != 0)) {
                System.err.println("Error: nbprocs is " + this.problemClass.NUM_PROCS +
                    " which is not a power of two");
                System.exit(1);
            }

            // Calculate the
            this.npcols = this.nprows = NpbMath.ilog2(this.problemClass.NUM_PROCS) / 2;

            //???
            if ((this.npcols + this.nprows) != NpbMath.ilog2(this.problemClass.NUM_PROCS)) {
                this.npcols += 1;
            }
            this.npcols = (int) NpbMath.ipow2(this.npcols);
            this.nprows = (int) NpbMath.ipow2(this.nprows);

            // Check npcols parity
            if (this.npcols != 1 && ((this.npcols & (this.npcols - 1)) != 0)) {
                System.err.println("Error: num_proc_cols is " + this.npcols + " which is not a power of two");
                System.exit(1);
            }

            // Check nprows parity
            if (this.nprows != 1 && ((this.nprows & (this.nprows - 1)) != 0)) {
                System.err.println("Error: num_proc_rows is " + this.nprows + " which is not a power of two");
                System.exit(1);
            }

            // Get vitualNodes from the descriptor pad
            vnodeArray = pad.getVirtualNodes();

            // Begin the distribution
            pad.activateMappings();

            printBuffer += "-- Detecting " + vnodeArray.length + " virtual nodes ...\n";

            if (vnodeArray.length > 1) {

                // Create the final list of nodes
                nodeList = new ArrayList<Node>();

                // The round robin fashion mapping strategy
                if (KernelCG.ROUND_ROBIN_MAPPING_MODE) {
                    nodes = super.vnodeMapping(vnodeArray, this.problemClass.NUM_PROCS, this.npcols);

                } else {
                    // Get the total number of nodes
                    for (i = 0; i < vnodeArray.length; ++i) {
                        tempValue += vnodeArray[i].getNodes().length;
                    }

                    // If total number of nodes is smaller than the problem class one
                    if (tempValue < this.problemClass.NUM_PROCS) {
                        printBuffer += "-- Warning !! There is not enough nodes. Probably there will be several workers mapped to a single node !\n";
                    }

                    // The mapping strategy is to put a max workers per virtualNode
                    for (i = 0; i < vnodeArray.length; i++) {
                        // Get the array of node from current virtualNode
                        tempArray = vnodeArray[i].getNodes();
                        // If there is enough nodes in this virtualNode
                        if (tempArray.length >= npcols) {
                            tempValue = (tempArray.length / npcols) * npcols;
                            printBuffer += "-- Mapping " + tempValue + " workers to the virtualNode " +
                                vnodeArray[i].getName() + "\n";
                            // Adding nodes to the final nodeList
                            for (k = 0; k < tempValue; k++) {
                                nodeList.add(tempArray[k]);
                            }
                        } else {
                            printBuffer += "-- Warning !! There is not enough nodes on the virtualNode " +
                                vnodeArray[i].getName() + " : " + " there are currently only " +
                                tempArray.length + " nodes " + " there should be at minimum " + npcols +
                                " nodes.\n" + "-- BE AWARE : The mapping will NOT be optimal !\n";
                            for (k = 0; k < tempArray.length; k++) {
                                nodeList.add(tempArray[k]);
                            }
                        }
                    }
                    nodes = nodeList.toArray(new Node[0]);
                }
            } else {
                vnode = vnodeArray[0];
                nodes = vnode.getNodes();
            }
            if (nodes == null) {
                throw new ProActiveException("No nodes found");
            }

            printBuffer += "" + nodes.length + " node" + (nodes.length == 1 ? "" : "s") + " found\n";
            System.out.println(printBuffer);

            // Pre-calculate the nzz value
            this.nzz = ((this.problemClass.na * (this.problemClass.nonzer + 1) * (this.problemClass.nonzer + 1)) / this.problemClass.NUM_PROCS) +
                ((this.problemClass.na * (this.problemClass.nonzer + 2 + (this.problemClass.NUM_PROCS / 256))) / this.npcols);

            ////////////////////////////////////////////////////////////////////
            // Group creation begins here ...
            ////////////////////////////////////////////////////////////////////

            // Fill the constructor arguments
            param = new Object[] { problemClass, new Integer(this.npcols), new Integer(this.nprows),
                    new Integer(this.nzz) };
            params = new Object[this.problemClass.NUM_PROCS][];
            for (i = 0; i < problemClass.NUM_PROCS; i++) {
                params[i] = param;
            }

            // Create the workers group
            workers = (WorkerCG) PASPMD.newSPMDGroup(WorkerCG.class.getName(), params, nodes);

            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(workers);

            workers.start();

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            HierarchicalTimerStatistics tstats = bstats.getTimerStatistics();
            EventStatistics estats = bstats.getEventsStatistics();

            Kernel.printEnd(this.problemClass, tstats.getMax(0, 0, 0), Double.valueOf(estats.getEventValue(
                    "mflops").toString()), (bstats.getInformation().indexOf("UNSUCCESSFUL") == -1));

            System.out.println(bstats);

        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will be called by the kill method of the Benchmark class
     * to terminate all workers.
     */
    public void killKernel() {
        workers.terminate();

        Group<WorkerCG> g = PAGroup.getGroup(workers);
        ProxyForGroup<WorkerCG> p = (ProxyForGroup<WorkerCG>) g;
        p.finalize();
    }

    public static void printStarted(String kernel, char className, long[] size, int nbIteration,
            int nbProcess, int nonzer, int shift) {
        Kernel.printStarted(kernel, className, size, nbIteration, nbProcess);
        System.out.println(" Number of nonzeroes per rows: " + nonzer);
        System.out.println(" Eigenvalue shift: " + shift);
    }
}
