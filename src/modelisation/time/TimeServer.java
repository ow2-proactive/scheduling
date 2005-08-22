package modelisation.time;

import Jama.Matrix;


/**
 * @author fhuet
 *
 *
 *
 */
public class TimeServer {
    private static final int MATRIX_SIZE = 27;
    protected double lambda;
    protected double nu;
    protected double delta;
    protected double gamma1;
    protected double gamma2;
    protected double mu;

    public TimeServer() {
    }

    public TimeServer(double lambda, double nu, double delta, double gamma1,
        double gamma2, double mu) {
        this.lambda = lambda;
        this.nu = nu;
        this.delta = delta;
        this.gamma1 = gamma1;
        this.gamma2 = gamma2;
        this.mu = mu;
    }

    public Matrix generateAb() {
        Matrix Ab = new Matrix(MATRIX_SIZE, MATRIX_SIZE);

        //   for (int i = 0; i < MATRIX_SIZE; i++) {
        //      array_type array_element = array[i];
        //
        //   }
        double t1 = lambda + nu;
        double t3 = lambda + delta;
        double t4 = nu + gamma1;
        double t7 = delta + gamma1;
        double t8 = gamma2 + nu;
        double t11 = gamma2 + delta;
        double t12 = nu + mu;
        double t15 = mu + delta;
        Ab.set(0, 0, t1);
        Ab.set(0, 1, -mu);
        Ab.set(1, 1, lambda + nu + mu);
        Ab.set(1, 2, -delta);
        Ab.set(1, 4, -delta);
        Ab.set(1, 13, -delta);
        Ab.set(2, 0, -nu);
        Ab.set(2, 2, t3);
        Ab.set(2, 4, -mu);
        Ab.set(3, 0, -lambda);
        Ab.set(3, 3, t4);
        Ab.set(3, 5, -mu);
        Ab.set(4, 1, -nu);
        Ab.set(4, 4, mu + lambda + delta);
        Ab.set(5, 1, -lambda);
        Ab.set(5, 5, nu + gamma1 + mu);
        Ab.set(5, 6, -delta);
        Ab.set(5, 8, -delta);
        Ab.set(5, 12, -delta);
        Ab.set(5, 25, -gamma2);
        Ab.set(6, 2, -lambda);
        Ab.set(6, 3, -nu);
        Ab.set(6, 6, t7);
        Ab.set(6, 8, -mu);
        Ab.set(7, 3, -gamma1);
        Ab.set(7, 7, t8);
        Ab.set(7, 9, -mu);
        Ab.set(8, 4, -lambda);
        Ab.set(8, 5, -nu);
        Ab.set(8, 8, gamma1 + mu + delta);
        Ab.set(8, 26, -gamma2);
        Ab.set(9, 5, -gamma1);
        Ab.set(9, 9, mu + nu + gamma2);
        Ab.set(9, 10, -delta);
        Ab.set(9, 14, -delta);
        Ab.set(9, 22, -delta);
        Ab.set(10, 6, -gamma1);
        Ab.set(10, 7, -nu);
        Ab.set(10, 10, t11);
        Ab.set(10, 14, -mu);
        Ab.set(11, 7, -gamma2);
        Ab.set(11, 11, t12);
        Ab.set(11, 15, -mu);
        Ab.set(12, 12, t7);
        Ab.set(12, 13, -lambda);
        Ab.set(12, 17, -nu);
        Ab.set(12, 24, -gamma2);
        Ab.set(13, 13, t3);
        Ab.set(13, 18, -nu);
        Ab.set(14, 8, -gamma1);
        Ab.set(14, 9, -nu);
        Ab.set(14, 14, mu + gamma2 + delta);
        Ab.set(15, 9, -gamma2);
        Ab.set(15, 15, nu + mu);
        Ab.set(15, 19, -delta);
        Ab.set(15, 20, -mu);
        Ab.set(16, 10, -gamma2);
        Ab.set(16, 11, -nu);
        Ab.set(16, 16, t15);
        Ab.set(16, 19, -mu);
        Ab.set(17, 17, t4);
        Ab.set(17, 18, -lambda);
        Ab.set(17, 23, -gamma2);
        Ab.set(18, 17, -gamma1);
        Ab.set(18, 18, t1);
        Ab.set(19, 14, -gamma2);
        Ab.set(19, 15, -nu);
        Ab.set(19, 19, mu + delta);
        Ab.set(19, 21, -mu);
        Ab.set(20, 16, -delta);
        Ab.set(20, 20, t12);
        Ab.set(20, 21, -delta);
        Ab.set(21, 20, -nu);
        Ab.set(21, 21, t15);
        Ab.set(22, 12, -gamma1);
        Ab.set(22, 22, delta);
        Ab.set(23, 11, -mu);
        Ab.set(23, 23, t8);
        Ab.set(24, 16, -mu);
        Ab.set(24, 23, -nu);
        Ab.set(24, 24, t11);
        Ab.set(25, 24, -delta);
        Ab.set(25, 25, t8);
        Ab.set(25, 26, -delta);
        Ab.set(26, 0, 1.0);
        Ab.set(26, 1, 1.0);
        Ab.set(26, 2, 1.0);
        Ab.set(26, 3, 1.0);
        Ab.set(26, 4, 1.0);
        Ab.set(26, 5, 1.0);
        Ab.set(26, 6, 1.0);
        Ab.set(26, 7, 1.0);
        Ab.set(26, 8, 1.0);
        Ab.set(26, 9, 1.0);
        Ab.set(26, 10, 1.0);
        Ab.set(26, 11, 1.0);
        Ab.set(26, 12, 1.0);
        Ab.set(26, 13, 1.0);
        Ab.set(26, 14, 1.0);
        Ab.set(26, 15, 1.0);
        Ab.set(26, 16, 1.0);
        Ab.set(26, 17, 1.0);
        Ab.set(26, 18, 1.0);
        Ab.set(26, 19, 1.0);
        Ab.set(26, 20, 1.0);
        Ab.set(26, 21, 1.0);
        Ab.set(26, 22, 1.0);
        Ab.set(26, 23, 1.0);
        Ab.set(26, 24, 1.0);
        Ab.set(26, 25, 1.0);
        Ab.set(26, 26, 1.0);
        return Ab;
    }

