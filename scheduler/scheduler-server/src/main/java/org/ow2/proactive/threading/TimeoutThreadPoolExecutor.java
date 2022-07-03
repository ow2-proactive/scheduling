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
package org.ow2.proactive.threading;

import java.util.List;
import java.util.concurrent.*;


public class TimeoutThreadPoolExecutor extends ThreadPoolExecutor {

    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();

    public TimeoutThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public static TimeoutThreadPoolExecutor newCachedThreadPool(int nThreads, ThreadFactory threadFactory) {

        BlockingQueue<Runnable> queue = new LinkedTransferQueue<Runnable>() {
            @Override
            public boolean offer(Runnable e) {
                return tryTransfer(e);
            }
        };
        TimeoutThreadPoolExecutor threadPool = new TimeoutThreadPoolExecutor(0,
                                                                             nThreads,
                                                                             120L,
                                                                             TimeUnit.SECONDS,
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

    public <T> Future<T> submitWithTimeout(final CallableWithTimeoutAction<T> callable, long timeout, TimeUnit unit) {
        final Future<T> future = super.submit(callable);
        timeoutExecutor.schedule(new Runnable() {
            public void run() {
                // don't call timeoutAction if future completed or cancelled
                if (future.cancel(true)) {
                    callable.timeoutAction();
                }
            }
        }, timeout, unit);
        return future;
    }

    @Override
    public void shutdown() {
        timeoutExecutor.shutdown();
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        timeoutExecutor.shutdownNow();
        return super.shutdownNow();
    }
}
