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

import java.util.Timer;
import java.util.TimerTask;


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