    public Matrix generateA() {
        Matrix A = new Matrix(MATRIX_SIZE, MATRIX_SIZE);
        double t1 = lambda + nu;
        double t3 = lambda + delta;
        double t4 = nu + gamma1;
        double t7 = delta + gamma1;
        double t8 = gamma2 + nu;
        double t11 = gamma2 + delta;
        double t12 = nu + mu;
        double t15 = mu + delta;
        A.set(0, 0, t1);
        A.set(0, 1, -mu);
        A.set(1, 1, lambda + nu + mu);
        A.set(1, 2, -delta);
        A.set(1, 4, -delta);
        A.set(1, 13, -delta);
        A.set(2, 0, -nu);
        A.set(2, 2, t3);
        A.set(2, 4, -mu);
        A.set(3, 0, -lambda);
        A.set(3, 3, t4);
        A.set(3, 5, -mu);
        A.set(4, 1, -nu);
        A.set(4, 4, mu + lambda + delta);
        A.set(5, 1, -lambda);
        A.set(5, 5, nu + gamma1 + mu);
        A.set(5, 6, -delta);
        A.set(5, 8, -delta);
        A.set(5, 12, -delta);
        A.set(5, 25, -gamma2);
        A.set(6, 2, -lambda);
        A.set(6, 3, -nu);
        A.set(6, 6, t7);
        A.set(6, 8, -mu);
        A.set(7, 3, -gamma1);
        A.set(7, 7, t8);
        A.set(7, 9, -mu);
        A.set(8, 4, -lambda);
        A.set(8, 5, -nu);
        A.set(8, 8, gamma1 + mu + delta);
        A.set(8, 26, -gamma2);
        A.set(9, 5, -gamma1);
        A.set(9, 9, mu + nu + gamma2);
        A.set(9, 10, -delta);
        A.set(9, 14, -delta);
        A.set(9, 22, -delta);
        A.set(10, 6, -gamma1);
        A.set(10, 7, -nu);
        A.set(10, 10, t11);
        A.set(10, 14, -mu);
        A.set(11, 7, -gamma2);
        A.set(11, 11, t12);
        A.set(11, 15, -mu);
        A.set(12, 12, t7);
        A.set(12, 13, -lambda);
        A.set(12, 17, -nu);
        A.set(12, 24, -gamma2);
        A.set(13, 13, t3);
        A.set(13, 18, -nu);
        A.set(14, 8, -gamma1);
        A.set(14, 9, -nu);
        A.set(14, 14, mu + gamma2 + delta);
        A.set(15, 9, -gamma2);
        A.set(15, 15, nu + mu);
        A.set(15, 19, -delta);
        A.set(15, 20, -mu);
        A.set(16, 10, -gamma2);
        A.set(16, 11, -nu);
        A.set(16, 16, t15);
        A.set(16, 19, -mu);
        A.set(17, 17, t4);
        A.set(17, 18, -lambda);
        A.set(17, 23, -gamma2);
        A.set(18, 17, -gamma1);
        A.set(18, 18, t1);
        A.set(19, 14, -gamma2);
        A.set(19, 15, -nu);
        A.set(19, 19, mu + delta);
        A.set(19, 21, -mu);
        A.set(20, 16, -delta);
        A.set(20, 20, t12);
        A.set(20, 21, -delta);
        A.set(21, 20, -nu);
        A.set(21, 21, t15);
        A.set(22, 12, -gamma1);
        A.set(22, 22, delta);
        A.set(23, 11, -mu);
        A.set(23, 23, t8);
        A.set(24, 16, -mu);
        A.set(24, 23, -nu);
        A.set(24, 24, t11);
        A.set(25, 24, -delta);
        A.set(25, 25, t8);
        A.set(25, 26, -delta);
        A.set(26, 25, -nu);
        A.set(26, 26, t11);
        return A;
    }

