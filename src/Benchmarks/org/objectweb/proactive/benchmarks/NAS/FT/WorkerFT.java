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
package org.objectweb.proactive.benchmarks.NAS.FT;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.util.Complex;
import org.objectweb.proactive.benchmarks.NAS.util.ComplexArray;
import org.objectweb.proactive.benchmarks.NAS.util.ComplexArrayGroup;
import org.objectweb.proactive.benchmarks.NAS.util.Reduce;
import org.objectweb.proactive.benchmarks.NAS.util.Shift;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;
import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventObserver;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.topology.Plan;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * NAS PARALLEL BENCHMARKS
 * 
 * Kernel FT
 * 
 * A 3-D partial differential equation solution using FFTs. This kernel performs the essence of many
 * "spectral" codes. It is a rigorous test of long-distance communication performance.
 */
public class WorkerFT extends Timed implements Serializable {

    public static final boolean COMMUNICATION_PATTERN_OBSERVING_MODE = false;

    // Timers
    public TimerCounter T_total = new TimerCounter("Total");
    public TimerCounter T_setup = new TimerCounter("Setup");
    public TimerCounter T_fft = new TimerCounter("FFT");
    public TimerCounter T_evolve = new TimerCounter("Evolve");
    public TimerCounter T_checksum = new TimerCounter("Checksum");
    public TimerCounter T_fftlow = new TimerCounter("FFT low");
    public TimerCounter T_fftcopy = new TimerCounter("FFT copy");
    public TimerCounter T_transpose = new TimerCounter("Transpose");
    public TimerCounter T_transxzloc = new TimerCounter("TransXZ loc");
    public TimerCounter T_transxzglo = new TimerCounter("TransXZ glo");
    public TimerCounter T_transxzfin = new TimerCounter("TransXZ fin");
    public TimerCounter T_transxyloc = new TimerCounter("TransXY loc");
    public TimerCounter T_transxyglo = new TimerCounter("TransXY glo");
    public TimerCounter T_transxyfin = new TimerCounter("TransXY fin");
    // Event observers
    public CommEventObserver E_count;
    public CommEventObserver E_size;
    public EventObserver E_mflops;

    // FT
    private FTProblemClass clss;
    private RandomFT rng;
    private static final int fftblock_default = 16;
    private static final int fftblockpad_default = 18;
    private static final int transblock = 32;
    private static final int transblockpad = 34;

    // values for random data generation
    public static final double d2m46 = Math.pow(0.5, 46);
    public static final long i246m1 = (long) Math.pow(2, 46) - 1;
    private static final double seed = 314159265.;
    private static final double a = 1220703125.;
    private static final double pi = 3.141592653589793238;
    private static final double alpha = .000001;

    // communicators
    private WorkerFT commslice1;
    private WorkerFT commslice2;
    private int commslice1Size;
    private int commslice2Size;

    // data for kernel computation
    private double[] twiddle;
    private ComplexArrayGroup u0, u1, u2, scratch;
    private ComplexArray cplxArray;
    private double[][] cplxSums;
    private double[][] u;
    private int[] xstart;
    private int[] xend;
    private int[] ystart;
    private int[] yend;
    private int[] zstart;
    private int[] zend;
    private int fftblock;
    private int fftblockpad;
    private int[] dims11;
    private int[] dims12;
    private int[] dims13;
    private int[][] dims1;
    private int iteration = 1;
    private int checkSumCount = iteration;
    private Complex[][] z;

    // Double arrays multi-dim
    private int d0, d01;

    // reducing operations
    private Reduce reduce;
    private int nbReceiveDataField;
    private boolean reduceStatus;
    private Complex allchk;

    // ProActive
    private int rank;
    private int groupSize;
    private boolean isLeader;
    private WorkerFT workers, me;
    private Body body;

    //
    // --------------- CONSTRUCTORS ---------------------
    //
    public WorkerFT() {
    }

    public WorkerFT(FTProblemClass clss) {
        this.clss = clss;
    }

    //
    // --------------- PUBLIC METHODS --------------------
    //
    public void start(Reduce reduce) {
        groupSize = PASPMD.getMySPMDGroupSize();
        rank = PASPMD.getMyRank();
        workers = (WorkerFT) PASPMD.getSPMDGroup();
        me = (WorkerFT) PAActiveObject.getStubOnThis();
        body = PAActiveObject.getBodyOnThis();
        isLeader = (rank == 0);
        this.reduce = reduce;
        rng = new RandomFT();

        // TimIt variables initialization
        E_count = new CommEventObserver("FT Communication pattern " + clss.PROBLEM_CLASS_NAME + " " +
            groupSize, groupSize, rank);
        E_size = new CommEventObserver("FT Communication pattern " + clss.PROBLEM_CLASS_NAME + " " +
            groupSize, groupSize, rank);

        E_mflops = new DefaultEventObserver("mflops", DefaultEventData.MIN, DefaultEventData.MIN);
        EventObserver[] eventObservers = new EventObserver[] { E_mflops };

        if (COMMUNICATION_PATTERN_OBSERVING_MODE) {
            eventObservers = new EventObserver[] { E_count, E_size, E_mflops };
        }

        super.activate(new TimerCounter[] { T_total, T_setup, T_fft, T_evolve, T_checksum, T_fftlow,
                T_fftcopy, // T_transpose,
                T_transxzloc, T_transxzglo, T_transxzfin, T_transxyloc, T_transxyglo, T_transxyfin },
                eventObservers);

        // ***** warmup *****
        T_total.start();
        setup();
        compute_indexmap(twiddle, dims13);
        compute_initial_conditions(u1, dims11);
        fft_init(clss.dims[1][1]);
        fft(1, u1, u0);
        T_total.stop();
        super.resetTimer();

        // ***** real start *****
        PASPMD.totalBarrier("start");
        blockingServe();
        T_total.start();
        T_setup.start();
        compute_indexmap(twiddle, dims13);
        compute_initial_conditions(u1, dims11);

        fft_init(clss.dims[1][1]);
        T_setup.stop();

        T_fft.start();
        fft(1, u1, u0);
        T_fft.stop();

        me.iterate(1);
    }

