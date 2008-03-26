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
package org.objectweb.proactive.benchmarks.timit.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


/**
 * This class provide some methods to benchmark your applications. <br>
 * Example of use : (TODO)<br>
 * <code>
 * extends Timeable<br>
 * Timer timer = new Timer();<br>
 * timer.start(T_total);<br>
 * timer.stop(T_total);<br>
 * Stats stats = reduce(timer, rank, leader, groupSize);
 * </code>
 *
 * @see org.objectweb.proactive.benchmarks.timit.examples
 * @author The ProActive Team
 *
 */
public class HierarchicalTimer implements Serializable {

    /**
     *
     */
    public static final int MAX_ENABLED_COUNTERS = 20;
    private static final int MAX_DEPTH = 3;

    /*
     * The ArrayList of HierarchicalTimers collected from others Workers ie
     * timed entities
     */
    private ArrayList<HierarchicalTimer> timersList;

    /* A 3 dim array of collected values */
    private int[][][] total; // MAX_DEPTH related

    /* The initial values */
    private long[] start;

    /* The current level used in case for hierarchy */
    protected int level;

    /* The parent counters array */
    protected int[] parent;

    /* The parent counters array */
    protected int[] parentStarted;

    /* The counter names */
    protected String[] counter_name;

    /* The number of counters */
    private int nbCounters;

    /**
     * Create a timer with all counters enabled. Note: Only MAX_ENABLED_COUNTERS
     * can be enabled at a time
     */
    public HierarchicalTimer() {
    }

    /**
     * Invoked by TimIt to activate only specified counters
     *
     * @param counters
     *            the counters you want to specify
     * @param tr
     *            the TimItReductor instance
     */
    public void activateCounters(TimerCounter[] counters, TimItReductor tr) {
        this.nbCounters = counters.length;

        if (this.nbCounters > HierarchicalTimer.MAX_ENABLED_COUNTERS) {
            throw new RuntimeException("Too many Counters to create. Max is " +
                HierarchicalTimer.MAX_ENABLED_COUNTERS);
        }
        this.total = new int[this.nbCounters][this.nbCounters][this.nbCounters];
        this.start = new long[HierarchicalTimer.MAX_DEPTH];
        this.parent = new int[HierarchicalTimer.MAX_DEPTH];
        this.parentStarted = new int[HierarchicalTimer.MAX_DEPTH];
        this.counter_name = new String[this.nbCounters];
        this.resetTimer();
        for (int i = 0; i < this.nbCounters; i++) {
            counters[i].setId(i);
            counters[i].setTimer(this);
            this.counter_name[i] = counters[i].getName();
            if (counters[i].isMigratable()) {
                ((MigratableCounter) (counters[i])).setClock(tr);
            }
        }
    }

    /**
     * Reset the timing values
     */
    public void resetTimer() {
        int i;
        int j;
        int k;
        for (i = 0; i < this.nbCounters; i++) {
            for (j = 0; j < this.nbCounters; j++) {
                for (k = 0; k < this.nbCounters; k++) {
                    this.total[i][j][k] = -1;
                }
            }
        }
        Arrays.fill(this.parent, -1);
        this.level = -1;
    }

    /**
     * Reset only one counter
     *
     * @param n
     *            the counter id
     */
    public void resetCounter(int n) {
        for (int i = 0; i < this.nbCounters; i++) {
            for (int j = 0; j < this.nbCounters; j++) {
                this.total[i][j][n] = -1;
            }
        }
    }

    /**
     * Adds a HierarchicalTimer instance to the arrayList.
     *
     * @param t
     *            The HierarchicalTimer instance to add.
     */
    public void addInstance(HierarchicalTimer t) {
        if (this.timersList == null) {
            this.timersList = new ArrayList<HierarchicalTimer>();
        }
        this.timersList.add(t);
    }

    /**
     * Get the number of available counters
     *
     * @return the number of counters
     */
    public int getNbCounter() {
        return this.nbCounters;
    }

    /**
     * Get the name of counter from his id
     *
     * @param n
     *            the id of the counter
     * @return the name of the counter
     */
    public String getCounterName(int n) {
        return this.counter_name[n];
    }

    /**
     * Know if a counter is started or not
     *
     * @param n
     *            the id of the counter
     * @return true if started, false otherwise
     */
    public boolean isStarted(int n) {
        return (this.parent[0] == n) || (this.parent[1] == n) || (this.parent[2] == n);
    }

    /**
     * Starts a counter.
     *
     * @param n
     *            The integer that idetifies the timer to stop.
     */
    public void start(int n) {
        this.parent[++this.level] = n;
        this.start[this.level] = HierarchicalTimer.getCtm();
    }

