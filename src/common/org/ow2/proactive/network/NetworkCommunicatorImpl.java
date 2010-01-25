/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.network;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NetworkCommunicatorImpl implements NetworkCommunicator {

    private ExecutorService executor;

    public NetworkCommunicatorImpl(int nThreads) {
        executor = Executors.newFixedThreadPool(nThreads);
    }

    public void execute(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null.");
        }
        executor.execute(task);
    }

    public void execute(Collection<? extends Runnable> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks collection must not be null.");
        }
        if (tasks.size() == 0) {
            return;
        }
        for (Runnable task : tasks) {
            executor.execute(task);
        }
    }

    public <T> Collection<T> execute(Collection<? extends Timed<T>> tasks, long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }

        this.execute(tasks);

        Collection<T> results = new LinkedList<T>();
        long timeSpent = 0;
        do {

            // all results are available;
            if (tasks.size() == 0) {
                break;
            }

            long start = System.currentTimeMillis();
            Iterator<? extends Timed<T>> iter = tasks.iterator();

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

        for (Timed<T> task : tasks) {
            synchronized (task) {
                task.timeoutAction();
            }
        }

        return results;
    }

}
