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
package org.objectweb.proactive.examples.flowshop;

import java.util.Arrays;
import java.util.Vector;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.extensions.branchnbound.core.Result;
import org.objectweb.proactive.extensions.branchnbound.core.Task;
import org.objectweb.proactive.extensions.branchnbound.core.exception.NoResultsException;


/**
 * A Task for the FlowShop problem.
 *
 * A FlowShopTask can split the problem in subproblem and compute its best
 * makespan. FlowShopTask extends Task, this allow to take advantage of the
 * branch and bound api from ProActive, which manage task comunication and
 * repartition on the available nodes.
 *
 * @author Cedric Dalmasso
 */
public class FlowShopTask extends Task {
    //in ms
    private static final long MAX_TIME_TO_SPLIT = 120000; // 2'
    private FlowShop fs;
    private FlowShopResult fsr;

    /**
     * The permutation with we work
     */
    private int[] currentPerm;

    /**
     * The best know permutation and its makespan
     */

    // private int[] bestPerm;
    // private long bestMakespan;

    /**
     * The last permutation we must explore.
     */
    private int[] lastPerm;

    /**
     * The depth tree where we tests all permutations
     */
    private int depth;

    /**
     * for benchmark
     */
    private boolean com;
    private boolean randomInit;

    /**
     *
     */
    private long lowerBound;
    private long upperBound;
    private Result r;

    public FlowShopTask() {
        // the empty no args constructor for ProActive
    }

    /**
     * Contruct a Task which search solution for all permutations to the
     * Flowshop problem. Use it to create the root Task.
     *
     * @param fs the description of the Flowshop problem
     * @param com for bench
     * @param randomInit for bench
     */
    public FlowShopTask(FlowShop fs, long lowerBound, long upperBound,
        boolean com, boolean randomInit) {
        this(fs, lowerBound, upperBound, null, null, 0, com, randomInit);
        currentPerm = new int[fs.jobs.length];
        for (int i = 0; i < currentPerm.length; i++) {
            currentPerm[i] = i;
        }
    }

    /**
     * @param fs
     * @param lowerBound
     * @param upperBound
     * @param currentPerm
     * @param lastPerm
     * @param depth
     * @param com
     * @param randomInit
     */
    public FlowShopTask(FlowShop fs, long lowerBound, long upperBound,
        int[] currentPerm, int[] lastPerm, int depth, boolean com,
        boolean randomInit) {
        this.fs = fs;
        this.fsr = new FlowShopResult();
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.currentPerm = (currentPerm == null) ? null
                                                 : (int[]) currentPerm.clone();
        this.lastPerm = (lastPerm == null) ? null : (int[]) lastPerm.clone();
        this.depth = depth;
        this.com = com;
        this.randomInit = randomInit;
        this.r = new Result();
    }

    /**
    * Return the next permutation. Warning, the parmeter are modified.
    *
    * @param perm the permutation we modifie to get the next
    * @return the next permutation if exist, null otherwise
    */
    private static int[] nextPerm(int[] perm) {
        int i = perm.length - 1;
        while ((i > 0) && (perm[i - 1] >= perm[i]))
            i--;
        if (i == 0) {
            // pas de permutation suivante
            return null;
        }
        int m = i - 1; // TODO do i-- and remove m
        int j = perm.length - 1;

        while (perm[m] >= perm[j])
            j--;
        int tmp = perm[m];
        perm[m] = perm[j];
        perm[j] = tmp;
        int k = m + 1;
        int lamb = perm.length - 1;
        while (k < lamb) {
            tmp = perm[k];
            perm[k] = perm[lamb];
            perm[lamb] = tmp;
            k++;
            lamb--;
        }
        return perm;
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
    private static int computeConditionalMakespan(FlowShop fs,
        int[] permutation, long bound, int[] timeMachine) {
        //contains cumulated time by machine
        for (int i = 0; i < timeMachine.length; i++) {
            timeMachine[i] = 0;
        }
        int nbJob = permutation.length;
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
            if ((timeMachine[timeMachine.length - 1] +
                    cumulateTimeOnLastMachine) >= bound) {
                return -(i + 1);
            }
        }

        return timeMachine[timeMachine.length - 1];
    }