    public Matrix generateBB() {
        Matrix BB = new Matrix(6, 6);
        double t1 = -lambda - nu;
        double t3 = -lambda - delta;
        BB.set(0, 0, t1);
        BB.set(0, 2, nu);
        BB.set(1, 0, mu);
        BB.set(1, 1, -lambda - nu - mu);
        BB.set(1, 3, nu);
        BB.set(2, 1, delta);
        BB.set(2, 2, t3);
        BB.set(3, 1, delta);
        BB.set(3, 2, mu);
        BB.set(3, 3, -mu - lambda - delta);
        BB.set(4, 1, delta);
        BB.set(4, 4, t3);
        BB.set(5, 4, nu);
        BB.set(5, 5, t1);
        return BB;
    }

    public double evaluate() {
        Matrix tmp = new Matrix(MATRIX_SIZE, MATRIX_SIZE);
        Matrix A = generateA();
        Matrix Q = tmp.minus(A).transpose();
        Matrix BB = generateBB();

        //        BB.print(5, 5);
        Matrix MQ = minor(Q, 18, 18);

        //        System.out.println("*** MQ ***");
        //        MQ.print(5, 5);
        Matrix vectT = new Matrix(26, 1);

        //        vectT.print(5,5);
        //        System.out.println("*** inverse MQ ***");
        //        MQ.inverse().print(5, 5);
        for (int i = 0; i < 26; i++) {
            vectT.set(i, 0, -1);
        }

        //        System.out.println("*** vectT ***");
        //        vectT.print(5, 5);
        Matrix resultT = MQ.inverse().times(vectT);

        //        System.out.println(" *** ResultT ***");
        //        resultT.print(10, 10);
        //         resultT.print(5,5);
        Matrix tmp2 = new Matrix(6, 1);
        tmp2.set(0, 0, -Q.get(0, 3));
        Matrix RR3 = BB.inverse().times(tmp2);

        //        RR3.print(5, 5);
        tmp2.set(0, 0, 0);
        tmp2.set(1, 0, -Q.get(1, 5));
        Matrix RR5 = BB.inverse().times(tmp2);

        //        RR5.print(5, 5);
        tmp2.set(1, 0, 0);
        tmp2.set(2, 0, -Q.get(2, 6));
        Matrix RR6 = BB.inverse().times(tmp2);

        //        RR6.print(10, 10);
        tmp2.set(2, 0, 0);
        tmp2.set(3, 0, -Q.get(4, 8));
        Matrix RR8 = BB.inverse().times(tmp2);

        //        RR8.print(10, 10);
        double p3_18 = RR3.get(5, 0);
        double p5_18 = RR5.get(5, 0);
        double p6_18 = RR6.get(5, 0);
        double p8_18 = RR8.get(5, 0);
        double p12_18 = (nu / (nu + lambda) * lambda) / (lambda + delta);
        double p17_18 = lambda / (lambda + nu);

        //        System.out.println(p3_18);
        //        System.out.println(p5_18);
        //        System.out.println(p6_18);
        //        System.out.println(p8_18);
        //        System.out.println(p12_18);
        //        System.out.println(p17_18);
        double TempsT3 = resultT.get(3, 0);
        double TempsT5 = resultT.get(5, 0);
        double TempsT6 = resultT.get(6, 0);
        double TempsT8 = resultT.get(8, 0);
        double TempsT12 = resultT.get(12, 0);
        double TempsT17 = resultT.get(17, 0);

        //        System.out.println(TempsT3);
        //        System.out.println(TempsT5);
        //        System.out.println(TempsT6);
        //        System.out.println(TempsT8);
        //        System.out.println(TempsT12);
        //        System.out.println(TempsT17);
        //RR5=evalm(inverse(BB) &* [0,-Q[2,6],0,0,0,0]);
        //RR6=evalm(inverse(BB) &* [0,0,-Q[3,7],0,0,0]);
        //RR8=evalm(inverse(BB) &* [0,0,0,-Q[5,9],0,0]);
        return ((p3_18 * TempsT3) + (p5_18 * TempsT5) + (p6_18 * TempsT6) +
        (p8_18 * TempsT8) + (p12_18 * TempsT12) + (p17_18 * TempsT17)) * 1000;
    }

