/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.selection;

/**
 * 
 * This class represents probability entity, provides means for
 * increasing and decreasing it indefinitely and subjects to the following
 * properties: <BR>
 * 
 * 1. Value always stays in bounds between 0 and 1 including ends. <BR>
 * 2. By calling "increase" method it goes to 1 but never reach it. <BR> 
 * 3. By calling "decrease" method it goes to 0 but never reach it. <BR>
 * 4. By calling "increase" method N times and then "decrease" method N times
 * it goes to the initial value.<BR>
 */
public class Probability {

    private int step = 0;
    private double probability = calcProbability(step);

    /**
     * Default constructor
     */
    public Probability() {
    }

    /**
     * Creates an object with given probability
     */
    public Probability(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the probability value
     */
    public double value() {
        return probability;
    }

    /**
     * Calculates default probability value  
     */
    public static double defaultValue() {
        return calcProbability(0);
    }

    /**
     * Decreases the probability
     */
    public void decrease() {
        probability = calcProbability(--step);
    }

    /**
     * Increases the probability
     */
    public void increase() {
        probability = calcProbability(++step);
    }

    /**
     * Returns string probability representation
     */
    public String toString() {
        return String.valueOf(probability);
    }

    private static double calcProbability(int step) {
        return Math.atan(step) / Math.PI + 0.5;
    }
}