    public void iterate(int iteration) {
        if (iteration <= clss.niter) {
            T_evolve.start();
            evolve(u0, u1, twiddle, dims11);
            T_evolve.stop();

            T_fft.start();
            fft(-1, u1, u2);
            T_fft.stop();

            T_checksum.start();
            checksum(u2, dims11);
            T_checksum.stop();

            me.iterate(iteration + 1);

        } else {
            // ***** verification *****
            boolean verified = verify();
            T_total.stop();

            super.getEventObservable().notifyObservers(new Event(E_mflops, getMflops()));

            // ***** THE END *****
            if (isLeader) {
                super.finalizeTimed(rank, verified ? "" : "UNSUCCESSFUL");
            } else {
                super.finalizeTimed(rank, "");
            }
        }
    }

    public void terminate() {
        PAActiveObject.getBodyOnThis().terminate();
    }

    private void setup() {
        dims11 = new int[3 + 1];
        dims12 = new int[3 + 1];
        dims13 = new int[3 + 1];
        dims1 = new int[3 + 1][];
        dims1[1] = dims11;
        dims1[2] = dims12;
        dims1[3] = dims13;

        cplxSums = new double[clss.niter + 1][2];

        z = new Complex[transblockpad + 1][transblock + 1];
        for (int i = 0; i <= transblockpad; i++)
            for (int j = 0; j <= transblock; j++)
                z[i][j] = new Complex();

        // allow to emulate fortran to pass by reference any section of an array
        // and take heed of the different memory allocation
        for (int j = 1; j <= 3; j++) {
            dims11[j] = clss.dims[j][1]; // column 1
            dims12[j] = clss.dims[j][2]; // column 2
            dims13[j] = clss.dims[j][3]; // column 3
        }

        if (isLeader) {
            /* Printout initial NPB info */
            KernelFT.printStarted(clss.KERNEL_NAME, clss.PROBLEM_CLASS_NAME, new long[] { clss.nx, clss.ny,
                    clss.nz }, clss.niter, this.groupSize, clss.np1, clss.np2, clss.layout_type);
        }

        // Determine processor coordinates of this processor
        // Processor grid is np1xnp2.
        // Arrays are always (n1, n2/np1, n3/np2)
        // Processor coords are zero-based.
        // emulation of comm_split with plan topology
        int me2 = rank % clss.np2; // goes from 0...np2-1
        int me1 = rank / clss.np2; // goes from 0...np1-1

        // Communicators for rows/columns of processor grid.
        // commslice1 is communicator of all procs with same me1, ranked as me2
        // commslice2 is communicator of all procs with same me2, ranked as me1
        Group<WorkerFT> g = PAGroup.getGroup(workers);
        Plan<WorkerFT> pl = null;

        try {
            commslice1 = (WorkerFT) PAGroup.newGroup(WorkerFT.class.getName());
            commslice2 = (WorkerFT) PAGroup.newGroup(WorkerFT.class.getName());

            pl = new Plan<WorkerFT>(g, clss.np1, clss.np2);

            List<WorkerFT> c1 = pl.line(me1);
            List<WorkerFT> c2 = pl.column(me2);
            if (c1 != null)
                PAGroup.getGroup(commslice1).addAll(c1);
            if (c2 != null)
                PAGroup.getGroup(commslice2).addAll(c2);

        } catch (ClassNotReifiableException e1) {
            e1.printStackTrace();
            terminate();
            return;

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            terminate();
            return;

        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
            terminate();
            return;
        }

        commslice1Size = PAGroup.getGroup(commslice1).size();
        commslice2Size = PAGroup.getGroup(commslice2).size();

        // size + 1 for translation from FORTRAN
        xstart = new int[3 + 1];
        xend = new int[3 + 1];
        ystart = new int[3 + 1];
        yend = new int[3 + 1];
        zstart = new int[3 + 1];
        zend = new int[3 + 1];

        twiddle = new double[clss.ntdivnp];
        doubleArraySetDimension(clss.ntdivnp, 1, 1);

        u = new double[2][clss.nx + 1];

        u0 = new ComplexArrayGroup(rank, clss.np, clss.ntdivnp / clss.np);
        u1 = new ComplexArrayGroup(rank, clss.np, clss.ntdivnp / clss.np);
        u2 = new ComplexArrayGroup(rank, clss.np, clss.ntdivnp / clss.np);

        // Determine which section of the grid is owned by this processor
        if (clss.layout_type == FTClasses.LAYOUT_0D) {
            for (int i = 1; i <= 3; i++) {
                xstart[i] = 1;
                xend[i] = clss.nx;
                ystart[i] = 1;
                yend[i] = clss.ny;
                zstart[i] = 1;
                zend[i] = clss.nz;
            }
        } else if (clss.layout_type == FTClasses.LAYOUT_1D) {
            xstart[1] = 1;
            xend[1] = clss.nx;
            ystart[1] = 1;
            yend[1] = clss.ny;
            zstart[1] = 1 + ((me2 * clss.nz) / clss.np2);
            zend[1] = ((me2 + 1) * clss.nz) / clss.np2;

            xstart[2] = 1;
            xend[2] = clss.nx;
            ystart[2] = 1;
            yend[2] = clss.ny;
            zstart[2] = 1 + ((me2 * clss.nz) / clss.np2);
            zend[2] = ((me2 + 1) * clss.nz) / clss.np2;

            xstart[3] = 1;
            xend[3] = clss.nx;
            ystart[3] = 1 + ((me2 * clss.ny) / clss.np2);
            yend[3] = ((me2 + 1) * clss.ny) / clss.np2;
            zstart[3] = 1;
            zend[3] = clss.nz;
        } else if (clss.layout_type == FTClasses.LAYOUT_2D) {
            xstart[1] = 1;
            xend[1] = clss.nx;
            ystart[1] = 1 + ((me1 * clss.ny) / clss.np1);
            yend[1] = ((me1 + 1) * clss.ny) / clss.np1;
            zstart[1] = 1 + ((me2 * clss.nz) / clss.np2);
            zend[1] = ((me2 + 1) * clss.nz) / clss.np2;

            xstart[2] = 1 + ((me1 * clss.nx) / clss.np1);
            xend[2] = ((me1 + 1) * clss.nx) / clss.np1;
            ystart[2] = 1;
            yend[2] = clss.ny;
            zstart[2] = zstart[1];
            zend[2] = zend[1];

            xstart[3] = xstart[2];
            xend[3] = xend[2];
            ystart[3] = 1 + ((me2 * clss.ny) / clss.np2);
            yend[3] = ((me2 + 1) * clss.ny) / clss.np2;
            zstart[3] = 1;
            zend[3] = clss.nz;
        }

        // Set up info for blocking of ffts and transposes. This improves
        // performance on cache-based systems. Blocking involves
        // working on a chunk of the problem at a time, taking chunks
        // along the first, second, or third dimension.
        //
        // - In cffts1 blocking is on 2nd dimension (with fft on 1st dim)
        // - In cffts2/3 blocking is on 1st dimension (with fft on 2nd and 3rd dims)
        // Since 1st dim is always in processor, we'll assume it's long enough
        // (default blocking factor is 16 so min size for 1st dim is 16)
        // The only case we have to worry about is cffts1 in a 2d decomposition.
        // so the blocking factor should not be larger than the 2nd dimension.
        fftblock = fftblock_default;
        fftblockpad = fftblockpad_default;

        if (clss.layout_type == FTClasses.LAYOUT_2D) {
            if (clss.dims[2][1] < fftblock) {
                fftblock = clss.dims[2][1];
            }
            if (clss.dims[2][2] < fftblock) {
                fftblock = clss.dims[2][2];
            }
            if (clss.dims[2][3] < fftblock) {
                fftblock = clss.dims[2][3];
            }
        }

        if (fftblock != fftblock_default) {
            fftblockpad = fftblock + 3;
        }
    } // setup()