    /**
     * Jump the n&#33;-th following permutations. That cut an entire branch of the
     * permutation's tree, for example with the jumpPerm( {1 2 3 4}, 3) we
     * return {1 4 3 2}, the first branch are skipped.
     *
     * @param perm the current permutation
     * @param n
     * @return int[]
     */
    private static int[] jumpPerm(int[] perm, int n, int[] tmp) {
        System.arraycopy(perm, perm.length - n, tmp, 0, n);

        /*for (int i = perm.length - n, j = 0; i < perm.length; i++, j++) {
                tmp[j] = perm[i];
        }*/
        Arrays.sort(tmp); //necessary when we jump an uncomplete branch
        for (int i = 0, srcI = perm.length - 1; i < n; i++, srcI--) {
            perm[srcI] = tmp[i];
        }
        return perm;
    }

    /**
     * Explore all permutation between currentPerm and lastPerm. May decide
     * also to split in sub Task.
     *
     * @see org.objectweb.proactive.branchnbound.core.Task#execute()
     */
    @Override
    public Result execute() {
        int[] timeMachine = new int[fs.nbMachine];
        long time = System.currentTimeMillis();
        long nbPerm = 1;

        //        int[] cutbacks = new int[fs.jobs.length];
        int nbLoop = 0;
        int theLastJobFixed = currentPerm[depth - 1];

        //		Main.logger.info("depth  " +Permutation.string(currentPerm));
        //CHANGE HERE THE DEPTH OF SPLIT
        boolean mustSplit = ((depth < 2) && ((currentPerm.length - depth) > 2)); //Why not ?

        if (com) {
            this.bestKnownSolution = fsr;
            r.setSolution(fsr);
            this.worker.setBestCurrentResult(r);
        } else {
            this.bestKnownSolution = fsr;
        }
        if (!mustSplit) {
            int[][] tmpPerm = new int[currentPerm.length][];
            for (int i = 0; i < tmpPerm.length; i++) {
                tmpPerm[i] = new int[i];
            }
            while ((FlowShopTask.nextPerm(currentPerm)) != null) {
                nbLoop++;
                if ((lastPerm != null) &&
                        (currentPerm[depth - 1] != theLastJobFixed)) {
                    //					(Permutation.compareTo(currentPerm, lastPerm) >= 0)) {
                    //					Main.logger.info("depth " + depth + "  cmp  " + Permutation.string(currentPerm) + " >= " + Permutation.string(lastPerm));
                    break;
                }
                int currentMakespan;

                if (com) {
                    fsr.makespan = ((FlowShopResult) this.bestKnownSolution).makespan;
                    fsr.permutation = ((FlowShopResult) this.bestKnownSolution).permutation;
                }

                if ((currentMakespan = FlowShopTask.computeConditionalMakespan(
                                fs, currentPerm,
                                ((FlowShopResult) this.bestKnownSolution).makespan,
                                timeMachine)) < 0) {
                    //bad branch
                    int n = currentPerm.length + currentMakespan;
                    FlowShopTask.jumpPerm(currentPerm, n, tmpPerm[n]);
                    //					cutbacks[-currentMakespan - 1]++;
                    if (nbLoop > 100000000) { // TODO verify
                        if (((System.currentTimeMillis() - time) > MAX_TIME_TO_SPLIT) &&
                                worker.isHungry().booleanValue()) { // avoid too tasks
                            mustSplit = true;
                            nbPerm++;
                            break;
                        } else {
                            nbLoop = 0;
                        }
                    }
                } else {
                    // better branch than previous best
                    if (com) {
                        fsr.makespan = currentMakespan;
                        System.arraycopy(currentPerm, 0, fsr.permutation, 0,
                            currentPerm.length);
                        r.setSolution(fsr);
                        this.worker.setBestCurrentResult(r);
                    } else {
                        ((FlowShopResult) this.bestKnownSolution).makespan = currentMakespan;
                        System.arraycopy(currentPerm, 0,
                            ((FlowShopResult) this.bestKnownSolution).permutation,
                            0, currentPerm.length);
                    }
                }

                nbPerm++;
            }
        }
        time = System.currentTimeMillis() - time;

        if (mustSplit) {
            this.worker.sendSubTasksToTheManager(((FlowShopTask) ProActiveObject.getStubOnThis()).split());
        }

        Main.logger.info(" -- Explore " + nbPerm + " permutations in " + time +
            " ms\nBest makespan :" +
            ((FlowShopResult) this.bestKnownSolution).makespan +
            " with this permutation " +
            Permutation.string(
                ((FlowShopResult) this.bestKnownSolution).permutation));
        //            + ". We have cut " + Permutation.string(cutbacks));
        ((FlowShopResult) this.bestKnownSolution).nbPermutationTested = nbPerm;
        ((FlowShopResult) this.bestKnownSolution).time = time;
        //        ((FlowShopResult) this.bestKnownResult).makespanCut = cutbacks;
        r.setSolution(bestKnownSolution);
        return r;
    }