    /**
     *  returns a minux a row and a column
     */
    public Matrix minor(Matrix a, int row, int column) {
        //        Matrix tmp = new Matrix(a.getRowDimension() - 1,
        //                                a.getColumnDimension() - 1);
        int[] rows = new int[a.getRowDimension() - 1];
        int[] columns = new int[a.getColumnDimension() - 1];
        int j = 0;
        for (int i = 0; i < a.getRowDimension(); i++) {
            if (i != row) {
                rows[j] = i;
                j++;
            }
        }
        j = 0;
        for (int i = 0; i < a.getColumnDimension(); i++) {
            if (i != column) {
                columns[j] = i;
                j++;
            }
        }

        //        for (int k = 0; k < rows.length; k++) {
        //            System.out.println(rows[k] + " ");
        //        }
        //        for (int k = 0; k < columns.length; k++) {
        //            System.out.println(columns[k] + " ");
        //        }
        return a.getMatrix(rows, columns);
        //
        //        tmp.print(5, 5);
        //        return tmp;
    }

    /**
     * Returns the delta.
     * @return double
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Returns the gamma1.
     * @return double
     */
    public double getGamma1() {
        return gamma1;
    }

    /**
     * Returns the gamma2.
     * @return double
     */
    public double getGamma2() {
        return gamma2;
    }

    /**
     * Returns the lambda.
     * @return double
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Returns the mu.
     * @return double
     */
    public double getMu() {
        return mu;
    }

    /**
     * Returns the nu.
     * @return double
     */
    public double getNu() {
        return nu;
    }

    /**
     * Sets the delta.
     * @param delta The delta to set
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }

    /**
     * Sets the gamma1.
     * @param gamma1 The gamma1 to set
     */
    public void setGamma1(double gamma1) {
        this.gamma1 = gamma1;
    }

    /**
     * Sets the gamma2.
     * @param gamma2 The gamma2 to set
     */
    public void setGamma2(double gamma2) {
        this.gamma2 = gamma2;
    }

    /**
     * Sets the lambda.
     * @param lambda The lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * Sets the mu.
     * @param mu The mu to set
     */
    public void setMu(double mu) {
        this.mu = mu;
    }

    /**
     * Sets the nu.
     * @param nu The nu to set
     */
    public void setNu(double nu) {
        this.nu = nu;
    }

    public static void main(String[] args) {
        Matrix m = Matrix.random(5, 5);
        m.print(5, 5);
        TimeServer ts = new TimeServer();
        ts.minor(m, 2, 2);
        System.out.println("Starting Simulator");
        System.out.println("     lambda = " + args[0]);
        System.out.println("         nu = " + args[1]);
        System.out.println("      delta = " + args[2]);
        System.out.println("      gamma1 = " + args[3]);
        System.out.println("      gamma2 = " + args[4]);
        System.out.println("      mu= " + args[5]);
        TimeServer t = new TimeServer(Double.parseDouble(args[0]),
                Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                Double.parseDouble(args[3]), Double.parseDouble(args[4]),
                Double.parseDouble(args[5]));
        System.out.println(t.evaluate());
    }
}
