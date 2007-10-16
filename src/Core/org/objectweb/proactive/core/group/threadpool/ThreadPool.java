/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.group.threadpool;

import java.util.ArrayList;

import org.objectweb.proactive.core.group.AbstractProcessForGroup;


/**
 * A thread pool is a set of threads waiting for jobs. The thread in the pool
 * are created one time and re-used for new jobs.
 */
public class ThreadPool {

    /** The set of threads. */
    private Thread[] threads = null;

    /** The queue of pending jobs waiting to be served by a thread. */
    private ArrayList<AbstractProcessForGroup> pendingJobs = null;

    /** The controler that is looking for the end of jobs to perform. */
    protected EndControler controler = new EndControler();

    /** the member to thread ratio, i.e. the number of members served by a single thread */
    private int memberToThreadRatio = 4;

    /** the number of thread added to benefit nultithreading in low number of members */
    private int additionalThreads = 3;

    /** Builds a ThreadPool.
     * By default, the number of thread in the pool is 1.
     */
    public ThreadPool() {
        this(1); //+3); //this.additionalThreads
    }

    /** Builds a ThreadPool, specifying the number of thread to create.
     * @param size - the number of thread in the thread pool.
     */
    public ThreadPool(int size) {
        this.threads = new ThreadInThePool[size];
        this.pendingJobs = new ArrayList<AbstractProcessForGroup>(size);
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new ThreadInThePool(this);
            this.threads[i].start();
        }
    }

    /**
     * Creates the needed threads for this ThreadPool
     * @param number - the number of threads needed
     */
    protected void createThreads(int number) {
        this.threads = new ThreadInThePool[number];
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new ThreadInThePool(this);
            this.threads[i].start();
        }
    }

    /**
     * Check wether the number of threads in this threadpool
     * is sufficient compared to the number of members in the group
     * @param members - the number of members in the group
     */
    public void checkNumberOfThreads(int members) {
        int i;
        int f;

        // System.out.println("ThreadPool: there are " + members + " members in the pool");
        if (this.memberToThreadRatio != 0) {
            f = ((int) Math.ceil(((float) members) / ((float) this.memberToThreadRatio))) +
                this.additionalThreads;
        } else {
            f = this.additionalThreads;
        }

        // System.out.println("ThreadPool: we need " + f + " threads and we have " + this.threads.length);
        if (this.threads.length < f) {
            Thread[] tmp = new Thread[f];
            for (i = 0; i < this.threads.length; i++) {
                tmp[i] = this.threads[i];
            }
            for (; i < f; i++) {
                tmp[i] = new ThreadInThePool(this);
                tmp[i].start();
            }
            this.threads = tmp;
        } else if (this.threads.length > f) {
            Thread[] tmp = new Thread[f];
            for (i = 0; i < f; i++) {
                tmp[i] = this.threads[i];
            }
            for (; i < this.threads.length; i++) {
                this.threads[i].interrupt();
                this.threads[i] = null;
            }
            this.threads = tmp;
        }
    }

    /**
     * Modifies the number of members served by one thread
     * @param i - the new ratio
     */
    public void ratio(int i) {
        this.memberToThreadRatio = i;
    }

    /**
     * Modifies the number of additional threads to serve members
     * @param i - the new number
     */
    public void thread(int i) {
        this.additionalThreads = i;
    }

    /** Adds a job to the pending queue of the thread pool. */
    public synchronized void addAJob(AbstractProcessForGroup r) {
        this.controler.jobStart();
        this.pendingJobs.add(r);
        this.notify();
    }

    /** Picks up new job to execute in the pending queue.
     * @return A new job to execute
     */
    public synchronized Runnable getJobForThePendingQueue() {
        try {
            //        	System.out.println("ThreadPool.getJobForThePendingQueue() currently " + this.pendingJobs.size() + " in the queue" );
            while (!this.pendingJobs.iterator().hasNext()) {
                this.wait();
                //              	System.out.println("ThreadPool.getJobForThePendingQueue() woken currently " + this.pendingJobs.size() + " in the queue" );
            }

            //        	System.out.println("ThreadPool.getJobForThePendingQueue() picking a job from the queue");
            Runnable r = (Runnable) this.pendingJobs.iterator().next();
            this.pendingJobs.remove(r);
            return r;
        } catch (InterruptedException e) {
            this.controler.jobFinish();
            return null;
        }
    }

    /** Waits until the ThreadPool has no more job to execute (pending queue is empty). */
    public void complete() {
        //this.controler.waitBegin();
        this.controler.waitDone();
    }

    //    /** Cleanly destroys a ThreadPool object */
    //    public void finalize() {
    //        this.controler.reset();
    //        for (int i = 0; i < threads.length; i++) {
    //            this.threads[i].interrupt();
    //            this.controler.jobStart();
    //            // this.threads[i].destroy();   // deprecated
    //        }
    //        this.controler.waitDone();
    //    }

    /** Interrupts the thread in the pool */
    public void clean() {
        for (int i = 0; i < threads.length; i++) {
            this.threads[i].interrupt();
        }
    }
}
