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
package org.ow2.proactive.network;

import java.util.Collection;


/**
 * NetworkCommunicator is the interface describing how to manage network tasks.
 * This contains method allowing non blocking communication utilities.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public interface NetworkCommunicator {

    /**
     * Executes a single tasks in the thread pool.<br>
     * This task is added to the queue of task to execute. The total number of parallel tasks
     * will be the number of working threads (non-blocked).<br>
     * This is a non-blocking method, use {@link NetworkCommunicator#execute(Collection)} method<br>
     * to wait for task to be terminated.
     *
     * @param task the task to be executed
     * @throws IllegalArgumentException if task is null
     */
    public void execute(Runnable task);

    /**
     * Executes the sequence of parallel tasks in the thread pool.<br>
     * The number of parallel tasks will be the number of working threads (non-blocked).<br>
     * This is a non-blocking method, use {@link NetworkCommunicator#execute(Collection, long)} method<br>
     * to get the result of each task if needed.
     *
     * @param tasks the list of tasks to be executed
     * @throws IllegalArgumentException if tasks collection is null
     */
    public void execute(Collection<? extends Runnable> tasks);

    /**
     * Executes the sequence of parallel tasks in the thread pool.<br>
     * Blocks the current thread until timeout is expired or all results are available.<br>
     * This method returns as many results as possible.<br>
     * <br>
     * This method also remove terminated task from the given tasks list. So it remains possible to do an other<br>
     * call with the same reference on the same list that won't contain the previous terminated task anymore.<br>
     * <br>
     * Every tasks that are still running when timeout expires will have their {@link Timed#timeoutAction()} method called.<br>
     *
     * @param tasks a list of tasks on which to get the result
     * @param timeout the maximum amount of time to spend retrieving the results
     * @return a list of every tasks that have terminated, can be an empty list.
     * @throws IllegalArgumentException if tasks collection is null or empty, or if timeout is not positive.
     */
    public <T> Collection<T> execute(Collection<? extends Timed<T>> tasks, long timeout);
}
