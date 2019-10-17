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
package org.ow2.proactive.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Helper class to create executors (not provided by java standard api)
 * @author ActiveEon Team
 * @since 17/10/2019
 */
public class PAExecutors {

    /**
     * Similar to Executors.newCachedThreadPool but allows a maximum pool size.
     * Cached thread pool are interesting in the sense that they can grow and shrink at will.
     * Default cachedThreadPool implementation does not allow to have a maximum capacity
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param timeUnit the time unit for the {@code keepAliveTime} argument
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedBoundedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit timeUnit, ThreadFactory threadFactory) {
        BlockingQueue<Runnable> queue = new LinkedTransferQueue<Runnable>() {
            @Override
            public boolean offer(Runnable e) {
                return tryTransfer(e);
            }
        };
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize,
                                                               maximumPoolSize,
                                                               keepAliveTime,
                                                               timeUnit,
                                                               queue,
                                                               threadFactory);
        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        return threadPool;
    }
}
