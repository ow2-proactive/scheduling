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
package org.objectweb.proactive.examples.timit.jacobi;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;


/**
 * Worker contains a bloc of the global matrix
 *
 * @author cdelbe
 */
public class Worker extends Timed implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3829306560222826269L;
    public static final boolean COMMUNICATION_PATTERN_OBSERVING_MODE = true;

    /** ORIENTATION */
    static final int UP = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;
    static final int RIGHT = 4;

    /** The rank of this worker */
    private int id;

    /** Synchronisation */
    private int iteration;
    private int maxIter;
    private int nbGetBoundRcvd;
    private int nbEndIterRcvd;
    private boolean hasStarted;
    private boolean alreadyRcvBound;

    /** The neigborhood */
    private Worker up;
    private Worker down;
    private Worker left;
    private Worker right;
    private int nbNeighbours;

    /** Neigborhood ranks */
    private int up_id;
    private int down_id;
    private int left_id;
    private int right_id;

    /** Current virtual boudaries */
    private double[] vbUp;
    private double[] vbDown;
    private double[] vbLeft;
    private double[] vbRight;

    /** The upper left x and y */
    private int upperLeftX;
    private int upperLeftY;

    /** The sub matrix size */
    private int subMatrixSize;

    /** The global matrix size */
    private int globalMatrixSize;

    /** The boundaries value */
    private double boundaryValue;

    /** The sub matrix */
    private double[][] subMatrix;
    private double[][] subTemp;

    /** Local profiling */
    private long startTime;
    private long elapsedTime;

    /** Counters for the timing system. */
    public TimerCounter T_TOTAL = new TimerCounter("total");
    public TimerCounter T_WORK = new TimerCounter("work");
    public boolean isTotalStarted = false;

    /** The size of the group */
    private int groupSize;

    /** An communication observer for the number of message density distribution */
    public CommEventObserver nbCommObserver;

    /** An communication observer for the data density distribution */
    public CommEventObserver commSizeObserver;

    // ////
    // CONSTRUCTORS
    // ////
    public Worker() {
    }

    /**
     *
     * @param id
     */
    public Worker(Integer id) {
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

    /**
     *
     * @param id
     * @param boundaryValue
     * @param maxIter
     * @param nbWorker
     */
    public Worker(Integer id, Double boundaryValue, Integer maxIter,
        Integer nbWorker) {
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
        this.groupSize = nbWorker;

        // The nbCommObserver is used to build
        // the message density pattern, a chart will be built from
        // the gathered data.
        this.nbCommObserver = new CommEventObserver("communication pattern",
                this.groupSize, id);
        // The commSizeObserver comunication observer is used to build
        // the message density pattern, a chart will be built from
        // the gathered data.
        this.commSizeObserver = new CommEventObserver("data density pattern",
                this.groupSize, id);
    }

    // ////
    // INITIALIZATION
    // ////

    /**
     * Setter for the sub matrix.
     *
     * @param globalMatrixSize
     * @param subMatrixSize
     * @param upperLeftX
     * @param upperLeftY
     * @param subMatrix
     * @return
     */
    public void setSubMatrix(int globalMatrixSize, int subMatrixSize,
        int upperLeftX, int upperLeftY, double[][] subMatrix) {
        this.globalMatrixSize = globalMatrixSize;
        this.subMatrixSize = subMatrixSize;
        this.upperLeftX = upperLeftX;
        this.upperLeftY = upperLeftY;
        this.subMatrix = subMatrix.clone();
        this.subTemp = new double[subMatrixSize][subMatrixSize];
        System.out.println("[JACOBI] worker " + this.id +
            " : submatrix intialized on " +
            PAActiveObject.getBodyOnThis().getNodeURL());
    }

    /**
     * Method called by the Launcher to set the neighborhood.
     *
     * @param up
     *            The upper neighbour Worker.
     * @param down
     *            The down neighbour Worker.
     * @param left
     *            The left neighbour Worker.
     * @param right
     *            The right neighbour Worker.
     * @param up_id
     *            The upper neighbour rank.
     * @param down_id
     *            The down neighbour rank.
     * @param left_id
     *            The left neighbour Worker.
     * @param right_id
     *            The upper neighbour Worker.
     */
    public void setNeighbours(Worker up, Worker down, Worker left,
        Worker right, int up_id, int down_id, int left_id, int right_id) {
        System.out.println("");
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.nbNeighbours = 0;
        this.nbNeighbours += ((up == null) ? 0 : 1);
        this.nbNeighbours += ((down == null) ? 0 : 1);
        this.nbNeighbours += ((left == null) ? 0 : 1);
        this.nbNeighbours += ((right == null) ? 0 : 1);
        this.up_id = up_id;
        this.down_id = down_id;
        this.left_id = left_id;
        this.right_id = right_id;
        System.out.println("[JACOBI] worker " + this.id +
            " : neighboroud initialized (" + this.nbNeighbours +
            " neighbours)");

        super.activate(new TimerCounter[] { this.T_TOTAL, this.T_WORK },
            new EventObserver[] { this.nbCommObserver, this.commSizeObserver }); // IF NOT ACTIVATED THERE IS
                                                                                 // A NULLPOINTER IN THE
                                                                                 // GETTOTALIME !!!!!
    }

    // ////
    // COMPUTATION
    // ////

    /**
     * One iteration of jacobi process
     */
    public void computeNewSubMatrix() {
        // TIMER
        if (!this.isTotalStarted) {
            this.T_TOTAL.start();
            this.isTotalStarted = true;
        }

        if (this.startTime == 0) {
            this.startTime = System.currentTimeMillis();
        }

        // send border values to neighbors
        this.sendBoundaries();

        // compute INSIDE the submatrix
        this.T_WORK.start();
        for (int line = this.upperLeftY + 1;
                line < ((this.upperLeftY + this.subMatrixSize) - 1); line++) {
            this.computeInsideLine(line);
        }
        this.T_WORK.stop();

        // the first iteration
        if (!this.hasStarted) {
            this.hasStarted = true;
            if (this.alreadyRcvBound) {
                // finish computation
                this.computeBorderLine();
                this.updateMatrix();
                this.iteration++;
                this.nbGetBoundRcvd = 0;
                // send ack to neighbors
                this.sendEndIter();
            }
        }
    }

    /**
     * Compute variation for one line Use to refine granularity of the
     * computation so as to serve get request
     *
     * @param line
     *            global matrix
     */
    public void computeInsideLine(int line) {
        int i;
        for (i = this.upperLeftX + 1;
                i < ((this.upperLeftX + this.subMatrixSize) - 1); i++) {
            this.subTemp[i - this.upperLeftX][line - this.upperLeftY] = (this.getLeftValue(i,
                    line) + this.getRightValue(i, line) +
                this.getUpperValue(i, line) + this.getDownerValue(i, line)) * 0.25;
        }
    }

    /**
     * Compute varaition for the border value of subMatrix
     */
    public void computeBorderLine() {
        // compute first and last line
        for (int i = this.upperLeftX;
                i < ((this.subMatrixSize + this.upperLeftX) - 1); i++) {
            this.computeOnePoint(i, this.upperLeftY);
            this.computeOnePoint(i, (this.subMatrixSize + this.upperLeftY) - 1);
        }

        // compute fisrt and last column
        for (int j = this.upperLeftY;
                j < (this.upperLeftY + this.subMatrixSize); j++) {
            this.computeOnePoint(this.upperLeftX, j);
            this.computeOnePoint((this.upperLeftX + this.subMatrixSize) - 1, j);
        }
    }

    /**
     * perform the computation for the point (i,j) i and j in the global matrix
     *
     * @param i
     *            The line
     * @param j
     *            The column
     */
    private void computeOnePoint(int i, int j) {
        this.subTemp[i - this.upperLeftX][j - this.upperLeftY] = (this.getLeftValue(i,
                j) + this.getRightValue(i, j) + this.getUpperValue(i, j) +
            this.getDownerValue(i, j)) * 0.25;
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

    /**
     * virtual accessor for neighbour values x an y in the global matrix
     *
     * @param i
     *            The line
     * @param j
     *            The column
     */
    private double getUpperValue(int x, int y) {
        if (y == 0) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (y == this.upperLeftY) {
            return this.vbUp[x - this.upperLeftX];
        } else {
            return this.subMatrix[x - this.upperLeftX][y - 1 - this.upperLeftY];
        }
    }

    /**
     * virtual accessor for neighbour values
     *
     * @param i
     *            The line
     * @param j
     *            The column
     */
    private double getDownerValue(int x, int y) {
        if (y == (this.globalMatrixSize - 1)) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (y == ((this.upperLeftY + this.subMatrixSize) - 1)) {
            return this.vbDown[x - this.upperLeftX];
        } else {
            return this.subMatrix[x - this.upperLeftX][(y + 1) -
            this.upperLeftY];
        }
    }

    private double getLeftValue(int x, int y) {
        if (x == 0) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (x == this.upperLeftX) {
            return this.vbLeft[y - this.upperLeftY];
        } else {
            return this.subMatrix[x - 1 - this.upperLeftX][y - this.upperLeftY];
        }
    }

    private double getRightValue(int x, int y) {
        if (x == (this.globalMatrixSize - 1)) {
            // must return boudarie value
            return this.boundaryValue;
        } else if (x == ((this.upperLeftX + this.subMatrixSize) - 1)) {
            return this.vbRight[y - this.upperLeftY];
        } else {
            return this.subMatrix[(x + 1) - this.upperLeftX][y -
            this.upperLeftY];
        }
    }

    // ///
    // SYNCHRONIZATION
    // ///

    /**
     * Sends the boundary values to the neighbours.
     *
     */
    public void sendBoundaries() {
        if (this.up != null) {
            double[] upline = new double[this.subMatrixSize];
            for (int i = 0; i < this.subMatrixSize; i++) {
                upline[i] = this.subMatrix[i][0];
            }
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.up_id, TimIt.getObjectSize(upline) + 4);
            }
            this.up.receiveBoundary(Worker.DOWN, upline, this.iteration);
        }
        if (this.down != null) {
            double[] downline = new double[this.subMatrixSize];
            for (int i = 0; i < this.subMatrixSize; i++) {
                downline[i] = this.subMatrix[i][this.subMatrixSize - 1];
            }
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.down_id,
                    TimIt.getObjectSize(downline) + 4);
            }
            this.down.receiveBoundary(Worker.UP, downline, this.iteration);
        }
        if (this.left != null) {
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.left_id,
                    TimIt.getObjectSize(this.subMatrix[0]) + 4);
            }
            this.left.receiveBoundary(Worker.RIGHT, this.subMatrix[0],
                this.iteration);
        }
        if (this.right != null) {
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.right_id,
                    TimIt.getObjectSize(this.subMatrixSize - 1) + 4);
            }
            this.right.receiveBoundary(Worker.LEFT,
                this.subMatrix[this.subMatrixSize - 1], this.iteration);
        }
    }

    /**
     * The end of the iteration.
     *
     * @param iter
     */
    public void endIter(int iter) {
        this.nbEndIterRcvd++;
        if (this.nbEndIterRcvd == this.nbNeighbours) {
            if (this.iteration == this.maxIter) {
                // end of the computation
                this.elapsedTime = System.currentTimeMillis() - this.startTime;
                this.T_TOTAL.stop();
                super.finalizeTimed(this.id,
                    "Worker " + this.id + " has finished Wtime = " +
                    this.T_WORK.getTotalTime());
                System.out.println("[JACOBI] Worker " + this.id +
                    " : computation ended after " + this.iteration);
                System.out.println("[JACOBI] Time elapsed for Worker " +
                    this.id + " : " + this.elapsedTime);

                return;
            } else {
                // iter again
                this.nbEndIterRcvd = 0;
                this.computeNewSubMatrix();
            }
        }
    }

    /**
     * Method exchanges values between workers.
     *
     * @param fromWho
     *            The rank of the sender
     * @param boundary
     *            The boundary array
     * @param iter
     *            The iteration of the caller
     */
    public void receiveBoundary(int fromWho, double[] boundary, int iter) {
        this.nbGetBoundRcvd++;
        switch (fromWho) {
        case Worker.UP:
            this.vbUp = boundary;
            break;
        case Worker.DOWN:
            this.vbDown = boundary;
            break;
        case Worker.LEFT:
            this.vbLeft = boundary;
            break;
        case Worker.RIGHT:
            this.vbRight = boundary;
            break;
        }
        if (this.nbGetBoundRcvd == this.nbNeighbours) {
            if (!this.hasStarted) {
                this.alreadyRcvBound = true;
            } else {
                // finish computation
                this.computeBorderLine();
                this.updateMatrix();
                this.iteration++;
                if ((this.iteration % 500) == 0) {
                    System.out.println("Worker " + this.id + " : " +
                        this.iteration + " (" + this.subMatrix[0][0] + ") in " +
                        (System.currentTimeMillis() - this.startTime) + " ms");
                }
                this.nbGetBoundRcvd = 0;
                // send ack to neighbors
                this.sendEndIter();
            }
        }
    }

    /**
     * Sends ack to neighbors.
     */
    private void sendEndIter() {
        if (this.up != null) {
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.up_id, 4);
            }
            this.up.endIter(this.iteration);
        }
        if (this.down != null) {
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.down_id, 4);
            }
            this.down.endIter(this.iteration);
        }
        if (this.left != null) {
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.left_id, 4);
            }
            this.left.endIter(this.iteration);
        }
        if (this.right != null) {
            if (Worker.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                this.notifyOneRank(this.right_id, 4);
            }
            this.right.endIter(this.iteration);
        }
    }

    /**
     * Description of this worker.
     *
     * @return The description of this worker
     */
    @Override
    public String toString() {
        return " Worker " + this.id + " at iter " + this.iteration;
    }

    /**
     * Called by Launcher to kill this active object
     *
     * @see org.objectweb.proactive.benchmarks.timit.examples.example2.Launcher
     */
    public void terminate() {
        PAActiveObject.terminateActiveObject(true);
    }

    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    // METHODS USED TO OBSERVE NB COMMS AND COMM SIZE
    // //////////////////////////////////////////////////////////////////////////
    private void notifyOneRank(int destRank, int messageSize) {
        // Notification of 1 communication with the dest rank
        super.getEventObservable().setChanged();
        super.getEventObservable()
             .notifyObservers(new CommEvent(this.nbCommObserver, destRank, 1));

        // Notification
        super.getEventObservable().setChanged();
        super.getEventObservable()
             .notifyObservers(new CommEvent(this.commSizeObserver, destRank,
                messageSize));
    }
}
