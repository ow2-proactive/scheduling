package org.ow2.proactive.resourcemanager.nodesource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ThreadPoolExecutor first tries to run task
 * in a corePool. If all threads are busy it
 * tries to add task to the waiting queue. If it
 * fails it run task in maximumPool.
 *
 * We want corePool to be 0 and
 * maximumPool to be predefined.
 * We need to change the order of the execution.
 * First try corePool then try maximumPool
 * and only then store to the waiting
 * queue. We can not do that because we would
 * need access to the private methods.
 *
 * Instead we enlarge corePool to
 * maxPool size before the execution and
 * shrink it back to 0 after. 
 * It does pretty much what we need.
 *
 * While we changing the corePoolSize we need
 * to stop running worker threads from accepting new
 * tasks.
 */
public class BoundedNonRejectedThreadPool extends ThreadPoolExecutor {

    private final ReentrantLock pauseLock = new ReentrantLock();
    private final Condition unpaused = pauseLock.newCondition();
    private boolean isPaused = false;
    private final ReentrantLock executeLock = new ReentrantLock();

    public BoundedNonRejectedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public void execute(Runnable command) {

        //we need atomicity for the execute method.
        executeLock.lock();
        try {

            pauseLock.lock();
            try {
                isPaused = true;
            } finally {
                pauseLock.unlock();
            }

            setCorePoolSize(getMaximumPoolSize());
            super.execute(command);
            setCorePoolSize(0);

            pauseLock.lock();
            try {
                isPaused = false;
                unpaused.signalAll();
            } finally {
                pauseLock.unlock();
            }
        } finally {
            executeLock.unlock();
        }
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        pauseLock.lock();
        try {
            while (isPaused) {
                unpaused.await();
            }
        } catch (InterruptedException ignore) {

        } finally {
            pauseLock.unlock();
        }
    }

}
