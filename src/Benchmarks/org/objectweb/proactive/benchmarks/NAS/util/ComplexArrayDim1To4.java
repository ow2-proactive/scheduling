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

import java.io.Serializable;


/**
 * Especially design for the FT kernel translation from fortran.
 * You could change the number of dimensions and the dimensions size
 * dynamically.
 *
 */
public class ComplexArrayDim1To4 implements Serializable {

    private Complex[] array;
    private int d1;
    private int d2;
    private int d3;
    private int d4;
    private int source;

    public ComplexArrayDim1To4() {
    }

    public ComplexArrayDim1To4(Complex[] array, int rank) {
        System.out.println("\n\t\t**************** rank=" + rank + "\t" + "array.length=" + array.length +
            "  0:" + array[0] + "  1:" + array[1] + "  2:" + array[2]);
        this.array = array;
        this.source = rank;
        setDimension(array.length - 1, 0, 0);
    }

    /**
     * @param array   like fortran array (e.g. index go from 1 to n). The
     * first element array[0] is not used.
     */
    public ComplexArrayDim1To4(Complex[] array) {
        this(array, -1);
    }

    public void setSource(int s) {
        source = s;
    }

    public int getSource() {
        return source;
    }

    public void showMe_old(String str, int rank, int n) {
        boolean one = false;
        System.out.print("\n**** " + str + "  RANK = " + rank + " ****\t\t");
        for (int i = 1; i <= n; i++) {
            if (array[i] != null && array[i].getImg() != 0 && array[i].getReal() != 0) {
                one = true;
                System.out.print("[" + i + "]" + array[i] + "  ");
            }
        }
        if (!one)
            System.out.print("\t==== Array empty ====");
        System.out.println();
    }

    public void showMe(String str, int rank, int n) {
        showMe(str, rank, n, 1);
    }

    public void showMe(String str, int rank, int n, int offset) {
        for (int i = offset; i < n + offset; i++) {
            System.out.println(str + "[ " + rank + " / " + i + " ] = " + array[i] + "\t\tsize=" +
                array.length);
        }
    }

