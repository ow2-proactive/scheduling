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
import java.text.DecimalFormat;


/**
 * A representation of complex. Warning! Complex objects are mutable.
 *
 */
public class Complex implements Cloneable, Serializable {

    private static int quantity = 0;// DEBUG
    public double real;
    public double img;

    public Complex() {
        this(0., 0.);
    }

    public Complex(double r) {
        this(r, 0.);
    }

    public Complex(double[] val) {
        this(val[0], val[1]);
    }

    public Complex(double real, double img) {
        quantity++;
        this.real = real;
        this.img = img;
    }

    public void set(double r, double i) {
        this.real = r;
        this.img = i;
    }

    public double getImg() {
        return img;
    }

    public void setImg(double img) {
        this.img = img;
    }

    public double getReal() {
        return real;
    }

    public double[] get() {
        return new double[] { real, img };
    }

    public void set(double[] value) {
        this.real = value[0];
        this.img = value[1];
    }

    public void setReal(double real) {
        this.real = real;
    }

    public Complex div(double d) {
        return new Complex(this.real / d, this.img / d);
    }

    public void divMe(double d) {
        this.real = this.real / d;
        this.img = this.img / d;
    }

    public Complex plus(Complex complex) {
        return new Complex(this.real + complex.real, this.img + complex.img);
    }

    public void plusMe(Complex complex) {
        this.real += complex.real;
        this.img += complex.img;
    }

    public void plusMe(double real, double img) {
        this.real += real;
        this.img += img;
    }

    public Complex minus(Complex c) {
        return new Complex(this.real - c.real, this.img - c.img);
    }

    public Complex mult(double d) {
        return new Complex(this.real * d, this.img * d);
    }

    public Complex mult(Complex complex) {
        return new Complex((this.real * complex.real) - (this.img * complex.img), (this.real * complex.img) +
            (this.img * complex.real));
    }

    public void multMe(double d) {
        this.real *= d;
        this.img *= d;
    }

    public void multMe(Complex complex) {
        this.real = (this.real * complex.real) - (this.img * complex.img);
        this.img = (this.real * complex.img) + (this.img * complex.real);
    }

    /**
     * A function that returns the complex conjugate of the argument.  If
     * the complex is (X,Y), its complex conjugate is (X,-Y).
     * @return the conjugate of this
     */
    public Complex conjg() {
        return new Complex(this.real, -this.img);
    }

    public String toString() {
        DecimalFormat norm = new DecimalFormat("0.0000000000");
        return "(" + norm.format(this.real) + "  \t " + norm.format(this.img) + ")";
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Complex c1;
        Complex c2;

        c1 = new Complex(1., 2);

        c2 = new Complex(0, 1.);

        System.out.println(" c1: " + c1 + " c2: " + c2);

        System.out.println(" c1.getReal(): " + c1.getReal());
        System.out.println(" c1.getImg(): " + c1.getImg());

        System.out.println(" c1.div(2): " + c1.div(2));
        System.out.println(" c1.mult(c2): " + c1.mult(c2));
        //        c1.divMe(2);
        //        System.out.println(" c1.divMe(2): " + c1);
        c1.plusMe(c2);
        System.out.println(" c1.plusMe(c2): " + c1);
    }

    public static int getQuantity() {
        return quantity;
    }
}
