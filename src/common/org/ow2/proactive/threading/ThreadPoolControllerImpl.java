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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * ThreadPoolControllerImpl is the default implementation of the threadpool controller.
 * This implementation uses fixed thread pool to handle tasks.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class ThreadPoolControllerImpl implements ThreadPoolController {

    private ExecutorService executor;
    private Map<TimedRunnable, CountDownLatch> countdownByTask;

    /**
     * Create a new instance of ThreadPoolControllerImpl passing the number of threads to be used.
     *
     * @param nThreads the number of thread in the thread pool
     */
    public ThreadPoolControllerImpl(int nThreads) {
        executor = Executors.newFixedThreadPool(nThreads);
        countdownByTask = new ConcurrentHashMap<TimedRunnable, CountDownLatch>();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null.");
        }
        executor.submit(task);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Collection<? extends Runnable> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks collection must not be null.");
        }
        if (tasks.size() == 0) {
            return;
        }
        for (Runnable task : tasks) {
            try {
                executor.execute(task);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T extends TimedRunnable> Collection<T> execute(Collection<T> tasks, long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }

        //create countdownlatch initialized to the number of tasks to execute
        CountDownLatch cdl = new CountDownLatch(tasks.size());
        //convert collection of tasks to collection of runnable callback wrapper
        Collection<TimedRunnableCallbackWrapper> convertedTasks = new ArrayList<TimedRunnableCallbackWrapper>();
        for (T t : tasks) {
            countdownByTask.put(t, cdl);
            convertedTasks.add(new TimedRunnableCallbackWrapper(this, t));
        }

        this.execute(convertedTasks);

        try {
            //wait for the timeout or every task to be terminated
            cdl.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
        }

        //fill result list with terminated task
        Collection<T> results = new LinkedList<T>();
        Iterator<TimedRunnableCallbackWrapper> iter = convertedTasks.iterator();
        while (iter.hasNext()) {
            TimedRunnableCallbackWrapper task = iter.next();
            synchronized (task) {
                if (task.isDone()) {
                    iter.remove();
                    results.add((T) task.getRunnable());
                }
            }
        }

        //call timeout action for every non-terminated task
        RuntimeException pendingException = null;
        for (TimedRunnable task : convertedTasks) {
            synchronized (task) {
                try {
                    task.timeoutAction();
                } catch (RuntimeException e) {
                    pendingException = e;
                }
            }
        }
        //if a problem occurs while calling timeout action
        if (pendingException != null) {
            throw pendingException;
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    public void taskTerminated(TimedRunnable task) {
        //get concerned countdown
        CountDownLatch cdl = countdownByTask.remove(task);
        if (cdl != null) {
            //decrement current count
            cdl.countDown();
        }
    }

}
