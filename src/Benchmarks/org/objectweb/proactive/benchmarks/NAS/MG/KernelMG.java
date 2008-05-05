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
package org.objectweb.proactive.benchmarks.NAS.MG;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
import org.objectweb.proactive.benchmarks.NAS.util.Communicator;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.EventStatistics;
import org.objectweb.proactive.benchmarks.timit.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


/**
 * Kernel MG
 *
 * A simplified multi-grid kernel. It requires highly structured long
 * distance communication and tests both short and long distance data
 * communication.
 * It approximates a solution to the discrete Poisson problem.
 */
public class KernelMG extends Kernel {

    private MGProblemClass problemClass;

    /** The reference for the typed group of Workers */
    private WorkerMG workers;

    private Communicator communicator;

    /** The main constructor */
    public KernelMG(NASProblemClass pclass) {
        this.problemClass = (MGProblemClass) pclass;
    }

    public void runKernel(ProActiveDescriptor pad) throws ProActiveException {
        // The Array of virtual nodes
        VirtualNode[] vnodeArray;
        // The Array of nodes
        Node[] nodes;
        // The array of parameters for each worker
        Object[] param;
        // The array of parameters for all workers
        Object[][] params;

        try {
            // Get vitualNodes from the descriptor pad
            vnodeArray = pad.getVirtualNodes();

            // Begin the distribution
            pad.activateMappings();

            // Map nodes in a round robin mode
            nodes = super.vnodeMapping(vnodeArray, this.problemClass.NUM_PROCS);
            if (nodes == null) {
                throw new ProActiveException("No nodes found");
            }

            // Fill the constructor arguments
            param = new Object[] { this.problemClass };

            // Create the workers group
            params = new Object[this.problemClass.NUM_PROCS][];
            for (int i = 0; i < this.problemClass.NUM_PROCS; i++) {
                params[i] = param;
            }

            /* Creating group */
            workers = (WorkerMG) PASPMD.newSPMDGroup(WorkerMG.class.getName(), params, nodes);

            // Create the array view to select workers by their rank
            WorkerMG[] workersArray = PAGroup.getGroup(workers).toArray(new WorkerMG[0]);

            // Get the chosen worker from the array
            WorkerMG chosenOne = (workersArray.length <= 1 ? workersArray[0] : workersArray[1]);

            // Get the node from the URL
            Node chosenOneNode = NodeFactory.getNode(PAActiveObject.getActiveObjectNodeUrl(chosenOne));

            // Construct the communicator active object
            communicator = (Communicator) PAActiveObject.newActive(Communicator.class.getName(),
                    new Object[] { new Integer(this.problemClass.NUM_PROCS) }, chosenOneNode);

            communicator.setup();

            // Set the communicator stub for every body
            workers.setCommunicator(communicator);

            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(workers);

            workers.start();

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            HierarchicalTimerStatistics tstats = bstats.getTimerStatistics();
            EventStatistics estats = bstats.getEventsStatistics();

            Kernel.printEnd(this.problemClass, tstats.getMax(0, 0, 0), Double.valueOf(estats.getEventValue(
                    "mflops").toString()), (bstats.getInformation().indexOf("UNSUCCESSFUL") == -1));

            System.out.println(tManager.getBenchmarkStatistics());

        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will be called by the kill method of the Benchmark class
     * to terminate all workers.
     */
    public void killKernel() {
        workers.terminate();
    }

    public static void printStarted(String kernel, char className, long[] size, int nbIteration, int nbProcess) {
        Kernel.printStarted(kernel, className, size, nbIteration, nbProcess);
    }
}