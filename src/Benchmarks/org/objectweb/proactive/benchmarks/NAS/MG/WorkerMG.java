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

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.util.Communicator;
import org.objectweb.proactive.benchmarks.NAS.util.NpbMath;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;
import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventObserver;
import org.objectweb.proactive.extra.hpc.exchange.ExchangeableDouble;
import org.objectweb.proactive.extra.hpc.exchange.Exchanger;


/**
 * Kernel MG
 *
 * A simplified multi-grid kernel. It requires highly structured long
 * distance communication and tests both short and long distance data
 * communication.
 * It approximates a solution to the discrete Poisson problem.
 */
public class WorkerMG extends Timed implements Serializable {

    /*
     * TimIt related variables
     */
    /** Enables the observing mode */
    public static final boolean COMMUNICATION_PATTERN_OBSERVING_MODE = false;

    // Event observers
    private EventObserver E_mflops;
    private CommEventObserver nbCommObserver;
    private CommEventObserver commSizeObserver;
    private int reductorRank;

    // Timer counters
    private TimerCounter T_total = new TimerCounter("Total");
    private TimerCounter T_init = new TimerCounter("Init");
    private TimerCounter T_bench = new TimerCounter("Bench");
    private TimerCounter T_bubble = new TimerCounter("Bubble");
    private TimerCounter T_comm3 = new TimerCounter("Comm3");
    private TimerCounter T_comm3_ex = new TimerCounter("Comm3Ex");
    private TimerCounter T_reduce_sum = new TimerCounter("ReduceSum");
    private TimerCounter T_reduce_min = new TimerCounter("ReduceMin");
    private TimerCounter T_reduce_max = new TimerCounter("ReduceMax");
    private TimerCounter T_reduce_max_array = new TimerCounter("ReduceMaxA");
    private TimerCounter T_psinv_loop = new TimerCounter("T_psinv_loop");

    /*
     * MG kernel specific variables
     */

    // Some MG constants
    private MGProblemClass clss;
    private static final int M = 1037;
    private static final int MM = 10;
    private static final double A = Math.pow(5.0, 13);
    private static final double X = 314159265.0;

    // Data for kernel computation
    private double[] u;
    private double[] v;
    private double[] r;
    private double[] a;
    private double[] c;
    private double rnm2;
    private double rnmu;
    private int n1Inst;
    private int n2Inst;
    private int n3Inst;
    private int lb;
    private int is1;
    private int is2;
    private int is3;
    private int ie1;
    private int ie2;
    private int ie3;
    private int[] nx;
    private int[] ny;
    private int[] nz;
    private int[] m1;
    private int[] m2;
    private int[] m3;
    private int[] ir;
    private boolean[] dead;
    private boolean[][] take_ex;
    private boolean[][] give_ex;
    private double[][] buff;
    private int[][][] nbr;
    private WorkerMG.MatrixEchanger matrixExchanger;
    private Exchanger exchanger;

    // Multi-dim variables
    private int ud0;
    private int ud01;
    private int rd0;
    private int rd01;
    private int vd0;
    private int vd01;
    private int zd0;
    private int zd01;
    private int sd0;
    private int sd01;

    // ProActive
    private int rank;
    private int groupSize;
    private boolean isLeader;
    private Body body;
    private Communicator communicator;
    private int iter;

    //
    // --------------- CONSTRUCTORS ---------------------
    //
    public WorkerMG() {
    }

    public WorkerMG(MGProblemClass clss) {
        E_mflops = new DefaultEventObserver("mflops", DefaultEventData.MIN, DefaultEventData.MIN);

        super.activate(new TimerCounter[] { T_total, T_init, T_bench, T_bubble, T_comm3, T_comm3_ex,
                T_reduce_sum, T_reduce_min, T_reduce_max, T_reduce_max_array, T_psinv_loop },
                new EventObserver[] { E_mflops });
        this.clss = clss;
    }

    public WorkerMG(MGProblemClass clss, Communicator comm) {
        E_mflops = new DefaultEventObserver("mflops", DefaultEventData.MIN, DefaultEventData.MIN);

        super.activate(new TimerCounter[] { T_total, T_init, T_bench, T_bubble, T_comm3, T_comm3_ex,
                T_reduce_sum, T_reduce_min, T_reduce_max, T_reduce_max_array, T_psinv_loop },
                new EventObserver[] { E_mflops });
        this.clss = clss;
        this.communicator = comm;
    }

    //
    // --------------- PUBLIC METHODS --------------------
    //
    /* The entry point of the MG benchmark */
    public void start() {
        this.rank = PASPMD.getMyRank();
        this.groupSize = PASPMD.getMySPMDGroupSize();
        this.body = PAActiveObject.getBodyOnThis();
        this.isLeader = (rank == 0);
        this.matrixExchanger = new MatrixEchanger(this);
        this.exchanger = Exchanger.getExchanger();

        this.reductorRank = ((this.groupSize == 1) ? 0 : 1);

        if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // Create a observer to observe the number of communication
            this.nbCommObserver = new CommEventObserver("nbCommObserver", groupSize, rank);

            // Create a observer to observe the size of the communication
            this.commSizeObserver = new CommEventObserver("commSizeObserver", this.groupSize, rank);

            super.activate(new EventObserver[] { nbCommObserver, commSizeObserver });
        }

        T_total.start();
        init();

        int k = clss.lt;

        T_bench.start();
        resid(u, 0, v, 0, r, 0, n1Inst, n2Inst, n3Inst, a, k);
        norm2u3(r, 0, n1Inst, n2Inst, n3Inst, nx[k], ny[k], nz[k]);

        // Main iteration
        for (this.iter = 0; this.iter < clss.niter; this.iter++) {
            mg3P(u, 0, v, 0, r, 0, a, c, n1Inst, n2Inst, n3Inst);
            resid(u, 0, v, 0, r, 0, n1Inst, n2Inst, n3Inst, a, k);

            if (isLeader) {
                System.out.println("iteration #" + iter);
            }
        }

        norm2u3(r, 0, n1Inst, n2Inst, n3Inst, nx[k], ny[k], nz[k]);
        T_bench.stop();
        T_total.stop();
        // ***** THE END *****

        // ***** finalization ****
        super.getEventObservable().notifyObservers(new Event(E_mflops, getMflops()));

