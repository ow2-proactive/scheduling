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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.utils;

import java.util.Timer;
import java.util.TimerTask;

import org.ow2.proactive.scheduler.newimpl.TaskLauncher;
import org.apache.log4j.Logger;


/**
 * Class responsible for killing an executable if it does not finish before the walltime.
 * It accepts in constructor an executable and walltime, then we can start it by calling schedule method
 * If the executable finishes before the walltime we should call cancel method to cancel the killing function scheduled for future invocation.

 * @author The ProActive Team
 */
public class KillTask {

    public static final Logger logger = Logger.getLogger(TaskLauncher.class);

    private Guard guard;
    private Timer timer;
    private long walltime;
    private volatile boolean walltimeReached = false;

    /**
     * Create a new instance of KillTask.
     *
     * @param guard the guard that may be killed.
     * @param walltime the walltime not to be exceeded.
     */
    public KillTask(Guard guard, long walltime) {
        this.guard = guard;
        this.walltime = walltime;
    }

    /**
     * Starting a timer for killing an executable when the walltime is over
     */
    public void schedule() {
        timer = new Timer("KillTask");
        timer.schedule(new KillProcess(), walltime);
    }

    public boolean walltimeReached() {
        return walltimeReached;
    }

    /**
     * Canceling a timer scheduled for killing an executable in the future
     */
    synchronized public void cancel() {
        timer.cancel();
    }

    class KillProcess extends TimerTask {
        /**
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            logger.info("Walltime of " + walltime + " ms exceeded.");
            guard.kill(true);
            walltimeReached = true;
        }
    }
}
