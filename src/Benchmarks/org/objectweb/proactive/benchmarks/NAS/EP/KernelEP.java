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
package org.objectweb.proactive.benchmarks.NAS.EP;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.EventStatistics;
import org.objectweb.proactive.benchmarks.timit.util.HierarchicalTimerStatistics;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * Kernel EP
 *
 * An "Embarrassingly Parallel" kernel. It provides an estimate of the
 * upper achievable limits for floating point performance, i.e., the
 * performance without significant interprocessor communication.
 */
public class KernelEP extends Kernel {

    private EPProblemClass problemClass;
    private Node[] nodes;

    /* The reference for the typed group of Workers */
    private WorkerEP workers;

    public KernelEP() {
    }

    public KernelEP(NASProblemClass pclass) {
        this.problemClass = (EPProblemClass) pclass;
    }

    public void runKernel(ProActiveDescriptor pad) throws ProActiveException {
        Object[] param = new Object[] { problemClass };

        Object[][] params = new Object[problemClass.NUM_PROCS][];
        for (int i = 0; i < problemClass.NUM_PROCS; i++) {
            params[i] = param;
        }

        try {
            pad.activateMappings();
            nodes = super.vnodeMapping(pad.getVirtualNodes(), problemClass.NUM_PROCS);
            if (nodes == null) {
                throw new ProActiveException("No nodes found");
            }

            if (nodes.length < this.problemClass.NUM_PROCS) {
                System.err.println("Not enough nodes: get " + nodes.length + ", need " +
                    this.problemClass.NUM_PROCS);
            }

            workers = (WorkerEP) PASPMD.newSPMDGroup(WorkerEP.class.getName(), params, nodes);

            TimItManager tManager = TimItManager.getInstance();
            tManager.setTimedObjects(workers);

            workers.start();

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            HierarchicalTimerStatistics tstats = bstats.getTimerStatistics();
            EventStatistics estats = bstats.getEventsStatistics();

            KernelEP.printEnd(this.problemClass, tstats.getMax(0, 0, 0), Double.valueOf(estats.getEventValue(
                    "mflops").toString()), (bstats.getInformation().indexOf("UNSUCCESSFUL") == -1));

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
        System.out.print("\n\n NAS Parallel Benchmarks ProActive -- " + kernel + " Benchmark\n\n");
        System.out.println(" Class: " + className);
        System.out.println(" Number of random numbers generated: " + Math.pow(2., (size[0] + 1)));
        System.out.println(" Number of active processes: " + nbProcess);
    }
}
