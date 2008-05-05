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

public class ComplexArray implements java.io.Serializable {

    private static final int REAL = 0;
    private static final int IMG = 1;
    private static int quantity = 0; // DEBUG

    protected double[][] array;
    private int arraySize;
    private int rank;
    private int shift;

    private double re11, im11, re21, im21;

    /* Constructors */
    public ComplexArray() {
    }

    public ComplexArray(int arraySize, int rank) {
        quantity++;
        this.arraySize = arraySize;
        this.rank = rank;
        shift = 0;
        array = new double[2][arraySize]; //[0][x]->real   [1][x]->img
    }

    /* Public methods */
    public int getSize() {
        return arraySize;
    }

    public int getRank() {
        return rank;
    }

    public void stockham(int localShift, ComplexArray x, int shiftX, double real, double img, int lk, int ny,
            int i11, int i12, int i21, int i22, int xdim) {
        int n11, n12, n21, n22;
        for (int k = 0; k < lk; k++) {
            n11 = (i11 + k) * xdim + shiftX;
            n12 = (i12 + k) * xdim + shiftX;
            n21 = (i21 + k) * xdim + localShift;
            n22 = (i22 + k) * xdim + localShift;
            for (int j = 0; j < ny; j++) {
                re11 = x.array[REAL][n11 + j];
                im11 = x.array[IMG][n11 + j];
                re21 = x.array[REAL][n12 + j];
                im21 = x.array[IMG][n12 + j];
                array[REAL][n21 + j] = re11 + re21;
                array[IMG][n21 + j] = im11 + im21;
                re11 -= re21;
                im11 -= im21;
                array[REAL][n22 + j] = re11 * real - im11 * img;
                array[IMG][n22 + j] = re11 * img + im11 * real;
            }
        }
    }

    /* GETTERS */
    public double getReal(int i) {
        return array[REAL][i + shift];
    }

    public double getImg(int i) {
        return array[IMG][i + shift];
    }

    /* SETTERS */
    public void setReal(int i, double value) {
        array[REAL][i + shift] = value;
    }

    public void setImg(int i, double value) {
        array[IMG][i + shift] = value;
    }

    public void set(int i, double real, double img) {
        array[REAL][i + shift] = real;
        array[IMG][i + shift] = img;
    }

    /* OPERATIONS */
    public void mult(int i, double value) {
        array[REAL][i + shift] *= value;
        array[IMG][i + shift] *= value;
    }

    public void div(int i, double value) {
        array[REAL][i + shift] /= value;
        array[IMG][i + shift] /= value;
    }

    public void add(int i, double real, double img) {
        array[REAL][i + shift] += real;
        array[IMG][i + shift] += img;
    }

    public void mult(int i, double real, double img) {
        array[REAL][i + shift] = (array[REAL][i] * real) - (array[IMG][i + shift] * img);
        array[IMG][i + shift] = (array[REAL][i] * img) + (array[IMG][i + shift] * real);
    }

    /* DEBUG */
    public double[] getSum() {
        double real = 0, img = 0;
        for (int i = 0; i < array.length; i++) {
            real += array[REAL][i];
            img += array[IMG][i];
        }
        return new double[] { real, img, array.length };
    }

    public static int getQuantity() {
        return quantity;
    }
}
