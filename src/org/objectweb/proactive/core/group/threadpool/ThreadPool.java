/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.group.threadpool;

import java.util.ArrayList;

/**
 * A thread pool is a set of threads waiting for jobs. The thread in the pool
 * are created one time and re-used for new jobs. 
 */
public class ThreadPool {

	/** The set of threads. */
	protected Thread[] threads = null;

	/** The queue of pending jobs waiting to be served by a thread. */
	private ArrayList pendingJobs = null;

	/** The controler that is looking for the end of jobs to perform. */
	protected EndControler controler = new EndControler();


	/** Builds a ThreadPool.
	 * By default, the number of thread in the pool is 10.
	 */
	public ThreadPool() {
		this(10);
	}

	/** Builds a ThreadPool, specifying the number of thread to create.
	 * @param <code>size<code> the number of thread in the thread pool. 
	 */
    public ThreadPool(int size) {
        this.threads = new ThreadInThePool[size];
        this.pendingJobs = new ArrayList(size);
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new ThreadInThePool(this);
            this.threads[i].start();
        }
    }


	/** Adds a job to the pending queue of the thread pool. */
	public synchronized void addAJob(Runnable r) {
		this.controler.jobStart();
		this.pendingJobs.add(r);
		this.notify();
	}

	/** Picks up new job to execute in the pending queue.
	 * @return A new job to execute
	 */
	public synchronized Runnable getJobForThePendingQueue() {
		try {
			while (!this.pendingJobs.iterator().hasNext()) {
				this.wait();
			}
			Runnable r = (Runnable) this.pendingJobs.iterator().next();
			this.pendingJobs.remove(r);
			return r; }
		catch (InterruptedException e) {
			this.controler.jobFinish();
			return null;
		}
	}

	/** Waits until the ThreadPool has no more job to execute (pending queue is empty). */
	public void complete() {
	    //this.controler.waitBegin();
		this.controler.waitDone();
	}

	/** Cleanly destroys a ThreadPool object */
	public void finalize() {
		this.controler.reset();
		for (int i = 0; i < threads.length; i++) {
			this.threads[i].interrupt();
			this.controler.jobStart();
			this.threads[i].destroy();
		}
		this.controler.waitDone();
	}

}
