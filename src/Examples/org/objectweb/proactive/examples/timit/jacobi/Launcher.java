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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.Startable;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 */
public class Launcher implements Startable {
    static final public double boudaryValue = 0;
    static final public double initialValue = 1000000000;

    // for killing
    private GCMApplication pad;
    private Worker[] workers;

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Startable s = new Launcher();
        s.start(args);
    }

    /**
     *
     * @param args
     */
    public void start(String[] args) {
        // cmd : java Launcher globalSize nbWorker maxIter
        System.out.println(" *** JACOBI ODD-EVEN SYNHCRONIZATION **** ");

        TimItManager tManager = null;
        if (args.length < 4) {
            System.err.println("java Launcher deploymentDesc globalSize nbWorker maxIter [server url]");
            System.exit(1);
        }

        String desc = args[0];
        int globalSize = Integer.parseInt(args[1]);
        int nbWorker = Integer.parseInt(args[2]);
        int maxIter = Integer.parseInt(args[3]);
        if (args.length > 4) {
        }
        int workerGridSize = (int) Math.sqrt((nbWorker));
        int submatrixSize = globalSize / workerGridSize;
        this.workers = new Worker[nbWorker];

        System.out.println("[JACOBI] Initialization with :");
        System.out.println("         * global matrix size = " + globalSize);
        System.out.println("         * sub matrix size    = " + submatrixSize);
        System.out.println("         * # of workers       = " + nbWorker);
        System.out.println("         * worker grid size   = " + workerGridSize);
        System.out.println("         * # of iterations    = " + maxIter);
        System.out.println("         * boundary value     = " + Launcher.boudaryValue);

        List<Node> nodeSet = null;

        try {
            GCMVirtualNode vnode;

            // create nodes
            this.pad = PAGCMDeployment.loadApplicationDescriptor(new File(desc));
            this.pad.startDeployment();
            vnode = this.pad.getVirtualNode("Workers");
            vnode.waitReady();

            nodeSet = vnode.getCurrentNodes();
            Node[] nodes = nodeSet.toArray(new Node[] {});

            // create workers
            for (int i = 0; i < nbWorker; i++) {
                this.workers[i] = (Worker) (PAActiveObject.newActive(Worker.class.getName(), new Object[] {
                        new Integer(i), new Double(Launcher.boudaryValue), new Integer(maxIter),
                        new Integer(nbWorker) }, ((nodes.length == (nbWorker - 1)) ? nodes[i] : nodes[0])));
            }
            System.out.println("[JACOBI] Workers are deployed");

            // You must create a TimerManager instance and give to it
            // typed group of Timed workers
            tManager = TimItManager.getInstance();
            tManager.setTimedObjects(this.workers);

            BenchmarkStatistics bstats = tManager.getBenchmarkStatistics();
            System.out.println(bstats);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        // initializing workers
        this.createVirtualGrid(workerGridSize);
        this.createAndSplitMatrix(workerGridSize, submatrixSize, globalSize);

        // start computation
        for (int i = 0; i < nbWorker; i++) {
            this.workers[i].computeNewSubMatrix();
        }

        // ... and show the results.
        // Note that this method will wait for results (ie all Timed object
        // returned their data to the TimIt Reductor
        System.out.println(tManager.getBenchmarkStatistics());

        System.out.println("[JACOBI] end of main thread.");
    }

    /**
     * Creates the virtual grid used as mapping.
     *
     * @param workerGridSize
     *            The size of the grid
     */
    public void createVirtualGrid(int workerGridSize) {
        System.out.println("[JACOBI] Creating workers virtual grid... ");
        int nbWorker = this.workers.length;
        int up_id;
        int down_id;
        int left_id;
        int right_id = -1;
        for (int i = 0; i < nbWorker; i++) {
            System.out.println("[JACOBI] Initializing worker " + i + "...");
            Worker currentWorker = this.workers[i];
            Worker up;
            Worker down;
            Worker left;
            Worker right;
            if (i == 0) { // upper left
                left = null;
                left_id = -1;
                up = null;
                up_id = -1;
                right = this.workers[1];
                right_id = 1;
                down = this.workers[workerGridSize];
                down_id = workerGridSize;
            } else if (i == (workerGridSize - 1)) { // upper right
                up = null;
                up_id = -1;
                right = null;
                right_id = -1;
                left = this.workers[i - 1];
                left_id = i - 1;
                down = this.workers[i + workerGridSize];
                down_id = i + workerGridSize;
            } else if (i == (nbWorker - workerGridSize)) { // lower left
                left = null;
                left_id = -1;
                down = null;
                down_id = -1;
                up = this.workers[i - workerGridSize];
                up_id = i - workerGridSize;
                right = this.workers[i + 1];
                right_id = i + 1;
            } else if (i == (nbWorker - 1)) { // lower right
                down = null;
                down_id = -1;
                right = null;
                right_id = -1;
                up = this.workers[i - workerGridSize];
                up_id = i - workerGridSize;
                left = this.workers[i - 1];
                left_id = i - 1;
            } else if ((i > 0) && (i < (workerGridSize - 1))) { // up
                up = null;
                up_id = -1;
                right = this.workers[i + 1];
                right_id = i + 1;
                left = this.workers[i - 1];
                left_id = i - 1;
                down = this.workers[i + workerGridSize];
                down_id = i + workerGridSize;
            } else if ((i > (workerGridSize * (workerGridSize - 1))) &&
                (i < ((workerGridSize * workerGridSize) - 1))) { // down
                up = this.workers[i - workerGridSize];
                up_id = i - workerGridSize;
                right = this.workers[i + 1];
                right_id = i + 1;
                left = this.workers[i - 1];
                left_id = i - 1;
                down = null;
                down_id = -1;
            } else if ((i % workerGridSize) == 0) { // left
                left = null;
                left_id = -1;
                right = this.workers[i + 1];
                right_id = i + 1;
                up = this.workers[i - workerGridSize];
                up_id = i - workerGridSize;
                down = this.workers[i + workerGridSize];
                down_id = i + workerGridSize;
            } else if ((i % workerGridSize) == (workerGridSize - 1)) { // right
                right = null;
                right_id = -1;
                left = this.workers[i - 1];
                left_id = i - 1;
                down = this.workers[i + workerGridSize];
                down_id = i + workerGridSize;
                up = this.workers[i - workerGridSize];
                up_id = i - workerGridSize;
            } else { // inside
                up = this.workers[i - workerGridSize];
                up_id = i - workerGridSize;
                down = this.workers[i + workerGridSize];
                down_id = i + workerGridSize;
                left = this.workers[i - 1];
                left_id = i - 1;
                right = this.workers[i + 1];
                right_id = i + 1;
            }
            currentWorker.setNeighbours(up, down, left, right, up_id, down_id, left_id, right_id);
        }
        System.out.println("[JACOBI] Virtual grid is created.");
    }

    /**
     * Creates and splits the matrix.
     *
     * @param workerGridSize
     *            The size of the grid matrix.
     * @param subMatrixSize
     *            The size of the sub matrix.
     * @param globalMatrixSize
     *            The size of the global matrix.
     */
    public void createAndSplitMatrix(int workerGridSize, int subMatrixSize, int globalMatrixSize) {
        System.out.println("[JACOBI] Creating and spliting global matrix");
        for (int currentW = 0; currentW < this.workers.length; currentW++) {
            // create submatrix
            double[][] currentSubMatrix = new double[subMatrixSize][subMatrixSize];
            for (int i = 0; i < subMatrixSize; i++) {
                for (int j = 0; j < subMatrixSize; j++) {
                    // **** HERE MATRIX VALUES ****
                    currentSubMatrix[i][j] = Launcher.initialValue;
                }
            }

            // compute upperLeft values
            int upperLeftX = (currentW % workerGridSize) * subMatrixSize;
            int upperLeftY = (currentW / workerGridSize) * subMatrixSize;

            // send submatrix to worker
            this.workers[currentW].setSubMatrix(globalMatrixSize, subMatrixSize, upperLeftX, upperLeftY,
                    currentSubMatrix);
        }
        System.out.println("[JACOBI]Global Matrix created and splitted");
    }

    /**
     * Part of the Startable implementation. TimIt will invoke this method
     * between each run.
     *
     * @see org.objectweb.proactive.benchmarks.timit.util.Startable
     */
    public void kill() {
        for (Worker element : this.workers) {
            element.terminate();
        }
    }

    /**
     * Part of the Startable implementation. TimIt will invoke this method
     * between each benchmark.
     *
     * @see org.objectweb.proactive.benchmarks.timit.util.Startable
     */
    public void masterKill() {
        this.pad.kill();
    }
}
