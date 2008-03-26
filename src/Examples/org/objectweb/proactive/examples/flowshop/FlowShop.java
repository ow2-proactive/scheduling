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
package org.objectweb.proactive.examples.flowshop;

import java.io.Serializable;


/**
 * FlowShop describe a problem instance with this jobs and the number of
 * machine and k
 * @author The ProActive Team
 *
 */
public class FlowShop implements Serializable {
    public int[][] jobs;
    public int nbMachine;
    public long cumulateTimeOnLastMachine = 0;

    /**
     * @param nbMachine
     * @param jobs
     */
    public FlowShop(int nbMachine, int[][] jobs) {
        this.nbMachine = nbMachine;
        this.jobs = jobs;
        for (int i = 0; i < jobs.length; i++) {
            cumulateTimeOnLastMachine += jobs[i][nbMachine - 1];
        }

        // more secure for the begin, do this in the parser ?
        for (int i = 0; i < jobs.length; i++)
            if (jobs[i].length != nbMachine) {
                System.out.println("Bad Job");
            }
    }

    /**
     * Compute the makespan for a FlowShop and this permutation.
     *
     * @param fs
     * @param permutation
     * @return the makespan, the overall completion time for all the jobs of
     * the fs
     */
    public static int computeMakespan(FlowShop fs, int[] permutation) {
        return computePartialMakespan(fs, permutation, permutation.length)[fs.nbMachine - 1];
    }

    /**
     * Compute the makespan for a FlowShop and this permutation, for only the
     * nbJob first job.
     *
     * @param fs
     * @param permutation
     * @param nbJob the length use of the permutation, begin 0
     * @return the makespan for the nbJob first job of permutation
     */
    public static int[] computePartialMakespan(FlowShop fs, int[] permutation, int nbJob) {
        int[] timeMachine = new int[fs.nbMachine];
        for (int i = 0; i < nbJob; i++) {
            int[] currentJob = fs.jobs[permutation[i]];
            timeMachine[0] += currentJob[0];
            for (int j = 1; j < timeMachine.length; j++) {
                if (timeMachine[j] > timeMachine[j - 1]) {
                    timeMachine[j] = timeMachine[j] + currentJob[j];
                } else {
                    timeMachine[j] = timeMachine[j - 1] + currentJob[j];
                }
            }
        }
        return timeMachine;
    }

    /**
     * Compute the makespan while it does not exceed bound.
     *
     * @param fs
     * @param permutation
     * @param nbJob
     * @param bound
     * @return the makespan if it not exceed bound else the negative index
     * where the makespan exeed the bound
     */
    public static int computeConditionalMakespan(FlowShop fs, int[] permutation, int nbJob, long bound) {
        //contains cumulated time by machine
        int[] timeMachine = new int[fs.nbMachine];
        long cumulateTimeOnLastMachine = fs.cumulateTimeOnLastMachine;

        for (int i = 0; i < nbJob; i++) {
            int[] currentJob = fs.jobs[permutation[i]];
            timeMachine[0] += currentJob[0];
            for (int j = 1; j < timeMachine.length; j++) {
                if (timeMachine[j] > timeMachine[j - 1]) {
                    timeMachine[j] = timeMachine[j] + currentJob[j];
                } else {
                    // the machine j is later than machine j-1 
                    timeMachine[j] = timeMachine[j - 1] + currentJob[j];
                }
            }
            cumulateTimeOnLastMachine -= currentJob[timeMachine.length - 1];
            if ((timeMachine[timeMachine.length - 1] + cumulateTimeOnLastMachine) >= bound) {
                return -(i + 1);
            }
        }

        return timeMachine[timeMachine.length - 1];
    }

    public static long computeLowerBound(FlowShop fs) {
        long timeOnLastMachines = 0;
        int lastMachineId = fs.nbMachine - 1;
        for (int i = 0; i < fs.jobs.length; i++) {
            timeOnLastMachines += fs.jobs[i][lastMachineId];
        }
        long lowerBound = 0;
        int[] currentJob = fs.jobs[0];
        for (int j = 0; j < lastMachineId; j++) { // we don't add the last machine time, already in timeOnLastMachines
            lowerBound += currentJob[j];
        }
        lowerBound += timeOnLastMachines;
        long tmpBound = 0;
        for (int i = 1; i < fs.jobs.length; i++) {
            currentJob = fs.jobs[i];
            for (int j = 0; j < lastMachineId; j++) { // we don't add the last machine time, already in timeOnLastMachines
                tmpBound += currentJob[j];
            }
            tmpBound += timeOnLastMachines;
            if (tmpBound < lowerBound) {
                lowerBound = tmpBound;
            }
        }
        return lowerBound;
    }
}
