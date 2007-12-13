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

/**
 * This object is used by a thread pool to control the termination of the jobs.
 * A member of the ThreadPool class uses this object as a lock and perfoms
 * wait call.
 */
public class EndControler {

    /** The number of active threads currently awake. */
    private int numberOfAwakeThreads = 0;

    /** This boolean keeps track of if the very first thread has started or not. This prevents
     * this object from falsely reporting that the ThreadPool is done, just because the first
     * thread has not yet started.
     */

    //  private boolean started = false;
    /** Suspends the current thread until all the pending jobs in the ThreadPool are done. */
    synchronized public void waitDone() {
        try {
            while (this.numberOfAwakeThreads > 0) {
                this.wait();
            }
        } catch (InterruptedException e) {
            System.err.println("InterruptedException");
        }
    }

    //	/** Waits for the first thread to start. */
    //	synchronized public void waitBegin() {
    ////		Thread.dumpStack();
    //		this.started=true;
    //	//	try {
    //		//	while (!this.started) {
    ////				System.out.println("EndControler.waitBegin() started " + this.started);
    //			//	this.wait();
    //			//} }
    //	//	catch (InterruptedException e) { System.err.println("InterruptedException"); }
    //	}

    /** A ThreadInThePool object calls this method to indicate it has started a job. */
    synchronized public void jobStart() {
        //		Thread.dumpStack();
        //	System.out.println("EndControler.jobStart()");
        this.numberOfAwakeThreads++;
        //	this.started = true;
        //	this.notify();
    }

    /** A ThreadInThePool object calls this method to indicate it has finished a job. */
    synchronized public void jobFinish() {
        // 	System.out.println("EndControler.jobFinish()");
        //    	Thread.dumpStack();
        this.numberOfAwakeThreads--;
        this.notify();
    }

    /** Resets the controler to its initial state (no job awake). */
    synchronized public void reset() {
        this.numberOfAwakeThreads = 0;
    }
}
