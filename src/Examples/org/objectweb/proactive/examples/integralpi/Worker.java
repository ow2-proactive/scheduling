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
package org.objectweb.proactive.examples.integralpi;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;


/**
 * A simple distributed application that calulates Pi.<br>
 * The application have two classes : Launcher, Worker<br>
 * Launcher will deploy some Workers to do a job.
 *
 * See Launcher for more details.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class Worker implements Serializable {

    /** The number that identifies the worker in a group */
    private int rank;

    /** The size of the current group of workers */
    private int groupSize;

    /** The array of stubs to all workers of the group */
    private Worker[] workersArray;

    /** The body of this worker **/
    private Body body;

    /** The number of global iterations */
    private long N;

    /** The value that will be updated remotely */
    private double x;

    /** An empty no args constructor, as needed by ProActive */
    public Worker() {
    }

    /**
     * This method is called by the Launcher to start the job.
     *
     * @param numOfIterations The number of iterations.
     * @return boolean A reifiable type to get the result.
     * @see org.objectweb.proactive.examples.integralpi.Launcher
     */
    public DoubleWrapper start(long numOfIterations) {
        N = numOfIterations;

        // ProActive initialization
        rank = PASPMD.getMyRank();
        groupSize = PASPMD.getMySPMDGroupSize();
        workersArray = (Worker[]) PAGroup.getGroup(PASPMD.getSPMDGroup()).toArray(new Worker[0]);
        body = PAActiveObject.getBodyOnThis();

        if (this.rank == 0) {
            // Call start on each other
            for (int i = 1; i < groupSize; i++) {
                workersArray[i].start(numOfIterations);
            }
        }

        // Do the computation in N steps
        // There are "groupSize" instances participating.  Each
        // instance should do 1/groupSize of the calculation.  Since we want
        // i = 1..n but rank = 0, 1, 2..., we start off with rank+1.
        long startTime = System.currentTimeMillis();

        long i;
        double err = 0.0d;
        double sum = 0.0d;
        double w = 1.0 / N;

        for (i = rank + 1; i <= N; i += groupSize)
            sum += f((i - 0.5) * w);
        sum *= w;

        // The leader collects partial results.
        // Others just send their computed data to the rank 0.
        if (rank == 0) {
            for (i = 1; i < groupSize; i++) {
                body.serve(body.getRequestQueue().blockingRemoveOldest("updateX")); // block until an updateX call
                sum += x;
            }
        } else {
            workersArray[0].updateX(sum);
        }

        long elapsedTime = (System.currentTimeMillis() - startTime);

        System.out.println("\n\t Worker " + rank + " Calculated x = " + sum + " in " + elapsedTime + " ms\n");

        return new DoubleWrapper(sum);
    }

    /**
     *    4
     * --------
     * (1 + x²)
     *
     * @param x The x value of the f(x)
     *
     * @return The computed result
     */
    public static final double f(final double x) {
        return ((4.0 / (1.0 + (x * x))));
    }

    /**
     * This method will be called remotely by a worker to send its value.
     *
     * @param value The value to remotely update.
     */
    public void updateX(double value) {
        this.x = value;
    }
}
