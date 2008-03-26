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
package org.objectweb.proactive.loadbalancing.util;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;


/**
 * Worker contains a bloc of the global matrix
 * @author The ProActive Team
 **/
public class JacobiWorker implements java.io.Serializable {
    // ORIENTATION
    static final int UP = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;
    static final int RIGHT = 4;

    // ID
    private int id;

    //synchro
    private int iteration;
    private int maxIter;
    private int nbGetBoundRcvd;
    private int nbEndIterRcvd;
    private boolean hasStarted;
    private boolean alreadyRcvBound;

    // neigbohroud
    private JacobiWorker up;
    private JacobiWorker down;
    private JacobiWorker left;
    private JacobiWorker right;
    private int nbNeighbours;

    // current virtual boudaries
    private double[] vbUp;
    private double[] vbDown;
    private double[] vbLeft;
    private double[] vbRight;

    // upper left x y
    private int upperLeftX;
    private int upperLeftY;

    // square size
    private int subMatrixSize;

    // global matrix size
    private int globalMatrixSize;

    // boundaries value
    private double boundaryValue;

    //sub matrix
    private double[][] subMatrix;
    private double[][] subTemp;

    // Profiling
    private long startTime;
    private long elapsedTime;

    //////
    // CONSTRUCTORS
    //////
    public JacobiWorker() {
        //void
    }

    public JacobiWorker(Integer id) {
        this.id = id.intValue();
        this.boundaryValue = 0;
        this.iteration = 0;
        this.nbGetBoundRcvd = 0;
        this.maxIter = 100;
        this.startTime = 0;
        this.elapsedTime = 0;
        this.nbEndIterRcvd = 0;
        this.alreadyRcvBound = false;
        this.hasStarted = false;
    }

    public JacobiWorker(Integer id, Double boundaryValue, Integer maxIter) {
        this.id = id.intValue();
        this.boundaryValue = boundaryValue.doubleValue();
        this.iteration = 0;
        this.nbGetBoundRcvd = 0;
        this.maxIter = maxIter.intValue();
        this.startTime = 0;
        this.elapsedTime = 0;
        this.nbEndIterRcvd = 0;
        this.alreadyRcvBound = false;
        this.hasStarted = false;
    }

    //////
    // INITIALIZATION
    //////
    public int setSubMatrix(int globalMatrixSize, int subMatrixSize, int upperLeftX, int upperLeftY,
            double[][] subMatrix) {
        this.globalMatrixSize = globalMatrixSize;
        this.subMatrixSize = subMatrixSize;
        this.upperLeftX = upperLeftX;
        this.upperLeftY = upperLeftY;
        this.subMatrix = subMatrix;
        this.subTemp = new double[subMatrixSize][subMatrixSize];
        System.out.println("[JACOBI] worker " + id + " : submatrix intialized");
        //this.displayMatrix(true);
        return 0;
    }

