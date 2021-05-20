/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.utils.PAExecutors;


/**
 * 
 * Class to hold a number of independent thread pools.
 * Provides a synchronous access to pools allowing to submit tasks to them. 
 *
 */
public class ThreadPoolHolder {

    private ExecutorService[] pools;

    private static final Logger logger = Logger.getLogger(ThreadPoolHolder.class);

    /**
     * Instantiates the thread pools.
     */
    public ThreadPoolHolder(int[] poolSizes, NamedThreadFactory[] threadFactories) {
        if (poolSizes == null || threadFactories == null) {
            throw new NullPointerException("poolSizes cannot be null");
        }
        if (poolSizes.length == 0) {
            throw new IllegalArgumentException("poolSizes must contain at least one element");
        }
        if (poolSizes.length != threadFactories.length) {
            throw new IllegalArgumentException("poolSizes and threadFactories must have the same size");
        }

        this.pools = new ExecutorService[poolSizes.length];
        for (int i = 0; i < poolSizes.length; i++) {
            pools[i] = PAExecutors.newCachedBoundedThreadPool(1,
                                                              poolSizes[i],
                                                              120L,
                                                              TimeUnit.SECONDS,
                                                              threadFactories[i]);
        }
    }

    /**
     * Submits a task to the specified thread pool
     * 
     * @param num thread pool index
     * @param task to be submitted
     * @return the future result of the task
     */
    public synchronized <T> Future<T> submit(int num, Callable<T> task) {
        checkPoolNumber(num);

        return pools[num].submit(task);
    }

    /**
     * Executes a task in the specified thread pool
     *
     * @param num thread pool index
     * @param task to be executed
     */
    public synchronized void execute(int num, Runnable task) {
        checkPoolNumber(num);

        pools[num].execute(task);
    }

    public synchronized void shutdown(int num) {
        checkPoolNumber(num);
        pools[num].shutdown();
        try {
            pools[num].awaitTermination(PAResourceManagerProperties.RM_SHUTDOWN_TIMEOUT.getValueAsLong() - 10,
                                        TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Thread pool termination interrupted");
        }
    }

    private void checkPoolNumber(int num) {
        if (num > pools.length) {
            throw new IllegalArgumentException("Incorrect thread pool number " + num);
        }
    }

}
