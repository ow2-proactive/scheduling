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
package org.objectweb.proactive.benchmarks.NAS.IS;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
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
import org.objectweb.proactive.core.node.NodeFactory;


/**
 * Kernel IS
 *
 * A large integer sort. This kernel performs a sorting operation that is
 * important in "particle method" codes. It tests both integer computation
 * speed and communication performance.
 */
public class KernelIS extends Kernel {

    private ISProblemClass problemClass;

    /** The reference for the typed group of Workers */
    private WorkerIS workers;

    public KernelIS() {
    }

    public KernelIS(NASProblemClass pclass) {
        this.problemClass = (ISProblemClass) pclass;
    }

    public void runKernel(ProActiveDescriptor pad) throws ProActiveException {

        // The Array of virtual nodes
        VirtualNode[] vnodeArray;
        Node[] nodes;
        AllBucketSize allBucketSize = null;

        try {
            // Get vitualNodes from the descriptor pad
            vnodeArray = pad.getVirtualNodes();

            // Begin the distribution
            pad.activateMappings();

            nodes = super.vnodeMapping(vnodeArray, this.problemClass.NUM_PROCS);
            if (nodes == null) {
                throw new ProActiveException("No nodes found");
            }

            /* Creating AO for allBucketSize arrays management */
            int arraySize = this.problemClass.NUM_BUCKETS + this.problemClass.TEST_ARRAY_SIZE;

            Object[] param = new Object[] { this.problemClass };

            Object[][] params = new Object[this.problemClass.NUM_PROCS][];
            for (int i = 0; i < this.problemClass.NUM_PROCS; i++) {
                params[i] = param;
            }

            /* Creating group */
            workers = (WorkerIS) PASPMD.newSPMDGroup(WorkerIS.class.getName(), params, nodes);

            WorkerIS[] workersArray = PAGroup.getGroup(workers).toArray(new WorkerIS[0]);

            // Get the chosen worker from the array
            WorkerIS chosenOne = (workersArray.length <= 1 ? workersArray[0] : workersArray[1]);

            // Get the node from the URL
            Node chosenOneNode = NodeFactory.getNode(PAActiveObject.getActiveObjectNodeUrl(chosenOne));

            allBucketSize = (AllBucketSize) PAActiveObject
                    .newActive(AllBucketSize.class.getName(), new Object[] { workers,
                            new Integer(this.problemClass.NUM_PROCS), new Integer(arraySize) }, chosenOneNode);

            workers.setAllBucketSize(allBucketSize);

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

        Group<WorkerIS> g = PAGroup.getGroup(workers);
        ProxyForGroup<WorkerIS> p = (ProxyForGroup<WorkerIS>) g;
        p.finalize();
    }
}