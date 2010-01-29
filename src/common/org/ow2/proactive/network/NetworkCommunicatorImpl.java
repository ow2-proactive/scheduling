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
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.network;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * NetworkCommunicatorImpl is the default implementation if the network communicator.
 * This implementation uses fixed thread pool to handle tasks.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class NetworkCommunicatorImpl implements NetworkCommunicator {

    private ExecutorService executor;

    /**
     * Create a new instance of NetworkCommunicatorImpl passing the number of threads to be used.
     * 
     * @param nThreads the number of thread in the thread pool
     */
    public NetworkCommunicatorImpl(int nThreads) {
        executor = Executors.newFixedThreadPool(nThreads);
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
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> Collection<T> execute(Collection<? extends Timed<T>> tasks, long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }

        // do not touch initial collection
        LinkedList<Timed<T>> internalTaskList = new LinkedList<Timed<T>>();
        internalTaskList.addAll(tasks);

        this.execute(internalTaskList);

        Collection<T> results = new LinkedList<T>();
        long timeSpent = 0;
        do {

            // all results are available;
            if (internalTaskList.size() == 0) {
                break;
            }

            long start = System.currentTimeMillis();
            Iterator<? extends Timed<T>> iter = internalTaskList.iterator();

            while (iter.hasNext()) {
                Timed<T> task = iter.next();
                synchronized (task) {
                    if (task.isDone()) {
                        iter.remove();
                        T result = task.getResult();
                        if (result != null) {
                            results.add(result);
                        }
                    }
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            timeSpent += (System.currentTimeMillis() - start);
        } while (timeSpent < timeout);

        RuntimeException pendingException = null;
        for (Timed<T> task : internalTaskList) {
            synchronized (task) {
                try {
                    task.timeoutAction();
                } catch (RuntimeException e) {
                    pendingException = e;
                }
            }
        }

        if (pendingException != null) {
            throw pendingException;
        }

        return results;
    }

}
