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
package org.ow2.proactive.resourcemanager.nodesource;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.utils.NamedThreadFactory;


/**
 * 
 * Class to hold a number of independent thread pools.
 * Provides a synchronous access to pools allowing to submit tasks to them. 
 * Reinitializes all the pools after shutdown automatically. 
 *
 */
public class ThreadPoolHolder {

    private int[] poolSizes;
    private ExecutorService[] pools;

    /**
     * Instantiates the thread pools.
     */
    public ThreadPoolHolder(int[] poolSizes) {
        if (poolSizes == null) {
            throw new NullPointerException("poolSizes cannot be null");
        }
        if (poolSizes.length == 0) {
            throw new IllegalArgumentException("poolSizes must contain al least one element");
        }

        this.poolSizes = poolSizes;
    }

    /**
     * Reinitializes all the pools
     */
    private synchronized void init() {
        this.pools = new ExecutorService[poolSizes.length];

        for (int i = 0; i < poolSizes.length; i++) {
            pools[i] = Executors.newFixedThreadPool(poolSizes[i], new NamedThreadFactory(
                "Node Source threadpool # " + i));
        }
    }

    /**
     * Submits a task to the specified thread pool
     * 
     * @param number of thread pool
     * @param task to be submitted
     * @return the future result of the task
     */
    public synchronized <T> Future<T> submit(int num, Callable<T> task) {
        if (num > poolSizes.length) {
            throw new IllegalArgumentException("Incorrect thread pool number " + num);
        }

        if (pools == null) {
            init();
        }

        return pools[num].submit(task);
    }

    /**
     * Executes a task in the specified thread pool
     * 
     * @param number of the thread pool
     * @param task to be executed
     */
    public synchronized void execute(int num, Runnable task) {
        if (num > poolSizes.length) {
            throw new IllegalArgumentException("Incorrect thread pool number " + num);
        }

        if (pools == null) {
            init();
        }

        pools[num].execute(task);
    }

    /**
     * Shutdown all the thread pools
     * 
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void shutdown() throws InterruptedException {

        if (pools != null) {
            for (ExecutorService es : pools) {
                es.shutdown();
            }
            for (ExecutorService es : pools) {
                es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
            pools = null;
        }
    }
}
