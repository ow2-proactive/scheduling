/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.selection.statistics;

import java.util.Timer;
import java.util.TimerTask;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


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

    public static final Probability ZERO = new Probability(0);
    public static final Probability ONE = new Probability(1);

    private static Timer timer = new Timer(true);
    private int step = 0;
    private double probability = calcProbability(step);

    private class RestoreProbabilityTask extends TimerTask {

        private final double probability;

        private RestoreProbabilityTask(double probability) {
            this.probability = probability;
        }

        @Override
        public void run() {
            synchronized (Probability.this) {
                Probability.this.probability = probability;
            }
        }
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
    public synchronized double value() {
        return probability;
    }

    /**
     * Calculates default probability value
     */
    public static double defaultValue() {
        return calcProbability(0);
    }

    /**
     * Sets the probability to zero until the timer restores it.
     * It's done to pause the permanent execution of dynamic selection scripts
     */
    public synchronized void decrease() {

        if (probability > 0 && probability < 1) {
            // setting the probability to 0 to timeout the script execution on this node
            // and scheduling the timer
            probability = calcProbability(--step);
            timer.schedule(new RestoreProbabilityTask(probability),
                    PAResourceManagerProperties.RM_SELECT_SCRIPT_NODE_DYNAMICITY.getValueAsInt());
        }

        probability = 0;
    }

    /**
     * Increases the probability
     */
    public synchronized void increase() {
        probability = calcProbability(++step);
    }

    /**
     * Returns string probability representation
     */
    @Override
    public synchronized String toString() {
        return String.valueOf(probability);
    }

    private static double calcProbability(int step) {
        return Math.atan(step) / Math.PI + 0.5;
    }
}