    public int getIndex(int x, int y) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        return x + ((y - 1) * d1);
    }

    public int getIndex(int x, int y, int z) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)))) {
            throw new RuntimeException("invalid coord" + ((0 < x) && (x <= d1)) + "  " +
                ((0 < y) && (y <= d2)) + " " + ((0 < z) && (z <= d3)));
        }
        return x + ((y - 1) * d1) + ((z - 1) * d1 * d2);
    }

    public int getIndex(int x, int y, int z, int a) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)) && ((0 < a) && (a <= d4)))) {
            throw new RuntimeException("invalid coord");
        }
        return x + ((y - 1) * d1) + ((z - 1) * d1 * d2) + ((a - 1) * d1 * d2 * d3);
    }

    /**
     * like <code>tab(nth) = c </code> in fortran
     *
     * @param nth 1 to n included
     * @param c
     */
    public void set(int nth, Complex c) {
        array[nth] = c;
    }

    /**
     * like <code>tab(x, y) = val </code>in fortran
     *
     * @param x 1 to d1 included
     * @param y 1 to d2 included
     * @param val
     */
    public void set(int x, int y, Complex val) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        array[x + ((y - 1) * d1)] = val;
    }

    public void setShift(int x, int y, Complex val, int shift) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        array[shift + x + ((y - 1) * d1)] = val;
    }

    /**
     * like <code>tab(x, y, z) = val</code> in fortran
     *
     * @param x 1 to d1 included
     * @param y 1 to d2 included
     * @param z 1 to d3 included
     * @param val
     */
    public void set(int x, int y, int z, Complex val) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)))) {
            throw new RuntimeException("invalid coord");
        }
        array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2)] = val;
    }

    /**
     * like <code>tab(x, y, z) = val</code> in fortran
     *
     * @param x 1 to d1 included
     * @param y 1 to d2 included
     * @param z 1 to d3 included
     * @param a 0 to d4 included
     * @param val
     */
    public void set(int x, int y, int z, int a, Complex val) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)) && ((0 < a) && (a <= d4)))) {
            throw new RuntimeException("invalid coord");
        }

        array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2) + ((a - 1) * d1 * d2 * d3)] = val;
    }

    /**
     * like <code>tab(x)</code> in fortran
     *
     * @param nth 1 to d1 included
     * @return the nth element
     */
    public Complex get(int nth) {
        return array[nth];
    }

    /**
     * like <code>tab(x)</code> in fortran
     *
     * @param nth 1 to d1 included
     * @return a copy of the nth element
     */
    public Complex getClone(int nth) {
        return (Complex) get(nth).clone();
    }

    public Complex getShift(int x, int shift) {
        return array[shift + x];
    }

    /**
     * like <code>tab(x)</code> in fortran
     *
     * @param x 1 to d1 included
     * @param y 1 to d2 included
     * @return the (x, y) element
     */
    public Complex get(int x, int y) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        return array[x + ((y - 1) * d1)];
    }

    /**
     * like <code>tab(x,y)</code> in fortran
     *
     * @param x 1 to d1 included
     * @param y 1 to d2 included
     * @return the (x, y) element
     */
    public Complex getClone(int x, int y) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        return (Complex) get(x, y).clone();
    }

    public Complex getShift(int x, int y, int shift) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        return array[shift + x + ((y - 1) * d1)];
    }

    public Complex getCloneShift(int x, int y, int shift) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)))) {
            throw new RuntimeException("invalid coord");
        }
        return (Complex) getShift(x, y, shift).clone();
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Complex get(int x, int y, int z) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) /*&&
                                                                                            ((0 < z) && (z <= d3))*/)) {
            throw new RuntimeException("invalid coord");
        }
        try {
            return array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2)];
        } catch (RuntimeException e) {
            System.err.println(d1 + " * " + d2 + " * " + d3 + " ; " + " (" + x + "," + y + "," + z + ")");
            throw e;
        }

        //        return get(x + ((y-1) * d1) + ((z-1) * d1 * d2));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Complex getClone(int x, int y, int z) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) /*&&
                                                                                            ((0 < z) && (z <= d3))*/)) {
            throw new RuntimeException("invalid coord " + ((0 < x) && (x <= d1)) + " " +
                ((0 < y) && (y <= d2)) + " " + ((0 < z) && (z <= d3)) + " " + z + " and dim = " + d3);
        }
        return (Complex) get(x, y, z).clone();
    }

    public Complex getShift(int x, int y, int z, int shift) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)))) {
            throw new RuntimeException("invalid coord");
        }
        return array[shift + x + ((y - 1) * d1) + ((z - 1) * d1 * d2)];
    }

    public Complex get(int x, int y, int z, int a) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)) && ((0 < a) && (a <= d4)))) {
            throw new RuntimeException("invalid coord");
        }
        return array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2) + ((a - 1) * d1 * d2 * d3)];
    }

    public Complex getClone(int x, int y, int z, int a) {
        if (!(((0 < x) && (x <= d1)) && ((0 < y) && (y <= d2)) && ((0 < z) && (z <= d3)) && ((0 < a) && (a <= d4)))) {
            throw new RuntimeException("invalid coord");
        }
        return (Complex) get(x, y, z, a).clone();
    }

    public void setDimension(int d1) {
        setDimension(d1, 1, 1, 1);
    }

    public void setDimension(int d1, int d2) {
        setDimension(d1, d2, 1, 1);
    }

    public void setDimension(int d1, int d2, int d3) {
        System.err.println("setDimension  d1: " + d1 + "  d2: " + d2 + "  d3: " + d3);
        setDimension(d1, d2, d3, 1);
    }

    public void setDimension(int d1, int d2, int d3, int d4) {
        System.err.println("setDimension  d1: " + d1 + "  d2: " + d2 + "  d3: " + d3 + "  d4: " + d4);
        this.d1 = d1;
        this.d2 = d2;
        this.d3 = d3;
        this.d4 = d4;
    }

    /* Multiplication */

    public void mult(int x, int y, int z, int t, Complex c) {
        array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2) + ((t - 1) * d1 * d2 * d3)].multMe(c);
    }

    public void mult(int x, int y, int z, Complex c) {
        array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2)].multMe(c);
    }

    public void mult(int x, int y, Complex c) {
        array[x + ((y - 1) * d1)].multMe(c);
    }

    public void mult(int x, Complex c) {
        array[x].multMe(c);
    }

    public void mult(int x, int y, int z, int t, double c) {
        array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2) + ((t - 1) * d1 * d2 * d3)].multMe(c);
    }

    public void mult(int x, int y, int z, double c) {
        array[x + ((y - 1) * d1) + ((z - 1) * d1 * d2)].multMe(c);
    }

    public void mult(int x, int y, double c) {
        array[x + ((y - 1) * d1)].multMe(c);
    }

    public void mult(int x, double c) {
        array[x].multMe(c);
    }

    public void set(int x, int y, int z, int t, ComplexArrayDim1To4 u, int i, int j, int k, int l) {
        int a = x + ((y - 1) * d1) + ((z - 1) * d1 * d2) + ((t - 1) * d1 * d2 * d3);
        int b = i + ((j - 1) * d1) + ((k - 1) * d1 * d2) + ((l - 1) * d1 * d2 * d3);
        array[a].real = u.array[b].real;
        array[a].img = u.array[b].img;
    }

    public void set(int x, int y, int z, ComplexArrayDim1To4 u, int i, int j, int k) {
        int a = x + ((y - 1) * d1) + ((z - 1) * d1 * d2);
        int b = i + ((j - 1) * d1) + ((k - 1) * d1 * d2);
        array[a].real = u.array[b].real;
        array[a].img = u.array[b].img;
    }

    public void set(int x, int y, ComplexArrayDim1To4 u, int i, int j) {
        int a = x + ((y - 1) * d1);
        int b = i + ((j - 1) * d1);
        array[a].real = u.array[b].real;
        array[a].img = u.array[b].img;
    }

    public void set(int x, ComplexArrayDim1To4 u, int i) {
        array[x].real = u.array[i].real;
        array[x].img = u.array[i].img;
    }

    /* Set from different dimension */
    public void set(int x, int y, ComplexArrayDim1To4 u, int i, int j, int k) {
        int a = x + ((y - 1) * d1);
        int b = i + ((j - 1) * d1 + ((k - 1) * d1 * d2));
        array[a].real = u.array[b].real;
        array[a].img = u.array[b].img;
    }

    public Complex[] getArray() {
        return array;
    }
}
