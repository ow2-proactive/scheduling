package org.objectweb.proactive.benchmarks.NAS.util;

public class NpbMath {
    private static final double PRECISION = 0.00000000000000000000001;
    private static int count;
    private static double term, sum;
    private static final double t23 = Math.pow(2, 23);
    private static final double t46 = t23 * t23;
    private static final double r23 = Math.pow(0.5, 23);
    private static final double r46 = r23 * r23;

    public static double dpowE(double x) {
        count = 1;
        sum = 1.0;
        term = x;
        while (term >= PRECISION) {
            sum += term;
            count++;
            term *= (x / count);
        }
        return sum;
    }

    public static int ilog2(int n) {
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

    public static final long ipow2(long n) {
        return (1 << n);
    }

    public static double randlc(double[] x, double a) {
        double t1, t2, t3, t4, a1, a2, x1, x2, z;

        /*--------------------------------------------------------------------
         *   Break A into two parts such that A = 2^23 * A1 + A2.
         *--------------------------------------------------------------------*/
        t1 = r23 * a;
        a1 = (int) t1;
        a2 = a - t23 * a1;

        /*--------------------------------------------------------------------
         *   Break X into two parts such that X = 2^23 * X1 + X2, compute
         *   Z = A1 * X2 + A2 * X1  (mod 2^23), and then
         *   X = 2^23 * Z + A2 * X2  (mod 2^46).
         *--------------------------------------------------------------------*/
        t1 = r23 * (x[0]);
        x1 = (int) t1;
        x2 = (x[0]) - t23 * x1;
        t1 = a1 * x2 + a2 * x1;
        t2 = (int) (r23 * t1);
        z = t1 - t23 * t2;
        t3 = t23 * z + a2 * x2;
        t4 = (int) (r46 * t3);
        x[0] = t3 - t46 * t4;

        return (r46 * x[0]);
    }

    public static void vranlc(int n, double[] x_seed, double a, double y[], int yoff) {
        int i;
        double x, t1, t2, t3, t4, a1, a2, x1, x2, z;

        /*--------------------------------------------------------------------
         *   Break A into two parts such that A = 2^23 * A1 + A2.
         *--------------------------------------------------------------------*/
        t1 = r23 * a;
        a1 = (int) t1;
        a2 = a - t23 * a1;
        x = x_seed[0];

        /*--------------------------------------------------------------------
         *   Generate N results.   This loop is not vectorizable.
         *--------------------------------------------------------------------*/
        for (i = 0; i < n; i++) {

            /*----------------------------------------------------------------
             *   Break X into two parts such that X = 2^23 * X1 + X2, compute
             *   Z = A1 * X2 + A2 * X1  (mod 2^23), and then
             *   X = 2^23 * Z + A2 * X2  (mod 2^46).
             *----------------------------------------------------------------*/
            t1 = r23 * x;
            x1 = (int) t1;
            x2 = x - t23 * x1;
            t1 = a1 * x2 + a2 * x1;
            t2 = (int) (r23 * t1);
            z = t1 - t23 * t2;
            t3 = t23 * z + a2 * x2;
            t4 = (int) (r46 * t3);
            x = t3 - t46 * t4;
            y[i + yoff] = r46 * x;
        }
        x_seed[0] = x;
    }

    public static double power(double a, int n) {
        int nj;
        double[] power = new double[1];
        double[] aj = new double[1];

        power[0] = 1.0;
        nj = n;
        aj[0] = a;
        while (nj != 0) {
            if (nj % 2 == 1)
                NpbMath.randlc(power, aj[0]);
            NpbMath.randlc(aj, aj[0]);
            nj /= 2;
        }
        return power[0];
    }

}
