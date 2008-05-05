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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;


/**
 * This data structure represents a large complex array distributed on
 * multiple sub arrays all defined as a scatter group.
 */
public class ComplexArrayGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private ComplexArray arrays;
    private ComplexArray[] array; // just for efficient access
    private int rank;
    public int dataSize;
    private double[][][] cache, cache2;

    private int d0, /* d1, d2, d3, */d01, d012;

    // ------------------------------------------------------------------------
    // 		CONSTRUCTORS
    // ------------------------------------------------------------------------
    public ComplexArrayGroup() {
    }

    public ComplexArrayGroup(int rank, int groupSize, int dataSize) {
        try {
            this.rank = rank;
            this.dataSize = dataSize;

            array = new ComplexArray[groupSize];
            for (int i = 0; i < array.length; i++) {
                array[i] = new ComplexArray(dataSize, rank);
            }

            arrays = (ComplexArray) PAGroup.newGroup(ComplexArray.class.getName());
            Group<ComplexArray> gArrays = PAGroup.getGroup(arrays);
            PAGroup.setScatterGroup(arrays);

            for (int i = 0; i < groupSize; i++) {
                if (!gArrays.add(array[i])) {
                    throw new RuntimeException("Can't create group");
                }
            }

            gArrays = null; // free memory
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // 		MISCELLANEOUS METHODS
    // ------------------------------------------------------------------------
    public ComplexArray getTypedGroup() {
        return arrays;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setComplexArray(int i, ComplexArray ca) {
        array[i] = ca;
    }

    public void setDimension(int d0, int d1, int d2, int d3) {
        this.d0 = d0;
        //		this.d1 = d1;
        //		this.d2 = d2;
        //		this.d3 = d3;
        d01 = d0 * d1;
        d012 = d01 * d2;
    }

    public void stockham(int localShift, ComplexArrayGroup x, int shiftX, double real, double img, int lk,
            int ny, int i11, int i12, int i21, int i22) {
        array[0].stockham(localShift, x.array[0], shiftX, real, img, lk, ny, i11, i12, i21, i22, x.d0);
    }

    public int resolve(int[] coords) {
        switch (coords.length) {
            case 1:
                return coords[0];
            case 2:
                return resolve(coords[0], coords[1]);
            case 3:
                return resolve(coords[0], coords[1], coords[2]);
            case 4:
                return resolve(coords[0], coords[1], coords[2], coords[3]);
            default:
                throw new RuntimeException("Invalid coordinates");
        }
    }

    public final int resolve(int a, int b) {
        return a + b * d0;
    }

    public final int resolve(int a, int b, int c) {
        return a + b * d0 + c * d01;
    }

    public final int resolve(int a, int b, int c, int d) {
        return a + b * d0 + c * d01 + d * d012;
    }

    // ------------------------------------------------------------------------
    // 		GETTERS			Dimension 1 to 4
    // ------------------------------------------------------------------------
    // Masters
    public double getReal(int n) {
        return select(n).getReal(n % dataSize);
    }

    public double getImg(int n) {
        return select(n).getImg(n % dataSize);
    }

    // getReal
    public final double getReal(int a, int b) {
        return getReal(resolve(a, b));
    }

    public final double getReal(int a, int b, int c) {
        return getReal(resolve(a, b, c));
    }

    public final double getReal(int a, int b, int c, int d) {
        return getReal(resolve(a, b, c, d));
    }

    public final double getReal(int[] target) {
        return getReal(resolve(target));
    }

    // getImg
    public final double getImg(int a, int b) {
        return getImg(resolve(a, b));
    }

    public final double getImg(int a, int b, int c) {
        return getImg(resolve(a, b, c));
    }

    public final double getImg(int a, int b, int c, int d) {
        return getImg(resolve(a, b, c, d));
    }

    public final double getImg(int[] target) {
        return getImg(resolve(target));
    }

    // ------------------------------------------------------------------------
    // 		SETTERS			Dimension 1 to 3
    // ------------------------------------------------------------------------
    // Masters
    public void setReal(int n, double value) {
        select(n).setReal(n % dataSize, value);
    }

    public void setImg(int n, double value) {
        select(n).setImg(n % dataSize, value);
    }

    public void set(int n, double real, double img) {
        select(n).set(n % dataSize, real, img);
    }

    // setReal
    public final void setReal(int a, int b, double value) {
        setReal(resolve(a, b), value);
    }

    public final void setReal(int a, int b, int c, double value) {
        setReal(resolve(a, b, c), value);
    }

    public final void setReal(int a, int b, int c, int d, double value) {
        setReal(resolve(a, b, c, c), value);
    }

    public final void setReal(int[] target, double value) {
        setReal(resolve(target), value);
    }

    // setImg
    public final void setImg(int a, int b, double value) {
        setImg(resolve(a, b), value);
    }

    public final void setImg(int a, int b, int c, double value) {
        setImg(resolve(a, b, c), value);
    }

    public final void setImg(int a, int b, int c, int d, double value) {
        setImg(resolve(a, b, c, d), value);
    }

    public final void setImg(int[] target, double value) {
        setImg(resolve(target), value);
    }

    // set
    public final void set(int a, int b, double r, double i) {
        set(resolve(a, b), r, i);
    }

    public final void set(int a, int b, int c, double r, double i) {
        set(resolve(a, b, c), r, i);
    }

    public final void set(int a, int b, int c, int d, double r, double i) {
        set(resolve(a, b, c, d), r, i);
    }

    public final void set(int[] target, double r, double i) {
        set(resolve(target), r, i);
    }

    public final void set(int a, int b, double[] value) {
        set(resolve(a, b), value[0], value[1]);
    }

    public final void set(int a, int b, int c, double[] value) {
        set(resolve(a, b, c), value[0], value[1]);
    }

    public final void set(int a, int b, int c, int d, double[] value) {
        set(resolve(a, b, c, d), value[0], value[1]);
    }

    public final void set(int[] target, double[] value) {
        set(resolve(target), value[0], value[1]);
    }

    // ------------------------------------------------------------------------
    // 		OPERATIONS		Dimension 1 to 3
    // ------------------------------------------------------------------------
    // Masters
    public void mult(int n, double value) {
        select(n).mult(n % dataSize, value);
    }

    public void mult(int n, int m) {
        ComplexArray cam = select(m);
        int mds = m % dataSize;
        select(n).mult(n % dataSize, cam.getReal(mds), cam.getImg(mds));
    }

    public void div(int n, double value) {
        select(n).div(n % dataSize, value);
    }

    public void add(int n, double real, double img) {
        select(n).add(n % dataSize, real, img);
    }

    public void add(int n, int m) {
        ComplexArray cam = select(m);
        int mds = m % dataSize;
        select(n).add(n % dataSize, cam.getReal(mds), cam.getImg(mds));
    }

    // mult
    public final void mult(int a, int b, double value) {
        mult(resolve(a, b), value);
    }

    public final void mult(int a, int b, int c, double value) {
        mult(resolve(a, b, c), value);
    }

    public final void mult(int a, int b, int c, int d, double value) {
        mult(resolve(a, b, c, d), value);
    }

    public final void mult(int[] target, double value) {
        mult(resolve(target), value);
    }

    public final void mult(int a, int b, int x, int y) {
        mult(resolve(a, b), resolve(x, y));
    }

    public final void mult(int a, int b, int c, int x, int y, int z) {
        mult(resolve(a, b, c), resolve(x, y, z));
    }

    public final void mult(int a, int b, int c, int d, int x, int y, int z, int t) {
        mult(resolve(a, b, c, d), resolve(x, y, z, t));
    }

    public final void mult(int[] target, int[] source) {
        mult(resolve(target), resolve(source));
    }

    // div
    public final void div(int a, int b, double value) {
        div(resolve(a, b), value);
    }

    public final void div(int a, int b, int c, double value) {
        div(resolve(a, b, c), value);
    }

    public final void div(int a, int b, int c, int d, double value) {
        div(resolve(a, b, c, d), value);
    }

    public final void div(int[] target, double value) {
        div(resolve(target), value);
    }

    // add
    public final void add(int a, int b, double real, double img) {
        add(resolve(a, b), real, img);
    }

    public final void add(int a, int b, int c, double real, double img) {
        add(resolve(a, b, c), real, img);
    }

    public final void add(int a, int b, int c, int d, double real, double img) {
        add(resolve(a, b, c, d), real, img);
    }

    public final void add(int[] target, double real, double img) {
        add(resolve(target), real, img);
    }

    public final void add(int a, int b, int x, int y) {
        add(resolve(a, b), resolve(x, y));
    }

    public final void add(int a, int b, int c, int x, int y, int z) {
        add(resolve(a, b, c), resolve(x, y, z));
    }

    public final void add(int a, int b, int c, int d, int x, int y, int z, int t) {
        add(resolve(a, b, c, d), resolve(x, y, z, t));
    }

    public final void add(int[] target, int[] source) {
        add(resolve(target), resolve(source));
    }

    // ------------------------------------------------------------------------
    // 		PRIVATE METHODS
    // ------------------------------------------------------------------------
    public final ComplexArray select(int n) {
        return array[n / dataSize];
    }

    // TODO: brian: Remove debugging stuff
    // ------------------------------------------------------------------------
    // 		DEBUGGING STUFF
    // ------------------------------------------------------------------------
    private void message(String str) {
        System.out.println("\t " + rank + "\t--------> " + str);
    }

    public void showInfo(String str) {
        message(str + "\t CAG=" + getObjectSize(this) + " KB" + "\t CA[0]=" + getObjectSize(array[0]) +
            " KB" + "\t CA.len=" + array.length + "\t d[][]=" + getObjectSize(array[0].array) + " KB");
    }

    public void showAll(String str, int n) {
        showAll(str, n, 0);
    }

    public void showAll(String str, int n, int shift) {
        DecimalFormat df = new DecimalFormat("#####.###");
        double[] sumAll = new double[] { 0, 0, 0 };
        for (int i = 0; i < array.length; i++) {
            double[] sum = array[i].getSum();
            sumAll[0] += sum[0];
            sumAll[1] += sum[1];
            sumAll[2] += sum[2];
        }
        message("Show " + n + " first elements of each member of " + str + " -> Full sum = " +
            df.format(sumAll[0]) + " ; " + df.format(sumAll[1]) + " [" + sumAll[2] + "]");
        String result = "";
        for (int i = 0; i < array.length; i++) {
            result += "\t\t [" + i + "] = ";
            for (int j = 0; j < n; j++) {
                result += " " + df.format(array[i].getReal(j + shift)) + " " +
                    df.format(array[i].getImg(j + shift)) + "\t";
            }
            result += "\n";
        }
        System.out.println(result);
    }

    public static void main(String[] args) {
        ComplexArrayGroup cag = new ComplexArrayGroup(0, 4, 8);
        cag.showAll("cag-init", 8);
        cag.setDimension(8, 0, 0, 0);
        for (int i = 0; i < 32; i++) {
            cag.set(i, i, i);
        }
        cag.showAll("cag-set", 8);
    }

    public static int getObjectSize(Object object) {
        if (object == null) {
            //System.err.println("Object is null. Cannot measure.");
            return -1;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            oos.close();
            baos.close();
            return bytes.length / 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void arraycopy11(ComplexArrayGroup src, int li, int lj, int jj, int k) {
        ComplexArray in, base = array[0];
        int n, res;
        for (int i = 0; i < li; i++) {
            res = src.resolve(0, i + jj, k);
            in = src.select(res);
            for (int j = 0; j < lj; j++) {
                n = (res + j) % src.dataSize;
                base.array[0][resolve(i, j)] = in.array[0][n];
                base.array[1][resolve(i, j)] = in.array[1][n];
            }
        }
    }

    public void arraycopy12(ComplexArrayGroup src, int li, int lj, int jj, int k) {
        ComplexArray out, base = src.array[0];
        int n, m, res;
        for (int i = 0; i < li; i++) {
            res = resolve(0, i + jj, k);
            out = select(res);
            for (int j = 0; j < lj; j++) {
                n = src.resolve(i, j);
                m = (res + j) % dataSize;
                out.array[0][m] = base.array[0][n];
                out.array[1][m] = base.array[1][n];
            }
        }
    }

    public void arraycopy21(ComplexArrayGroup src, int li, int lj, int jj, int k) {
        ComplexArray in, base = array[0];
        int n, m, res1, res2;
        for (int i = 0; i < li; i++) {
            res1 = src.resolve(jj, i, k);
            res2 = resolve(0, i);
            in = src.select(res1);
            for (int j = 0; j < lj; j++) {
                n = (res1 + j) % src.dataSize;
                m = res2 + j;
                base.array[0][m] = in.array[0][n];
                base.array[1][m] = in.array[1][n];
            }
        }
    }

    public void arraycopy22(ComplexArrayGroup src, int li, int lj, int jj, int k) {
        ComplexArray out, base = src.array[0];
        int n, m, res1, res2;
        for (int i = 0; i < li; i++) {
            res1 = src.resolve(0, i);
            res2 = resolve(jj, i, k);
            out = select(res2);
            for (int j = 0; j < lj; j++) {
                n = res1 + j;
                m = (res2 + j) % dataSize;
                out.array[0][m] = base.array[0][n];
                out.array[1][m] = base.array[1][n];
            }
        }
    }

    public void arraycopy31(ComplexArrayGroup src, int li, int lj, int jj, int k) {
        ComplexArray in, base = array[0];
        int n, m, res1, res2;
        for (int i = 0; i < li; i++) {
            res1 = src.resolve(jj, k, i);
            res2 = resolve(0, i);
            in = src.select(res1);
            for (int j = 0; j < lj; j++) {
                n = (res1 + j) % src.dataSize;
                m = res2 + j;
                base.array[0][m] = in.array[0][n];
                base.array[1][m] = in.array[1][n];
            }
        }
    }

    public void arraycopy32(ComplexArrayGroup src, int li, int lj, int jj, int k) {
        ComplexArray out, base = src.array[0];
        int n, m, res1, res2;
        for (int i = 0; i < li; i++) {
            res1 = src.resolve(0, i);
            res2 = resolve(jj, k, i);
            out = select(res2);
            for (int j = 0; j < lj; j++) {
                n = res1 + j;
                m = (res2 + j) % dataSize;
                out.array[0][m] = base.array[0][n];
                out.array[1][m] = base.array[1][n];
            }
        }
    }

    public void transpose(ComplexArrayGroup xin, int n, int m) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                set(i, j, xin.getReal(j, i), xin.getImg(j, i));
            }
        }
    }

    public void transposeBlock(ComplexArrayGroup xin, int transblock, int i, int j) {
        int pos, posi, res;
        double[][] tab;
        if (cache == null)
            cache = new double[2][transblock + 1][transblock + 1];

        for (int jj = 0; jj < transblock; jj++) {
            pos = xin.resolve(i, j + jj);
            tab = xin.select(pos).array;
            for (int ii = 0; ii < transblock; ii++) {
                posi = (pos + ii) % dataSize;
                cache[0][jj][ii] = tab[0][posi];
                cache[1][jj][ii] = tab[1][posi];
            }
        }

        for (int ii = 0; ii < transblock; ii++) {
            res = resolve(j, i + ii);
            for (int jj = 0; jj < transblock; jj++) {
                tab = select(res + jj).array;
                posi = (res + jj) % dataSize;
                tab[0][posi] = cache[0][jj][ii];
                tab[1][posi] = cache[1][jj][ii];
            }
        }
    }

    public void transposeBlock(ComplexArrayGroup xin, int block1, int block2, int ii, int j, int kk) {
        int posi, res, i, k;
        double[][] tab;
        i = 0;
        k = 0;
        res = 0;
        if (cache2 == null)
            cache2 = new double[2][block2 + 1][block1 + 1];

        message("block1=" + block1 + "  block2=" + block2);

        message("Phase 1: xin.dataSize = " + xin.dataSize);
        for (k = 0; k < block2; k++) {
            res = xin.resolve(ii, j, k + kk);
            for (i = 0; i < block1; i++) {
                tab = xin.select(res + i).array;
                posi = (res + i) % dataSize;
                cache2[0][k][i] = tab[0][posi];
                cache2[1][k][i] = tab[1][posi];
            }
        }

        try {
            message("Phase 2: dataSize = " + dataSize);
            for (i = 0; i < block1; i++) {
                res = resolve(kk, j, i + ii);
                for (k = 0; k < block2; k++) {
                    tab = select(res + k).array;
                    posi = (res + k) % dataSize;
                    tab[0][posi] = cache2[0][k][i];
                    tab[1][posi] = cache2[1][k][i];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            message("i=" + i + "  k=" + k + "  array.length=" + array.length + "  array.array.length=" +
                array[0].array.length + "  res+k=" + (res + k));
            if (this.rank == 0 || this.rank == 7 || this.rank == 56) {
                e.printStackTrace();
            }
            System.exit(1);
        }

    }

    public void evolve(ComplexArrayGroup u1, int a, int b, int c, double[] twiddle) {
        int pos, res;
        double tw;
        double[][] tu0, tu1;

        for (int k = 0; k < c; k++) {
            for (int j = 0; j < b; j++) {
                res = resolve(0, j, k); // this and u1 have the same dimension
                tu1 = u1.select(res).array;
                tu0 = select(res).array;
                for (int i = 0; i < a; i++) {
                    pos = (res + i) % dataSize;
                    tw = twiddle[res + i];
                    tu0[0][pos] *= tw;
                    tu0[1][pos] *= tw;
                    tu1[0][pos] = tu0[0][pos];
                    tu1[1][pos] = tu0[1][pos];
                }
            }
        }
    }

    public double[] checksum(int nx, int ny, int nz, int xstart, int xend, int ystart, int yend, int zstart,
            int zend) {
        int q, r, s, n;
        double real = 0;
        double img = 0;

        for (int j = 1; j <= 1024; j++) {
            q = (j % nx) + 1;
            if ((q >= xstart) && (q <= xend)) {
                r = ((3 * j) % ny) + 1;
                if ((r >= ystart) && (r <= yend)) {
                    s = ((5 * j) % nz) + 1;
                    if ((s >= zstart) && (s <= zend)) {
                        n = resolve(q - xstart, r - ystart, s - zstart);
                        real += select(n).array[0][n % dataSize];
                        img += select(n).array[1][n % dataSize];
                    }
                }
            }
        }
        return new double[] { real, img };
    }
}
