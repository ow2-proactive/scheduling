/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.task.utils.task.termination;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;


public class TaskKiller {

    private static final Logger LOGGER = Logger.getLogger(TaskKiller.class);

    private CleanupTimeoutGetter cleanupTimeoutGetter;

    private static final long SLEEP_MILLISECONDS_DURING_THREAD_INTERRUPT = 10;

    private static final long SLEEP_MILLISECONDS_BETWEEN_IS_THREAD_ALIVE_CHECK = 100;

    private static final long MULTIPLIER_FROM_SECONDS_TO_ITERATION_WITH_CLEANUP_TIME_SLEEPER = 10;

    private static final Sleeper threadInterruptSleeper = new Sleeper(SLEEP_MILLISECONDS_DURING_THREAD_INTERRUPT,
                                                                      LOGGER);

    private static final Sleeper cleanupTimeSleeper = new Sleeper(SLEEP_MILLISECONDS_BETWEEN_IS_THREAD_ALIVE_CHECK,
                                                                  LOGGER);

    private Thread threadToKill;

    private boolean wasKilled = false;

    private Status status = Status.NOT_YET_KILLED;

    public TaskKiller(Thread threadToKill, CleanupTimeoutGetter cleanupTimeoutGetter) {
        this.threadToKill = threadToKill;
        this.cleanupTimeoutGetter = cleanupTimeoutGetter;
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

    /**
     * Interrupts and gives the thread time to cleanup
     */
    private void interruptAndWaitCleanupTime() {
        threadToKill.interrupt();
        long iterationsToHitWaitingTimeLimit = cleanupTimeoutGetter.getCleanupTimeSeconds() *
                                               MULTIPLIER_FROM_SECONDS_TO_ITERATION_WITH_CLEANUP_TIME_SLEEPER;
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
        KILLED_MANUALLY,
        WALLTIME_REACHED,
        NOT_YET_KILLED
    }

}
