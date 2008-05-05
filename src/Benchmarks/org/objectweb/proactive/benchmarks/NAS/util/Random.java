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
package org.objectweb.proactive.benchmarks.NAS.util;

/**
 * This class can compute a uniform pseudorandom double precision number in
 * range (0, 1) by using the linear congruential generator
 * x_{k+1} = a x_k (mod 2^46)
 * where 0 < x_k < 2^46 and 0 < a < 2^46.
 * This scheme generates 2^44 numbers before repeating.
 *
 */
public class Random implements java.io.Serializable {

    private long T46m1;
    private long lseed, lgmult;
    private double seed, gmult;
    private double R23;
    private double R46;
    private double T23;
    private double T46;

    /* Constructor */
    public Random() {
        /* Initial values of seed an gmult */
        this.seed = 314159265.00; /* First 9 digits of PI */
        this.gmult = 1220703125.00; /* 5^13 */

        /* Compute some big constants
         * T23=2^23   R23=2^-23   T46=2^46   R46=2^-46		*/
        R23 = R46 = T23 = T46 = 1.0;

        for (int i = 1; i <= 23; i++) {
            R23 *= 0.50;
            T23 *= 2.0;
        }
        for (int i = 1; i <= 46; i++) {
            R46 *= 0.50;
            T46 *= 2.0;
        }
        T46m1 = (long) T46 - 1;
    }

    public double getGmult() {
        return gmult;
    }

    public double getSeed() {
        return seed;
    }

    public long getLGmult() {
        return lgmult;
    }

    public long getLSeed() {
        return lseed;
    }

    /**
     * Set the seed of the generator
     * Must be odd double precision integer in the range (1, 2^46)
     * @param seed
     */
    public void setSeed(double seed) {
        this.seed = seed;
    }

    /**
     * Set the gen multiplier of the generator
     * Must be odd double precision integer in the range (1, 2^46)
     * @param gmult
     */
    public void setGmult(double gmult) {
        this.gmult = gmult;
    }

    /**
     * Set the seed of the generator
     * Must be odd double precision integer in the range (1, 2^46)
     * @param seed
     */
    public void setLSeed(long seed) {
        this.lseed = seed;
    }

    /**
     * Set the gen multiplier of the generator
     * Must be odd double precision integer in the range (1, 2^46)
     * @param gmult
     */
    public void setLGmult(long gmult) {
        this.lgmult = gmult;
    }

    public void setLSeedGmult(long seed, long gmult) {
        this.lseed = seed;
        this.lgmult = gmult;
    }

    /**
     * Get pseudorandom number
     * @return a pseudorandom double precision number
     */
    public double randlc() {
        double T1, T2, T3, T4;
        double A1, A2;
        double X1, X2;
        double Z;

        /*  Break A into two parts such that
           A = 2^23 * A1 + A2 and set X = N.			*/
        A1 = (int) (R23 * this.gmult);
        A2 = this.gmult - (T23 * A1);

        /*  Break X into two parts such that
           X = 2^23 * X1 + X2, compute
           Z = A1 * X2 + A2 * X1  (mod 2^23), and then
           X = 2^23 * Z + A2 * X2  (mod 2^46).			*/
        X1 = (int) (R23 * this.seed);
        X2 = this.seed - (T23 * X1);
        T1 = (A1 * X2) + (A2 * X1);

        T2 = (int) (R23 * T1);
        Z = T1 - (T23 * T2);
        T3 = (T23 * Z) + (A2 * X2);
        T4 = (int) (R46 * T3);
        this.seed = T3 - (T46 * T4);

        return (R46 * this.seed);
    }

    public double lrandlc() {
        this.lseed = (this.lseed * this.lgmult) & T46m1;
        return R46 * this.lseed;
    }

    /**
     * 
     * @param n
     * @param x
     * @param a
     * @param y
     * @param offset
     * @return
     */
    public double vranlc(final int n, final double x, final double a, double[] y, final int offset) {
        long Lx = (long) x;
        long La = (long) a;

        for (int i = 0; i < n; i++) {
            Lx = (Lx * La) & (T46m1);
            y[offset + i] = R46 * Lx;
        }

        return (double) Lx;
    }

    public final void vranlc1(final int n, long Lx, final long La, double[] y) {
        for (int i = 1; i <= n; i++) {
            Lx = (Lx * La) & (T46m1);
            y[i] = R46 * Lx;
        }
    }

    public static final double ln(double x) {
        return 6 * (x - 1) / (x + 1 + 4 * (Math.sqrt(x)));
    }

    /**
     * Integer pow method
     * @param x
     * @return
     */
    public static int ipow2(int x) {
        int res = 1; // TODO: Check with <<
        for (int i = 1; i <= x; i++) {
            res *= 2;
        }
        return res;
    }

    /**
     *  This routine returns a uniform pseudorandom double precision number in the
     *  range (0, 1) by using the linear congruential generator
     *
     *  x_{k+1} = a x_k  (mod 2^46)
     *          
     *  where 0 < x_k < 2^46 and 0 < a < 2^46.  This scheme generates 2^44 numbers
     *  before repeating.  The argument A is the same as 'a' in the above formula,
     *  and X is the same as x_0.  A and X must be odd double precision integers
     *  in the range (1, 2^46).  The returned value RANDLC is normalized to be
     *  between 0 and 1, i.e. RANDLC = 2^(-46) * x_1.  X is updated to contain
     *  the new seed x_1, so that subsequent calls to RANDLC using the same
     *  arguments will generate a continuous sequence.
     */
    public double randlc(double a, double b) {
        return 0.0d;
    }

    public double randlc(DoublePtr a, double b) {
        return 0.0d;
    }
}
