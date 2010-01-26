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

import java.util.ArrayList;
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
        executor.execute(task);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Collection<? extends Runnable> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks collection must not be null.");
        }
        for (Runnable task : tasks) {
            executor.execute(task);
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T extends Timed<?>> Collection<T> execute(Collection<T> tasks, long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }

        if (tasks.size() == 0) {
            return new ArrayList<T>();
        }

        //put each tasks in the thread pool queue
        this.execute(tasks);

        Collection<T> results = new LinkedList<T>();
        long timeSpent = 0;
        //wait for all result to be available
        do {
            // all results are available;
            if (tasks.size() == 0) {
                break;
            }

            long start = System.currentTimeMillis();
            Iterator<T> iter = tasks.iterator();

            while (iter.hasNext()) {
                T task = iter.next();
                synchronized (task) {
                    if (task.isDone()) {
                        iter.remove();
                        results.add(task);
                    }
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            timeSpent += (System.currentTimeMillis() - start);
            //time out reached
        } while (timeSpent < timeout);

        //for every non-terminated task, call timeout action
        for (T task : tasks) {
            synchronized (task) {
                task.timeoutAction();
            }
        }

        return results;
    }

}
