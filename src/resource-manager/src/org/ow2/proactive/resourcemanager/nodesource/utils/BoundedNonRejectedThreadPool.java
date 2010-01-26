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
package org.ow2.proactive.resourcemanager.nodesource.utils;

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

    @Override
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

    @Override
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
