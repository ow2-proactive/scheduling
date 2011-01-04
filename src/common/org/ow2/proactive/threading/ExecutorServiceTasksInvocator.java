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
package org.ow2.proactive.threading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * ExecutorServiceTasksInvocator just contains some static methods to help using {@link ExecutorService}.<br>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public final class ExecutorServiceTasksInvocator {

    /**
     * Executes the given tasks, returning a list of Futures holding their status and results
     * when all complete or the timeout expires, whichever happens first (Blocks the current thread).<br>
     * <br>
     * Every tasks still running (or not started) when timeout expires will have their
     * {@link CallableWithTimeoutAction#timeoutAction()} method called.<br>
     *
     * See {@link ExecutorService#invokeAll(Collection, long, TimeUnit)} for more details
     * about the execution of the tasks and the behavior of the future results list.
     *
     * @param pool the thread pool in which to run the tasks
     * @param tasks the list of tasks to be executed
     * @param timeout the maximum amount of time to spend retrieving the results in milliseconds.
     * @return A list of Futures representing the tasks, in the same sequential order as produced
     * 		   	by the iterator for the given tasks list. If the operation did not time out,
     * 		   	each task will have completed.
     * 		   	If it did time out, some of these tasks will not have completed.<br>
     * 			Returns null if tasks argument was empty, or if the thread calling this method is interrupted.
     * @throws IllegalArgumentException - if pool is null, tasks collection is null,
     * 			or if timeout is not a positive number.
     * @throws NullPointerException - if any of the elements in tasks are null
     * @throws RejectedExecutionException - if any task cannot be scheduled for execution.
     */
    public static <T> List<Future<T>> invokeAllWithTimeoutAction(ExecutorService pool,
            Collection<? extends CallableWithTimeoutAction<T>> tasks, long timeout) {
        //Check tasks is not null
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks collection must not be null.");
        }
        //check tasks is not empty
        if (tasks.size() == 0) {
            return null;
        }
        //
        List<Future<T>> results = null;
        try {
            //invoke tasks and get future results
            results = pool.invokeAll(new ArrayList<Callable<T>>(tasks), timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //if this thread is interrupted, just leave the list results as it is
        }
        //call timeout action on every canceled tasks
        int i = 0;
        Iterator<? extends CallableWithTimeoutAction<T>> iter = tasks.iterator();
        while (iter.hasNext()) {
            //results list and iterator element should be in the same order (specified by jdk5.0)
            CallableWithTimeoutAction<T> callable = iter.next();
            if (results.get(i++).isCancelled()) {
                //if task has been canceled, call timeout on it
                callable.timeoutAction();
            }
        }
        return results;
    }

    /**
     * Submits a list of value-returning task for execution and returns a list of Future representing
     * the pending results of the tasks.<br/>
     *
     * If the given tasks collection contains null element, they just won't be executed.
     *
     * @param pool the thread pool in which to run the tasks
     * @param tasks the list of tasks to be executed
     * @return A list of Futures representing the tasks, in the same sequential order as produced
     * 		   	by the iterator for the given tasks collection.<br>
     * 			Returns null if tasks argument was empty.
     * @throws IllegalArgumentException - if pool is null, or tasks collection is null
     * @throws RejectedExecutionException - if task cannot be scheduled for execution
     */
    public static List<Future<?>> submitAll(ExecutorService pool, Collection<? extends Callable<?>> tasks) {
        //Check tasks is not null
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks collection must not be null.");
        }
        //check tasks is not empty
        if (tasks.size() > 0) {
            //submit each non-null tasks
            List<Future<?>> results = new ArrayList<Future<?>>();
            for (Callable<?> callable : tasks) {
                if (callable != null) {
                    results.add(pool.submit(callable));
                }
            }
            return results;
        }
        return null;
    }

    /**
     * Submits a list of runnable task for execution at some time in the future.<br>
     *
     * If the given tasks collection contains null element, they just won't be executed.
     *
     * @param pool the thread pool in which to run the tasks
     * @param tasks the list of tasks to be executed
     * @throws IllegalArgumentException - if pool is null, or tasks collection is null
     * @throws RejectedExecutionException - if task cannot be scheduled for execution
     */
    public static void executeAll(ExecutorService pool, Collection<? extends Runnable> tasks) {
        //Check tasks is not null
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks collection must not be null.");
        }
        //check tasks is not empty
        if (tasks.size() > 0) {
            //submit each non-null tasks
            for (Runnable runnable : tasks) {
                if (runnable != null) {
                    pool.execute(runnable);
                }
            }
        }
    }

}