        if (isLeader) {
            super.finalizeTimed(this.rank, verify() ? "" : "UNSUCCESSFUL");
        } else {
            super.finalizeTimed(this.rank, "");
        }
    } // start()

    public void terminate() {
    }

    public void setCommunicator(Communicator comm) {
        this.communicator = comm;
    }

    //
    // --------------- PRIVATE METHODS --------------------
    //
    private void init() {
        T_init.start();

        if (isLeader) {
            KernelMG.printStarted(clss.KERNEL_NAME, clss.PROBLEM_CLASS_NAME, new long[] { clss.nxSz,
                    clss.nySz, clss.nzSz }, clss.niter, clss.np);
        }

        r = new double[clss.nr + 1];
        v = new double[clss.nv + 1];
        u = new double[clss.nr + 1];

        // Init nbr
        this.nbr = new int[5][5][clss.maxLevel + 1];
        this.buff = new double[6][clss.nm2 * 2];

        // Init nx, ny, nz
        this.nx = new int[clss.maxLevel];
        this.ny = new int[clss.maxLevel];
        this.nz = new int[clss.maxLevel];
        this.m1 = new int[clss.maxLevel];
        this.m2 = new int[clss.maxLevel];
        this.m3 = new int[clss.maxLevel];

        this.nx[clss.lt] = clss.nxSz;
        this.ny[clss.lt] = clss.nySz;
        this.nz[clss.lt] = clss.nzSz;

        this.dead = new boolean[clss.maxLevel];
        this.give_ex = new boolean[4][clss.maxLevel];
        this.take_ex = new boolean[4][clss.maxLevel];

        this.ir = new int[clss.maxLevel];

        a = new double[] { -8.0 / 3.0, 0.0, 1.0 / 6.0, 1.0 / 12.0 };

        if ((clss.PROBLEM_CLASS_NAME == 'S') || (clss.PROBLEM_CLASS_NAME == 'W') ||
            (clss.PROBLEM_CLASS_NAME == 'A')) {
            c = new double[] { -3.0 / 8.0, 1.0 / 32.0, -1.0 / 64.0, 0.0 };
        } else {
            c = new double[] { -3.0 / 17.0, 1.0 / 33.0, -1.0 / 61.0, 0.0 };
        }

        int k = clss.lt;
        this.lb = 1;
        // Setup
        setup();
        zero3(u, 0, n1Inst, n2Inst, n3Inst);
        zran3(v, 0, n1Inst, n2Inst, n3Inst, nx[k], ny[k], k);
        norm2u3(v, 0, n1Inst, n2Inst, n3Inst, nx[k], ny[k], nz[k]);
        resid(u, 0, v, 0, r, 0, n1Inst, n2Inst, n3Inst, a, k);
        norm2u3(r, 0, n1Inst, n2Inst, n3Inst, nx[k], ny[k], nz[k]);

        // Warmup
        mg3P(u, 0, v, 0, r, 0, a, c, n1Inst, n2Inst, n3Inst);
        resid(u, 0, v, 0, r, 0, n1Inst, n2Inst, n3Inst, a, k);
        setup();
        zero3(u, 0, n1Inst, n2Inst, n3Inst);
        zran3(v, 0, n1Inst, n2Inst, n3Inst, nx[k], ny[k], k);

        if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // Notification of a global barrier synchronization
            this.notifyAllGroupRanks(TimIt.getObjectSize("warmup"));
        }

        PASPMD.totalBarrier("ready");
        this.blockingServe();
        T_init.stop();
    }

    private void setup() {
        int dx;
        int dy;
        int log_p;
        int dir;

        int[] idi = new int[4];
        int[] pi = new int[4];
        int[] next = new int[4];
        int[][] idin = new int[4][3];
        int[][] ng = new int[4][10];
        int[][] mi = new int[4][10];
        int[][] mip = new int[4][10];

        ng[1][clss.lt] = nx[clss.lt];
        ng[2][clss.lt] = ny[clss.lt];
        ng[3][clss.lt] = nz[clss.lt];

        next[1] = 1;

        for (int k = clss.lt - 1; k >= 1; k--)
            ng[1][k] = ng[1][k + 1] / 2;

        next[2] = 1;

        for (int k = clss.lt - 1; k >= 1; k--)
            ng[2][k] = ng[2][k + 1] / 2;

        next[3] = 1;

        for (int k = clss.lt - 1; k >= 1; k--)
            ng[3][k] = ng[3][k + 1] / 2;

        for (int k = clss.lt; k >= 1; k--) {
            nx[k] = ng[1][k];
            ny[k] = ng[2][k];
            nz[k] = ng[3][k];
        }

        log_p = NpbMath.ilog2(clss.np);

        dx = log_p / 3;
        pi[1] = (int) NpbMath.ipow2(dx);
        idi[1] = rank % pi[1];

        dy = (log_p - dx) / 2;
        pi[2] = (int) NpbMath.ipow2(dy);
        idi[2] = (rank / pi[1]) % pi[2];

        pi[3] = clss.np / (pi[1] * pi[2]);
        idi[3] = rank / (pi[1] * pi[2]);

        for (int k = clss.lt; k >= 1; k--) {
            dead[k] = false;

            for (int ax = 1; ax <= 3; ax++) {
                take_ex[ax][k] = false;
                give_ex[ax][k] = false;

                mi[ax][k] = (2 + (((idi[ax] + 1) * ng[ax][k]) / pi[ax])) -
                    (((idi[ax] + 0) * ng[ax][k]) / pi[ax]);
                mip[ax][k] = (2 + (((next[ax] + idi[ax] + 1) * ng[ax][k]) / pi[ax])) -
                    (((next[ax] + idi[ax] + 0) * ng[ax][k]) / pi[ax]);

                if ((mip[ax][k] == 2) || (mi[ax][k] == 2)) {
                    next[ax] = 2 * next[ax];
                }

                if ((k + 1) <= clss.lt) {
                    if ((mip[ax][k] == 2) && (mi[ax][k] == 3)) {
                        give_ex[ax][k + 1] = true;
                    }

                    if ((mip[ax][k] == 3) && (mi[ax][k] == 2)) {
                        take_ex[ax][k + 1] = true;
                    }
                }
            }

            if ((mi[1][k] == 2) || (mi[2][k] == 2) || (mi[3][k] == 2)) {
                dead[k] = true;
            }

            m1[k] = mi[1][k];
            m2[k] = mi[2][k];
            m3[k] = mi[3][k];

            idin[1][2] = (idi[1] + next[1] + pi[1]) % pi[1];
            idin[1][0] = (idi[1] - next[1] + pi[1]) % pi[1];

            idin[2][2] = (idi[2] + next[2] + pi[2]) % pi[2];
            idin[2][0] = (idi[2] - next[2] + pi[2]) % pi[2];

            idin[3][2] = (idi[3] + next[3] + pi[3]) % pi[3];
            idin[3][0] = (idi[3] - next[3] + pi[3]) % pi[3];

            for (dir = 2; dir >= 0; dir -= 2) {
                nbr[1][dir][k] = idin[1][dir] + (pi[1] * (idi[2] + (pi[2] * idi[3])));
                nbr[2][dir][k] = idi[1] + (pi[1] * (idin[2][dir] + (pi[2] * idi[3])));
                nbr[3][dir][k] = idi[1] + (pi[1] * (idi[2] + (pi[2] * idin[3][dir])));
            }
        }

        int k = clss.lt;

        is1 = (2 + ng[1][k]) - (((pi[1] - idi[1]) * ng[1][clss.lt]) / pi[1]);
        ie1 = (1 + ng[1][k]) - (((pi[1] - 1 - idi[1]) * ng[1][clss.lt]) / pi[1]);
        this.n1Inst = (3 + ie1) - is1;

        is2 = (2 + ng[2][k]) - (((pi[2] - idi[2]) * ng[2][clss.lt]) / pi[2]);
        ie2 = (1 + ng[2][k]) - (((pi[2] - 1 - idi[2]) * ng[2][clss.lt]) / pi[2]);
        this.n2Inst = (3 + ie2) - is2;

        is3 = (2 + ng[3][k]) - (((pi[3] - idi[3]) * ng[3][clss.lt]) / pi[3]);
        ie3 = (1 + ng[3][k]) - (((pi[3] - 1 - idi[3]) * ng[3][clss.lt]) / pi[3]);
        this.n3Inst = (3 + ie3) - is3;
        ir[clss.lt] = 1;

        for (int j = clss.lt - 1; j >= 1; j--) {
            ir[j] = ir[j + 1] + (m1[j + 1] * m2[j + 1] * m3[j + 1]);
        }

        k = clss.lt;
    } // setup

    private void mg3P(double[] u, int uoff, double[] v, int voff, double[] r, int roff, double[] a,
            double[] c, int n1, int n2, int n3) {
        /*---------------------------------------------------------------------
         *     multigrid V-cycle routine
         *--------------------------------------------------------------------*/
        int j;
        int k;

        /*---------------------------------------------------------------------
         *     down cycle.
         *     restrict the residual from the find grid to the coarse
         *--------------------------------------------------------------------*/
        for (k = clss.lt; k >= (lb + 1); k--) {
            j = k - 1;
            rprj3(r, (ir[k] + roff) - 1, m1[k], m2[k], m3[k], r, (ir[j] + roff) - 1, m1[j], m2[j], m3[j], k);
        }

        k = lb;

        /*---------------------------------------------------------------------
         *      compute an approximate solution on the coarsest grid
         *--------------------------------------------------------------------*/
        zero3(u, (ir[k] + uoff) - 1, m1[k], m2[k], m3[k]);
        psinv(r, (ir[k] + roff) - 1, u, (ir[k] + uoff) - 1, m1[k], m2[k], m3[k], c, k);

        for (k = lb + 1; k <= (clss.lt - 1); k++) {
            j = k - 1;

            /*-----------------------------------------------------------------
             *       prolongate from level k-1  to k
             *----------------------------------------------------------------*/
            zero3(u, (ir[k] + uoff) - 1, m1[k], m2[k], m3[k]);
            interp(u, (ir[j] + uoff) - 1, m1[j], m2[j], m3[j], u, (ir[k] + uoff) - 1, m1[k], m2[k], m3[k], k);

            /*-----------------------------------------------------------------
             *        compute residual for level k
             *----------------------------------------------------------------*/
            resid(u, (ir[k] + uoff) - 1, r, (ir[k] + roff) - 1, r, (ir[k] + roff) - 1, m1[k], m2[k], m3[k],
                    a, k);

            /*-----------------------------------------------------------------
             *        apply smoother
             *----------------------------------------------------------------*/
            psinv(r, (ir[k] + roff) - 1, u, (ir[k] + uoff) - 1, m1[k], m2[k], m3[k], c, k);
        }

        j = clss.lt - 1;
        k = clss.lt;
        interp(u, (ir[j] + uoff) - 1, m1[j], m2[j], m3[j], u, uoff, n1, n2, n3, k);
        resid(u, uoff, v, voff, r, roff, n1, n2, n3, a, k);
        psinv(r, roff, u, uoff, n1, n2, n3, c, k);
    } // mg3p

    private void resid(double[] u, int uoff, double[] v, int voff, double[] r, int roff, int n1, int n2,
            int n3, double[] a, int k) {
        double[] u1 = new double[M];
        double[] u2 = new double[M];

        this.usetDimension(n1, n2, n3);
        this.vsetDimension(n1, n2, n3);
        this.rsetDimension(n1, n2, n3);

        for (int i3 = 2; i3 <= (n3 - 1); i3++) {
            for (int i2 = 2; i2 <= (n2 - 1); i2++) {
                for (int i1 = 1; i1 <= n1; i1++) {
                    u1[i1] = u[uresolve(i1, (i2 - 1), i3) + uoff] + u[uresolve(i1, (i2 + 1), i3) + uoff] +
                        u[uresolve(i1, i2, i3 - 1) + uoff] + u[uresolve(i1, i2, i3 + 1) + uoff];
                    u2[i1] = u[uresolve(i1, (i2 - 1), i3 - 1) + uoff] +
                        u[uresolve(i1, (i2 + 1), i3 - 1) + uoff] + u[uresolve(i1, (i2 - 1), i3 + 1) + uoff] +
                        u[uresolve(i1, (i2 + 1), i3 + 1) + uoff];
                }

                for (int i1 = 2; i1 <= (n1 - 1); i1++) {
                    r[rresolve(i1, i2, i3) + roff] = v[vresolve(i1, i2, i3) + voff] -
                        (a[0] * u[uresolve(i1, i2, i3) + uoff]) -
                        (a[2] * (u2[i1] + u1[i1 - 1] + u1[i1 + 1])) - (a[3] * (u2[i1 - 1] + u2[i1 + 1]));
                }
            }
        }

        /*--------------------------------------------------------------------
         *     exchange boundary data
         *--------------------------------------------------------------------*/
        T_comm3.start();
        comm3(r, roff, n1, n2, n3, k);
        T_comm3.stop();
    }

    private void interp(double[] z, int zoff, int mm1, int mm2, int mm3, double[] u, int uoff, int n1,
            int n2, int n3, int k) {
        int d1;
        int d2;
        int d3;
        int t1;
        int t2;
        int t3;
        double[] z1 = new double[M];
        double[] z2 = new double[M];
        double[] z3 = new double[M];

        this.zsetDimension(mm1, mm2, mm3);
        this.usetDimension(n1, n2, n3);

        //		note that m = 1037 but for this only need to be 535 to handle up to
        //		1024^3
        if ((n1 != 3) && (n2 != 3) && (n3 != 3)) {
            for (int i3 = 1; i3 <= (mm3 - 1); i3++) {
                for (int i2 = 1; i2 <= (mm2 - 1); i2++) {
                    for (int i1 = 1; i1 <= mm1; i1++) {
                        z1[i1] = z[zresolve(i1, i2 + 1, i3) + zoff] + z[zresolve(i1, i2, i3) + zoff];
                        z2[i1] = z[zresolve(i1, i2, i3 + 1) + zoff] + z[zresolve(i1, i2, i3) + zoff];
                        z3[i1] = z[zresolve(i1, i2 + 1, i3 + 1) + zoff] + z[zresolve(i1, i2, i3 + 1) + zoff] +
                            z1[i1];
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - 1, (2 * i2) - 1, (2 * i3) - 1) + uoff] += z[zresolve(i1, i2, i3) +
                            zoff];
                        u[uresolve(2 * i1, (2 * i2) - 1, (2 * i3) - 1) + uoff] += (0.5 * (z[zresolve(i1 + 1,
                                i2, i3) +
                            zoff] + z[zresolve(i1, i2, i3) + zoff]));
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - 1, 2 * i2, (2 * i3) - 1) + uoff] += (0.5 * z1[i1]);
                        u[uresolve(2 * i1, 2 * i2, (2 * i3) - 1) + uoff] += (0.25 * (z1[i1] + z1[i1 + 1]));
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - 1, (2 * i2) - 1, 2 * i3) + uoff] += (0.5 * z2[i1]);
                        u[uresolve(2 * i1, (2 * i2) - 1, 2 * i3) + uoff] += (0.25 * (z2[i1] + z2[i1 + 1]));
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - 1, 2 * i2, 2 * i3) + uoff] += (0.25 * z3[i1]);
                        u[uresolve(2 * i1, 2 * i2, 2 * i3) + uoff] += (0.125 * (z3[i1] + z3[i1 + 1]));
                    }
                }
            }
        } else {
            d1 = (n1 == 3) ? 2 : 1;
            d2 = (n2 == 3) ? 2 : 1;
            d3 = (n3 == 3) ? 2 : 1;
            t1 = (n1 == 3) ? 1 : 0;
            t2 = (n2 == 3) ? 1 : 0;
            t3 = (n3 == 3) ? 1 : 0;

            for (int i3 = d3; i3 <= (mm3 - 1); i3++) {
                for (int i2 = d2; i2 <= (mm2 - 1); i2++) {
                    for (int i1 = d1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - d1, (2 * i2) - d2, (2 * i3) - d3) + uoff] += z[zresolve(i1, i2,
                                i3) +
                            zoff];
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - t1, (2 * i2) - d2, (2 * i3) - d3) + uoff] += (0.5 * (z[zresolve(
                                i1 + 1, i2, i3) +
                            zoff] + z[zresolve(i1, i2, i3) + zoff]));
                    }
                }

                for (int i2 = 1; i2 <= (mm2 - 1); i2++) {
                    for (int i1 = d1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - d1, (2 * i2) - t2, (2 * i3) - d3) + uoff] += (0.5 * (z[zresolve(
                                i1, i2 + 1, i3) +
                            zoff] + z[zresolve(i1, i2, i3) + zoff]));
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - t1, (2 * i2) - t2, (2 * i3) - d3) + uoff] += (0.25 * (z[zresolve(
                                i1 + 1, i2 + 1, i3) +
                            zoff] +
                            z[zresolve(i1 + 1, i2, i3) + zoff] + z[zresolve(i1, i2 + 1, i3) + zoff] + z[zresolve(
                                i1, i2, i3) +
                            zoff]));
                    }
                }
            }

            for (int i3 = 1; i3 <= (mm3 - 1); i3++) {
                for (int i2 = d2; i2 <= (mm2 - 1); i2++) {
                    for (int i1 = d1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - d1, (2 * i2) - d2, (2 * i3) - t3) + uoff] += (0.5 * (z[zresolve(
                                i1, i2, i3 + 1) +
                            zoff] + z[zresolve(i1, i2, i3) + zoff]));
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - t1, (2 * i2) - d2, (2 * i3) - t3) + uoff] += (0.25 * (z[zresolve(
                                i1 + 1, i2, i3 + 1) +
                            zoff] +
                            z[zresolve(i1, i2, i3 + 1) + zoff] + z[zresolve(i1 + 1, i2, i3) + zoff] + z[zresolve(
                                i1, i2, i3) +
                            zoff]));
                    }
                }

                for (int i2 = 1; i2 <= (mm2 - 1); i2++) {
                    for (int i1 = d1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - d1, (2 * i2) - t2, (2 * i3) - t3) + uoff] += (0.25 * (z[zresolve(
                                i1, i2 + 1, i3 + 1) +
                            zoff] +
                            z[zresolve(i1, i2, i3 + 1) + zoff] + z[zresolve(i1, i2 + 1, i3) + zoff] + z[zresolve(
                                i1, i2, i3) +
                            zoff]));
                    }

                    for (int i1 = 1; i1 <= (mm1 - 1); i1++) {
                        u[uresolve((2 * i1) - t1, (2 * i2) - t2, (2 * i3) - t3) + uoff] += (0.125 * (z[zresolve(
                                i1 + 1, i2 + 1, i3 + 1) +
                            zoff] +
                            z[zresolve(i1 + 1, i2, i3 + 1) + zoff] +
                            z[zresolve(i1, i2 + 1, i3 + 1) + zoff] +
                            z[zresolve(i1, i2, i3 + 1) + zoff] +
                            z[zresolve(i1 + 1, i2 + 1, i3) + zoff] +
                            z[zresolve(i1 + 1, i2, i3) + zoff] + z[zresolve(i1, i2 + 1, i3) + zoff] + z[zresolve(
                                i1, i2, i3) +
                            zoff]));
                    }
                }
            }
        }

        T_comm3_ex.start();
        comm3_ex(u, uoff, n1, n2, n3, k);
        T_comm3_ex.stop();
    }

    /*--------------------------------------------------------------------
     *     psinv applies an approximate inverse as smoother:  u = u + Cr
     *
     *     This  implementation costs  15A + 4M per result, where
     *     A and M denote the costs of Addition and Multiplication.
     *     Presuming coefficient c(3) is zero (the NPB assumes this,
     *     but it is thus not a general case), 2A + 1M may be eliminated,
     *     resulting in 13A + 3M.
     *     Note that this vectorizes, and is also fine for cache
     *     based machines.
     *-------------------------------------------------------------------*/
    private void psinv(double[] r, int roff, double[] u, int uoff, int n1, int n2, int n3, double[] c, int k) {
        double[] r1 = new double[M];
        double[] r2 = new double[M];

        this.rsetDimension(n1, n2, n2);
        this.usetDimension(n1, n2, n3);

        T_psinv_loop.start();

        for (int i3 = 2; i3 <= (n3 - 1); i3++) {
            for (int i2 = 2; i2 <= (n2 - 1); i2++) {
                for (int i1 = 1; i1 <= n1; i1++) {
                    r1[i1] = r[this.rresolve(i1, (i2 - 1), i3) + roff] +
                        r[this.rresolve(i1, (i2 + 1), i3) + roff] + r[this.rresolve(i1, i2, i3 - 1) + roff] +
                        r[this.rresolve(i1, i2, i3 + 1) + roff];

                    r2[i1] = r[this.rresolve(i1, (i2 - 1), i3 - 1) + roff] +
                        r[this.rresolve(i1, (i2 + 1), i3 - 1) + roff] +
                        r[this.rresolve(i1, (i2 - 1), i3 + 1) + roff] +
                        r[this.rresolve(i1, (i2 + 1), i3 + 1) + roff];
                }

                for (int i1 = 2; i1 <= (n1 - 1); i1++) {
                    u[this.uresolve(i1, i2, i3) + uoff] = u[this.uresolve(i1, i2, i3) + uoff] +
                        (c[0] * r[this.rresolve(i1, i2, i3) + roff]) +
                        (c[1] * (r[this.rresolve(i1 - 1, i2, i3) + roff] +
                            r[this.rresolve(i1 + 1, i2, i3) + roff] + r1[i1])) +
                        (c[2] * (r2[i1] + r1[i1 - 1] + r1[i1 + 1]));
                }
            }
        }

        T_psinv_loop.stop();

        /*--------------------------------------------------------------------
         *     exchange boundary points
         *-------------------------------------------------------------------*/
        T_comm3.start();
        comm3(u, uoff, n1, n2, n3, k);
        T_comm3.stop();
    }

    /*--------------------------------------------------------------------
     *     norm2u3 evaluates approximations to the L2 norm and the
     *     uniform (or L-infinity or Chebyshev) norm, under the
     *     assumption that the boundaries are periodic or zero.  Add the
     *     boundaries in with half weight (quarter weight on the edges
     *     and eighth weight at the corners) for inhomogeneous boundaries.
     *-------------------------------------------------------------------*/
    private void norm2u3(double[] r, int roff, int n1, int n2, int n3, int nx, int ny, int nz) {
        double a;
        double[] sum_max;
        double s = 0.0;
        int n = nx * ny * nz;
        this.rnmu = 0.0;
        this.rsetDimension(n1, n2, n3);

        for (int i3 = 2; i3 <= (n3 - 1); i3++) {
            for (int i2 = 2; i2 <= (n2 - 1); i2++) {
                for (int i1 = 2; i1 <= (n1 - 1); i1++) {
                    double tmp = r[rresolve(i1, i2, i3) + roff];
                    s += (tmp * tmp);
                    a = Math.abs(tmp);

                    if (a > this.rnmu) {
                        this.rnmu = a;
                    }
                }
            }
        }

        T_reduce_sum.start();
        sum_max = this.communicator.sumAndMax(s, rnmu); // XXX sumAndMax
        T_reduce_sum.stop();

        if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // Notification of a communication to the reductor ( by convention
            // it's the rank 1 ) to the communicator
            this.notifyOneRank(this.reductorRank, 16 /* the size of the message s and rnmu a doubles */);

            // The communicator will compute sum and max and broadcast an
            // array of double
            if (this.rank == 1) {
                this.notifyAllGroupRanks(TimIt.getObjectSize(sum_max));
            }
        }

        rnmu = sum_max[1];
        rnm2 = Math.sqrt(sum_max[0] / n);
    }

    private void comm3(double[] u, int uoff, int n1, int n2, int n3, int kk) {
        if (!dead[kk]) {
            for (int axis = 1; axis <= 3; axis++) {
                if (clss.np != 1) {
                    matrixExchanger.prepare(axis, +1, u, uoff, u, uoff, n1, n2, n3);
                    exchanger.exchange("pos" + axis, nbr[axis][2][kk], matrixExchanger, matrixExchanger);
                    matrixExchanger.prepare(axis, -1, u, uoff, u, uoff, n1, n2, n3);
                    exchanger.exchange("neg" + axis, nbr[axis][0][kk], matrixExchanger, matrixExchanger);
                } else {
                    comm1p(++axis, u, uoff, n1, n2, n3, kk);
                }
            }
        } else {
            zero3(u, uoff, n1, n2, n3);
        }
    }

    private void comm3_ex(double[] u, int uoff, int n1, int n2, int n3, int kk) {
        for (int axis = 1; axis <= 3; axis++) {
            if (clss.np != 1) {
                matrixExchanger.prepare(axis, +1, u, uoff, u, uoff, n1, n2, n3);
                exchanger.exchange("pos" + axis, nbr[axis][2][kk], matrixExchanger, matrixExchanger);
                matrixExchanger.prepare(axis, -1, u, uoff, u, uoff, n1, n2, n3);
                exchanger.exchange("neg" + axis, nbr[axis][0][kk], matrixExchanger, matrixExchanger);
            } else {
                comm1p_ex(axis, u, uoff, n1, n2, n3, kk);
            }
        }
    }

    private void comm1p(int axis, double[] u, int uoff, int n1, int n2, int n3, int kk) {
        int buff_len;
        int indx;

        this.usetDimension(n1, n2, n3);

        buff_len = 0;

        switch (axis) {
            case 1:

                for (int i3 = 2; i3 <= (n3 - 1); i3++) {
                    for (int i2 = 2; i2 <= (n2 - 1); i2++) {
                        buff[3][++buff_len] = u[uresolve(n1 - 1, i2, i3) + uoff];
                        buff[1][++buff_len] = u[uresolve(2, i2, i3) + uoff];
                    }
                }

                break;

            case 2:

                for (int i3 = 2; i3 <= (n3 - 1); i3++) {
                    for (int i1 = 1; i1 <= n1; i1++) {
                        buff[3][++buff_len] = u[uresolve(i1, n2 - 1, i3) + uoff];
                        buff[1][++buff_len] = u[uresolve(i1, 2, i3) + uoff];
                    }
                }

                break;

            case 3:

                for (int i2 = 1; i2 <= n2; i2++) {
                    for (int i1 = 1; i1 <= n1; i1++) {
                        buff[3][++buff_len] = u[uresolve(i1, i2, n3 - 1) + uoff];
                        buff[1][++buff_len] = u[uresolve(i1, i2, 2) + uoff];
                    }
                }

                break;
        }

        for (int i = 1; i <= clss.nm2; i++) {
            buff[4][i] = buff[3][i];
            buff[2][i] = buff[1][i];
        }

        indx = 0;

        switch (axis) {
            case 1:

                for (int i3 = 2; i3 <= (n3 - 1); i3++) {
                    for (int i2 = 2; i2 <= (n2 - 1); i2++) {
                        u[uresolve(n1, i2, i3) + uoff] = buff[2][++indx];
                        u[uresolve(1, i2, i3) + uoff] = buff[4][++indx];
                    }
                }

                break;

            case 2:

                for (int i3 = 2; i3 <= (n3 - 1); i3++) {
                    for (int i1 = 1; i1 <= n1; i1++) {
                        u[uresolve(i1, n2, i3) + uoff] = buff[2][++indx];
                        u[uresolve(i1, 1, i3) + uoff] = buff[4][++indx];
                    }
                }

                break;

            case 3:

                for (int i2 = 1; i2 <= n2; i2++) {
                    for (int i1 = 1; i1 <= n1; i1++) {
                        u[uresolve(i1, i2, n3) + uoff] = buff[2][++indx];
                        u[uresolve(i1, i2, 1) + uoff] = buff[4][++indx];
                    }
                }
        }
    }

    private void comm1p_ex(int axis, double[] u, int uoff, int n1, int n2, int n3, int kk) {
        int buff_len;
        int indx;

        this.usetDimension(n1, n2, n3);

        if (take_ex[axis][kk]) {
            buff_len = clss.nm2;

            for (int i = 1; i <= buff_len; i++) {
                buff[2][i] = 0;
                buff[4][i] = 0;
            }

            indx = 0;

            switch (axis) {
                case 1:

                    for (int i3 = 1; i3 <= n3; i3++) {
                        for (int i2 = 1; i2 <= n2; i2++) {
                            u[uresolve(n1, i2, i3) + uoff] = buff[2][++indx];
                            u[uresolve(1, i2, i3) + uoff] = buff[4][++indx];
                            u[uresolve(2, i2, i3) + uoff] = buff[4][++indx];
                        }
                    }

                    break;

                case 2:

                    for (int i3 = 1; i3 <= n3; i3++) {
                        for (int i1 = 1; i1 <= n1; i1++) {
                            u[uresolve(i1, n2, i3) + uoff] = buff[2][++indx];
                            u[uresolve(i1, 1, i3) + uoff] = buff[4][++indx];
                            u[uresolve(i1, 2, i3) + uoff] = buff[4][++indx];
                        }
                    }

                    break;

                case 3:

                    for (int i2 = 1; i2 <= n2; i2++) {
                        for (int i1 = 1; i1 <= n1; i1++) {
                            u[uresolve(i1, i2, n3) + uoff] = buff[2][++indx];
                            u[uresolve(i1, i2, 1) + uoff] = buff[4][++indx];
                            u[uresolve(i1, i2, 2) + uoff] = buff[4][++indx];
                        }
                    }

                    break;
            }
        }

        if (this.give_ex[axis][kk]) {
            buff_len = 0;

            switch (axis) {
                case 1:

                    for (int i3 = 1; i3 <= n3; i3++) {
                        for (int i2 = 1; i2 <= n2; i2++) {
                            buff[3][++buff_len] = u[uresolve(n1 - 1, i2, n3 - 1) + uoff];
                            buff[3][++buff_len] = u[uresolve(n1, i2, n3 - 1) + uoff];
                            buff[1][++buff_len] = u[uresolve(2, i2, i3) + uoff];
                        }
                    }

                    break;

                case 2:

                    for (int i3 = 1; i3 <= n3; i3++) {
                        for (int i1 = 1; i1 <= n1; i1++) {
                            buff[3][++buff_len] = u[uresolve(i1, n2 - 1, i3) + uoff];
                            buff[3][++buff_len] = u[uresolve(i1, n2, i3) + uoff];
                            buff[1][++buff_len] = u[uresolve(i1, 2, i3) + uoff];
                        }
                    }

                    break;

                case 3:

                    for (int i2 = 1; i2 <= n2; i2++) {
                        for (int i1 = 1; i1 <= n1; i1++) {
                            buff[3][++buff_len] = u[uresolve(i1, i2, n3 - 1) + uoff];
                            buff[3][++buff_len] = u[uresolve(i1, i2, n3) + uoff];
                            buff[1][++buff_len] = u[uresolve(i1, i2, 2) + uoff];
                        }
                    }
            }
        }

        for (int i = 1; i <= clss.nm2; i++) {
            buff[4][i] = buff[3][i];
            buff[2][i] = buff[1][i];
        }
    }

    /*--------------------------------------------------------------------
     *     rprj3 projects onto the next coarser grid,
     *     using a trilinear Finite Element projection:  s = r' = P r
     *
     *     This  implementation costs  20A + 4M per result, where
     *     A and M denote the costs of Addition and Multiplication.
     *     Note that this vectorizes, and is also fine for cache
     *     based machines.
     *-------------------------------------------------------------------*/
    private void rprj3(double[] r, int roff, int m1k, int m2k, int m3k, double[] s, int soff, int m1j,
            int m2j, int m3j, int k) {
        int d1;
        int d2;
        int d3;
        double[] x1 = new double[M];
        double[] y1 = new double[M];
        double x2;
        double y2;
        this.rsetDimension(m1k, m2k, m3k);
        this.ssetDimension(m1j, m2j, m3j);

        d1 = (m1k == 3) ? 2 : 1;
        d2 = (m2k == 3) ? 2 : 1;
        d3 = (m3k == 3) ? 2 : 1;

        for (int j3 = 2; j3 <= (m3j - 1); j3++) {
            int i3 = (2 * j3) - d3;

            for (int j2 = 2; j2 <= (m2j - 1); j2++) {
                int i2 = (2 * j2) - d2;

                for (int j1 = 2; j1 <= m1j; j1++) {
                    int i1 = (2 * j1) - d1;
                    x1[i1 - 1] = r[rresolve(i1 - 1, i2 - 1, i3) + roff] +
                        r[rresolve(i1 - 1, i2 + 1, i3) + roff] + r[rresolve(i1 - 1, i2, i3 - 1) + roff] +
                        r[rresolve(i1 - 1, i2, i3 + 1) + roff];

                    y1[i1 - 1] = r[rresolve(i1 - 1, i2 - 1, i3 - 1) + roff] +
                        r[rresolve(i1 - 1, i2 - 1, i3 + 1) + roff] +
                        r[rresolve(i1 - 1, i2 + 1, i3 - 1) + roff] +
                        r[rresolve(i1 - 1, i2 + 1, i3 + 1) + roff];
                }

                for (int j1 = 2; j1 <= (m1j - 1); j1++) {
                    int i1 = (2 * j1) - d1;

                    y2 = r[rresolve(i1, i2 - 1, i3 - 1) + roff] + r[rresolve(i1, i2 - 1, i3 + 1) + roff] +
                        r[rresolve(i1, i2 + 1, i3 - 1) + roff] + r[rresolve(i1, i2 + 1, i3 + 1) + roff];

                    x2 = r[rresolve(i1, i2 - 1, i3) + roff] + r[rresolve(i1, i2 + 1, i3) + roff] +
                        r[rresolve(i1, i2, i3 - 1) + roff] + r[rresolve(i1, i2, i3 + 1) + roff];

                    s[sresolve(j1, j2, j3) + soff] = (0.5 * r[rresolve(i1, i2, i3) + roff]) +
                        (0.25 * (r[rresolve(i1 - 1, i2, i3) + roff] + r[rresolve(i1 + 1, i2, i3) + roff] + x2)) +
                        (0.125 * (x1[i1 - 1] + x1[i1 + 1] + y2)) + (0.0625 * (y1[i1 - 1] + y1[i1 + 1]));
                }
            }
        }

        T_comm3.start();
        comm3(s, soff, m1j, m2j, m3j, k - 1);
        T_comm3.stop();
    }

    private void bubble(double[][] ten, int[][] j1, int[][] j2, int[][] j3, int m, int ind) {
        double temp;
        int j_temp;

        T_bubble.start();

        if (ind == 1) {
            for (int i = 1; i <= (m - 1); i++) {
                if (ten[i][ind] > ten[i + 1][ind]) {
                    temp = ten[i + 1][ind];
                    ten[i + 1][ind] = ten[i][ind];
                    ten[i][ind] = temp;

                    j_temp = j1[i + 1][ind];
                    j1[i + 1][ind] = j1[i][ind];
                    j1[i][ind] = j_temp;

                    j_temp = j2[i + 1][ind];
                    j2[i + 1][ind] = j2[i][ind];
                    j2[i][ind] = j_temp;

                    j_temp = j3[i + 1][ind];
                    j3[i + 1][ind] = j3[i][ind];
                    j3[i][ind] = j_temp;
                } else {
                    T_bubble.stop();

                    return;
                }
            }
        } else {
            for (int i = 1; i <= (m - 1); i++) {
                if (ten[i][ind] < ten[i + 1][ind]) {
                    temp = ten[i + 1][ind];
                    ten[i + 1][ind] = ten[i][ind];
                    ten[i][ind] = temp;

                    j_temp = j1[i + 1][ind];
                    j1[i + 1][ind] = j1[i][ind];
                    j1[i][ind] = j_temp;

                    j_temp = j2[i + 1][ind];
                    j2[i + 1][ind] = j2[i][ind];
                    j2[i][ind] = j_temp;

                    j_temp = j3[i + 1][ind];
                    j3[i + 1][ind] = j3[i][ind];
                    j3[i][ind] = j_temp;
                } else {
                    T_bubble.stop();

                    return;
                }
            }
        }

        T_bubble.stop();
    }

    private void zero3(double[] z, int zoff, int n1, int n2, int n3) {
        this.zsetDimension(n1, n2, n3);

        for (int i3 = 1; i3 <= n3; i3++) {
            for (int i2 = 1; i2 <= n2; i2++) {
                for (int i1 = 1; i1 <= n1; i1++) {
                    z[zresolve(i1, i2, i3) + zoff] = 0.0;
                }
            }
        }
    }

    /*--------------------------------------------------------------------
     *     zran3  loads +1 at ten randomly chosen points,
     *     loads -1 at a different ten random points,
     *     and zero elsewhere.
     *--------------------------------------------------------------------*/
    private void zran3(double[] z, int zoff, int n1, int n2, int n3, int nx, int ny, int k) {
        int m0;
        int m1;
        int d1;
        int e2;
        int e3;
        double a1;
        double a2;
        double ai;
        double temp;
        double best;

        double[] xxptr = new double[1];
        double[] x0ptr = new double[1];
        double[] x1ptr = new double[1];

        double[][] ten = new double[MM + 1][2];
        int[][] j1 = new int[MM + 1][2];
        int[][] j2 = new int[MM + 1][2];
        int[][] j3 = new int[MM + 1][2];
        int[][][] jg = new int[5][MM + 1][2];
        int[] jg_temp;

        this.zsetDimension(n1, n2, n3);

        a1 = NpbMath.power(A, nx);
        a2 = NpbMath.power(A, nx * ny);

        zero3(z, zoff, n1, n2, n3);

        ai = NpbMath.power(A, is1 - 2 + (nx * (is2 - 2 + (ny * (is3 - 2)))));
        d1 = ie1 - is1 + 1;
        e2 = ie2 - is2 + 2;
        e3 = ie3 - is3 + 2;
        x0ptr[0] = X;
        NpbMath.randlc(x0ptr, ai);

        for (int i3 = 2; i3 <= e3; i3++) {
            x1ptr[0] = x0ptr[0];

            for (int i2 = 2; i2 <= e2; i2++) {
                xxptr[0] = x1ptr[0];
                NpbMath.vranlc(d1, xxptr, A, z, zresolve(2, i2, i3) + zoff);
                NpbMath.randlc(x1ptr, a1);
            }

            NpbMath.randlc(x0ptr, a2);
        }

        /*--------------------------------------------------------------------
         *       call comm3(z,n1,n2,n3)
         *       call showall(z,n1,n2,n3)
         *-------------------------------------------------------------------*/

        /*--------------------------------------------------------------------
         *     each processor looks for twenty candidates
         *-------------------------------------------------------------------*/
        for (int i = 1; i <= MM; i++) {
            ten[i][1] = 0.0;
            j1[i][1] = 0;
            j2[i][1] = 0;
            j3[i][1] = 0;
            ten[i][0] = 1.0;
            j1[i][0] = 0;
            j2[i][0] = 0;
            j3[i][0] = 0;
        }

        for (int i3 = 2; i3 <= (n3 - 1); i3++) {
            for (int i2 = 2; i2 <= (n2 - 1); i2++) {
                for (int i1 = 2; i1 <= (n1 - 1); i1++) {
                    temp = z[zresolve(i1, i2, i3) + zoff];

                    if (temp > ten[1][1]) {
                        ten[1][1] = temp;
                        j1[1][1] = i1;
                        j2[1][1] = i2;
                        j3[1][1] = i3;
                        bubble(ten, j1, j2, j3, MM, 1);
                    }

                    if (temp < ten[1][0]) {
                        ten[1][0] = temp;
                        j1[1][0] = i1;
                        j2[1][0] = i2;
                        j3[1][0] = i3;
                        bubble(ten, j1, j2, j3, MM, 0);
                    }
                }
            }
        }

        if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // Notification of a global barrier synchronization
            this.notifyAllGroupRanks(TimIt.getObjectSize("zran3a" + this.iter));
        }
        PASPMD.totalBarrier("zran3a");
        this.blockingServe();

        /*--------------------------------------------------------------------
         *     Now which of these are globally best?
         *-------------------------------------------------------------------*/
        int i1 = MM;
        int i0 = MM;

        for (int i = MM; i >= 1; i--) {
            best = z[zresolve(j1[i1][1], j2[i1][1], j3[i1][1]) + zoff];

            T_reduce_max.start();
            temp = this.communicator.max(best); // XXX REDUCES MAX
            T_reduce_max.stop();

            if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // Notification of a communication to the reductor ( by
                // convention it's the rank 1 ) to the communicator
                this.notifyOneRank(this.reductorRank, 8 /* the size of the message */);

                // The communicator will compute the max and broadcast it
                if (this.rank == 1) {
                    this.notifyAllGroupRanks(8);
                }
            }

            best = temp;

            if (best == z[zresolve(j1[i1][1], j2[i1][1], j3[i1][1]) + zoff]) {
                jg[0][i][1] = rank;
                jg[1][i][1] = is1 - 2 + j1[i1][1];
                jg[2][i][1] = is2 - 2 + j2[i1][1];
                jg[3][i][1] = is3 - 2 + j3[i1][1];
                i1--;
            } else {
                jg[0][i][1] = 0;
                jg[1][i][1] = 0;
                jg[2][i][1] = 0;
                jg[3][i][1] = 0;
            }

            ten[i][1] = best;

            // XXX Reduce Max array
            T_reduce_max_array.start();
            jg_temp = this.communicator.max(new int[] { jg[0][i][1], jg[1][i][1], jg[2][i][1], jg[3][i][1] });
            T_reduce_max_array.stop();

            if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // Notification of a communication to the reductor ( by
                // convention it's the rank 1 ) to the communicator
                this
                        .notifyOneRank(this.reductorRank, TimIt.getObjectSize(jg_temp) /* the size of the message */);

                // The communicator will compute the max and broadcast it
                if (this.rank == 1) {
                    this.notifyAllGroupRanks(TimIt.getObjectSize(jg_temp));
                }
            }

            jg[0][i][1] = jg_temp[0];
            jg[1][i][1] = jg_temp[1];
            jg[2][i][1] = jg_temp[2];
            jg[3][i][1] = jg_temp[3];
            best = z[zresolve(j1[i0][0], j2[i0][0], j3[i0][0]) + zoff];

            // XXX Reduce Min
            T_reduce_min.start();
            best = this.communicator.min(best);
            T_reduce_min.stop();

            if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // Notification of a communication to the reductor ( by
                // convention it's the rank 1 ) to the communicator
                this.notifyOneRank(this.reductorRank, 8 /* the size of the message */);

                // / The communicator will compute the min and broadcast it
                if (this.rank == 1) {
                    this.notifyAllGroupRanks(8);
                }
            }

            if (best == z[zresolve(j1[i0][0], j2[i0][0], j3[i0][0]) + zoff]) {
                jg[0][i][0] = rank;
                jg[1][i][0] = is1 - 2 + j1[i0][0];
                jg[2][i][0] = is2 - 2 + j2[i0][0];
                jg[3][i][0] = is3 - 2 + j3[i0][0];
                i0--;
            } else {
                jg[0][i][0] = 0;
                jg[1][i][0] = 0;
                jg[2][i][0] = 0;
                jg[3][i][0] = 0;
            }

            ten[i][0] = best;

            // XXX Reduce Max array
            T_reduce_max_array.start();
            jg_temp = this.communicator.max(new int[] { jg[0][i][0], jg[1][i][0], jg[2][i][0], jg[3][i][0] });
            T_reduce_max_array.stop();

            if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // Notification of a communication to the reductor ( by
                // convention it's the rank 1 ) to the communicator
                this
                        .notifyOneRank(this.reductorRank, TimIt.getObjectSize(jg_temp) /* the size of the message */);

                // / The communicator will compute the max and broadcast it
                if (this.rank == 1) {
                    this.notifyAllGroupRanks(TimIt.getObjectSize(jg_temp));
                }
            }

            jg[0][i][0] = jg_temp[0];
            jg[1][i][0] = jg_temp[1];
            jg[2][i][0] = jg_temp[2];
            jg[3][i][0] = jg_temp[3];
        }

        m1 = i1 + 1;
        m0 = i0 + 1;

        if (WorkerMG.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // Notification of a global barrier synchronization
            this.notifyAllGroupRanks(TimIt.getObjectSize("zran3b" + this.iter));
        }
        PASPMD.totalBarrier("zran3b");
        this.blockingServe();

        this.zsetDimension(n1, n2, n3);
        zero3(z, zoff, n1, n2, n3);
        this.zsetDimension(n1, n2, n3);

        for (int i = MM; i >= m0; i--) {
            z[zresolve(j1[i][0], j2[i][0], j3[i][0]) + zoff] = -1.0;
        }

        for (int i = MM; i >= m1; i--) {
            z[zresolve(j1[i][1], j2[i][1], j3[i][1]) + zoff] = +1.0;
        }

        T_comm3.start();
        comm3(z, zoff, n1, n2, n3, k);
        T_comm3.stop();
    }

    private boolean verify() {
        double epsilon = 0.000000001;
        double verify_value = 0;
        boolean verified;

        switch (clss.PROBLEM_CLASS_NAME) {
            case 'S':
                verify_value = 0.0000530770700573;

                break;

            case 'W':
                verify_value = 0.00000646732937534;

                break;

            case 'A':
                verify_value = 0.00000243336530907;

                break;

            case 'B':
                verify_value = 0.00000180056440136;

                break;

            case 'C':
                verify_value = 0.000000570673228574;

                break;

            case 'D':
                verify_value = 0.000000000158327506043;
        }

        if (Math.abs(rnm2 - verify_value) <= epsilon) {
            verified = true;
            System.out.println(" VERIFICATION SUCCESSFUL\n");
            System.out.println(" L2 Norm is " + rnm2);
            System.out.println(" Error is " + (rnm2 - verify_value));
        } else {
            verified = false;
            System.out.println(" VERIFICATION FAILED\n");
            System.out.println(" L2 Norm is             " + rnm2);
            System.out.println(" The correct L2 Norm is " + verify_value);
        }

        return verified;
    }

    private double getMflops() {
        double time = T_total.getTotalTime() / 1000.0;
        double nn = nx[clss.lt] * ny[clss.lt] * nz[clss.lt];
        double mflops = (58 * clss.niter * nn * 0.000001) / time;

        return mflops;
    }

    /*
     * These methods are usefull to play with arrays as it is a 1-dim or a 3-dim
     */
    private void usetDimension(int d0, int d1, int d2) {
        this.ud0 = d0;
        this.ud01 = ud0 * d1;
    }

    private final int uresolve(int a, int b, int c) {
        return a + ((b - 1) * ud0) + ((c - 1) * ud01);
    }

    private void vsetDimension(int d0, int d1, int d2) {
        this.vd0 = d0;
        this.vd01 = vd0 * d1;
    }

    private final int vresolve(int a, int b, int c) {
        return a + ((b - 1) * vd0) + ((c - 1) * vd01);
    }

    private void rsetDimension(int d0, int d1, int d2) {
        this.rd0 = d0;
        this.rd01 = rd0 * d1;
    }

    private final int rresolve(int a, int b, int c) {
        return a + ((b - 1) * rd0) + ((c - 1) * rd01);
    }

    private void zsetDimension(int d0, int d1, int d2) {
        this.zd0 = d0;
        this.zd01 = zd0 * d1;
    }

    private final int zresolve(int a, int b, int c) {
        return a + ((b - 1) * zd0) + ((c - 1) * zd01);
    }

    private void ssetDimension(int d0, int d1, int d2) {
        this.sd0 = d0;
        this.sd01 = sd0 * d1;
    }

    private final int sresolve(int a, int b, int c) {
        return a + ((b - 1) * sd0) + ((c - 1) * sd01);
    }

    /*
     * Forces the associated worker used to block the treatment of the
     * requestQueue.
     */
    private final void blockingServe() {
        body.serve(body.getRequestQueue().blockingRemoveOldest());
    }

    /*
     * Methods used to observe the number and the size of communication
     */
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

    private class MatrixEchanger implements ExchangeableDouble {
        private WorkerMG worker;
        private int axis;
        private double[] src_u;
        private double[] dst_u;
        private int src_uoff;
        private int dst_uoff;
        private int len;
        private int getPos; // current position
        private int putPos; // current position
        private int src_x;
        private int src_y;
        private int src_z;
        private int dst_x;
        private int dst_y;
        private int dst_z;
        private int x0;
        private int x1;
        private int y0;
        private int y1;
        private int z0;

        //        private int z1; // Never used

        public MatrixEchanger(WorkerMG worker) {
            this.worker = worker;
        }

        public void prepare(int axis, int dir, double[] src_u, int src_uoff, double[] dst_u, int dst_uoff,
                int n1, int n2, int n3) {
            worker.usetDimension(n1, n2, n3);
            this.getPos = 1;
            this.putPos = 1;
            this.src_u = src_u;
            this.dst_u = dst_u;
            this.src_uoff = src_uoff;
            this.dst_uoff = dst_uoff;
            this.axis = axis;

            switch (axis) {
                case 1:
                    len = ((n2 - 2) * (n3 - 2)) + 1;
                    x0 = 2; // neg
                    x1 = n1 - 1; // pos
                    y0 = 2;
                    y1 = n2 - 1;
                    z0 = 2;
                    //                    z1 = n3 - 1;

                    if (dir == -1) {
                        src_x = 2;
                        dst_x = n1;
                    } else {
                        src_x = n1 - 1;
                        dst_x = 1;
                    }

                    src_y = y0;
                    dst_y = y0;
                    src_z = z0;
                    dst_z = z0;

                    break;

                case 2:
                    len = (n1 * (n3 - 2)) + 1;
                    x0 = 1;
                    x1 = n1;
                    y0 = 2; // neg
                    y1 = n2 - 1; // pos
                    z0 = 2;
                    //                    z1 = n3 - 1;
                    src_x = x0;
                    dst_x = x0;

                    if (dir == -1) {
                        src_y = 2;
                        dst_y = n2;
                    } else {
                        src_y = n2 - 1;
                        dst_y = 1;
                    }

                    src_z = z0;
                    dst_z = z0;

                    break;

                case 3:
                    len = (n1 * n2) + 1;
                    x0 = 1;
                    x1 = n1;
                    y0 = 1;
                    y1 = n2;
                    z0 = 2; // neg
                    //                    z1 = n3 - 1; // pos
                    src_x = x0;
                    dst_x = x0;
                    src_y = y0;
                    dst_y = y0;

                    if (dir == -1) {
                        src_z = 2;
                        dst_z = n3;
                    } else {
                        src_z = n3 - 1;
                        dst_z = 1;
                    }

                    break;
            }
        }

        public double get() {
            getPos++;

            double res = src_u[worker.uresolve(src_x, src_y, src_z) + src_uoff];

            switch (axis) {
                case 1: // x

                    if (src_y < y1) {
                        src_y++;
                    } else {
                        src_z++;
                        src_y = y0;
                    }

                    break;

                case 2: // y

                    if (src_x < x1) {
                        src_x++;
                    } else {
                        src_z++;
                        src_x = x0;
                    }

                    break;

                case 3: // z

                    if (src_x < x1) {
                        src_x++;
                    } else {
                        src_y++;
                        src_x = x0;
                    }

                    break;
            }

            return res;
        }

        public boolean hasNextGet() {
            return (getPos < len);
        }

        public boolean hasNextPut() {
            return (putPos < len);
        }

        public void put(double value) {
            putPos++;
            dst_u[worker.uresolve(dst_x, dst_y, dst_z) + dst_uoff] = value;

            switch (axis) {
                case 1: // x

                    if (dst_y < y1) {
                        dst_y++;
                    } else {
                        dst_z++;
                        dst_y = y0;
                    }

                    break;

                case 2: // y

                    if (dst_x < x1) {
                        dst_x++;
                    } else {
                        dst_z++;
                        dst_x = x0;
                    }

                    break;

                case 3: // z

                    if (dst_x < x1) {
                        dst_x++;
                    } else {
                        dst_y++;
                        dst_x = x0;
                    }

                    break;
            }
        }
    }
}
