/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.utils;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

public class TaskKiller {

    private static final Logger LOGGER = Logger.getLogger(TaskKiller.class);

    private static final long CLEANUP_TIME_DEFAULT_SECONDS = 10;
    private static final long SLEEP_MILLISECONDS_DURING_THREAD_INTERRUPT = 10;
    private static final long SLEEP_MILLISECONDS_BETWEEN_IS_THREAD_ALIVE_CHECK = 100;
    private static final long MULTIPLIER_FROM_SECONDS_TO_ITERATION_WITH_CLEANUP_TIME_SLEEPER = 10;
    private static final Sleeper threadInterruptSleeper = new Sleeper(SLEEP_MILLISECONDS_DURING_THREAD_INTERRUPT, LOGGER);
    private static final Sleeper cleanupTimeSleeper = new Sleeper(SLEEP_MILLISECONDS_BETWEEN_IS_THREAD_ALIVE_CHECK, LOGGER);
    private Thread threadToKill;
    private boolean wasKilled = false;
    private Status status = Status.NOT_YET_KILLED;

    public TaskKiller(Thread threadToKill) { // executor service?
        this.threadToKill = threadToKill;
    }

    public synchronized boolean wasKilled() {
        return wasKilled;
    }

    /**
     * Interrupts the target thread repeatedly as InterruptedException can be catched without action
     */
    private void interruptRepeatedly() {
        while (threadToKill.isAlive()) {
            threadToKill.interrupt();
            threadInterruptSleeper.sleep();
        }
    }

    private long getCleanupTimeSeconds() {
        try {
            String cleanupTimeString = System.getProperty(RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME);
            if (cleanupTimeString != null) {
                return Long.parseLong(cleanupTimeString);
            } else {
                return CLEANUP_TIME_DEFAULT_SECONDS;
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("proactive.task.cleanup.time: "
                    + System.getProperty(RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME)
                    + " is not parsable to long, fallback to default value. Error : "
                    + e.getMessage());
            return CLEANUP_TIME_DEFAULT_SECONDS;
        }
    }

    /**
     * Interrupts and gives the thread time to cleanup
     */
    private void interruptAndWaitCleanupTime() {
        threadToKill.interrupt();
        long iterationsToHitWaitingTimeLimit =
                getCleanupTimeSeconds() * MULTIPLIER_FROM_SECONDS_TO_ITERATION_WITH_CLEANUP_TIME_SLEEPER;
        for (long i = 0; threadToKill.isAlive() && i < iterationsToHitWaitingTimeLimit; i++) {
            cleanupTimeSleeper.sleep();

        }
    }

    public void kill(Status status) {
        synchronized (this) {
            wasKilled = true;
            this.status = status;
        }
        interruptAndWaitCleanupTime();
        interruptRepeatedly();
    }

    public synchronized Status getStatus() {
        return status;
    }

    public enum Status {
        KILLED_MANUALLY, WALLTIME_REACHED, NOT_YET_KILLED
    }

}
