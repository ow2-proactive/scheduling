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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task;

import java.util.Timer;
import java.util.TimerTask;

import org.ow2.proactive.scheduler.common.task.executable.Executable;


/**
 * Class responsible for killing an executable if it does not finish before the walltime.
 * It accepts in constructor an executable and walltime, then we can start it by calling schedule method
 * If the executable finishes before the walltime we should call cancel method to cancel the killing function scheduled for future invocation.

 * @author The ProActive Team
 */
public class KillTask {

    private Executable executable;
    private Timer timer;
    private long walltime;

    /**
     * Create a new instance of KillTask.
     *
     * @param executable the executable that may be killed.
     * @param walltime the walltime not to be exceeded.
     */
    public KillTask(Executable executable, long walltime) {
        this.executable = executable;
        this.walltime = walltime;
    }

    /**
     * Starting a timer for killing an executable when the walltime is over
     */
    public void schedule() {
        timer = new Timer("KillTask");
        timer.schedule(new KillProcess(), walltime);
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
        public void run() {
            executable.kill();
        }
    }
}
