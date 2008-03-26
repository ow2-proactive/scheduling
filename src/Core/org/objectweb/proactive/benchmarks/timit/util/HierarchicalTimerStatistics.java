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

import org.objectweb.proactive.benchmarks.timit.TimIt;


/**
 * Represents some pure time statistics of one run
 *
 * @author The ProActive Team
 */
public class HierarchicalTimerStatistics implements Serializable {

    /**
     *
     */
    private String[] timerName;
    private double[][][] deviation;
    private double[][][] average;
    private double[][][] min;
    private double[][][] max;
    private int[] hierarchy;
    private int nb;
    private int padding;
    private boolean empty;

    /**
     * Create an empty HierarchicalTimerStatistics (used if no counters are
     * activated)
     */
    public HierarchicalTimerStatistics() {
        this(new String[1], new double[1][1][1], new double[1][1][1], new double[1][1][1],
                new double[1][1][1], new int[1], 0);
        this.empty = true;
    }

    /**
     * Invoked by HierarchicalTimer to create statistics when all timed objects
     * were finalized
     *
     * @param timerName
     * @param deviation
     * @param average
     * @param min
     * @param max
     * @param hierarchy
     * @param nb
     */
    public HierarchicalTimerStatistics(String[] timerName, double[][][] deviation, double[][][] average,
            double[][][] min, double[][][] max, int[] hierarchy, int nb) {
        this.timerName = timerName.clone();
        this.deviation = deviation.clone();
        this.average = average.clone();
        this.min = min.clone();
        this.max = max.clone();
        this.hierarchy = hierarchy.clone();
        this.nb = nb;
        this.empty = false;
    }

    public double getDeviation(int i, int j, int k) {
        return this.deviation[i][j][k];
    }

    public double getAverage(int i, int j, int k) {
        return this.average[i][j][k];
    }

    public double getMin(int i, int j, int k) {
        return this.min[i][j][k];
    }

    public double getMax(int i, int j, int k) {
        return this.max[i][j][k];
    }

    public String getFormDeviation(int i, int j, int k) {
        return TimIt.df.format(this.deviation[i][j][k]);
    }

    public String getFormAverage(int i, int j, int k) {
        return TimIt.df.format(this.average[i][j][k]);
    }

    public String getFormMin(int i, int j, int k) {
        return TimIt.df.format(this.min[i][j][k]);
    }

    public String getFormMax(int i, int j, int k) {
        return TimIt.df.format(this.max[i][j][k]);
    }

    public int getParent(int i, int j, int k) {
        return this.hierarchy[i];
    }

    public String[] getNameArray() {
        return this.timerName.clone();
    }

    public int getNb() {
        return this.nb;
    }

    public int[] getHierarchy() {
        return this.hierarchy.clone();
    }

    public void setTimerName(int id, String name) {
        this.timerName[id] = name;
    }

    /**
     * Construct a well formatted String which show timers value with hierarchy
     */
    @Override
    public String toString() {
        if (this.empty || (this.timerName.length == 0)) {
            return "";
        }

        String result = "";
        int i;
        int j;
        int k;

        // Suppose that the root timer is the first of the timerName array
        String rootName = this.timerName[0];
        String tName;
        String first = "\n";

        this.padding = (int) Math.ceil(Math.log10(this.max[0][0][0])) + 4;

        for (i = 0; i < this.nb; i++) {
            for (j = 0; j < this.nb; j++) {
                for (k = 0; k < this.nb; k++) {
                    if (this.min[i][j][k] != -1) {
                        // If the current timer name is the rootName print data
                        if (this.timerName[k] != null) {
                            if (this.timerName[k].equals(rootName)) {
                                tName = rootName + " :";
                                tName = this.paddingString(tName, 30, ' ', false);
                                result += (tName + "min " + this.format(this.min[i][j][k]));
                            } else {
                                if (this.timerName[j] != null) {
                                    if (this.timerName[k].equals(this.timerName[j])) {
                                        tName = "-----> " + this.timerName[j] + " :";
                                        tName = this.paddingString(tName, 30, ' ', false);
                                        result += (tName + "min " + this.format(this.min[i][j][k]));
                                    } else {
                                        tName = "        -----> " + this.timerName[k] + " :";
                                        tName = this.paddingString(tName, 30, ' ', false);
                                        result += (tName + "min " + this.format(this.min[i][j][k]));
                                    }
                                }
                            }
                        }
                    }

                    if (this.average[i][j][k] != -1) {
                        result += ("avg " + this.format(this.average[i][j][k]));
                    }
                    if (this.max[i][j][k] != -1) {
                        result += ("max " + this.format(this.max[i][j][k]));
                    }
                    if (this.deviation[i][j][k] != -1) {
                        result += ("dev " + this.format(this.deviation[i][j][k]) + "\n" + first);
                    }
                    first = "";
                }
            }
        }

        return result;
    }

    /**
     * Get a formatted String from a double value (time)
     *
     * @param t
     *            time value
     * @return formatted String (padded)
     */
    private final String format(double t) {
        return this.paddingString(TimIt.df.format(t), this.padding, ' ', true) + "s    ";
    }

    /**
     * Pad a string S with a size of N with char C on the left (True) or on the
     * right(false)
     */
    private String paddingString(String s, int n, char c, boolean paddingLeft) {
        StringBuffer str = new StringBuffer(s);
        int strLength = str.length();
        if ((n > 0) && (n > strLength)) {
            for (int i = 0; i <= n; i++) {
                if (paddingLeft) {
                    if (i < (n - strLength)) {
                        str.insert(0, c);
                    }
                } else {
                    if (i > strLength) {
                        str.append(c);
                    }
                }
            }
        }
        return str.toString();
    }
}
