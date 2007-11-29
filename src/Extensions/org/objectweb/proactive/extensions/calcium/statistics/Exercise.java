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
package org.objectweb.proactive.extensions.calcium.statistics;

import java.io.Serializable;
import java.util.Comparator;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * This class provides profiling on a Muscle function. After
 * the execution of the program, this class says how many times
 * a muscle function was invoked, and how much time was spent in the computation
 * of this class.
 * 
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class Exercise implements Serializable {
    public final static Comparator<Exercise> compareByComputationTime = new CompareByComputationTime();
    public final static Comparator<Exercise> compareByInvokedTimes = new CompareByInvokedTimes();
    private Class<?> c;
    private long computationTime;
    private int numberExecutedTimes;

    public Exercise(Class<?> c) {
        this.c = c;
        computationTime = 0;
        numberExecutedTimes = 0;
    }

    public Exercise(Class<?> c, int computationTime, int numberExecutedTimes) {
        this.c = c;
        this.computationTime = computationTime;
        this.numberExecutedTimes = numberExecutedTimes;
    }

    /**
     * @return The Class of the muscle that corresponds to this exercise.
     */
    public Class<?> getMuscleClass() {
        return c;
    }

    /**
     * @return Returns the computationTime.
     */
    public long getComputationTime() {
        return computationTime;
    }

    /**
     * @return Returns the numberExecutedTimes.
     */
    public long getNumberExecutedTimes() {
        return numberExecutedTimes;
    }

    /**
     * @param computationTime The computationTime to increment in.
     */
    void incrementComputationTime(Timer time) {
        this.computationTime += time.getTime();
        numberExecutedTimes += time.getNumberOfActivatedTimes();
    }

    void incrementComputationTime(Exercise exercise) {
        this.computationTime += exercise.computationTime;
        this.numberExecutedTimes += exercise.numberExecutedTimes;
    }

    @Override
    public String toString() {
        return computationTime + "/" + numberExecutedTimes;
    }

    static class CompareByComputationTime implements Comparator<Exercise> {
        public int compare(Exercise o1, Exercise o2) {
            return (new Long(o1.computationTime)).compareTo(new Long(
                    o2.computationTime));
        }
    }

    static class CompareByInvokedTimes implements Comparator<Exercise> {
        public int compare(Exercise o1, Exercise o2) {
            return (new Long(o1.numberExecutedTimes)).compareTo(new Long(
                    o2.numberExecutedTimes));
        }
    }
}