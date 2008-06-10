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
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.util.Random;
import org.objectweb.proactive.benchmarks.NAS.util.ReduceAll;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;
import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventObserver;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.NodeException;


/**
 * Kernel EP
 * 
 * An "Embarrassingly Parallel" kernel. It provides an estimate of the upper achievable limits for
 * floating point performance, i.e., the performance without significant interprocessor
 * communication.
 */
public class WorkerEP extends Timed {

    public static final boolean COMMUNICATION_PATTERN_OBSERVING_MODE = false;
    private static final boolean TIMERS_ENABLED = true;

    public static final double R46 = Math.pow(0.5, 46);
    public static final long T46m1 = (long) Math.pow(2, 46) - 1;

    private int rank;
    private int groupSize;
    private Random rng;
    private EPProblemClass clss;
    private WorkerEP typedGroup;
    private ReduceAll allReduceOp;
    private int nbReceive;
    private double sx;
    private double sy;
    private double[] q;
    private int nq;
    private double gc;
    private double[] x;
    private Group<WorkerEP> groupWithoutMe;
    private WorkerEP typedGroupWithoutMe;

    /* Timer counters */
    private TimerCounter T_total = new TimerCounter("Total");
    private TimerCounter T_gaussian = new TimerCounter("Gaussian");
    private TimerCounter T_random = new TimerCounter("Random");
    private TimerCounter T_tabulation = new TimerCounter("Tabulation");
    public EventObserver E_mflops;
    private CommEventObserver nbCommObserver;
    private CommEventObserver commSizeObserver;

    private int reductorRank;

    public WorkerEP() {
    }

    public WorkerEP(EPProblemClass clss) {
        this.clss = clss;
        rng = new Random();
    }