    public void startAsync(int n) {
        this.start(n);
        switch (this.level) {
            case 0:
                this.parentStarted[0] = this.parent[0];
                this.parentStarted[1] = this.parent[0];
                this.parentStarted[2] = this.parent[0];
                break;
            case 1:
                this.parentStarted[0] = this.parent[0];
                this.parentStarted[1] = this.parent[1];
                this.parentStarted[2] = this.parent[1];
                break;
            case 2:
            case 3:
                this.parentStarted[0] = this.parent[0];
                this.parentStarted[1] = this.parent[1];
                this.parentStarted[2] = this.parent[2];
                break;
        }
    }

    /**
     * Stops the adequate counter.
     *
     * @param n
     *            The integer that idetifies the timer to stop.
     */
    public void stop(int n) {
        switch (this.level) {
            case 0:
                if (this.total[this.parent[0]][this.parent[0]][this.parent[0]] < 0) {
                    this.total[this.parent[0]][this.parent[0]][this.parent[0]] = 1;
                }
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] += (HierarchicalTimer.getCtm() - this.start[this.level]);
                break;
            case 1:
                if (this.total[this.parent[0]][this.parent[1]][this.parent[1]] < 0) {
                    this.total[this.parent[0]][this.parent[1]][this.parent[1]] = 1;
                }
                this.total[this.parent[0]][this.parent[1]][this.parent[1]] += (HierarchicalTimer.getCtm() - this.start[this.level]);
                break;
            case 2:
            case 3:
                if (this.total[this.parent[0]][this.parent[1]][this.parent[2]] < 0) {
                    this.total[this.parent[0]][this.parent[1]][this.parent[2]] = 1;
                }
                this.total[this.parent[0]][this.parent[1]][this.parent[2]] += (HierarchicalTimer.getCtm() - this.start[this.level]);
        }
        this.level--;
    }

    public void stopAsync(int n) {
        if (this.total[this.parent[0]][this.parent[1]][this.parent[2]] < 0) {
            this.total[this.parent[0]][this.parent[1]][this.parent[2]] = 1;
        }
        this.total[this.parentStarted[0]][this.parentStarted[1]][this.parentStarted[2]] += (HierarchicalTimer
                .getCtm() - this.start[this.level]);
        this.level--;
    }

    public void setValue(int n, int time) {
        this.parent[++this.level] = n;
        switch (this.level) {
            case 0:
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] = time;
                break;
            case 1:
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] -= this.total[this.parent[0]][this.parent[1]][this.parent[1]];
                this.total[this.parent[0]][this.parent[1]][this.parent[1]] = time;

                this.total[this.parent[0]][this.parent[0]][this.parent[0]] += time;
                break;
            case 2:
            case 3:
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] -= this.total[this.parent[0]][this.parent[1]][this.parent[2]];
                this.total[this.parent[0]][this.parent[1]][this.parent[1]] -= this.total[this.parent[0]][this.parent[1]][this.parent[2]];

                this.total[this.parent[0]][this.parent[1]][this.parent[2]] = time;

                this.total[this.parent[0]][this.parent[0]][this.parent[0]] += time;
                this.total[this.parent[0]][this.parent[1]][this.parent[1]] += time;
        }
        this.level--;
    }

    public void addValue(int n, int time) {
        this.parent[++this.level] = n;
        switch (this.level) {
            case 0:
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] += time;
                break;
            case 1:
                this.total[this.parent[0]][this.parent[1]][this.parent[1]] += time;
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] += time;
                break;
            case 2:
            case 3:
                this.total[this.parent[0]][this.parent[1]][this.parent[2]] += time;
                this.total[this.parent[0]][this.parent[0]][this.parent[0]] += time;
                this.total[this.parent[0]][this.parent[1]][this.parent[1]] += time;
        }
        this.level--;
    }

    /**
     * Returns the value
     */
    public int readTimer(int i, int j, int k) {
        return this.total[i][j][k];
    }

    /**
     * Returns the time in milliseconds elapsed since last start of this counter
     *
     * @param n
     *            the counter Id
     * @return the elapsed time since start
     */
    public int getElapsedTime(int n) {
        return (int) (HierarchicalTimer.getCtm() - this.start[this.level]);
    }

    /**
     * Returns the total time in milliseconds of this counter for this hierarchy
     *
     * @param n
     *            the counter Id
     * @return the total elapsed time for this hierarchy.
     */
    public int getHierarchicalTime(int n) {
        switch (this.level) {
            case 0:
                return this.total[this.parent[0]][this.parent[0]][this.parent[0]] + this.getElapsedTime(n);
            case 1:
                return this.total[this.parent[0]][this.parent[1]][this.parent[1]] + this.getElapsedTime(n);
            case 2:
            case 3:
                return this.total[this.parent[0]][this.parent[1]][this.parent[2]] + this.getElapsedTime(n);
            default:
                return 0;
        }
    }

    /**
     * Returns the total time in milliseconds of this counter for all
     * hierarchies
     *
     * @param n
     *            the counter Id
     * @return the total time elapsed for this counter in all hierarchies
     */
    public int getTotalTime(int n) {
        int value = 0;
        for (int[][] element : this.total) {
            for (int j = 0; j < this.total.length; j++) {
                value += element[j][n];
            }
        }
        return value;
    }

    /**
     *
     * @return The description of the current hierarchical timer.
     */
    @Override
    public String toString() {
        String result = "";
        int i;
        int j;
        int k;
        for (i = 0; i < this.total.length; i++) {
            for (j = 0; j < this.total.length; j++) {
                for (k = 0; k < this.total.length; k++) {
                    if (this.total[i][j][k] != -1) {
                        result += (this.counter_name[i] + " -> " + this.counter_name[j] + " -> " +
                            this.counter_name[k] + "\t = " + this.total[i][j][k] + " ms\n");
                    }
                }
            }
        }
        return result;
    }

    /**
     * This function must be called after all timers were collected.
     *
     * @return HierarchicalTimerStatistics An instance of stats class.
     */
    public HierarchicalTimerStatistics getStats() {
        double[][][] deviation = new double[this.nbCounters][this.nbCounters][this.nbCounters];
        double[][][] average = new double[this.nbCounters][this.nbCounters][this.nbCounters];
        double[][][] min = new double[this.nbCounters][this.nbCounters][this.nbCounters];
        double[][][] max = new double[this.nbCounters][this.nbCounters][this.nbCounters];
        int[][][] t;

        Iterator<HierarchicalTimer> it = this.timersList.iterator();

        int i;
        int j;
        int k;

        // Initialization of deviation, average, min and max values
        // The value 0 is not used as init val of average and deviation
        // because it can be a timer value so -1d is used instead;
        for (i = 0; i < this.nbCounters; i++) {
            for (j = 0; j < this.nbCounters; j++) {
                for (k = 0; k < this.nbCounters; k++) {
                    deviation[i][j][k] = -1d;
                    average[i][j][k] = -1d;
                    if (this.total[i][j][k] != -1) {
                        min[i][j][k] = Double.MAX_VALUE;
                        max[i][j][k] = Double.MIN_VALUE;
                    } else {
                        min[i][j][k] = -1d;
                        max[i][j][k] = -1d;
                    }
                }
            }
        }

        HierarchicalTimer timer;
        double tempValue;
        int groupSize = 0;

        // Read all timers
        while (it.hasNext()) {
            groupSize++;
            timer = (it.next());

            t = timer.total;

            for (i = 0; i < this.nbCounters; i++) {
                for (j = 0; j < this.nbCounters; j++) {
                    for (k = 0; k < this.nbCounters; k++) {
                        if (t[i][j][k] != -1) {
                            tempValue = t[i][j][k] / 1000.0;

                            if (min[i][j][k] > tempValue) {
                                min[i][j][k] = tempValue;
                            }
                            if (max[i][j][k] < tempValue) {
                                max[i][j][k] = tempValue;
                            }

                            // Avoid init value addition
                            if (deviation[i][j][k] == -1d) {
                                deviation[i][j][k] = tempValue * tempValue;
                            } else {
                                deviation[i][j][k] += (tempValue * tempValue);
                            }

                            // Same for average
                            if (average[i][j][k] == -1d) {
                                average[i][j][k] = tempValue;
                            } else {
                                average[i][j][k] += tempValue;
                            }
                        }
                    }
                }
            }
        }

        for (i = 0; i < this.nbCounters; i++) {
            for (j = 0; j < this.nbCounters; j++) {
                for (k = 0; k < this.nbCounters; k++) {
                    if ((average[i][j][k] != -1d) && (deviation[i][j][k] != -1d)) {
                        average[i][j][k] /= groupSize;
                        tempValue = average[i][j][k] * average[i][j][k];
                        deviation[i][j][k] = Math.sqrt((deviation[i][j][k] / groupSize) - tempValue);
                    }
                }
            }
        }
        return new HierarchicalTimerStatistics(this.counter_name, deviation, average, min, max, this.parent,
            this.nbCounters);
    }

    /**
     * Prints a 3dim array of doubles, suppose that array was initialized with
     * -1d values.
     *
     * @param array
     *            The 3 dim array of values to print.
     * @param counterName
     *            The array of counter names.
     * @param n
     *            The number of counters.
     */
    public static void printArray(double[][][] array, String[] counterName, int n) {
        String result = "";
        int i;
        int j;
        int k;
        for (i = 0; i < n; i++) {
            for (j = 0; j < n; j++) {
                for (k = 0; k < n; k++) {
                    if (array[i][j][k] != -1d) {
                        result += (counterName[i] + " -> " + counterName[j] + " -> " + counterName[k] +
                            "\t = " + array[i][j][k] + " s\n");
                    }
                }
            }
        }
        System.out.println(result);
    }

    /**
     * Get currentTimeMillis() maybe one day we will be able to enhanced this
     * call... Note: no performances impact (verified)
     */
    protected static final long getCtm() {
        return System.currentTimeMillis();
    }
}
