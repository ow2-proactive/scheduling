/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.threading;

import java.util.Collection;


/**
 * ThreadPoolController is the interface describing how to manage threaded tasks.
 * This contains method allowing non blocking stuff utilities.
 * The number of threads holding tasks executions is defined by the implementing classes.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public interface ThreadPoolController {

    /**
     * Executes a single tasks in the thread pool.<br>
     * This task is added to the queue of task to execute. The total number of parallel tasks
     * will be the number of working threads (non-blocked).<br>
     * This is a non-blocking method, use {@link ThreadPoolController#execute(Collection)} method<br>
     * to wait for task to be terminated.
     *
     * @param task the task to be executed
     * @throws IllegalArgumentException if task is null
     */
    public void execute(Runnable task);

    /**
     * Executes the sequence of parallel tasks in the thread pool.<br>
     * The number of parallel tasks will be the number of working threads (non-blocked).<br>
     * This is a non-blocking method, use {@link ThreadPoolController#execute(Collection, long)} method<br>
     * to get the result of each task if needed.
     *
     * @param tasks the list of tasks to be executed
     * @throws IllegalArgumentException if tasks collection is null
     */
    public void execute(Collection<? extends Runnable> tasks);

    /**
     * Executes the sequence of parallel tasks in the thread pool.<br>
     * Blocks the current thread until timeout is expired or all results are available.<br>
     * This method returns every terminated tasks (tasks have not had their {@link TimedRunnable#timeoutAction()} called).<br>
     * <br>
     * Every tasks that are still running when timeout expires will have their {@link TimedRunnable#timeoutAction()} method called.
     * IE : each task not present in the returned list.<br>
     *
     * @param tasks a list of tasks to be executed
     * @param timeout the maximum amount of time to spend retrieving the results in milliseconds.
     * @return a list of every tasks that have terminated, can be an empty list. (Each task in this list won't have their
     * 			{@link TimedRunnable#timeoutAction()} method called !
     * @throws IllegalArgumentException if tasks collection is null or empty, or if timeout is not a positive integer.
     */
    public <T extends TimedRunnable> Collection<T> execute(Collection<T> tasks, long timeout);

    /**
     * Used to notify the communicator that a task has terminated (internal use only).
     * 
     * @param task the terminated task.
     */
    void taskTerminated(TimedRunnable task);
}
