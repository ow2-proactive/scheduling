/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.threading;

/**
 * TimedRunnableCallbackWrapper is a wrapper for {@link TimedRunnable} class.<br>
 * It allows a {@link TimedRunnable} task to notify the controller (threadPool) of the end of this task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class TimedRunnableCallbackWrapper implements TimedRunnable {

    private ThreadPoolController communicator;
    private TimedRunnable runnable;

    /**
     * Create a new instance of TimedRunnableCallbackWrapper
     * 
     * @param communicator
     * @param runnable
     */
    TimedRunnableCallbackWrapper(ThreadPoolController communicator, TimedRunnable runnable) {
        this.communicator = communicator;
        this.runnable = runnable;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        runnable.run();
        communicator.taskTerminated(runnable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDone() {
        return runnable.isDone();
    }

    /**
     * {@inheritDoc}
     */
    public void timeoutAction() {
        runnable.timeoutAction();
    }

    /**
     * Get the real task runnable associated with this wrapper
     */
    TimedRunnable getRunnable() {
        return runnable;
    }

}
