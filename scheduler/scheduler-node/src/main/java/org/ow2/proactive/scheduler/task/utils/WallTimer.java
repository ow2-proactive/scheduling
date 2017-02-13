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
package org.ow2.proactive.scheduler.task.utils;

import java.util.Timer;
import java.util.TimerTask;

import org.ow2.proactive.scheduler.task.utils.task.termination.TaskKiller;


public class WallTimer {

    private Timer timer;

    private final TaskKiller taskKiller;

    private final long wallTimeDuration;

    /**
     * Create and start a WallTimer instance.
     *
     * @param wallTimeDuration the maximum timeout period to wait for (in ms).
     * @param taskKiller the task killer used to kill the task once the timeout is reached.
     */
    public WallTimer(final long wallTimeDuration, final TaskKiller taskKiller) {
        this.wallTimeDuration = wallTimeDuration;
        this.taskKiller = taskKiller;
        this.timer = new Timer();
    }

    public void start() {
        if (wallTimeDuration > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (this) {
                        taskKiller.kill(TaskKiller.Status.WALLTIME_REACHED);
                    }
                }
            }, wallTimeDuration);
        }
    }

    public synchronized boolean hasWallTimed() {
        return taskKiller.wasKilled();
    }

    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

}