    private void doubleArraySetDimension(int a, int b, int c) {
        d0 = a;
        d01 = a * b;
    }

    private void compute_indexmap(double[] twiddle, int[] d) {
        // compute function from local (i,j,k) to ibar^2+jbar^2+kbar^2 for time evolution exponent.
        int i, j, k, ii, ii2, jj, ij2, kk;
        double ap;

        doubleArraySetDimension(d[1], d[2], d[3]);

        // this function is very different depending on whether
        // we are in the 0d, 1d or 2d layout. Compute separately.
        // basiy we want to convert the fortran indices
        // 1 2 3 4 5 6 7 8
        // to
        // 0 1 2 3 -4 -3 -2 -1
        // The following magic formula does the trick:
        // mod(i-1+n/2, n) - n/2
        ap = -4.d * alpha * pi * pi;
        int nx2 = clss.nx / 2;
        int ny2 = clss.ny / 2;
        int nz2 = clss.nz / 2;
        if (clss.layout_type == FTClasses.LAYOUT_0D) { // xyz layout
            int ilen = clss.dims[1][3];
            int jlen = clss.dims[2][3];
            int klen = clss.dims[3][3];

            for (i = 0; i < ilen; i++) {
                ii = (((i + xstart[3]) - 1 + nx2) % clss.nx) - nx2;
                ii2 = ii * ii;
                for (j = 0; j < jlen; j++) {
                    jj = (((j + ystart[3]) - 1 + ny2) % clss.ny) - ny2;
                    ij2 = (jj * jj) + ii2;
                    for (k = 0; k < klen; k++) {
                        kk = (((k + zstart[3]) - 1 + nz2) % clss.nz) - nz2;
                        twiddle[resolve(i, j, k)] = Math.exp(ap * ((kk * kk) + ij2));
                    }
                }
            }
        } else if (clss.layout_type == FTClasses.LAYOUT_1D) { // zxy layout
            int ilen = clss.dims[2][3];
            int jlen = clss.dims[3][3];
            int klen = clss.dims[1][3];

            for (i = 0; i < ilen; i++) {
                ii = (((i + xstart[3]) - 1 + nx2) % clss.nx) - nx2;
                ii2 = ii * ii;
                for (j = 0; j < jlen; j++) {
                    jj = (((j + ystart[3]) - 1 + ny2) % clss.ny) - ny2;
                    ij2 = (jj * jj) + ii2;
                    for (k = 0; k < klen; k++) {
                        kk = (((k + zstart[3]) - 1 + nz2) % clss.nz) - nz2;
                        twiddle[resolve(k, i, j)] = Math.exp(ap * ((kk * kk) + ij2));
                    }
                }
            }
        } else if (clss.layout_type == FTClasses.LAYOUT_2D) { // zxy layout
            int ilen = clss.dims[2][3];
            int jlen = clss.dims[3][3];
            int klen = clss.dims[1][3];

            for (i = 0; i < ilen; i++) {
                ii = (((i + xstart[3]) - 1 + nx2) % clss.nx) - nx2;
                ii2 = ii * ii;
                for (j = 0; j < jlen; j++) {
                    jj = (((j + ystart[3]) - 1 + ny2) % clss.ny) - ny2;
                    ij2 = (jj * jj) + ii2;
                    for (k = 0; k < klen; k++) {
                        kk = ((k + zstart[3]) - 1 + (nz2 % clss.nz)) - nz2;
                        twiddle[resolve(k, i, j)] = Math.exp(ap * ((kk * kk) + ij2));
                    }
                }
            }
        } else {
            System.out.println(" Unknown layout type " + clss.layout_type);
            terminate();
            return;
        }
    } // compute_indexmap()

