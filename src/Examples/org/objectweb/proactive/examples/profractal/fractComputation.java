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
package org.objectweb.proactive.examples.profractal;


/* fractComputation.java
 *
 * Created on Semptember 6 2003, 11.56
 */

/**
 * @author Daniele Di Felice
 */
import java.io.Serializable;


/**
 * This class perform the real computation by complex formula iteration
 */
public class fractComputation implements Serializable {
    private Complex ul;
    private Complex lr;
    private long step;
    private double zoom;

    /** Empty constructor */
    public fractComputation() {
    }

    /** Constructor
     * @param ul: the upper left complex number
     * @param lr: the lower right complex number
     * @param step: the iteration number
     * @param zoom: the zoom value
     * @return an instance of the fractComputation class
     */
    public fractComputation(Complex ul, Complex lr, long step, double zoom) {
        this.ul = ul;
        this.lr = lr;
        this.step = step;
        this.zoom = zoom;
    }

    /** Sets the Upper Left complex number of the region
     * @param ul: the upper left complex number
     * @return void
     */
    public void setUL(Complex ul) {
        this.ul = ul;
    }

    /** Sets the Lower Right complex number of the region
     * @param lr: the lower right complex number
     * @return void
     */
    public void setLR(Complex lr) {
        this.lr = lr;
    }

    /** Sets interation number
     * @param step: the iteration number
     * @return void
     */
    public void setStep(long step) {
        this.step = step;
    }

    /** Sets the zoom value
     * @param zoom: the zoom value
     * @return void
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    /** Gets the Upper Left complex number of the region
     * @param void
     * @return ul: the upper left complex number
     */
    public Complex getUL() {
        return this.ul;
    }

    /** Gets the Lower Right complex number of the region
     * @param void
     * @return lr: the lower right complex number
     */
    public Complex getLR() {
        return this.lr;
    }

    /** Gets the interation number
     * @param void
     * @return step: the iteration number
     */
    public long getStep() {
        return this.step;
    }

    /** Gets the zoom value
     * @param void
     * @return zoom: the zoom value
     */
    public double getZoom() {
        return this.zoom;
    }

    /** Perform the iterations on the specified region
     * @param void
     * @return an instance of fractResult: computation's results
     */
    public fractResult Compute() {
        // Check if the difference are divisible by zoom
        double column = ul.real() - lr.real();
        double row = ul.imag() - lr.imag();
        System.out.println("Checking parameters...");
        Double checkR = new Double(row / zoom);
        checkR = new Double(checkR.doubleValue() - checkR.intValue());
        Double checkC = new Double(column / zoom);
        checkC = new Double(checkC.doubleValue() - checkC.intValue());
        if (!checkR.equals(new Double(0.0)) || !checkC.equals(new Double(0.0)) ||
                (ul.real() > lr.real()) || (ul.imag() < lr.imag())) {
            System.err.println("Error: wrong parameters!");
            System.exit(1);
        }
        System.out.println("Parameters ok...");
        row = Math.abs(row / zoom);
        column = Math.abs(column / zoom);
        Complex point = new Complex();

        // Grid generation
        boolean[][] results = new boolean[(int) row][(int) column];
        point = ul;
        // For all number of the grid
        for (int i = 0; i < row; i++) {
            // Substract to the imaginary part
            point = new Complex(ul.real(), point.imag() - zoom);
            for (int j = 0; j < column; j++) {
                // Adding to the real part
                point = new Complex(point.real() + zoom, point.imag());
                Complex tmp = new Complex();

                // Execute step iterations
                // Debug information
                //System.out.print("Point: " + (new Double(point.real())).toString() + " " + (new Double(point.imag())).toString());
                for (int k = 0; k < step; k++) {
                    tmp = Complex.plus(Complex.times(tmp, tmp), point);
                }
                Double app1 = new Double(tmp.real());
                Double app2 = new Double(tmp.imag());
                if (app1.isInfinite() || app2.isInfinite()) {
                    // Debug information
                    //System.out.print(" ---> Infinity");
                } else {
                    // Debug information
                    //System.out.print(" --> " + (new Double(tmp.real())).toString() + " " + (new Double(tmp.imag())).toString());
                    // Generate the result
                    Complex diff = Complex.minus(tmp, new Complex());
                    results[i][j] = (Math.sqrt((diff.real() * diff.real()) +
                            (diff.imag() * diff.imag())) <= 1.0);
                }

                // Debug information
                //System.out.print(".");
                //System.out.println("");
            }
        }

        // Returns the results
        return (new fractResult(results));
    }
}
