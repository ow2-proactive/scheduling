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
package org.objectweb.proactive.examples.jacobi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.group.topology.Plan;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class SubMatrix implements Serializable {

    /** Default width value of a submatrix */
    private static final int DEFAULT_WIDTH = 100;

    /** Default height value of a submatrix */
    private static final int DEFAULT_HEIGHT = 50;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** Step */
    private int iterationsToStop = Jacobi.ITERATIONS;

    /** Min Diff of the loop */
    private double minDiff = Jacobi.MINDIFF + 1;

    /** the name of the submatrix */
    private String name;

    /** Width of the submatrix */
    private int width;

    /** Height of the submatrix */
    private int height;

    /** the current values */
    private double[] current;

    /** the old values */
    private double[] old;

    /** North submatrix neighbor */
    private SubMatrix north;

    /** South submatrix neighbor */
    private SubMatrix south;

    /** West submatrix neighbor */
    private SubMatrix west;

    /** East submatrix neighbor */
    private SubMatrix east;

    /** A ProActive reference to this */
    private SubMatrix asyncRefToMe;

    /** The neighbors submatix */
    private SubMatrix neighbors;

    /** The whole matrix */
    private SubMatrix matrix;

    /** The resultFile * */
    private File resultFile;

    /** A reference to the main program **/
    private Jacobi jacobi;

    /** Border of the north submatrix neighbor */
    private double[] northNeighborBorder;

    /** Border of the south submatrix neighbor */
    private double[] southNeighborBorder;

    /** Border of the west submatrix neighbor */
    private double[] westNeighborBorder;

    /** Border of the east submatrix neighbor */
    private double[] eastNeighborBorder;

    /** Empty constructor */
    public SubMatrix() {
        this(SubMatrix.DEFAULT_WIDTH, SubMatrix.DEFAULT_HEIGHT);
    }

    public SubMatrix(String name, File resultFile, Jacobi jacobi) {
        this();
        this.name = name;
        this.resultFile = resultFile;
        this.jacobi = jacobi;
    }

    /**
     * Constructor
     *
     * @param x
     *            the width of the submatrix
     * @param y
     *            the height of the matrix
     */
    public SubMatrix(int x, int y) {
        this.width = x;
        this.height = y;
        this.current = new double[x * y];
        this.old = new double[x * y];
        for (int i = 0; i < this.old.length; i++) {
            this.old[i] = Math.random() * 10000;
        }
    }

    /**
     * Returns the value at the specified position
     *
     * @param x
     *            the column of the value
     * @param y
     *            the line of the value
     * @return the value at the specified position
     */
    public double get(int x, int y) {
        return this.old[(x * this.width) + y];
    }

    /**
     * Builds the central part of the new submatrix that does not require
     * communication with other submatrix
     */
    public void internalCompute() {
        int index = this.width + 1;
        double current;
        double diff = Jacobi.MINDIFF + 1;
        double west;
        double center;
        double east;
        for (int y = 1; y < (this.height - 1); y++) {
            for (int x = 1; x < (this.width - 1); x++) {
                center = old[index];
                west = old[index - 1];
                east = old[index + 1];
                current = (west + east + this.old[index - this.width] +
                    this.old[index + this.width]) / 4;
                this.current[index] = current;
                diff = Math.abs(current - center);
                if (diff < this.minDiff) {
                    this.minDiff = diff;
                }

                index++;
            }
            index += 2;
        }
    }

    /**
     * Builds the border part of the new submatrix that require the
     * communications with other submatrix are done.
     */
    public void borderCompute() {
        int index;
        double current;
        double diff = Jacobi.MINDIFF + 1;

        // north-west corner
        index = 0;
        current = (this.northNeighborBorder[0] + this.old[index + this.width] +
            this.westNeighborBorder[0] + this.old[index + 1]) / 4;
        this.current[index] = current;
        diff = Math.abs(current - this.old[index]);
        if (diff < this.minDiff) {
            this.minDiff = diff;
        }

        // north-east corner
        index = this.width - 1;
        current = (this.northNeighborBorder[this.width - 1] +
            this.old[index + this.width] + this.old[index - 1] +
            this.eastNeighborBorder[0]) / 4;
        this.current[index] = current;
        diff = Math.abs(current - this.old[index]);
        if (diff < this.minDiff) {
            this.minDiff = diff;
        }

        // north border
        for (index = 1; index < (this.width - 1); index++) {
            current = (this.northNeighborBorder[index] +
                this.old[this.width + index] + this.old[index - 1] +
                this.old[index + 1]) / 4;
            this.current[index] = current;
            diff = Math.abs(current - this.old[index]);
            if (diff < this.minDiff) {
                this.minDiff = diff;
            }
        }

        // south-west corner
        index = (this.width - 1) * this.height;
        current = (this.old[index - this.width] + this.southNeighborBorder[0] +
            this.westNeighborBorder[this.height - 1] + this.old[index + 1]) / 4;
        this.current[index] = current;
        diff = Math.abs(current - this.old[index]);
        if (diff < this.minDiff) {
            this.minDiff = diff;
        }

        // west border
        index = this.width;
        for (int i = 1; i < (this.height - 1); i++) {
            current = (this.old[index - this.width] +
                this.old[index + this.width] + this.westNeighborBorder[i] +
                this.old[index + 1]) / 4;
            this.current[index] = current;
            diff = Math.abs(current - this.old[index]);
            if (diff < this.minDiff) {
                this.minDiff = diff;
            }
            index += this.width;
        }

        // south-east corner
        index = (this.width * this.height) - 1;
        current = (this.old[index - this.width] +
            this.southNeighborBorder[this.width - 1] + this.old[index - 1] +
            this.eastNeighborBorder[this.height - 1]) / 4;
        this.current[index] = current;
        diff = Math.abs(current - this.old[index]);
        if (diff < this.minDiff) {
            this.minDiff = diff;
        }

        // south border
        index = (this.width * (this.height - 1)) + 1;
        for (int i = 1; i < (this.width - 1); i++) {
            current = (this.old[index - this.width] +
                this.southNeighborBorder[i] + this.old[index - 1] +
                this.old[index + 1]) / 4;
            this.current[index] = current;
            diff = Math.abs(current - this.old[index]);
            if (diff < this.minDiff) {
                this.minDiff = diff;
            }
            index++;
        }

        // east border
        index = (this.width * 2) - 1;
        for (int i = 1; i < (this.height - 1); i++) {
            current = (this.old[index - this.width] +
                this.old[index + this.width] + this.old[index - 1] +
                this.eastNeighborBorder[i]) / 4;
            this.current[index] = current;
            diff = Math.abs(current - this.old[index]);
            if (diff < this.minDiff) {
                this.minDiff = diff;
            }
            index += this.width;
        }
    }

    /**
     * Exchange the old values with the new
     */
    public void exchange() {
        double[] tmp = this.current;
        this.current = this.old;
        this.old = tmp;
    }

    /**
     * Connects this submatrix with its neighbors and send them its borders
     */
    public void buildNeighborhood() {
        this.matrix = (SubMatrix) ProSPMD.getSPMDGroup();
        Group allSubMatrix = PAGroup.getGroup(this.matrix);
        Plan topology = null;
        try {
            topology = new Plan(allSubMatrix, Jacobi.HEIGHT, Jacobi.WIDTH);
        } catch (ConstructionOfReifiedObjectFailedException e) {
            logger.error(
                "[JACOBI] ** ConstructionOfReifiedObjectFailedException ** - Unable to build the plan topology");
            e.printStackTrace();
        }

        this.asyncRefToMe = (SubMatrix) PAActiveObject.getStubOnThis();
        this.north = (SubMatrix) topology.up(this.asyncRefToMe);
        this.south = (SubMatrix) topology.down(this.asyncRefToMe);
        this.west = (SubMatrix) topology.left(this.asyncRefToMe);
        this.east = (SubMatrix) topology.right(this.asyncRefToMe);

        try {
            this.neighbors = (SubMatrix) PAGroup.newGroup(SubMatrix.class.getName());
        } catch (ClassNotReifiableException e) {
            logger.error(
                "[JACOBI] ** ClassNotReifiableException ** - Unable to build the neighbors group");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.error(
                "[JACOBI] ** ClassNotFoundException ** - Unable to build the neighbors group");
            e.printStackTrace();
        }
        Group neighborsGroup = PAGroup.getGroup(this.neighbors);

        if (this.north == null) {
            this.northNeighborBorder = this.buildFakeBorder(this.width);
        } else {
            neighborsGroup.add(this.north);
            this.north.setSouthBorder(this.buildNorthBorder());
        }
        if (this.south == null) {
            this.southNeighborBorder = this.buildFakeBorder(this.width);
        } else {
            neighborsGroup.add(this.south);
            this.south.setNorthBorder(this.buildSouthBorder());
        }
        if (this.west == null) {
            this.westNeighborBorder = this.buildFakeBorder(this.height);
        } else {
            neighborsGroup.add(this.west);
            this.west.setEastBorder(this.buildWestBorder());
        }
        if (this.east == null) {
            this.eastNeighborBorder = this.buildFakeBorder(this.height);
        } else {
            neighborsGroup.add(this.east);
            this.east.setWestBorder(this.buildEastBorder());
        }
        neighborsGroup.add(this.asyncRefToMe);
    }

    /**
     * Builds a "fake border" for the external submatrix filled with
     * DEFAULT_BORDER_VALUE
     *
     * @param size -
     *            the size of the border
     * @return a "fake border" for the external submatrix
     */
    private double[] buildFakeBorder(int size) {
        double[] line = new double[size];
        for (int i = 0; i < line.length; i++) {
            line[i] = Jacobi.DEFAULT_BORDER_VALUE;
        }
        return line;
    }

    /**
     * Returns the north border of the submatrix
     *
     * @return the north border of the submatrix
     */
    private double[] buildNorthBorder() {
        double[] line = new double[this.width];
        for (int i = 0; i < this.width; i++) {
            line[i] = this.old[i];
        }
        return line;
    }

    /**
     * Returns the south border of the submatrix
     *
     * @return the south border of the submatrix
     */
    private double[] buildSouthBorder() {
        double[] line = new double[this.width];
        int index = this.width * (this.height - 1);
        for (int i = 0; i < this.width; i++) {
            line[i] = this.old[index++];
        }
        return line;
    }

    /**
     * Returns the west border of the submatrix
     *
     * @return the west border of the submatrix
     */
    private double[] buildWestBorder() {
        double[] line = new double[this.height];
        int index = 0;
        for (int i = 0; i < this.height; i++) {
            line[i] = this.old[index];
            index += this.width;
        }
        return line;
    }

    /**
     * Returns the west border of the submatrix
     *
     * @return the west border of the submatrix
     */
    private double[] buildEastBorder() {
        double[] line = new double[this.height];
        int index = this.width - 1;
        for (int i = 0; i < this.height; i++) {
            line[i] = this.old[index];
            index += this.width;
        }
        return line;
    }

    /**
     * Sends the border values to the corresponding neighbors
     */
    public void sendBordersToNeighbors() {
        if (this.north != null) {
            this.north.setSouthBorder(this.buildNorthBorder());
        }
        if (this.south != null) {
            this.south.setNorthBorder(this.buildSouthBorder());
        }
        if (this.west != null) {
            this.west.setEastBorder(this.buildWestBorder());
        }
        if (this.east != null) {
            this.east.setWestBorder(this.buildEastBorder());
        }
    }

    /**
     * Set the north border
     *
     * @param border -
     *            the north border
     */
    public void setNorthBorder(double[] border) {
        this.northNeighborBorder = border;
    }

    /**
     * Set the south border
     *
     * @param border -
     *            the south border
     */
    public void setSouthBorder(double[] border) {
        this.southNeighborBorder = border;
    }

    /**
     * Set the west border
     *
     * @param border -
     *            the west border
     */
    public void setWestBorder(double[] border) {
        this.westNeighborBorder = border;
    }

    /**
     * Set the east border
     *
     * @param border -
     *            the east border
     */
    public void setEastBorder(double[] border) {
        this.eastNeighborBorder = border;
    }

    /**
     * Launch the calculus
     */
    public void compute() {
        this.buildNeighborhood();
        ProSPMD.barrier("InitDone");

        this.asyncRefToMe.loop();
    }

    /**
     * Launch the main loop
     */
    public void loop() {
        logger.debug("[JACOBI] [" + this.name + "] Iteration : " +
            iterationsToStop);

        // compute the internal values
        this.internalCompute();
        // synchronization to be sure that all submatrix have exchanged borders
        ProSPMD.barrier("SynchronizationWithNeighbors" + this.iterationsToStop,
            this.neighbors);
        // compute the border values
        this.asyncRefToMe.borderCompute();
        // send the borders to neighbors
        this.asyncRefToMe.sendBordersToNeighbors();
        // decrement the iteration counter
        this.iterationsToStop--;
        // continue or stop ?
        if ((this.iterationsToStop > 0) && (this.minDiff > Jacobi.MINDIFF)) {
            this.asyncRefToMe.exchange();
            this.asyncRefToMe.loop();
        } else {
            logger.info("[JACOBI] [" + this.name +
                "] Computation over :\n      " + this.minDiff +
                " (asked less than " + Jacobi.MINDIFF + ")");
        }
        if (this.minDiff < Jacobi.MINDIFF) {
            System.out.println("[" + this.name + "] sent the \"end signal\"");
            this.matrix.stop();
        }
    }

    /**
     * Stops the submatrix
     */
    public void stop() {
        this.iterationsToStop = 0;
        logger.debug("[JACOBI] [" + this.name +
            "] Stop Message Received, waiting for others.");
        ProSPMD.barrier("stop");
        if ((north != null) && (east != null) && (west != null) &&
                (south != null)) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileOutputStream(resultFile), true);

                DecimalFormat decimalFormat = new DecimalFormat("0.00");

                for (int i = 0; i < height; i++) {
                    String line = "[";
                    for (int j = 0; j < (width - 1); j++) {
                        line += (decimalFormat.format(current[(i * width) + j]) +
                        " ");
                    }
                    line += decimalFormat.format(current[((i * width) + width) -
                        1]);
                    line += "] ";
                    pw.println(line);
                }
                logger.info("CALCULATION COMPLETE, plese find results in: " +
                    resultFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                pw.close();
            }
            // Send the terminate signal to the main program
            this.jacobi.terminateAll();
        }
    }
}