    // public final int resolve(int a, int b) {
    // return a + b * d0;
    // }

    private final int resolve(int a, int b, int c) {
        return a + b * d0 + c * d01;
    }

    //
    // --------------- PRIVATE METHODS --------------------
    //
    private void compute_initial_conditions(ComplexArrayGroup u0, int[] d) {
        // Fill in array u0 with initial conditions from
        // random number generator
        int k;
        double x0;
        double start;
        double an;
        u0.setDimension(d[1], d[2], d[3], 0);

        // 0-D and 1-D layouts are easy because each processor gets a contiguous
        // chunk of the array, in the Fortran ordering sense.
        // For a 2-D layout, it's a bit more complicated. We always
        // have entire x-lines (contiguous) in processor.
        // We can do ny/np1 of them at a time since we have
        // ny/np1 contiguous in y-direction. But { we jump
        // by z-planes (nz/np2 of them, total).
        // For the 0-D and 1-D layouts we could do larger chunks, but
        // this turns out to have no measurable impact on performance.
        start = seed;
        // Jump to the starting element for our first plane.
        an = ipow46(a, 2 * clss.nx, ((zstart[1] - 1) * clss.ny) + (ystart[1] - 1));
        start = rng.randlc(start, an);
        an = ipow46(a, 2 * clss.nx, clss.ny);

        // --------------------------------------------------------------------
        // Go through by z planes filling in one square at a time.
        // --------------------------------------------------------------------
        for (k = 0; k < clss.dims[3][1]; k++) {
            x0 = start;
            x0 = vranlc(2 * clss.nx * clss.dims[2][1], x0, a, u0, u0.resolve(new int[] { 0, 0, k }));

            if (k != clss.dims[3][1]) {
                start = rng.randlc(start, an);
            }
        }
    }

    private void fft_init(int n) {
        // compute the roots-of-unity array that will be used for subsequent FFTs.
        int ku;
        int ln;
        int m;
        double t;
        double ti;

        // Initialize the U array with sines and cosines in a manner that permits
        // stride one access at each FFT iteration.
        m = ilog2(n);
        u[0][0] = m;
        ku = 2;
        ln = 1;

        for (int j = 0; j < m; j++) {
            t = pi / ln;

            for (int i = 0; i <= ln - 1; i++) {
                ti = i * t;
                u[0][i + ku] = Math.cos(ti);
                u[1][i + ku] = Math.sin(ti);
            }

            ku += ln;
            ln *= 2;
        }
    }

    private void evolve(ComplexArrayGroup u0, ComplexArrayGroup u1, double[] twiddle, int[] d) {
        // evolve u0 -> u1 (t time steps) in fourier space
        u0.setDimension(d[1], d[2], d[3], 0);
        u1.setDimension(d[1], d[2], d[3], 0);
        doubleArraySetDimension(d[1], d[2], d[3]);

        u0.evolve(u1, d[1], d[2], d[3], twiddle);
    }