    public int setNeighbours(JacobiWorker up, JacobiWorker down, JacobiWorker left, JacobiWorker right) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.nbNeighbours = 0;
        this.nbNeighbours += ((up == null) ? 0 : 1);
        this.nbNeighbours += ((down == null) ? 0 : 1);
        this.nbNeighbours += ((left == null) ? 0 : 1);
        this.nbNeighbours += ((right == null) ? 0 : 1);
        System.out.println("[JACOBI] worker " + id + " : neighboroud initialized (" + this.nbNeighbours +
            " neighbours)");
        return 0;
    }

    //////
    // COMPUTATION
    //////

    /**
     * One iteration of jacobi process
     */
    public void computeNewSubMatrix() {
        //TIMER
        if (this.startTime == 0) {
            this.startTime = System.currentTimeMillis();
        }

        // send border values to neighbors 
        this.sendBoundaries();

        // compute INSIDE the submatrix
        for (int line = this.upperLeftY + 1; line < ((this.upperLeftY + this.subMatrixSize) - 1); line++) {
            this.computeInsideLine(line);
        }

        // the first iteration
        if (!this.hasStarted) {
            this.hasStarted = true;
            if (this.alreadyRcvBound) {
                // finish computation
                this.computeBorderLine();
                this.updateMatrix();
                this.iteration++;
                this.nbGetBoundRcvd = 0;
                //send ack to neighbors
                this.sendEndIter();
            }
        }
    }

    /**
     * Compute variation for one line
     * Use to refine granularity of the computation so as to serve get request
     * @param line global matrix
     */
    public void computeInsideLine(int line) {
        int i;
        for (i = this.upperLeftX + 1; i < ((this.upperLeftX + this.subMatrixSize) - 1); i++) {
            this.subTemp[i - this.upperLeftX][line - this.upperLeftY] = (this.getLeftValue(i, line) +
                this.getRightValue(i, line) + this.getUpperValue(i, line) + this.getDownerValue(i, line)) * 0.25;
        }
    }

    /**
     * Compute varaition for the border value of subMatrix
     */
    public void computeBorderLine() {
        //	compute first and last line
        for (int i = this.upperLeftX; i < ((this.subMatrixSize + this.upperLeftX) - 1); i++) {
            this.computeOnePoint(i, this.upperLeftY);
            this.computeOnePoint(i, (this.subMatrixSize + this.upperLeftY) - 1);
        }

        // compute fisrt and last column
        for (int j = this.upperLeftY; j < (this.upperLeftY + this.subMatrixSize); j++) {
            this.computeOnePoint(this.upperLeftX, j);
            this.computeOnePoint((this.upperLeftX + this.subMatrixSize) - 1, j);
        }
    }

    /**
     * perform the computation for the point (i,j)
     * i and j in the global matrix
     * @param i
     * @param j
     */
    private void computeOnePoint(int i, int j) {
        this.subTemp[i - this.upperLeftX][j - this.upperLeftY] = (this.getLeftValue(i, j) +
            this.getRightValue(i, j) + this.getUpperValue(i, j) + this.getDownerValue(i, j)) * 0.25;
    }

    /**
     * Switch temp and matrix
     */
    private void updateMatrix() {
        // swap matrix to avoid usless creation
        double[][] tmp = this.subMatrix;
        this.subMatrix = this.subTemp;
        this.subTemp = tmp;
    }

    //virtual accessor for neighbour values
    // x an y in the global matrix
    private double getUpperValue(int x, int y) {
        if (y == 0) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (y == this.upperLeftY) {
            return this.vbUp[x - this.upperLeftX];
        } else {
            return subMatrix[x - this.upperLeftX][y - 1 - this.upperLeftY];
        }
    }

    private double getDownerValue(int x, int y) {
        if (y == (this.globalMatrixSize - 1)) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (y == ((this.upperLeftY + this.subMatrixSize) - 1)) {
            return this.vbDown[x - this.upperLeftX];
        } else {
            return subMatrix[x - this.upperLeftX][(y + 1) - this.upperLeftY];
        }
    }

    private double getLeftValue(int x, int y) {
        if (x == 0) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (x == this.upperLeftX) {
            return this.vbLeft[y - this.upperLeftY];
        } else {
            return subMatrix[x - 1 - this.upperLeftX][y - this.upperLeftY];
        }
    }

    private double getRightValue(int x, int y) {
        if (x == (this.globalMatrixSize - 1)) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (x == ((this.upperLeftX + this.subMatrixSize) - 1)) {
            return this.vbRight[y - this.upperLeftY];
        } else {
            return subMatrix[(x + 1) - this.upperLeftX][y - this.upperLeftY];
        }
    }

    /////
    // SYNCHRONIZATION
    /////

    /**
     * return the border line oriented which.value
     * @param which
     * @return
     */
    public void sendBoundaries() {
        if (this.up != null) {
            double[] upline = new double[this.subMatrixSize];
            for (int i = 0; i < this.subMatrixSize; i++) {
                upline[i] = this.subMatrix[i][0];
            }
            this.up.receiveBoundary(JacobiWorker.DOWN, upline, this.iteration);
        }
        if (this.down != null) {
            double[] downline = new double[this.subMatrixSize];
            for (int i = 0; i < this.subMatrixSize; i++) {
                downline[i] = this.subMatrix[i][this.subMatrixSize - 1];
            }
            this.down.receiveBoundary(JacobiWorker.UP, downline, this.iteration);
        }
        if (this.left != null) {
            this.left.receiveBoundary(JacobiWorker.RIGHT, this.subMatrix[0], this.iteration);
        }
        if (this.right != null) {
            this.right.receiveBoundary(JacobiWorker.LEFT, this.subMatrix[this.subMatrixSize - 1],
                    this.iteration);
        }
    }

    public void endIter(int iter) {
        //        System.out.println("[JACOBI] Worker " + this.id +  " receives endIter " + iter);
        this.nbEndIterRcvd++;
        if (this.nbEndIterRcvd == this.nbNeighbours) {
            if (this.iteration == this.maxIter) {
                // end of the computation
                this.elapsedTime = System.currentTimeMillis() - this.startTime;
                System.out.println("[JACOBI] Worker " + this.id + " : computation ended after " +
                    this.iteration + "(" + this.subMatrix[10][10] + ").");
                System.out.println("[JACOBI] Time elapsed for Worker " + this.id + " : " + this.elapsedTime);
                return;
            } else {
                // iter again
                this.nbEndIterRcvd = 0;
                this.computeNewSubMatrix();
            }
        }
    }

    public void receiveBoundary(int fromWho, double[] boundary, int iter) {
        //        System.out.println("[JACOBI] Worker " + this.id +  " receives boundary " + iter + " from " + fromWho);
        this.nbGetBoundRcvd++;
        switch (fromWho) {
            case JacobiWorker.UP:
                this.vbUp = boundary;
                break;
            case JacobiWorker.DOWN:
                this.vbDown = boundary;
                break;
            case JacobiWorker.LEFT:
                this.vbLeft = boundary;
                break;
            case JacobiWorker.RIGHT:
                this.vbRight = boundary;
                break;
        }
        UniqueID idpa = PAActiveObject.getBodyOnThis().getID();

        //System.out.println(idpa + " Worker " + id + " has received " + this.nbGetBoundRcvd);
        if (this.nbGetBoundRcvd == this.nbNeighbours) {
            if (!this.hasStarted) {
                this.alreadyRcvBound = true;
            } else {
                if (iter != this.iteration) {
                    System.err.println(PAActiveObject.getBodyOnThis().getID() + " ITER ERROR : local = " +
                        this.iteration + " ; remote = " + iter + "\n\n\n\n\n\n");
                    try {
                        Thread.sleep(100000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // finish computation
                this.computeBorderLine();
                //System.out.print(""+iter+"-");
                //this.displayMatrix(true);
                this.updateMatrix();
                this.iteration++;
                if ((iteration % 50) == 0) {
                    System.out.println("Worker " + id + " : " + iteration + " (" + this.subMatrix[10][10] +
                        ") in " + (System.currentTimeMillis() - this.startTime) + " ms");
                }
                this.nbGetBoundRcvd = 0;
                //send ack to neighbors
                this.sendEndIter();
            }
        }
    }

    private void sendEndIter() {
        UniqueID idpa = PAActiveObject.getBodyOnThis().getID();

        //System.out.println(idpa + " Worker " + id + " send enIter " + this.iteration);
        //send ack to neighbors
        if (this.up != null) {
            this.up.endIter(this.iteration);
        }
        if (this.down != null) {
            this.down.endIter(this.iteration);
        }
        if (this.left != null) {
            this.left.endIter(this.iteration);
        }
        if (this.right != null) {
            this.right.endIter(this.iteration);
        }
    }

    /**
     * Dislpay
     * @author The ProActive Team
     */
    private void displayMatrix(boolean inDouble) {
        System.out.println("[JACOBI] Submatrix for worker " + this.id);
        if (inDouble) {
            for (int i = 0; i < this.subMatrixSize; i++) {
                for (int j = 0; j < this.subMatrixSize; j++) {
                    System.out.print(" " + this.subMatrix[i][j]);
                }
                System.out.println("");
            }
        } else {
            for (int i = 0; i < this.subMatrixSize; i++) {
                for (int j = 0; j < this.subMatrixSize; j++) {
                    System.out.print(" " + (int) (this.subMatrix[i][j]));
                }
                System.out.println("");
            }
        }
    }

    /**
     * Display the fisrt line
     */
    private void displayLine(int line) {
        System.out.println("[JACOBI] Line " + line + " for worker " + this.id);
        for (int j = 0; j < this.subMatrixSize; j++) {
            System.out.print(" " + this.subMatrix[line][j]);
        }
        System.out.println("");
    }

    /**
     * Debug
     * @author The ProActive Team
     */
    private void printDebug() {
        System.out.println("[JACOBI] Worker " + id + " : ");
        System.out.println("         * upperLeftX   = " + this.upperLeftX);
        System.out.println("         * upperLeftY   = " + this.upperLeftY);
        System.out.println("         * nbNeighbours = " + this.nbNeighbours);
    }

    @Override
    public String toString() {
        return " Worker " + id + " at iter " + this.iteration;
    }
}
