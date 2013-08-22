package org.ow2.proactive.threading;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class TimeoutThreadPoolExecutor extends ThreadPoolExecutor {

    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor();

    public TimeoutThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public static TimeoutThreadPoolExecutor newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new TimeoutThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), threadFactory);
    }

    public <T> Future<T> submitWithTimeout(final CallableWithTimeoutAction<T> callable, long timeout,
            TimeUnit unit) {
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