    private void fft(int dir, ComplexArrayGroup x1, ComplexArrayGroup x2) {
        if (scratch == null) {
            scratch = new ComplexArrayGroup(rank, 1, fftblockpad_default * clss.maxdim * 2);
        }
        // note: args x1, x2 must be different arrays
        // note: args for cfftsx are (direction, layout, xin, xout, scratch)
        // xin/xout may be the same and it can be somewhat faster
        // if they are
        // note: args for transpose are (layout1, layout2, xin, xout)
        // xin/xout must be different
        if (dir == 1) {
            if (clss.layout_type == FTClasses.LAYOUT_0D) {
                cffts1(1, dims11, x1, x1, scratch);
                cffts2(1, dims12, x1, x1, scratch);
                cffts3(1, dims13, x1, x2, scratch);

            } else if (clss.layout_type == FTClasses.LAYOUT_1D) {
                cffts1(1, dims11, x1, x1, scratch);
                cffts2(1, dims12, x1, x1, scratch);
                T_transpose.start();
                transpose_xy_z(2, 3, x1, x2);
                T_transpose.stop();
                cffts1(1, dims13, x2, x2, scratch);

            } else if (clss.layout_type == FTClasses.LAYOUT_2D) {
                cffts1(1, dims11, x1, x1, scratch);
                T_transpose.start();
                transpose_x_y(1, 2, x1, x2);
                T_transpose.stop();
                cffts1(1, dims12, x2, x2, scratch);
                T_transpose.start();
                transpose_x_z(2, 3, x2, x1);
                T_transpose.stop();
                cffts1(1, dims13, x1, x2, scratch);
            }

        } else if (clss.layout_type == FTClasses.LAYOUT_0D) {
            cffts3(-1, dims13, x1, x1, scratch);
            cffts2(-1, dims12, x1, x1, scratch);
            cffts1(-1, dims11, x1, x2, scratch);

        } else if (clss.layout_type == FTClasses.LAYOUT_1D) {
            cffts1(-1, dims13, x1, x1, scratch);
            T_transpose.start();
            transpose_x_yz(3, 2, x1, x2);
            T_transpose.stop();
            cffts2(-1, dims12, x2, x2, scratch);
            cffts1(-1, dims11, x2, x2, scratch);

        } else if (clss.layout_type == FTClasses.LAYOUT_2D) {
            cffts1(-1, dims13, x1, x1, scratch);
            T_transpose.start();
            transpose_x_z(3, 2, x1, x2);
            T_transpose.stop();
            cffts1(-1, dims12, x2, x2, scratch);
            T_transpose.start();
            transpose_x_y(2, 1, x2, x1);
            T_transpose.stop();
            cffts1(-1, dims11, x1, x2, scratch);
        }
    } // fft()

    private void cffts1(int is, int[] d, ComplexArrayGroup x, ComplexArrayGroup xout, ComplexArrayGroup y) {
        int logd1 = ilog2(d[1]);
        int d2mfftblock = d[2] - fftblock;

        x.setDimension(d[1], d[2], d[3], 0);
        xout.setDimension(d[1], d[2], d[3], 0);
        y.setDimension(fftblockpad, d[1], 2, 0);

        Shift y112 = new Shift(y);
        Shift y0 = new Shift(y);
        y112.setShift(0, 0, 1);

        for (int k = 0; k < d[3]; k++) {
            for (int jj = 0; jj <= d2mfftblock; jj += fftblock) {
                T_fftcopy.start();
                y.arraycopy11(x, fftblock, d[1], jj, k);
                T_fftcopy.stop();

                T_fftlow.start();
                cfftz(is, logd1, d[1], y0, y112);
                y.setDimension(fftblockpad, d[1], 2, 0);
                T_fftlow.stop();

                T_fftcopy.start();
                xout.arraycopy12(y, fftblock, d[1], jj, k);
                T_fftcopy.stop();
            }
        }
    }

    private void cffts2(int is, int[] d, ComplexArrayGroup x, ComplexArrayGroup xout, ComplexArrayGroup y) {
        int logd2 = ilog2(d[2]);

        x.setDimension(d[1], d[2], d[3], 0);
        xout.setDimension(d[1], d[2], d[3], 0);
        y.setDimension(fftblockpad, d[2], 2, 0);

        Shift y112 = new Shift(y);
        Shift y0 = new Shift(y);
        y112.setShift(0, 0, 1);

        for (int k = 0; k < d[3]; k++) {
            for (int ii = 0; ii <= (d[1] - fftblock); ii += fftblock) {
                T_fftcopy.start();
                y.arraycopy21(x, d[2], fftblock, ii, k);
                T_fftcopy.stop();

                T_fftlow.start();
                cfftz(is, logd2, d[2], y0, y112);
                y.setDimension(fftblockpad, d[2], 2, 0);
                T_fftlow.stop();

                T_fftcopy.start();
                xout.arraycopy22(y, d[2], fftblock, ii, k);
                T_fftcopy.stop();
            }
        }
    }

    private void cffts3(int is, int[] d, ComplexArrayGroup x, ComplexArrayGroup xout, ComplexArrayGroup y) {
        int logd3 = ilog2(d[3]);

        x.setDimension(d[1], d[2], d[3], 0);
        xout.setDimension(d[1], d[2], d[3], 0);
        y.setDimension(fftblockpad, d[3], 2, 0);

        Shift y112 = new Shift(y);
        Shift y0 = new Shift(y);
        y112.setShift(0, 0, 1);

        for (int j = 0; j < d[2]; j++) {
            for (int ii = 0; ii <= (d[1] - fftblock); ii += fftblock) {
                T_fftcopy.start();
                y.arraycopy31(x, d[3], fftblock, ii, j);
                T_fftcopy.stop();

                T_fftlow.start();
                cfftz(is, logd3, d[3], y0, y112);
                y.setDimension(fftblockpad, d[3], 2, 0);
                T_fftlow.stop();

                T_fftcopy.start();
                xout.arraycopy32(y, d[3], fftblock, ii, j);
                T_fftcopy.stop();
            }
        }
    }