    // M is the Log_2 of the number of complex pairs of uniform (0, 1) random numbers.
    // MK is the Log_2 of the size of each batch of uniform random numbers.
    // MK can be set for convenience on a given system, since it does not affect the results.
    public void start() {
        double an, a, s;
        double t1, t2, t3, t4;
        double x1, x2;
        double[] dum = new double[3 + 1];
        int mk, mm, nn, nk, nk2, np;
        int i, ik, kk, k;
        int no_large_nodes;
        int np_add;
        int k_offset;

        t2 = t3 = 0;

        groupSize = PASPMD.getMySPMDGroupSize();
        rank = PASPMD.getMyRank();
        typedGroup = (WorkerEP) PASPMD.getSPMDGroup();
        typedGroupWithoutMe = (WorkerEP) PAGroup.captureView(typedGroup);
        groupWithoutMe = PAGroup.getGroup(typedGroupWithoutMe);
        groupWithoutMe.remove(PAActiveObject.getStubOnThis());
        this.reductorRank = (this.groupSize == 1 ? 0 : 1);

        if (WorkerEP.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            this.nbCommObserver = new CommEventObserver("nbCommObserver", groupSize, rank);
            this.commSizeObserver = new CommEventObserver("commSizeObserver", this.groupSize, rank);
            super.activate(new EventObserver[] { nbCommObserver, commSizeObserver });
        }

        // Setup the timing system
        E_mflops = new DefaultEventObserver("mflops", DefaultEventData.MIN, DefaultEventData.MIN);

        super.activate(new TimerCounter[] { T_total, T_gaussian, T_random, T_tabulation },
                new EventObserver[] { E_mflops });

        if (rank == this.reductorRank) {
            try {
                allReduceOp = (ReduceAll) PAActiveObject.newActive(WorkerEP.ReduceSumOp.class.getName(),
                        new Object[] { typedGroup, new Integer(groupSize) });
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
                return;
            } catch (NodeException e) {
                e.printStackTrace();
                return;
            }
            typedGroupWithoutMe.setAllReduceOp(allReduceOp);
        }

        mk = 16;
        mm = clss.m - mk;
        nn = Random.ipow2(mm);
        nk = Random.ipow2(mk);
        nk2 = nk * 2;
        nq = 10;
        a = 1220703125.;
        s = 271828183.;

        x = new double[(nk2) + 1];
        q = new double[nq]; // start to 0
        dum[1] = 1.;
        dum[2] = 1.;
        dum[3] = 1.;
        if (rank == 0) {
            KernelEP.printStarted(clss.KERNEL_NAME, clss.PROBLEM_CLASS_NAME, new long[] { clss.m }, 0,
                    clss.NUM_PROCS);
        }

        // Compute the number of "batches" of random number pairs generated
        // per processor. Adjust if the number of processors does not evenly
        // divide the total number
        np = nn / groupSize;
        no_large_nodes = (nn % groupSize);
        if (groupSize < no_large_nodes) {
            np_add = 1;
        } else {
            np_add = 0;
        }
        np = np + np_add;

        if (np == 0) {
            System.out.println("Too many nodes: " + groupSize + " " + nn);
            return;
        }

        // Call the random number generator functions and initialize
        // the x-array to reduce the effects of paging on the timings.
        // Also, all mathematical functions that are used. Make
        // sure these initializations cannot be eliminated as dead code.
        dum[1] = rng.vranlc(0, dum[1], dum[2], dum, 3 + 1);
        dum[1] = this.randlc(dum[2], dum[3]);
        java.util.Arrays.fill(x, -1 * Math.pow(10, -99));
        x[0] = 0.0d;

        T_total.reset();
        T_gaussian.reset();
        T_random.reset();
        T_tabulation.reset();
        T_total.start();

        rng.vranlc1(0, (long) 0, (long) a, x);

        rng.setLSeed((long) a);
        rng.setLGmult((long) a);
        for (int j = 0; j <= mk; j++) {
            rng.lrandlc();
            rng.setLGmult(rng.getLSeed());
        }

        an = (double) rng.getLSeed();
        gc = 0.;
        sx = 0.;
        sy = 0.;

        // Each instance of this loop may be performed independently. We compute
        // the k offsets separately to take into account the fact that some nodes
        // have more numbers to generate than others
        if (np_add == 1) {
            k_offset = (rank * np) - 1;
        } else {
            k_offset = ((no_large_nodes * (np + 1)) + ((rank - no_large_nodes) * np)) - 1;
        }

        for (k = 1; k <= np; k++) {
            kk = k_offset + k;
            t1 = s;
            t2 = an;

            // Find starting seed t1 for this kk.
            for (i = 1; i <= 100; i++) {
                ik = kk / 2;
                if ((2 * ik) != kk) {
                    t1 = (double) (((long) t1 * (long) t2) & T46m1);
                }
                if (ik == 0) {
                    break;
                }
                t2 = (double) (((long) t2 * (long) t2) & T46m1);
                kk = ik;
            }

            // Compute uniform pseudorandom numbers.
            if (TIMERS_ENABLED) {
                T_random.start();
            }
            rng.vranlc1(nk2, (long) t1, (long) a, x);

            if (TIMERS_ENABLED) {
                T_random.stop();
            }

            // Compute Gaussian deviates by acceptance-rejection method and
            // totally counts in concentric square annuli. This loop is not
            // vectorizable.
            if (TIMERS_ENABLED) {
                T_gaussian.start();
            }
            for (i = 1; i <= nk; i++) {
                x1 = 2 * x[2 * i - 1] - 1.;
                x2 = 2 * x[2 * i] - 1.;
                t1 = x1 * x1 + x2 * x2;
                if (t1 <= 1.) {
                    t2 = Math.sqrt(-2 * Math.log(t1) / t1);
                    t3 = (x1 * t2);
                    t4 = (x2 * t2);
                    q[(byte) Math.max(Math.abs(t3), Math.abs(t4))]++;// += 1.;
                    sx += t3;
                    sy += t4;
                }
            }
            if (TIMERS_ENABLED) {
                T_gaussian.stop();
            }
        }
        T_tabulation.start();

        while (this.allReduceOp == null) {

            PAActiveObject.getBodyOnThis().serve(
                    PAActiveObject.getBodyOnThis().getRequestQueue().blockingRemoveOldest());
        }

        if (WorkerEP.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            this.notifyOneRank(this.reductorRank, 8 /* the size of sx */);
        }

        allReduceOp.sum(sx);

    } // start()

    /** The method to kill this Active object */
    public void terminate() {
        PAActiveObject.getBodyOnThis().terminate();
    }

    public void setAllReduceOp(ReduceAll allReduceOp) {
        this.allReduceOp = allReduceOp;
    }

    public void receiveSum(double d) {
        if (WorkerEP.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // If the current rank is 1 therefor the reductor is on the same node
            // so we simulate a broadcast of the recolted data
            if (this.rank == this.reductorRank) {
                this.notifyAllGroupRanks(8 /* the size of the parameter d */);
            }
        }

        if (nbReceive == 0) {
            sx = d;
            nbReceive++;

            if (WorkerEP.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // Notification of a communication to the reductor ( by convention it's the rank 1)
                // will be done by each rank
                this.notifyOneRank(this.reductorRank, 8 /* the size of sx */);
            }

            allReduceOp.sum(sy);
        } else if (nbReceive == 1) {
            sy = d;
            nbReceive++;

            if (WorkerEP.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // Notification of a communication to the reductor ( by convention it's the rank 1)
                // will be done by each rank
                this.notifyOneRank(this.reductorRank, TimIt.getObjectSize(q) /* the size of q */);
            }
            allReduceOp.sum(q);
        }
    }