    /**
     * Split the root Task in subtask. Can be called by the method execute() if
     * we want to split again.
     *
     * @see org.objectweb.proactive.branchnbound.core.Task#split()
     */
    @Override
    public Vector split() {
        int nbTasks = fs.jobs.length - depth;

        Vector tasks = new Vector(nbTasks);

        int[] perm = (int[]) currentPerm.clone();
        int[] beginPerm = new int[perm.length];

        do {
            if ((lastPerm != null) &&
                    (Permutation.compareTo(perm, lastPerm) > 0)) {
                break;
            }
            System.arraycopy(perm, 0, beginPerm, 0, perm.length);

            Permutation.jumpPerm(perm, perm.length - (depth + 1));

            tasks.add(new FlowShopTask(fs, this.lowerBound, this.upperBound,
                    beginPerm, perm, depth + 1, com, randomInit));
        } while (Permutation.nextPerm(perm) != null);
        if (tasks.size() != 0) {
            ((FlowShopTask) tasks.lastElement()).lastPerm = lastPerm;
        }
        Main.logger.info("We split in " + tasks.size() + " subtask at depth " +
            depth + " : " + Permutation.string(currentPerm) + ", " +
            Permutation.string(lastPerm));

        /*Iterator i = tasks.iterator();
           while (i.hasNext()) {
               Task t = (Task) i.next();
               Main.logger.info(t);
               Main.logger.info("");
           }*/
        return tasks;
    }

    /**
     *
     * @throws NoResultsException
     * @see org.objectweb.proactive.branchnbound.core.Task#gather(org.objectweb.proactive.branchnbound.core.Result[])
     */
    @Override
    public Result gather(Result[] results) {
        Result r = super.gather(results);
        long nbPerm = 0;
        long time = 0;

        //        int[] cuts = new int[currentPerm.length];
        for (int i = 0; i < results.length; i++) {
            FlowShopResult result = ((FlowShopResult) results[i].getSolution());
            nbPerm += result.getNbPermutationTested();
            time += result.getTime();
            //                int[] t = result.getMakespanCut();
            //                for (int j = 0; j < t.length; j++) {
            //                    cuts[j] += t[j];
            //                }
        }
        long fact = fs.jobs.length;
        for (int i = fs.jobs.length - 1; i > 0; i--) {
            fact *= i;
        }
        double percent = (((double) nbPerm) / fact) * 100;
        Main.logger.info("We test " + nbPerm + " permutation on " + fact +
            " (" + percent + "%) in " + time + " ms.");
        //+ ". We have cut " + Permutation.string(cuts));
        return r;
    }

    @Override
    public void initLowerBound() {
        if (lowerBound == -1) {
            lowerBound = FlowShop.computeLowerBound(this.fs);
            Main.logger.info("We compute a lower bound: " + lowerBound);
        }
    }

    @Override
    public void initUpperBound() {
        int[] randomPerm = (int[]) currentPerm.clone();
        for (int i = depth + 1; i < randomPerm.length; i++) {
            int randomI = (int) (i +
                (Math.random() * (randomPerm.length - (i + 1))));
            int tmp = randomPerm[i];
            randomPerm[i] = randomPerm[randomI];
            randomPerm[randomI] = tmp;
        }
        Main.logger.info("initUpperBound => " +
            (randomInit
            ? ("random Perm : " + Permutation.string(randomPerm) +
            " her makespan " + FlowShop.computeMakespan(fs, randomPerm))
            : (" non random Perm " + Permutation.string(currentPerm) +
            FlowShop.computeMakespan(fs, currentPerm))));
        fsr.makespan = randomInit ? FlowShop.computeMakespan(fs, randomPerm)
                                  : FlowShop.computeMakespan(fs, currentPerm);
        fsr.permutation = randomInit ? randomPerm : (int[]) currentPerm.clone();
    }

    public void setCom(boolean b) {
        com = b;
    }

    @Override
    public String toString() {
        return "FSTask: makespan " +
        ((fsr.permutation == null) ? ""
                                   : ("" +
        FlowShop.computeMakespan(fs,
            ((FlowShopResult) this.bestKnownSolution).permutation))) +
        ",\nbest perm : " +
        (((FlowShopResult) this.bestKnownSolution == null) ? ""
                                                           : ((((FlowShopResult) this.bestKnownSolution).permutation == null)
        ? ""
        : Permutation.string(((FlowShopResult) this.bestKnownSolution).permutation))) +
        ",\ncurrent perm : " + Permutation.string(currentPerm) +
        " and\nlast Perm : " + Permutation.string(lastPerm);
    }
}