    private void cfftz(int is, int m, int n, Shift x, Shift y) {
        // Computes NY N-point complex-to-complex FFTs of X using an algorithm due
        // to Swarztrauber. X is both the input and the output array, while Y is a
        // scratch array. It is assumed that N = 2^M. Before calling CFFTZ to
        // perform FFTs, the array U must be initialized by calling CFFTZ with IS
        // set to 0 and M set to MX, where MX is the maximum value of M for any
        // subsequent call.
        int mx;
        x.setDimension(fftblockpad, n, 0, 0);
        y.setDimension(fftblockpad, n, 0, 0);

        // Check if input parameters are invalid.
        mx = (int) u[0][0];
        if (((is != 1) && (is != -1)) || (m < 1) || (m > mx)) {
            System.out.println("CFFTZ: Either U has not been initialized, or else " +
                "one of the input parameters is invalid" + is + " " + m + " " + mx);
            terminate();
        }

        // Perform one variant of the Stockham FFT.
        for (int l = 1; l <= m; l += 2) {
            fftz2(is, l, m, n, fftblock, fftblockpad, u, x, y);

            if (l == m) {
                // Copy Y to X.
                for (int j = 0; j < n; j++)
                    for (int i = 0; i < fftblock; i++)
                        x.set(i, j, y.getReal(i, j), y.getImg(i, j));
                return;
            }
            fftz2(is, l + 1, m, n, fftblock, fftblockpad, u, y, x);
        }
    }

    private void fftz2(int is, int l, int m, int n, int ny, int ny1, double[][] u, Shift x, Shift y) {
        // Performs the L-th iteration of the second variant of the Stockham FFT.
        x.setDimension(ny1, n, 0, 0);
        y.setDimension(ny1, n, 0, 0);

        int n1;
        int lk;
        int li;
        int lj;
        int ku;
        int i11, i12, i21, i22;

        // Set initial parameters.
        n1 = n / 2;
        lk = (1 << (l - 1));
        li = (1 << (m - l));
        lj = 2 * lk;
        ku = li + 1;

        for (int i = 0; i < li; i++) {
            i11 = (i * lk);
            i12 = i11 + n1;
            i21 = (i * lj);
            i22 = i21 + lk;

            // This loop is vectorizable.
            y.stockham(x, u[0][ku + i], (is < 1) ? -u[1][ku + i] : u[1][ku + i], lk, ny, i11, i12, i21, i22);
        }
    }

    private void transpose_x_yz(int l1, int l2, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        transpose2_local(clss.dims[1][l1], clss.dims[2][l1] * clss.dims[3][l1], xin, xout);
        transpose2_global(xout, xin);
        transpose2_finish(clss.dims[1][l1], clss.dims[2][l1] * clss.dims[3][l1], xin, xout);
    }

    private void transpose_xy_z(int l1, int l2, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        transpose2_local(clss.dims[1][l1] * clss.dims[2][l1], clss.dims[3][l1], xin, xout);
        transpose2_global(xout, xin);
        transpose2_finish(clss.dims[1][l1] * clss.dims[2][l1], clss.dims[3][l1], xin, xout);
    }

    private void transpose2_local(int n1, int n2, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        xin.setDimension(n1, n2, 0, 0);
        xout.setDimension(n2, n1, 0, 0);

        T_transxzloc.start();

        // If possible, block the transpose for cache memory systems.
        // How much does this help? Example: R8000 Power Challenge (90 MHz)
        // Blocked version decreases time spend in this routine
        // from 14 seconds to 5.2 seconds on 8 nodes class A.
        if ((n1 < transblock) || (n2 < transblock)) {
            if (n1 >= n2) {
                xout.transpose(xin, n2, n1);
            } else {
                xout.transpose(xin, n1, n2);
            }
        } else {
            for (int j = 0; j < n2; j += transblock) {
                for (int i = 0; i < n1; i += transblock) {
                    xout.transposeBlock(xin, transblock, i, j);
                }
            }
        }
        T_transxzloc.stop();
    }

    private void transpose2_global(ComplexArrayGroup xin, ComplexArrayGroup xout) {
        if (!reduceStatus) {
            T_transxzglo.start();

            if (COMMUNICATION_PATTERN_OBSERVING_MODE) {
                for (int i = 0; i < commslice1Size; i++) {
                    super.getEventObservable().notifyObservers(new CommEvent(E_count, i, 1));
                    super.getEventObservable().notifyObservers(
                            new CommEvent(E_size, i, xin.getDataSize() * 16));
                }
            }

            commslice1.transpose2_receive(xin.getTypedGroup());
            reduceStatus = true;
        }
        while (nbReceiveDataField < commslice1Size) {
            cplxArray = null;
            blockingServe();
            if (cplxArray != null) {
                xout.setComplexArray(cplxArray.getRank(), cplxArray);
            }
        }
        reduceStatus = false;
        nbReceiveDataField = 0;

        T_transxzglo.stop();
    }

    private void transpose2_finish(int n1, int n2, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        xin.setDimension(n2, n1 / clss.np2, clss.np2, 0);
        xout.setDimension(n2 * clss.np2, n1 / clss.np2, 0, 0);
        int ioff;

        T_transxzfin.start();
        for (int p = 0; p <= (clss.np2 - 1); p++) {
            ioff = p * n2;
            for (int j = 0; j < (n1 / clss.np2); j++)
                for (int i = 0; i < n2; i++) {
                    xout.set(i + ioff, j, xin.getReal(i, j, p), xin.getImg(i, j, p)); // p + 1 due
                    // to the
                    // wrapper
                    // implementation
                }
        }
        T_transxzfin.stop();
    }

