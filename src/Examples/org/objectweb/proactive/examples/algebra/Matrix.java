/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.algebra;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Matrix implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    int m; // height
    int n; // width
    double[][] elements; // values

    public Matrix() {
        super();
    }

    public Matrix(int _m, int _n) {
        super();
        this.m = _m;
        this.n = _n;
        this.elements = new double[_m][_n];
    }

    public Matrix(int _n) {
        this(_n, _n);
    }

    public Matrix(Matrix _m) {
        this(_m.getHeight(), _m.getWidth());
        logger.info("Matrix: constructor (Matrix _m)");
        int i;
        Row r;

        for (i = 0; i < m; i++) {
            r = _m.getRow(i);
            this.setRow(i, r);
        }
    }

    public Matrix(Column c_) {
        this(c_.getSize(), 1);
        this.setColumn(0, c_);
    }

    public Matrix(Row r_) {
        this(1, r_.getSize());
        this.setRow(0, r_);
    }

    public Matrix identity(int n) {
        // Returns an I matrix of size n
        int i;
        Matrix m;

        m = new Matrix(n);

        for (i = 0; i < n; i++) {
            m.setElement(i, i, 1.0);
        }
        return m;
    }

    public Matrix identity() {
        Matrix result;
        if (this.m != this.n) {
            return null;
        }
        result = identity(this.m);
        return result;
    }

    public double makeDiagOne(int i) {
        double a;
        int j;

        a = this.getElement(i, i);
        if (a == 0) {
            return a;
        }

        for (j = 0; j < this.n; j++) {
            this.setElement(i, j, this.getElement(i, j) / a);
        }
        return a;
    }

    public/* synchronized */Matrix transpose() {
        Matrix mat;
        int i;
        int j;

        mat = new Matrix(this.n, this.m); // m and n are swapped

        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                mat.setElement(j, i, this.getElement(i, j));
            }
        }

        return mat;
    }

    public/* synchronized */void display() {
        int i;
        int j;

        for (i = 0; i < this.m; i++) {
            logger.info(i + " |");
            for (j = 0; j < this.n; j++) {
                logger.info(this.getElement(i, j) + " ");
            }
            logger.info("|");
        }

        System.out.println();

        return;
    }

    public void randomizeFillIn() {
        int i;
        int j;

        for (i = 0; i < this.m; i++) {
            for (j = 0; j < this.n; j++) {
                this.setElement(i, j, Math.random());
            }
        }
        return;
    }

    public/* synchronized */void setElement(int i, int j, double x) {
        this.elements[i][j] = x;
        return;
    }

    public/* synchronized */double getElement(int i, int j) {
        return this.elements[i][j];
    }

    public/* synchronized */void setRow(int a, Vector r) {
        int j;

        for (j = 0; j < this.n; j++) {
            this.setElement(a, j, r.getElement(j));
        }
        return;
    }

    public/* synchronized */Row getRow(int i) {
        Row r;
        int j;

        r = new Row(this.n);
        for (j = 0; j < this.n; j++) {
            r.setElement(j, this.getElement(i, j));
        }

        return r;
    }

    public/* synchronized */void setColumn(int a, Vector c) {
        int i;

        for (i = 0; i < this.m; i++) {
            this.setElement(i, a, c.getElement(i));
        }
        return;
    }

    public/* synchronized */Column getColumn(int j) {
        Column c;
        int i;

        c = new Column(this.m);
        for (i = 0; i < this.m; i++) {
            c.setElement(i, this.getElement(i, j));
        }

        return c;
    }

    public Matrix getBlock(int i0, int j0, int i1, int j1) {
        Matrix result;
        int i;
        int j;

        result = new Matrix((i1 - i0 + 1), (j1 - j0 + 1));

        for (i = i0; i <= i1; i++) {
            for (j = j0; j <= j1; j++) {
                result.setElement((i - i0), (j - j0), this.getElement(i, j));
            }
        }

        return result;
    }

    public void setBlock(int i0, int j0, Matrix m) {
        int i;
        int j;

        for (i = 0; i < m.getHeight(); i++) {
            for (j = 0; j < m.getWidth(); j++) {
                this.setElement(i + i0, j + j0, m.getElement(i, j));
            }
        }
        return;
    }

    public void swapRows(int a, int b) {
        Row firstrow;
        Row lastrow;

        firstrow = this.getRow(a);
        lastrow = this.getRow(b);

        this.setRow(a, lastrow);
        this.setRow(b, firstrow);

        return;
    }

    public void swapColumns(int a, int b) {
        Column firstcol;
        Column lastcol;

        firstcol = this.getColumn(a);
        lastcol = this.getColumn(b);

        this.setColumn(a, lastcol);
        this.setColumn(b, firstcol);

        return;
    }

    public Matrix rightProduct(Matrix m) {
        // Let's verify dimensions (product is this*m)
        int i;

        // Let's verify dimensions (product is this*m)
        int j;

        // Let's verify dimensions (product is this*m)
        int k;
        Matrix result;
        double s;

        if (this.getWidth() != m.getHeight()) {
            return null;
        }

        result = new Matrix(this.getHeight(), m.getWidth());

        for (i = 0; i < result.getHeight(); i++) {
            for (j = 0; j < result.getWidth(); j++) {
                s = 0;
                for (k = 0; k < m.getHeight(); k++) {
                    s = s + (this.getElement(i, k) * m.getElement(k, j));
                }
                result.setElement(i, j, s);
            }
        }

        return result;
    }

    public Vector rightProduct(Vector v) {
        Vector result = new Vector(this.getHeight());
        int i;
        int j;
        double s;

        for (i = 0; i < this.getHeight(); i++) {
            s = 0;
            for (j = 0; j < this.getWidth(); j++) {
                s = s + (this.getElement(i, j) * v.getElement(j));
            }
            result.setElement(i, s);
        }

        return result;
    }

    public int getWidth() {
        return this.n;
    }

    public int getHeight() {
        return this.m;
    }

    public/* synchronized */void rowLC(int firstrow, int secondrow, double alpha, double beta) {
        int j;

        for (j = 0; j < this.n; j++) {
            this.setElement(firstrow, j, (alpha * this.getElement(firstrow, j)) +
                (beta * this.getElement(secondrow, j)));
        }
        return;
    }

    public/* synchronized */void columnLC(int firstcol, int secondcol, double alpha, double beta) {
        int i;

        for (i = 0; i < this.m; i++) {
            this.setElement(i, firstcol, (alpha * this.getElement(i, firstcol)) +
                (beta * this.getElement(i, secondcol)));
        }
        return;
    }

    public Matrix getInverse() {
        Matrix source;
        Matrix result;
        int i;
        int p;
        int r;
        int s;
        double alpha;
        double beta;
        double gamma;

        source = new Matrix(this);

        if (source.m != source.n) {
            return null; // is a square matrix ?
        }
        s = source.m;

        result = identity(s);

        for (r = 0; r < s; r++) {
            // Choix du pivot 
            p = source.findPivot(r);

            source.swapRows(r, p); // Le pivot est maintenant sur la ligne courante (ligne r)
            result.swapRows(r, p);

            // On normalise la ligne du pivot
            gamma = source.makeDiagOne(r);
            result.rowLC(r, 0, 1 / gamma, 0); // On multiplie de la meme maniere sur l'autre

            // On elimine les zeros dans la colonne du pivot, en dessous ET en dessus de la 
            // ligne courante
            for (i = 0; i < r; i++) {
                alpha = 1;
                beta = ((-1) * source.getElement(i, r)) / source.getElement(r, r);
                source.rowLC(i, r, alpha, beta);
                result.rowLC(i, r, alpha, beta);
            }
            for (i = r + 1; i < s; i++) {
                alpha = 1;
                beta = ((-1) * source.getElement(i, r)) / source.getElement(r, r);
                source.rowLC(i, r, alpha, beta);
                result.rowLC(i, r, alpha, beta);
            }
        }
        logger.info("Inversion terminee pour n = " + this.m);

        return result;
    }

    private int findPivot(int r) {
        // Find pivot row for rank r
        int i;

        // Find pivot row for rank r
        int result;

        result = r;
        for (i = r; i < this.m; i++) {
            if (Math.abs(this.getElement(i, r)) > result) {
                result = i;
            }
        }
        return result;
    }

    public double distance(Matrix mat) {
        double result = 0;
        double temp = 0;
        int i;
        int j;

        if (this.m != mat.getHeight()) {
            return -1;
        }
        if (this.n != mat.getWidth()) {
            return -1;
        }

        for (i = 0; i < this.m; i++) {
            for (j = 0; j < this.n; j++) {
                temp = Math.abs(this.getElement(i, j) - mat.getElement(i, j));
                if (temp > result) {
                    result = temp;
                }
            }
        }
        return temp;
    }

    public double trace() {
        double result = 0;
        int i;

        if (this.m != this.n) {
            return 0;
        }
        for (i = 0; i < m; i++) {
            result = result + this.getElement(i, i);
        }
        return result;
    }
}