    public void receiveSum(double[] tab) {
        if (WorkerEP.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // If the current rank is 1 therefor the reductor is on the same node
            // so we simulate a broadcast of the recolted data
            if (this.rank == this.reductorRank) {
                this.notifyAllGroupRanks(TimIt.getObjectSize(tab) /* the size of the parameter tab */);
            }
        }

        for (int i = 0; i < nq; i++) {
            q[i] = tab[i];
        }

        for (int i = 0; i <= (nq - 1); i++) {
            gc += q[i];
        }

        T_tabulation.stop();
        T_total.stop();

        PASPMD.totalBarrier("timeReduce");
        super.getEventObservable().notifyObservers(new Event(E_mflops, getMflops()));

        if (this.rank == 0) {
            super.finalizeTimed(rank, verify() ? "" : "UNSUCCESSFUL");
        } else {
            super.finalizeTimed(rank, "");
        }

    }

    private boolean verify() {
        double epsilon = .00000001d;
        boolean verified = false;

        switch (clss.m) {
            case 24:
                verified = ((Math.abs((sx - (-3247.834652034740)) / sx) <= epsilon) && (Math
                        .abs((sy - (-6958.407078382297)) / sy) <= epsilon));
                break;
            case 25:
                verified = ((Math.abs((sx - (-2863.319731645753)) / sx) <= epsilon) && (Math
                        .abs((sy - (-6320.053679109499)) / sy) <= epsilon));
                break;
            case 28:
                verified = ((Math.abs((sx - (-4295.875165629892)) / sx) <= epsilon) && (Math
                        .abs((sy - (-15807.32573678431)) / sy) <= epsilon));
                break;
            case 30:
                verified = ((Math.abs((sx - (40338.15542441498)) / sx) <= epsilon) && (Math
                        .abs((sy - (-26606.69192809235)) / sy) <= epsilon));
                break;
            case 32:
                verified = ((Math.abs((sx - (47643.67927995374)) / sx) <= epsilon) && (Math
                        .abs((sy - (-80840.72988043731)) / sy) <= epsilon));
                break;
            case 36:
                verified = ((Math.abs((sx - (198248.1200946593)) / sx) <= epsilon) && (Math
                        .abs((sy - (-102059.6636361769)) / sy) <= epsilon));
                break;
        }

        return verified;
    }

    private double randlc(double x, double a) {
        // This routine returns a uniform pseudorandom double precision number in the
        // range (0, 1) by using the linear congruential generator
        //
        // x_{k+1} = a x_k (mod 2^46)
        //
        // where 0 < x_k < 2^46 and 0 < a < 2^46. This scheme generates 2^44 numbers before
        // repeating. The argument A is the same as 'a' in the above formula, and X is the
        // same as x_0. A and X must be odd double precision integers in the range (1, 2^46).
        // The returned value RANDLC is normalized to be between 0 and 1, i.e.
        // RANDLC = 2^(-46) * x_1
        // X is updated to contain the new seed x_1, so that subsequent calls to RANDLC using
        // the same arguments will generate a continuous sequence.
        long Lx;
        long La;

        Lx = (long) x;
        La = (long) a;

        Lx = (Lx * La) & T46m1;
        double randlc = R46 * Lx;
        // randlcPtr = Lx;

        return randlc;
    }

    private double getMflops() {
        double timeInSec = T_total.getTotalTime() / 1000.0;
        double mflops = Math.pow(2.0, clss.m + 1) / timeInSec / 1000000.0;
        return mflops;
    }

    // METHODS USED TO OBSERVE NB COMMS AND COMM SIZE
    private void notifyOneRank(int destRank, int messageSize) {
        // Notification of 1 communication with the dest rank
        super.getEventObservable().notifyObservers(new CommEvent(this.nbCommObserver, destRank, 1));

        // Notification
        super.getEventObservable().notifyObservers(
                new CommEvent(this.commSizeObserver, destRank, messageSize));
    }

    private void notifyAllGroupRanks(int messageSize) {
        for (int i = 0; i < this.groupSize; i++) {
            this.notifyOneRank(i, messageSize);
        }
    }

    public static class ReduceSumOp extends ReduceAll {
        public static final long serialVersionUID = 1L;

        public ReduceSumOp() {
        }

        public ReduceSumOp(Object g, int n) {
            super(g, n);
        }

        protected void send() {
            if (super.tableD != null) {
                ((WorkerEP) super.typedGroup).receiveSum(super.tableD);
            } else if (super.sumDble != 0.) {
                ((WorkerEP) super.typedGroup).receiveSum(super.sumDble);
            }
            super.reset();
        }
    }
}