    public void transpose2_receive(ComplexArray data) {
        cplxArray = data;
        nbReceiveDataField++;
    }

    private void transpose_x_z(int l1, int l2, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        transpose_x_z_local(dims1[l1], xin, xout);
        transpose_x_z_global(dims1[l1], xout, xin);
        transpose_x_z_finish(dims1[l2], xin, xout);
    }

    private void transpose_x_z_local(int[] d, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        xin.setDimension(d[1], d[2], d[3], 0);
        xout.setDimension(d[1], d[2], d[3], 0);
        int block1;
        int block3;

        T_transxzloc.start();
        if (d[1] < 32) {
            // basic transpose
            for (int j = 0; j < d[2]; j++)
                for (int k = 0; k < d[3]; k++)
                    for (int i = 0; i < d[1]; i++)
                        xout.set(k, j, i, xin.getReal(i, j, k), xin.getImg(i, j, k));

            // all done
            T_transxzloc.stop();
            return;
        }
        block3 = d[3];
        if (block3 == 1) {
            // basic transpose
            for (int j = 0; j < d[2]; j++)
                for (int k = 0; k < d[3]; k++)
                    for (int i = 0; i < d[1]; i++)
                        xout.set(k, j, i, xin.getReal(i, j, k), xin.getImg(i, j, k));

            // all done
            T_transxzloc.stop();
            return;
        }
        if (block3 > transblock) {
            block3 = transblock;
        }
        block1 = d[1];
        if ((block1 * block3) > (transblock * transblock)) {
            block1 = (transblock * transblock) / block3;
        }

        // blocked transpose
        for (int j = 0; j < d[2]; j++) {
            for (int kk = 0; kk <= (d[3] - block3); kk += block3) {
                for (int ii = 1; ii <= (d[1] - block1); ii += block1) {
                    xout.transposeBlock(xin, block1, block3, ii, j, kk);
                }
            }
        }
        T_transxzloc.stop();
    }

    private void transpose_x_z_global(int[] d, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        if (!reduceStatus) {
            T_transxzglo.start();

            if (COMMUNICATION_PATTERN_OBSERVING_MODE) {
                for (int i = 0; i < commslice1Size; i++) {
                    super.getEventObservable().notifyObservers(new CommEvent(E_count, i, 1));
                    super.getEventObservable().notifyObservers(
                            new CommEvent(E_size, i, xin.getDataSize() * 16));
                }
            }
            commslice1.transpose2_receive(xin.getTypedGroup());
            reduceStatus = true;
        }
        while (nbReceiveDataField < commslice1Size) {
            cplxArray = null;
            blockingServe();
            if (cplxArray != null) {
                xout.setComplexArray(cplxArray.getRank(), cplxArray);
            }
        }
        reduceStatus = false;
        nbReceiveDataField = 0;

        T_transxzglo.stop();
    }

    private void transpose_x_z_finish(int[] d, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        xin.setDimension(d[1] / clss.np2, d[2], d[3], clss.np2);
        xout.setDimension(d[1], d[2], d[3], 0);
        int ioff;

        T_transxzfin.start();
        for (int p = 0; p <= (clss.np2 - 1); p++) {
            ioff = (p * d[1]) / clss.np2;
            for (int k = 0; k < d[3]; k++)
                for (int j = 0; j < d[2]; j++)
                    for (int i = 0; i < (d[1] / clss.np2); i++)
                        xout.set(i + ioff, j, k, xin.getReal(i, j, k, p + 1), xin.getImg(i, j, k, p + 1));
        }
        T_transxzfin.stop();
    }

    private void transpose_x_y(int l1, int l2, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        // xy transpose is a little tricky, since we don't want
        // to touch 3rd axis. But alltoall must involve 3rd axis (most
        // slowly varying) to be efficient. So we do
        // (nx, ny/np1, nz/np2) -> (ny/np1, nz/np2, nx) (local)
        // (ny/np1, nz/np2, nx) -> ((ny/np1*nz/np2)*np1, nx/np1) (global)
        // { local finish.
        transpose_x_y_local(dims1[l1], xin, xout);
        transpose_x_y_global(dims1[l1], xout, xin);
        transpose_x_y_finish(dims1[l1], xin, xout);
    }

    private void transpose_x_y_local(int[] d, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        xin.setDimension(d[1], d[2], d[3], 0);
        xout.setDimension(d[2], d[3], d[1], 0);
        T_transxyloc.start();
        for (int k = 0; k < d[3]; k++)
            for (int i = 0; i < d[1]; i++)
                for (int j = 0; j < d[2]; j++)
                    xout.set(j, k, i, xin.getReal(i, j, k), xin.getImg(i, j, k));
        T_transxyloc.stop();
    }

