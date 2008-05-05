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

public class Shift {
    private ComplexArrayGroup cag;
    private int shift;

    public Shift(ComplexArrayGroup cag) {
        this.cag = cag;
        this.shift = 0;
    }

    // SHIFT MANAGEMENT
    public void setShift(int a) {
        shift = a;
    }

    public void setShift(int a, int b) {
        shift = cag.resolve(a, b);
    }

    public void setShift(int a, int b, int c) {
        shift = cag.resolve(a, b, c);
    }

    public void setShift(int a, int b, int c, int d) {
        shift = cag.resolve(a, b, c, d);
    }

    public int getShift() {
        return shift;
    }

    // SETTERS
    public void set(int n, double real, double img) {
        cag.set(n + shift, real, img);
    }

    public final void set(int a, int b, double real, double img) {
        set(cag.resolve(a, b), real, img);
    }

    public final void set(int a, int b, int c, double real, double img) {
        set(cag.resolve(a, b, c), real, img);
    }

    public final void set(int a, int b, int c, int d, double real, double img) {
        set(cag.resolve(a, b, c, d), real, img);
    }

    // GETTERS
    public double getReal(int n) {
        return cag.getReal(n + shift);
    }

    public double getImg(int n) {
        return cag.getImg(n + shift);
    }

    public final double getReal(int a, int b) {
        return getReal(cag.resolve(a, b));
    }

    public final double getImg(int a, int b) {
        return getImg(cag.resolve(a, b));
    }

    public final double getReal(int a, int b, int c) {
        return getReal(cag.resolve(a, b, c));
    }

    public final double getImg(int a, int b, int c) {
        return getImg(cag.resolve(a, b, c));
    }

    public final double getReal(int a, int b, int c, int d) {
        return getReal(cag.resolve(a, b, c, d));
    }

    public final double getImg(int a, int b, int c, int d) {
        return getImg(cag.resolve(a, b, c, d));
    }

    // MISC
    public void setDimension(int a, int b, int c, int d) {
        cag.setDimension(a, b, c, d);
    }

    public ComplexArrayGroup getCAG() {
        return this.cag;
    }

    public void stockham(Shift x, double real, double img, int lk, int ny, int i11, int i12, int i21, int i22) {
        cag.stockham(shift, x.cag, x.shift, real, img, lk, ny, i11, i12, i21, i22);
    }

    public int resolve(int a, int b) {
        return cag.resolve(a, b);
    }
}
