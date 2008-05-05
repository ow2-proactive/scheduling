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

public class RandomFT implements java.io.Serializable {

    public double tran = 314159265.0; // First 9 digits of PI
    public double amult = 1220703125.0; // 5.0^13
    public int KS = 0;
    public double R23, R46, T23, T46;
    // constants
    public static final double d2m46 = Math.pow(0.5, 46);
    protected static final long i246m1 = (long) Math.pow(2, 46) - 1;

    public RandomFT() {
    }

    public RandomFT(double sd) {
        seed = sd;
    }

    public double randlc(double x, double a) {
        double r23, r46, t23, t46, t1, t2, t3, t4, a1, a2, x1, x2, z;
        r23 = Math.pow(0.5, 23);
        r46 = Math.pow(r23, 2);
        t23 = Math.pow(2.0, 23);
        t46 = Math.pow(t23, 2);
        // ---------------------------------------------------------------------
        // Break A into two parts such that A = 2^23 * A1 + A2.
        // ---------------------------------------------------------------------
        t1 = r23 * a;
        a1 = (int) t1;
        a2 = a - t23 * a1;
        // ---------------------------------------------------------------------
        // Break X into two parts such that X = 2^23 * X1 + X2, compute
        // Z = A1 * X2 + A2 * X1 (mod 2^23), and then
        // X = 2^23 * Z + A2 * X2 (mod 2^46).
        // ---------------------------------------------------------------------
        t1 = r23 * x;
        x1 = (int) t1;
        x2 = x - t23 * x1;
        t1 = a1 * x2 + a2 * x1;
        t2 = (int) (r23 * t1);
        z = t1 - t23 * t2;
        t3 = t23 * z + a2 * x2;
        t4 = (int) (r46 * t3);
        x = t3 - t46 * t4;
        return x;
    }

    // Random number generator with an internal seed
    public double randlc(double a) {
        double r23, r46, t23, t46, t1, t2, t3, t4, a1, a2, x1, x2, z;
        r23 = Math.pow(0.5, 23);
        r46 = Math.pow(r23, 2);
        t23 = Math.pow(2.0, 23);
        t46 = Math.pow(t23, 2);
        // ---------------------------------------------------------------------
        // Break A into two parts such that A = 2^23 * A1 + A2.
        // ---------------------------------------------------------------------
        t1 = r23 * a;
        a1 = (int) t1;
        a2 = a - t23 * a1;
        // ---------------------------------------------------------------------
        // Break X into two parts such that X = 2^23 * X1 + X2, compute
        // Z = A1 * X2 + A2 * X1 (mod 2^23), and then
        // X = 2^23 * Z + A2 * X2 (mod 2^46).
        // ---------------------------------------------------------------------
        t1 = r23 * tran;
        x1 = (int) t1;
        x2 = tran - t23 * x1;
        t1 = a1 * x2 + a2 * x1;
        t2 = (int) (r23 * t1);
        z = t1 - t23 * t2;
        t3 = t23 * z + a2 * x2;
        t4 = (int) (r46 * t3);
        tran = t3 - t46 * t4;
        return (r46 * tran);
    }

    public double vranlc(int n, double x, double a, double y[], int offset) {
        double r23, r46, t23, t46, t1, t2, t3, t4, a1, a2, x1, x2, z;
        r23 = Math.pow(0.5, 23);
        r46 = Math.pow(r23, 2);
        t23 = Math.pow(2.0, 23);
        t46 = Math.pow(t23, 2);
        // ---------------------------------------------------------------------
        // Break A into two parts such that A = 2^23 * A1 + A2.
        // ---------------------------------------------------------------------
        t1 = r23 * a;
        a1 = (int) t1;
        a2 = a - t23 * a1;

        // ---------------------------------------------------------------------
        // Generates N results. This loop is not vectorizable
        // ---------------------------------------------------------------------
        for (int i = 0; i < n; i++) {

            // -----------------------------------------------------------------
            // Break X into two parts such that X = 2^23 * X1 + X2, compute
            // Z = A1 * X2 + A2 * X1 (mod 2^23), and then
            // X = 2^23 * Z + A2 * X2 (mod 2^46).
            // -----------------------------------------------------------------
            t1 = r23 * x;
            x1 = (int) t1;
            x2 = x - t23 * x1;
            t1 = a1 * x2 + a2 * x1;
            t2 = (int) (r23 * t1);
            z = t1 - t23 * t2;
            t3 = t23 * z + a2 * x2;
            t4 = (int) (r46 * t3);
            x = t3 - t46 * t4;
            y[i + offset] = r46 * x;
        }
        return x;
    }

    public double vranlc_old(double n, double x, double a, double y[], int offset) {
        long Lx = (long) x;
        long La = (long) a;

        for (int i = 0; i < n; i++) {
            Lx = (Lx * La) & (i246m1);
            y[offset + i] = (double) (d2m46 * Lx);
        }
        return (double) Lx;
    }

    public double seed;

    public double ipow46(double a, int exponent) {
        int n, n2;
        double q, r;
        // ---------------------------------------------------------------------
        // Use
        // a^n = a^(n/2)*a^(n/2) if n even else
        // a^n = a*a^(n-1) if n odd
        // ---------------------------------------------------------------------
        if (exponent == 0)
            return seed;
        q = a;
        r = 1;
        n = exponent;

        while (n > 1) {
            n2 = n / 2;
            if (n2 * 2 == n) {
                seed = randlc(q, q);
                q = seed;
                n = n2;
            } else {
                seed = randlc(r, q);
                r = seed;
                n = n - 1;
            }
        }
        seed = randlc(r, q);
        return seed;
    }

    public double power(double a, int n) {
        // ---------------------------------------------------------------------
        // power raises an integer, disguised as a double
        // precision real, to an integer power
        // ---------------------------------------------------------------------
        double aj, ajj, pow;
        int nj;

        pow = 1.0;
        nj = n;
        aj = a;
        while (nj != 0) {
            if (nj % 2 == 1) {
                seed = randlc(pow, aj);
                pow = seed;
            }
            ajj = aj;
            seed = randlc(aj, ajj);
            aj = seed;
            nj = nj / 2;
        }
        return pow;
    }
}