    private void transpose_x_y_global(int[] d, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        if (!reduceStatus) {
            T_transxzglo.start();

            if (COMMUNICATION_PATTERN_OBSERVING_MODE) {
                for (int i = 0; i < commslice2Size; i++) {
                    super.getEventObservable().notifyObservers(new CommEvent(E_count, i, 1));
                    super.getEventObservable().notifyObservers(
                            new CommEvent(E_size, i, xin.getDataSize() * 16));
                }
            }
            commslice2.transpose2_receive(xin.getTypedGroup());
            reduceStatus = true;
        }
        while (nbReceiveDataField < commslice1Size) {
            cplxArray = null;
            blockingServe();
            if (cplxArray != null) {
                xout.setComplexArray(cplxArray.getRank(), cplxArray);
            }
        }
        reduceStatus = false;
        nbReceiveDataField = 0;

        T_transxzglo.stop();
    }

    private void transpose_x_y_finish(int[] d, ComplexArrayGroup xin, ComplexArrayGroup xout) {
        xin.setDimension(d[1] / clss.np1, d[3], d[2], clss.np1);
        xout.setDimension(d[1], d[2], d[3], 0);
        T_transxyfin.start();
        for (int p = 0; p <= (clss.np1 - 1); p++) {
            int ioff = (p * d[1]) / clss.np1;
            for (int k = 0; k < d[3]; k++)
                for (int j = 0; j < d[2]; j++)
                    for (int i = 0; i < (d[1] / clss.np1); i++)
                        xout.set(i + ioff, j, k, xin.getReal(i, k, j, p), xin.getImg(i, k, j, p));
        }
        T_transxyfin.stop();
    }

    private void checksum(ComplexArrayGroup u1, int[] d) {
        u1.setDimension(d[1], d[2], d[3], 0);

        double[] chksum = u1.checksum(clss.nx, clss.ny, clss.nz, xstart[1], xend[1], ystart[1], yend[1],
                zstart[1], zend[1]);
        Complex chk = new Complex(chksum[0], chksum[1]);

        chk.divMe(clss.ntotal_f);

        allchk = reduce.sumC(chk);
        if (COMMUNICATION_PATTERN_OBSERVING_MODE) {
            super.getEventObservable().notifyObservers(new CommEvent(E_count, 1, 1));
            super.getEventObservable().notifyObservers(new CommEvent(E_size, 1, 16));
            if (rank == 1) {
                for (int i = 0; i < groupSize; i++) {
                    super.getEventObservable().notifyObservers(new CommEvent(E_count, i, 1));
                    super.getEventObservable().notifyObservers(new CommEvent(E_size, i, 16));
                }
            }
        }

        if (isLeader) {
            System.out.println("T=" + checkSumCount + " Checksum=" + allchk);
        }
        if (checkSumCount > 0) {
            cplxSums[checkSumCount][0] = allchk.getReal();
            cplxSums[checkSumCount][1] = allchk.getImg();
            checkSumCount++;
        }
    }

    private boolean verify() {
        int nt = clss.niter;
        int i;
        double err;
        double epsilon = 0.000000000001; // 1.0E-12

        if (rank != 0)
            return false;

        for (i = 1; i <= nt; i++) {
            err = (cplxSums[i][0] - clss.vdata_real[i]) / clss.vdata_real[i];
            if (Math.abs(err) > epsilon)
                return false;
            err = (cplxSums[i][1] - clss.vdata_imag[i]) / clss.vdata_imag[i];
            if (Math.abs(err) > epsilon)
                return false;
        }
        return true;
    }

    private double getMflops() {
        double ntf = clss.ntotal_f;
        double time = T_total.getTotalTime();
        double mflops = ntf *
            (14.8157 + 7.19641 * Math.log(ntf) + (5.23518 + 7.21113 * Math.log(ntf)) * clss.niter) / time /
            1000.0;
        return mflops;
    }

    private double ipow46(double a, int exp_1, int exp_2) {
        // compute a^exponent mod 2^46
        double r;
        double result;
        double q;
        int n;
        int n2;
        boolean two_pow;

        // Use
        // a^n = a^(n/2)*a^(n/2) if n even else
        // a^n = a*a^(n-1) if n odd
        result = 1;
        if ((exp_2 == 0) || (exp_1 == 0)) {
            return result;
        }
        q = a;
        r = 1;
        n = exp_1;
        two_pow = true;

        while (two_pow) {
            n2 = n / 2;
            if ((n2 * 2) == n) {
                q = rng.randlc(q, q);
                n = n2;
            } else {
                n = n * exp_2;
                two_pow = false;
            }
        }

        while (n > 1) {
            n2 = n / 2;
            if ((n2 * 2) == n) {
                q = rng.randlc(q, q);
                n = n2;
            } else {
                r = rng.randlc(r, q);
                n = n - 1;
            }
        }
        r = rng.randlc(r, q);
        return r;
    }

    private double vranlc(int n, double x, double a, ComplexArrayGroup y, int offset) {
        long Lx = (long) x;
        long La = (long) a;

        for (int i = 0; i < (n / 2); i++) {
            Lx = (Lx * La) & (i246m1);
            y.setReal(i + offset, (double) (d2m46 * Lx));
            Lx = (Lx * La) & (i246m1);
            y.setImg(i + offset, (double) (d2m46 * Lx));
        }
        return (double) Lx;
    }

    private static final int ilog2(int n) {
        int nn, lg;

        if (n == 1)
            return 0;

        lg = 1;
        nn = 2;
        while (nn < n) {
            nn *= 2;
            lg += 1;
        }
        return lg;
    }

    private final void blockingServe() {
        body.serve(body.getRequestQueue().blockingRemoveOldest());
    }
}
